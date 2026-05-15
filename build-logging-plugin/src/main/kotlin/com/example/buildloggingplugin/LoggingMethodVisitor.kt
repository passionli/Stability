package com.example.buildloggingplugin

import org.objectweb.asm.*
import org.objectweb.asm.commons.LocalVariablesSorter

class LoggingMethodVisitor(
    api: Int,
    access: Int,
    name: String,
    descriptor: String,
    mv: MethodVisitor,
    private val className: String,
    private val params: LoggingParameters
) : LocalVariablesSorter(api, access, descriptor, mv) {

    private val argumentTypes = Type.getArgumentTypes(descriptor)
    private val returnType = Type.getReturnType(descriptor)
    private val isStatic = (access and Opcodes.ACC_STATIC) != 0
    private val methodName = name

    private var returnValueLocalIndex: Int = -1

    override fun visitCode() {
        super.visitCode()
        insertMethodEntryLogging()
        if (params.printStackTrace.get()) {
            insertStacktraceLogging()
        }
    }

    private fun insertMethodEntryLogging() {
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/StringBuilder",
            "<init>",
            "()V",
            false
        )

        BytecodeUtils.appendStringBuilder(mv, "[$className] $methodName(")

        var localIndex = if (isStatic) 0 else 1
        for (i in argumentTypes.indices) {
            if (i > 0) {
                BytecodeUtils.appendStringBuilder(mv, ", ")
            }
            val type = argumentTypes[i]
            BytecodeUtils.appendStringBuilder(mv, "arg$i=")
            BytecodeUtils.appendType(mv, type, localIndex)
            localIndex += BytecodeUtils.getSize(type)
        }

        BytecodeUtils.appendStringBuilder(mv, ")")

        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "toString",
            "()Ljava/lang/String;",
            false
        )

        mv.visitLdcInsn(params.logTag.get())
        mv.visitInsn(Opcodes.SWAP)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            BytecodeUtils.ANDROID_LOG_CLASS,
            BytecodeUtils.ANDROID_LOG_METHOD_NAME,
            "(Ljava/lang/String;Ljava/lang/String;)I",
            false
        )
        mv.visitInsn(Opcodes.POP)
    }

    private fun insertStacktraceLogging() {
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/Exception")
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Exception",
            "<init>",
            "()V",
            false
        )
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Exception",
            "printStackTrace",
            "()V",
            false
        )
    }

    override fun visitInsn(opcode: Int) {
        if (opcode in Opcodes.IRETURN..Opcodes.RETURN) {
            if (params.printReturnValue.get() && opcode != Opcodes.RETURN) {
                saveReturnValue(opcode)
                insertReturnValueLogging()
                loadReturnValue(opcode)
            }
        }
        super.visitInsn(opcode)
    }

    private fun saveReturnValue(opcode: Int) {
        if (opcode != Opcodes.RETURN) {
            returnValueLocalIndex = newLocal(returnType)
            val storeOpcode = BytecodeUtils.getStoreOpcode(returnType)
            mv.visitVarInsn(storeOpcode, returnValueLocalIndex)
        }
    }

    private fun loadReturnValue(opcode: Int) {
        if (returnValueLocalIndex != -1) {
            val loadOpcode = BytecodeUtils.getLoadOpcode(returnType)
            mv.visitVarInsn(loadOpcode, returnValueLocalIndex)
        }
    }

    private fun insertReturnValueLogging() {
        if (returnValueLocalIndex != -1) {
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/lang/StringBuilder",
                "<init>",
                "()V",
                false
            )

            BytecodeUtils.appendStringBuilder(mv, "[$className] $methodName return=")

            val loadOpcode = BytecodeUtils.getLoadOpcode(returnType)
            mv.visitVarInsn(loadOpcode, returnValueLocalIndex)
            BytecodeUtils.appendObject(mv, returnType)

            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/StringBuilder",
                "toString",
                "()Ljava/lang/String;",
                false
            )

            mv.visitLdcInsn(params.logTag.get())
            mv.visitInsn(Opcodes.SWAP)
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                BytecodeUtils.ANDROID_LOG_CLASS,
                BytecodeUtils.ANDROID_LOG_METHOD_NAME,
                "(Ljava/lang/String;Ljava/lang/String;)I",
                false
            )
            mv.visitInsn(Opcodes.POP)
        }
    }
}
