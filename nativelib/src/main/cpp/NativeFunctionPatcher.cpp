#include "NativeFunctionPatcher.h"
#include <android/log.h>
#include <dlfcn.h>
#include <sys/mman.h>
#include <cstring>
#include <xdl.h>

#define LOG_TAG "NativeFunctionPatcher"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

NativeFunctionPatcher::NativeFunctionPatcher() {
    LOGD("NativeFunctionPatcher initialized");
}

NativeFunctionPatcher::~NativeFunctionPatcher() {
    clearAllPatches();
}

NativeFunctionPatcher& NativeFunctionPatcher::getInstance() {
    static NativeFunctionPatcher instance;
    return instance;
}

int NativeFunctionPatcher::protectMemory(void* addr, size_t size, int prot) {
    uintptr_t page_start = reinterpret_cast<uintptr_t>(addr) & ~(4095);
    size_t page_size = ((size + 4095) / 4096) * 4096;
    
    if (mprotect(reinterpret_cast<void*>(page_start), page_size, prot) != 0) {
        LOGE("mprotect failed: %s", strerror(errno));
        return -1;
    }
    return 0;
}

int NativeFunctionPatcher::findSymbol(const char* so_name, const char* symbol_name, void** out_addr) {
    if (!so_name || !symbol_name || !out_addr) {
        LOGE("Invalid arguments");
        return PATCH_ERROR_INVALID_ARG;
    }

    void* handle = xdl_open(so_name, XDL_DEFAULT);
    if (!handle) {
        LOGE("Failed to open SO: %s", so_name);
        return PATCH_ERROR_NOT_FOUND;
    }

    size_t symbol_size = 0;
    void* sym = xdl_sym(handle, symbol_name, &symbol_size);
    if (!sym) {
        sym = xdl_dsym(handle, symbol_name, &symbol_size);
    }

    xdl_close(handle);

    if (!sym) {
        LOGE("Symbol not found: %s", symbol_name);
        return PATCH_ERROR_NOT_FOUND;
    }

    *out_addr = sym;
    LOGD("Found symbol %s at %p", symbol_name, *out_addr);
    return PATCH_SUCCESS;
}

int NativeFunctionPatcher::writeReturnInstruction(void* addr, uint64_t return_value, uint8_t* original_bytes, size_t* out_size) {
    uint8_t* code = reinterpret_cast<uint8_t*>(addr);
    
    memcpy(original_bytes, code, 16);
    
    if (return_value == 0) {
        *reinterpret_cast<uint32_t*>(code) = 0xD65F03C0;
        *out_size = 4;
    } else {
        if (return_value <= 0xFFFF) {
            uint32_t mov_inst = 0x52800000 | (return_value << 5);
            *reinterpret_cast<uint32_t*>(code) = mov_inst;
            *reinterpret_cast<uint32_t*>(code + 4) = 0xD65F03C0;
            *out_size = 8;
        } else if (return_value <= 0xFFFFFFFF) {
            *reinterpret_cast<uint32_t*>(code) = 0x58000000 | ((return_value >> 32) & 0xFFFF);
            *reinterpret_cast<uint32_t*>(code + 4) = 0xD2800000 | (return_value & 0xFFFF);
            *reinterpret_cast<uint32_t*>(code + 8) = 0xD65F03C0;
            *out_size = 12;
        } else {
            *reinterpret_cast<uint32_t*>(code) = 0x58000000 | ((return_value >> 32) & 0xFFFF);
            *reinterpret_cast<uint32_t*>(code + 4) = 0xD2800000 | ((return_value >> 16) & 0xFFFF);
            *reinterpret_cast<uint32_t*>(code + 8) = 0xF2A00000 | (return_value & 0xFFFF);
            *reinterpret_cast<uint32_t*>(code + 12) = 0xD65F03C0;
            *out_size = 16;
        }
    }
    
    return PATCH_SUCCESS;
}

int NativeFunctionPatcher::patchFunction(void* func_addr, uint64_t return_value) {
    if (!func_addr) {
        LOGE("Invalid function address");
        return PATCH_ERROR_INVALID_ARG;
    }

    std::lock_guard<std::mutex> lock(mutex_);
    
    if (patches_.find(func_addr) != patches_.end()) {
        LOGE("Function already patched: %p", func_addr);
        return PATCH_ERROR_ALREADY_PATCHED;
    }

    if (protectMemory(func_addr, 16, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
        return PATCH_ERROR_PROTECT;
    }

    PatchInfo info;
    info.function_addr = func_addr;
    info.return_value = return_value;

    int result = writeReturnInstruction(func_addr, return_value, info.original_bytes, &info.original_size);
    if (result != PATCH_SUCCESS) {
        protectMemory(func_addr, 16, PROT_READ | PROT_EXEC);
        return result;
    }

    patches_[func_addr] = info;
    
    if (protectMemory(func_addr, 16, PROT_READ | PROT_EXEC) != 0) {
        LOGE("Failed to restore memory protection after patch");
    }

    LOGD("Successfully patched function at %p to return 0x%lx", func_addr, return_value);
    return PATCH_SUCCESS;
}

int NativeFunctionPatcher::patchFunctionByName(const char* so_name, const char* symbol_name, uint64_t return_value) {
    void* func_addr = nullptr;
    int result = findSymbol(so_name, symbol_name, &func_addr);
    if (result != PATCH_SUCCESS) {
        return result;
    }
    return patchFunction(func_addr, return_value);
}

int NativeFunctionPatcher::restoreFunction(void* func_addr) {
    if (!func_addr) {
        LOGE("Invalid function address");
        return PATCH_ERROR_INVALID_ARG;
    }

    std::lock_guard<std::mutex> lock(mutex_);
    
    auto it = patches_.find(func_addr);
    if (it == patches_.end()) {
        LOGE("Function not patched: %p", func_addr);
        return PATCH_ERROR_NOT_PATCHED;
    }

    PatchInfo& info = it->second;

    if (protectMemory(func_addr, info.original_size, PROT_READ | PROT_WRITE | PROT_EXEC) != 0) {
        return PATCH_ERROR_PROTECT;
    }

    memcpy(func_addr, info.original_bytes, info.original_size);
    
    if (protectMemory(func_addr, info.original_size, PROT_READ | PROT_EXEC) != 0) {
        LOGE("Failed to restore memory protection after restore");
    }

    patches_.erase(it);
    LOGD("Successfully restored function at %p", func_addr);
    return PATCH_SUCCESS;
}

int NativeFunctionPatcher::restoreFunctionByName(const char* so_name, const char* symbol_name) {
    void* func_addr = nullptr;
    int result = findSymbol(so_name, symbol_name, &func_addr);
    if (result != PATCH_SUCCESS) {
        return result;
    }
    return restoreFunction(func_addr);
}

bool NativeFunctionPatcher::isPatched(void* func_addr) {
    std::lock_guard<std::mutex> lock(mutex_);
    return patches_.find(func_addr) != patches_.end();
}

size_t NativeFunctionPatcher::getPatchedCount() {
    std::lock_guard<std::mutex> lock(mutex_);
    return patches_.size();
}

void NativeFunctionPatcher::clearAllPatches() {
    std::lock_guard<std::mutex> lock(mutex_);
    
    for (auto& pair : patches_) {
        PatchInfo& info = pair.second;
        if (protectMemory(info.function_addr, info.original_size, PROT_READ | PROT_WRITE | PROT_EXEC) == 0) {
            memcpy(info.function_addr, info.original_bytes, info.original_size);
            protectMemory(info.function_addr, info.original_size, PROT_READ | PROT_EXEC);
        }
    }
    
    patches_.clear();
    LOGD("All patches cleared");
}