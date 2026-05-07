# APK DEX 分析工具

一个使用 dexlib2 库解析 APK 文件的命令行工具，支持查询类所在 DEX 文件、解析 Dalvik 指令等功能。

## 功能特性

- **find-class**: 查询指定类位于哪个 DEX 文件中
- **parse-method**: 解析指定类中方法的 Dalvik 指令
- **list-classes**: 列出 APK 中的所有类

## 环境要求

- JDK 8 或更高版本
- Android SDK (用于获取测试 APK)

## 依赖说明

本工具主要依赖以下库：

| 依赖 | 版本 | 说明 |
|------|------|------|
| dexlib2 | 2.5.2 | smali 项目核心库，用于解析 DEX 文件 |
| Kotlin | 2.0.0 | 编程语言 |

## 安装步骤

1. 确保已安装 JDK 8+
2. 克隆项目或复制 apktool 模块到本地
3. 在项目根目录执行编译：

```bash
./gradlew :apktool:build
```

4. 生成的 JAR 文件位于：`apktool/build/libs/apktool.jar`

## 使用方法

### 基本命令格式

```bash
java -jar apktool.jar <apk_file> <command> [options]
```

### 1. find-class - 查找类所在的 DEX 文件

查找指定类位于哪个 DEX 文件中。

```bash
java -jar apktool.jar app.apk find-class Lcom/example/Test;
```

**参数说明：**
- `classname`: 类的全限定名，使用 Dalvik 格式（以 L 开头，分号结尾）

**输出示例：**
```
找到类: Lcom/example/Test;
所在 DEX: classes.dex
类类型: Lcom/example/Test;
访问标志: 1
```

### 2. parse-method - 解析方法的 Dalvik 指令

解析指定类中方法的 Dalvik 字节码指令。

```bash
java -jar apktool.jar app.apk parse-method Lcom/example/Test; onCreate (V)V
```

**参数说明：**
- `classname`: 类的全限定名（Dalvik 格式）
- `method`: 方法名
- `descriptor`: 方法描述符

**方法描述符示例：**
- `(V)` - 返回 void，无参数
- `(I)V` - 参数为 int，返回 void
- `(Ljava/lang/String;)V` - 参数为 String，返回 void
- `(J)J` - 参数为 long，返回 long

**输出示例：**
```
方法: onCreate (V)V
所在类: Lcom/example/Test;
指令数量: 5

Dalvik 指令：
  [0] invoke-super {v0}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V
  [1] const-string v1, "Hello"
  [2] invoke-virtual {v0, v1}, Lcom/example/Test;->setText(Ljava/lang/String;)V
  [3] move-result v1
  [4] return-void
```

### 3. list-classes - 列出所有类

列出 APK 中的所有类，按 DEX 文件分组。

```bash
java -jar apktool.jar app.apk list-classes
```

**输出示例：**
```
APK 中共有 150 个类

=== classes.dex (120 个类) ===
  Lcom/example/MainActivity;
  Lcom/example/Utils;
  ...

=== classes2.dex (30 个类) ===
  Lcom/example/SecondaryClass;
  ...
```

## Dalvik 指令格式说明

Dalvik 指令格式遵循 Android 虚拟机规范，主要包括：

| 指令类型 | 示例 | 说明 |
|----------|------|------|
| 方法调用 | invoke-virtual | 调用虚方法 |
| 方法调用 | invoke-super | 调用父类方法 |
| 方法调用 | invoke-direct | 调用直接方法 |
| 方法调用 | invoke-static | 调用静态方法 |
| 实例操作 | new-instance | 创建新实例 |
| 返回指令 | return-void | 无返回值返回 |
| 返回指令 | return | 有返回值返回 |
| 数据移动 | move-result | 移动方法结果 |

## 常见问题

### Q: 类名格式是什么？
A: Dalvik 格式，以 L 开头，分号结尾。例如：`Lcom/example/Test;`

### Q: 如何获取方法的描述符？
A: 方法描述符格式为 `(参数类型)返回类型`。常用类型：
- V = void
- I = int
- J = long
- Z = boolean
- Ljava/lang/String; = String

### Q: 编译失败怎么办？
A: 确保 JDK 版本为 8+，并检查网络连接（需要下载 dexlib2 依赖）

## 许可证

本项目仅供学习和研究使用。

## 作者

APK Tool Developer

## 版本历史

- **1.0.0** - 初始版本，支持基本 DEX 解析功能
