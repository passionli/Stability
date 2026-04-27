package com.example.stability.design_patterns.creational.intermediate

import android.util.Log

/**
 * 创建型设计模式中级示例
 * 展示创建型设计模式的中级功能，如工厂方法模式、抽象工厂模式等
 */
class CreationalIntermediateExample {
    
    /**
     * 运行所有创建型中级示例
     */
    fun runAllExamples() {
        Log.d("DesignPatterns", "=== CreationalIntermediateExample.runAllExamples called ===")
        Log.d("DesignPatterns", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行工厂方法模式示例
        factoryMethodPatternExample()
        
        // 运行抽象工厂模式示例
        abstractFactoryPatternExample()
        
        Log.d("DesignPatterns", "=== CreationalIntermediateExample.runAllExamples completed ===")
    }
    
    /**
     * 工厂方法模式示例
     * 解决问题：将对象的创建委托给子类，实现创建与使用的分离
     */
    private fun factoryMethodPatternExample() {
        Log.d("DesignPatterns", "=== 运行工厂方法模式示例 ===")
        
        // 创建具体工厂
        val factoryA = ConcreteFactoryA()
        val factoryB = ConcreteFactoryB()
        
        // 使用工厂创建产品
        val productA = factoryA.createProduct()
        val productB = factoryB.createProduct()
        
        // 使用产品
        productA.use()
        productB.use()
        
        Log.d("DesignPatterns", "=== 工厂方法模式示例完成 ===")
    }
    
    /**
     * 抽象工厂模式示例
     * 解决问题：创建一系列相关或相互依赖的对象，而无需指定它们的具体类
     */
    private fun abstractFactoryPatternExample() {
        Log.d("DesignPatterns", "=== 运行抽象工厂模式示例 ===")
        
        // 创建具体工厂
        val factory1 = ConcreteFactory1()
        val factory2 = ConcreteFactory2()
        
        // 使用工厂创建产品族
        val productA1 = factory1.createProductA()
        val productB1 = factory1.createProductB()
        val productA2 = factory2.createProductA()
        val productB2 = factory2.createProductB()
        
        // 使用产品
        productA1.use()
        productB1.use()
        productA2.use()
        productB2.use()
        
        Log.d("DesignPatterns", "=== 抽象工厂模式示例完成 ===")
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
     * 具体产品 A
     */
    class ConcreteProductA : Product {
        override fun use() {
            Log.d("DesignPatterns", "ConcreteProductA.use() called")
        }
    }
    
    /**
     * 具体产品 B
     */
    class ConcreteProductB : Product {
        override fun use() {
            Log.d("DesignPatterns", "ConcreteProductB.use() called")
        }
    }
    
    /**
     * 工厂接口
     */
    interface Factory {
        /**
         * 创建产品
         */
        fun createProduct(): Product
    }
    
    /**
     * 具体工厂 A
     */
    class ConcreteFactoryA : Factory {
        override fun createProduct(): Product {
            Log.d("DesignPatterns", "ConcreteFactoryA.createProduct() called")
            return ConcreteProductA()
        }
    }
    
    /**
     * 具体工厂 B
     */
    class ConcreteFactoryB : Factory {
        override fun createProduct(): Product {
            Log.d("DesignPatterns", "ConcreteFactoryB.createProduct() called")
            return ConcreteProductB()
        }
    }
    
    /**
     * 抽象产品 A
     */
    interface AbstractProductA {
        /**
         * 使用产品 A
         */
        fun use()
    }
    
    /**
     * 抽象产品 B
     */
    interface AbstractProductB {
        /**
         * 使用产品 B
         */
        fun use()
    }
    
    /**
     * 具体产品 A1
     */
    class ConcreteProductA1 : AbstractProductA {
        override fun use() {
            Log.d("DesignPatterns", "ConcreteProductA1.use() called")
        }
    }
    
    /**
     * 具体产品 A2
     */
    class ConcreteProductA2 : AbstractProductA {
        override fun use() {
            Log.d("DesignPatterns", "ConcreteProductA2.use() called")
        }
    }
    
    /**
     * 具体产品 B1
     */
    class ConcreteProductB1 : AbstractProductB {
        override fun use() {
            Log.d("DesignPatterns", "ConcreteProductB1.use() called")
        }
    }
    
    /**
     * 具体产品 B2
     */
    class ConcreteProductB2 : AbstractProductB {
        override fun use() {
            Log.d("DesignPatterns", "ConcreteProductB2.use() called")
        }
    }
    
    /**
     * 抽象工厂
     */
    interface AbstractFactory {
        /**
         * 创建产品 A
         */
        fun createProductA(): AbstractProductA
        
        /**
         * 创建产品 B
         */
        fun createProductB(): AbstractProductB
    }
    
    /**
     * 具体工厂 1
     */
    class ConcreteFactory1 : AbstractFactory {
        override fun createProductA(): AbstractProductA {
            Log.d("DesignPatterns", "ConcreteFactory1.createProductA() called")
            return ConcreteProductA1()
        }
        
        override fun createProductB(): AbstractProductB {
            Log.d("DesignPatterns", "ConcreteFactory1.createProductB() called")
            return ConcreteProductB1()
        }
    }
    
    /**
     * 具体工厂 2
     */
    class ConcreteFactory2 : AbstractFactory {
        override fun createProductA(): AbstractProductA {
            Log.d("DesignPatterns", "ConcreteFactory2.createProductA() called")
            return ConcreteProductA2()
        }
        
        override fun createProductB(): AbstractProductB {
            Log.d("DesignPatterns", "ConcreteFactory2.createProductB() called")
            return ConcreteProductB2()
        }
    }
}