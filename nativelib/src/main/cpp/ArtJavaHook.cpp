// ArtJavaHook.cpp
#include "ArtJavaHook.h"

#include <dlfcn.h>
#include <android/log.h>
#include <string>
#include "shadowhook.h"
#include "xdl.h"

#define LOG_TAG "ArtJavaHook"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// art::mirror::Class::AllocObject(art::Thread*)
// 返回类型: art::mirror::Object*
// 参数: art::mirror::Class* this, art::Thread* thread
// 使用 void* 来简化类型定义
typedef void* (*art_Class_AllocObject_func_type_t)(void*, void*);

// art::mirror::Class::PrettyClass()
// 返回类型: std::string
// 参数: art::mirror::Class* this
typedef std::string (*art_Class_PrettyClass_func_type_t)(void *);

// art::Thread::DumpFromGdb() const
// 返回类型: void
// 参数: art::Thread* this
typedef void (*art_Thread_DumpFromGdb_func_type_t)(void*);

// art_quick_to_interpreter_bridge
// 函数类型定义
typedef void (*art_quick_to_interpreter_bridge_func_type_t)(void*);

// art_quick_resolution_trampoline
// 函数类型定义
typedef void (*art_quick_resolution_trampoline_func_type_t)(void);

// art::ArtMethod::PrettyMethod(ArtMethod*, bool)
// 返回类型: std::string
// 参数: art::ArtMethod* this, art::ArtMethod* compare_to, bool with_signature
typedef std::string (*art_ArtMethod_PrettyMethod_func_type_t)(void*, void*, bool);

// art::mirror::Class::SetAccessFlags 函数类型
// 参数: art::mirror::Class* this, uint32_t access_flags
typedef void (*art_Class_SetAccessFlags_func_type_t)(void*, uint32_t);

// art::Thread::DecodeJObject 函数类型
// 参数: art::Thread* this, jobject obj
// 返回类型: void*
typedef void* (*art_Thread_DecodeJObject_func_type_t)(void*, jobject);

// art::Thread::CurrentFromGdb 函数类型
// 无参数
// 返回类型: art::Thread*
typedef void* (*art_Thread_CurrentFromGdb_func_type_t)(void);

// art::gc::Heap::AddFinalizerReference 函数类型
// 参数: art::gc::Heap* this, art::Thread* thread, art::ObjPtr<art::mirror::Object> obj
// 无返回值
typedef void (*art_gc_Heap_AddFinalizerReference_func_type_t)(void*, void*, void*);

// art::mirror::Object::PrettyTypeOf 函数类型
// 参数: art::ObjPtr<art::mirror::Object> obj
// 返回类型: std::string
typedef void (*art_Object_PrettyTypeOf_func_type_t)(std::string *, void*);

// 原始函数指针数组
void* orig_functions[20] = {NULL};
static art_Class_PrettyClass_func_type_t art_Class_PrettyClass = NULL;
static art_Thread_DumpFromGdb_func_type_t art_Thread_DumpFromGdb = NULL;
static art_quick_to_interpreter_bridge_func_type_t art_quick_to_interpreter_bridge = NULL;
static art_quick_resolution_trampoline_func_type_t art_quick_resolution_trampoline = NULL;
// art::ArtMethod::PrettyMethod 函数指针
static art_ArtMethod_PrettyMethod_func_type_t art_ArtMethod_PrettyMethod = NULL;
// art::mirror::Class::SetAccessFlags 函数指针
static art_Class_SetAccessFlags_func_type_t art_Class_SetAccessFlags = NULL;
// art::Thread::DecodeJObject 函数指针
static art_Thread_DecodeJObject_func_type_t art_Thread_DecodeJObject = NULL;
// art::Thread::CurrentFromGdb 函数指针
static art_Thread_CurrentFromGdb_func_type_t art_Thread_CurrentFromGdb = NULL;
// art::gc::Heap::AddFinalizerReference 函数指针
static art_gc_Heap_AddFinalizerReference_func_type_t orig_art_gc_Heap_AddFinalizerReference = NULL;
// art::mirror::Object::PrettyTypeOf 函数指针
static art_Object_PrettyTypeOf_func_type_t art_Object_PrettyTypeOf = NULL;
// art runtime 单例指针
static void* art_runtime_instance = NULL;
// DecorView 类的全局引用
static jclass g_decorViewClass = NULL;

// 定义 ArtMethod 结构
// 总大小为 32 字节，最后一个字段是 ptr_sized_fields_ (8 字节)
struct ArtMethod {
    // 前 24 字节的字段
    uint32_t field1; // 4 字节
    uint32_t field2; // 4 字节
    uint32_t field3; // 4 字节
    uint32_t field4; // 4 字节
    uint32_t field5; // 4 字节
    uint32_t field6; // 4 字节
    // 最后 8 字节的字段
    uint64_t ptr_sized_fields_; // 8 字节
};

// 打印 ArtMethod 的入口点信息
void PrintEntryPoint(ArtMethod* art_method);

// 设置 Class 的访问标志
// 参数: class_ptr - Class 指针, access_flags - 新的访问标志
void SetClassAccessFlags(void* class_ptr, uint32_t access_flags) {
    if (art_Class_SetAccessFlags != NULL && class_ptr != NULL) {
        try {
            art_Class_SetAccessFlags(class_ptr, access_flags);
            LOGD("Successfully set access flags to 0x%x for class at %p", access_flags, class_ptr);
        } catch (const std::exception& e) {
            LOGE("Exception when calling art_Class_SetAccessFlags: %s", e.what());
        }
    } else {
        if (art_Class_SetAccessFlags == NULL) {
            LOGE("art_Class_SetAccessFlags is NULL");
        }
        if (class_ptr == NULL) {
            LOGE("class_ptr is NULL");
        }
    }
}

// 解码 JObject
// 参数: thread_ptr - Thread 指针, obj - Java 对象
// 返回: 解码后的对象指针
void* DecodeJObject(void* thread_ptr, jobject obj) {
    if (art_Thread_DecodeJObject != NULL && thread_ptr != NULL) {
        try {
            void* result = art_Thread_DecodeJObject(thread_ptr, obj);
            LOGD("Successfully decoded jobject %p to %p", obj, result);
            return result;
        } catch (const std::exception& e) {
            LOGE("Exception when calling art_Thread_DecodeJObject: %s", e.what());
            return NULL;
        }
    } else {
        if (art_Thread_DecodeJObject == NULL) {
            LOGE("art_Thread_DecodeJObject is NULL");
        }
        if (thread_ptr == NULL) {
            LOGE("thread_ptr is NULL");
        }
        return NULL;
    }
}

// 获取当前 Thread 指针 (从 GDB)
// 返回: 当前 Thread 指针
void* GetCurrentThreadFromGdb() {
    if (art_Thread_CurrentFromGdb != NULL) {
        try {
            void* result = art_Thread_CurrentFromGdb();
            LOGD("Successfully got current thread from GDB: %p", result);
            return result;
        } catch (const std::exception& e) {
            LOGE("Exception when calling art_Thread_CurrentFromGdb: %s", e.what());
            return NULL;
        }
    } else {
        LOGE("art_Thread_CurrentFromGdb is NULL");
        return NULL;
    }
}


// 获取对象的类型信息
// 参数: obj - 对象指针 (art::ObjPtr<art::mirror::Object>)
// 返回: 类型信息字符串
std::string PrettyTypeOf(void* obj) {
    if (art_Object_PrettyTypeOf != NULL && obj != NULL) {
        try {
            std::string result ;
            art_Object_PrettyTypeOf(&result, obj);
            LOGD("Successfully got pretty type of object %p: %s", obj, result.c_str());
            return result;
        } catch (const std::exception& e) {
            LOGE("Exception when calling art_Object_PrettyTypeOf: %s", e.what());
            return "";
        }
    } else {
        if (art_Object_PrettyTypeOf == NULL) {
            LOGE("art_Object_PrettyTypeOf is NULL");
        }
        if (obj == NULL) {
            LOGE("obj is NULL");
        }
        return "";
    }
}


static __attribute__((noinline)) void art_Class_PrettyClass_I(void * class_ptr, void * result, void * func) {
    __asm__ volatile (
        "mov x8, x1\n"
        "br x2\n"
    );
//    art_Class_PrettyClass();
}

// 获取 Class 的类型信息
// 参数: class_ptr - Class 指针
// 返回: 类型信息字符串
static __attribute__((noinline)) std::string PrettyClass(void* class_ptr) {
    if (art_Class_PrettyClass != NULL && class_ptr != NULL) {
        LOGD("start got pretty class of %p out of %p", class_ptr);
        std::string classInfo = art_Class_PrettyClass(class_ptr);
//        PrettyClass(thiz, &classInfo);
//        LOGD("AllocObject thiz Class info: %s", classInfo.c_str());
//        art_Class_PrettyClass_I(class_ptr, out, (void *)(art_Class_PrettyClass));
        LOGD("Successfully got pretty class of %p: %s", class_ptr, classInfo.c_str());
    } else {
        if (art_Class_PrettyClass == NULL) {
            LOGE("art_Class_PrettyClass is NULL");
        }
        if (class_ptr == NULL) {
            LOGE("class_ptr is NULL");
        }
    }
}

void * GetClass(void * obj) {
    return (void *)(*(uint32_t *)(obj));
}

// 代理函数: art::gc::Heap::AddFinalizerReference
static void proxy_art_gc_Heap_AddFinalizerReference(void* heap, void* thread, void** obj) {
    SHADOWHOOK_STACK_SCOPE();
    
    LOGD("proxy_art_gc_Heap_AddFinalizerReference called");
    LOGD("  heap: %p", heap);
    LOGD("  thread: %p", thread);
    LOGD("  obj: %p", obj);
    art_Thread_DumpFromGdb(thread);
    // 注意 obj 是 ObjPtr 类型，二级指针
    void *clazz = GetClass(*obj);
    void *clz = DecodeJObject(thread, g_decorViewClass);

    LOGD("  obj clazz %p, DecorView clz %p", clazz, clz);
    std::string string = art_Class_PrettyClass(clazz);
    if (!string.empty()) {
        LOGD("PrettyClass %s", string.c_str());
    }
    if (clazz == clz) {
        LOGE("===== CRITICAL: DecorView object is being finalized! =====");
        LOGE("  obj address: %p", *obj);
        LOGE("  obj class: %s", string.c_str());
        LOGE("  thread: %p", thread);
        LOGE("  heap: %p", heap);
        LOGE("===== This should not happen! DecorView should not be finalized! =====");
    }


    // 调用原始函数
    if (orig_art_gc_Heap_AddFinalizerReference != NULL) {
        try {
            SHADOWHOOK_CALL_PREV(proxy_art_gc_Heap_AddFinalizerReference, heap, thread, obj);
            LOGD("Successfully called original art_gc_Heap_AddFinalizerReference");
        } catch (const std::exception& e) {
            LOGE("Exception when calling original art_gc_Heap_AddFinalizerReference: %s", e.what());
        }
    } else {
        LOGE("orig_art_gc_Heap_AddFinalizerReference is NULL");
    }
}


// 打印 ArtMethod 的详细信息
void PrintArtMethodInfo(ArtMethod* art_method) {
    if (art_method == NULL) {
        LOGE("PrintArtMethodInfo: art_method is NULL");
        return;
    }
    
    LOGD("ArtMethod address: %p", art_method);
    
    // 调用 art::ArtMethod::PrettyMethod 获取详细信息
    if (art_ArtMethod_PrettyMethod != NULL) {
        try {
            std::string method_info = art_ArtMethod_PrettyMethod(art_method, NULL, true);
            LOGD("ArtMethod info: %s", method_info.c_str());
        } catch (const std::exception& e) {
            LOGE("Exception when calling art_ArtMethod_PrettyMethod: %s", e.what());
        }
    } else {
        LOGE("art_ArtMethod_PrettyMethod is NULL, cannot get detailed info");
    }
    
    // 打印入口点信息
    PrintEntryPoint(art_method);
}

void PrintEntryPoint(ArtMethod* art_method) {
    if (art_method == NULL) {
        LOGE("PrintEntryPoint: art_method is NULL");
        return;
    }
    
    uint64_t entry_point = art_method->ptr_sized_fields_;
    LOGD("ArtMethod entry point: 0x%lx", entry_point);
    
    // 检查是否等于 art_quick_resolution_trampoline
    if (art_quick_resolution_trampoline != NULL && (uint64_t)art_quick_resolution_trampoline == entry_point) {
        LOGD("Execution type: Interpreted (art_quick_resolution_trampoline)");
        return;
    }
    
    // 检查是否等于 art_quick_to_interpreter_bridge
    if (art_quick_to_interpreter_bridge != NULL && (uint64_t)art_quick_to_interpreter_bridge == entry_point) {
        LOGD("Execution type: Interpreted (art_quick_to_interpreter_bridge)");
        return;
    }
    
    // 使用 dladdr 查询内存区域信息
    Dl_info info;
    if (dladdr((void*)entry_point, &info)) {
        LOGD("Entry point memory region:");
        LOGD("  dli_fname: %s", info.dli_fname);
        LOGD("  dli_fbase: %p", info.dli_fbase);
        LOGD("  dli_sname: %s", info.dli_sname);
        LOGD("  dli_saddr: %p", info.dli_saddr);
        
        // 判断执行类型
        if (info.dli_fname != NULL) {
            // 检查是否在 libart.so 内
            if (strstr(info.dli_fname, "libart.so")) {
                LOGD("Execution type: Interpreted (libart.so)");
            }
            // 检查是否在 JIT 内
            else if (strstr(info.dli_fname, "jit") || strstr(info.dli_fname, "JIT")) {
                LOGD("Execution type: JIT compiled");
            }
            // 检查是否在 odex 或 oat 文件内
            else if (strstr(info.dli_fname, ".odex") || strstr(info.dli_fname, ".oat")) {
                LOGD("Execution type: AOT compiled");
            }
            // 其他情况
            else {
                LOGD("Execution type: Unknown");
            }
        } else {
            LOGD("Execution type: Unknown (no filename)");
        }
    } else {
        LOGE("PrintEntryPoint: dladdr failed for entry point 0x%lx", entry_point);
        LOGD("Execution type: Unknown (dladdr failed)");
    }
}

// 通过 jmethodID 获取 ArtMethod 指针
ArtMethod* ToArtMethod(JNIEnv* env, jmethodID methodId) {
    if (env == NULL || methodId == NULL) {
        LOGE("ToArtMethod: invalid parameters");
        return NULL;
    }
    
    // 创建一个临时对象来获取 Class
    jclass temp_class = env->FindClass("java/lang/Object");
    if (temp_class == NULL) {
        LOGE("ToArtMethod: failed to find Object class");
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
        return NULL;
    }
    
    // 通过 Java 反射获取 ArtMethod
    jobject reflected_method = env->ToReflectedMethod(temp_class, methodId, JNI_FALSE);
    if (reflected_method != nullptr) {
        // 获取 artMethod 字段
        jclass method_class = env->GetObjectClass(reflected_method);
        if (method_class != NULL) {
            jfieldID art_method_field = env->GetFieldID(method_class, "artMethod", "J");
            
            if (art_method_field != nullptr) {
                jlong art_method_ptr = env->GetLongField(reflected_method, art_method_field);
                ArtMethod* artMethod = reinterpret_cast<ArtMethod*>(art_method_ptr);
                
                // 清理本地引用
                env->DeleteLocalRef(reflected_method);
                env->DeleteLocalRef(method_class);
                env->DeleteLocalRef(temp_class);
                
                return artMethod;
            } else {
                LOGE("ToArtMethod: failed to find artMethod field");
                if (env->ExceptionCheck()) {
                    env->ExceptionClear();
                }
            }
            
            env->DeleteLocalRef(method_class);
        } else {
            LOGE("ToArtMethod: failed to get reflected method class");
            if (env->ExceptionCheck()) {
                env->ExceptionClear();
            }
        }
        
        env->DeleteLocalRef(reflected_method);
    } else {
        LOGE("ToArtMethod: failed to get reflected method");
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
    }
    
    env->DeleteLocalRef(temp_class);
    return NULL;
}

// 要 hook 的函数名列表
const char* function_names[] = {
    "_ZN3art6mirror5Class11AllocObjectEPNS_6ThreadE",
    "artAllocObjectFromCodeWithChecksDlMalloc",
    "artAllocObjectFromCodeResolvedDlMalloc",
    "artAllocObjectFromCodeInitializedDlMalloc",
    "artAllocObjectFromCodeWithChecksRosAlloc",
    "artAllocObjectFromCodeResolvedRosAlloc",
    "artAllocObjectFromCodeInitializedRosAlloc",
    "artAllocObjectFromCodeWithChecksBumpPointer",
    "artAllocObjectFromCodeResolvedBumpPointer",
    "artAllocObjectFromCodeInitializedBumpPointer",
    "artAllocObjectFromCodeWithChecksTLAB",
    "artAllocObjectFromCodeResolvedTLAB",
    "artAllocObjectFromCodeInitializedTLAB",
    "artAllocObjectFromCodeWithChecksRegion",
    "artAllocObjectFromCodeResolvedRegion",
    "artAllocObjectFromCodeInitializedRegion",
    "artAllocObjectFromCodeWithChecksRegionTLAB",
    "artAllocObjectFromCodeResolvedRegionTLAB",
    "artAllocObjectFromCodeInitializedRegionTLAB"
};

static void* proxy_art_Class_AllocObject(void* thiz, void* thread) {
    SHADOWHOOK_STACK_SCOPE();

    uintptr_t lr = (uintptr_t)__builtin_return_address(0);
    Dl_info info;
    dladdr((void*)lr, &info);
    LOGD("%s %d %s caller %s in %s", __FILE_NAME__, __LINE__, __FUNCTION__, info.dli_sname, info.dli_fname);

    // 调用 PrettyClass 打印 Class 信息
    if (art_Class_PrettyClass != NULL && thiz != NULL) {
        std::string classInfo = art_Class_PrettyClass(thiz);
//        PrettyClass(thiz, &classInfo);
        LOGD("AllocObject thiz Class info: %s", classInfo.c_str());
        
        // 如果类是 DecorView、Application、Activity、Window 或 View，则打印 Java 堆栈
        if (classInfo.find("DecorView") != std::string::npos ||
            classInfo.find("Application") != std::string::npos ||
            classInfo.find("Activity") != std::string::npos ||
            classInfo.find("Window") != std::string::npos ||
            classInfo.find("View") != std::string::npos) {
            LOGD("===== %s AllocObject Java Stack =====", classInfo.c_str());
            // 打印 Java 堆栈的代码
            // 调用 Thread::DumpFromGdb 函数打印 Java 堆栈
            if (art_Thread_DumpFromGdb != NULL && thread != NULL) {
                LOGD("Calling Thread::DumpFromGdb...");
                art_Thread_DumpFromGdb(thread);
            } else {
                // 简单实现：打印当前线程信息
                pthread_t pthread = pthread_self();
                LOGD("Current thread: %lu", pthread);
            }
            LOGD("====================================");
        }
    }

    return SHADOWHOOK_CALL_PREV(proxy_art_Class_AllocObject, thiz, thread);
}

int ArtJavaHook::start(JNIEnv* env) {
    // 使用 xdl 查找 PrettyClass 符号
    void* handle = xdl_open("libart.so", XDL_DEFAULT);
    if (handle != NULL) {
        size_t symbol_size = 0;
        art_Class_PrettyClass = (art_Class_PrettyClass_func_type_t)xdl_dsym(handle, "_ZN3art6mirror5Class11PrettyClassEv", &symbol_size);
        if (art_Class_PrettyClass != NULL) {
            LOGD("Found _ZN3art6mirror5Class11PrettyClassEv at %p, size=%zu", art_Class_PrettyClass, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art6mirror5Class11PrettyClassEv");
        }
        
        // 查找 Thread::DumpFromGdb 符号
        symbol_size = 0;
        art_Thread_DumpFromGdb = (art_Thread_DumpFromGdb_func_type_t)xdl_dsym(handle, "_ZNK3art6Thread11DumpFromGdbEv", &symbol_size);
        if (art_Thread_DumpFromGdb != NULL) {
            LOGD("Found _ZNK3art6Thread11DumpFromGdbEv at %p, size=%zu", art_Thread_DumpFromGdb, symbol_size);
        } else {
            LOGE("Failed to find _ZNK3art6Thread11DumpFromGdbEv");
        }
        
        // 查找 art runtime 单例符号
        symbol_size = 0;
        void* runtime_instance_ptr = xdl_dsym(handle, "_ZN3art7Runtime9instance_E", &symbol_size);
        if (runtime_instance_ptr != NULL) {
            // 读取指针指向的值，即 runtime 实例的地址
            art_runtime_instance = *(void**)runtime_instance_ptr;
            LOGD("Found _ZN3art7Runtime9instance_E at %p, runtime instance at %p, size=%zu", runtime_instance_ptr, art_runtime_instance, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art7Runtime9instance_E");
        }

        if (art_runtime_instance != NULL) {
            // 破解 hidden_api 限制
            *(int *)((uintptr_t)(art_runtime_instance) + 0x544) = 0;
        }
        
        // 查找 art_quick_to_interpreter_bridge 符号
        symbol_size = 0;
        art_quick_to_interpreter_bridge = (art_quick_to_interpreter_bridge_func_type_t)xdl_dsym(handle, "art_quick_to_interpreter_bridge", &symbol_size);
        if (art_quick_to_interpreter_bridge != NULL) {
            LOGD("Found art_quick_to_interpreter_bridge at %p, size=%zu", art_quick_to_interpreter_bridge, symbol_size);
        } else {
            LOGE("Failed to find art_quick_to_interpreter_bridge");
        }
        
        // 查找 art_quick_resolution_trampoline 符号
        symbol_size = 0;
        art_quick_resolution_trampoline = (art_quick_resolution_trampoline_func_type_t)xdl_dsym(handle, "art_quick_resolution_trampoline", &symbol_size);
        if (art_quick_resolution_trampoline != NULL) {
            LOGD("Found art_quick_resolution_trampoline at %p, size=%zu", art_quick_resolution_trampoline, symbol_size);
        } else {
            LOGE("Failed to find art_quick_resolution_trampoline");
        }
        
        // 查找 art::ArtMethod::PrettyMethod 符号
        symbol_size = 0;
        art_ArtMethod_PrettyMethod = (art_ArtMethod_PrettyMethod_func_type_t)xdl_dsym(handle, "_ZN3art9ArtMethod12PrettyMethodEPS0_b", &symbol_size);
        if (art_ArtMethod_PrettyMethod != NULL) {
            LOGD("Found _ZN3art9ArtMethod12PrettyMethodEPS0_b at %p, size=%zu", art_ArtMethod_PrettyMethod, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art9ArtMethod12PrettyMethodEPS0_b");
        }
        
        // 查找 art::mirror::Class::SetAccessFlags 符号
        symbol_size = 0;
        art_Class_SetAccessFlags = (art_Class_SetAccessFlags_func_type_t)xdl_dsym(handle, "_ZN3art6mirror5Class14SetAccessFlagsEj", &symbol_size);
        if (art_Class_SetAccessFlags != NULL) {
            LOGD("Found _ZN3art6mirror5Class14SetAccessFlagsEj at %p, size=%zu", art_Class_SetAccessFlags, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art6mirror5Class14SetAccessFlagsEj");
        }
        
        // 查找 art::Thread::DecodeJObject 符号
        symbol_size = 0;
        art_Thread_DecodeJObject = (art_Thread_DecodeJObject_func_type_t)xdl_dsym(handle, "_ZNK3art6Thread13DecodeJObjectEP8_jobject", &symbol_size);
        if (art_Thread_DecodeJObject != NULL) {
            LOGD("Found _ZNK3art6Thread13DecodeJObjectEP8_jobject at %p, size=%zu", art_Thread_DecodeJObject, symbol_size);
        } else {
            LOGE("Failed to find _ZNK3art6Thread13DecodeJObjectEP8_jobject");
        }
        
        // 查找 art::Thread::CurrentFromGdb 符号
        symbol_size = 0;
        art_Thread_CurrentFromGdb = (art_Thread_CurrentFromGdb_func_type_t)xdl_dsym(handle, "_ZN3art6Thread14CurrentFromGdbEv", &symbol_size);
        if (art_Thread_CurrentFromGdb != NULL) {
            LOGD("Found _ZN3art6Thread14CurrentFromGdbEv at %p, size=%zu", art_Thread_CurrentFromGdb, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art6Thread14CurrentFromGdbEv");
        }
        
        // 查找 art::mirror::Object::PrettyTypeOf 符号
        symbol_size = 0;
        art_Object_PrettyTypeOf = (art_Object_PrettyTypeOf_func_type_t)xdl_dsym(handle, "_ZN3art6mirror6Object12PrettyTypeOfENS_6ObjPtrIS1_EE", &symbol_size);
        if (art_Object_PrettyTypeOf != NULL) {
            LOGD("Found _ZN3art6mirror6Object12PrettyTypeOfENS_6ObjPtrIS1_EE at %p, size=%zu", art_Object_PrettyTypeOf, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art6mirror6Object12PrettyTypeOfENS_6ObjPtrIS1_EE");
        }
        
        xdl_close(handle);
    } else {
        LOGE("Failed to open libart.so");
    }
    
    // 反射获取 MeasureArtMethodSize 类的 a 和 b 函数的 jmethodID，计算 ArtMethod 结构的大小
    if (env != NULL) {
        jclass measureClass = env->FindClass("com/example/stability/MeasureArtMethodSize");
        if (measureClass != NULL) {
            jmethodID methodA = env->GetStaticMethodID(measureClass, "a", "()V");
            ArtMethod* artMethodA = NULL;
            
            if (methodA != NULL) {
                LOGD("Found MeasureArtMethodSize.a method: %p", methodA);
                
                // 调用 ToArtMethod 转换为 ArtMethod 指针
                artMethodA = ToArtMethod(env, methodA);
                if (artMethodA != NULL) {
                    LOGD("Converted methodA to ArtMethod: %p", artMethodA);
                    
                    // 打印 ptr_sized_fields_ 字段
                    LOGD("ArtMethodA ptr_sized_fields_: 0x%lx", artMethodA->ptr_sized_fields_);
                    
                    // 打印 ArtMethod 详细信息
                    PrintArtMethodInfo(artMethodA);
                    
                    // 检查 ptr_sized_fields_ 字段指向的内存区域
                    uint64_t field_value = artMethodA->ptr_sized_fields_;
                    if (field_value != 0) {
                        Dl_info info;
                        if (dladdr((void*)field_value, &info)) {
                            LOGD("ptr_sized_fields_ (0x%lx) points to:", field_value);
                            LOGD("  dli_fname: %s", info.dli_fname);
                            LOGD("  dli_fbase: %p", info.dli_fbase);
                            LOGD("  dli_sname: %s", info.dli_sname);
                            LOGD("  dli_saddr: %p", info.dli_saddr);
                            
                            // 判断是否是 art_quick_to_interpreter_bridge
                            if (info.dli_sname && strstr(info.dli_sname, "art_quick_to_interpreter_bridge")) {
                                LOGD("  Found art_quick_to_interpreter_bridge!");
                            }
                        }
                    }
                } else {
                    LOGE("Failed to convert methodA to ArtMethod");
                }
                
                // 清理可能的 pending 异常
                if (env->ExceptionCheck()) {
                    LOGD("Clearing pending exception after GetStaticMethodID for a");
                    env->ExceptionClear();
                }
            } else {
                LOGE("Failed to find MeasureArtMethodSize.a method");
                
                // 清理可能的 pending 异常
                if (env->ExceptionCheck()) {
                    LOGD("Clearing pending exception after failed GetStaticMethodID for a");
                    env->ExceptionClear();
                }
            }
            
            jmethodID methodB = env->GetStaticMethodID(measureClass, "b", "()V");
            ArtMethod* artMethodB = NULL;
            if (methodB != NULL) {
                LOGD("Found MeasureArtMethodSize.b method: %p", methodB);
                
                // 调用 ToArtMethod 转换为 ArtMethod 指针
                artMethodB = ToArtMethod(env, methodB);
                if (artMethodB != NULL) {
                    LOGD("Converted methodB to ArtMethod: %p", artMethodB);
                    
                    // 打印 ArtMethod 详细信息
                    PrintArtMethodInfo(artMethodB);
                } else {
                    LOGE("Failed to convert methodB to ArtMethod");
                }
                
                // 清理可能的 pending 异常
                if (env->ExceptionCheck()) {
                    LOGD("Clearing pending exception after GetStaticMethodID for b");
                    env->ExceptionClear();
                }
            } else {
                LOGE("Failed to find MeasureArtMethodSize.b method");
                
                // 清理可能的 pending 异常
                if (env->ExceptionCheck()) {
                    LOGD("Clearing pending exception after failed GetStaticMethodID for b");
                    env->ExceptionClear();
                }
            }
            
            // 计算 ArtMethod 结构的大小
            if (artMethodA != NULL && artMethodB != NULL) {
                ptrdiff_t artMethodSize = (char*)artMethodB - (char*)artMethodA;
                LOGD("ArtMethod structure size: %td bytes (0x%tx)", artMethodSize, artMethodSize);
            } else if (methodA != NULL && methodB != NULL) {
                // 回退到使用 jmethodID 指针计算
                ptrdiff_t artMethodSize = (char*)methodB - (char*)methodA;
                LOGD("ArtMethod structure size (using jmethodID): %td bytes (0x%tx)", artMethodSize, artMethodSize);
            } else {
                LOGE("Cannot calculate ArtMethod size: missing methods");
            }
            
            env->DeleteLocalRef(measureClass);
            
            // 清理可能的 pending 异常
            if (env->ExceptionCheck()) {
                LOGD("Clearing pending exception after DeleteLocalRef");
                env->ExceptionClear();
            }
        } else {
            LOGE("Failed to find MeasureArtMethodSize class");
            
            // 清理可能的 pending 异常
            if (env->ExceptionCheck()) {
                LOGD("Clearing pending exception after FindClass");
                env->ExceptionClear();
            }
        }
    } else {
        LOGE("JNIEnv is NULL");
    }

    // 获取 DecorView 类的构造函数对应的 jmethodID
    if (env != NULL) {
        jclass decorViewClass = env->FindClass("com/android/internal/policy/DecorView");
        if (decorViewClass != NULL) {
            // 保存为全局引用
            if (g_decorViewClass == NULL) {
                g_decorViewClass = (jclass)env->NewGlobalRef(decorViewClass);
                if (g_decorViewClass != NULL) {
                    LOGD("Successfully created global reference for DecorView class");
                } else {
                    LOGE("Failed to create global reference for DecorView class");
                }
            }
            
            // TODO SOA 进入 虚拟机内部 running 状态
            void * thread_ptr = GetCurrentThreadFromGdb();
            void * internalClass = DecodeJObject(thread_ptr, decorViewClass);
            LOGD("DecorView class %p", internalClass);
            uint32_t old_flag = *(uint32_t *) (((uintptr_t) (internalClass) & 0xffffffff) + 0x40);
            uint32_t new_flag = old_flag | 0x80000000;
            art_Class_SetAccessFlags(internalClass, new_flag);
            
            // 反射打印 Class 对象的字段和偏移量
            LOGD("===== Reflecting Class fields for DecorView ====");
            
            // 获取 Class 类的 getDeclaredFields 方法
            jclass classClass = env->FindClass("java/lang/Class");
            if (classClass != NULL) {
                jmethodID getDeclaredFieldsMethod = env->GetMethodID(classClass, "getDeclaredFields", "()[Ljava/lang/reflect/Field;");
                if (getDeclaredFieldsMethod != NULL) {
                    // 调用 getDeclaredFields 获取所有字段
                    jobjectArray fieldsArray = (jobjectArray)env->CallObjectMethod(classClass, getDeclaredFieldsMethod);
                    if (fieldsArray != NULL) {
                        jsize fieldsCount = env->GetArrayLength(fieldsArray);
                        LOGD("Found %d declared fields in DecorView class", fieldsCount);
                        
                        // 获取 Field 类的 getName 方法
                        jclass fieldClass = env->FindClass("java/lang/reflect/Field");
                        if (fieldClass != NULL) {
                            jmethodID getNameMethod = env->GetMethodID(fieldClass, "getName", "()Ljava/lang/String;");
                            if (getNameMethod != NULL) {
                                // 尝试获取 Field 类的 getOffset 方法
                                jmethodID getOffsetMethod = env->GetMethodID(fieldClass, "getOffset", "()I");
                                
                                // 遍历所有字段
                                for (jsize i = 0; i < fieldsCount; i++) {
                                    jobject field = env->GetObjectArrayElement(fieldsArray, i);
                                    if (field != NULL) {
                                        // 获取字段名称
                                        jstring fieldName = (jstring)env->CallObjectMethod(field, getNameMethod);
                                        if (fieldName != NULL) {
                                            const char* fieldNameCStr = env->GetStringUTFChars(fieldName, NULL);
                                            if (fieldNameCStr != NULL) {
                                                // 打印字段名称
                                                LOGD("Field %d: %s", i, fieldNameCStr);
                                                
                                                // 尝试调用 getOffset 方法
                                                if (getOffsetMethod != NULL) {
                                                    jint offset = env->CallIntMethod(field, getOffsetMethod);
                                                    LOGD("  Offset: %d", offset);
                                                }
                                                
                                                // 重点关注 accessFlags 字段
                                                if (strcmp(fieldNameCStr, "accessFlags") == 0) {
                                                    LOGD("===== Found accessFlags field! =====");
                                                    if (getOffsetMethod != NULL) {
                                                        jint offset = env->CallIntMethod(field, getOffsetMethod);
                                                        LOGD("  accessFlags offset: %d", offset);
                                                    }
                                                }
                                                
                                                // 重点关注 objectSizeAllocFastPath 字段
                                                if (strcmp(fieldNameCStr, "objectSizeAllocFastPath") == 0) {
                                                    LOGD("===== Found objectSizeAllocFastPath field! =====");
                                                    if (getOffsetMethod != NULL) {
                                                        jint offset = env->CallIntMethod(field, getOffsetMethod);
                                                        LOGD("  objectSizeAllocFastPath offset: %d", offset);
                                                    }
                                                    
                                                    // 尝试设置字段值为 max int
                                                    jmethodID setIntMethod = env->GetMethodID(fieldClass, "setInt", "(Ljava/lang/Object;I)V");
                                                    if (setIntMethod != NULL) {
                                                        // 获取 int 的最大值
                                                        jint maxInt = 0x7FFFFFFF; // 2^31 - 1
                                                        // 设置字段值
                                                        env->CallVoidMethod(field, setIntMethod, decorViewClass, maxInt);
                                                        LOGD("Successfully set objectSizeAllocFastPath to max int: %d", maxInt);
                                                    } else {
                                                        LOGE("Failed to find Field.setInt method");
                                                    }
                                                }

                                                
                                                
                                                env->ReleaseStringUTFChars(fieldName, fieldNameCStr);
                                            }
                                            env->DeleteLocalRef(fieldName);
                                        }
                                        env->DeleteLocalRef(field);
                                    }
                                }
                            } else {
                                LOGE("Failed to find Field.getName method");
                            }
                            env->DeleteLocalRef(fieldClass);
                        } else {
                            LOGE("Failed to find Field class");
                        }
                    } else {
                        LOGE("Failed to get declared fields");
                    }
                    env->DeleteLocalRef(fieldsArray);
                } else {
                    LOGE("Failed to find Class.getDeclaredFields method");
                }
                env->DeleteLocalRef(classClass);
            } else {
                LOGE("Failed to find Class class");
            }
            
            // 清理可能的 pending 异常
            if (env->ExceptionCheck()) {
                LOGD("Clearing pending exception after reflecting Class fields");
                env->ExceptionClear();
            }
            LOGD("===== End of Class fields reflection ====");

            
            // 尝试不同的构造函数签名
            const char* signatures[] = {
                "(Landroid/content/Context;)V",
                "(Landroid/content/Context;ILandroid/view/WindowManager$LayoutParams;)V",
                "(Landroid/content/Context;ILcom/android/internal/policy/PhoneWindow;)V",
                "(Landroid/content/Context;Landroid/view/WindowManager$LayoutParams;)V",
                "(Landroid/content/Context;ILcom/android/internal/policy/PhoneWindow;Landroid/view/WindowManager$LayoutParams;)V"
            };
            
            int signature_count = sizeof(signatures) / sizeof(signatures[0]);
            
            int found_count = 0;
            for (int i = 0; i < signature_count; i++) {
                jmethodID temp_constructor = env->GetMethodID(decorViewClass, "<init>", signatures[i]);
                if (temp_constructor != NULL) {
                    LOGD("Found DecorView constructor with signature %s: %p", signatures[i], temp_constructor);
                    
                    // 打印 ArtMethod 结构的信息
                    // 使用 ToArtMethod 函数获取 ArtMethod 指针
                    ArtMethod* art_method = ToArtMethod(env, temp_constructor);
                    LOGD("ArtMethod address: %p", art_method);
                    if (art_method != NULL) {
                        LOGD("ArtMethod ptr_sized_fields_: 0x%lx", art_method->ptr_sized_fields_);
                        
                        // 打印 ArtMethod 详细信息
                        PrintArtMethodInfo(art_method);

                        // 改解释执行
                        art_method->ptr_sized_fields_ = (uintptr_t)(art_quick_to_interpreter_bridge);
                    } else {
                        LOGE("Failed to convert temp_constructor to ArtMethod");
                    }
                    
                    found_count++;
                }
                
                // 清理可能的 pending 异常
                if (env->ExceptionCheck()) {
                    LOGD("Clearing pending exception for signature %s", signatures[i]);
                    env->ExceptionClear();
                }
            }
            
            if (found_count == 0) {
                LOGE("Failed to find DecorView constructor with any of the tried signatures");
            } else {
                LOGD("Found %d DecorView constructors", found_count);
            }
            
            env->DeleteLocalRef(decorViewClass);
            
            // 清理可能的 pending 异常
            if (env->ExceptionCheck()) {
                LOGD("Clearing pending exception after DeleteLocalRef");
                env->ExceptionClear();
            }
        } else {
            LOGE("Failed to find DecorView class");
            
            // 清理可能的 pending 异常
            if (env->ExceptionCheck()) {
                LOGD("Clearing pending exception after FindClass");
                env->ExceptionClear();
            }
        }
    } else {
        LOGE("JNIEnv is NULL");
    }

    // 获取 PhoneWindow 类的 generateDecor 方法
    if (env != NULL) {
        jclass phoneWindowClass = env->FindClass("com/android/internal/policy/PhoneWindow");
        if (phoneWindowClass != NULL) {
            jmethodID generateDecorMethod = env->GetMethodID(phoneWindowClass, "generateDecor", "(I)Lcom/android/internal/policy/DecorView;");
            if (generateDecorMethod != NULL) {
                LOGD("Found PhoneWindow.generateDecor method: %p", generateDecorMethod);
                
                // 打印 ArtMethod 结构的信息
                ArtMethod* art_method = ToArtMethod(env, generateDecorMethod);
                LOGD("ArtMethod address: %p", art_method);
                if (art_method != NULL) {
                    LOGD("ArtMethod ptr_sized_fields_: 0x%lx", art_method->ptr_sized_fields_);
                    
                    // 打印 ArtMethod 详细信息
                    PrintArtMethodInfo(art_method);

                    // 改解释执行
                    art_method->ptr_sized_fields_ = (uintptr_t)(art_quick_to_interpreter_bridge);
                } else {
                    LOGE("Failed to convert generateDecorMethod to ArtMethod");
                }
            } else {
                LOGE("Failed to find PhoneWindow.generateDecor method");
                
                // 清理可能的 pending 异常
                if (env->ExceptionCheck()) {
                    LOGD("Clearing pending exception after GetMethodID for generateDecor");
                    env->ExceptionClear();
                }
            }
            
            env->DeleteLocalRef(phoneWindowClass);
            
            // 清理可能的 pending 异常
            if (env->ExceptionCheck()) {
                LOGD("Clearing pending exception after DeleteLocalRef");
                env->ExceptionClear();
            }
        } else {
            LOGE("Failed to find PhoneWindow class");
            
            // 清理可能的 pending 异常
            if (env->ExceptionCheck()) {
                LOGD("Clearing pending exception after FindClass");
                env->ExceptionClear();
            }
        }
    } else {
        LOGE("JNIEnv is NULL");
    }
    


    // 批量 hook 所有函数
    int function_count = sizeof(function_names) / sizeof(function_names[0]);
    for (int i = 0; i < function_count; i++) {
        const char* func_name = function_names[i];
        void* result = shadowhook_hook_sym_name("libart.so", func_name,
                                             (void*)proxy_art_Class_AllocObject, (void**)&orig_functions[i]);
        if (result != NULL) {
            LOGD("Successfully hooked %s", func_name);
        } else {
            LOGE("Failed to hook %s", func_name);
        }
    }
    
    // Hook art::gc::Heap::AddFinalizerReference 函数
    void* result = shadowhook_hook_sym_name("libart.so", "_ZN3art2gc4Heap21AddFinalizerReferenceEPNS_6ThreadEPNS_6ObjPtrINS_6mirror6ObjectEEE",
                                         (void*)proxy_art_gc_Heap_AddFinalizerReference, (void**)&orig_art_gc_Heap_AddFinalizerReference);
    if (result != NULL) {
        LOGD("Successfully hooked _ZN3art2gc4Heap21AddFinalizerReferenceEPNS_6ThreadEPNS_6ObjPtrINS_6mirror6ObjectEEE");
    } else {
        LOGE("Failed to hook _ZN3art2gc4Heap21AddFinalizerReferenceEPNS_6ThreadEPNS_6ObjPtrINS_6mirror6ObjectEEE");
    }
    
    return 0;
}
