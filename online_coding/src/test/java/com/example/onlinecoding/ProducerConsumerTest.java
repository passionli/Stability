package com.example.onlinecoding;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 生产者-消费者模型单元测试
 * 
 * 测试两种实现方式：
 * 1. BlockingQueue 实现
 * 2. wait()/notify() 实现
 */
@DisplayName("生产者-消费者模型测试")
class ProducerConsumerTest {
    
    /**
     * 测试超时时间（秒）
     */
    private static final long TIMEOUT_SECONDS = 30;
    
    /**
     * 每个生产者生产的数据量
     */
    private static final int PRODUCE_COUNT = 10;
    
    /**
     * 每个消费者消费的数据量
     */
    private static final int CONSUME_COUNT = 10;
    
    /**
     * 存储所有测试线程，用于测试结束后清理
     */
    private List<Thread> threads;
    
    @BeforeEach
    void setUp() {
        // 初始化线程列表
        threads = new ArrayList<>();
    }
    
    @AfterEach
    void tearDown() {
        // 清理所有线程
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
    }
    
    /**
     * 测试 BlockingQueue 实现的生产者-消费者模型
     * 
     * 测试场景：
     * - 2 个生产者，每个生产 10 个数据
     * - 2 个消费者，每个消费 10 个数据
     * - 缓冲区大小为 5
     */
    @Test
    @DisplayName("测试 BlockingQueue 实现")
    void testBlockingQueueImplementation() throws InterruptedException {
        System.out.println("=== 开始测试 BlockingQueue 实现 ===");
        
        // 创建 BlockingQueue 实现的生产者-消费者实例
        BlockingQueueExample example = new BlockingQueueExample();
        
        // 使用 CountDownLatch 等待所有线程完成
        CountDownLatch latch = new CountDownLatch(4); // 2 生产者 + 2 消费者
        
        // 创建生产者线程
        Thread producer1 = new Thread(() -> {
            example.produce(1, PRODUCE_COUNT);
            latch.countDown();
        }, "Producer-1");
        
        Thread producer2 = new Thread(() -> {
            example.produce(2, PRODUCE_COUNT);
            latch.countDown();
        }, "Producer-2");
        
        // 创建消费者线程
        Thread consumer1 = new Thread(() -> {
            example.consume(1, CONSUME_COUNT);
            latch.countDown();
        }, "Consumer-1");
        
        Thread consumer2 = new Thread(() -> {
            example.consume(2, CONSUME_COUNT);
            latch.countDown();
        }, "Consumer-2");
        
        // 添加到线程列表，便于清理
        threads.add(producer1);
        threads.add(producer2);
        threads.add(consumer1);
        threads.add(consumer2);
        
        // 启动所有线程
        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
        
        // 等待所有线程完成，设置超时时间
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        // 停止生产者（虽然测试应该已完成）
        example.stop();
        
        // 验证测试结果
        assertTrue(completed, "测试超时，线程未在规定时间内完成");
        assertEquals(0, example.getBufferSize(), "测试结束时缓冲区应为空");
        
        System.out.println("=== BlockingQueue 实现测试完成 ===");
    }
    
    /**
     * 测试 wait()/notify() 实现的生产者-消费者模型
     * 
     * 测试场景：
     * - 2 个生产者，每个生产 10 个数据
     * - 2 个消费者，每个消费 10 个数据
     * - 缓冲区大小为 5
     */
    @Test
    @DisplayName("测试 wait/notify 实现")
    void testWaitNotifyImplementation() throws InterruptedException {
        System.out.println("=== 开始测试 wait/notify 实现 ===");
        
        // 创建 wait/notify 实现的生产者-消费者实例
        WaitNotifyExample example = new WaitNotifyExample();
        
        // 使用 CountDownLatch 等待所有线程完成
        CountDownLatch latch = new CountDownLatch(4); // 2 生产者 + 2 消费者
        
        // 创建生产者线程
        Thread producer1 = new Thread(() -> {
            example.produce(1, PRODUCE_COUNT);
            latch.countDown();
        }, "Producer-1");
        
        Thread producer2 = new Thread(() -> {
            example.produce(2, PRODUCE_COUNT);
            latch.countDown();
        }, "Producer-2");
        
        // 创建消费者线程
        Thread consumer1 = new Thread(() -> {
            example.consume(1, CONSUME_COUNT);
            latch.countDown();
        }, "Consumer-1");
        
        Thread consumer2 = new Thread(() -> {
            example.consume(2, CONSUME_COUNT);
            latch.countDown();
        }, "Consumer-2");
        
        // 添加到线程列表，便于清理
        threads.add(producer1);
        threads.add(producer2);
        threads.add(consumer1);
        threads.add(consumer2);
        
        // 启动所有线程
        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
        
        // 等待所有线程完成，设置超时时间
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        // 停止生产者（虽然测试应该已完成）
        example.stop();
        
        // 验证测试结果
        assertTrue(completed, "测试超时，线程未在规定时间内完成");
        assertEquals(0, example.getBufferSize(), "测试结束时缓冲区应为空");
        
        System.out.println("=== wait/notify 实现测试完成 ===");
    }
    
    /**
     * 测试单个生产者和单个消费者的场景
     * 用于验证基本的生产-消费流程
     */
    @Test
    @DisplayName("测试单生产者单消费者")
    void testSingleProducerSingleConsumer() throws InterruptedException {
        System.out.println("=== 开始测试单生产者单消费者 ===");
        
        BlockingQueueExample example = new BlockingQueueExample();
        CountDownLatch latch = new CountDownLatch(2);
        
        Thread producer = new Thread(() -> {
            example.produce(1, 5);
            latch.countDown();
        }, "Single-Producer");
        
        Thread consumer = new Thread(() -> {
            example.consume(1, 5);
            latch.countDown();
        }, "Single-Consumer");
        
        threads.add(producer);
        threads.add(consumer);
        
        producer.start();
        consumer.start();
        
        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "测试超时");
        assertEquals(0, example.getBufferSize(), "缓冲区应为空");
        
        System.out.println("=== 单生产者单消费者测试完成 ===");
    }
}