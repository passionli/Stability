package com.example.stability.design_patterns.structural.intermediate

import android.util.Log

/**
 * 结构型设计模式中级示例
 * 展示结构型设计模式的中级功能，如代理模式、桥接模式等
 */
class StructuralIntermediateExample {
    
    /**
     * 运行所有结构型中级示例
     */
    fun runAllExamples() {
        Log.d("DesignPatterns", "=== StructuralIntermediateExample.runAllExamples called ===")
        Log.d("DesignPatterns", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行代理模式示例
        proxyPatternExample()
        
        // 运行桥接模式示例
        bridgePatternExample()
        
        Log.d("DesignPatterns", "=== StructuralIntermediateExample.runAllExamples completed ===")
    }
    
    /**
     * 代理模式示例
     * 解决问题：为其他对象提供一种代理以控制对这个对象的访问
     */
    private fun proxyPatternExample() {
        Log.d("DesignPatterns", "=== 运行代理模式示例 ===")
        
        // 创建代理
        val proxy = Proxy(RealSubject())
        
        // 使用代理
        proxy.request()
        
        Log.d("DesignPatterns", "=== 代理模式示例完成 ===")
    }
    
    /**
     * 桥接模式示例
     * 解决问题：将抽象部分与实现部分分离，使它们可以独立变化
     */
    private fun bridgePatternExample() {
        Log.d("DesignPatterns", "=== 运行桥接模式示例 ===")
        
        // 创建实现
        val implementation1 = ConcreteImplementation1()
        val implementation2 = ConcreteImplementation2()
        
        // 创建抽象
        val abstraction1 = RefinedAbstraction(implementation1)
        val abstraction2 = RefinedAbstraction(implementation2)
        
        // 使用抽象
        abstraction1.operation()
        abstraction2.operation()
        
        Log.d("DesignPatterns", "=== 桥接模式示例完成 ===")
    }
    
    /**
     * 主题接口
     */
    interface Subject {
        /**
         * 请求方法
         */
        fun request()
    }
    
    /**
     * 真实主题
     */
    class RealSubject : Subject {
        override fun request() {
            Log.d("DesignPatterns", "RealSubject.request() called")
        }
    }
    
    /**
     * 代理类
     */
    class Proxy(private val realSubject: RealSubject) : Subject {
        override fun request() {
            Log.d("DesignPatterns", "Proxy.request() called")
            // 前置处理
            beforeRequest()
            // 调用真实主题
            realSubject.request()
            // 后置处理
            afterRequest()
        }
        
        /**
         * 前置处理
         */
        private fun beforeRequest() {
            Log.d("DesignPatterns", "Proxy.beforeRequest() called")
        }
        
        /**
         * 后置处理
         */
        private fun afterRequest() {
            Log.d("DesignPatterns", "Proxy.afterRequest() called")
        }
    }
    
    /**
     * 实现接口
     */
    interface Implementation {
        /**
         * 实现方法
         */
        fun operationImpl()
    }
    
    /**
     * 具体实现 1
     */
    class ConcreteImplementation1 : Implementation {
        override fun operationImpl() {
            Log.d("DesignPatterns", "ConcreteImplementation1.operationImpl() called")
        }
    }
    
    /**
     * 具体实现 2
     */
    class ConcreteImplementation2 : Implementation {
        override fun operationImpl() {
            Log.d("DesignPatterns", "ConcreteImplementation2.operationImpl() called")
        }
    }
    
    /**
     * 抽象类
     */
    abstract class Abstraction(private val implementation: Implementation) {
        /**
         * 操作方法
         */
        open fun operation() {
            implementation.operationImpl()
        }
    }
    
    /**
     *  refined 抽象类
     */
    class RefinedAbstraction(implementation: Implementation) : Abstraction(implementation) {
        override fun operation() {
            Log.d("DesignPatterns", "RefinedAbstraction.operation() called")
            super.operation()
        }
    }
}