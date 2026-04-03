package com.example.nativelib2

class NativeLib2 {

    /**
     * A native method that is implemented by the 'nativelib2' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String


    /**
     * A native method that demonstrates pthread key leak by creating keys without deleting them.
     */
    external fun createPthreadKeyLeak(): Int

    companion object {
        // Used to load the 'nativelib2' library on application startup.
        init {
            System.loadLibrary("nativelib2")
        }
    }
}