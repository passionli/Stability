pluginManagement {
    // 插件管理配置
    repositories {
        // 仓库配置
        // 国内镜像，用于加速依赖下载
        maven {
            // Maven 仓库配置
            setUrl("https://maven.aliyun.com/repository/google")
            // 设置 Google 仓库的国内镜像地址
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/central")
            // 设置 Maven Central 仓库的国内镜像地址
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/public")
            // 设置阿里云公共仓库地址
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/gradle-plugin")
            // 设置 Gradle 插件仓库的国内镜像地址
        }

        google {
            // Google 仓库
            content {
                // 内容过滤，只包含特定组的依赖
                includeGroupByRegex("com\\.android.*")
                // 包含 com.android 开头的组
                includeGroupByRegex("com\\.google.*")
                // 包含 com.google 开头的组
                includeGroupByRegex("androidx.*")
                // 包含 androidx 开头的组
            }
        }
        mavenCentral()
        // Maven Central 仓库
        gradlePluginPortal()
        // Gradle 插件门户
    }
}
plugins {
    // 插件配置
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    // 使用 Foojay 工具链解析器约定插件，版本 1.0.0
    // 该插件用于简化 JDK 工具链的管理
}
dependencyResolutionManagement {
    // 依赖解析管理
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    // 设置仓库模式为 FAIL_ON_PROJECT_REPOS，意味着如果项目中定义了仓库，构建会失败
    // 这样可以确保所有仓库配置都集中在 settings.gradle.kts 文件中
    repositories {
        // 仓库配置
        // 国内镜像，用于加速依赖下载
        maven {
            setUrl("https://maven.aliyun.com/repository/google")
            // 设置 Google 仓库的国内镜像地址
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/central")
            // 设置 Maven Central 仓库的国内镜像地址
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/public")
            // 设置阿里云公共仓库地址
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/gradle-plugin")
            // 设置 Gradle 插件仓库的国内镜像地址
        }
        google()
        // Google 仓库
        mavenCentral()
        // Maven Central 仓库
    }
}

rootProject.name = "Stability"
// 设置根项目名称为 "Stability"
include(":app")
// 包含 app 模块
include(":nativelib")
// 包含 nativelib 模块
include(":nativelib2")
// 包含 nativelib2 模块
include(":online_coding")
// 包含 online_coding 模块
