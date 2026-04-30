plugins {
    // 应用 Java Library 插件，用于创建纯 Java 库模块
    id("java-library")
}

java {
    // 配置 Java 版本兼容性
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    // 添加 JUnit 5 测试依赖
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.2")
}

tasks.test {
    // 使用 JUnit 5 测试框架
    useJUnitPlatform()
    // 显示测试结果
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
    }
}