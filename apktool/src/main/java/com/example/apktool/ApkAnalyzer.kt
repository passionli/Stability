package com.example.apktool

import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.DexFile
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * APK 文件分析器
 */
class ApkAnalyzer(val apkPath: String) {

    private var dexFiles: MutableMap<String, DexFile>? = null

    fun getDexFiles(): Map<String, DexFile> {
        if (dexFiles == null) {
            dexFiles = loadDexFiles()
        }
        return dexFiles!!
    }

    private fun loadDexFiles(): MutableMap<String, DexFile> {
        val result = mutableMapOf<String, DexFile>()
        val apkFile = File(apkPath)
        val opcodes = Opcodes.getDefault()

        ZipInputStream(FileInputStream(apkFile)).use { zipStream ->
            var entry: ZipEntry? = zipStream.nextEntry
            while (entry != null) {
                if (entry.name.endsWith(".dex", ignoreCase = true)) {
                    val tempFile = File.createTempFile("dex", ".dex")
                    tempFile.deleteOnExit()
                    tempFile.outputStream().use { output ->
                        zipStream.copyTo(output)
                    }
                    val dexFile = DexFileFactory.loadDexFile(tempFile, opcodes)
                    result[entry.name] = dexFile
                }
                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
        }

        return result
    }

    fun getDexFileNames(): List<String> {
        return getDexFiles().keys.toList()
    }

    fun getAllClasses(): List<ClassInfo> {
        val allClasses = mutableListOf<ClassInfo>()

        for ((dexName, dexFile) in getDexFiles()) {
            for (classDef in dexFile.classes) {
                allClasses.add(
                    ClassInfo(
                        className = classDef.type,
                        dexFileName = dexName,
                        classDef = classDef
                    )
                )
            }
        }

        return allClasses
    }

    fun findClassInDex(dexName: String, className: String): ClassDef? {
        val dexFile = getDexFiles()[dexName] ?: return null
        val normalizedClassName = normalizeClassName(className)
        return findClassByName(dexFile, normalizedClassName)
    }

    private fun findClassByName(dexFile: DexFile, className: String): ClassDef? {
        for (classDef in dexFile.classes) {
            if (classDef.type == className) {
                return classDef
            }
        }
        return null
    }

    fun findClass(className: String): Pair<String, ClassDef>? {
        val normalizedClassName = normalizeClassName(className)

        for ((dexName, dexFile) in getDexFiles()) {
            val classDef = findClassByName(dexFile, normalizedClassName)
            if (classDef != null) {
                return Pair(dexName, classDef)
            }
        }
        return null
    }

    fun getMethodsOfClass(className: String): List<MethodInfo> {
        val result = mutableListOf<MethodInfo>()
        val normalizedClassName = normalizeClassName(className)
        val classResult = findClass(normalizedClassName)

        if (classResult != null) {
            val (dexName, classDef) = classResult
            for (method in classDef.methods) {
                val descriptor = getMethodDescriptor(method)
                result.add(
                    MethodInfo(
                        methodName = method.name,
                        descriptor = descriptor,
                        accessFlags = method.accessFlags,
                        className = normalizedClassName,
                        dexFileName = dexName
                    )
                )
            }
        }

        return result
    }

    private fun normalizeClassName(className: String): String {
        var result = className.trim()

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
     * 获取方法描述符（方法签名）
     *
     * 使用多种方式尝试获取方法描述符，优先使用公共 API，降级到反射。
     */
    private fun getMethodDescriptor(method: org.jf.dexlib2.iface.Method): String {
        return try {
            // 尝试1: 使用 method.toString() 获取完整签名
            val methodStr = method.toString()
            // 解析方法签名部分，格式通常为: methodName(参数)返回类型
            if (methodStr.contains("(")) {
                val idx = methodStr.indexOf("(")
                methodStr.substring(idx)
            } else {
                // 尝试其他方式
                getDescriptorByReflection(method)
            }
        } catch (e: Exception) {
            getDescriptorByReflection(method)
        }
    }

    /**
     * 通过反射获取方法描述符
     */
    private fun getDescriptorByReflection(method: org.jf.dexlib2.iface.Method): String {
        return try {
            // 尝试获取 proto 字段
            val protoField = method.javaClass.getDeclaredField("proto")
            protoField.isAccessible = true
            val proto = protoField.get(method)
            if (proto != null) {
                proto.toString()
            } else {
                ""
            }
        } catch (e: Exception) {
            try {
                // 尝试获取 descriptor 字段
                val descField = method.javaClass.getDeclaredField("descriptor")
                descField.isAccessible = true
                val desc = descField.get(method) as? String ?: ""
                desc
            } catch (ex: Exception) {
                ""
            }
        }
    }

    fun reload() {
        dexFiles = null
    }
}

data class ClassInfo(
    val className: String,
    val dexFileName: String,
    val classDef: ClassDef
)

data class MethodInfo(
    val methodName: String,
    val descriptor: String,
    val accessFlags: Int,
    val className: String,
    val dexFileName: String
)