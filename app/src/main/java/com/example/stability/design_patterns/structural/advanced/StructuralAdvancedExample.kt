package com.example.stability.design_patterns.structural.advanced

import android.util.Log

/**
 * 结构型设计模式高级示例
 * 展示结构型设计模式的高级功能，如组合模式、外观模式等
 */
class StructuralAdvancedExample {
    
    /**
     * 运行所有结构型高级示例
     */
    fun runAllExamples() {
        Log.d("DesignPatterns", "=== StructuralAdvancedExample.runAllExamples called ===")
        Log.d("DesignPatterns", "Thread ID: ${Thread.currentThread().id}")
        
        // 运行组合模式示例
        compositePatternExample()
        
        // 运行外观模式示例
        facadePatternExample()
        
        Log.d("DesignPatterns", "=== StructuralAdvancedExample.runAllExamples completed ===")
    }
    
    /**
     * 组合模式示例
     * 解决问题：将对象组合成树形结构以表示"部分-整体"的层次结构，使得客户端对单个对象和组合对象的使用具有一致性
     */
    private fun compositePatternExample() {
        Log.d("DesignPatterns", "=== 运行组合模式示例 ===")
        
        // 创建叶节点
        val leaf1 = Leaf("Leaf 1")
        val leaf2 = Leaf("Leaf 2")
        val leaf3 = Leaf("Leaf 3")
        
        // 创建组合节点
        val composite1 = Composite("Composite 1")
        val composite2 = Composite("Composite 2")
        
        // 构建树形结构
        composite1.add(leaf1)
        composite1.add(leaf2)
        composite2.add(leaf3)
        composite1.add(composite2)
        
        // 使用组合
        composite1.operation()
        
        Log.d("DesignPatterns", "=== 组合模式示例完成 ===")
    }
    
    /**
     * 外观模式示例
     * 解决问题：为子系统中的一组接口提供一个统一的入口，使子系统更容易使用
     */
    private fun facadePatternExample() {
        Log.d("DesignPatterns", "=== 运行外观模式示例 ===")
        
        // 创建外观
        val facade = Facade()
        
        // 使用外观
        facade.operationA()
        facade.operationB()
        
        Log.d("DesignPatterns", "=== 外观模式示例完成 ===")
    }
    
    /**
     * 组件接口
     */
    interface Component {
        /**
         * 操作方法
         */
        fun operation()
        
        /**
         * 添加子组件
         */
        fun add(component: Component)
        
        /**
         * 移除子组件
         */
        fun remove(component: Component)
        
        /**
         * 获取子组件
         */
        fun getChild(index: Int): Component?
    }
    
    /**
     * 叶节点
     */
    class Leaf(private val name: String) : Component {
        override fun operation() {
            Log.d("DesignPatterns", "Leaf.operation() called: $name")
        }
        
        override fun add(component: Component) {
            // 叶节点不支持添加子组件
        }
        
        override fun remove(component: Component) {
            // 叶节点不支持移除子组件
        }
        
        override fun getChild(index: Int): Component? {
            // 叶节点没有子组件
            return null
        }
    }
    
    /**
     * 组合节点
     */
    class Composite(private val name: String) : Component {
        private val children = mutableListOf<Component>()
        
        override fun operation() {
            Log.d("DesignPatterns", "Composite.operation() called: $name")
            // 递归调用子组件的操作
            for (child in children) {
                child.operation()
            }
        }
        
        override fun add(component: Component) {
            children.add(component)
            Log.d("DesignPatterns", "Composite.add() called: $name added ${component.javaClass.simpleName}")
        }
        
        override fun remove(component: Component) {
            children.remove(component)
            Log.d("DesignPatterns", "Composite.remove() called: $name removed ${component.javaClass.simpleName}")
        }
        
        override fun getChild(index: Int): Component? {
            return if (index < children.size) children[index] else null
        }
    }
    
    /**
     * 子系统 A
     */
    class SubsystemA {
        /**
         * 操作 A
         */
        fun operationA() {
            Log.d("DesignPatterns", "SubsystemA.operationA() called")
        }
    }
    
    /**
     * 子系统 B
     */
    class SubsystemB {
        /**
         * 操作 B
         */
        fun operationB() {
            Log.d("DesignPatterns", "SubsystemB.operationB() called")
        }
    }
    
    /**
     * 子系统 C
     */
    class SubsystemC {
        /**
         * 操作 C
         */
        fun operationC() {
            Log.d("DesignPatterns", "SubsystemC.operationC() called")
        }
    }
    
    /**
     * 外观类
     */
    class Facade {
        private val subsystemA = SubsystemA()
        private val subsystemB = SubsystemB()
        private val subsystemC = SubsystemC()
        
        /**
         * 操作 A
         */
        fun operationA() {
            Log.d("DesignPatterns", "Facade.operationA() called")
            subsystemA.operationA()
            subsystemC.operationC()
        }
        
        /**
         * 操作 B
         */
        fun operationB() {
            Log.d("DesignPatterns", "Facade.operationB() called")
            subsystemB.operationB()
            subsystemC.operationC()
        }
    }
}