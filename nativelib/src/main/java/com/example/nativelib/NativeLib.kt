package com.example.nativelib

import com.bytedance.shadowhook.ShadowHook

class NativeLib {

    /**
     * A native method that is implemented by the 'nativelib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    /**
     * A native method that demonstrates pthread key leak by creating keys without deleting them.
     */
    external fun createPthreadKeyLeak(): Int

    companion object {
        // Used to load the 'nativelib' library on application startup.
        init {
            ShadowHook.init(ShadowHook.ConfigBuilder()
                .setMode(ShadowHook.Mode.SHARED)
                .build())
            System.loadLibrary("nativelib")
        }
    }
}