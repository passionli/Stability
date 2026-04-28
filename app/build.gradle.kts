plugins {
    // 插件配置
    alias(libs.plugins.android.application)
    // 应用 Android 应用插件
    // 使用别名引用插件，版本在 libs.versions.toml 中定义
    
    alias(libs.plugins.kotlin.compose)
    // 应用 Kotlin Compose 插件
    // 用于支持 Compose UI 开发
}

android {
    // Android 配置
    namespace = "com.example.stability"
    // 设置应用的命名空间
    
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
        applicationId = "com.example.stability"
        // 应用 ID，用于在 Google Play 商店中唯一标识应用
        
        minSdk = 24
        // 最低支持的 Android SDK 版本
        
        targetSdk = 36
        // 目标 Android SDK 版本
        
        versionCode = 1
        // 版本代码，用于应用更新
        
        versionName = "1.0"
        // 版本名称，显示给用户

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // 测试运行器

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }
    }

    signingConfigs {
        // 签名配置
        create("release") {
            // 创建名为 "release" 的签名配置
            // 替换成你自己的 jks 文件路径
            storeFile = file("../keystore/stability.jks")
            // 密钥库文件路径
            
            // 密钥库密码
            storePassword = "123456"
            // 密钥库的密码
            
            // 别名
            keyAlias = "key0"
            // 密钥的别名
            
            // 密钥密码
            keyPassword = "123456"
            // 密钥的密码
            
            // 必须开启，适配所有 Android 版本
            enableV1Signing = true
            // 启用 V1 签名（旧版签名方式）
            
            enableV2Signing = true
            // 启用 V2 签名（新版签名方式）
        }
    }

    buildTypes {
        // 构建类型配置
        release {
            // 发布版本配置
            signingConfig = signingConfigs.getByName("release")
            // 使用名为 "release" 的签名配置

            isMinifyEnabled = false
            // 关闭代码混淆
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 指定混淆规则文件
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
        compose = true
        // 启用 Compose 功能
    }

    packagingOptions {
        // 打包选项
        pickFirst("**/libxdl.so")
        // 如果有多个相同的 libxdl.so 文件，选择第一个
        
        pickFirst("**/libshadowhook.so")
        // 如果有多个相同的 libshadowhook.so 文件，选择第一个
        
        pickFirst("**/libshadowhook_nothing.so")
        // 如果有多个相同的 libshadowhook_nothing.so 文件，选择第一个
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
// 👇 这一行就是依赖 native 模块（核心）
    implementation(project(":nativelib"))
    // 依赖 nativelib 模块
    
    implementation(project(":nativelib2"))
    // 依赖 nativelib2 模块

    implementation(libs.androidx.core.ktx)
    // 依赖 AndroidX Core KTX 库
    
    implementation(libs.androidx.appcompat)
    // 依赖 AndroidX AppCompat 库
    
    implementation(libs.material)
    // 依赖 Material Design 库
    
    implementation(libs.androidx.activity)
    // 依赖 AndroidX Activity 库
    
    implementation(libs.androidx.constraintlayout)
    // 依赖 AndroidX ConstraintLayout 库
    
    // Compose 相关依赖
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    // 使用 Compose BOM（Bill of Materials）来管理 Compose 相关依赖的版本
    
    implementation("androidx.compose.ui:ui")
    // 依赖 Compose UI 核心库
    
    implementation("androidx.compose.ui:ui-graphics")
    // 依赖 Compose UI 图形库
    
    implementation("androidx.compose.ui:ui-tooling-preview")
    // 依赖 Compose UI 预览工具库
    
    implementation("androidx.compose.material3:material3")
    // 依赖 Compose Material3 库
    
    implementation("androidx.compose.material:material-icons-extended")
    // 依赖 Material Icons 库，用于图标
    
    implementation("androidx.activity:activity-compose:1.9.0")
    // 依赖 Activity Compose 库，用于在 Activity 中使用 Compose
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    // 仅在调试模式下依赖 Compose UI 工具库
    
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // 仅在调试模式下依赖 Compose UI 测试清单
    
    // Jetpack ViewModel 和 LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    // 依赖 ViewModel KTX 库
    
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    // 依赖 ViewModel Compose 库，用于在 Compose 中使用 ViewModel
    
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    // 依赖 LiveData KTX 库
    
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    // 依赖 Lifecycle Runtime KTX 库
    
    // Media3 (ExoPlayer) 视频播放依赖
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    // 依赖 Media3 ExoPlayer 库，用于视频播放
    
    implementation("androidx.media3:media3-ui:1.2.1")
    // 依赖 Media3 UI 库，用于视频播放界面
    
    implementation("androidx.media3:media3-common:1.2.1")
    // 依赖 Media3 Common 库
    
    implementation("androidx.media3:media3-effect:1.2.1")
    // 依赖 Media3 Effect 库，用于视频滤镜效果
    
    testImplementation(libs.junit)
    // 测试依赖 JUnit 库
    
    androidTestImplementation(libs.androidx.junit)
    // Android 测试依赖 AndroidX JUnit 库
    
    androidTestImplementation(libs.androidx.espresso.core)
    // Android 测试依赖 Espresso 核心库

    // Coil 图片加载库
    implementation("io.coil-kt:coil-compose:2.5.0")
    // 依赖 Coil Compose 库，用于加载图片
}