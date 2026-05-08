package com.example.stability.arouter

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/arouter/uriTest")
class UriTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val data = intent.data
        val queryParams = data?.queryParameterNames?.associateWith { data.getQueryParameter(it) }
        
        val textView = TextView(this).apply {
            text = """Uri跳转示例

Path: /arouter/uriTest

Uri: ${data?.toString() ?: "null"}

查询参数:
${queryParams?.entries?.joinToString("\n") { "- ${it.key}: ${it.value}" } ?: "  (无参数)"}

ARouter支持通过Uri Scheme进行路由跳转，格式为：
arouter://host/path?param1=value1&param2=value2
"""
            textSize = 18f
            setPadding(24, 24, 24, 24)
        }
        
        setContentView(textView)
        title = "Uri跳转"
    }
}