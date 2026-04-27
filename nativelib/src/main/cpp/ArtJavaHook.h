// ArtJavaHook.h
#ifndef ARTJAVAHOOK_H
#define ARTJAVAHOOK_H

#include <jni.h>

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

    int start(JNIEnv* env);

private:
    ArtJavaHook() {}
    ~ArtJavaHook() = default;
};

#endif // ARTJAVAHOOK_H
