# Bytecode Logging Plugin

基于 ASM 字节码框架的 Gradle 插件，能够在 Android 项目编译过程中，通过修改字节码自动为指定的函数、类、包添加调用日志和堆栈信息，支持有源码和无源码的第三方 SDK。

## 功能特性

- ✅ 支持配置需要插桩的包名、类名、方法名（支持通配符）
- ✅ 在函数入口处打印调用日志（类名、方法名、参数）
- ✅ 在函数出口处打印返回值（返回值）
- ✅ 支持打印调用堆栈
- ✅ 支持无源码的第三方 SDK 的插桩
- ✅ 使用 AGP 8.x 最新的 AsmClassVisitorFactory API
- ✅ 易于集成到现有 Android 项目

## 快速开始

### 1. 项目结构要求

本插件是项目内的插件模块，需要在 `settings.gradle.kts` 中配置：

```kotlin
pluginManagement {
    includeBuild("build-logging-plugin")
    // ...
}
```

### 2. 在 app 模块中应用插件

在 `app/build.gradle.kts` 中：

```kotlin
plugins {
    id("com.android.application")
    id("com.example.buildloggingplugin")
}
```

### 3. 配置插件

在 `app/build.gradle.kts` 中添加配置：

```kotlin
bytecodeLogging {
    // 配置需要插桩的包名（支持通配符）
    packages.set(listOf("com.example.stability.**", "com.example.other.*"))
    
    // 或者配置需要插桩的类名
    // classes.set(listOf("com.example.stability.MainActivity", "com.example.stability.MyService"))
    
    // 或者配置需要插桩的方法名
    // methods.set(listOf("onCreate", "doSomething"))
    
    // 是否打印调用堆栈
    printStackTrace.set(true)
    
    // 是否打印返回值
    printReturnValue.set(true)
    
    // 日志标签
    logTag.set("MyAppLogger")
}
```

## 配置说明

| 配置项 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| `packages` | `List<String>` | 空列表 | 需要插桩的包名，支持通配符匹配，如 `com.example.**` |
| `classes` | `List<String>` | 空列表 | 需要插桩的完整类名 |
| `methods` | `List<String>` | 空列表 | 需要插桩的方法名 |
| `printStackTrace` | `Boolean` | `false` | 是否打印调用堆栈 |
| `printReturnValue` | `Boolean` | `true` | 是否打印返回值 |
| `logTag` | `String` | `"BytecodeLogging"` | 日志标签 |

## 通配符匹配规则

- `*` 匹配任意数量的字符（不包含路径分隔符）
- `**` 匹配任意数量的字符（包含路径分隔符）

示例：
- `com.example.*` 匹配 `com.example.MainActivity` 但不匹配 `com.example.sub.OtherActivity`
- `com.example.**` 匹配 `com.example` 包及其所有子包下的类

## 使用示例

### 示例 1: 插桩指定包下的所有类

```kotlin
bytecodeLogging {
    packages.set(listOf("com.example.stability.**"))
    printStackTrace.set(false)
    printReturnValue.set(true)
    logTag.set("StabilityLogger")
}
```

### 示例 2: 插桩特定类

```kotlin
bytecodeLogging {
    classes.set(listOf(
        "com.example.stability.MainActivity",
        "com.example.stability.webview.WebViewModel"
    ))
    printStackTrace.set(true)
}
```

### 示例 3: 插桩第三方 SDK

```kotlin
bytecodeLogging {
    packages.set(listOf("com.some.sdk.**"))
    logTag.set("SDKLogger")
}
```

## 日志输出格式

### 方法入口日志

```
D/BytecodeLogging: [Entry] com.example.stability.MainActivity.onCreate(Bundle savedInstanceState)
D/BytecodeLogging:   Parameters: [android.os.Bundle@a1b2c3d]
```

### 方法返回日志

```
D/BytecodeLogging: [Exit]  com.example.stability.MainActivity.calculateSum(int a, int b)
D/BytecodeLogging:   Return value: 42
```

### 堆栈日志（如果启用）

```
D/BytecodeLogging: [Stacktrace] com.example.stability.MainActivity.onCreate(MainActivity.kt:20)
D/BytecodeLogging:   at com.example.stability.MainActivity.onCreate(MainActivity.kt:20)
D/BytecodeLogging:   at android.app.Activity.performCreate(Activity.java:8567)
...
```

## 工作原理

本插件使用 Android Gradle Plugin (AGP) 8.x 提供的 `AsmClassVisitorFactory` API，在编译过程中：

1. 遍历所有需要处理的类文件
2. 使用 ASM 9.7 读取类文件
3. 根据配置判断是否需要对类进行插桩
4. 对符合条件的方法，在方法入口和出口插入日志打印代码
5. 将修改后的字节码写回

## 注意事项

1. 插件默认跳过构造函数、静态初始化块、抽象方法、本地方法和合成方法
2. 插件不会引入额外的运行时依赖
3. 建议在 Debug 构建中使用，Release 构建可以禁用以减少包大小
4. 对于大规模插桩，可能会增加编译时间

## 项目结构

```
build-logging-plugin/
├── src/main/kotlin/com/example/buildloggingplugin/
│   ├── BytecodeLoggingPlugin.kt       # 插件主入口
│   ├── BytecodeLoggingExtension.kt    # 配置扩展
│   ├── BytecodeUtils.kt               # 字节码工具类
│   ├── LoggingClassVisitorFactory.kt  # ASM 访问器工厂
│   ├── LoggingMethodVisitor.kt        # 方法访问器（插入日志）
│   ├── LoggingParameters.kt           # 插桩参数
│   └── WildcardMatcher.kt             # 通配符匹配
└── build.gradle.kts
```

## 许可证

本项目仅供学习和研究使用。
