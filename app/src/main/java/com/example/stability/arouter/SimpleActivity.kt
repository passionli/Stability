package com.example.stability.arouter

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/arouter/simple")
class SimpleActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ARouter-Simple"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "=== SimpleActivity.onCreate() called ===")
        Log.d(TAG, "Intent: $intent")
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = """简单路由跳转示例

Path: /arouter/simple

路由信息:
- 源页面: ARouterMainActivity
- 目标页面: SimpleActivity
- 路由类型: 简单跳转（无参数）

这是一个最简单的ARouter路由跳转示例，没有传递任何参数。

ARouter 路由表会在编译时生成，并通过 ARouter.init() 加载。
如果路由表未正确生成或加载，会导致页面无法跳转。
"""
            textSize = 16f
            setPadding(24, 24, 24, 24)
        }

        setContentView(textView)
        title = "简单路由"
    }
}