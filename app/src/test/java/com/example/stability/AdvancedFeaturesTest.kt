package com.example.stability

import org.junit.Assert.*
import org.junit.Test

fun String.reverse(): String = this.reversed()
fun String.toTitleCase(): String = split(" ").joinToString(" ") { it.capitalize() }

class AdvancedFeaturesTest {

    private inline fun execute(block: () -> Unit) {
        block()
    }

    private inline fun <reified T> getTypeName(): String = T::class.simpleName ?: "Unknown"

    @Test
    fun `safe call operator returns null for null receiver`() {
        val nullableString: String? = null
        val length = nullableString?.length
        assertNull(length)
    }

    @Test
    fun `elvis operator returns default value for null`() {
        val nullableString: String? = null
        val result = nullableString ?: "Default"
        assertEquals("Default", result)
    }

    @Test
    fun `not null assertion throws for null`() {
        val nullableString: String? = null
        assertThrows(NullPointerException::class.java) {
            nullableString!!.length
        }
    }

    @Test
    fun `extension function works`() {
        val text = "Hello"
        assertEquals("olleH", text.reverse())
    }

    @Test
    fun `toTitleCase extension function works`() {
        val text = "hello world"
        assertEquals("Hello World", text.toTitleCase())
    }

    @Test
    fun `lambda expression as function parameter`() {
        val result = calculate(5, 3) { a, b -> a + b }
        assertEquals(8, result)
    }

    @Test
    fun `filter collection with lambda`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        val evenNumbers = numbers.filter { it % 2 == 0 }
        assertEquals(listOf(2, 4), evenNumbers)
    }

    @Test
    fun `map collection with lambda`() {
        val numbers = listOf(1, 2, 3)
        val doubled = numbers.map { it * 2 }
        assertEquals(listOf(2, 4, 6), doubled)
    }

    @Test
    fun `reduce collection with lambda`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        val sum = numbers.reduce { acc, i -> acc + i }
        assertEquals(15, sum)
    }

    @Test
    fun `infix function works`() {
        infix fun Int.add(other: Int): Int = this + other
        val result = 5 add 10
        assertEquals(15, result)
    }

    @Test
    fun `operator function works`() {
        data class Point(val x: Int, val y: Int) {
            operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)
        }
        val p1 = Point(1, 2)
        val p2 = Point(3, 4)
        val p3 = p1 + p2
        assertEquals(Point(4, 6), p3)
    }

    @Test
    fun `tailrec function works`() {
        tailrec fun factorial(n: Int, acc: Int = 1): Int =
            if (n <= 1) acc else factorial(n - 1, n * acc)
        assertEquals(120, factorial(5))
    }

    @Test
    fun `inline function works`() {
        var executed = false
        execute {
            executed = true
        }
        assertTrue(executed)
    }

    @Test
    fun `reified generic works`() {
        assertEquals("String", getTypeName<String>())
        assertEquals("Int", getTypeName<Int>())
    }

    private fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int = operation(a, b)
}
