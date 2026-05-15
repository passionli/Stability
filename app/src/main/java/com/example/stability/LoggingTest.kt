package com.example.stability

import android.util.Log

/**
 * A simple test class to verify our bytecode logging plugin is working.
 */
object LoggingTest {
    
    private const val TAG = "LoggingTest"
    
    fun testMethod(a: Int, b: String): Boolean {
        Log.d(TAG, "testMethod called with a=$a, b=$b")
        return a > 0 && b.isNotEmpty()
    }
    
    fun calculateSum(x: Int, y: Int): Int {
        return x + y
    }
    
    fun greet(name: String): String {
        return "Hello, $name!"
    }
}
