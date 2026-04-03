//
// Created by liguang on 2026/4/1.
//

#ifndef STABILITY_UNWIND_MANUL_H
#define STABILITY_UNWIND_MANUL_H

#pragma once

#include <ctype.h>

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <elf.h>
#include <android/log.h>

#define LOG_TAG "unwind_manul"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define ARM64_NR_REGS 32
#define LR_REG_NUM 30
#define FP_REG_NUM 29

typedef struct {
    uint64_t pc;
    uint64_t sp;
    uint64_t regs[ARM64_NR_REGS];
} regs_t;

typedef struct {
    uint64_t cfa;
    uint64_t reg_off[ARM64_NR_REGS];
    int reg_valid[ARM64_NR_REGS];
} cfa_state_t;

typedef struct {
    uint64_t start;
    uint64_t end;
    const uint8_t *ins;
    size_t ins_len;
} fde_t;

typedef struct frame {
    uint64_t pc;
    struct frame *next;
} frame_t;

// ------------------------------
// 日志颜色（方便查看）
// ------------------------------
#define NONE "\033[0m"
#define RED "\033[31m"
#define GREEN "\033[32m"
#define YELLOW "\033[33m"
#define BLUE "\033[34m"


void get_current_pc(uint64_t* pc) {
    *pc = (uint64_t)__builtin_return_address(0);
//    __asm__ volatile (
//            "mov x8, lr\n"
//            "bl 1f\n"
//            "1:\n"
//            "str lr, [%0]\n"
//            "mov lr, x8\n"
//            : "=r"(pc)
//            :
//            : "x8", "lr"
//            );
}

// ------------------------------
// 1. 获取当前寄存器
// ------------------------------
void get_current_regs(regs_t *r) {
    memset(r, 0, sizeof(regs_t));
//    __asm__ volatile ("mov %0, pc" : "=r"(r->pc));
    get_current_pc(&(r->pc));
    __asm__ volatile ("mov %0, sp" : "=r"(r->sp));
    __asm__ volatile ("mov %0, x29" : "=r"(r->regs[29]));
    __asm__ volatile ("mov %0, x30" : "=r"(r->regs[30]));
}

// ------------------------------
// 2. ULEB128 解析
// ------------------------------
static uint64_t read_uleb128(const uint8_t **p) {
    uint64_t res = 0;
    int shift = 0;
    uint8_t byte;
    do {
        byte = *(*p)++;
        res |= (byte & 0x7F) << shift;
        shift += 7;
    } while (byte & 0x80);
    return res;
}

// ------------------------------
// 3. DWARF CFA 解释器 + 日志
// ------------------------------
void execute_dwarf(
        const uint8_t *ins, size_t len,
        uint64_t target_pc, uint64_t func_start,
        const regs_t *regs, cfa_state_t *state
) {
    const uint8_t *p = ins;
    const uint8_t *end = p + len;
    uint64_t current_loc = 0;
    memset(state, 0, sizeof(cfa_state_t));
    state->cfa = regs->sp;

    LOGD(GREEN "\n[DWARF] 执行展开规则，目标PC=0x%lx\n" NONE, target_pc);
    LOGD(GREEN "[DWARF] 初始SP=0x%lx 初始FP=0x%lx\n" NONE, regs->sp, regs->regs[29]);

    while (p < end) {
        uint8_t op = *p++;
        if (op >= 0x40 && op < 0x7F) {
            current_loc += (op - 0x40) * 4;
            continue;
        }

        switch (op) {
            case 0x0C: { // DW_CFA_def_cfa
                uint32_t reg = read_uleb128(&p);
                uint64_t off = read_uleb128(&p);
                state->cfa = regs->regs[reg] + off;
                LOGD(BLUE "[DWARF] CFA = x%d + %lu → 0x%lx\n" NONE, reg, off, state->cfa);
                break;
            }
            case 0x0E: { // DW_CFA_def_cfa_offset
                uint64_t off = read_uleb128(&p);
                state->cfa = (state->cfa & ~0xFFFFFFFFFFFFULL) + off;
                LOGD(BLUE "[DWARF] CFA 偏移更新 → 0x%lx\n" NONE, state->cfa);
                break;
            }
            case 0x80 ... 0xBF: { // DW_CFA_offset
                uint32_t reg = op & 0x3F;
                int64_t off = read_uleb128(&p) * -8;
                state->reg_off[reg] = off;
                state->reg_valid[reg] = 1;
                LOGD(BLUE "[DWARF] x%d 保存在 CFA %+lld\n" NONE, reg, off);
                break;
            }
            case 0x00: break;
            default: break;
        }

        uint64_t abs_pc = func_start + current_loc;
        if (abs_pc > target_pc) break;
    }
}

// ------------------------------
// 4. 查找 FDE
// ------------------------------
int find_fde(uint64_t pc, const uint8_t *eh_frame, size_t eh_len, fde_t *fde) {
    const uint8_t *p = eh_frame;
    while (p < eh_frame + eh_len - 8) {
        uint32_t len = *(uint32_t *)p;
        uint32_t id = *(uint32_t *)(p + 4);
        const uint8_t *entry = p + 8;
        p += 8 + len;
        if (id == 0) continue;

        const uint8_t *d = entry;
        uint64_t start = read_uleb128(&d);
        uint64_t range = read_uleb128(&d);
        uint64_t end = start + range;

        if (pc >= start && pc < end) {
            fde->start = start;
            fde->end = end;
            fde->ins = d;
            fde->ins_len = p - d;
            LOGD(YELLOW "\n[FDE] 找到匹配函数：0x%lx ~ 0x%lx\n" NONE, start, end);
            return 0;
        }
    }
    return -1;
}

// ------------------------------
// 5. 从 ELF 读取 .eh_frame
// ------------------------------
int read_eh_frame(const char *path, void *mapFile, uint8_t **out_eh, size_t *out_len) {
//    int fd = open(path, O_RDONLY);
//    if (fd < 0) {
//        LOGD(YELLOW "[ELF] 读取 path %s fd < 0\n" NONE, path);
//        return -1;
//    }
//    struct stat st;
//    fstat(fd, &st);
    void *data = mapFile;
//    void *data = mmap(0, st.st_size, PROT_READ, MAP_PRIVATE, fd, 0);

    Elf64_Ehdr *eh = (Elf64_Ehdr *)data;
    Elf64_Shdr *sh = (Elf64_Shdr *)((uint8_t *)data + eh->e_shoff);
    for (int i = 0; i < eh->e_shnum; i++) {
        if (strcmp((const char *)data + sh[eh->e_shstrndx].sh_offset + sh[i].sh_name, ".eh_frame") == 0) {
            *out_eh = static_cast<uint8_t *>(malloc(sh[i].sh_size));
            memcpy(*out_eh, (uint8_t *)data + sh[i].sh_offset, sh[i].sh_size);
            *out_len = sh[i].sh_size;
//            munmap(data, st.st_size);
//            close(fd);
            LOGD(YELLOW "[ELF] 读取 .eh_frame 成功，大小=%zu 字节\n" NONE, *out_len);
            return 0;
        }
    }
//    munmap(data, st.st_size);
//    close(fd);
    return -1;
}

bool parse_maps_line(const char* line,
                     uint64_t* start, uint64_t* end,
                     char* perm, uint64_t* offset,
                     char* dev, uint64_t* inode,
                     char* path, size_t path_len)
{
    // 跳过前面空白
    while (*line && isspace(*line)) line++;
    if (*line == 0) return false;

    // --------------------------
    // 解析前 6 个固定字段
    // --------------------------
    int ret = sscanf(line,
                     "%lx-%lx %5s %lx %5s %lu",
                     start, end, perm, offset, dev, inode);
    if (ret != 6) return false;

    // --------------------------
    // 跳到第6个字段结束的位置
    // --------------------------
    for (int i = 0; i < 6; ++i) {
        while (*line && !isspace(*line)) line++;
        while (*line && isspace(*line)) line++;
    }

    // --------------------------
    // 剩下全部 = 路径（含空格）
    // --------------------------
    strncpy(path, line, path_len - 1);
    path[path_len - 1] = 0;

    // 去掉行尾换行
    size_t len = strlen(path);
    while (len > 0 && (path[len-1] == '\n' || path[len-1] == '\r')) {
        path[--len] = 0;
    }

    return true;
}

// 功能：从 /proc/self/maps 一行中，提取最后的 path
// line：输入的一行
// out_path：输出完整路径
// max_len：输出缓冲区大小
void get_maps_path(const char* line, char* out_path, int max_len) {
    if (!line || !out_path || max_len <= 0) return;

    // 跳过前 6 个字段（固定格式）
    int field = 0;
    while (*line && field < 5) {
        // 跳过一段内容
        while (*line && !isspace((unsigned char)*line)) line++;
        // 跳过空格
        while (*line && isspace((unsigned char)*line)) line++;
        field++;
    }

    // 剩下的全部就是路径（包括空格）
    strncpy(out_path, line, max_len - 1);
    out_path[max_len - 1] = '\0';

    // 去掉换行符
    char* end = out_path + strlen(out_path);
    while (end > out_path && (*(end - 1) == '\n' || *(end - 1) == '\r')) {
        *(--end) = '\0';
    }
}

// ------------------------------
// 6. 查找 PC 所在 SO
// ------------------------------
int find_so_for_pc(uint64_t pc, char *path, size_t path_size) {
    FILE *f = fopen("/proc/self/maps", "r");
    if (!f) return -1;
    char line[512], perms[8], map[16], dev[16];
    uint64_t start, end, offset, inode;
    LOGD(GREEN "\n[MAPS] 开始 PC=0x%lx \n" NONE, pc);
    while (fgets(line, sizeof(line), f)) {
        LOGD(GREEN "\n[MAPS] line=%s PC=0x%lx\n" NONE, line, pc);
//        if (!parse_maps_line(line, &start, &end, perms, &offset, dev, &inode, path, path_size)) {
//            continue;
//        }
//        if (sscanf(line, "%lx-%lx %s %s %s %s %s", &start, &end, perms, offset, dev, inode, path) < 7) continue;
//        LOGD(GREEN "\n[MAPS] start=0x%lx, end=0x%lx , PC=0x%lx 潜在 SO: %s\n" NONE, start, end, pc, path);
        get_maps_path(line, path, path_size);
        LOGD(GREEN "\n[MAPS] path=%s PC=0x%lx\n" NONE, path, pc);
//        if (strstr(path, "libnativelib.so")) {
        if (pc >= start && pc < end) {
            fclose(f);
            LOGD(GREEN "\n[MAPS] PC=0x%lx 位于 SO: %s\n" NONE, pc, line);
            return 0;
        }
    }
    fclose(f);
    return -1;
}

// ------------------------------
// 7. Unwind 一层（全日志）
// ------------------------------
int unwind_once(regs_t *in) {
    char path[256] = {0};
//    if (find_so_for_pc(in->pc, path, sizeof(path))) return -1;

    Dl_info info;
    if (!dladdr((void *)in->pc, &info)) return -1;
    LOGD(RED "[Unwind] dli_fname %s info.dli_fbase 0x%lx \n" NONE, info.dli_fname, info.dli_fbase);
    strncpy(path, info.dli_fname, sizeof(path));

    uint8_t *eh = NULL;
    size_t eh_len = 0;
    if (read_eh_frame(path, info.dli_fbase, &eh, &eh_len)) return -1;

    fde_t fde;
    if (find_fde(in->pc, eh, eh_len, &fde)) { free(eh); return -1; }

    cfa_state_t state;
    execute_dwarf(fde.ins, fde.ins_len, in->pc, fde.start, in, &state);

    if (!state.reg_valid[LR_REG_NUM]) {
        LOGD(RED "[Unwind] LR 无效\n" NONE);
        free(eh);
        return -1;
    }

    // 读取 LR（返回地址）
    uint64_t lr_addr = state.cfa + state.reg_off[LR_REG_NUM];
    uint64_t lr_val = *(uint64_t *)lr_addr;

    LOGD(GREEN "\n[Unwind] LR 地址 = CFA %+lld = 0x%lx\n" NONE, state.reg_off[LR_REG_NUM], lr_addr);
    LOGD(GREEN "[Unwind] 读取栈值 = 0x%lx (上一层PC)\n" NONE, lr_val);

    in->pc = lr_val;
    in->sp = state.cfa;

    // 恢复 FP
    if (state.reg_valid[FP_REG_NUM]) {
        uint64_t fp_addr = state.cfa + state.reg_off[FP_REG_NUM];
        in->regs[FP_REG_NUM] = *(uint64_t *)fp_addr;
    }

    free(eh);
    return 0;
}

// ------------------------------
// 8. 完整回溯
// ------------------------------
frame_t *unwind_full() {
    regs_t regs;
    get_current_regs(&regs);
    frame_t *first = NULL, **prev = &first;

    LOGD(BLUE "\n==================== 开始 Unwind ====================\n" NONE);

    while (1) {
        frame_t *fr = static_cast<frame_t *>(malloc(sizeof(frame_t)));
        fr->pc = regs.pc;
        fr->next = NULL;
        *prev = fr;
        prev = &fr->next;

        LOGD(BLUE "\n--------------------------------------------------------\n" NONE);
        LOGD(BLUE "当前栈帧 PC=0x%lx\n" NONE, fr->pc);

        if (unwind_once(&regs) < 0) {
            LOGD(RED "\n[Unwind] 结束：无法继续回溯\n" NONE);
            break;
        }
        if (regs.pc == 0 || regs.pc < 0x1000) break;
    }

    LOGD(BLUE "==================== Unwind 结束 ====================\n\n" NONE);
    return first;
}

// ------------------------------
// 9. 打印最终栈
// ------------------------------
void print_stack(frame_t *f) {
    LOGD("==================== 调用栈结果 ====================\n");
    int i = 0;
    while (f) {
        Dl_info info;
        if (dladdr((void *)f->pc, &info) && info.dli_sname) {
            LOGD("#%d 0x%016lx %s\n", i++, f->pc, info.dli_sname);
        } else {
            LOGD("#%d 0x%016lx\n", i++, f->pc);
        }
        f = f->next;
    }
    LOGD("=====================================================\n");
}

// ------------------------------
// 测试
// ------------------------------
int main() {
    frame_t *stack = unwind_full();
    print_stack(stack);
    return 0;
}

#ifdef __cplusplus
}
#endif

#endif //STABILITY_UNWIND_MANUL_H
