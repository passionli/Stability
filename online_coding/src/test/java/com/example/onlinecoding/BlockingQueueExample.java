package com.example.onlinecoding;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 使用 BlockingQueue 实现生产者-消费者模型
 * 
 * BlockingQueue 是 Java 并发包提供的线程安全队列，
 * 内置了阻塞机制，无需手动处理 wait/notify
 * 
 * @see java.util.concurrent.BlockingQueue
 * @see java.util.concurrent.ArrayBlockingQueue
 */
public class BlockingQueueExample {
    
    /**
     * 缓冲区大小，限制队列中最多存放的数据量
     */
    private static final int BUFFER_SIZE = 5;
    
    /**
     * 使用 ArrayBlockingQueue 作为缓冲区
     * ArrayBlockingQueue 是一个有界阻塞队列，基于数组实现
     * 特点：
     * - 有界：容量固定，不能扩容
     * - 阻塞：队列满时 put() 阻塞，队列空时 take() 阻塞
     * - 线程安全：内部使用 ReentrantLock 保证线程安全
     */
    private final BlockingQueue<Integer> buffer;
    
    /**
     * 控制生产者停止生产的标志
     */
    private volatile boolean isRunning;
    
    /**
     * 构造方法，初始化缓冲区
     */
    public BlockingQueueExample() {
        this.buffer = new ArrayBlockingQueue<>(BUFFER_SIZE);
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
                
                // put() 方法：如果队列已满，当前线程会被阻塞，直到队列有空间
                // 相比 add() 和 offer()，put() 会阻塞而不是抛出异常或返回 false
                buffer.put(data);
                
                // 打印生产信息
                System.out.printf("[生产者-%d] 生产数据: %d, 缓冲区大小: %d%n", 
                    producerId, data, buffer.size());
                
                // 模拟生产耗时
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
                // take() 方法：如果队列为空，当前线程会被阻塞，直到队列有数据
                // 相比 remove() 和 poll()，take() 会阻塞而不是抛出异常或返回 null
                Integer data = buffer.take();
                
                // 打印消费信息
                System.out.printf("[消费者-%d] 消费数据: %d, 缓冲区大小: %d%n", 
                    consumerId, data, buffer.size());
                
                // 模拟消费耗时
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
    }
    
    /**
     * 获取当前缓冲区大小
     */
    public int getBufferSize() {
        return buffer.size();
    }
}