# ARouter 框架接入指南

## 一、ARouter 简介

ARouter 是阿里巴巴开源的 Android 路由框架，用于实现组件化开发中的页面路由。它提供了一种通过 URL 路径来访问 Activity、Fragment 或服务的机制，实现了模块间的解耦。

**核心特性：**
- 支持简单路由跳转
- 支持带参数的路由跳转
- 支持服务发现（IProvider）
- 支持拦截器机制
- 支持路由分组
- 支持 Uri Scheme 跳转

## 二、接入步骤

### 2.1 添加依赖配置

在 `gradle/libs.versions.toml` 中添加版本配置：

```toml
[versions]
arouter = "1.5.2"

[libraries]
arouter-api = { group = "com.alibaba", name = "arouter-api", version.ref = "arouter" }
arouter-compiler = { group = "com.alibaba", name = "arouter-compiler", version.ref = "arouter" }
```

### 2.2 配置模块 build.gradle

在 `app/build.gradle.kts` 中：

```kotlin
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["AROUTER_MODULE_NAME"] = project.name
            }
        }
    }
}

dependencies {
    implementation(libs.arouter.api)
    annotationProcessor(libs.arouter.compiler)
}
```

### 2.3 初始化 ARouter

在 Application 类中初始化：

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            ARouter.openDebug()
            ARouter.openLog()
        }
        ARouter.init(this)
    }
}
```

## 三、使用示例

### 3.1 简单路由跳转

**步骤1：在目标 Activity 上添加 @Route 注解**

```kotlin
@Route(path = "/arouter/simple")
class SimpleActivity : AppCompatActivity() {
    // ...
}
```

**步骤2：发起路由跳转**

```kotlin
ARouter.getInstance().build("/arouter/simple").navigation()
```

### 3.2 带参数路由跳转

**步骤1：定义接收参数的 Activity**

```kotlin
@Route(path = "/arouter/withParams")
class WithParamsActivity : AppCompatActivity() {
    
    @Autowired(name = "name")
    @JvmField
    var name: String? = "默认名称"
    
    @Autowired(name = "count")
    @JvmField
    var count: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        // 使用参数...
    }
}
```

**步骤2：传递参数并跳转**

```kotlin
ARouter.getInstance()
    .build("/arouter/withParams")
    .withString("name", "测试用户")
    .withInt("count", 100)
    .withBoolean("isVip", true)
    .navigation()
```

### 3.3 服务发现

**步骤1：定义服务接口**

```kotlin
interface IHelloService : IProvider {
    fun sayHello(name: String)
    fun getGreeting(): String
}
```

**步骤2：实现服务**

```kotlin
@Route(path = "/service/hello")
class HelloServiceImpl : IHelloService {
    
    private lateinit var context: Context
    
    override fun init(context: Context) {
        this.context = context
    }
    
    override fun sayHello(name: String) {
        Toast.makeText(context, "Hello, $name!", Toast.LENGTH_SHORT).show()
    }
    
    override fun getGreeting(): String {
        return "Welcome!"
    }
}
```

**步骤3：获取并使用服务**

```kotlin
val helloService = ARouter.getInstance().navigation(IHelloService::class.java)
helloService?.sayHello("ARouter")
```

### 3.4 Bundle 传递复杂数据

```kotlin
@Parcelize
data class UserInfo(
    val userId: String,
    val userName: String,
    val userAge: Int
) : Parcelable

// 传递
val userInfo = UserInfo("U123456", "张三", 28)
ARouter.getInstance()
    .build("/arouter/bundle")
    .withParcelable("userInfo", userInfo)
    .navigation()

// 接收
val userInfo = intent.getParcelableExtra<UserInfo>("userInfo")
```

### 3.5 Uri Scheme 跳转

```kotlin
val uri = Uri.parse("arouter://example.com/arouter/uriTest?source=main&type=test")
ARouter.getInstance().build(uri).navigation()
```

### 3.6 路由分组

```kotlin
@Route(path = "/arouter/group/test", group = "test")
class GroupTestActivity : AppCompatActivity() {
    // ...
}
```

## 四、路由路径规范

| 类型 | 路径示例 | 说明 |
|------|---------|------|
| Activity | `/module/page` | 模块名/页面名 |
| Service | `/service/serviceName` | 服务类型/服务名 |

**建议规范：**
- 路径使用 `/` 分隔
- 全小写字母
- 使用有意义的名称

## 五、关键类和注解说明

### 5.1 注解

| 注解 | 用途 | 示例 |
|------|------|------|
| `@Route` | 标记路由目标 | `@Route(path = "/arouter/main")` |
| `@Autowired` | 自动注入参数 | `@Autowired(name = "userId")` |

### 5.2 核心 API

| API | 用途 |
|-----|------|
| `ARouter.init()` | 初始化 ARouter |
| `ARouter.openDebug()` | 开启调试模式 |
| `ARouter.openLog()` | 开启日志 |
| `ARouter.getInstance().build(path)` | 构建路由 |
| `ARouter.getInstance().navigation()` | 执行跳转 |
| `ARouter.getInstance().navigation(serviceClass)` | 获取服务 |

## 六、注意事项

1. **@Autowired 注解使用注意：**
   - 字段必须使用 `@JvmField` 注解
   - 在 `onCreate()` 中调用 `ARouter.getInstance().inject(this)`
   - 支持基本类型、String、Parcelable 等

2. **路由路径唯一性：**
   - 确保 path 在整个应用中唯一
   - 建议按模块划分路径

3. **混淆配置：**
   ```proguard
   -keep public class com.alibaba.android.arouter.routes.**{*;}
   -keep public class com.alibaba.android.arouter.facade.**{*;}
   -keep class * implements com.alibaba.android.arouter.facade.template.ISyringe{*;}
   ```

4. **调试模式：**
   - 仅在 debug 版本开启 `openDebug()` 和 `openLog()`
   - 生产环境关闭以提高性能

## 七、项目结构

```
app/src/main/java/com/example/stability/arouter/
├── ARouterMainActivity.kt    # ARouter 主示例页面
├── SimpleActivity.kt         # 简单路由示例
├── WithParamsActivity.kt     # 带参数路由示例
├── BundleActivity.kt         # Bundle 传递示例
├── GroupTestActivity.kt      # 路由分组示例
├── UriTestActivity.kt        # Uri 跳转示例
├── IHelloService.kt          # 服务接口
└── HelloServiceImpl.kt       # 服务实现
```

## 八、总结

ARouter 是一款优秀的 Android 路由框架，通过 URL 解耦模块间的依赖关系，非常适合大型项目的组件化开发。本项目提供的示例涵盖了 ARouter 的主要使用场景，包括：

1. 简单路由跳转
2. 参数传递与自动注入
3. Bundle 复杂数据传递
4. 服务发现机制
5. 路由分组
6. Uri Scheme 跳转

开发者可以根据实际需求选择合适的使用方式。