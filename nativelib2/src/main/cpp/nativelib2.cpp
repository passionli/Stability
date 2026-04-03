#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h> // 包含 sleep 函数的头文件

#define LOG_TAG "KEY_MONITOR"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativelib2_NativeLib2_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
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

extern "C" JNIEXPORT jint JNICALL
Java_com_example_nativelib2_NativeLib2_createPthreadKeyLeak(
        JNIEnv* env,
        jobject /* this */) {
    int count = 0;
    pthread_key_t key;

    // 1. 获取函数地址（本质就是取当前SO的GOT表条目）
    void* addr = (void*)pthread_key_create;

    // 2. 用 dladdr 解析真实归属
    print_got_symbol(addr, "pthread_key_create");

    // 循环创建 pthread 键，不删除它们，直到达到系统限制
    while (true) {
//        sleep(1); // 睡眠 1 秒
        // 这个 so 的调用无法被监控 so 使用同名函数覆盖调用到
        int result = pthread_key_create(&key, NULL);
        if (result != 0) {
            // 创建失败，返回创建的键数量
            return count;
        }
        count++;
    }
}