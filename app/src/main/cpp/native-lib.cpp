#include <jni.h>
#include <string>
#include <iostream>
#include <vector>
#include <map>
#include <memory>
#include <thread>
#include <mutex>
#include <exception>

/**
 * 日志宏，用于在 C++ 代码中打印日志
 */
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "Cpp", __VA_ARGS__)

// 包含 Android 日志头文件
#include <android/log.h>

/**
 * 模板函数，返回两个值中的最大值
 */
template <typename T>
T max(T a, T b) {
    return a > b ? a : b;
}

/**
 * 模板类，实现栈数据结构
 */
template <typename T>
class Stack {
private:
    std::vector<T> elements;
public:
    void push(T const &item) {
        elements.push_back(item);
    }
    
    T pop() {
        if (elements.empty()) {
            throw std::out_of_range("Stack<>::pop(): empty stack");
        }
        T top = elements.back();
        elements.pop_back();
        return top;
    }
    
    bool empty() const {
        return elements.empty();
    }
};

/**
 * 基本数据类型示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_cpp_basic_BasicCppExample_basicDataTypes(JNIEnv *env, jobject obj) {
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
    
    // 布尔型
    bool b = true;
    LOGD("布尔型: %s", b ? "true" : "false");
    
    // 字符串
    std::string s = "Hello, C++!";
    LOGD("字符串: %s", s.c_str());
    
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
JNIEXPORT jint JNICALL Java_com_example_stability_cpp_basic_BasicCppExample_controlFlow(JNIEnv *env, jobject obj) {
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
 * 函数示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_cpp_basic_BasicCppExample_functions(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行函数示例 ===");
    
    // 声明函数
    int add(int a, int b);
    void printHello();
    
    // 调用函数
    int result = add(10, 20);
    LOGD("add(10, 20) = %d", result);
    
    printHello();
    
    // 打印日志
    LOGD("=== 函数示例完成 ===");
    
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
    LOGD("Hello from C++ function!");
}

/**
 * 面向对象编程示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_cpp_intermediate_IntermediateCppExample_objectOrientedProgramming(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行面向对象编程示例 ===");
    
    // 定义类
    class Person {
    private:
        std::string name;
        int age;
    public:
        // 构造函数
        Person(std::string n, int a) : name(n), age(a) {
            LOGD("Person constructor called: %s, %d", name.c_str(), age);
        }
        
        // 析构函数
        ~Person() {
            LOGD("Person destructor called: %s", name.c_str());
        }
        
        // 成员函数
        void sayHello() {
            LOGD("Hello, my name is %s, I'm %d years old", name.c_str(), age);
        }
        
        // 静态成员函数
        static void staticMethod() {
            LOGD("Static method called");
        }
    };
    
    // 创建对象
    Person p("Alice", 20);
    p.sayHello();
    
    // 调用静态方法
    Person::staticMethod();
    
    // 继承示例
    class Student : public Person {
    private:
        std::string school;
    public:
        // 构造函数
        Student(std::string n, int a, std::string s) : Person(n, a), school(s) {
            LOGD("Student constructor called: %s, %s", n.c_str(), s.c_str());
        }
        
        // 重写方法
        void sayHello() {
            LOGD("Hello, I'm a student at %s", school.c_str());
        }
    };
    
    // 创建派生类对象
    Student s("Bob", 18, "Harvard");
    s.sayHello();
    
    // 打印日志
    LOGD("=== 面向对象编程示例完成 ===");
    
    return 0;
}

/**
 * STL 示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_cpp_intermediate_IntermediateCppExample_stl(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行 STL 示例 ===");
    
    // vector 示例
    LOGD("vector 示例:");
    std::vector<int> v;
    v.push_back(1);
    v.push_back(2);
    v.push_back(3);
    for (int i = 0; i < v.size(); i++) {
        LOGD("v[%d] = %d", i, v[i]);
    }
    
    // map 示例
    LOGD("map 示例:");
    std::map<std::string, int> m;
    m["Alice"] = 20;
    m["Bob"] = 18;
    m["Charlie"] = 22;
    for (auto &pair : m) {
        LOGD("%s: %d", pair.first.c_str(), pair.second);
    }
    
    // 打印日志
    LOGD("=== STL 示例完成 ===");
    
    return 0;
}

/**
 * 异常处理示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_cpp_intermediate_IntermediateCppExample_exceptionHandling(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行异常处理示例 ===");
    
    try {
        LOGD("Try block entered");
        
        // 抛出异常
        throw std::runtime_error("Test exception");
        
        LOGD("This line will not be executed");
    } catch (std::exception &e) {
        LOGD("Exception caught: %s", e.what());
    } catch (...) {
        LOGD("Unknown exception caught");
    }
    
    // 打印日志
    LOGD("=== 异常处理示例完成 ===");
    
    return 0;
}

/**
 * 模板示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_cpp_advanced_AdvancedCppExample_templates(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行模板示例 ===");
    
    // 使用模板函数
    int i_val = max(10, 20);
    LOGD("max(10, 20) = %d", i_val);
    
    double d_val = max(3.14, 2.71);
    LOGD("max(3.14, 2.71) = %lf", d_val);
    
    // 使用模板类
    Stack<int> intStack;
    intStack.push(1);
    intStack.push(2);
    intStack.push(3);
    LOGD("Stack elements:");
    while (!intStack.empty()) {
        LOGD("%d", intStack.pop());
    }
    
    // 打印日志
    LOGD("=== 模板示例完成 ===");
    
    return 0;
}

/**
 * 智能指针示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_cpp_advanced_AdvancedCppExample_smartPointers(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行智能指针示例 ===");
    
    // unique_ptr 示例
    LOGD("unique_ptr 示例:");
    std::unique_ptr<int> up1(new int(10));
    LOGD("*up1 = %d", *up1);
    
    // 转移所有权
    std::unique_ptr<int> up2 = std::move(up1);
    LOGD("*up2 = %d", *up2);
    
    // shared_ptr 示例
    LOGD("shared_ptr 示例:");
    std::shared_ptr<int> sp1(new int(20));
    LOGD("*sp1 = %d, use_count = %ld", *sp1, sp1.use_count());
    
    // 共享所有权
    std::shared_ptr<int> sp2 = sp1;
    LOGD("*sp2 = %d, use_count = %ld", *sp2, sp1.use_count());
    
    // weak_ptr 示例
    LOGD("weak_ptr 示例:");
    std::weak_ptr<int> wp = sp1;
    if (auto sp3 = wp.lock()) {
        LOGD("*sp3 = %d", *sp3);
    }
    
    // 打印日志
    LOGD("=== 智能指针示例完成 ===");
    
    return 0;
}

/**
 * 多线程示例
 */
JNIEXPORT jint JNICALL Java_com_example_stability_cpp_advanced_AdvancedCppExample_multithreading(JNIEnv *env, jobject obj) {
    // 打印日志
    LOGD("=== 运行多线程示例 ===");
    
    // 共享变量
    int counter = 0;
    std::mutex mtx;
    
    // 线程函数
    auto threadFunction = [&counter, &mtx](int id) {
        for (int i = 0; i < 5; i++) {
            // 加锁
            std::lock_guard<std::mutex> lock(mtx);
            counter++;
            LOGD("Thread %d: counter = %d", id, counter);
            // 自动解锁
        }
    };
    
    // 创建线程
    std::thread t1(threadFunction, 1);
    std::thread t2(threadFunction, 2);
    
    // 等待线程完成
    t1.join();
    t2.join();
    
    LOGD("Final counter value: %d", counter);
    
    // 打印日志
    LOGD("=== 多线程示例完成 ===");
    
    return 0;
}
