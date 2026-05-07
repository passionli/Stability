package com.example.apktool

import org.jf.dexlib2.iface.ClassDef

/**
 * 类所在 DEX 定位器
 *
 * 负责在 APK 的多个 DEX 文件中查找指定类，并返回该类所在的 DEX 文件信息。
 * Android 应用通常会包含多个 DEX 文件（classes.dex, classes2.dex 等），
 * 该类帮助确定目标类位于哪个 DEX 文件中。
 *
 * @param analyzer APK 分析器实例
 * @author APK Tool Developer
 * @version 1.0.0
 */
class DexClassLocator(private val analyzer: ApkAnalyzer) {

    /**
     * 查找指定类所在的 DEX 文件信息
     *
     * 该方法遍历 APK 中的所有 DEX 文件，查找与给定类名匹配的类。
     * 类名支持 Dalvik 格式（以 L 开头，分号结尾），如 Lcom/example/Test;
     *
     * @param className 类的全限定名（Dalvik 格式）
     * @return ClassLocation? 如果找到则返回包含 DEX 文件名和类信息的结果，否则返回 null
     */
    fun findClassInDexes(className: String): ClassLocation? {
        val normalizedClassName = normalizeClassName(className)

        for ((dexName, dexFile) in analyzer.getDexFiles()) {
            val classDef = findClassByName(dexFile, normalizedClassName)
            if (classDef != null) {
                return ClassLocation(
                    dexFileName = dexName,
                    classDef = classDef,
                    className = normalizedClassName
                )
            }
        }

        return null
    }

    /**
     * 根据类名在 DEX 文件中查找类
     */
    private fun findClassByName(dexFile: org.jf.dexlib2.iface.DexFile, className: String): ClassDef? {
        for (classDef in dexFile.classes) {
            if (classDef.type == className) {
                return classDef
            }
        }
        return null
    }

    /**
     * 标准化类名格式
     */
    private fun normalizeClassName(className: String): String {
        var result = className

        if (!result.startsWith("L")) {
            result = "L$result"
        }

        if (!result.endsWith(";")) {
            result = "$result;"
        }

        if (result.startsWith("L") && !result.contains("/")) {
            result = result.substring(1, result.length - 1).replace(".", "/")
            result = "L$result;"
        }

        return result
    }

    /**
     * 获取所有包含指定包名的类
     */
    fun findClassesInPackage(packageName: String): List<ClassLocation> {
        val results = mutableListOf<ClassLocation>()
        val normalizedPackage = if (packageName.endsWith("/")) packageName else "$packageName/"

        for ((dexName, dexFile) in analyzer.getDexFiles()) {
            for (classDef in dexFile.classes) {
                if (classDef.type.startsWith(normalizedPackage)) {
                    results.add(
                        ClassLocation(
                            dexFileName = dexName,
                            classDef = classDef,
                            className = classDef.type
                        )
                    )
                }
            }
        }

        return results
    }
}

/**
 * 类位置信息数据类
 */
data class ClassLocation(
    val dexFileName: String,
    val classDef: ClassDef,
    val className: String
) {
    val accessFlags: Int
        get() = classDef.accessFlags

    val classType: String
        get() = classDef.type

    val superclass: String?
        get() = classDef.superclass

    val interfaces: List<String>
        get() = classDef.interfaces.toList()
}