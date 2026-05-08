package com.example.stability.arouter

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/service/hello")
class HelloServiceImpl : IHelloService {

    private lateinit var context: Context

    override fun init(context: Context) {
        this.context = context
        android.util.Log.i("HelloService", "HelloServiceImpl initialized")
    }

    override fun sayHello(name: String) {
        val greeting = "Hello, $name! This message comes from ARouter service."
        android.util.Log.i("HelloService", greeting)
        
        android.widget.Toast.makeText(context, greeting, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun getGreeting(): String {
        return "Welcome to ARouter Service!"
    }
}