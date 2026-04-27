// package - 包声明关键字
// 作用：指定当前文件所在的包，用于组织代码结构
// 使用方式：package 包名
package com.example.stability.kotlin_learning.intermediate

// import - 导入语句关键字
// 作用：导入其他包中的类或函数，以便在当前文件中使用
// 使用方式：import 包名.类名
import android.util.Log

/**
 * Kotlin 进阶特性示例
 */
// class - 类声明关键字
// 作用：定义一个类，用于封装数据和行为
// 使用方式：class 类名 {
//     // 类成员
// }
class AdvancedFeatures {
    
    /**
     * 运行进阶特性示例
     */
    // fun - 函数声明关键字
    // 作用：定义一个函数，用于执行特定的任务
    // 使用方式：fun 函数名(参数列表): 返回类型 {
    //     // 函数体
    // }
    fun runAdvancedFeatures() {
        Log.d("KotlinLearning", "=== AdvancedFeatures.runAdvancedFeatures called ===")
        Log.d("KotlinLearning", "Thread ID: ${Thread.currentThread().id}")
        
        // 打印调用堆栈
        Log.d("KotlinLearning", "Call stack:")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) {
                Log.d("KotlinLearning", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        
        // 空安全
        testNullSafety()
        
        // 扩展函数
        testExtensionFunctions()
        
        // Lambda 表达式
        testLambdaExpressions()
        
        // 集合操作
        testCollectionOperations()
        
        Log.d("KotlinLearning", "=== AdvancedFeatures.runAdvancedFeatures completed ===")
    }
    
    /**
     * 测试空安全
     */
    // private - 访问修饰符关键字
    // 作用：限制函数或属性只能在当前类中访问
    // 使用方式：private fun 函数名() { ... }
    private fun testNullSafety() {
        Log.d("KotlinLearning", "=== testNullSafety called ===")
        
        // 可空类型
        // var - 可变变量声明关键字
        // 作用：声明一个可变的变量，值可以随时更改
        // 使用方式：var 变量名: 类型? = 值
        // ? - 可空类型标记
        // 作用：表示变量可以为 null
        // 使用方式：var 变量名: 类型? = 值
        var nullableString: String? = "Hello"
        Log.d("KotlinLearning", "nullableString: $nullableString")
        
        // 安全调用
        // val - 不可变变量声明关键字
        // 作用：声明一个不可变（只读）的变量，值一旦赋值就不能更改
        // 使用方式：val 变量名: 类型 = 值
        // ?. - 安全调用操作符
        // 作用：如果对象不为 null，则调用方法或访问属性；否则返回 null
        // 使用方式：对象?.方法() 或 对象?.属性
        val length = nullableString?.length
        Log.d("KotlinLearning", "length: $length")
        
        // Elvis 操作符
        // ?: - Elvis 操作符
        // 作用：如果左侧表达式不为 null，则返回左侧表达式的值；否则返回右侧表达式的值
        // 使用方式：表达式1 ?: 表达式2
        val result = nullableString ?: "Default"
        Log.d("KotlinLearning", "result: $result")
        
        // 非空断言
        nullableString = "World"
        // !! - 非空断言操作符
        // 作用：断言对象不为 null，如果为 null 则抛出 NullPointerException
        // 使用方式：对象!!.方法() 或 对象!!.属性
        val nonNullLength = nullableString!!.length
        Log.d("KotlinLearning", "nonNullLength: $nonNullLength")
        
        Log.d("KotlinLearning", "=== testNullSafety completed ===")
    }
    
    /**
     * 测试扩展函数
     */
    private fun testExtensionFunctions() {
        Log.d("KotlinLearning", "=== testExtensionFunctions called ===")
        
        val text = "Hello, Kotlin!"
        Log.d("KotlinLearning", "Original text: $text")
        
        // 使用扩展函数
        val reversed = text.reverse()
        Log.d("KotlinLearning", "Reversed text: $reversed")
        
        val titleCase = text.toTitleCase()
        Log.d("KotlinLearning", "Title case text: $titleCase")
        
        Log.d("KotlinLearning", "=== testExtensionFunctions completed ===")
    }
    
    /**
     * 测试 Lambda 表达式
     */
    private fun testLambdaExpressions() {
        Log.d("KotlinLearning", "=== testLambdaExpressions called ===")
        
        // 高阶函数
        // Lambda 表达式语法
        // 作用：创建一个匿名函数
        // 使用方式：{ 参数列表 -> 表达式 }
        val result = calculate(5, 3) { a, b -> a + b }
        Log.d("KotlinLearning", "Result of addition: $result")
        
        val result2 = calculate(5, 3) { a, b -> a * b }
        Log.d("KotlinLearning", "Result of multiplication: $result2")
        
        // Lambda 作为参数
        // listOf - 集合创建函数
        // 作用：创建一个不可变的列表
        // 使用方式：listOf(元素1, 元素2, ...)
        val numbers = listOf(1, 2, 3, 4, 5)
        // filter - 集合过滤操作
        // 作用：根据条件过滤集合中的元素
        // 使用方式：集合.filter { 条件 }
        // it - Lambda 表达式中的默认参数名
        // 作用：当 Lambda 表达式只有一个参数时，可以使用 it 代替参数名
        val evenNumbers = numbers.filter { it % 2 == 0 }
        Log.d("KotlinLearning", "Even numbers: $evenNumbers")
        
        Log.d("KotlinLearning", "=== testLambdaExpressions completed ===")
    }
    
    /**
     * 测试集合操作
     */
    private fun testCollectionOperations() {
        Log.d("KotlinLearning", "=== testCollectionOperations called ===")
        
        val numbers = listOf(1, 2, 3, 4, 5)
        Log.d("KotlinLearning", "Original list: $numbers")
        
        // map 操作
        // map - 集合映射操作
        // 作用：对集合中的每个元素应用一个函数，返回一个新的集合
        // 使用方式：集合.map { 转换函数 }
        val doubled = numbers.map { it * 2 }
        Log.d("KotlinLearning", "Doubled list: $doubled")
        
        // filter 操作
        // filter - 集合过滤操作
        // 作用：根据条件过滤集合中的元素
        // 使用方式：集合.filter { 条件 }
        val greaterThan2 = numbers.filter { it > 2 }
        Log.d("KotlinLearning", "Numbers greater than 2: $greaterThan2")
        
        // reduce 操作
        // reduce - 集合归约操作
        // 作用：将集合中的元素归约为一个单一的值
        // 使用方式：集合.reduce { 累加器, 当前元素 -> 操作 }
        val sum = numbers.reduce { acc, i -> acc + i }
        Log.d("KotlinLearning", "Sum of list: $sum")
        
        Log.d("KotlinLearning", "=== testCollectionOperations completed ===")
    }
    
    /**
     * 高阶函数示例
     */
    // 高阶函数 - 接受函数作为参数或返回函数的函数
    // 作用：实现函数式编程范式
    // 使用方式：fun 函数名(参数, 函数参数: (参数类型) -> 返回类型): 返回类型 {
    //     // 函数体
    // }
    private fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
        Log.d("KotlinLearning", "calculate called with a: $a, b: $b")
        val result = operation(a, b)
        Log.d("KotlinLearning", "calculate returned: $result")
        return result
    }
}

/**
 * 字符串扩展函数：反转字符串
 */
// 扩展函数 - 为现有类添加新的函数
// 作用：在不修改原有类的情况下，为其添加新的功能
// 使用方式：fun 接收者类型.函数名(参数列表): 返回类型 {
//     // 函数体
// }
fun String.reverse(): String {
    return this.reversed()
}

/**
 * 字符串扩展函数：转换为标题大小写
 */
fun String.toTitleCase(): String {
    return this.split(" ")
        .joinToString(" ") { it.capitalize() }
}
