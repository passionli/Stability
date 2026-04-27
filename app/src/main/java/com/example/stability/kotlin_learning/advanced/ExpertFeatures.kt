// package - 包声明关键字
// 作用：指定当前文件所在的包，用于组织代码结构
// 使用方式：package 包名
package com.example.stability.kotlin_learning.advanced

// import - 导入语句关键字
// 作用：导入其他包中的类或函数，以便在当前文件中使用
// 使用方式：import 包名.类名
import android.util.Log

/**
 * Kotlin 精通级特性示例
 */
// class - 类声明关键字
// 作用：定义一个类，用于封装数据和行为
// 使用方式：class 类名 {
//     // 类成员
// }
class ExpertFeatures {
    
    /**
     * 运行精通级特性示例
     */
    // fun - 函数声明关键字
    // 作用：定义一个函数，用于执行特定的任务
    // 使用方式：fun 函数名(参数列表): 返回类型 {
    //     // 函数体
    // }
    fun runExpertFeatures() {
        Log.d("KotlinLearning", "=== ExpertFeatures.runExpertFeatures called ===")
        Log.d("KotlinLearning", "Thread ID: ${Thread.currentThread().id}")
        
        // 打印调用堆栈
        Log.d("KotlinLearning", "Call stack:")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) {
                Log.d("KotlinLearning", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        
        // 测试数据类
        testDataClasses()
        
        // 测试密封类
        testSealedClasses()
        
        // 测试泛型
        testGenerics()
        
        // 测试委托
        testDelegation()
        
        Log.d("KotlinLearning", "=== ExpertFeatures.runExpertFeatures completed ===")
    }
    
    /**
     * 测试数据类
     */
    // private - 访问修饰符关键字
    // 作用：限制函数或属性只能在当前类中访问
    // 使用方式：private fun 函数名() { ... }
    private fun testDataClasses() {
        Log.d("KotlinLearning", "=== testDataClasses called ===")
        
        // 创建数据类实例
        // val - 不可变变量声明关键字
        // 作用：声明一个不可变（只读）的变量，值一旦赋值就不能更改
        // 使用方式：val 变量名: 类型 = 值
        val user1 = User(1, "张三", "zhangsan@example.com")
        Log.d("KotlinLearning", "user1: $user1")
        
        // 复制数据类实例
        // copy - 数据类的复制函数
        // 作用：创建数据类的副本，并可以修改部分属性
        // 使用方式：数据类实例.copy(属性1 = 值1, 属性2 = 值2, ...)
        val user2 = user1.copy(email = "zhangsan_new@example.com")
        Log.d("KotlinLearning", "user2: $user2")
        
        // 解构数据类
        // 解构声明 - 同时声明多个变量
        // 作用：从对象中提取多个属性并赋值给多个变量
        // 使用方式：val (变量1, 变量2, ...) = 对象
        val (id, name, email) = user1
        Log.d("KotlinLearning", "Destructured: id=$id, name=$name, email=$email")
        
        Log.d("KotlinLearning", "=== testDataClasses completed ===")
    }
    
    /**
     * 测试密封类
     */
    private fun testSealedClasses() {
        Log.d("KotlinLearning", "=== testSealedClasses called ===")
        
        // 测试密封类
        val result1 = Result.Success("Operation successful")
        val result2 = Result.Error(Exception("Operation failed"))
        
        handleResult(result1)
        handleResult(result2)
        
        Log.d("KotlinLearning", "=== testSealedClasses completed ===")
    }
    
    /**
     * 处理结果
     */
    private fun handleResult(result: Result) {
        // when - 条件表达式关键字
        // 作用：根据不同的条件执行不同的代码块
        // 使用方式：when (表达式) {
        //     条件1 -> 代码块1
        //     条件2 -> 代码块2
        //     ...
        // }
        // is - 类型检查操作符
        // 作用：检查对象是否为指定类型
        // 使用方式：对象 is 类型
        when (result) {
            is Result.Success -> Log.d("KotlinLearning", "Success: ${result.data}")
            is Result.Error -> Log.d("KotlinLearning", "Error: ${result.exception.message}")
        }
    }
    
    /**
     * 测试泛型
     */
    private fun testGenerics() {
        Log.d("KotlinLearning", "=== testGenerics called ===")
        
        // 测试泛型函数
        val intList = listOf(1, 2, 3, 4, 5)
        val doubleList = listOf(1.1, 2.2, 3.3, 4.4, 5.5)
        
        val intSum = sum(intList)
        val doubleSum = sum(doubleList)
        
        Log.d("KotlinLearning", "Int sum: $intSum")
        Log.d("KotlinLearning", "Double sum: $doubleSum")
        
        // 测试泛型类
        val intBox = Box(42)
        val stringBox = Box("Hello")
        
        Log.d("KotlinLearning", "Int box: ${intBox.value}")
        Log.d("KotlinLearning", "String box: ${stringBox.value}")
        
        Log.d("KotlinLearning", "=== testGenerics completed ===")
    }
    
    /**
     * 测试委托
     */
    private fun testDelegation() {
        Log.d("KotlinLearning", "=== testDelegation called ===")
        
        // 创建委托实例
        val delegate = RealDataSource()
        val dataSource = DataSourceWrapper(delegate)
        
        // 使用委托
        dataSource.saveData("key1", "value1")
        val value = dataSource.getData("key1")
        Log.d("KotlinLearning", "Retrieved value: $value")
        
        Log.d("KotlinLearning", "=== testDelegation completed ===")
    }
    
    /**
     * 泛型求和函数
     */
    // private - 访问修饰符关键字
    // 作用：限制函数或属性只能在当前类中访问
    // 使用方式：private fun 函数名() { ... }
    // <T : Number> - 泛型参数及上界
    // 作用：使函数可以处理不同类型的数据，并限制类型范围
    // 使用方式：fun <T : 上界类型> 函数名(): T { ... }
    private fun <T : Number> sum(list: List<T>): Double {
        Log.d("KotlinLearning", "sum called with list: $list")
        val result = list.sumOf { it.toDouble() }
        Log.d("KotlinLearning", "sum returned: $result")
        return result
    }
}

/**
 * 数据类示例
 */
// data class - 数据类关键字
// 作用：创建一个主要用于存储数据的类，自动生成 equals()、hashCode()、toString()、copy() 等方法
// 使用方式：data class 类名(val 属性1: 类型, val 属性2: 类型, ...)
data class User(val id: Int, val name: String, val email: String)

/**
 * 密封类示例
 */
// sealed class - 密封类关键字
// 作用：创建一个受限的类层次结构，子类必须在同一个文件中定义
// 使用方式：sealed class 类名 {
//     子类1
//     子类2
//     ...
// }
sealed class Result {
    // data class - 数据类关键字
    // 作用：创建一个主要用于存储数据的类，自动生成 equals()、hashCode()、toString()、copy() 等方法
    // 使用方式：data class 类名(val 属性1: 类型, val 属性2: 类型, ...)
    data class Success(val data: String) : Result()
    data class Error(val exception: Exception) : Result()
}

/**
 * 泛型类示例
 */
// class - 类声明关键字
// 作用：定义一个类，用于封装数据和行为
// <T> - 泛型参数
// 作用：使类可以处理不同类型的数据
// 使用方式：class 类名<T>(val 属性: T) {
//     // 类体
// }
class Box<T>(val value: T)

/**
 * 数据源接口
 */
// interface - 接口声明关键字
// 作用：定义一个接口，用于规定类的行为
// 使用方式：interface 接口名 {
//     方法声明
//     属性声明
// }
interface DataSource {
    // fun - 函数声明关键字
    // 作用：定义一个函数，用于执行特定的任务
    // 使用方式：fun 函数名(参数列表): 返回类型
    fun saveData(key: String, value: String)
    fun getData(key: String): String?
}

/**
 * 真实数据源实现
 */
// class - 类声明关键字
// 作用：定义一个类，用于封装数据和行为
// : - 继承关键字
// 作用：指定当前类实现的接口
// 使用方式：class 类名 : 接口名 {
//     // 接口方法实现
// }
class RealDataSource : DataSource {
    // private - 访问修饰符关键字
    // 作用：限制属性只能在当前类中访问
    // 使用方式：private val 属性名: 类型 = 值
    // val - 不可变变量声明关键字
    // 作用：声明一个不可变（只读）的变量，值一旦赋值就不能更改
    // 使用方式：val 变量名: 类型 = 值
    // mutableMapOf - 可变映射创建函数
    // 作用：创建一个可变的映射（键值对集合）
    // 使用方式：mutableMapOf<键类型, 值类型>()
    private val data = mutableMapOf<String, String>()
    
    // override - 重写关键字
    // 作用：重写接口或父类的方法
    // 使用方式：override fun 方法名() { ... }
    override fun saveData(key: String, value: String) {
        Log.d("KotlinLearning", "RealDataSource.saveData called with key: $key, value: $value")
        data[key] = value
    }
    
    override fun getData(key: String): String? {
        Log.d("KotlinLearning", "RealDataSource.getData called with key: $key")
        val value = data[key]
        Log.d("KotlinLearning", "RealDataSource.getData returned: $value")
        return value
    }
}

/**
 * 数据源委托包装器
 */
// class - 类声明关键字
// 作用：定义一个类，用于封装数据和行为
// : - 继承关键字
// 作用：指定当前类实现的接口
// by - 委托关键字
// 作用：将接口的实现委托给另一个对象
// 使用方式：class 类名(private val 委托对象: 接口) : 接口 by 委托对象 {
//     // 可以重写部分方法
// }
class DataSourceWrapper(private val delegate: DataSource) : DataSource by delegate {
    // 可以重写部分方法
    override fun saveData(key: String, value: String) {
        Log.d("KotlinLearning", "DataSourceWrapper.saveData called with key: $key, value: $value")
        // 调用委托的方法
        delegate.saveData(key, value)
        Log.d("KotlinLearning", "DataSourceWrapper.saveData completed")
    }
}
