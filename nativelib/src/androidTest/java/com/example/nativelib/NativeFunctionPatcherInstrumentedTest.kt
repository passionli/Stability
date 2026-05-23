package com.example.nativelib

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class NativeFunctionPatcherInstrumentedTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.nativelib.test", appContext.packageName)
    }

    @Test
    fun testFindSymbolInLibc() {
        val nativeLib = NativeLib()
        val addr = nativeLib.findSymbol("libc.so", "malloc")
        assertNotEquals("malloc symbol should be found", 0L, addr)
    }

    @Test
    fun testFindSymbolInLibm() {
        val nativeLib = NativeLib()
        val addr = nativeLib.findSymbol("libm.so", "sin")
        assertNotEquals("sin symbol should be found", 0L, addr)
    }

    @Test
    fun testFindSymbolNotFound() {
        val nativeLib = NativeLib()
        val addr = nativeLib.findSymbol("libc.so", "nonexistent_symbol_xyz123")
        assertEquals("Nonexistent symbol should return 0", 0L, addr)
    }

    @Test
    fun testPatchedCountStartsAtZero() {
        val nativeLib = NativeLib()
        assertEquals("Patched count should start at 0", 0, nativeLib.getPatchedCount())
    }

    @Test
    fun testIsFunctionPatchedInitiallyFalse() {
        val nativeLib = NativeLib()
        val addr = nativeLib.findSymbol("libc.so", "malloc")
        if (addr != 0L) {
            assertFalse("Function should not be patched initially", nativeLib.isFunctionPatched(addr))
        }
    }

    @Test
    fun testClearAllPatchesIsSafe() {
        val nativeLib = NativeLib()
        assertDoesNotThrow {
            nativeLib.clearAllPatches()
        }
    }

    @Test
    fun testPatchAndRestoreRoundTrip() {
        val nativeLib = NativeLib()
        val addr = nativeLib.findSymbol("libc.so", "malloc")
        
        if (addr != 0L) {
            val patchResult = nativeLib.patchFunctionByAddr(addr, 0)
            assertEquals("Patch should succeed", NativeLib.PATCH_SUCCESS, patchResult)
            
            assertTrue("Function should be marked as patched", nativeLib.isFunctionPatched(addr))
            assertEquals("Patched count should be 1", 1, nativeLib.getPatchedCount())
            
            val restoreResult = nativeLib.restoreFunctionByAddr(addr)
            assertEquals("Restore should succeed", NativeLib.PATCH_SUCCESS, restoreResult)
            
            assertFalse("Function should no longer be patched", nativeLib.isFunctionPatched(addr))
            assertEquals("Patched count should be 0", 0, nativeLib.getPatchedCount())
        }
    }

    @Test
    fun testPatchAlreadyPatchedFunction() {
        val nativeLib = NativeLib()
        val addr = nativeLib.findSymbol("libc.so", "malloc")
        
        if (addr != 0L) {
            val firstPatch = nativeLib.patchFunctionByAddr(addr, 0)
            if (firstPatch == NativeLib.PATCH_SUCCESS) {
                val secondPatch = nativeLib.patchFunctionByAddr(addr, 1)
                assertEquals("Patching already patched function should fail", 
                    NativeLib.PATCH_ERROR_ALREADY_PATCHED, secondPatch)
                
                nativeLib.restoreFunctionByAddr(addr)
            }
        }
    }

    @Test
    fun testRestoreNonPatchedFunction() {
        val nativeLib = NativeLib()
        val addr = nativeLib.findSymbol("libc.so", "malloc")
        
        if (addr != 0L) {
            val result = nativeLib.restoreFunctionByAddr(addr)
            assertEquals("Restoring non-patched function should fail", 
                NativeLib.PATCH_ERROR_NOT_PATCHED, result)
        }
    }
}