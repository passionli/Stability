package com.example.nativelib

import org.junit.Test
import org.junit.Assert.*

class NativeFunctionPatcherTest {

    @Test
    fun testPatchSuccessConstant() {
        assertEquals(0, NativeLib.PATCH_SUCCESS)
    }

    @Test
    fun testErrorConstants() {
        assertEquals(-1, NativeLib.PATCH_ERROR_NOT_FOUND)
        assertEquals(-2, NativeLib.PATCH_ERROR_PROTECT)
        assertEquals(-3, NativeLib.PATCH_ERROR_ALREADY_PATCHED)
        assertEquals(-4, NativeLib.PATCH_ERROR_NOT_PATCHED)
        assertEquals(-5, NativeLib.PATCH_ERROR_INVALID_ARG)
    }

    @Test
    fun testPatchedCountInitiallyZero() {
        val nativeLib = NativeLib()
        assertEquals(0, nativeLib.getPatchedCount())
    }

    @Test
    fun testIsFunctionPatchedWithZeroAddress() {
        val nativeLib = NativeLib()
        assertFalse(nativeLib.isFunctionPatched(0))
    }

    @Test
    fun testFindSymbolWithInvalidArguments() {
        val nativeLib = NativeLib()
        assertEquals(0, nativeLib.findSymbol("", ""))
        assertEquals(0, nativeLib.findSymbol("libnonexistent.so", "nonexistent"))
    }

    @Test
    fun testPatchWithInvalidAddress() {
        val nativeLib = NativeLib()
        val result = nativeLib.patchFunctionByAddr(0, 0)
        assertEquals(NativeLib.PATCH_ERROR_INVALID_ARG, result)
    }

    @Test
    fun testRestoreWithInvalidAddress() {
        val nativeLib = NativeLib()
        val result = nativeLib.restoreFunctionByAddr(0)
        assertEquals(NativeLib.PATCH_ERROR_INVALID_ARG, result)
    }

    @Test
    fun testRestoreNonPatchedFunction() {
        val nativeLib = NativeLib()
        val result = nativeLib.restoreFunctionByAddr(0x12345678)
        assertEquals(NativeLib.PATCH_ERROR_NOT_PATCHED, result)
    }

    @Test
    fun testClearAllPatchesIsSafe() {
        val nativeLib = NativeLib()
        assertDoesNotThrow {
            nativeLib.clearAllPatches()
        }
    }
}