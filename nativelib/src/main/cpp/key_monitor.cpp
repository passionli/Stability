#include <pthread.h>
#include <dlfcn.h>
#include <android/log.h>
#include <stdint.h>
#include <atomic>
#include <unordered_map>
#include <mutex>
#include <string>

#define LOG_TAG "KEY_MONITOR"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ==============================
// 【关键配置】你的系统上限 = 128
// ==============================
#define PTHREAD_KEY_MAX 128
#define KEY_WARN_LIMIT 100  // 达到 100 就开始告警

// ==============================
// 全局统计 & 泄漏监控
// ==============================
static std::atomic<int> g_key_total{0};
static std::mutex g_key_map_mutex;
static std::unordered_map<pthread_key_t, std::string> g_key_call_stack;

// ==============================
// 获取 LR 寄存器（调用者）
// ==============================
#ifdef __aarch64__
static uintptr_t get_lr() {
    uintptr_t lr;
    __asm__ __volatile__("mov %0, x30" : "=r"(lr));
    return lr;
}
#elif defined(__arm__)
static uintptr_t get_lr() {
    uintptr_t lr;
    __asm__ __volatile__("mov %0, lr" : "=r"(lr));
    return lr;
}
#else
static uintptr_t get_lr() { return 0; }
#endif

// ==============================
// 解析地址 => 库名 + 偏移
// ==============================
static std::string resolve_addr(uintptr_t addr) {
    Dl_info info{};
    if (dladdr((void*)addr, &info) && info.dli_fname) {
        char buf[256];
        snprintf(buf, sizeof(buf), "[%s] + %td", info.dli_fname, (char*)addr - (char*)info.dli_saddr);
        return std::string(buf);
    }
    char buf[64];
    snprintf(buf, sizeof(buf), "%p", (void*)addr);
    return buf;
}

// ==============================
// 原始函数指针
// ==============================
static int (*real_pthread_key_create)(pthread_key_t*, void (*)(void*)) = nullptr;
static int (*real_pthread_key_delete)(pthread_key_t) = nullptr;

// ==============================
// 接管 pthread_key_create
// ==============================
extern "C" int pthread_key_create(pthread_key_t* key_ptr, void (*destructor)(void*)) {
    uintptr_t lr = get_lr();
    std::string caller = resolve_addr(lr);

    if (!real_pthread_key_create) {
        real_pthread_key_create = (int (*)(pthread_key_t*, void (*)(void*)))dlsym(RTLD_NEXT, "pthread_key_create");
    }

    int ret = real_pthread_key_create(key_ptr, destructor);

    if (ret == 0) {
        pthread_key_t key = *key_ptr;
        int curr = ++g_key_total;

        {
            std::lock_guard<std::mutex> lock(g_key_map_mutex);
            g_key_call_stack[key] = caller;
        }

        LOGD("✅ KEY_CREATE | key=%d | total=%d | caller=%s", key, curr, caller.c_str());

        // 【128上限专用告警】
        if (curr >= KEY_WARN_LIMIT) {
            LOGE("=====================================");
            LOGE("⚠️  WARNING: key count HIGH = %d / %d", curr, PTHREAD_KEY_MAX);
            LOGE("⚠️  caller: %s", caller.c_str());
            LOGE("=====================================");
        }
    } else {
        LOGE("=====================================");
        LOGE("❌ FAILED: pthread_key_create err=%d (MAX=%d)", ret, PTHREAD_KEY_MAX);
        LOGE("❌ caller: %s", caller.c_str());
        LOGE("=====================================");
    }

    return ret;
}

// ==============================
// 接管 pthread_key_delete
// ==============================
extern "C" int pthread_key_delete(pthread_key_t key) {
    if (!real_pthread_key_delete) {
        real_pthread_key_delete = (int (*)(pthread_key_t))dlsym(RTLD_NEXT, "pthread_key_delete");
    }

    int ret = real_pthread_key_delete(key);

    if (ret == 0) {
        int curr = --g_key_total;
        {
            std::lock_guard<std::mutex> lock(g_key_map_mutex);
            g_key_call_stack.erase(key);
        }
        LOGD("🗑️ KEY_DELETE | key=%d | total=%d", key, curr);
    }

    return ret;
}

// ==============================
// 对外接口
// ==============================
extern "C" int get_pthread_key_count() {
    return g_key_total.load();
}