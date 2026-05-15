package com.example.buildloggingplugin

import org.objectweb.asm.Type
import org.objectweb.asm.Opcodes

object BytecodeUtils {
    const val ANDROID_LOG_CLASS = "android/util/Log"
    const val ANDROID_LOG_METHOD_NAME = "d"

    fun getLoadOpcode(type: Type): Int {
        return when (type.sort) {
            Type.BOOLEAN, Type.BYTE, Type.SHORT, Type.INT -> Opcodes.ILOAD
            Type.LONG -> Opcodes.LLOAD
            Type.FLOAT -> Opcodes.FLOAD
            Type.DOUBLE -> Opcodes.DLOAD
            Type.CHAR -> Opcodes.ILOAD
            Type.ARRAY, Type.OBJECT -> Opcodes.ALOAD
            else -> Opcodes.ALOAD
        }
    }

    fun getStoreOpcode(type: Type): Int {
        return when (type.sort) {
            Type.BOOLEAN, Type.BYTE, Type.SHORT, Type.INT -> Opcodes.ISTORE
            Type.LONG -> Opcodes.LSTORE
            Type.FLOAT -> Opcodes.FSTORE
            Type.DOUBLE -> Opcodes.DSTORE
            Type.CHAR -> Opcodes.ISTORE
            Type.ARRAY, Type.OBJECT -> Opcodes.ASTORE
            else -> Opcodes.ASTORE
        }
    }

    fun getReturnOpcode(type: Type): Int {
        return when (type.sort) {
            Type.VOID -> Opcodes.RETURN
            Type.BOOLEAN, Type.BYTE, Type.SHORT, Type.INT, Type.CHAR -> Opcodes.IRETURN
            Type.LONG -> Opcodes.LRETURN
            Type.FLOAT -> Opcodes.FRETURN
            Type.DOUBLE -> Opcodes.DRETURN
            Type.ARRAY, Type.OBJECT -> Opcodes.ARETURN
            else -> Opcodes.ARETURN
        }
    }

    fun getSize(type: Type): Int {
        return when (type.sort) {
            Type.LONG, Type.DOUBLE -> 2
            else -> 1
        }
    }

    fun valueOfDescriptor(type: Type): String {
        return when (type.sort) {
            Type.BOOLEAN -> "java/lang/Boolean"
            Type.BYTE -> "java/lang/Byte"
            Type.SHORT -> "java/lang/Short"
            Type.INT -> "java/lang/Integer"
            Type.LONG -> "java/lang/Long"
            Type.FLOAT -> "java/lang/Float"
            Type.DOUBLE -> "java/lang/Double"
            Type.CHAR -> "java/lang/Character"
            else -> "java/lang/Object"
        }
    }

    fun valueOfMethodDescriptor(type: Type): String {
        return when (type.sort) {
            Type.BOOLEAN -> "(Z)Ljava/lang/Boolean;"
            Type.BYTE -> "(B)Ljava/lang/Byte;"
            Type.SHORT -> "(S)Ljava/lang/Short;"
            Type.INT -> "(I)Ljava/lang/Integer;"
            Type.LONG -> "(J)Ljava/lang/Long;"
            Type.FLOAT -> "(F)Ljava/lang/Float;"
            Type.DOUBLE -> "(D)Ljava/lang/Double;"
            Type.CHAR -> "(C)Ljava/lang/Character;"
            else -> "(Ljava/lang/Object;)Ljava/lang/Object;"
        }
    }

    fun appendStringBuilder(mv: org.objectweb.asm.MethodVisitor, value: String) {
        mv.visitLdcInsn(value)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
    }

    fun appendType(mv: org.objectweb.asm.MethodVisitor, type: Type, localIndex: Int) {
        when (type.sort) {
            Type.BOOLEAN, Type.BYTE, Type.SHORT, Type.INT, Type.CHAR -> {
                mv.visitVarInsn(Opcodes.ILOAD, localIndex)
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(I)Ljava/lang/StringBuilder;",
                    false
                )
            }
            Type.LONG -> {
                mv.visitVarInsn(Opcodes.LLOAD, localIndex)
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(J)Ljava/lang/StringBuilder;",
                    false
                )
            }
            Type.FLOAT -> {
                mv.visitVarInsn(Opcodes.FLOAD, localIndex)
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(F)Ljava/lang/StringBuilder;",
                    false
                )
            }
            Type.DOUBLE -> {
                mv.visitVarInsn(Opcodes.DLOAD, localIndex)
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(D)Ljava/lang/StringBuilder;",
                    false
                )
            }
            Type.ARRAY, Type.OBJECT -> {
                mv.visitVarInsn(Opcodes.ALOAD, localIndex)
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
                    false
                )
            }
        }
    }

    fun appendObject(mv: org.objectweb.asm.MethodVisitor, type: Type) {
        when (type.sort) {
            Type.BOOLEAN, Type.BYTE, Type.SHORT, Type.INT, Type.CHAR -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(I)Ljava/lang/StringBuilder;",
                    false
                )
            }
            Type.LONG -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(J)Ljava/lang/StringBuilder;",
                    false
                )
            }
            Type.FLOAT -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(F)Ljava/lang/StringBuilder;",
                    false
                )
            }
            Type.DOUBLE -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(D)Ljava/lang/StringBuilder;",
                    false
                )
            }
            Type.ARRAY, Type.OBJECT -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
                    false
                )
            }
        }
    }
}
