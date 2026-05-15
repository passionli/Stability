package com.example.buildloggingplugin

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class BytecodeLoggingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("bytecodeLogging", BytecodeLoggingExtension::class.java)
        println("BytecodeLoggingPlugin applied to ${project.name}")

        val androidComponents = project.extensions.findByType(AndroidComponentsExtension::class.java)
        androidComponents?.onVariants { variant ->
            variant.instrumentation.transformClassesWith(
                LoggingClassVisitorFactory::class.java,
                com.android.build.api.instrumentation.InstrumentationScope.ALL
            ) { params ->
                params.packages.set(extension.packages)
                params.classes.set(extension.classes)
                params.methods.set(extension.methods)
                params.printStackTrace.set(extension.printStackTrace)
                params.printReturnValue.set(extension.printReturnValue)
                params.logTag.set(extension.logTag)
            }
        }
    }
}
