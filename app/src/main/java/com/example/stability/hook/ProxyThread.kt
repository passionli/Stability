package com.example.stability.hook

import android.util.Log
import com.example.nativelib.NativeLib
import java.lang.reflect.Executable

class ProxyThread {
    private val TAG = "ProxyThread"

    fun start() {
        Log.d(TAG, "start() called")
        callOrigin(this as Any)
    }

    companion object {
        private const val TAG = "ProxyThread"
        private fun callOrigin(obj: Any) {
            val method = Thread::class.java.getDeclaredMethod("start")
            
            method.isAccessible = true
            NativeLib.setTLSMethodEnabled(false, NativeLib.originMethod)
//            method.invoke(obj)
            val measureClass = Class.forName("com.example.stability.MeasureArtMethodSize")
            val invokeMethod = measureClass.getDeclaredMethod("invoke")
            invokeMethod.isAccessible = true
            Log.d(TAG, "invoke() before")
//            invokeMethod.invoke(arrayOf(method, obj))
            invokeMethod.invoke(arrayOf(method, obj, getShorty(method)))
            Log.d(TAG, "invoke() after")
            NativeLib.setTLSMethodEnabled(true, NativeLib.originMethod)
        }

        private fun getShorty(method: Executable): Array<Char> {
            val shorty = ArrayList<Char>()
            
            // 处理返回值类型
            // 如果是 Method 类型
            if (method is java.lang.reflect.Method) {
                val returnType = method.returnType
                shorty.add(getTypeChar(returnType))
            }
            
            // 处理参数类型
            for (paramType in method.parameterTypes) {
                shorty.add(getTypeChar(paramType))
            }
            
            return shorty.toTypedArray()
        }

        private fun getTypeChar(type: Class<*>): Char {
            return when {
                type == Void.TYPE -> 'V'
                type == Boolean::class.javaPrimitiveType || type == Boolean::class.java -> 'Z'
                type == Byte::class.javaPrimitiveType || type == Byte::class.java -> 'B'
                type == Char::class.javaPrimitiveType || type == Char::class.java -> 'C'
                type == Short::class.javaPrimitiveType || type == Short::class.java -> 'S'
                type == Int::class.javaPrimitiveType || type == Int::class.java -> 'I'
                type == Long::class.javaPrimitiveType || type == Long::class.java -> 'J'
                type == Float::class.javaPrimitiveType || type == Float::class.java -> 'F'
                type == Double::class.javaPrimitiveType || type == Double::class.java -> 'D'
                type.isArray -> '['  // 数组类型简化为 [
                else -> 'L'  // 对象类型简化为 L
            }
        }
    }
}