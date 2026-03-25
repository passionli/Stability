#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativelib_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    {
        // 直接崩溃，不需要多余代码
        *(int*)0 = 0;
    }

    {
        int arr[5] = {0};

        // 越界写内存 → SIGSEGV
        arr[999] = 1234;
    }

    {
        int* ptr = new int(10);

        // 释放内存
        delete ptr;

        // 再次访问 → 野指针崩溃
        *ptr = 20;
    }

    return env->NewStringUTF(hello.c_str());
}