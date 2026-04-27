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
        
        // 循环
        // while - 循环关键字
        // 作用：当条件为真时，重复执行代码块
        // 使用方式：while (条件) {
        //     // 循环体
        // }
        Log.d("KotlinLearning", "while 循环示例:")
        var count = 1
        while (count <= 3) {
            Log.d("KotlinLearning", "count: $count")
            count++
        }
        
        // do-while - 循环关键字
        // 作用：先执行一次代码块，然后当条件为真时重复执行
        // 使用方式：do {
        //     // 循环体
        // } while (条件)
        Log.d("KotlinLearning", "do-while 循环示例:")
        var i = 1
        do {
            Log.d("KotlinLearning", "i: $i")
            i++
        } while (i <= 3)
        
        // 异常处理
        // try-catch-finally - 异常处理关键字
        // 作用：捕获和处理异常
        // 使用方式：try {
        //     // 可能抛出异常的代码
        // } catch (e: 异常类型) {
        //     // 异常处理代码
        // } finally {
        //     // 无论是否异常都会执行的代码
        // }
        Log.d("KotlinLearning", "异常处理示例:")
        try {
            val result = 10 / 0
            Log.d("KotlinLearning", "result: $result")
        } catch (e: ArithmeticException) {
            Log.d("KotlinLearning", "捕获到异常: ${e.message}")
        } finally {
            Log.d("KotlinLearning", "finally 块执行")
        }
        
        // throw - 抛出异常关键字
        // 作用：主动抛出异常
        // 使用方式：throw 异常对象
        Log.d("KotlinLearning", "抛出异常示例:")
        try {
            throw IllegalArgumentException("参数错误")
        } catch (e: IllegalArgumentException) {
            Log.d("KotlinLearning", "捕获到异常: ${e.message}")
        }
        
        // 类型检查和转换
        // is - 类型检查操作符
        // 作用：检查对象是否为指定类型
        // 使用方式：对象 is 类型
        val obj: Any = "Hello"
        if (obj is String) {
            Log.d("KotlinLearning", "obj 是 String 类型: ${obj.length}")
        }
        
        // as - 类型转换操作符
        // 作用：将对象转换为指定类型
        // 使用方式：对象 as 类型
        val str = obj as String
        Log.d("KotlinLearning", "类型转换: $str")
        
        // lateinit - 延迟初始化关键字
        // 作用：声明一个非空类型的变量，但延迟初始化
        // 使用方式：lateinit var 变量名: 类型
        lateinit var lateinitVar: String
        lateinitVar = "Late initialized"
        Log.d("KotlinLearning", "lateinit 变量: $lateinitVar")
        
        // val - 不可变变量（在函数中使用）
        // 作用：声明一个不可变（只读）的变量，值一旦赋值就不能更改
        // 注意：const 只能在顶层或伴生对象中使用
        val MAX_VALUE = 100
        Log.d("KotlinLearning", "常量: $MAX_VALUE")
        
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
