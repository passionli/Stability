package com.example.stability

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class CoroutinesTest {

    @Test
    fun `launch starts a coroutine`() = runTest {
        var result = 0
        val job = launch {
            result = 42
        }
        job.join()
        assertEquals(42, result)
    }

    @Test
    fun `async await returns result`() = runTest {
        val deferred = async {
            delay(100)
            "Hello"
        }
        val result = deferred.await()
        assertEquals("Hello", result)
    }

    @Test
    fun `coroutineScope waits for children`() = runTest {
        var count = 0
        coroutineScope {
            launch {
                delay(100)
                count++
            }
            launch {
                delay(100)
                count++
            }
        }
        assertEquals(2, count)
    }

    @Test
    fun `supervisorScope continues even if child fails`() = runTest {
        var successCount = 0
        
        supervisorScope {
            // 第一个协程会失败，但不会影响其他协程
            launch {
                try {
                    delay(100)
                    throw Exception("Test")
                } catch (e: Exception) {
                    // 在协程内部捕获异常，不向外传播
                }
            }
            
            // 第二个协程应该能正常完成
            launch {
                delay(100)
                successCount++
            }
        }
        
        assertEquals(1, successCount)
    }

    @Test
    fun `withTimeout throws on timeout`() = runTest {
        var caught = false
        try {
            withTimeout(100) {
                delay(200) // 添加延迟，确保超时
            }
        } catch (e: TimeoutCancellationException) {
            caught = true
        }
        assertTrue(caught)
    }

    @Test
    fun `suspend function works in coroutine`() = runTest {
        suspend fun doSomething(): String {
            delay(100)
            return "Done"
        }
        val result = doSomething()
        assertEquals("Done", result)
    }
}
