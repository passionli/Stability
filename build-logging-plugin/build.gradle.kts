plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.1.0"
}

group = "com.example.buildloggingplugin"
version = "1.0.0"

repositories {
    maven {
        setUrl("https://maven.aliyun.com/repository/google")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/central")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/public")
    }
    google()
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
    implementation("org.ow2.asm:asm-tree:9.7")
    implementation(gradleApi())
    compileOnly("com.android.tools.build:gradle-api:8.1.0")
}

gradlePlugin {
    plugins {
        create("bytecodeLoggingPlugin") {
            id = "com.example.buildloggingplugin"
            implementationClass = "com.example.buildloggingplugin.BytecodeLoggingPlugin"
        }
    }
}
