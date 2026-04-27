package com.example.stability.design_patterns.structural.basic

import android.util.Log

/**
 * 结构型设计模式初级示例
 * 展示结构型设计模式的基本功能，如适配器模式、装饰器模式等
 */
class StructuralBasicExample {
    
    /**
     * 运行所有结构型初级示例
     */
    fun runAllExamples() {
        Log.d("DesignPatterns", "=== StructuralBasicExample.runAllExamples called ===")
        Log.d("DesignPatterns", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行适配器模式示例
        adapterPatternExample()
        
        // 运行装饰器模式示例
        decoratorPatternExample()
        
        Log.d("DesignPatterns", "=== StructuralBasicExample.runAllExamples completed ===")
    }
    
    /**
     * 适配器模式示例
     * 解决问题：将一个类的接口转换成客户端希望的另一个接口，使原本不兼容的类可以一起工作
     */
    private fun adapterPatternExample() {
        Log.d("DesignPatterns", "=== 运行适配器模式示例 ===")
        
        // 创建适配器
        val adapter = Adapter(Adaptee())
        
        // 使用适配器
        adapter.request()
        
        Log.d("DesignPatterns", "=== 适配器模式示例完成 ===")
    }
    
    /**
     * 装饰器模式示例
     * 解决问题：动态地给一个对象添加一些额外的职责，而不修改其结构
     */
    private fun decoratorPatternExample() {
        Log.d("DesignPatterns", "=== 运行装饰器模式示例 ===")
        
        // 创建原始组件
        val component = ConcreteComponent()
        
        // 创建装饰器
        val decorator1 = ConcreteDecoratorA(component)
        val decorator2 = ConcreteDecoratorB(decorator1)
        
        // 使用装饰器
        decorator2.operation()
        
        Log.d("DesignPatterns", "=== 装饰器模式示例完成 ===")
    }
    
    /**
     * 目标接口
     */
    interface Target {
        /**
         * 请求方法
         */
        fun request()
    }
    
    /**
     * 适配者类
     */
    class Adaptee {
        /**
         * 特定请求方法
         */
        fun specificRequest() {
            Log.d("DesignPatterns", "Adaptee.specificRequest() called")
        }
    }
    
    /**
     * 适配器类
     */
    class Adapter(private val adaptee: Adaptee) : Target {
        override fun request() {
            Log.d("DesignPatterns", "Adapter.request() called")
            // 调用适配者的方法
            adaptee.specificRequest()
        }
    }
    
    /**
     * 组件接口
     */
    interface Component {
        /**
         * 操作方法
         */
        fun operation()
    }
    
    /**
     * 具体组件
     */
    class ConcreteComponent : Component {
        override fun operation() {
            Log.d("DesignPatterns", "ConcreteComponent.operation() called")
        }
    }
    
    /**
     * 装饰器抽象类
     */
    abstract class Decorator(private val component: Component) : Component {
        override fun operation() {
            component.operation()
        }
    }
    
    /**
     * 具体装饰器 A
     */
    class ConcreteDecoratorA(component: Component) : Decorator(component) {
        override fun operation() {
            super.operation()
            addedFunctionalityA()
        }
        
        /**
         * 添加的功能 A
         */
        private fun addedFunctionalityA() {
            Log.d("DesignPatterns", "ConcreteDecoratorA.addedFunctionalityA() called")
        }
    }
    
    /**
     * 具体装饰器 B
     */
    class ConcreteDecoratorB(component: Component) : Decorator(component) {
        override fun operation() {
            super.operation()
            addedFunctionalityB()
        }
        
        /**
         * 添加的功能 B
         */
        private fun addedFunctionalityB() {
            Log.d("DesignPatterns", "ConcreteDecoratorB.addedFunctionalityB() called")
        }
    }
}