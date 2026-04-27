package com.example.stability

/**
 * ============================================================
 * Kotlin 实战速查手册
 * 说明：这是一个可直接运行的 Kotlin 文件，按注释执行即可
 * 运行方式：复制到 .kt 文件 -> 右键 Run
 * ============================================================
 */

fun main() {
    println("=== Kotlin 基础语法 ===")
    basicSyntax()

    println("\n=== 函数与 Lambda ===")
    functionsAndLambdas()

    println("\n=== 类与对象 ===")
    classesAndObjects()

    println("\n=== 集合操作 ===")
    collectionsDemo()

    println("\n=== 协程基础 ===")
    coroutineBasics()

    println("\n=== 扩展函数与属性 ===")
    extensionsDemo()

    println("\n=== 作用域函数 ===")
    scopeFunctionsDemo()

    println("\n=== 内联与高阶函数 ===")
    inlineAndHigherOrder()

    println("\n=== 密封类与委托 ===")
    sealedAndDelegation()
}

// ==================== 第一部分：基础语法 ====================
fun basicSyntax() {
    // 1. 变量声明
    val immutable = "不可变变量"  // 运行时：编译为 final 变量
    var mutable = "可变变量"      // 运行时：普通变量
    mutable = "修改后的值"

    // 2. 类型推断
    val number = 42              // 类型推断为 Int
    val text = "Kotlin"          // 类型推断为 String
    val pi = 3.14                // 类型推断为 Double

    // 3. 可空类型（重点！）
    var nullable: String? = null  // 可空类型必须加 ?
    val length = nullable?.length ?: 0  // 安全调用 + Elvis 操作符
    // !! 非空断言（慎用，NPE 风险）
    // nullable!!.length  // 如果 nullable 为 null 会抛出 NPE

    // 4. 字符串模板
    val name = "World"
    val greeting = "Hello, $name! Length: ${name.length}"
    println(greeting)  // 输出: Hello, World! Length: 5

    // 5. 条件表达式
    val max = if (number > 10) number else 10
    val result = when (number) {  // 类似 switch
        in 1..10 -> "个位数"
        11, 12 -> "十一或十二"
        else -> "其他"
    }

    // 6. 循环
    for (i in 1..5) print("$i ")  // 1 2 3 4 5
    println()
    for (i in 1 until 5) print("$i ")  // 1 2 3 4
    println()
    for (i in 5 downTo 1) print("$i ")  // 5 4 3 2 1
    println()
    for (i in 1..10 step 2) print("$i ")  // 1 3 5 7 9
    println()

    // 7. 区间
    val range = 1..10
    val charRange = 'a'..'z'
    val exists = 5 in range  // true

    println("基础语法示例完成")
}

// ==================== 第二部分：函数与 Lambda ====================
fun functionsAndLambdas() {
    // 1. 基本函数
    fun add(a: Int, b: Int): Int = a + b  // 表达式函数体

    // 2. 默认参数
    fun greet(name: String = "World") = "Hello, $name!"
    println(greet())        // Hello, World!
    println(greet("Kotlin")) // Hello, Kotlin!

    // 3. 具名参数
    fun createUser(name: String, age: Int = 18, city: String = "深圳") =
        "$name($age 岁)来自$city"
    println(createUser(age = 25, name = "张三", city = "北京"))

    // 4. Lambda 表达式
    val sum: (Int, Int) -> Int = { a, b -> a + b }
    println("Lambda 求和: ${sum(3, 4)}")

    // 5. 带接收者的 Lambda（DSL 基础）
    val stringBuilder = StringBuilder().apply {
        append("Hello, ")
        append("World!")
    }
    println(stringBuilder)

    // 6. 函数类型
    val operation: (Int) -> Int = { it * 2 }
    val numbers = listOf(1, 2, 3)
    val doubled = numbers.map(operation)
    println("翻倍: $doubled")
}

// ==================== 第三部分：类与对象 ====================
fun classesAndObjects() {
    // 1. 数据类（自动生成 equals, hashCode, toString, copy）
    data class Person(val name: String, val age: Int)
    val person1 = Person("Alice", 30)
    val person2 = person1.copy(name = "Bob")
    println("数据类: $person1, copy: $person2")

    // 2. 主构造函数 vs 次构造函数
    class User(val name: String) {  // 主构造
        var age: Int = 0

        // 次构造必须委托给主构造
        constructor(name: String, age: Int) : this(name) {
            this.age = age
        }
    }

    // 3. 初始化块
    class InitDemo(val value: Int) {
        init {  // 主构造调用时执行
            println("Init 块执行, value=$value")
        }
    }
    InitDemo(42)

    // 4. 伴生对象（替代 Java static）
    class MyClass {
        companion object {
            const val CONSTANT = "常量"
            fun staticMethod() = "静态方法"
        }
    }
    println("伴生对象: ${MyClass.CONSTANT}")

    // 5. 对象表达式（匿名类）
    val obj = object {
        val x = 10
        val y = 20
    }
    println("匿名对象: x=${obj.x}, y=${obj.y}")

    // 6. 单例对象
    object Singleton {
        val version = "1.0.0"
        fun doSomething() = println("单例方法")
    }
    Singleton.doSomething()

    // 7. 枚举类
    enum class Color(val rgb: Int) {
        RED(0xFF0000),
        GREEN(0x00FF00),
        BLUE(0x0000FF);

        fun description() = when(this) {
            RED -> "红色"
            GREEN -> "绿色"
            BLUE -> "蓝色"
        }
    }
    println("枚举: ${Color.RED.description()}")
}

// ==================== 第四部分：集合操作 ====================
fun collectionsDemo() {
    // 1. 创建集合
    val list = listOf("a", "b", "c")      // 不可变
    val mutableList = mutableListOf(1, 2, 3)  // 可变
    val set = setOf(1, 2, 2, 3)           // 自动去重: [1, 2, 3]
    val map = mapOf("a" to 1, "b" to 2)

    // 2. 集合操作（链式调用）
    val numbers = (1..10).toList()
    val result = numbers
        .filter { it % 2 == 0 }           // 过滤偶数
        .map { it * it }                  // 平方
        .take(3)                          // 取前3个
        .sum()                            // 求和
    println("集合操作结果: $result")

    // 3. 分组
    val words = listOf("apple", "banana", "cherry", "date")
    val grouped = words.groupBy { it.first() }
    println("按首字母分组: $grouped")

    // 4. 折叠与规约
    val sum = numbers.reduce { acc, i -> acc + i }  // 累加
    val product = numbers.fold(1) { acc, i -> acc * i }  // 累乘
    println("累加: $sum, 累乘: $product")

    // 5. 序列（惰性求值，类似 Java Stream）
    val sequenceSum = numbers.asSequence()
        .filter {
            println("过滤: $it")
            it % 2 == 0
        }
        .map {
            println("映射: $it")
            it * 2
        }
        .take(2)
        .sum()
    println("序列结果: $sequenceSum")
    // 注意：序列是惰性的，只有遇到终端操作时才会执行
}

// ==================== 第五部分：协程基础 ====================
import kotlinx.coroutines.*

fun coroutineBasics() {
    // 注意：实际项目中需要添加依赖 kotlinx-coroutines-core

    println("开始协程演示")

    // 1. 启动协程
    runBlocking {  // 阻塞当前线程，直到协程完成
        launch {  // 启动一个子协程
            delay(100)  // 非阻塞挂起
            println("协程1执行完毕")
        }

        // 2. async/await
        val deferred = async {  // 返回 Deferred<T>
            delay(50)
            "异步结果"
        }
        println("async 结果: ${deferred.await()}")

        // 3. 取消协程
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("工作中: $i")
                    delay(500)
                }
            } finally {
                println("协程被取消，但仍在执行 finally 块")
            }
        }
        delay(1500)
        job.cancelAndJoin()  // 取消并等待结束

        // 4. 超时控制
        try {
            withTimeout(1000) {  // 1秒超时
                delay(2000)  // 延迟2秒，会超时
            }
        } catch (e: TimeoutCancellationException) {
            println("操作超时")
        }

        // 5. 协程上下文和调度器
        launch(Dispatchers.Default) {  // 默认线程池
            println("运行在 Default 调度器: ${Thread.currentThread().name}")
        }

        launch(Dispatchers.IO) {  // I/O 密集型
            println("运行在 IO 调度器: ${Thread.currentThread().name}")
        }

        launch(Dispatchers.Main) {  // 主线程（需依赖 UI 库）
            // Android 主线程
        }
    }
}

// ==================== 第六部分：扩展函数与属性 ====================
fun extensionsDemo() {
    // 1. 扩展函数
    fun String.addExclamation(): String = "$this!"
    println("Hello".addExclamation())  // Hello!

    // 2. 扩展属性
    val String.firstChar: Char
    get() = this[0]
    println("Kotlin".firstChar)  // K

    // 3. 为 List 添加扩展
    fun <T> List<T>.secondOrNull(): T? = if (size > 1) this[1] else null
    val list = listOf(1, 2, 3)
    println("第二个元素: ${list.secondOrNull()}")  // 2

    // 运行时行为：扩展函数是静态解析的
    open class Shape
    class Rectangle : Shape()

    fun Shape.getName() = "形状"
    fun Rectangle.getName() = "矩形"

    fun printName(shape: Shape) {
        println(shape.getName())  // 编译时确定调用哪个扩展
    }

    val rectangle: Shape = Rectangle()
    printName(rectangle)  // 输出: 形状（因为参数类型是 Shape）
}

// ==================== 第七部分：作用域函数 ====================
data class User(val name: String, var age: Int, var city: String = "")

fun scopeFunctionsDemo() {
    val user = User("张三", 25)

    // 1. let - 执行代码块，返回结果
    val letResult = user.let {
        println("let: ${it.name}")
        it.age + 5
    }
    println("let 返回值: $letResult")

    // 2. run - 在对象上下文中执行代码块
    val runResult = user.run {
        age = 30
        "姓名: $name, 年龄: $age"
    }
    println("run 结果: $runResult")

    // 3. with - 非扩展函数，在对象上下文中执行
    val withResult = with(user) {
        city = "深圳"
        "修改了城市: $city"
    }
    println("with 结果: $withResult")

    // 4. apply - 配置对象，返回对象本身
    val newUser = User("李四", 20).apply {
        city = "北京"
        age += 1
    }
    println("apply 结果: $newUser")

    // 5. also - 执行附加操作，返回对象本身
    val alsoUser = user.also {
        println("also: 正在处理 ${it.name}")
        it.age = 28
    }
    println("also 结果: $alsoUser")

    // 使用场景对比：
    // - 初始化对象：apply
    // - 计算并返回结果：run/with
    // - 链式调用额外操作：also
    // - 非空检查后执行：let
}

// ==================== 第八部分：内联与高阶函数 ====================
fun inlineAndHigherOrder() {
    // 1. 内联函数（减少高阶函数的运行时开销）
    inline fun measureTimeMillis(block: () -> Unit): Long {
        val start = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - start
    }

    val time = measureTimeMillis {
        // 这里的 lambda 会被内联，不会创建 Function 对象
        var sum = 0
        for (i in 1..1000000) {
            sum += i
        }
    }
    println("执行时间: ${time}ms")

    // 2. crossinline 和 noinline
    inline fun doSomething(
        crossinline onStart: () -> Unit,  // 不能直接 return
        noinline onFinish: () -> Unit     // 不内联此参数
    ) {
        onStart()
        // 这里可以保存 onFinish 引用
        val callback = onFinish
        println("进行中...")
        onFinish()
    }

    // 3. 带接收者的函数类型
    fun buildString(builder: StringBuilder.() -> Unit): String {
        val sb = StringBuilder()
        sb.builder()  // 在 StringBuilder 上下文中调用
        return sb.toString()
    }

    val built = buildString {
        append("Hello, ")
        append("World!")
    }
    println("buildString: $built")
}

// ==================== 第九部分：密封类与委托 ====================
fun sealedAndDelegation() {
    // 1. 密封类（限制继承，常用于状态机）
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val exception: Throwable) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    fun handleResult(result: Result<String>) = when(result) {
        is Result.Success -> println("成功: ${result.data}")
        is Result.Error -> println("错误: ${result.exception}")
        Result.Loading -> println("加载中...")
        // 不需要 else 分支，因为密封类已覆盖所有情况
    }

    handleResult(Result.Success("数据"))
    handleResult(Result.Loading)

    // 2. 类委托（装饰器模式）
    interface Database {
        fun save(data: String)
    }

    class RealDatabase : Database {
        override fun save(data: String) {
            println("保存数据: $data")
        }
    }

    class LoggingDatabase(private val db: Database) : Database by db {
        override fun save(data: String) {
            println("开始保存...")
            db.save(data)
            println("保存完成")
        }
    }

    val db = LoggingDatabase(RealDatabase())
    db.save("用户信息")

    // 3. 属性委托
    class Example {
        var lazyValue: String by lazy {  // 惰性初始化
            println("计算惰性值")
            "Hello"
        }

        // 可观察属性
        var observed: String by Delegates.observable("初始值") {
                property, old, new ->
            println("属性 ${property.name} 从 $old 变为 $new")
        }

        // 属性映射
        private val map = mutableMapOf<String, Any?>()
        var mapped: String by map
    }

    val example = Example()
    println("第一次访问 lazyValue: ${example.lazyValue}")
    println("第二次访问 lazyValue: ${example.lazyValue}")

    example.observed = "第一次修改"
    example.observed = "第二次修改"

    example.mapped = "映射值"
    println("从 map 中获取: ${example.mapped}")
}

// ==================== 第十部分：实际项目常见模式 ====================
object PracticalPatterns {
    // 1. 单例
    object NetworkManager {
        fun request() = println("网络请求")
    }

    // 2. 伴生对象工厂
    class User private constructor(val name: String) {
        companion object {
            fun create(name: String): User {
                return User(name)
            }
        }
    }

    // 3. DSL 示例
    class Html {
        private val children = mutableListOf<Any>()

        fun body(block: Body.() -> Unit) {
            children.add(Body().apply(block))
        }

        override fun toString() = children.joinToString("\n")
    }

    class Body {
        private val elements = mutableListOf<String>()

        fun p(text: String) {
            elements.add("<p>$text</p>")
        }

        override fun toString() = """
            <body>
            ${elements.joinToString("\n")}
            </body>
        """.trimIndent()
    }

    fun html(block: Html.() -> Unit): Html = Html().apply(block)

    // 4. 类型安全的构建器
    fun buildHtml() = html {
        body {
            p("Hello, Kotlin DSL!")
            p("This is a DSL example")
        }
    }
}

// 运行所有示例