package com.example.stability.design_patterns.behavioral.advanced

import android.util.Log

/**
 * 行为型设计模式高级示例
 * 展示行为型设计模式的高级功能，如状态模式、迭代器模式等
 */
class BehavioralAdvancedExample {
    
    /**
     * 运行所有行为型高级示例
     */
    fun runAllExamples() {
        Log.d("DesignPatterns", "=== BehavioralAdvancedExample.runAllExamples called ===")
        Log.d("DesignPatterns", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行状态模式示例
        statePatternExample()
        
        // 运行迭代器模式示例
        iteratorPatternExample()
        
        Log.d("DesignPatterns", "=== BehavioralAdvancedExample.runAllExamples completed ===")
    }
    
    /**
     * 状态模式示例
     * 解决问题：允许一个对象在其内部状态改变时改变它的行为，对象看起来似乎修改了它的类
     */
    private fun statePatternExample() {
        Log.d("DesignPatterns", "=== 运行状态模式示例 ===")
        
        // 创建上下文
        val context = Context()
        
        // 执行操作
        context.request()
        context.request()
        context.request()
        
        Log.d("DesignPatterns", "=== 状态模式示例完成 ===")
    }
    
    /**
     * 迭代器模式示例
     * 解决问题：提供一种方法顺序访问一个聚合对象中的各个元素，而又不暴露该对象的内部表示
     */
    private fun iteratorPatternExample() {
        Log.d("DesignPatterns", "=== 运行迭代器模式示例 ===")
        
        // 创建聚合
        val aggregate = ConcreteAggregate()
        aggregate.addItem("Item 1")
        aggregate.addItem("Item 2")
        aggregate.addItem("Item 3")
        
        // 创建迭代器
        val iterator = aggregate.createIterator()
        
        // 使用迭代器
        while (!iterator.isDone()) {
            Log.d("DesignPatterns", "Iterator next: ${iterator.next()}")
        }
        
        Log.d("DesignPatterns", "=== 迭代器模式示例完成 ===")
    }
    
    /**
     * 状态接口
     */
    interface State {
        /**
         * 处理方法
         */
        fun handle(context: Context)
    }
    
    /**
     * 具体状态 A
     */
    class ConcreteStateA : State {
        override fun handle(context: Context) {
            Log.d("DesignPatterns", "ConcreteStateA.handle() called")
            // 切换到状态 B
            context.setState(ConcreteStateB())
        }
    }
    
    /**
     * 具体状态 B
     */
    class ConcreteStateB : State {
        override fun handle(context: Context) {
            Log.d("DesignPatterns", "ConcreteStateB.handle() called")
            // 切换到状态 C
            context.setState(ConcreteStateC())
        }
    }
    
    /**
     * 具体状态 C
     */
    class ConcreteStateC : State {
        override fun handle(context: Context) {
            Log.d("DesignPatterns", "ConcreteStateC.handle() called")
            // 切换到状态 A
            context.setState(ConcreteStateA())
        }
    }
    
    /**
     * 上下文类
     */
    class Context {
        private var state: State
        
        init {
            // 初始状态为 A
            state = ConcreteStateA()
            Log.d("DesignPatterns", "Context initialized with ConcreteStateA")
        }
        
        /**
         * 设置状态
         */
        fun setState(state: State) {
            this.state = state
            Log.d("DesignPatterns", "Context.setState() called: ${state.javaClass.simpleName}")
        }
        
        /**
         * 请求方法
         */
        fun request() {
            Log.d("DesignPatterns", "Context.request() called")
            state.handle(this)
        }
    }
    
    /**
     * 迭代器接口
     */
    interface Iterator {
        /**
         * 下一个元素
         */
        fun next(): Any
        
        /**
         * 是否完成
         */
        fun isDone(): Boolean
    }
    
    /**
     * 聚合接口
     */
    interface Aggregate {
        /**
         * 创建迭代器
         */
        fun createIterator(): Iterator
    }
    
    /**
     * 具体迭代器
     */
    class ConcreteIterator(private val aggregate: ConcreteAggregate) : Iterator {
        private var index = 0
        
        override fun next(): Any {
            return aggregate.items[index++]
        }
        
        override fun isDone(): Boolean {
            return index >= aggregate.items.size
        }
    }
    
    /**
     * 具体聚合
     */
    class ConcreteAggregate : Aggregate {
        val items = mutableListOf<String>()
        
        /**
         * 添加项
         */
        fun addItem(item: String) {
            items.add(item)
            Log.d("DesignPatterns", "ConcreteAggregate.addItem() called: $item")
        }
        
        override fun createIterator(): Iterator {
            Log.d("DesignPatterns", "ConcreteAggregate.createIterator() called")
            return ConcreteIterator(this)
        }
    }
}