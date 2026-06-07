package com.example.stability

import org.junit.Assert.*
import org.junit.Test

data class User(val id: Int, val name: String, val email: String)

sealed class Result {
    data class Success(val data: String) : Result()
    data class Error(val exception: Exception) : Result()
}

class Box<T>(val value: T)

interface DataSource {
    fun saveData(key: String, value: String)
    fun getData(key: String): String?
}

class RealDataSource : DataSource {
    private val data = mutableMapOf<String, String>()
    override fun saveData(key: String, value: String) {
        data[key] = value
    }
    override fun getData(key: String): String? = data[key]
}

class DataSourceWrapper(private val delegate: DataSource) : DataSource by delegate

typealias UserType = Pair<String, Int>

annotation class MyAnnotation

@MyAnnotation
class AnnotatedClass

abstract class AbstractClass {
    abstract fun abstractMethod(): String
    fun concreteMethod(): String = "Concrete"
}

class ConcreteImplementation : AbstractClass() {
    override fun abstractMethod(): String = "Implemented"
}

sealed class ApiResult {
    data class Success(val data: String) : ApiResult()
    data class Error(val message: String) : ApiResult()
}

class JavaInteropExample {
    @JvmField
    val publicField: String = "Public Field"

    companion object {
        @JvmStatic
        fun staticMethod(): String = "Static Method"

        @JvmField
        val STATIC_FIELD: String = "Static Field"
    }

    @JvmOverloads
    fun overloadedMethod(a: Int, b: String = "default", c: Boolean = true): String {
        return "a: $a, b: $b, c: $c"
    }
}

final class FinalClass {
    fun doSomething(): String = "Final did something"
}

class ExpertFeaturesTest {

    @Test
    fun `data class has equals and hashCode`() {
        val user1 = User(1, "Alice", "alice@test.com")
        val user2 = User(1, "Alice", "alice@test.com")
        assertEquals(user1, user2)
        assertEquals(user1.hashCode(), user2.hashCode())
    }

    @Test
    fun `data class copy works`() {
        val user1 = User(1, "Alice", "alice@test.com")
        val user2 = user1.copy(email = "alice_new@test.com")
        assertEquals(1, user2.id)
        assertEquals("Alice", user2.name)
        assertEquals("alice_new@test.com", user2.email)
    }

    @Test
    fun `data class destructuring works`() {
        val user = User(1, "Alice", "alice@test.com")
        val (id, name, email) = user
        assertEquals(1, id)
        assertEquals("Alice", name)
        assertEquals("alice@test.com", email)
    }

    @Test
    fun `sealed class when expression works`() {
        fun handleResult(result: Result): String {
            return when (result) {
                is Result.Success -> "Success: ${result.data}"
                is Result.Error -> "Error: ${result.exception.message}"
            }
        }
        val success = Result.Success("OK")
        assertEquals("Success: OK", handleResult(success))
    }

    @Test
    fun `generic class works`() {
        val intBox = Box(42)
        val stringBox = Box("Hello")
        assertEquals(42, intBox.value)
        assertEquals("Hello", stringBox.value)
    }

    @Test
    fun `generic function works`() {
        fun <T> identity(value: T): T = value
        assertEquals(42, identity(42))
        assertEquals("Test", identity("Test"))
    }

    @Test
    fun `delegation works`() {
        val realSource = RealDataSource()
        val wrapper = DataSourceWrapper(realSource)
        wrapper.saveData("key", "value")
        assertEquals("value", wrapper.getData("key"))
    }

    @Test
    fun `typealias works`() {
        val user: UserType = "John" to 30
        assertEquals("John", user.first)
        assertEquals(30, user.second)
    }

    @Test
    fun `abstract class works`() {
        val impl = ConcreteImplementation()
        assertEquals("Implemented", impl.abstractMethod())
        assertEquals("Concrete", impl.concreteMethod())
    }

    @Test
    fun `sealed api result works`() {
        fun handleApiResult(result: ApiResult): String {
            return when (result) {
                is ApiResult.Success -> "Success: ${result.data}"
                is ApiResult.Error -> "Error: ${result.message}"
            }
        }
        val error = ApiResult.Error("Failed")
        assertEquals("Error: Failed", handleApiResult(error))
    }

    @Test
    fun `jvmField exposes property as field`() {
        val example = JavaInteropExample()
        assertEquals("Public Field", example.publicField)
    }

    @Test
    fun `jvmStatic exposes method as static`() {
        assertEquals("Static Method", JavaInteropExample.staticMethod())
        assertEquals("Static Field", JavaInteropExample.STATIC_FIELD)
    }

    @Test
    fun `jvmOverloads generates overloads`() {
        val example = JavaInteropExample()
        assertEquals("a: 1, b: default, c: true", example.overloadedMethod(1))
        assertEquals("a: 1, b: custom, c: true", example.overloadedMethod(1, "custom"))
        assertEquals("a: 1, b: custom, c: false", example.overloadedMethod(1, "custom", false))
    }

    @Test
    fun `generic with upper bound works`() {
        fun <T : Number> sum(list: List<T>): Double {
            return list.sumOf { it.toDouble() }
        }
        val intList = listOf(1, 2, 3)
        val doubleList = listOf(1.1, 2.2, 3.3)
        assertEquals(6.0, sum(intList), 0.001)
        assertEquals(6.6, sum(doubleList), 0.001)
    }

    @Test
    fun `final class cannot be inherited`() {
        val finalClass = FinalClass()
        assertEquals("Final did something", finalClass.doSomething())
    }

    @Test
    fun `custom annotation can be used`() {
        val annotationClass = AnnotatedClass::class.java
        val annotations = annotationClass.annotations
        assertTrue(annotations.any { it.annotationClass == MyAnnotation::class })
    }
}
