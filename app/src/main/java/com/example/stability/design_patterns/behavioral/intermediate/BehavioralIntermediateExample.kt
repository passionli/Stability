package com.example.stability.design_patterns.behavioral.intermediate

import android.util.Log

/**
 * 行为型设计模式中级示例
 * 展示行为型设计模式的中级功能，如命令模式、模板方法模式等
 */
class BehavioralIntermediateExample {
    
    /**
     * 运行所有行为型中级示例
     */
    fun runAllExamples() {
        Log.d("DesignPatterns", "=== BehavioralIntermediateExample.runAllExamples called ===")
        Log.d("DesignPatterns", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行命令模式示例
        commandPatternExample()
        
        // 运行模板方法模式示例
        templateMethodPatternExample()
        
        Log.d("DesignPatterns", "=== BehavioralIntermediateExample.runAllExamples completed ===")
    }
    
    /**
     * 命令模式示例
     * 解决问题：将一个请求封装为一个对象，从而使你可用不同的请求对客户进行参数化，对请求排队或记录请求日志，以及支持可撤销的操作
     */
    private fun commandPatternExample() {
        Log.d("DesignPatterns", "=== 运行命令模式示例 ===")
        
        // 创建接收者
        val receiver = Receiver()
        
        // 创建命令
        val command1 = ConcreteCommand1(receiver)
        val command2 = ConcreteCommand2(receiver)
        
        // 创建调用者
        val invoker = Invoker()
        
        // 设置命令
        invoker.setCommand(command1)
        // 执行命令
        invoker.executeCommand()
        
        // 设置命令
        invoker.setCommand(command2)
        // 执行命令
        invoker.executeCommand()
        
        Log.d("DesignPatterns", "=== 命令模式示例完成 ===")
    }
    
    /**
     * 模板方法模式示例
     * 解决问题：定义一个算法的骨架，而将一些步骤延迟到子类中，使得子类可以不改变一个算法的结构即可重定义该算法的某些特定步骤
     */
    private fun templateMethodPatternExample() {
        Log.d("DesignPatterns", "=== 运行模板方法模式示例 ===")
        
        // 创建具体子类
        val concreteClass1 = ConcreteClass1()
        val concreteClass2 = ConcreteClass2()
        
        // 调用模板方法
        concreteClass1.templateMethod()
        concreteClass2.templateMethod()
        
        Log.d("DesignPatterns", "=== 模板方法模式示例完成 ===")
    }
    
    /**
     * 命令接口
     */
    interface Command {
        /**
         * 执行命令
         */
        fun execute()
    }
    
    /**
     * 接收者
     */
    class Receiver {
        /**
         * 操作 1
         */
        fun action1() {
            Log.d("DesignPatterns", "Receiver.action1() called")
        }
        
        /**
         * 操作 2
         */
        fun action2() {
            Log.d("DesignPatterns", "Receiver.action2() called")
        }
    }
    
    /**
     * 具体命令 1
     */
    class ConcreteCommand1(private val receiver: Receiver) : Command {
        override fun execute() {
            Log.d("DesignPatterns", "ConcreteCommand1.execute() called")
            receiver.action1()
        }
    }
    
    /**
     * 具体命令 2
     */
    class ConcreteCommand2(private val receiver: Receiver) : Command {
        override fun execute() {
            Log.d("DesignPatterns", "ConcreteCommand2.execute() called")
            receiver.action2()
        }
    }
    
    /**
     * 调用者
     */
    class Invoker {
        private var command: Command? = null
        
        /**
         * 设置命令
         */
        fun setCommand(command: Command) {
            this.command = command
            Log.d("DesignPatterns", "Invoker.setCommand() called")
        }
        
        /**
         * 执行命令
         */
        fun executeCommand() {
            Log.d("DesignPatterns", "Invoker.executeCommand() called")
            command?.execute()
        }
    }
    
    /**
     * 抽象类
     */
    abstract class AbstractClass {
        /**
         * 模板方法
         */
        fun templateMethod() {
            Log.d("DesignPatterns", "AbstractClass.templateMethod() called")
            // 调用基本方法
            primitiveOperation1()
            primitiveOperation2()
            hook()
        }
        
        /**
         * 基本方法 1
         */
        abstract fun primitiveOperation1()
        
        /**
         * 基本方法 2
         */
        abstract fun primitiveOperation2()
        
        /**
         * 钩子方法
         */
        open fun hook() {
            // 默认实现
        }
    }
    
    /**
     * 具体类 1
     */
    class ConcreteClass1 : AbstractClass() {
        override fun primitiveOperation1() {
            Log.d("DesignPatterns", "ConcreteClass1.primitiveOperation1() called")
        }
        
        override fun primitiveOperation2() {
            Log.d("DesignPatterns", "ConcreteClass1.primitiveOperation2() called")
        }
        
        override fun hook() {
            Log.d("DesignPatterns", "ConcreteClass1.hook() called")
        }
    }
    
    /**
     * 具体类 2
     */
    class ConcreteClass2 : AbstractClass() {
        override fun primitiveOperation1() {
            Log.d("DesignPatterns", "ConcreteClass2.primitiveOperation1() called")
        }
        
        override fun primitiveOperation2() {
            Log.d("DesignPatterns", "ConcreteClass2.primitiveOperation2() called")
        }
        // 不重写钩子方法，使用默认实现
    }
}