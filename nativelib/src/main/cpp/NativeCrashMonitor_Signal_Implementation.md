# NativeCrashMonitor 信号注册与回调实现原理

## 概述

NativeCrashMonitor 是一个基于 Linux 信号机制实现的 Native Crash 监控组件，用于捕获和分析 Android Native 层的崩溃事件。本文档详细描述其信号注册流程、回调机制及内部实现原理。

---

## 一、信号机制基础

### 1.1 Linux 信号概念

信号是 Linux/Unix 系统中进程间通信的一种机制，用于通知进程发生了某种事件。当进程发生崩溃（如段错误、非法指令等）时，操作系统会向进程发送相应的信号。

| 信号 | 名称 | 触发场景 |
|------|------|----------|
| SIGSEGV | 段错误 | 访问无效内存地址 |
| SIGABRT | 中止 | 调用 abort() 函数 |
| SIGFPE | 浮点异常 | 除零、溢出等 |
| SIGBUS | 总线错误 | 内存对齐错误 |
| SIGILL | 非法指令 | 执行无效机器指令 |
| SIGTRAP | 陷阱 | 断点或调试异常 |

### 1.2 信号处理方式

Linux 提供两种信号处理方式：
- **简单信号处理**：`signal()` 函数，只能获取信号编号
- **扩展信号处理**：`sigaction()` 函数，可以获取信号详细信息（`siginfo_t`）和上下文（`ucontext_t`）

NativeCrashMonitor 使用 `sigaction()` 实现精确的崩溃信息捕获。

---

## 二、信号注册流程

### 2.1 注册流程架构

```
┌─────────────────────────────────────────────────────────────────┐
│                   NativeCrashMonitor::start()                   │
├─────────────────────────────────────────────────────────────────┤
│  1. 初始化 sigaction 结构体                                     │
│     └─ sa_flags = SA_SIGINFO                                   │
│     └─ sa_sigaction = crash_handler                            │
│     └─ sa_mask = 0 (不阻塞其他信号)                             │
│                                                                 │
│  2. 遍历需要监控的信号列表                                       │
│     └─ SIGSEGV, SIGABRT, SIGFPE, SIGBUS, SIGILL, SIGTRAP       │
│                                                                 │
│  3. 调用 sigaction() 注册信号处理器                              │
│     └─ 保存旧的信号处理器（可选恢复）                             │
│                                                                 │
│  4. 返回注册结果                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 核心代码实现

```cpp
int NativeCrashMonitor::start(JNIEnv* env) {
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    
    // 设置 SA_SIGINFO 标志，使回调函数能接收 siginfo_t 和 ucontext_t 参数
    sa.sa_flags = SA_SIGINFO;
    
    // 设置信号处理函数
    sa.sa_sigaction = crash_handler;
    
    // 初始化信号掩码（不阻塞任何信号）
    sigemptyset(&sa.sa_mask);
    
    // 注册多个信号处理器
    int signals[] = {SIGSEGV, SIGABRT, SIGFPE, SIGBUS, SIGILL, SIGTRAP};
    for (size_t i = 0; i < sizeof(signals) / sizeof(signals[0]); i++) {
        struct sigaction old_sa;
        sigaction(signals[i], &sa, &old_sa);
    }
    return 0;
}
```

### 2.3 sigaction 结构体详解

```cpp
struct sigaction {
    // 信号处理函数指针（SA_SIGINFO 时使用）
    void (*sa_sigaction)(int, siginfo_t*, void*);
    
    // 信号掩码：处理信号期间需要阻塞的信号
    sigset_t sa_mask;
    
    // 标志位，控制信号行为
    int sa_flags;
};
```

**关键标志位说明**：
- `SA_SIGINFO`：启用扩展信号处理，回调函数接收 `siginfo_t*` 和 `void*` 参数
- `SA_RESTART`：被信号中断的系统调用自动重启（本实现未使用）
- `SA_NODEFER`：处理信号时不自动阻塞该信号（本实现未使用）

---

## 三、信号回调机制

### 3.1 回调函数签名

```cpp
void crash_handler(int sig, siginfo_t* info, void* ucontext);
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `sig` | `int` | 信号编号（如 SIGSEGV = 11） |
| `info` | `siginfo_t*` | 信号详细信息（崩溃地址、错误码等） |
| `ucontext` | `void*` | 上下文信息（寄存器状态、栈指针等） |

### 3.2 siginfo_t 结构体详解

```cpp
typedef struct siginfo {
    int si_signo;      // 信号编号
    int si_code;       // 信号代码（详细错误原因）
    int si_errno;      // 错误码
    pid_t si_pid;      // 发送信号的进程ID
    uid_t si_uid;      // 发送信号的用户ID
    void* si_addr;     // 引起故障的地址（关键！）
    // ... 其他字段
} siginfo_t;
```

**si_code 错误码对照表**：

| 信号 | si_code | 含义 |
|------|---------|------|
| SIGSEGV | SEGV_MAPERR | 地址未映射到任何对象 |
| SIGSEGV | SEGV_ACCERR | 对已映射地址无访问权限 |
| SIGFPE | FPE_INTDIV | 整数除零 |
| SIGFPE | FPE_FLTDIV | 浮点除零 |
| SIGFPE | FPE_INTOVF | 整数溢出 |
| SIGILL | ILL_ILLOPC | 非法操作码 |

### 3.3 ucontext_t 上下文信息

`ucontext_t` 包含信号发生时的完整寄存器状态，这对于分析崩溃现场至关重要。

**ARM64 架构下的 mcontext_t**：

```cpp
typedef struct mcontext {
    uint64_t regs[31];  // X0-X30 通用寄存器
    uint64_t sp;        // 栈指针
    uint64_t pc;        // 程序计数器（崩溃位置）
    uint64_t pstate;    // 程序状态寄存器
} mcontext_t;
```

**关键寄存器说明**：
- `pc` (Program Counter)：崩溃时正在执行的指令地址
- `sp` (Stack Pointer)：当前栈顶地址
- `lr` (Link Register = X30)：函数返回地址
- `fp` (Frame Pointer = X29)：栈帧基址

---

## 四、堆栈回溯实现

### 4.1 实现方案对比

| 方案 | 函数 | 依赖 | 兼容性 |
|------|------|------|--------|
| libunwind | `backtrace()` / `backtrace_symbols()` | 需要链接 -lunwind | 不兼容 NDK |
| DWARF | 解析 .eh_frame 段 | 需要调试信息 | 复杂且不可靠 |
| **_Unwind** | `_Unwind_Backtrace()` | 编译器内置支持 | **推荐方案** |

### 4.2 _Unwind_Backtrace 原理

`_Unwind_Backtrace` 是 GCC/Clang 编译器内置的堆栈回溯机制，基于 DWARF 异常处理信息实现。

```
┌─────────────────────────────────────────────────────────────────┐
│                    _Unwind_Backtrace 工作流程                   │
├─────────────────────────────────────────────────────────────────┤
│  1. 初始化 Unwind 上下文                                        │
│     └─ 获取当前 PC、SP、FP 寄存器值                              │
│                                                                 │
│  2. 遍历每个栈帧                                                 │
│     └─ 调用 unwind_callback 获取指令地址                         │
│     └─ 通过 DWARF CFI 信息计算上一帧的 FP/LR                    │
│                                                                 │
│  3. 直到到达栈底或达到最大帧数                                   │
│                                                                 │
│  4. 返回收集到的所有指令地址                                     │
└─────────────────────────────────────────────────────────────────┘
```

### 4.3 核心代码实现

```cpp
typedef struct {
    uintptr_t frames[MAX_STACK_FRAMES];
    size_t count;
} BacktraceContext;

static _Unwind_Reason_Code unwind_callback(struct _Unwind_Context *ctx, void *arg) {
    BacktraceContext *bt = (BacktraceContext *)arg;
    
    // 检查是否超过最大帧数量
    if (bt->count >= MAX_STACK_FRAMES) {
        return _URC_END_OF_STACK;
    }
    
    // 获取当前栈帧的指令地址
    uintptr_t ip = _Unwind_GetIP(ctx);
    if (ip == 0) {
        return _URC_END_OF_STACK;
    }
    
    // 保存到帧数组
    bt->frames[bt->count++] = ip;
    
    // 继续回溯
    return _URC_NO_REASON;
}

// 使用示例
BacktraceContext bt_ctx = {.count = 0};
_Unwind_Backtrace(unwind_callback, &bt_ctx);
```

---

## 五、符号解析机制

### 5.1 dladdr 函数

`dladdr()` 是 POSIX 标准函数，用于将内存地址转换为符号信息。

```cpp
#include <dlfcn.h>

int dladdr(const void *addr, Dl_info *info);
```

**Dl_info 结构体**：

```cpp
typedef struct {
    const char *dli_fname;  // 包含该地址的共享库路径
    void       *dli_fbase;  // 共享库加载基址
    const char *dli_sname;  // 符号名称
    void       *dli_saddr;  // 符号起始地址
} Dl_info;
```

### 5.2 符号解析流程

```cpp
for (size_t i = 0; i < bt_ctx.count; i++) {
    Dl_info dl_info;
    memset(&dl_info, 0, sizeof(Dl_info));
    
    if (dladdr(reinterpret_cast<void*>(bt_ctx.frames[i]), &dl_info)) {
        // 提取 SO 名称（去掉路径）
        const char* soname = dl_info.dli_fname ? strrchr(dl_info.dli_fname, '/') : nullptr;
        if (soname) soname++;
        else soname = "unknown";
        
        // 获取符号名称，未知则显示 "??"
        const char* symname = dl_info.dli_sname ? dl_info.dli_sname : "??";
        
        // 计算偏移地址
        uintptr_t offset = bt_ctx.frames[i] - reinterpret_cast<uintptr_t>(dl_info.dli_saddr);
        
        // 输出格式：#00  0x12345678  libnativelib.so!function_name + 0x123
        LOGE("#%02zu  0x%016llx  %s!%s + 0x%llx",
             i,
             static_cast<unsigned long long>(bt_ctx.frames[i]),
             soname,
             symname,
             static_cast<unsigned long long>(offset));
    }
}
```

---

## 六、完整工作流程图

```
┌─────────────────────────────────────────────────────────────────────┐
│                     NativeCrashMonitor 完整流程                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  [1] 应用启动                                                       │
│       │                                                            │
│       ▼                                                            │
│  [2] NativeCrashMonitor::start()                                   │
│       │                                                            │
│       ├─ 初始化 sigaction 结构体                                    │
│       │   └─ sa_flags = SA_SIGINFO                                 │
│       │   └─ sa_sigaction = crash_handler                          │
│       │                                                            │
│       ├─ 遍历信号列表                                              │
│       │   └─ SIGSEGV, SIGABRT, SIGFPE, SIGBUS, SIGILL, SIGTRAP    │
│       │                                                            │
│       └─ 调用 sigaction() 注册                                     │
│                                                                    │
│  [3] 程序运行中发生崩溃                                              │
│       │                                                            │
│       ▼                                                            │
│  [4] 操作系统发送信号                                                │
│       │                                                            │
│       ▼                                                            │
│  [5] crash_handler() 被调用                                        │
│       │                                                            │
│       ├─ 解析信号类型和原因（siginfo_t）                             │
│       │                                                            │
│       ├─ 提取寄存器状态（ucontext_t → mcontext_t）                   │
│       │   └─ PC, SP, LR, X0-X29                                   │
│       │                                                            │
│       ├─ 调用 _Unwind_Backtrace() 获取调用栈                        │
│       │   └─ 遍历每个栈帧，收集指令地址                              │
│       │                                                            │
│       ├─ 使用 dladdr() 解析每个地址的符号                            │
│       │   └─ SO名称、函数名、偏移                                   │
│       │                                                            │
│       └─ 通过 __android_log_print() 输出到 Logcat                   │
│                                                                    │
│  [6] 调用 _exit(1) 终止进程                                         │
│                                                                    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 七、关键设计要点

### 7.1 线程安全性

信号处理函数在信号上下文（interrupt context）中执行，需要注意：
- 避免调用不可重入函数（如 `malloc`、`printf`）
- 使用 `__android_log_print` 是安全的（Android 已处理重入问题）
- 避免复杂的数据结构操作

### 7.2 信号覆盖问题

多个库可能同时注册信号处理器，后注册的会覆盖先注册的。解决方案：
- 保存旧的信号处理器，处理完成后调用原处理器
- 使用信号链（signal chaining）机制

### 7.3 调试器兼容性

当程序被调试器（如 GDB）附加时，调试器会优先捕获信号。解决方案：
- 检测调试器存在（通过 `/proc/self/status` 或 `ptrace`）
- 如果存在调试器，不注册信号处理器或设置 `SA_RESETHAND`

---

## 八、总结

NativeCrashMonitor 通过以下技术实现可靠的 Native Crash 监控：

1. **信号注册**：使用 `sigaction()` 注册扩展信号处理器，获取完整的崩溃上下文
2. **上下文获取**：从 `ucontext_t` 提取 ARM64 寄存器状态
3. **堆栈回溯**：使用 `_Unwind_Backtrace()` 进行跨模块的栈帧遍历
4. **符号解析**：使用 `dladdr()` 将内存地址转换为可读的符号信息
5. **日志输出**：通过 Android Logcat 输出结构化的崩溃报告

该实现兼容所有编译优化级别的共享库，不依赖调试信息，是 Android Native 开发中可靠的崩溃监控方案。