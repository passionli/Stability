package com.example.stability.arouter

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/arouter/group/test", group = "test")
class GroupTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this).apply {
            text = """路由分组示例

Path: /arouter/group/test
Group: test

ARouter支持路由分组功能，可以将路由按模块进行分组管理。
分组名称由路径的第一段决定（默认），也可以通过 group 属性显式指定。

使用分组可以：
1. 按需加载，减少首次启动时间
2. 按模块组织路由
3. 便于权限控制和管理
"""
            textSize = 18f
            setPadding(24, 24, 24, 24)
        }
        
        setContentView(textView)
        title = "路由分组"
    }
}