package com.example.stability;

import androidx.annotation.Keep;

@Keep
public class MeasureArtMethodSize {
    public static void a() {
        new Object().toString();
    }
    public static void b() {
        new Object().toString();
    }
}
