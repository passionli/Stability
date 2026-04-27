package com.example.stability

import android.app.Application
import com.example.nativelib.NativeLib

class MyApplication : Application() {
    companion object {
        init {
            // 在 static 语句块中调用 NativeLib().stringFromJNI()
//            NativeLib().stringFromJNI()
        }
    }

    override fun onCreate() {
        super.onCreate()
    }
}
