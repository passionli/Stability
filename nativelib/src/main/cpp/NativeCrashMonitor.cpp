// NativeCrashMonitor.cpp
#include "NativeCrashMonitor.h"

#include <android/log.h>
#include <dlfcn.h>
#include <ucontext.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <unwind.h>

#define LOG_TAG "NativeCrashMonitor"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define MAX_STACK_FRAMES 32

static const char* get_signal_name(int sig) {
    switch (sig) {
        case SIGSEGV: return "SIGSEGV";
        case SIGABRT: return "SIGABRT";
        case SIGFPE:  return "SIGFPE";
        case SIGBUS:  return "SIGBUS";
        case SIGILL:  return "SIGILL";
        case SIGTRAP: return "SIGTRAP";
        default: return "UNKNOWN";
    }
}

typedef struct {
    uintptr_t frames[MAX_STACK_FRAMES];
    size_t count;
} BacktraceContext;

static _Unwind_Reason_Code unwind_callback(struct _Unwind_Context *ctx, void *arg) {
    BacktraceContext *bt = (BacktraceContext *)arg;
    if (bt->count >= MAX_STACK_FRAMES) {
        return _URC_END_OF_STACK;
    }
    uintptr_t ip = _Unwind_GetIP(ctx);
    if (ip == 0) {
        return _URC_END_OF_STACK;
    }
    bt->frames[bt->count++] = ip;
    return _URC_NO_REASON;
}

void NativeCrashMonitor::crash_handler(int sig, siginfo_t* info, void* ucontext) {
    LOGE("========================================");
    LOGE("       NATIVE CRASH DETECTED            ");
    LOGE("========================================");
    LOGE("Signal: %s (%d)", get_signal_name(sig), sig);

    if (info != nullptr) {
        LOGE("Fault address: %p", info->si_addr);

        if (sig == SIGSEGV) {
            if (info->si_code == SEGV_MAPERR) {
                LOGE("Reason: Address not mapped to object");
            } else if (info->si_code == SEGV_ACCERR) {
                LOGE("Reason: Invalid permissions for mapping");
            } else {
                LOGE("Reason: Unknown (si_code: %d)", info->si_code);
            }
        } else if (sig == SIGABRT) {
            LOGE("Reason: Process called abort()");
        } else if (sig == SIGFPE) {
            if (info->si_code == FPE_INTDIV) {
                LOGE("Reason: Integer division by zero");
            } else if (info->si_code == FPE_INTOVF) {
                LOGE("Reason: Integer overflow");
            } else if (info->si_code == FPE_FLTDIV) {
                LOGE("Reason: Floating-point division by zero");
            } else if (info->si_code == FPE_FLTOVF) {
                LOGE("Reason: Floating-point overflow");
            } else if (info->si_code == FPE_FLTUND) {
                LOGE("Reason: Floating-point underflow");
            } else {
                LOGE("Reason: Unknown (si_code: %d)", info->si_code);
            }
        } else if (sig == SIGILL) {
            if (info->si_code == ILL_ILLOPC) {
                LOGE("Reason: Illegal opcode");
            } else if (info->si_code == ILL_ILLTRP) {
                LOGE("Reason: Illegal trap");
            } else {
                LOGE("Reason: Unknown (si_code: %d)", info->si_code);
            }
        }
    }

#if defined(__aarch64__)
    if (ucontext != nullptr) {
        ucontext_t* ctx = static_cast<ucontext_t*>(ucontext);
        mcontext_t& mcontext = ctx->uc_mcontext;
        LOGE("========================================");
        LOGE("       REGISTERS (ARM64)                ");
        LOGE("========================================");
        LOGE("PC  :  0x%016llx", static_cast<unsigned long long>(mcontext.pc));
        LOGE("SP  :  0x%016llx", static_cast<unsigned long long>(mcontext.sp));
        LOGE("LR  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[30]));
        LOGE("X0  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[0]));
        LOGE("X1  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[1]));
        LOGE("X2  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[2]));
        LOGE("X3  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[3]));
        LOGE("X4  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[4]));
        LOGE("X5  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[5]));
        LOGE("X6  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[6]));
        LOGE("X7  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[7]));
        LOGE("X8  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[8]));
        LOGE("X9  :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[9]));
        LOGE("X10 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[10]));
        LOGE("X11 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[11]));
        LOGE("X12 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[12]));
        LOGE("X13 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[13]));
        LOGE("X14 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[14]));
        LOGE("X15 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[15]));
        LOGE("X16 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[16]));
        LOGE("X17 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[17]));
        LOGE("X18 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[18]));
        LOGE("X19 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[19]));
        LOGE("X20 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[20]));
        LOGE("X21 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[21]));
        LOGE("X22 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[22]));
        LOGE("X23 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[23]));
        LOGE("X24 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[24]));
        LOGE("X25 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[25]));
        LOGE("X26 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[26]));
        LOGE("X27 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[27]));
        LOGE("X28 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[28]));
        LOGE("X29 :  0x%016llx", static_cast<unsigned long long>(mcontext.regs[29]));
    }
#endif

    LOGE("========================================");
    LOGE("       STACK TRACE                      ");
    LOGE("========================================");

    BacktraceContext bt_ctx = {.count = 0};
    _Unwind_Backtrace(unwind_callback, &bt_ctx);

    for (size_t i = 0; i < bt_ctx.count; i++) {
        Dl_info dl_info;
        memset(&dl_info, 0, sizeof(Dl_info));

        if (dladdr(reinterpret_cast<void*>(bt_ctx.frames[i]), &dl_info)) {
            const char* soname = dl_info.dli_fname ? strrchr(dl_info.dli_fname, '/') : nullptr;
            if (soname) soname++;
            else soname = "unknown";

            const char* symname = dl_info.dli_sname ? dl_info.dli_sname : "??";
            uintptr_t offset = bt_ctx.frames[i] - reinterpret_cast<uintptr_t>(dl_info.dli_saddr);

            LOGE("#%02zu  0x%016llx  %s!%s + 0x%llx",
                 i,
                 static_cast<unsigned long long>(bt_ctx.frames[i]),
                 soname,
                 symname,
                 static_cast<unsigned long long>(offset));
        } else {
            LOGE("#%02zu  0x%016llx  [unknown]",
                 i,
                 static_cast<unsigned long long>(bt_ctx.frames[i]));
        }
    }

    LOGE("========================================");
    LOGE("       CRASH INFO END                   ");
    LOGE("========================================");

    _exit(1);
}

NativeCrashMonitor::NativeCrashMonitor() {
}

int NativeCrashMonitor::start(JNIEnv* env) {
    LOGD("NativeCrashMonitor::start() - Registering signal handlers");

    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_flags = SA_SIGINFO;
    sa.sa_sigaction = crash_handler;
    sigemptyset(&sa.sa_mask);

    int signals[] = {SIGSEGV, SIGABRT, SIGFPE, SIGBUS, SIGILL, SIGTRAP};
    const char* signal_names[] = {"SIGSEGV", "SIGABRT", "SIGFPE", "SIGBUS", "SIGILL", "SIGTRAP"};

    for (size_t i = 0; i < sizeof(signals) / sizeof(signals[0]); i++) {
        struct sigaction old_sa;
        if (sigaction(signals[i], &sa, &old_sa) == 0) {
            LOGD("Successfully registered handler for %s", signal_names[i]);
        } else {
            LOGE("Failed to register handler for %s", signal_names[i]);
        }
    }

    LOGD("NativeCrashMonitor::start() - Signal handlers registered");
    return 0;
}
