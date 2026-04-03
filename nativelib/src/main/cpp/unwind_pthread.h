//
// Created by liguang on 2026/4/1.
//

#ifndef STABILITY_UNWIND_PTHREAD_H
#define STABILITY_UNWIND_PTHREAD_H

#include <asm-generic/fcntl.h>
#include <sys/stat.h>
#include <fcntl.h>

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <sys/mman.h>
#include <elf.h>
#include <ctype.h>
#include <dlfcn.h>
#include <malloc.h>
#include <android/log.h>

#define LOG_TAG "unwind_pthread"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define LR_REG 30
#define FP_REG 29

// -----------------------------------------------------------------------------
// 1. 工具：获取 libc.so 基地址
// -----------------------------------------------------------------------------
uint64_t get_libc_base() {
    int fd = open("/apex/com.android.runtime/lib64/bionic/libc.so", O_RDONLY);
    if (fd < 0) {
        LOGD("[ELF] 读取 path %s fd < 0\n");
        return -1;
    }

    struct stat st;
    fstat(fd, &st);
    void *data = mmap(0, st.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
    if (data) {
        return (uint64_t)data;
    }


    Dl_info info;
    void* sym = dlsym(RTLD_DEFAULT, "pthread_setspecific");
    if (!sym) return 0;
    if (dladdr(sym, &info) == 0) return 0;
    return (uint64_t)info.dli_fbase;
}

// -----------------------------------------------------------------------------
// 2. 工具：从 ELF 中获取 .eh_frame
// -----------------------------------------------------------------------------
bool get_eh_frame(uint64_t lib_base, uint8_t** out_eh, size_t* out_len) {
    Elf64_Ehdr* eh = (Elf64_Ehdr*)lib_base;
    Elf64_Shdr* sh = (Elf64_Shdr*)(lib_base + eh->e_shoff);
    LOGD("[ELF] get_eh_frame %d \n", eh->e_shnum);

    for (int i = 0; i < eh->e_shnum; i++) {
        const char* name = (const char*)(lib_base + sh[eh->e_shstrndx].sh_offset + sh[i].sh_name);
        LOGD("[ELF] get_eh_frame %s \n", name);
        if (strcmp(name, ".eh_frame") == 0) {
            *out_eh = (uint8_t*)malloc(sh[i].sh_size);
            memcpy(*out_eh, (void*)(lib_base + sh[i].sh_offset), sh[i].sh_size);
            *out_len = sh[i].sh_size;
            return true;
        }
    }
    return false;
}

// -----------------------------------------------------------------------------
// 3. 手动解析 ULEB128（DWARF 必备）
// -----------------------------------------------------------------------------
uint64_t read_uleb(const uint8_t** p) {
    uint64_t res = 0;
    int shift = 0;
    uint8_t b;
    do {
        b = *(*p)++;
        res |= (b & 0x7F) << shift;
        shift += 7;
    } while (b & 0x80);
    return res;
}

// -----------------------------------------------------------------------------
// 4. 手动解析 .eh_frame 寻找 pthread_key_create 的 FDE
// -----------------------------------------------------------------------------
bool find_pthread_fde(
        uint64_t pc,
        const uint8_t* eh_frame, size_t eh_len,
        uint64_t* out_cfa, int* out_lr_off)
{
    LOGD("find_pthread_fde eh_frame %lx eh_len %d\n", eh_frame, eh_len);
    const uint8_t* p = eh_frame;
    int index = 0;
    while (p < eh_frame + eh_len - 8) {
        uint32_t len = *(uint32_t*)p;
        uint32_t id  = *(uint32_t*)(p + 4);
        const uint8_t* entry = p + 8;
        p += 8 + len;

        if (id == 0) continue; // CIE

        // 读取 FDE 地址范围
        const uint8_t* d = entry;
        uint64_t start = read_uleb(&d);
        uint64_t range = read_uleb(&d);
        uint64_t end = start + range;

        LOGD("[%d]    函数范围: 0x%lx ~ 0x%lx\n", index++, start, end);

        // 判断是否是 pthread_key_create
        if (pc >= start && pc < end) {
            LOGD("[+] 找到 pthread_key_create FDE\n");
            LOGD("    函数范围: 0x%lx ~ 0x%lx\n", start, end);

            // 执行 DWARF 指令（手动模拟 unwinder）
            uint64_t cfa = 0;
            int lr_off = 0;
            while (d < p) {
                uint8_t op = *d++;
                if (op >= 0x40 && op < 0x80) continue;
                if (op == 0x0C) { // DW_CFA_def_cfa
                    uint32_t reg = read_uleb(&d);
                    uint64_t off = read_uleb(&d);
                    cfa = off; // libc 几乎都是 SP 基址
                    LOGD("    CFA = SP + 0x%lx\n", cfa);
                } else if ((op & 0xC0) == 0x80) { // DW_CFA_offset
                    uint32_t reg = op & 0x3F;
                    int64_t o = read_uleb(&d) * -8;
                    if (reg == LR_REG) {
                        lr_off = o;
                        LOGD("    LR 保存于 CFA %+d\n", lr_off);
                    }
                }
            }

            *out_cfa = cfa;
            *out_lr_off = lr_off;
            return true;
        }
    }
    return false;
}

// -----------------------------------------------------------------------------
// 5. 手动解析 pthread_key_create 的 .eh_frame（主函数）
// -----------------------------------------------------------------------------
int main() {
    // 1. 获取 libc 基址
    uint64_t libc_base = get_libc_base();
    LOGD("libc 基址: 0x%lx\n", libc_base);

    // 2. 获取 pthread_key_create 地址
    void* sym = dlsym(RTLD_DEFAULT, "pthread_setspecific");
    uint64_t pc = (uint64_t)sym;
    Dl_info info;
    dladdr(sym, &info);
    pc = (uint64_t)info.dli_saddr - (uint64_t)info.dli_fbase;
    LOGD("pthread_key_create: 0x%lx\n", pc);

    // 3. 读取 .eh_frame
    uint8_t* eh_frame;
    size_t eh_len;
    if (!get_eh_frame(libc_base, &eh_frame, &eh_len)) {
        LOGD("获取 eh_frame 失败\n");
        return -1;
    }

    // 4. 手动解析 FDE
    uint64_t cfa_off;
    int lr_off;
    if (find_pthread_fde(pc, eh_frame, eh_len, &cfa_off, &lr_off)) {
        LOGD("\n=== 最终栈展开规则（pthread_key_create）===\n");
        LOGD("CFA = SP + 0x%lx\n", cfa_off);
        LOGD("LR = *(CFA %+d)\n", lr_off);
        LOGD("上一层返回地址 = 栈[SP + 0x%lx %+d]\n", cfa_off, lr_off);
    }

    free(eh_frame);
    return 0;
}


#ifdef __cplusplus
}
#endif


#endif //STABILITY_UNWIND_PTHREAD_H
