package com.example.buildloggingplugin

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class BytecodeLoggingExtension {
    abstract val packages: ListProperty<String>
    abstract val classes: ListProperty<String>
    abstract val methods: ListProperty<String>
    abstract val printStackTrace: Property<Boolean>
    abstract val printReturnValue: Property<Boolean>
    abstract val logTag: Property<String>

    init {
        packages.convention(emptyList())
        classes.convention(emptyList())
        methods.convention(emptyList())
        printStackTrace.convention(false)
        printReturnValue.convention(true)
        logTag.convention("BytecodeLogging")
    }
}
