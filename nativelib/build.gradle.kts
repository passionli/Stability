plugins {
    // 插件配置
    alias(libs.plugins.android.library)
    // 应用 Android 库插件
    // 使用别名引用插件，版本在 libs.versions.toml 中定义
}

android {
    // Android 配置
    namespace = "com.example.nativelib"
    // 设置库的命名空间
    
    compileSdk {
        // 编译 SDK 配置
        version = release(36) {
            // 使用 Android 36 版本
            minorApiLevel = 1
            // 设置次要 API 级别为 1
        }
    }

    defaultConfig {
        // 默认配置
        minSdk = 24
        // 最低支持的 Android SDK 版本

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // 测试运行器
        
        consumerProguardFiles("consumer-rules.pro")
        // 指定消费者混淆规则文件
        
        externalNativeBuild {
            // 外部原生构建配置
            cmake {
                // CMake 配置
                cppFlags("")
                // 设置 C++ 编译标志
            }
        }
        
        ndk {
            // NDK 配置
            abiFilters += listOf("arm64-v8a")
            // 指定支持的 ABI 架构，这里只支持 arm64-v8a
        }
    }

    buildTypes {
        // 构建类型配置
        release {
            // 发布版本配置
            isMinifyEnabled = false
            // 禁用代码混淆
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 指定混淆规则文件
        }
    }
    
    externalNativeBuild {
        // 外部原生构建配置
        cmake {
            // CMake 配置
            path("src/main/cpp/CMakeLists.txt")
            // 指定 CMakeLists.txt 文件路径
            
            version = "3.22.1"
            // 指定 CMake 版本
        }
    }
    
    compileOptions {
        // 编译选项
        sourceCompatibility = JavaVersion.VERSION_11
        // 源代码兼容性版本
        
        targetCompatibility = JavaVersion.VERSION_11
        // 目标兼容性版本
    }

    buildFeatures {
        // 构建特性
        prefab = true
        // 启用 Prefab 功能，用于分发原生库
    }
}

dependencies {
    implementation("io.github.hexhacking:xdl:2.3.0")
    // 依赖 xdl 库，版本 2.3.0
    // xdl 是一个用于动态加载和查找共享库符号的库
    
    implementation("com.bytedance.android:shadowhook:2.0.0")
    // 依赖 shadowhook 库，版本 2.0.0
    // shadowhook 是一个用于 hook 系统函数的库
    
    implementation(libs.androidx.core.ktx)
    // 依赖 AndroidX Core KTX 库
    
    implementation(libs.androidx.appcompat)
    // 依赖 AndroidX AppCompat 库
    
    implementation(libs.material)
    // 依赖 Material Design 库
    
    testImplementation(libs.junit)
    // 测试依赖 JUnit 库
    
    androidTestImplementation(libs.androidx.junit)
    // Android 测试依赖 AndroidX JUnit 库
    
    androidTestImplementation(libs.androidx.espresso.core)
    // Android 测试依赖 Espresso 核心库
}