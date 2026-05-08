package com.example.stability.arouter

import com.alibaba.android.arouter.facade.template.IProvider

interface IHelloService : IProvider {
    fun sayHello(name: String)
    fun getGreeting(): String
}