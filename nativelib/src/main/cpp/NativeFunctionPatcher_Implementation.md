# NativeFunctionPatcher 功能文档

## 概述

NativeFunctionPatcher 是一个用于修改 Native 函数行为的工具类，它可以将指定 SO 库中的 C/C++ 函数符号替换为直接返回指定值的指令，从而跳过函数的原始实现。

## 功能特性

- 支持通过符号名查找函数地址
- 支持将函数修改为直接返回指定值（64位整数/指针）
- 支持恢复函数的原始实现
- 线程安全的操作
- 自动处理内存保护属性

## 支持的架构

- **ARM64 (arm64-v8a)** - 仅支持此架构

## API 接口

### Kotlin 层 API

```kotlin
// 根据 SO 库名和符号名查找函数地址
external fun findSymbol(soName: String, symbolName: String): Long

// 根据 SO 库名和符号名 patch 函数
external fun patchFunctionByName(soName: String, symbolName: String, returnValue: Long): Int

// 根据函数地址 patch 函数
external fun patchFunctionByAddr(funcAddr: Long, returnValue: Long): Int

// 根据 SO 库名和符号名恢复函数
external fun restoreFunctionByName(soName: String, symbolName: String): Int

// 根据函数地址恢复函数
external fun restoreFunctionByAddr(funcAddr: Long): Int

// 检查函数是否被 patch
external fun isFunctionPatched(funcAddr: Long): Boolean

// 获取已 patch 的函数数量
external fun getPatchedCount(): Int

// 清除所有 patch
external fun clearAllPatches()
```

### 错误码定义

| 错误码 | 常量名 | 含义 |
|--------|--------|------|
| 0 | PATCH_SUCCESS | 操作成功 |
| -1 | PATCH_ERROR_NOT_FOUND | SO 库或符号未找到 |
| -2 | PATCH_ERROR_PROTECT | 内存保护修改失败 |
| -3 | PATCH_ERROR_ALREADY_PATCHED | 函数已被 patch |
| -4 | PATCH_ERROR_NOT_PATCHED | 函数未被 patch（恢复时） |
| -5 | PATCH_ERROR_INVALID_ARG | 无效参数 |

## 使用示例

### 示例 1：基本用法

```kotlin
val nativeLib = NativeLib()

// 查找 libc.so 中的 malloc 函数
val mallocAddr = nativeLib.findSymbol("libc.so", "malloc")
if (mallocAddr != 0L) {
    // 将 malloc 修改为直接返回 nullptr
    val result = nativeLib.patchFunctionByAddr(mallocAddr, 0)
    if (result == NativeLib.PATCH_SUCCESS) {
        println("malloc 已被 patch")
        
        // 检查是否被 patch
        val isPatched = nativeLib.isFunctionPatched(mallocAddr)
        
        // 恢复函数
        nativeLib.restoreFunctionByAddr(mallocAddr)
    }
}
```

### 示例 2：通过符号名 patch

```kotlin
val nativeLib = NativeLib()

// 将 libm.so 中的 sin 函数修改为返回 0
val result = nativeLib.patchFunctionByName("libm.so", "sin", 0)
if (result == NativeLib.PATCH_SUCCESS) {
    println("sin 函数已被 patch")
}
```

## 技术原理

### ARM64 Return 指令

本工具使用 ARM64 汇编指令实现函数的直接返回：

- **返回 0**: `ret` (0xD65F03C0)
- **返回非零值**: 通过 `mov` 指令将值放入 x0 寄存器，然后执行 `ret`

### 内存保护处理

1. 使用 `mprotect` 将代码段设置为可读写可执行 (PROT_READ | PROT_WRITE | PROT_EXEC)
2. 写入新的指令
3. 恢复代码段为只读可执行 (PROT_READ | PROT_EXEC)

### 线程安全

使用 `std::mutex` 保证多线程环境下的操作安全。

## 注意事项

1. **权限要求**: 需要具有修改内存保护属性的权限
2. **符号查找**: 使用 xdl 库进行符号查找，支持动态符号
3. **返回值类型**: 仅支持 64 位整数或指针类型的返回值
4. **函数调用约定**: 假设函数遵循标准 ARM64 调用约定（返回值在 x0 寄存器）
5. **代码完整性**: patch 会覆盖函数开头的指令，因此函数必须至少有足够的空间容纳新指令

## 文件结构

```
nativelib/src/main/cpp/
├── NativeFunctionPatcher.h    # 头文件，定义类接口
├── NativeFunctionPatcher.cpp  # 实现文件
└── nativelib.cpp              # JNI 接口
```

## 测试

### 单元测试

位于 `src/test/java/com/example/nativelib/NativeFunctionPatcherTest.kt`

### 集成测试

位于 `src/androidTest/java/com/example/nativelib/NativeFunctionPatcherInstrumentedTest.kt`

## 编译

确保在 CMakeLists.txt 中添加了 NativeFunctionPatcher.cpp：

```cmake
add_library(${CMAKE_PROJECT_NAME} SHARED
    ...
    NativeFunctionPatcher.cpp
    nativelib.cpp
)
```