package com.example.stability

import org.junit.Assert.*
import org.junit.Test

class ClassesAndObjectsTest {

    open class Person(val name: String, val age: Int) {
        open fun introduce(): String = "My name is $name"
    }

    class Student(name: String, age: Int, val studentId: String) : Person(name, age) {
        override fun introduce(): String = "${super.introduce()}, ID: $studentId"
    }

    object Singleton {
        fun doSomething(): String = "Singleton did something"
    }

    enum class Direction {
        NORTH, SOUTH, EAST, WEST
    }

    class Company {
        companion object {
            const val COMPANY_NAME = "Tech Corp"
            fun create(): Company = Company()
        }
    }

    class OuterClass {
        private val outerProperty = "Outer Value"
        inner class InnerClass {
            fun getOuterProperty(): String = outerProperty
        }
    }

    class LazyExample {
        val expensiveProperty by lazy {
            "Initialized"
        }
    }

    @Test
    fun `class can be instantiated`() {
        val person = Person("Alice", 25)
        assertEquals("Alice", person.name)
        assertEquals(25, person.age)
    }

    @Test
    fun `subclass overrides method`() {
        val student = Student("Bob", 20, "S123")
        assertEquals("My name is Bob, ID: S123", student.introduce())
    }

    @Test
    fun `singleton object is single instance`() {
        val result = Singleton.doSomething()
        assertEquals("Singleton did something", result)
    }

    @Test
    fun `enum class has correct values`() {
        assertEquals(Direction.NORTH, Direction.valueOf("NORTH"))
        assertEquals(4, Direction.values().size)
    }

    @Test
    fun `companion object has static members`() {
        assertEquals("Tech Corp", Company.COMPANY_NAME)
        assertNotNull(Company.create())
    }

    @Test
    fun `inner class can access outer properties`() {
        val outer = OuterClass()
        val inner = outer.InnerClass()
        assertEquals("Outer Value", inner.getOuterProperty())
    }

    @Test
    fun `by lazy initializes only once`() {
        val lazyExample = LazyExample()
        val value1 = lazyExample.expensiveProperty
        val value2 = lazyExample.expensiveProperty
        assertEquals("Initialized", value1)
        assertSame(value1, value2)
    }
}
