package com.example.stability.data_structures.basic

import android.util.Log

/**
 * 栈是一种后进先出（LIFO）的数据结构
 * 特点：push（入栈）和 pop（出栈）操作的时间复杂度都是 O(1)
 */
class StackExample {
    
    private val stack = mutableListOf<Int>()
    
    /**
     * 运行栈示例
     */
    fun runStackExample() {
        Log.d("DataStructures", "=== StackExample.runStackExample called ===")
        Log.d("DataStructures", "Thread ID: ${Thread.currentThread().id}")
        
        // 1. 入栈操作
        push(1)
        push(2)
        push(3)
        push(4)
        push(5)
        Log.d("DataStructures", "入栈操作完成，栈的大小: ${stack.size}")
        printStack()
        
        // 2. 查看栈顶元素
        val topElement = peek()
        Log.d("DataStructures", "栈顶元素: $topElement")
        
        // 3. 出栈操作
        val poppedElement = pop()
        Log.d("DataStructures", "出栈元素: $poppedElement")
        printStack()
        
        // 4. 再次出栈
        pop()
        Log.d("DataStructures", "再次出栈后:")
        printStack()
        
        // 5. 检查栈是否为空
        val isEmpty = isEmpty()
        Log.d("DataStructures", "栈是否为空: $isEmpty")
        
        // 6. 清空栈
        clear()
        Log.d("DataStructures", "清空栈后:")
        printStack()
        Log.d("DataStructures", "栈是否为空: ${isEmpty()}")
        
        // 7. 测试栈的应用 - 括号匹配
        val expression = "{[()()]}"
        val isBalanced = checkParenthesesBalance(expression)
        Log.d("DataStructures", "表达式 $expression 的括号是否匹配: $isBalanced")
        
        val expression2 = "{[()()]}"
        val isBalanced2 = checkParenthesesBalance(expression2)
        Log.d("DataStructures", "表达式 $expression2 的括号是否匹配: $isBalanced2")
        
        Log.d("DataStructures", "=== StackExample.runStackExample completed ===")
    }
    
    /**
     * 入栈操作
     * 时间复杂度: O(1)
     */
    private fun push(value: Int) {
        stack.add(value)
        Log.d("DataStructures", "入栈: $value")
    }
    
    /**
     * 出栈操作
     * 时间复杂度: O(1)
     */
    private fun pop(): Int? {
        if (isEmpty()) {
            Log.d("DataStructures", "栈为空，无法出栈")
            return null
        }
        
        val value = stack.removeAt(stack.size - 1)
        Log.d("DataStructures", "出栈: $value")
        return value
    }
    
    /**
     * 查看栈顶元素
     * 时间复杂度: O(1)
     */
    private fun peek(): Int? {
        if (isEmpty()) {
            Log.d("DataStructures", "栈为空，无法查看栈顶元素")
            return null
        }
        
        return stack[stack.size - 1]
    }
    
    /**
     * 检查栈是否为空
     */
    private fun isEmpty(): Boolean {
        return stack.isEmpty()
    }
    
    /**
     * 清空栈
     */
    private fun clear() {
        stack.clear()
        Log.d("DataStructures", "栈已清空")
    }
    
    /**
     * 打印栈
     */
    private fun printStack() {
        Log.d("DataStructures", "栈内容: $stack")
    }
    
    /**
     * 检查括号是否匹配
     * 应用：编译器语法检查、表达式求值等
     */
    private fun checkParenthesesBalance(expression: String): Boolean {
        val stack = mutableListOf<Char>()
        
        for (char in expression) {
            when (char) {
                '(', '[', '{' -> stack.add(char)
                ')' -> if (stack.isEmpty() || stack.removeAt(stack.size - 1) != '(') return false
                ']' -> if (stack.isEmpty() || stack.removeAt(stack.size - 1) != '[') return false
                '}' -> if (stack.isEmpty() || stack.removeAt(stack.size - 1) != '{') return false
            }
        }
        
        return stack.isEmpty()
    }
}