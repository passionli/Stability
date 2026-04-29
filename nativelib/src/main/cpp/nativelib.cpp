
#include "PthreadKeyOpt.h"
#include "unwind_pthread.h"

#include <jni.h>
#include <string>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h> // 包含 sleep 函数的头文件
#include <android/log.h>

#include "xdl.h"
#include "shadowhook.h"
#include "ArtJavaHook.h"
#include "NativeCrashMonitor.h"

#define LOG_TAG "KEY_MONITOR"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

int callback(struct dl_phdr_info * info, size_t size, void * data) {
    if (info->dlpi_name != nullptr) {
        LOGD("Found: %s", info->dlpi_name);
        LOGD("Base: 0x%tx", info->dlpi_addr);

        int ret = 0;
        {
            void * h = xdl_open(info->dlpi_name, XDL_DEFAULT);
            size_t symbol_size = 0;
//            const char *symbol = "_ZL7key_map";
            const char *symbol = "_ZN3art13gLogVerbosityE";
            void * sym = xdl_sym(h, symbol, &symbol_size);
            if (sym != nullptr) {
                LOGD("xdl_sym %s: addr=%p, size=%zu", symbol, sym, symbol_size);
                memset((void *)sym, 0x1, symbol_size / sizeof (uint8_t));
                ret = 1;
            } else {
                sym = xdl_dsym(h, symbol, &symbol_size);
                if (sym != nullptr) {
                    LOGD("xdl_dsym %s: addr=%p, size=%zu", symbol, sym, symbol_size);
                memset((void *)sym, 0x1, symbol_size / sizeof (uint8_t));
                    ret = 1;
                }
            }
            xdl_close(h);
        }

        return ret; // 找到即终止
    }
    return 0;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativelib_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    xdl_iterate_phdr(callback, NULL, XDL_FULL_PATHNAME);

    PthreadKeyOpt::getInstance().start();
    ArtJavaHook::getInstance().start(env);
    NativeCrashMonitor::getInstance().start(env);



    
//    {
//        // 直接崩溃，不需要多余代码
//        *(int*)0 = 0;
//    }
//
//    {
//        int arr[5] = {0};
//
//        // 越界写内存 → SIGSEGV
//        arr[999] = 1234;
//    }
//
//    {
//        int* ptr = new int(10);
//
//        // 释放内存
//        delete ptr;
//
//        // 再次访问 → 野指针崩溃
//        *ptr = 20;
//    }

    return env->NewStringUTF(hello.c_str());
}


// 打印一个函数指针在 GOT 里真实对应的 SO 和偏移
static void print_got_symbol(void* func_ptr, const char* name) {
    Dl_info info;
    if (!dladdr(func_ptr, &info)) {
        LOGD("dladdr failed\n");
        return;
    }

    LOGD("[%s]\n", name);
    LOGD("  所属SO: %s\n", info.dli_fname);
    LOGD("  基地址: %p\n", info.dli_fbase);
    LOGD("  符号名: %s\n", info.dli_sname);
    LOGD("  符号地址: %p\n", info.dli_saddr);
    LOGD("  偏移(符号 - SO基址): %lx\n", (unsigned long)((uintptr_t)info.dli_saddr - (uintptr_t)info.dli_fbase));
    LOGD("----------------------------------------\n");
}


#include <pthread.h>
#include <stdint.h>
#include <errno.h>
#include <string.h>

// 获取当前线程 SP (栈顶)
uintptr_t get_current_sp() {
    uintptr_t sp;
    // ARM64 读取 sp 寄存器
    __asm__ __volatile__("mov %0, sp" : "=r"(sp));
    return sp;
}

// 获取当前线程 栈底地址 + 栈大小
// out_stack_base: 输出栈底（高地址）
// out_stack_size: 输出栈总大小
int get_current_thread_stack(uintptr_t *out_stack_base, size_t *out_stack_size) {
    if (!out_stack_base || !out_stack_size) {
        return -1;
    }

    pthread_attr_t attr;
    // 初始化线程属性
    int ret = pthread_attr_init(&attr);
    if (ret != 0) {
        LOGD("pthread_attr_init failed: %s", strerror(errno));
        return ret;
    }

    // 获取当前线程属性 (np = non-portable, Android 支持)
    ret = pthread_getattr_np(pthread_self(), &attr);
    if (ret != 0) {
        LOGD("pthread_getattr_np failed: %s", strerror(errno));
        pthread_attr_destroy(&attr);
        return ret;
    }

    void *stack_addr = NULL;
    size_t stack_size = 0;
    // 获取栈基址(低地址) + 栈大小
    ret = pthread_attr_getstack(&attr, &stack_addr, &stack_size);
    if (ret != 0) {
        LOGD("pthread_attr_getstack failed: %s", strerror(errno));
        pthread_attr_destroy(&attr);
        return ret;
    }

    // ==============================
    // ARM64 栈：向下生长
    // 栈底(最高地址) = 栈基址 + 栈大小
    // ==============================
    *out_stack_base = (uintptr_t)stack_addr + stack_size;
    *out_stack_size = stack_size;

    // 销毁属性
    pthread_attr_destroy(&attr);
    return 0;
}


void print_thread_stack_info() {
    // 1. 获取栈底、栈大小
    uintptr_t stack_base;
    size_t stack_size;
    if (get_current_thread_stack(&stack_base, &stack_size) != 0) {
        return;
    }

    // 2. 获取当前 SP
    uintptr_t sp = get_current_sp();

    // 3. 计算栈最低地址
    uintptr_t stack_bottom = stack_base - stack_size;

    // 4. 输出
    LOGD("========== 当前线程栈信息(ARM64) ==========");
    LOGD("栈底(最高地址) : 0x%lx", stack_base);
    LOGD("栈顶(当前SP)   : 0x%lx", sp);
    LOGD("栈最低地址     : 0x%lx", stack_bottom);
    LOGD("栈总大小       : %zu bytes (%.2f MB)", stack_size, stack_size / 1024.0f / 1024.0f);
    LOGD("栈剩余空间     : %zu bytes", sp - stack_bottom);
}


#include <stdint.h>
#include <stdbool.h>

typedef struct {
    uintptr_t cfa;
    uintptr_t regs[32];
} UnwindContext;

// 你从 eh_frame 里解析出来的规则
void execute_dwarf_instructions(UnwindContext *ctx) {
    // 来自你的 FDE：
    // DW_CFA_def_cfa: r29, 48
    ctx->cfa = ctx->regs[29] + 48;

    // DW_CFA_offset: r30 at cfa-40
    uintptr_t lr_address = ctx->cfa - 40;
    ctx->regs[30] = *(uintptr_t*)lr_address;
}

// 手动 unwind 一次，得到上一层 LR
uintptr_t manual_unwind_once() {
    UnwindContext ctx;
    // 填入当前寄存器
    __asm__ __volatile__("mov %0, x29" : "=r"(ctx.regs[29]));
    __asm__ __volatile__("mov %0, x30" : "=r"(ctx.regs[30]));

    execute_dwarf_instructions(&ctx);

    // 得到上一层返回地址
    return ctx.regs[30];
}

#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>
#include <dlfcn.h>
#include <unwind.h>
#include <pthread.h>

#define LOG_TAG "UNWIND_STABLE"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// ==============================
// 稳定栈回溯（不依赖 FP，兼容所有优化 SO）
// ==============================

#define MAX_STACK_FRAMES 32

typedef struct {
    uintptr_t frames[MAX_STACK_FRAMES];
    size_t count;
} BacktraceContext;

static _Unwind_Reason_Code unwind_callback(struct _Unwind_Context *ctx, void *arg) {
    BacktraceContext *bt = (BacktraceContext *)arg;
    if (bt->count >= MAX_STACK_FRAMES) {
        return _URC_END_OF_STACK;
    }

    // 获取指令地址（不依赖 FP！）
    uintptr_t ip = _Unwind_GetIP(ctx);
    if (ip == 0) {
        return _URC_END_OF_STACK;
    }

    bt->frames[bt->count++] = ip;
    return _URC_NO_REASON;
}

// 安全获取调用栈（核心函数）
size_t safe_backtrace(uintptr_t *out_frames, size_t max_frames) {
    BacktraceContext ctx = {
            .count = 0
    };
    _Unwind_Backtrace(unwind_callback, &ctx);

    size_t copy = (ctx.count < max_frames) ? ctx.count : max_frames;
    memcpy(out_frames, ctx.frames, copy * sizeof(uintptr_t));
    return copy;
}

// 打印符号（dladdr）
void print_symbol(uintptr_t addr) {
    Dl_info info;
    if (dladdr((void *)addr, &info)) {
        const char *soname = info.dli_fname ? strrchr(info.dli_fname, '/') : "unknown";
        if (soname) soname++;
        const char *symname = info.dli_sname ? info.dli_sname : "??";
        LOGD("    [%s] %s + 0x%lx", soname, symname, addr - (uintptr_t)info.dli_saddr);
    } else {
        LOGD("    [unknown]");
    }
}

// ==============================
// 对外接口：打印完整调用栈（兼容所有 SO）
// ==============================
void print_safe_backtrace() {
    uintptr_t frames[MAX_STACK_FRAMES];
    size_t count = safe_backtrace(frames, MAX_STACK_FRAMES);

    LOGD("================ SAFE BACKTRACE ================");
    for (size_t i = 0; i < count; i++) {
        LOGD("#%zu 0x%lx", i, frames[i]);
        print_symbol(frames[i]);
    }
    LOGD("================================================");
}


#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>
#include <pthread.h>
#include <errno.h>
#include <dlfcn.h>

#define LOG_TAG "STACK_MEM_SYM"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// ==========================
// 1. 获取 SP（栈顶）
// ==========================
uintptr_t get_sp() {
    uintptr_t sp;
    __asm__ __volatile__("mov %0, sp" : "=r"(sp));
    return sp;
}

// ==========================
// 2. 获取线程栈范围（栈底+大小）
// ==========================
int get_thread_stack(uintptr_t *stack_base, uintptr_t *stack_low, size_t *stack_size) {
    pthread_attr_t attr;
    if (pthread_attr_init(&attr) != 0) return -1;
    if (pthread_getattr_np(pthread_self(), &attr) != 0) {
        pthread_attr_destroy(&attr);
        return -1;
    }

    void *addr = NULL;
    size_t size = 0;
    if (pthread_attr_getstack(&attr, &addr, &size) != 0) {
        pthread_attr_destroy(&attr);
        return -1;
    }
    pthread_attr_destroy(&attr);

    *stack_low = (uintptr_t)addr;
    *stack_base = (uintptr_t)addr + size;
    *stack_size = size;
    return 0;
}

// ==========================
// 3. 格式化打印：地址 + 8字节内存 + dladdr符号(so+偏移)
// ==========================
void print_mem_with_symbol(uintptr_t addr) {
    // 读取 8 字节内存
    uint64_t val = *(uint64_t *)addr;

    // 内存十六进制
    char hex[32];
    snprintf(hex, sizeof(hex), "%016lx", val);

    // ASCII 显示
    char ascii[9] = {0};
    for (int i = 0; i < 8; i++) {
        uint8_t b = (val >> (i * 8)) & 0xFF;
        ascii[i] = (b >= 32 && b < 127) ? b : '.';
    }

    // ==========================
    // dladdr 解析符号（关键）
    // ==========================
    Dl_info info;
    char sym_buf[256] = "[unknown]";
    if (dladdr((void *)addr, &info)) {
        // SO 名称
        const char *soname = info.dli_fname ? strrchr(info.dli_fname, '/') : NULL;
        if (soname) soname++;
        else soname = "unknown";

        // 符号名 + 偏移
        if (info.dli_sname) {
            uintptr_t offset = addr - (uintptr_t)info.dli_saddr;
            snprintf(sym_buf, sizeof(sym_buf), "[%s] %s + 0x%lx",
                     soname, info.dli_sname, offset);
        } else {
            snprintf(sym_buf, sizeof(sym_buf), "[%s] + 0x%lx",
                     soname, addr - (uintptr_t)info.dli_fbase);
        }
    }

    // 输出一行
    LOGD("0x%lx: %s | %s | %s",
         addr, hex, ascii, sym_buf);
}

// ==========================
// 4. 主函数：打印 SP → 栈底 所有内存 + 符号
// ==========================
void dump_stack_mem_to_base_with_symbols() {
    uintptr_t sp = get_sp();
    uintptr_t stack_base, stack_low, stack_size;

    if (get_thread_stack(&stack_base, &stack_low, &stack_size) != 0) {
        LOGD("get stack failed");
        return;
    }

    LOGD("==========================================================");
    LOGD("    STACK MEMORY + DLADDR SYMBOL (SP → STACK BASE)");
    LOGD("==========================================================");
    LOGD("SP      = 0x%lx", sp);
    LOGD("STACK   = 0x%lx ~ 0x%lx  SIZE = %zu KB",
         stack_low, stack_base, stack_size / 1024);
    LOGD("==========================================================\n");

    // 安全判断
    if (sp < stack_low || sp >= stack_base) {
        LOGD("ERROR: SP out of stack range!");
        return;
    }

    // 逐 8 字节打印（ARM64 标准）
    for (uintptr_t addr = sp; addr < stack_base; addr += 8) {
        print_mem_with_symbol(addr);
    }

    LOGD("\n========================= DONE ==========================\n");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_nativelib_NativeLib_createPthreadKeyLeak(
        JNIEnv* env,
        jobject /* this */) {
    int count = 0;
    pthread_key_t key;

    // 1. 获取函数地址（本质就是取当前SO的GOT表条目）
    void* addr = (void*)pthread_key_create;

    // 2. 用 dladdr 解析真实归属
    print_got_symbol(addr, "pthread_key_create");

    print_thread_stack_info();

    print_safe_backtrace();

    dump_stack_mem_to_base_with_symbols();

    main();

    // 循环创建 pthread 键，不删除它们，直到达到系统限制
    while (true) {
//        sleep(1); // 睡眠 1 秒
        int result = pthread_key_create(&key, NULL);
        if (result != 0) {
            // 创建失败，返回创建的键数量
            return count;
        }
        count++;
        return count;
    }
}