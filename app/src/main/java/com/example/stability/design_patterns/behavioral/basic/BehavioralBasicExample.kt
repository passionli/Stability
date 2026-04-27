package com.example.stability.design_patterns.behavioral.basic

import android.util.Log

/**
 * 行为型设计模式初级示例
 * 展示行为型设计模式的基本功能，如策略模式、观察者模式等
 */
class BehavioralBasicExample {
    
    /**
     * 运行所有行为型初级示例
     */
    fun runAllExamples() {
        Log.d("DesignPatterns", "=== BehavioralBasicExample.runAllExamples called ===")
        Log.d("DesignPatterns", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行策略模式示例
        strategyPatternExample()
        
        // 运行观察者模式示例
        observerPatternExample()
        
        Log.d("DesignPatterns", "=== BehavioralBasicExample.runAllExamples completed ===")
    }
    
    /**
     * 策略模式示例
     * 解决问题：定义一系列算法，把它们封装起来，并且使它们可以相互替换，使得算法可以独立于使用它的客户而变化
     */
    private fun strategyPatternExample() {
        Log.d("DesignPatterns", "=== 运行策略模式示例 ===")
        
        // 创建策略
        val strategyA = ConcreteStrategyA()
        val strategyB = ConcreteStrategyB()
        
        // 创建上下文
        val context = Context(strategyA)
        
        // 使用策略 A
        context.executeStrategy()
        
        // 切换策略 B
        context.setStrategy(strategyB)
        context.executeStrategy()
        
        Log.d("DesignPatterns", "=== 策略模式示例完成 ===")
    }
    
    /**
     * 观察者模式示例
     * 解决问题：定义对象间的一种一对多依赖关系，当一个对象状态发生变化时，其相关依赖对象都得到通知并被自动更新
     */
    private fun observerPatternExample() {
        Log.d("DesignPatterns", "=== 运行观察者模式示例 ===")
        
        // 创建主题
        val subject = ConcreteSubject()
        
        // 创建观察者
        val observer1 = ConcreteObserver("Observer 1")
        val observer2 = ConcreteObserver("Observer 2")
        
        // 注册观察者
        subject.registerObserver(observer1)
        subject.registerObserver(observer2)
        
        // 改变主题状态
        subject.state = 10
        
        // 移除观察者
        subject.removeObserver(observer1)
        
        // 再次改变主题状态
        subject.state = 20
        
        Log.d("DesignPatterns", "=== 观察者模式示例完成 ===")
    }
    
    /**
     * 策略接口
     */
    interface Strategy {
        /**
         * 执行策略
         */
        fun execute()
    }
    
    /**
     * 具体策略 A
     */
    class ConcreteStrategyA : Strategy {
        override fun execute() {
            Log.d("DesignPatterns", "ConcreteStrategyA.execute() called")
        }
    }
    
    /**
     * 具体策略 B
     */
    class ConcreteStrategyB : Strategy {
        override fun execute() {
            Log.d("DesignPatterns", "ConcreteStrategyB.execute() called")
        }
    }
    
    /**
     * 上下文类
     */
    class Context(private var strategy: Strategy) {
        /**
         * 设置策略
         */
        fun setStrategy(strategy: Strategy) {
            this.strategy = strategy
            Log.d("DesignPatterns", "Context.setStrategy() called")
        }
        
        /**
         * 执行策略
         */
        fun executeStrategy() {
            Log.d("DesignPatterns", "Context.executeStrategy() called")
            strategy.execute()
        }
    }
    
    /**
     * 主题接口
     */
    interface Subject {
        /**
         * 注册观察者
         */
        fun registerObserver(observer: Observer)
        
        /**
         * 移除观察者
         */
        fun removeObserver(observer: Observer)
        
        /**
         * 通知观察者
         */
        fun notifyObservers()
    }
    
    /**
     * 观察者接口
     */
    interface Observer {
        /**
         * 更新方法
         */
        fun update(subject: Subject)
    }
    
    /**
     * 具体主题
     */
    class ConcreteSubject : Subject {
        private val observers = mutableListOf<Observer>()
        var state: Int = 0
            set(value) {
                field = value
                notifyObservers()
            }
        
        override fun registerObserver(observer: Observer) {
            observers.add(observer)
            Log.d("DesignPatterns", "ConcreteSubject.registerObserver() called: ${observer.javaClass.simpleName}")
        }
        
        override fun removeObserver(observer: Observer) {
            observers.remove(observer)
            Log.d("DesignPatterns", "ConcreteSubject.removeObserver() called: ${observer.javaClass.simpleName}")
        }
        
        override fun notifyObservers() {
            Log.d("DesignPatterns", "ConcreteSubject.notifyObservers() called, state: $state")
            for (observer in observers) {
                observer.update(this)
            }
        }
    }
    
    /**
     * 具体观察者
     */
    class ConcreteObserver(private val name: String) : Observer {
        override fun update(subject: Subject) {
            if (subject is ConcreteSubject) {
                Log.d("DesignPatterns", "ConcreteObserver.update() called: $name, state: ${subject.state}")
            }
        }
    }
}