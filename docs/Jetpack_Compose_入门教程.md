
# Jetpack Compose 入门与实践

## 目录

1. [Jetpack Compose 简介](#jetpack-compose-简介)
2. [项目配置](#项目配置)
3. [核心概念](#核心概念)
4. [代码实践](#代码实践)
5. [State 管理](#state-管理)
6. [List 列表实现](#list-列表实现)
7. [ViewModel 集成](#viewmodel-集成)
8. [最佳实践](#最佳实践)

---

## Jetpack Compose 简介

Jetpack Compose 是 Google 推出的现代 Android UI 框架，采用声明式编程范式。与传统的 XML 布局和视图系统相比，Compose 提供了更简洁、更直观、更高效的 UI 开发方式。

### 主要优势

- **声明式 UI**：描述应该是什么样，而不是如何构建
- **更少的样板代码**：无需 findViewById、适配器等
- **更好的组合性**：组件可以灵活组合和复用
- **出色的 IDE 支持**：实时预览、交互式编辑
- **与现有代码集成**：可以逐步采用，与传统视图系统共存

---

## 项目配置

### 1. 依赖配置

在 `app/build.gradle.kts` 中进行以下配置：

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    // ...
    
    buildFeatures {
        compose = true
        // 启用 Compose 功能
    }
}

dependencies {
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
    
    implementation("androidx.activity:activity-compose:1.9.0")
    // 依赖 Activity Compose 库，用于在 Activity 中使用 Compose
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    // 仅在调试模式下依赖 Compose UI 工具库
    
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // 仅在调试模式下依赖 Compose UI 测试清单
    
    // ViewModel 相关
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
}
```

### 2. 最低 SDK 要求

```kotlin
minSdk = 24
```

Compose 要求最低 Android 7.0 (API 24)。

---

## 核心概念

### Composable 函数

Composable 函数是 Compose 的基本构建块，用 `@Composable` 注解标记。

```kotlin
@Composable
fun Greeting(name: String) {
    Text(text = "Hello, $name!")
}
```

### 状态 (State)

在 Compose 中，UI 是状态的函数。当状态变化时，UI 会自动更新。

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableIntStateOf(0) }
    
    Column {
        Text("Count: $count")
        Button(onClick = { count++ }) {
            Text("Increment")
        }
    }
}
```

### 布局

Compose 提供了几个核心布局函数：

- `Column`：垂直排列组件
- `Row`：水平排列组件
- `Box`：堆叠组件

### Modifier

Modifier 用于修改组件的外观和行为。

```kotlin
Text(
    text = "Hello",
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .background(Color.Blue)
)
```

---

## 代码实践

### 创建 Compose Activity

```kotlin
class ComposeActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ComposeExample()
                }
            }
        }
    }
}
```

### 数据模型

```kotlin
data class TodoItem(
    val id: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)
```

### 完整示例结构

```kotlin
@Composable
fun ComposeExample() {
    val viewModel: ComposeViewModel = viewModel()
    val count by viewModel.count.collectAsState()
    val todoList by viewModel.todoList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 计数器部分
        Text(
            text = "Count: $count",
            style = MaterialTheme.typography.displayLarge
        )
        
        Button(onClick = { viewModel.incrementCount() }) {
            Text("Increment")
        }
        
        // 待办列表部分
        LazyColumn {
            items(todoList, key = { it.id }) { item -&gt;
                TodoItemCard(item = item)
            }
        }
    }
}
```

---

## State 管理

### 1. 本地 State

```kotlin
@Composable
fun LocalStateExample() {
    var count by remember { mutableIntStateOf(0) }
    
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
```

### 2. 使用 ViewModel 和 StateFlow

```kotlin
class ComposeViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow&lt;Int&gt; = _count.asStateFlow()
    
    fun incrementCount() {
        _count.value++
    }
    
    fun resetCount() {
        _count.value = 0
    }
}

// 在 Composable 中使用
@Composable
fun ComposeExample() {
    val viewModel: ComposeViewModel = viewModel()
    val count by viewModel.count.collectAsState()
    
    Text("Count: $count")
}
```

---

## List 列表实现

### 1. LazyColumn - 垂直列表

```kotlin
LazyColumn(
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(todoList, key = { it.id }) { item -&gt;
        TodoItemCard(item = item)
    }
}
```

### 2. itemsIndexed - 带索引的列表

```kotlin
LazyColumn {
    itemsIndexed(listOf("苹果", "香蕉", "橙子")) { index, fruit -&gt;
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${index + 1}.")
            Text(fruit)
        }
    }
}
```

### 3. 固定数量的列表

```kotlin
LazyColumn {
    items(10) { index -&gt;
        Text("项目 ${index + 1}")
    }
}
```

---

## ViewModel 集成

### 完整的 ViewModel 实现

```kotlin
class ComposeViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow&lt;Int&gt; = _count.asStateFlow()
    
    private val _todoList = MutableStateFlow(
        mutableListOf(
            TodoItem(1, "学习 Kotlin", "掌握 Kotlin 基础语法"),
            TodoItem(2, "学习 Compose", "深入理解 Jetpack Compose"),
            TodoItem(3, "完成项目", "构建一个完整的应用")
        )
    )
    val todoList: StateFlow&lt;MutableList&lt;TodoItem&gt;&gt; = _todoList.asStateFlow()
    
    private var nextId = 4
    
    fun incrementCount() {
        _count.value++
    }
    
    fun resetCount() {
        _count.value = 0
    }
    
    fun addTodoItem() {
        val newItem = TodoItem(nextId++, "新任务 ${nextId}", "这是新添加的任务")
        val updatedList = _todoList.value.toMutableList()
        updatedList.add(newItem)
        _todoList.value = updatedList
    }
    
    fun removeLastTodoItem() {
        if (_todoList.value.isNotEmpty()) {
            val updatedList = _todoList.value.toMutableList()
            updatedList.removeAt(updatedList.lastIndex)
            _todoList.value = updatedList
        }
    }
    
    fun toggleTodoItem(id: Int) {
        val updatedList = _todoList.value.toMutableList()
        val index = updatedList.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = updatedList[index]
            updatedList[index] = item.copy(isCompleted = !item.isCompleted)
            _todoList.value = updatedList
        }
    }
    
    fun deleteTodoItem(id: Int) {
        val updatedList = _todoList.value.toMutableList()
        updatedList.removeIf { it.id == id }
        _todoList.value = updatedList
    }
}
```

### Todo 卡片组件

```kotlin
@Composable
fun TodoItemCard(
    item: TodoItem,
    onToggleComplete: () -&gt; Unit,
    onDelete: () -&gt; Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted)
                Color.Green.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.isCompleted) Color.Gray else Color.Unspecified
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onToggleComplete) {
                    Text(
                        text = if (item.isCompleted) "撤销" else "完成",
                        color = if (item.isCompleted) Color(0xFFFF9800) else Color.Green
                    )
                }
                
                TextButton(onClick = onDelete) {
                    Text(text = "删除", color = Color.Red)
                }
            }
        }
    }
}
```

---

## 最佳实践

### 1. 合理使用 remember

使用 `remember` 来保存跨重组的状态：

```kotlin
var count by remember { mutableIntStateOf(0) }
```

### 2. 使用 key 参数

在列表中使用 `key` 提高性能和稳定性：

```kotlin
items(todoList, key = { it.id }) { item -&gt;
    TodoItemCard(item = item)
}
```

### 3. 状态提升

将状态提升到合理的层级，避免过度向下传递：

```kotlin
@Composable
fun Parent() {
    var state by remember { mutableStateOf(0) }
    Child(state = state, onStateChange = { state = it })
}

@Composable
fun Child(state: Int, onStateChange: (Int) -&gt; Unit) {
    Button(onClick = { onStateChange(state + 1) }) {
        Text("State: $state")
    }
}
```

### 4. 预览

使用 `@Preview` 注解来预览 UI：

```kotlin
@Preview(showBackground = true)
@Composable
fun ComposeExamplePreview() {
    MaterialTheme {
        ComposeExample()
    }
}
```

### 5. Modifier 顺序

Modifier 的顺序很重要，通常按以下顺序：

1. 尺寸和位置 (size, padding, offset)
2. 外观 (background, border)
3. 交互 (clickable)

```kotlin
Text(
    text = "Hello",
    modifier = Modifier
        .fillMaxWidth()           // 1. 尺寸
        .padding(16.dp)           // 2. 位置
        .background(Color.Blue)   // 3. 外观
        .clickable { }            // 4. 交互
)
```

---

## 总结

Jetpack Compose 代表了 Android UI 开发的未来。通过这篇教程，你学习了：

1. 如何在项目中配置 Compose
2. Compose 的核心概念：Composable、State、布局、Modifier
3. 如何实现计数器、待办列表等常见功能
4. 如何使用 ViewModel 和 StateFlow 进行状态管理
5. Compose 的最佳实践

希望这篇教程能帮助你快速上手 Jetpack Compose，享受现代 UI 开发的乐趣！

---

*本文档基于 Stability 项目中的 ComposeActivity.kt 整理，可作为实际开发参考。*
