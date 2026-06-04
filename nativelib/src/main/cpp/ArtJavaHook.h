// ArtJavaHook.h
#ifndef ARTJAVAHOOK_H
#define ARTJAVAHOOK_H

#include <jni.h>
#include <cstddef>
#include "unordered_map"
#include "mutex"

// 存储 ArtMethod 信息的结构体
struct ArtMethodInfo {
    ptrdiff_t artMethodSize;      // ArtMethod 结构大小
    ptrdiff_t jniCodeOffset;      // jniCode 字段偏移量
    bool isValid;                 // 是否有效

    ArtMethodInfo() : artMethodSize(-1), jniCodeOffset(-1), isValid(false) {}
};

struct HookMethodInfo {
    void *proxyMethod;
    void *originMethod;
};

class ArtJavaHook {
public:
    ArtJavaHook(const ArtJavaHook&) = delete;
    ArtJavaHook& operator=(const ArtJavaHook&) = delete;
    ArtJavaHook(ArtJavaHook&&) = delete;
    ArtJavaHook& operator=(ArtJavaHook&&) = delete;

    static ArtJavaHook& getInstance() {
        static ArtJavaHook instance;
        return instance;
    }

    int start(JNIEnv *env, jlong proxyMethodThreadStart, jlong originMethodThreadStart);

    // 获取 ArtMethod 信息
    static ArtMethodInfo getArtMethodInfo() { return s_artMethodInfo; }

    // 设置 jniCode
    static void setJniCode(void* jniCode) { s_jniCode = jniCode; }

    // 获取 jniCode
    static void* getJniCode() { return s_jniCode; }

    void hookJavaNativeMethod(const char *className, const char *methodName, const char *sig,
                              void *proxyMethod, void **originMethod);

    void *onMethodEnter(void *originMethod);

    bool isHookEnabled(void *originMethod);

    void setHookEnabled(void *originMethod, bool enabled);

    void deopt(void *artMethod);

    // 调用 art::ArtMethod::Invoke
    static void invokeArtMethod(void* artMethod, void* thread, uint32_t argsSize, 
                                uint32_t* args, void* result, const char* shorty);

    // 调用 art::JNIEnvExt::AddLocalReference
    static void* addLocalReference(void* jniEnvExt, void* localRef);

    // 调用 art::BoxPrimitive
    static void* boxPrimitive(int type, void* jValue);

private:
    ArtJavaHook() {}
    ~ArtJavaHook() = default;

    // 计算 ArtMethod 大小和 jniCode 偏移量
    static ArtMethodInfo calculateArtMethodInfo(JNIEnv* env);

    // 存储 ArtMethod 信息
    static ArtMethodInfo s_artMethodInfo;

    // 存储 jniCode
    static void* s_jniCode;

    // 存储 HookMethodInfo 信息，支持 key-value 和多线程安全，key 为 originMethod
    std::unordered_map<void*, HookMethodInfo> s_hookMethodMap;
    std::mutex s_hookMethodMutex;

    static thread_local std::unordered_map<void*, bool> s_tls_enable_map;
};

#endif // ARTJAVAHOOK_H
