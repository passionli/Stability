package com.example.stability.arouter

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter

@Route(path = "/arouter/withParams")
class WithParamsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ARouter-WithParams"
    }

    @Autowired(name = "name")
    @JvmField
    var name: String? = "默认名称"

    @Autowired(name = "count")
    @JvmField
    var count: Int = 0

    @Autowired(name = "isVip")
    @JvmField
    var isVip: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "=== WithParamsActivity.onCreate() called ===")
        Log.d(TAG, "Intent: $intent")

        super.onCreate(savedInstanceState)

        Log.d(TAG, "Before ARouter.inject()")
        ARouter.getInstance().inject(this)
        Log.d(TAG, "After ARouter.inject()")
        Log.d(TAG, "name: $name, count: $count, isVip: $isVip")

        val textView = TextView(this).apply {
            text = """带参数路由跳转示例

Path: /arouter/withParams

接收到的参数:
- name: $name
- count: $count
- isVip: $isVip

使用 @Autowired 注解可以自动注入参数，无需手动从 Intent 中获取。

参数传递方式:
ARouter.getInstance()
    .build("/arouter/withParams")
    .withString("name", "测试用户")
    .withInt("count", 100)
    .withBoolean("isVip", true)
    .navigation()
"""
            textSize = 16f
            setPadding(24, 24, 24, 24)
        }

        setContentView(textView)
        title = "带参数路由"
    }
}