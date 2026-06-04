package com.example.stability;

import androidx.annotation.Keep;

@Keep
public class MeasureArtMethodSize {
    static {
        // 触发绑定 jni native 函数
        reserveMethod();
    }

    public static void a() {
        new Object().toString();
    }
    public static void b() {
        new Object().toString();
    }

    public static void invoke() {
        try {
            Object.class.hashCode();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static native void reserveMethod();
}
