package com.example.onlinecoding;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 使用 wait()/notify() 实现生产者-消费者模型
 * 
 * 这是传统的同步方式，需要手动处理锁和条件判断
 * 
 * 核心机制：
 * 1. 使用 synchronized 关键字获取对象锁
 * 2. wait()：释放锁并进入等待状态，直到被 notify/notifyAll 唤醒
 * 3. notifyAll()：唤醒所有在该对象上等待的线程
 * 4. 必须在循环中检查条件，防止虚假唤醒（spurious wakeup）
 */
public class WaitNotifyExample {
    
    /**
     * 缓冲区大小，限制队列中最多存放的数据量
     */
    private static final int BUFFER_SIZE = 5;
    
    /**
     * 使用 LinkedList 作为缓冲区（非线程安全）
     * 需要手动添加同步机制
     */
    private final Queue<Integer> buffer;
    
    /**
     * 控制生产者停止生产的标志
     * 使用 volatile 保证可见性
     */
    private volatile boolean isRunning;
    
    /**
     * 构造方法，初始化缓冲区
     */
    public WaitNotifyExample() {
        this.buffer = new LinkedList<>();
        this.isRunning = true;
    }
    
    /**
     * 生产者任务：向缓冲区中放入数据
     * 
     * @param producerId 生产者标识，用于区分不同生产者
     * @param count 生产数据的数量
     */
    public void produce(int producerId, int count) {
        try {
            for (int i = 0; i < count && isRunning; i++) {
                // 生成数据
                int data = producerId * 100 + i;
                
                // 使用 synchronized 获取对象锁
                // 锁对象使用 this，确保生产者和消费者使用同一把锁
                synchronized (this) {
                    // 循环检查条件：缓冲区满时等待
                    // 必须使用 while 循环，不能使用 if
                    // 原因：wait() 返回后可能是虚假唤醒，需要重新检查条件
                    while (buffer.size() == BUFFER_SIZE) {
                        System.out.printf("[生产者-%d] 缓冲区满，等待...%n", producerId);
                        // wait() 方法：
                        // 1. 释放当前持有的锁
                        // 2. 将当前线程加入等待队列
                        // 3. 直到被 notify/notifyAll 唤醒
                        this.wait();
                    }
                    
                    // 缓冲区有空间，放入数据
                    buffer.offer(data);
                    System.out.printf("[生产者-%d] 生产数据: %d, 缓冲区大小: %d%n", 
                        producerId, data, buffer.size());
                    
                    // notifyAll() 唤醒所有等待的线程
                    // 这里唤醒消费者线程，告知缓冲区有数据了
                    // 也可能唤醒其他生产者，但它们会在 while 循环中继续等待
                    this.notifyAll();
                }
                
                // 模拟生产耗时（在 synchronized 块外部，避免长时间持有锁）
                Thread.sleep((long) (Math.random() * 500));
            }
        } catch (InterruptedException e) {
            // 线程被中断时恢复中断状态
            Thread.currentThread().interrupt();
            System.out.printf("[生产者-%d] 被中断%n", producerId);
        }
    }
    
    /**
     * 消费者任务：从缓冲区中取出数据
     * 
     * @param consumerId 消费者标识，用于区分不同消费者
     * @param count 消费数据的数量
     */
    public void consume(int consumerId, int count) {
        try {
            for (int i = 0; i < count && isRunning; i++) {
                Integer data = null;
                
                // 使用 synchronized 获取对象锁
                synchronized (this) {
                    // 循环检查条件：缓冲区空时等待
                    while (buffer.isEmpty()) {
                        System.out.printf("[消费者-%d] 缓冲区空，等待...%n", consumerId);
                        this.wait();
                    }
                    
                    // 缓冲区有数据，取出数据
                    data = buffer.poll();
                    System.out.printf("[消费者-%d] 消费数据: %d, 缓冲区大小: %d%n", 
                        consumerId, data, buffer.size());
                    
                    // notifyAll() 唤醒所有等待的线程
                    // 这里唤醒生产者线程，告知缓冲区有空间了
                    this.notifyAll();
                }
                
                // 模拟消费耗时（在 synchronized 块外部）
                Thread.sleep((long) (Math.random() * 800));
            }
        } catch (InterruptedException e) {
            // 线程被中断时恢复中断状态
            Thread.currentThread().interrupt();
            System.out.printf("[消费者-%d] 被中断%n", consumerId);
        }
    }
    
    /**
     * 停止生产和消费
     */
    public void stop() {
        this.isRunning = false;
        // 唤醒所有等待的线程，使其退出
        synchronized (this) {
            this.notifyAll();
        }
    }
    
    /**
     * 获取当前缓冲区大小
     */
    public int getBufferSize() {
        synchronized (this) {
            return buffer.size();
        }
    }
}