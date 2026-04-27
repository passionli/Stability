#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h> // 为了使用 usleep 函数

/**
 * 日志宏，用于在 C 代码中打印日志
 */
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "C", __VA_ARGS__)

// 包含 Android 日志头文件
#include <android/log.h>

/**
 * 指针示例中使用的函数
 */
void increment(int *x) {
    (*x)++;
}

/**
 * 数组示例中使用的函数
 */
int sumArray(int arr[], int size) {
    int sum = 0;
    for (int i = 0; i < size; i++) {
        sum += arr[i];
    }
    return sum;
}

/**
 * 结构体示例中使用的结构体类型
 */
struct Person {
    char name[50];
    int age;
    float height;
};

/**
 * 结构体示例中使用的函数
 */
void printPerson(struct Person p) {
    LOGD("Person: name = %s, age = %d, height = %f", p.name, p.age, p.height);
}

/**
 * 多线程示例中使用的线程数据结构
 */
typedef struct {
    int id;
    int iterations;
} ThreadData;

/**
 * 多线程示例中使用的线程函数
 */
void *threadFunction(void *arg) {
    ThreadData *data = (ThreadData *)arg;
    
    for (int i = 0; i < data->iterations; i++) {
        LOGD("Thread %d: iteration %d", data->id, i);
        // 模拟耗时操作
        usleep(100000); // 100ms
    }
    
    free(data);
    return NULL;
}

/**
 * 基本数据类型示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_c_basic_BasicCExample_basicDataTypes(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行基本数据类型示例 ===");
    
    // 整型
    int i = 10;
    LOGD("整型: %d", i);
    
    // 浮点型
    float f = 3.14f;
    LOGD("浮点型: %f", f);
    
    // 双精度浮点型
    double d = 3.1415926;
    LOGD("双精度浮点型: %lf", d);
    
    // 字符型
    char c = 'A';
    LOGD("字符型: %c", c);
    
    // 字符串
    char s[] = "Hello, C!";
    LOGD("字符串: %s", s);
    
    // 数组
    int arr[5] = {1, 2, 3, 4, 5};
    LOGD("数组: ");
    for (int j = 0; j < 5; j++) {
        LOGD("%d ", arr[j]);
    }
    
    // 打印日志
    LOGD("=== 基本数据类型示例完成 ===");
    
    return 0;
}

/**
 * 控制流示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_c_basic_BasicCExample_controlFlow(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行控制流示例 ===");
    
    // if-else 语句
    int x = 10;
    if (x > 5) {
        LOGD("x > 5");
    } else {
        LOGD("x <= 5");
    }
    
    // for 循环
    LOGD("for 循环:");
    for (int i = 0; i < 5; i++) {
        LOGD("i = %d", i);
    }
    
    // while 循环
    LOGD("while 循环:");
    int j = 0;
    while (j < 5) {
        LOGD("j = %d", j);
        j++;
    }
    
    // switch 语句
    LOGD("switch 语句:");
    int k = 2;
    switch (k) {
        case 1:
            LOGD("k = 1");
            break;
        case 2:
            LOGD("k = 2");
            break;
        default:
            LOGD("k = other");
            break;
    }
    
    // 打印日志
    LOGD("=== 控制流示例完成 ===");
    
    return 0;
}

/**
 * 加法函数
 */
int add(int a, int b) {
    return a + b;
}

/**
 * 打印 Hello 的函数
 */
void printHello() {
    LOGD("Hello from C function!");
}

/**
 * 函数示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_c_basic_BasicCExample_functions(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行函数示例 ===");
    
    // 调用函数
    int result = add(10, 20);
    LOGD("add(10, 20) = %d", result);
    
    printHello();
    
    // 打印日志
    LOGD("=== 函数示例完成 ===");
    
    return 0;
}

/**
 * 指针示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_c_intermediate_IntermediateCExample_pointers(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行指针示例 ===");
    
    // 基本指针操作
    int num = 100;
    int *p = &num;
    LOGD("num = %d, *p = %d, &num = %p, p = %p", num, *p, &num, p);
    
    // 指针算术
    int arr[] = {1, 2, 3, 4, 5};
    int *ptr = arr;
    LOGD("数组元素:");
    for (int i = 0; i < 5; i++) {
        LOGD("arr[%d] = %d, *(ptr + %d) = %d", i, arr[i], i, *(ptr + i));
    }
    
    // 指针作为函数参数
    int value = 5;
    LOGD("Before increment: value = %d", value);
    increment(&value);
    LOGD("After increment: value = %d", value);
    
    // 打印日志
    LOGD("=== 指针示例完成 ===");
    
    return 0;
}

/**
 * 数组示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_c_intermediate_IntermediateCExample_arrays(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行数组示例 ===");
    
    // 一维数组
    int oneDimensional[5] = {1, 2, 3, 4, 5};
    LOGD("一维数组:");
    for (int i = 0; i < 5; i++) {
        LOGD("%d ", oneDimensional[i]);
    }
    LOGD("");
    
    // 二维数组
    int twoDimensional[2][3] = {{1, 2, 3}, {4, 5, 6}};
    LOGD("二维数组:");
    for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 3; j++) {
            LOGD("%d ", twoDimensional[i][j]);
        }
        LOGD("");
    }
    
    // 字符数组
    char str[] = "Hello, C Array!";
    LOGD("字符数组: %s", str);
    
    // 数组作为函数参数
    int sum = sumArray(oneDimensional, 5);
    LOGD("数组元素和: %d", sum);
    
    // 打印日志
    LOGD("=== 数组示例完成 ===");
    
    return 0;
}

/**
 * 结构体示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_c_intermediate_IntermediateCExample_structures(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行结构体示例 ===");
    
    // 创建结构体变量
    struct Person person1;
    strcpy(person1.name, "Alice");
    person1.age = 20;
    person1.height = 1.65;
    
    LOGD("Person 1: name = %s, age = %d, height = %f", person1.name, person1.age, person1.height);
    
    // 结构体指针
    struct Person *personPtr = &person1;
    LOGD("Person 1 via pointer: name = %s, age = %d, height = %f", personPtr->name, personPtr->age, personPtr->height);
    
    // 结构体作为函数参数
    printPerson(person1);
    
    // 打印日志
    LOGD("=== 结构体示例完成 ===");
    
    return 0;
}

/**
 * 内存管理示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_c_advanced_AdvancedCExample_memoryManagement(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行内存管理示例 ===");
    
    // 动态内存分配
    int *dynamicArray = (int *)malloc(5 * sizeof(int));
    if (dynamicArray == NULL) {
        LOGD("内存分配失败");
        return -1;
    }
    
    // 初始化动态数组
    for (int i = 0; i < 5; i++) {
        dynamicArray[i] = i * 10;
    }
    
    LOGD("动态数组元素:");
    for (int i = 0; i < 5; i++) {
        LOGD("%d ", dynamicArray[i]);
    }
    LOGD("");
    
    // 重新分配内存
    dynamicArray = (int *)realloc(dynamicArray, 10 * sizeof(int));
    if (dynamicArray == NULL) {
        LOGD("内存重新分配失败");
        return -1;
    }
    
    // 初始化新分配的内存
    for (int i = 5; i < 10; i++) {
        dynamicArray[i] = i * 10;
    }
    
    LOGD("重新分配后的动态数组元素:");
    for (int i = 0; i < 10; i++) {
        LOGD("%d ", dynamicArray[i]);
    }
    LOGD("");
    
    // 释放内存
    free(dynamicArray);
    LOGD("内存已释放");
    
    // 打印日志
    LOGD("=== 内存管理示例完成 ===");
    
    return 0;
}

/**
 * 文件操作示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_c_advanced_AdvancedCExample_fileOperations(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行文件操作示例 ===");
    
    // 获取应用数据目录
    jclass contextClass = (*env)->FindClass(env, "android/content/Context");
    jmethodID getFilesDirMethod = (*env)->GetMethodID(env, contextClass, "getFilesDir", "()Ljava/io/File;");
    jobject filesDir = (*env)->CallObjectMethod(env, obj, getFilesDirMethod);
    
    jclass fileClass = (*env)->FindClass(env, "java/io/File");
    jmethodID getAbsolutePathMethod = (*env)->GetMethodID(env, fileClass, "getAbsolutePath", "()Ljava/lang/String;");
    jstring pathString = (jstring)(*env)->CallObjectMethod(env, filesDir, getAbsolutePathMethod);
    const char *path = (*env)->GetStringUTFChars(env, pathString, NULL);
    
    // 构建文件路径
    char filePath[256];
    snprintf(filePath, sizeof(filePath), "%s/test.txt", path);
    LOGD("文件路径: %s", filePath);
    
    // 写入文件
    FILE *file = fopen(filePath, "w");
    if (file == NULL) {
        LOGD("文件打开失败");
        return -1;
    }
    
    fprintf(file, "Hello, C File Operations!\n");
    fprintf(file, "This is a test file.\n");
    fclose(file);
    LOGD("文件写入成功");
    
    // 读取文件
    file = fopen(filePath, "r");
    if (file == NULL) {
        LOGD("文件打开失败");
        return -1;
    }
    
    char buffer[256];
    LOGD("文件内容:");
    while (fgets(buffer, sizeof(buffer), file) != NULL) {
        LOGD("%s", buffer);
    }
    fclose(file);
    LOGD("文件读取成功");
    
    // 释放资源
    (*env)->ReleaseStringUTFChars(env, pathString, path);
    
    // 打印日志
    LOGD("=== 文件操作示例完成 ===");
    
    return 0;
}

/**
 * 多线程示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_c_advanced_AdvancedCExample_multithreading(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行多线程示例 ===");
    
    // 创建线程
    pthread_t threads[3];
    
    for (int i = 0; i < 3; i++) {
        ThreadData *data = (ThreadData *)malloc(sizeof(ThreadData));
        data->id = i + 1;
        data->iterations = 5;
        
        int result = pthread_create(&threads[i], NULL, threadFunction, data);
        if (result != 0) {
            LOGD("线程创建失败: %d", result);
            return -1;
        }
        LOGD("线程 %d 创建成功", i + 1);
    }
    
    // 等待所有线程完成
    for (int i = 0; i < 3; i++) {
        pthread_join(threads[i], NULL);
        LOGD("线程 %d 完成", i + 1);
    }
    
    // 打印日志
    LOGD("=== 多线程示例完成 ===");
    
    return 0;
}
