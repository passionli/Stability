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

    private inline fun inlineWithNoinline(inlineBlock: () -> Unit, noinline noinlineBlock: () -> Unit): () -> Unit {
        inlineBlock()
        return noinlineBlock
    }

    private inline fun inlineWithCrossinline(crossinline block: () -> Unit) {
        Runnable { block() }.run()
    }

    private fun calculate2(a: Int, b: Int, operation: (Int, Int) -> Int): (Int) -> Int {
        return { c -> operation(a, b) + c }
    }

    @Test
    fun `noinline keyword works`() {
        var inlineExecuted = false
        var noinlineExecuted = false
        val returnedBlock = inlineWithNoinline(
            { inlineExecuted = true },
            { noinlineExecuted = true }
        )
        assertTrue(inlineExecuted)
        assertFalse(noinlineExecuted)
        returnedBlock()
        assertTrue(noinlineExecuted)
    }

    @Test
    fun `crossinline keyword works`() {
        var executed = false
        inlineWithCrossinline {
            executed = true
        }
        assertTrue(executed)
    }

    @Test
    fun `higher order function returns function`() {
        val add5 = calculate2(2, 3) { a, b -> a + b }
        assertEquals(10, add5(5))
    }

    @Test
    fun `any collection operator works`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        assertTrue(numbers.any { it % 2 == 0 })
        assertFalse(numbers.any { it > 10 })
    }

    @Test
    fun `all collection operator works`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        assertTrue(numbers.all { it > 0 })
        assertFalse(numbers.all { it % 2 == 0 })
    }

    @Test
    fun `none collection operator works`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        assertTrue(numbers.none { it < 0 })
        assertFalse(numbers.none { it % 2 == 0 })
    }

    @Test
    fun `find collection operator works`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        assertEquals(4, numbers.find { it > 3 })
        assertNull(numbers.find { it > 10 })
    }

    @Test
    fun `groupBy collection operator works`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        val grouped = numbers.groupBy { it % 2 == 0 }
        assertEquals(listOf(1, 3, 5), grouped[false])
        assertEquals(listOf(2, 4), grouped[true])
    }

    @Test
    fun `mutableList add and remove works`() {
        val list = mutableListOf(1, 2, 3)
        list.add(4)
        assertEquals(listOf(1, 2, 3, 4), list)
        list.remove(2)
        assertEquals(listOf(1, 3, 4), list)
    }

    @Test
    fun `mutableMap put and remove works`() {
        val map = mutableMapOf("name" to "Kotlin", "version" to "1.9")
        map["author"] = "JetBrains"
        assertEquals("JetBrains", map["author"])
        map.remove("version")
        assertFalse(map.containsKey("version"))
    }

    @Test
    fun `setOf removes duplicates`() {
        val set = setOf(1, 2, 2, 3, 3, 3)
        assertEquals(3, set.size)
        assertTrue(set.containsAll(listOf(1, 2, 3)))
    }

    @Test
    fun `mutableSetOf can be modified`() {
        val set = mutableSetOf(1, 2, 3)
        set.add(4)
        assertTrue(set.contains(4))
        set.remove(2)
        assertFalse(set.contains(2))
    }

    @Test
    fun `hashMapOf works`() {
        val map = hashMapOf("a" to 1, "b" to 2)
        assertEquals(1, map["a"])
        assertEquals(2, map["b"])
    }

    @Test
    fun `linkedMapOf preserves order`() {
        val map = linkedMapOf("first" to 1, "second" to 2, "third" to 3)
        val keys = map.keys.toList()
        assertEquals("first", keys[0])
        assertEquals("second", keys[1])
        assertEquals("third", keys[2])
    }

    @Test
    fun `sortedMapOf is sorted`() {
        val map = sortedMapOf("c" to 3, "a" to 1, "b" to 2)
        val keys = map.keys.toList()
        assertEquals("a", keys[0])
        assertEquals("b", keys[1])
        assertEquals("c", keys[2])
    }
}
