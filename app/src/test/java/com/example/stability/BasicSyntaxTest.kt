package com.example.stability

import org.junit.Assert.*
import org.junit.Test

class BasicSyntaxTest {

    @Test
    fun `val variable is immutable`() {
        val a = 10
        assertEquals(10, a)
    }

    @Test
    fun `var variable is mutable`() {
        var b = 20
        b = 30
        assertEquals(30, b)
    }

    @Test
    fun `if expression returns value`() {
        val age = 18
        val result = if (age >= 18) "Adult" else "Minor"
        assertEquals("Adult", result)
    }

    @Test
    fun `for loop iterates through range`() {
        var sum = 0
        for (i in 1..5) {
            sum += i
        }
        assertEquals(15, sum)
    }

    @Test
    fun `while loop executes while condition is true`() {
        var count = 0
        var sum = 0
        while (count < 5) {
            sum += count
            count++
        }
        assertEquals(10, sum)
    }

    @Test
    fun `do while loop executes at least once`() {
        var count = 5
        var sum = 0
        do {
            sum += count
            count++
        } while (count < 5)
        assertEquals(5, sum)
    }

    @Test
    fun `try catch handles exception`() {
        var caught = false
        try {
            val result = 10 / 0
        } catch (e: ArithmeticException) {
            caught = true
        }
        assertTrue(caught)
    }

    @Test
    fun `is operator checks type`() {
        val obj: Any = "Hello"
        assertTrue(obj is String)
    }

    @Test
    fun `as operator casts type`() {
        val obj: Any = "Test"
        val str = obj as String
        assertEquals("Test", str)
    }

    @Test
    fun `lateinit variable can be initialized later`() {
        class LateinitTest {
            lateinit var name: String
        }
        val test = LateinitTest()
        test.name = "Kotlin"
        assertEquals("Kotlin", test.name)
    }

    @Test
    fun `string template works`() {
        val name = "World"
        val greeting = "Hello, $name!"
        assertEquals("Hello, World!", greeting)
    }
}
