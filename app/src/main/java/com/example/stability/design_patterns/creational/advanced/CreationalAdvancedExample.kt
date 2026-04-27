package com.example.stability.design_patterns.creational.advanced

import android.util.Log

/**
 * 创建型设计模式高级示例
 * 展示创建型设计模式的高级功能，如建造者模式、原型模式等
 */
class CreationalAdvancedExample {
    
    /**
     * 运行所有创建型高级示例
     */
    fun runAllExamples() {
        Log.d("DesignPatterns", "=== CreationalAdvancedExample.runAllExamples called ===")
        Log.d("DesignPatterns", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行建造者模式示例
        builderPatternExample()
        
        // 运行原型模式示例
        prototypePatternExample()
        
        Log.d("DesignPatterns", "=== CreationalAdvancedExample.runAllExamples completed ===")
    }
    
    /**
     * 建造者模式示例
     * 解决问题：复杂对象的创建过程与表示分离，使得同样的创建过程可以创建不同的表示
     */
    private fun builderPatternExample() {
        Log.d("DesignPatterns", "=== 运行建造者模式示例 ===")
        
        // 使用建造者创建产品
        val product1 = Product.Builder()
            .setPartA("Part A1")
            .setPartB("Part B1")
            .setPartC("Part C1")
            .build()
        
        val product2 = Product.Builder()
            .setPartA("Part A2")
            .setPartB("Part B2")
            .build()
        
        // 使用产品
        product1.show()
        product2.show()
        
        Log.d("DesignPatterns", "=== 建造者模式示例完成 ===")
    }
    
    /**
     * 原型模式示例
     * 解决问题：通过复制现有对象来创建新对象，而不是通过实例化类
     */
    private fun prototypePatternExample() {
        Log.d("DesignPatterns", "=== 运行原型模式示例 ===")
        
        // 创建原型对象
        val prototype1 = ConcretePrototype("Prototype 1")
        
        // 克隆原型
        val clone1 = prototype1.clone() as ConcretePrototype
        val clone2 = prototype1.clone() as ConcretePrototype
        
        // 修改克隆对象
        clone1.name = "Clone 1"
        clone2.name = "Clone 2"
        
        // 展示对象
        prototype1.show()
        clone1.show()
        clone2.show()
        
        Log.d("DesignPatterns", "=== 原型模式示例完成 ===")
    }
    
    /**
     * 产品类
     */
    class Product private constructor(
        val partA: String?,
        val partB: String?,
        val partC: String?
    ) {
        /**
         * 展示产品
         */
        fun show() {
            Log.d("DesignPatterns", "Product: partA=$partA, partB=$partB, partC=$partC")
        }
        
        /**
         * 建造者内部类
         */
        class Builder {
            private var partA: String? = null
            private var partB: String? = null
            private var partC: String? = null
            
            /**
             * 设置 partA
             */
            fun setPartA(partA: String): Builder {
                this.partA = partA
                return this
            }
            
            /**
             * 设置 partB
             */
            fun setPartB(partB: String): Builder {
                this.partB = partB
                return this
            }
            
            /**
             * 设置 partC
             */
            fun setPartC(partC: String): Builder {
                this.partC = partC
                return this
            }
            
            /**
             * 构建产品
             */
            fun build(): Product {
                Log.d("DesignPatterns", "Building Product with partA=$partA, partB=$partB, partC=$partC")
                return Product(partA, partB, partC)
            }
        }
    }
    
    /**
     * 原型接口
     */
    interface Prototype {
        /**
         * 克隆方法
         */
        fun clone(): Prototype
    }
    
    /**
     * 具体原型类
     */
    class ConcretePrototype(var name: String) : Prototype {
        override fun clone(): Prototype {
            Log.d("DesignPatterns", "Cloning ConcretePrototype: $name")
            return ConcretePrototype(name)
        }
        
        /**
         * 展示方法
         */
        fun show() {
            Log.d("DesignPatterns", "ConcretePrototype: $name")
        }
    }
}