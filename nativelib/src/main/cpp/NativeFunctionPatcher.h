#ifndef NATIVE_FUNCTION_PATCHER_H
#define NATIVE_FUNCTION_PATCHER_H

#include <cstdint>
#include <string>
#include <mutex>
#include <unordered_map>
#include <vector>

#define PATCH_SUCCESS 0
#define PATCH_ERROR_NOT_FOUND -1
#define PATCH_ERROR_PROTECT -2
#define PATCH_ERROR_ALREADY_PATCHED -3
#define PATCH_ERROR_NOT_PATCHED -4
#define PATCH_ERROR_INVALID_ARG -5

struct PatchInfo {
    void* function_addr;
    uint8_t original_bytes[16];
    size_t original_size;
    uint64_t return_value;
};

class NativeFunctionPatcher {
public:
    static NativeFunctionPatcher& getInstance();
    
    int findSymbol(const char* so_name, const char* symbol_name, void** out_addr);
    
    int patchFunction(void* func_addr, uint64_t return_value);
    
    int patchFunctionByName(const char* so_name, const char* symbol_name, uint64_t return_value);
    
    int restoreFunction(void* func_addr);
    
    int restoreFunctionByName(const char* so_name, const char* symbol_name);
    
    bool isPatched(void* func_addr);
    
    size_t getPatchedCount();
    
    void clearAllPatches();

private:
    NativeFunctionPatcher();
    ~NativeFunctionPatcher();
    
    NativeFunctionPatcher(const NativeFunctionPatcher&) = delete;
    NativeFunctionPatcher& operator=(const NativeFunctionPatcher&) = delete;
    
    int writeReturnInstruction(void* addr, uint64_t return_value, uint8_t* original_bytes, size_t* out_size);
    
    int protectMemory(void* addr, size_t size, int prot);
    
    std::mutex mutex_;
    std::unordered_map<void*, PatchInfo> patches_;
};

#endif // NATIVE_FUNCTION_PATCHER_H