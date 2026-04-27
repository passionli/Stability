// package - 包声明关键字
// 作用：指定当前文件所在的包，用于组织代码结构
// 使用方式：package 包名
package com.example.stability.kotlin_learning.basic

// import - 导入语句关键字
// 作用：导入其他包中的类或函数，以便在当前文件中使用
// 使用方式：import 包名.类名
import android.util.Log

/**
 * Kotlin 基础语法示例
 */
// class - 类声明关键字
// 作用：定义一个类，用于封装数据和行为
// 使用方式：class 类名 {
//     // 类成员
// }
class BasicSyntax {
    
    /**
     * 运行基础语法示例
     */
    // fun - 函数声明关键字
    // 作用：定义一个函数，用于执行特定的任务
    // 使用方式：fun 函数名(参数列表): 返回类型 {
    //     // 函数体
    // }
    fun runBasicSyntax() {
        Log.d("KotlinLearning", "=== BasicSyntax.runBasicSyntax called ===")
        Log.d("KotlinLearning", "Thread ID: ${Thread.currentThread().id}")
        
        // 打印调用堆栈
        Log.d("KotlinLearning", "Call stack:")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) {
                Log.d("KotlinLearning", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        
        // 变量声明
        // val - 不可变变量声明关键字
        // 作用：声明一个不可变（只读）的变量，值一旦赋值就不能更改
        // 使用方式：val 变量名: 类型 = 值
        val name = "Kotlin"
        
        // var - 可变变量声明关键字
        // 作用：声明一个可变的变量，值可以随时更改
        // 使用方式：var 变量名: 类型 = 值
        var age = 5
        
        Log.d("KotlinLearning", "name: $name, age: $age")
        
        // 类型推断
        val message = "Hello, $name!"
        Log.d("KotlinLearning", "message: $message")
        
        // 字符串模板
        val greeting = "Hello, ${name.uppercase()}!"
        Log.d("KotlinLearning", "greeting: $greeting")
        
        // 条件表达式
        // if - 条件表达式关键字
        // 作用：根据条件执行不同的代码块
        // 使用方式：if (条件) 表达式1 else 表达式2
        val result = if (age > 3) "成熟的语言" else "年轻的语言"
        Log.d("KotlinLearning", "result: $result")
        
        // 循环
        Log.d("KotlinLearning", "循环示例:")
        
        // for - 循环关键字
        // 作用：遍历一个范围或集合中的元素
        // 使用方式：for (变量 in 范围/集合) {
        //     // 循环体
        // }
        // in - 遍历操作符
        // 作用：用于检查元素是否在一个范围或集合中
        // 使用方式：元素 in 范围/集合
        for (i in 1..5) {
            Log.d("KotlinLearning", "i: $i")
        }
        
        // 函数调用
        val sum = add(3, 5)
        Log.d("KotlinLearning", "sum: $sum")
        
        Log.d("KotlinLearning", "=== BasicSyntax.runBasicSyntax completed ===")
    }
    
    /**
     * 加法函数
     * @param a 第一个参数
     * @param b 第二个参数
     * @return 两个参数的和
     */
    // private - 访问修饰符关键字
    // 作用：限制函数或属性只能在当前类中访问
    // 使用方式：private fun 函数名() { ... }
    private fun add(a: Int, b: Int): Int {
        Log.d("KotlinLearning", "add called with a: $a, b: $b")
        val result = a + b
        Log.d("KotlinLearning", "add returned: $result")
        
        // return - 返回语句关键字
        // 作用：从函数中返回一个值
        // 使用方式：return 表达式
        return result
    }
}
