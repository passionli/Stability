package com.example.stability.design_patterns.creational.basic

import android.util.Log

/**
 * 创建型设计模式初级示例
 * 展示创建型设计模式的基本功能，如单例模式、工厂模式等
 */
class CreationalBasicExample {
    
    /**
     * 运行所有创建型初级示例
     */
    fun runAllExamples() {
        Log.d("DesignPatterns", "=== CreationalBasicExample.runAllExamples called ===")
        Log.d("DesignPatterns", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行单例模式示例
        singletonPatternExample()
        
        // 运行简单工厂模式示例
        simpleFactoryPatternExample()
        
        Log.d("DesignPatterns", "=== CreationalBasicExample.runAllExamples completed ===")
    }
    
    /**
     * 单例模式示例
     * 解决问题：确保一个类只有一个实例，并提供一个全局访问点
     */
    private fun singletonPatternExample() {
        Log.d("DesignPatterns", "=== 运行单例模式示例 ===")
        
        // 获取单例实例
        val instance1 = Singleton.getInstance()
        val instance2 = Singleton.getInstance()
        
        // 验证是否是同一个实例
        Log.d("DesignPatterns", "instance1 === instance2: ${instance1 === instance2}")
        Log.d("DesignPatterns", "instance1 hashcode: ${instance1.hashCode()}")
        Log.d("DesignPatterns", "instance2 hashcode: ${instance2.hashCode()}")
        
        // 使用单例实例
        instance1.doSomething()
        
        Log.d("DesignPatterns", "=== 单例模式示例完成 ===")
    }
    
    /**
     * 简单工厂模式示例
     * 解决问题：将对象的创建与使用分离，简化客户端代码
     */
    private fun simpleFactoryPatternExample() {
        Log.d("DesignPatterns", "=== 运行简单工厂模式示例 ===")
        
        // 使用工厂创建产品
        val productA = ProductFactory.createProduct(ProductType.TYPE_A)
        val productB = ProductFactory.createProduct(ProductType.TYPE_B)
        
        // 使用产品
        productA?.use()
        productB?.use()
        
        Log.d("DesignPatterns", "=== 简单工厂模式示例完成 ===")
    }
    
    /**
     * 单例模式实现
     */
    class Singleton private constructor() {
        companion object {
            // 懒加载单例实例
            private var instance: Singleton? = null
            
            /**
             * 获取单例实例
             * 解决问题：确保只有一个实例被创建
             */
            fun getInstance(): Singleton {
                if (instance == null) {
                    instance = Singleton()
                    Log.d("DesignPatterns", "Singleton instance created")
                }
                return instance!!
            }
        }
        
        /**
         * 单例方法
         */
        fun doSomething() {
            Log.d("DesignPatterns", "Singleton.doSomething() called")
        }
    }
    
    /**
     * 产品类型枚举
     */
    enum class ProductType {
        TYPE_A,
        TYPE_B
    }
    
    /**
     * 产品接口
     */
    interface Product {
        /**
         * 使用产品
         */
        fun use()
    }
    
    /**
     * 产品 A 实现
     */
    class ProductA : Product {
        override fun use() {
            Log.d("DesignPatterns", "ProductA.use() called")
        }
    }
    
    /**
     * 产品 B 实现
     */
    class ProductB : Product {
        override fun use() {
            Log.d("DesignPatterns", "ProductB.use() called")
        }
    }
    
    /**
     * 产品工厂
     */
    object ProductFactory {
        /**
         * 创建产品
         * 解决问题：根据类型创建不同的产品实例
         */
        fun createProduct(type: ProductType): Product? {
            return when (type) {
                ProductType.TYPE_A -> {
                    Log.d("DesignPatterns", "Creating ProductA")
                    ProductA()
                }
                ProductType.TYPE_B -> {
                    Log.d("DesignPatterns", "Creating ProductB")
                    ProductB()
                }
            }
        }
    }
}