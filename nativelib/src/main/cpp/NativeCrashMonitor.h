// NativeCrashMonitor.h
#ifndef NATIVECRASHMONITOR_H
#define NATIVECRASHMONITOR_H

#include <jni.h>
#include <signal.h>

class NativeCrashMonitor {
public:
    NativeCrashMonitor(const NativeCrashMonitor&) = delete;
    NativeCrashMonitor& operator=(const NativeCrashMonitor&) = delete;
    NativeCrashMonitor(NativeCrashMonitor&&) = delete;
    NativeCrashMonitor& operator=(NativeCrashMonitor&&) = delete;

    static NativeCrashMonitor& getInstance() {
        static NativeCrashMonitor instance;
        return instance;
    }

    int start(JNIEnv* env);

private:
    NativeCrashMonitor();
    ~NativeCrashMonitor() = default;

    static void crash_handler(int sig, siginfo_t* info, void* ucontext);
};

#endif // NATIVECRASHMONITOR_H
