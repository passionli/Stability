package com.example.nativelib

import com.bytedance.shadowhook.ShadowHook

class NativeLib {

    /**
     * A native method that is implemented by the 'nativelib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(proxyThreadArtMethod: Long, threadArtMethod: Long): String

    /**
     * A native method that demonstrates pthread key leak by creating keys without deleting them.
     */
    external fun createPthreadKeyLeak(): Int

    /**
     * Patch a native function by name to return a specific value.
     *
     * @param soName The name of the SO library containing the function
     * @param symbolName The name of the function symbol to patch
     * @param returnValue The value the function should return
     * @return 0 on success, negative error code on failure
     */
    external fun patchFunctionByName(soName: String, symbolName: String, returnValue: Long): Int

    /**
     * Restore a native function by name to its original implementation.
     *
     * @param soName The name of the SO library containing the function
     * @param symbolName The name of the function symbol to restore
     * @return 0 on success, negative error code on failure
     */
    external fun restoreFunctionByName(soName: String, symbolName: String): Int

    /**
     * Find the address of a symbol in a SO library.
     *
     * @param soName The name of the SO library
     * @param symbolName The name of the symbol to find
     * @return The address of the symbol, or 0 if not found
     */
    external fun findSymbol(soName: String, symbolName: String): Long

    /**
     * Patch a native function by address to return a specific value.
     *
     * @param funcAddr The address of the function to patch
     * @param returnValue The value the function should return
     * @return 0 on success, negative error code on failure
     */
    external fun patchFunctionByAddr(funcAddr: Long, returnValue: Long): Int

    /**
     * Restore a native function by address to its original implementation.
     *
     * @param funcAddr The address of the function to restore
     * @return 0 on success, negative error code on failure
     */
    external fun restoreFunctionByAddr(funcAddr: Long): Int

    /**
     * Check if a function is currently patched.
     *
     * @param funcAddr The address of the function to check
     * @return true if the function is patched, false otherwise
     */
    external fun isFunctionPatched(funcAddr: Long): Boolean

    /**
     * Get the number of currently patched functions.
     *
     * @return The count of patched functions
     */
    external fun getPatchedCount(): Int

    /**
     * Clear all patches and restore all functions to their original implementations.
     */
    external fun clearAllPatches()

    companion object {
        var proxyMethod: Long = 0
        var originMethod: Long = 0

        const val PATCH_SUCCESS = 0
        const val PATCH_ERROR_NOT_FOUND = -1
        const val PATCH_ERROR_PROTECT = -2
        const val PATCH_ERROR_ALREADY_PATCHED = -3
        const val PATCH_ERROR_NOT_PATCHED = -4
        const val PATCH_ERROR_INVALID_ARG = -5

        init {
            ShadowHook.init(ShadowHook.ConfigBuilder()
                .setMode(ShadowHook.Mode.SHARED)
                .build())
            System.loadLibrary("nativelib")
        }

        external fun setTLSMethodEnabled(enabled: Boolean, method: Long): Int

        external fun deoptimize(method: Long): Int
    }
}