package com.example.apktool

import java.io.File

/**
 * APK DEX 分析工具的主入口类
 *
 * 该工具提供以下命令行功能：
 * 1. find-class: 查找指定类位于哪个 DEX 文件中
 * 2. parse-method: 解析指定类中方法的 Dalvik 指令（通过方法索引）
 * 3. list-classes: 列出 APK 中的所有类
 * 4. list-methods: 列出指定类的所有方法
 *
 * 使用方式：
 * java -jar apktool.jar <apk_file> <command> [options]
 *
 * @author APK Tool Developer
 * @version 1.0.0
 */
object ApkToolMain {

    private const val COMMAND_FIND_CLASS = "find-class"
    private const val COMMAND_PARSE_METHOD = "parse-method"
    private const val COMMAND_LIST_CLASSES = "list-classes"
    private const val COMMAND_LIST_METHODS = "list-methods"

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty() || args.size < 2) {
            printUsage()
            return
        }

        val apkPath = args[0]
        val command = args[1]

        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            println("错误：APK 文件不存在: $apkPath")
            return
        }

        if (!apkFile.name.endsWith(".apk", ignoreCase = true)) {
            println("警告：文件扩展名不是 .apk")
        }

        val analyzer = ApkAnalyzer(apkPath)

        when (command) {
            COMMAND_FIND_CLASS -> handleFindClass(args, analyzer)
            COMMAND_PARSE_METHOD -> handleParseMethod(args, analyzer)
            COMMAND_LIST_CLASSES -> handleListClasses(analyzer)
            COMMAND_LIST_METHODS -> handleListMethods(args, analyzer)
            else -> {
                println("错误：未知命令 '$command'")
                println()
                printUsage()
            }
        }
    }

    private fun handleFindClass(args: Array<String>, analyzer: ApkAnalyzer) {
        if (args.size < 3) {
            println("错误：find-class 命令需要指定类名")
            println("用法：apktool.jar <apk> find-class <classname>")
            println("示例：apktool.jar app.apk find-class Lcom/example/Test;")
            return
        }

        val className = args[2]
        val locator = DexClassLocator(analyzer)
        val result = locator.findClassInDexes(className)

        if (result != null) {
            println("找到类: $className")
            println("所在 DEX: ${result.dexFileName}")
            println("类类型: ${result.classType}")
            println("访问标志: ${result.accessFlags}")
        } else {
            println("未找到类: $className")
        }
    }

    /**
     * 处理 parse-method 命令
     * 通过方法索引解析指定类中方法的 Dalvik 指令
     *
     * 使用方法索引可以避免 shell 解析特殊字符的问题。
     * 用户可以先使用 list-methods 命令查看方法列表和索引。
     */
    private fun handleParseMethod(args: Array<String>, analyzer: ApkAnalyzer) {
        if (args.size < 4) {
            println("错误：parse-method 命令需要指定类名和方法索引")
            println("用法：apktool.jar <apk> parse-method <classname> <method_index>")
            println("示例：apktool.jar app.apk parse-method Lcom/example/Test; 0")
            println("提示：使用 list-methods 命令查看方法索引")
            return
        }

        val className = args[2]
        val methodIndexStr = args[3]

        val methodIndex = methodIndexStr.toIntOrNull()
        if (methodIndex == null || methodIndex < 0) {
            println("错误：方法索引必须是正整数: $methodIndexStr")
            return
        }

        val methods = analyzer.getMethodsOfClass(className)
        if (methods.isEmpty()) {
            println("未找到类: $className")
            return
        }

        if (methodIndex >= methods.size) {
            println("错误：方法索引超出范围 (0-${methods.size - 1})")
            println("提示：使用 list-methods 命令查看方法索引")
            return
        }

        val method = methods[methodIndex]
        println("方法: ${method.methodName}${method.descriptor}")
        println("所在类: $className")
        println("所在 DEX: ${method.dexFileName}")
        println()

        val parser = DalvikInstructionParser(analyzer)
        val instructions = parser.parseMethodInstructions(className, method.methodName, method.descriptor)

        if (instructions.isEmpty()) {
            println("该方法没有 Dalvik 指令（可能是抽象方法或接口方法）")
        } else {
            println("指令数量: ${instructions.size}")
            println()
            println("Dalvik 指令：")
            for ((index, instruction) in instructions.withIndex()) {
                println("  [$index] $instruction")
            }
        }
    }

    private fun handleListClasses(analyzer: ApkAnalyzer) {
        val allClasses = analyzer.getAllClasses()

        if (allClasses.isEmpty()) {
            println("APK 中未找到任何类")
            return
        }

        println("APK 中共有 ${allClasses.size} 个类")
        println()

        val classesByDex = allClasses.groupBy { it.dexFileName }

        for ((dexName, classes) in classesByDex) {
            println("=== $dexName (${classes.size} 个类) ===")
            for (classInfo in classes) {
                println("  ${classInfo.className}")
            }
            println()
        }
    }

    private fun handleListMethods(args: Array<String>, analyzer: ApkAnalyzer) {
        if (args.size < 3) {
            println("错误：list-methods 命令需要指定类名")
            println("用法：apktool.jar <apk> list-methods <classname>")
            println("示例：apktool.jar app.apk list-methods Lcom/example/MainActivity;")
            return
        }

        val className = args[2]
        val methods = analyzer.getMethodsOfClass(className)

        if (methods.isEmpty()) {
            println("未找到类: $className")
            return
        }

        val firstMethod = methods.first()
        println("类: $className")
        println("所在 DEX: ${firstMethod.dexFileName}")
        println("方法数量: ${methods.size}")
        println()
        println("方法列表：")
        for ((index, method) in methods.withIndex()) {
            println("  [$index] ${method.methodName}${method.descriptor}")
        }
    }

    private fun printUsage() {
        println("===========================================")
        println("       APK DEX 分析工具 v1.0.0")
        println("===========================================")
        println()
        println("使用方式：")
        println("  java -jar apktool.jar <apk_file> <command> [options]")
        println()
        println("支持的命令：")
        println()
        println("  1. find-class")
        println("     查找指定类位于哪个 DEX 文件中")
        println("     用法：apktool.jar <apk> find-class <classname>")
        println("     示例：apktool.jar app.apk find-class Lcom/example/Test;")
        println()
        println("  2. list-methods")
        println("     列出指定类的所有方法（显示方法索引）")
        println("     用法：apktool.jar <apk> list-methods <classname>")
        println("     示例：apktool.jar app.apk list-methods Lcom/example/MainActivity;")
        println()
        println("  3. parse-method")
        println("     解析指定类中方法的 Dalvik 指令（通过方法索引）")
        println("     用法：apktool.jar <apk> parse-method <classname> <method_index>")
        println("     示例：apktool.jar app.apk parse-method Lcom/example/Test; 0")
        println("     提示：先使用 list-methods 查看方法索引")
        println()
        println("  4. list-classes")
        println("     列出 APK 中的所有类")
        println("     用法：apktool.jar <apk> list-classes")
        println()
        println("注意事项：")
        println("  - 类名格式：Dalvik 格式，如 Lcom/example/Test;")
        println("  - 类名中的分号 ; 需要用引号包裹或转义")
        println("  - parse-method 使用方法索引，避免 shell 解析特殊字符的问题")
        println()
    }
}