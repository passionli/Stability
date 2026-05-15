package com.example.buildloggingplugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

abstract class LoggingClassVisitorFactory : AsmClassVisitorFactory<LoggingParameters> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val params = parameters.get()
        return object : ClassVisitor(Opcodes.ASM9, nextClassVisitor) {
            private var currentClassName: String? = null

            override fun visit(
                version: Int,
                access: Int,
                name: String,
                signature: String?,
                superName: String?,
                interfaces: Array<String>?
            ) {
                super.visit(version, access, name, signature, superName, interfaces)
                currentClassName = name.replace("/", ".")
            }

            override fun visitMethod(
                access: Int,
                name: String,
                descriptor: String,
                signature: String?,
                exceptions: Array<String>?
            ): MethodVisitor {
                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                val className = currentClassName ?: return mv

                if (name == "<init>" || name == "<clinit>") {
                    return mv
                }

                if ((access and Opcodes.ACC_ABSTRACT) != 0 ||
                    (access and Opcodes.ACC_NATIVE) != 0 ||
                    (access and Opcodes.ACC_SYNTHETIC) != 0
                ) {
                    return mv
                }

                val packages = params.packages.get()
                val classes = params.classes.get()
                val methods = params.methods.get()

                val matchesPackage = packages.any { pkg ->
                    WildcardMatcher.match(pkg, className)
                }

                val matchesClass = classes.any { cls ->
                    WildcardMatcher.match(cls, className)
                }

                val matchesMethod = methods.any { methodPattern ->
                    WildcardMatcher.match(methodPattern, "$className.$name")
                }

                if (matchesPackage || matchesClass || matchesMethod) {
            return LoggingMethodVisitor(
                Opcodes.ASM9,
                access,
                name,
                descriptor,
                mv,
                className,
                params
            )
        }

                return mv
            }
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val params = parameters.get()
        val className = classData.className.replace("/", ".")

        if (className.startsWith("android.") ||
            className.startsWith("androidx.") ||
            className.startsWith("kotlin.") ||
            className.startsWith("kotlinx.") ||
            className.startsWith("java.") ||
            className.startsWith("javax.")
        ) {
            return false
        }

        val packages = params.packages.get()
        val classes = params.classes.get()
        val methods = params.methods.get()

        if (packages.isEmpty() && classes.isEmpty() && methods.isEmpty()) {
            return false
        }

        val matchesPackage = packages.any { pkg ->
            WildcardMatcher.match(pkg, className)
        }

        val matchesClass = classes.any { cls ->
            WildcardMatcher.match(cls, className)
        }

        val matchesMethodPattern = methods.any { methodPattern ->
            val methodClass = methodPattern.substringBeforeLast('.')
            WildcardMatcher.match(methodClass, className)
        }

        return matchesPackage || matchesClass || matchesMethodPattern
    }
}
