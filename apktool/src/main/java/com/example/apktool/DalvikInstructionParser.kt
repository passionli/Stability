package com.example.apktool

import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.TypeReference
import org.jf.dexlib2.iface.reference.StringReference

/**
 * Dalvik 指令解析器
 */
class DalvikInstructionParser(private val analyzer: ApkAnalyzer) {

    fun parseMethodInstructions(
        className: String,
        methodName: String,
        methodDescriptor: String
    ): List<String> {
        val classDef = findClass(className) ?: return emptyList()
        val method = findMethod(classDef, methodName) ?: return emptyList()
        return parseInstructions(method)
    }

    private fun findClass(className: String): org.jf.dexlib2.iface.ClassDef? {
        val result = analyzer.findClass(className)
        return result?.second
    }

    private fun findMethod(
        classDef: org.jf.dexlib2.iface.ClassDef,
        methodName: String
    ): org.jf.dexlib2.iface.Method? {
        for (method in classDef.methods) {
            if (method.name == methodName) {
                return method
            }
        }
        return null
    }

    /**
     * 解析方法的 Dalvik 指令
     */
    private fun parseInstructions(method: org.jf.dexlib2.iface.Method): List<String> {
        val instructions = mutableListOf<String>()

        try {
            val implementation = method.implementation
            if (implementation == null) {
                return instructions
            }

            // 尝试使用反射调用 getInstructions() 方法
            val instructionsList = invokeGetInstructions(implementation)
            if (instructionsList != null) {
                for (item in instructionsList) {
                    if (item is Instruction) {
                        instructions.add(formatInstruction(item))
                    }
                }
                return instructions
            }

            // 如果上面的方法失败，尝试其他方式
            val instructionsObj = getInstructionsViaReflection(implementation)
            if (instructionsObj is Iterable<*>) {
                for (item in instructionsObj) {
                    if (item is Instruction) {
                        instructions.add(formatInstruction(item))
                    }
                }
            }
        } catch (e: Exception) {
            return listOf("Error parsing instructions: ${e.message}")
        }

        return instructions
    }

    /**
     * 使用反射调用 getInstructions() 方法
     */
    private fun invokeGetInstructions(implementation: Any): Iterable<*>? {
        try {
            val method = implementation.javaClass.getMethod("getInstructions")
            return method.invoke(implementation) as? Iterable<*>
        } catch (e: NoSuchMethodException) {
            try {
                val methods = implementation.javaClass.methods
                for (m in methods) {
                    if (m.name.contains("instruction", ignoreCase = true)) {
                        val result = m.invoke(implementation)
                        if (result is Iterable<*>) {
                            return result
                        }
                    }
                }
            } catch (ex: Exception) {
                // 忽略
            }
        } catch (e: Exception) {
            // 忽略
        }
        return null
    }

    /**
     * 通过反射获取指令列表
     */
    private fun getInstructionsViaReflection(implementation: Any): Any? {
        val fieldNames = listOf("instructions", "_instructions", "insts", "_insts")
        
        for (fieldName in fieldNames) {
            val field = findField(implementation.javaClass, fieldName)
            if (field != null) {
                try {
                    field.isAccessible = true
                    val result = field.get(implementation)
                    if (result is Iterable<*>) {
                        return result
                    }
                } catch (e: Exception) {
                    // 继续尝试下一个字段
                }
            }
        }

        val codeField = findField(implementation.javaClass, "code")
        if (codeField != null) {
            try {
                codeField.isAccessible = true
                val code = codeField.get(implementation)
                if (code != null) {
                    for (fieldName in fieldNames) {
                        val field = findField(code.javaClass, fieldName)
                        if (field != null) {
                            field.isAccessible = true
                            val result = field.get(code)
                            if (result is Iterable<*>) {
                                return result
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // 忽略
            }
        }

        return null
    }

    /**
     * 在类及其父类中查找字段
     */
    private fun findField(clazz: Class<*>, fieldName: String): java.lang.reflect.Field? {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName)
            } catch (e: NoSuchFieldException) {
                currentClass = currentClass.superclass
            }
        }
        return null
    }

    /**
     * 格式化指令为可读字符串
     */
    private fun formatInstruction(instruction: Instruction): String {
        try {
            // 获取操作码名称
            val opcode = instruction.opcode
            val opcodeName = opcode.name

            // 构建指令字符串
            val builder = StringBuilder()
            builder.append(opcodeName)

            // 如果是引用指令，添加引用信息
            if (instruction is ReferenceInstruction) {
                val reference = instruction.reference
                builder.append(" ")
                builder.append(formatReference(reference))
            }

            return builder.toString()
        } catch (e: Exception) {
            // 如果以上方法都失败，尝试使用 toString
            return try {
                val str = instruction.toString()
                // 移除类名部分，只保留指令部分
                if (str.contains("@")) {
                    opcodeToString(instruction)
                } else {
                    str
                }
            } catch (ex: Exception) {
                "unknown"
            }
        }
    }

    /**
     * 格式化引用为可读字符串
     */
    private fun formatReference(reference: Any?): String {
        return when (reference) {
            is MethodReference -> {
                val className = reference.definingClass.replace("/", ".")
                val methodName = reference.name
                reference.toString()
            }
            is FieldReference -> {
                val className = reference.definingClass.replace("/", ".")
                val fieldName = reference.name
                val type = reference.type.replace("/", ".")
                "$className.$fieldName:$type"
            }
            is TypeReference -> {
                reference.type.replace("/", ".")
            }
            is StringReference -> {
                "\"${reference.string}\""
            }
            else -> {
                reference.toString()
            }
        }
    }

    /**
     * 通过反射获取操作码字符串
     */
    private fun opcodeToString(instruction: Instruction): String {
        try {
            val opcodeField = instruction.javaClass.getDeclaredField("opcode")
            opcodeField.isAccessible = true
            val opcode = opcodeField.get(instruction)
            
            val nameField = opcode.javaClass.getDeclaredField("name")
            nameField.isAccessible = true
            return nameField.get(opcode) as String
        } catch (e: Exception) {
            return "unknown"
        }
    }
}