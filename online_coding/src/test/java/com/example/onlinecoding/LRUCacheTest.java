
package com.example.onlinecoding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LRU 缓存单元测试
 * 
 * 测试两种实现方式：
 * 1. LRUCacheWithLinkedHashMap - 使用 LinkedHashMap 实现
 * 2. LRUCacheWithDoubleLinkedList - 使用 HashMap + 双向链表实现
 * 
 * 测试场景：
 * - 基本读写操作
 * - 容量淘汰机制
 * - 值更新操作
 * - 删除操作
 * - 空缓存处理
 * - 边界容量测试
 */
@DisplayName("LRU 缓存测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LRUCacheTest {

    /**
     * 提供测试用的 LRU 缓存实现
     * 
     * @return 包含两种实现的 Stream
     */
    private Stream<LRUCache<Integer, String>> cacheProvider() {
        return Stream.of(
            new LRUCacheWithLinkedHashMap<>(3),
            new LRUCacheWithDoubleLinkedList<>(3)
        );
    }

    /**
     * 测试基本读写操作
     * 
     * 场景：写入数据后读取，验证返回正确的值
     */
    @ParameterizedTest
    @MethodSource("cacheProvider")
    @DisplayName("测试基本读写操作")
    void testBasicGetAndPut(LRUCache<Integer, String> cache) {
        System.out.println("测试: " + cache.getClass().getSimpleName());
        
        // 写入数据
        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");
        
        // 验证大小
        assertEquals(3, cache.size());
        
        // 读取数据，验证返回正确的值
        assertEquals("one", cache.get(1));
        assertEquals("two", cache.get(2));
        assertEquals("three", cache.get(3));
        
        System.out.println("基本读写测试通过");
    }

    /**
     * 测试容量淘汰机制
     * 
     * 场景：写入超过容量的数据，验证最久未使用的数据被淘汰
     */
    @ParameterizedTest
    @MethodSource("cacheProvider")
    @DisplayName("测试容量淘汰机制")
    void testCapacityEviction(LRUCache<Integer, String> cache) {
        System.out.println("测试: " + cache.getClass().getSimpleName());
        
        // 写入数据，填满容量
        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");
        
        // 验证当前大小
        assertEquals(3, cache.size());
        
        // 访问 key=1，使其变为最近使用
        cache.get(1);
        
        // 写入新数据，触发淘汰
        cache.put(4, "four");
        
        // 验证大小仍为 3
        assertEquals(3, cache.size());
        
        // 验证最久未使用的 key=2 被淘汰
        assertNull(cache.get(2));
        
        // 验证其他数据仍然存在
        assertEquals("one", cache.get(1));
        assertEquals("three", cache.get(3));
        assertEquals("four", cache.get(4));
        
        System.out.println("容量淘汰测试通过");
    }

    /**
     * 测试值更新操作
     * 
     * 场景：对已存在的 key 写入新值，验证值被更新
     */
    @ParameterizedTest
    @MethodSource("cacheProvider")
    @DisplayName("测试值更新操作")
    void testValueUpdate(LRUCache<Integer, String> cache) {
        System.out.println("测试: " + cache.getClass().getSimpleName());
        
        // 写入初始数据
        cache.put(1, "one");
        assertEquals("one", cache.get(1));
        
        // 更新值
        cache.put(1, "updated_one");
        
        // 验证值被更新
        assertEquals("updated_one", cache.get(1));
        
        // 验证大小不变
        assertEquals(1, cache.size());
        
        System.out.println("值更新测试通过");
    }

    /**
     * 测试删除操作
     * 
     * 场景：删除指定 key，验证数据被正确删除
     */
    @ParameterizedTest
    @MethodSource("cacheProvider")
    @DisplayName("测试删除操作")
    void testRemove(LRUCache<Integer, String> cache) {
        System.out.println("测试: " + cache.getClass().getSimpleName());
        
        // 写入数据
        cache.put(1, "one");
        cache.put(2, "two");
        
        // 验证初始状态
        assertEquals(2, cache.size());
        assertNotNull(cache.get(1));
        
        // 删除 key=1
        String removedValue = cache.remove(1);
        
        // 验证返回被删除的值
        assertEquals("one", removedValue);
        
        // 验证数据被删除
        assertNull(cache.get(1));
        assertEquals(1, cache.size());
        
        // 验证另一个数据仍然存在
        assertEquals("two", cache.get(2));
        
        // 测试删除不存在的 key
        assertNull(cache.remove(999));
        
        System.out.println("删除操作测试通过");
    }

    /**
     * 测试空缓存处理
     * 
     * 场景：从空缓存读取，验证返回 null
     */
    @ParameterizedTest
    @MethodSource("cacheProvider")
    @DisplayName("测试空缓存处理")
    void testEmptyCache(LRUCache<Integer, String> cache) {
        System.out.println("测试: " + cache.getClass().getSimpleName());
        
        // 验证缓存为空
        assertTrue(cache.isEmpty());
        assertEquals(0, cache.size());
        
        // 从空缓存读取，验证返回 null
        assertNull(cache.get(1));
        
        // 删除不存在的 key，验证返回 null
        assertNull(cache.remove(1));
        
        System.out.println("空缓存测试通过");
    }

    /**
     * 测试边界容量（容量为 1）
     * 
     * 场景：容量设置为 1，验证只能存储一个元素
     */
    @Test
    @DisplayName("测试边界容量（容量为 1）")
    void testBoundaryCapacity() {
        // 使用容量为 1 的缓存
        LRUCache<Integer, String> linkedHashMapCache = new LRUCacheWithLinkedHashMap<>(1);
        LRUCache<Integer, String> linkedListCache = new LRUCacheWithDoubleLinkedList<>(1);
        
        // 测试 LinkedHashMap 实现
        System.out.println("测试: LRUCacheWithLinkedHashMap (容量=1)");
        linkedHashMapCache.put(1, "one");
        assertEquals(1, linkedHashMapCache.size());
        
        // 写入第二个元素，触发淘汰
        linkedHashMapCache.put(2, "two");
        assertEquals(1, linkedHashMapCache.size());
        
        // 验证第一个元素被淘汰
        assertNull(linkedHashMapCache.get(1));
        assertEquals("two", linkedHashMapCache.get(2));
        
        // 测试 HashMap + 双向链表实现
        System.out.println("测试: LRUCacheWithDoubleLinkedList (容量=1)");
        linkedListCache.put(1, "one");
        assertEquals(1, linkedListCache.size());
        
        // 写入第二个元素，触发淘汰
        linkedListCache.put(2, "two");
        assertEquals(1, linkedListCache.size());
        
        // 验证第一个元素被淘汰
        assertNull(linkedListCache.get(1));
        assertEquals("two", linkedListCache.get(2));
        
        System.out.println("边界容量测试通过");
    }

    /**
     * 测试清空缓存操作
     */
    @ParameterizedTest
    @MethodSource("cacheProvider")
    @DisplayName("测试清空缓存操作")
    void testClear(LRUCache<Integer, String> cache) {
        System.out.println("测试: " + cache.getClass().getSimpleName());
        
        // 写入数据
        cache.put(1, "one");
        cache.put(2, "two");
        assertEquals(2, cache.size());
        
        // 清空缓存
        cache.clear();
        
        // 验证缓存为空
        assertTrue(cache.isEmpty());
        assertEquals(0, cache.size());
        assertNull(cache.get(1));
        assertNull(cache.get(2));
        
        System.out.println("清空缓存测试通过");
    }

    /**
     * 测试 LRU 顺序维护
     * 
     * 场景：验证访问顺序正确维护
     */
    @ParameterizedTest
    @MethodSource("cacheProvider")
    @DisplayName("测试 LRU 顺序维护")
    void testLRUOrder(LRUCache<Integer, String> cache) {
        System.out.println("测试: " + cache.getClass().getSimpleName());
        
        // 写入数据: 1, 2, 3
        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");
        
        // 访问顺序: 1 -> 2 -> 3，此时最久未使用的是 1
        
        // 访问 1，使其变为最新
        cache.get(1);
        
        // 写入新数据，应该淘汰 2（现在最久未使用的）
        cache.put(4, "four");
        
        // 验证 2 被淘汰
        assertNull(cache.get(2));
        
        // 验证 1, 3, 4 存在
        assertNotNull(cache.get(1));
        assertNotNull(cache.get(3));
        assertNotNull(cache.get(4));
        
        System.out.println("LRU 顺序维护测试通过");
    }

    /**
     * 测试非法容量参数
     */
    @Test
    @DisplayName("测试非法容量参数")
    void testInvalidCapacity() {
        // 测试容量为 0
        assertThrows(IllegalArgumentException.class, () -> {
            new LRUCacheWithLinkedHashMap<>(0);
        });
        
        // 测试容量为负数
        assertThrows(IllegalArgumentException.class, () -> {
            new LRUCacheWithDoubleLinkedList<>(-1);
        });
        
        System.out.println("非法容量参数测试通过");
    }
}
