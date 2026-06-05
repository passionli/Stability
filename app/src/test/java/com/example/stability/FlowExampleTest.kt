package com.example.stability

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class FlowExampleTest {

    @Test
    fun `basic flow emits values in order`() = runTest {
        val values = mutableListOf<Int>()
        flow {
            for (i in 1..5) {
                emit(i)
            }
        }.collect {
            values.add(it)
        }
        assertEquals(listOf(1, 2, 3, 4, 5), values)
    }

    @Test
    fun `filter operator keeps only even numbers`() = runTest {
        val values = mutableListOf<Int>()
        flow {
            for (i in 1..10) {
                emit(i)
            }
        }.filter { it % 2 == 0 }
            .collect {
                values.add(it)
            }
        assertEquals(listOf(2, 4, 6, 8, 10), values)
    }

    @Test
    fun `map operator transforms values`() = runTest {
        val values = mutableListOf<Int>()
        flow {
            for (i in 1..5) {
                emit(i)
            }
        }.map { it * 2 }
            .collect {
                values.add(it)
            }
        assertEquals(listOf(2, 4, 6, 8, 10), values)
    }

    @Test
    fun `take operator takes only first N values`() = runTest {
        val values = mutableListOf<Int>()
        flow {
            for (i in 1..10) {
                emit(i)
            }
        }.take(3)
            .collect {
                values.add(it)
            }
        assertEquals(listOf(1, 2, 3), values)
    }

    @Test
    fun `combined operators work together`() = runTest {
        val values = mutableListOf<Int>()
        flow {
            for (i in 1..10) {
                emit(i)
            }
        }.filter { it % 2 == 0 }
            .map { it * 2 }
            .take(3)
            .collect {
                values.add(it)
            }
        assertEquals(listOf(4, 8, 12), values)
    }

    @Test
    fun `cold flow executes only when collected`() = runTest {
        var executionCount = 0
        val coldFlow = flow {
            executionCount++
            emit(1)
        }
        assertEquals(0, executionCount)
        coldFlow.collect()
        assertEquals(1, executionCount)
    }

    @Test
    fun `cold flow re-executes on each collection`() = runTest {
        var executionCount = 0
        val coldFlow = flow {
            executionCount++
            emit(1)
        }
        coldFlow.collect()
        assertEquals(1, executionCount)
        coldFlow.collect()
        assertEquals(2, executionCount)
    }

    @Test
    fun `catch operator handles exceptions`() = runTest {
        val values = mutableListOf<Int>()
        var caughtException: Exception? = null
        flow {
            emit(1)
            emit(2)
            throw Exception("Test exception")
        }.catch { e ->
            caughtException = e as Exception
            emit(-1)
        }.collect {
            values.add(it)
        }
        assertEquals(listOf(1, 2, -1), values)
        assertNotNull(caughtException)
        assertEquals("Test exception", caughtException?.message)
    }

    @Test
    fun `buffer operator works with backpressure`() = runTest {
        val emittedValues = mutableListOf<Int>()
        val collectedValues = mutableListOf<Int>()
        flow {
            for (i in 1..5) {
                emittedValues.add(i)
                emit(i)
            }
        }.buffer(2)
            .collect {
                collectedValues.add(it)
            }
        assertEquals(listOf(1, 2, 3, 4, 5), emittedValues)
        assertEquals(listOf(1, 2, 3, 4, 5), collectedValues)
    }

    @Test
    fun `combine operator combines latest values`() = runTest {
        val values = mutableListOf<String>()
        val flow1 = flow {
            emit(1)
            emit(2)
        }
        val flow2 = flow {
            emit("A")
            emit("B")
        }
        flow1.combine(flow2) { a, b ->
            "$a$b"
        }.collect {
            values.add(it)
        }
        assertTrue(values.isNotEmpty())
    }

    @Test
    fun `stateFlow has initial value`() = runTest {
        val stateFlow = MutableStateFlow(0)
        assertEquals(0, stateFlow.value)
    }

    @Test
    fun `stateFlow notifies on value change`() = runTest {
        val collectedValues = mutableListOf<Int>()
        val stateFlow = MutableStateFlow(0)
        val job = launch {
            stateFlow.collect {
                collectedValues.add(it)
            }
        }
        stateFlow.value = 1
        stateFlow.value = 2
        stateFlow.value = 2
        stateFlow.value = 3
        job.cancel()
        assertTrue(collectedValues.containsAll(listOf(0, 1, 2, 3)))
    }

    @Test
    fun `stateFlow does not notify on same value`() = runTest {
        val collectedValues = mutableListOf<Int>()
        val stateFlow = MutableStateFlow(0)
        val job = launch {
            stateFlow.collect {
                collectedValues.add(it)
            }
        }
        stateFlow.value = 1
        stateFlow.value = 1
        job.cancel()
        assertEquals(listOf(0, 1), collectedValues)
    }

    @Test
    fun `sharedFlow emits to multiple collectors`() = runTest {
        val values1 = mutableListOf<Int>()
        val values2 = mutableListOf<Int>()
        val sharedFlow = MutableSharedFlow<Int>()
        val job1 = launch {
            sharedFlow.collect {
                values1.add(it)
            }
        }
        val job2 = launch {
            sharedFlow.collect {
                values2.add(it)
            }
        }
        sharedFlow.emit(1)
        sharedFlow.emit(2)
        job1.cancel()
        job2.cancel()
        assertEquals(listOf(1, 2), values1)
        assertEquals(listOf(1, 2), values2)
    }

    @Test
    fun `sharedFlow late collector misses previous values`() = runTest {
        val values1 = mutableListOf<Int>()
        val values2 = mutableListOf<Int>()
        val sharedFlow = MutableSharedFlow<Int>()
        val job1 = launch {
            sharedFlow.collect {
                values1.add(it)
            }
        }
        sharedFlow.emit(1)
        val job2 = launch {
            sharedFlow.collect {
                values2.add(it)
            }
        }
        sharedFlow.emit(2)
        job1.cancel()
        job2.cancel()
        assertEquals(listOf(1, 2), values1)
        assertEquals(listOf(2), values2)
    }

    @Test
    fun `flow toList converts flow to list`() = runTest {
        val result = flow {
            for (i in 1..5) {
                emit(i)
            }
        }.toList()
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    @Test
    fun `flow fold calculates sum`() = runTest {
        val sum = flow {
            for (i in 1..5) {
                emit(i)
            }
        }.fold(0) { acc, value -> acc + value }
        assertEquals(15, sum)
    }
}
