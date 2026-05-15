package com.example.buildloggingplugin

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

abstract class LoggingParameters : InstrumentationParameters {
    @get:Input
    abstract val packages: ListProperty<String>

    @get:Input
    abstract val classes: ListProperty<String>

    @get:Input
    abstract val methods: ListProperty<String>

    @get:Input
    abstract val printStackTrace: Property<Boolean>

    @get:Input
    abstract val printReturnValue: Property<Boolean>

    @get:Input
    abstract val logTag: Property<String>
}