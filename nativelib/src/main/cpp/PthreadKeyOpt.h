//
// Created by liguang on 2026/4/3.
//

#ifndef STABILITY_PTHREADKEYOPT_H
#define STABILITY_PTHREADKEYOPT_H

#include <iostream>
// 禁用拷贝/移动的工具头文件（C++11）
#include <utility>

class PthreadKeyOpt {

public:
    // 1. 删除拷贝构造、拷贝赋值，禁止复制对象
    PthreadKeyOpt(const PthreadKeyOpt&) = delete;
    PthreadKeyOpt& operator=(const PthreadKeyOpt&) = delete;
    // 2. 删除移动构造、移动赋值，禁止转移对象
    PthreadKeyOpt(PthreadKeyOpt&&) = delete;
    PthreadKeyOpt& operator=(PthreadKeyOpt&&) = delete;

    // 3. 静态获取实例方法（核心）
    static PthreadKeyOpt& getInstance() {
        // C++11 保证：局部静态变量 线程安全、只初始化一次
        static PthreadKeyOpt instance;
        return instance;
    }

    int start();

private:
    // 4. 构造函数私有化，禁止外部创建对象
    PthreadKeyOpt() {
        std::cout << "单例构造（仅执行一次）" << std::endl;
    }
    // 析构函数私有化（可选，防止外部delete）
    ~PthreadKeyOpt() = default;
};


#endif //STABILITY_PTHREADKEYOPT_H
