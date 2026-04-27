// package - 包声明关键字
// 作用：指定当前文件所在的包，用于组织代码结构
// 使用方式：package 包名
package com.example.stability.kotlin_learning.basic

// import - 导入语句关键字
// 作用：导入其他包中的类或函数，以便在当前文件中使用
// 使用方式：import 包名.类名
import android.util.Log

/**
 * Kotlin 类和对象示例
 */

// 基类
// open - 开放修饰符关键字
// 作用：允许其他类继承当前类，或允许其他类重写当前方法
// 使用方式：open class 类名 { ... }
// val - 不可变属性声明关键字
// 作用：在构造函数中声明一个不可变的属性
// 使用方式：class 类名(val 属性名: 类型) {
//     // 类体
// }
open class Person(val name: String, val age: Int) {
    // open - 开放修饰符关键字
    // 作用：允许子类重写当前方法
    // 使用方式：open fun 方法名() { ... }
    open fun introduce() {
        Log.d("KotlinLearning", "我叫 $name，今年 $age 岁")
    }
}

// 子类
// class - 类声明关键字
// 作用：定义一个类，用于封装数据和行为
// 使用方式：class 类名(参数列表) : 父类(参数列表) {
//     // 类体
// }
// : - 继承关键字
// 作用：指定当前类的父类
// 使用方式：class 子类 : 父类(参数列表) {
//     // 类体
// }
class Student(name: String, age: Int, val studentId: String) : Person(name, age) {
    // override - 重写关键字
    // 作用：重写父类的方法
    // 使用方式：override fun 方法名() { ... }
    override fun introduce() {
        // super - 父类引用关键字
        // 作用：引用父类的方法或属性
        // 使用方式：super.方法名() 或 super.属性名
        super.introduce()
        Log.d("KotlinLearning", "我的学号是 $studentId")
    }
}

// 单例对象
// object - 单例对象关键字
// 作用：创建一个单例对象，整个应用中只有一个实例
// 使用方式：object 对象名 {
//     // 对象成员
// }
object Singleton {
    val instance = Singleton
    fun doSomething() {
        Log.d("KotlinLearning", "Singleton.doSomething called")
    }
}

/**
 * 类和对象示例类
 */
class ClassesAndObjects {
    
    /**
     * 运行类和对象示例
     */
    fun runClassesAndObjects() {
        Log.d("KotlinLearning", "=== ClassesAndObjects.runClassesAndObjects called ===")
        Log.d("KotlinLearning", "Thread ID: ${Thread.currentThread().id}")
        
        // 打印调用堆栈
        Log.d("KotlinLearning", "Call stack:")
        Thread.currentThread().stackTrace.forEachIndexed { index, element ->
            if (index > 2) {
                Log.d("KotlinLearning", "  $index: ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        
        // 创建 Person 对象
        val person = Person("张三", 30)
        Log.d("KotlinLearning", "Created person: ${person.name}, ${person.age}")
        person.introduce()
        
        // 创建 Student 对象
        val student = Student("李四", 20, "2023001")
        Log.d("KotlinLearning", "Created student: ${student.name}, ${student.age}, ${student.studentId}")
        student.introduce()
        
        // 使用单例对象
        Log.d("KotlinLearning", "Using singleton:")
        Singleton.doSomething()
        
        Log.d("KotlinLearning", "=== ClassesAndObjects.runClassesAndObjects completed ===")
    }
}
