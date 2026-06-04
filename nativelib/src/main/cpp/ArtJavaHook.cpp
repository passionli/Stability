// ArtJavaHook.cpp
#include "ArtJavaHook.h"

#include <dlfcn.h>
#include <android/log.h>
#include <string>
#include "shadowhook.h"
#include "xdl.h"
#include "NativeFunctionPatcher.h"

#define LOG_TAG "ArtJavaHook"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// art::mirror::Class::AllocObject(art::Thread*)
// 返回类型: art::mirror::Object*
// 参数: art::mirror::Class* this, art::Thread* thread
// 使用 void* 来简化类型定义
typedef void *(*art_Class_AllocObject_func_type_t)(void *, void *);

// art::mirror::Class::PrettyClass()
// 返回类型: std::string
// 参数: art::mirror::Class* this
typedef std::string (*art_Class_PrettyClass_func_type_t)(void *);

// art::Thread::DumpFromGdb() const
// 返回类型: void
// 参数: art::Thread* this
typedef void (*art_Thread_DumpFromGdb_func_type_t)(void *);

// art_quick_to_interpreter_bridge
// 函数类型定义
typedef void (*art_quick_to_interpreter_bridge_func_type_t)(void *);

// art_quick_resolution_trampoline
// 函数类型定义
typedef void (*art_quick_resolution_trampoline_func_type_t)(void);

// art::ArtMethod::PrettyMethod(ArtMethod*, bool)
// 返回类型: std::string
// 参数: art::ArtMethod* this, art::ArtMethod* compare_to, bool with_signature
typedef std::string (*art_ArtMethod_PrettyMethod_func_type_t)(void *, void *, bool);

// art::ArtMethod::Invoke(art::Thread*, uint32_t, uint32_t, art::JValue*, const char*)
// 返回类型: void
// 参数: art::ArtMethod* this, art::Thread* self, uint32_t args_size, uint32_t args[], art::JValue* result, const char* shorty
typedef void (*art_ArtMethod_Invoke_func_type_t)(void *, void *, uint32_t *, uint32_t, void *, const char *);

// art::JNIEnvExt::AddLocalReference< _jstring >(JNIEnvExt*, _jstring)
// 返回类型: art::ObjPtr<art::mirror::Object>
// 参数: JNIEnvExt* this, _jstring obj
typedef jobject (*art_JNIEnvExt_AddLocalReference_func_type_t)(void *, void *);

// art::BoxPrimitive(Primitive::Type, const JValue&)
// 返回类型: art::ObjPtr<art::mirror::Object>
// 参数: art::Primitive::Type type, const JValue& value
typedef void *(*art_BoxPrimitive_func_type_t)(int, void *);

// art::mirror::Class::SetAccessFlags 函数类型
// 参数: art::mirror::Class* this, uint32_t access_flags
typedef void (*art_Class_SetAccessFlags_func_type_t)(void *, uint32_t);

// art::Thread::DecodeJObject 函数类型
// 参数: art::Thread* this, jobject obj
// 返回类型: void*
typedef void *(*art_Thread_DecodeJObject_func_type_t)(void *, jobject);

// art::Thread::CurrentFromGdb 函数类型
// 无参数
// 返回类型: art::Thread*
typedef void *(*art_Thread_CurrentFromGdb_func_type_t)(void);

// art::gc::Heap::AddFinalizerReference 函数类型
// 参数: art::gc::Heap* this, art::Thread* thread, art::ObjPtr<art::mirror::Object> obj
// 无返回值
typedef void (*art_gc_Heap_AddFinalizerReference_func_type_t)(void *, void *, void *);

// art::mirror::Object::PrettyTypeOf 函数类型
// 参数: art::ObjPtr<art::mirror::Object> obj
// 返回类型: std::string
typedef void (*art_Object_PrettyTypeOf_func_type_t)(std::string *, void *);

// art::DumpNativeStack 函数类型
// 参数: std::ostream& os, int skip_count, BacktraceMap* backtrace_map, const char* prefix, ArtMethod* method, void* ucontext, bool dump_native_stack
// 无返回值
typedef void (*art_DumpNativeStack_func_type_t)(void *, int, void *, const char *, void *, void *,
                                                bool);

// artQuickToInterpreterBridge 函数类型
// 参数: ArtMethod* method, Thread* self, ArtMethod** sp
// 返回: uint64_t
typedef uint64_t (*artQuickToInterpreterBridge_func_type_t)(void *, void *, void **);

// art::interpreter::DoCall<false> 函数类型
typedef bool (*art_interpreter_DoCall_f_func_type_t)(void *, void *, void *, const void *, int,
                                                     bool, void *);

// art::interpreter::DoCall<false, false> 函数类型
typedef bool (*art_interpreter_DoCall_ff_func_type_t)(void *, void *, void *, const void *, int,
                                                      void *);

// art::interpreter::DoCall<false, true> 函数类型
typedef bool (*art_interpreter_DoCall_ft_func_type_t)(void *, void *, void *, const void *, int,
                                                      void *);

// art::interpreter::DoCall<true, false> 函数类型
typedef bool (*art_interpreter_DoCall_tf_func_type_t)(void *, void *, void *, const void *, int,
                                                      void *);

// art::interpreter::DoCall<true, true> 函数类型
typedef bool (*art_interpreter_DoCall_tt_func_type_t)(void *, void *, void *, const void *, int,
                                                      void *);



// art::interpreter::EnterInterpreterFromInvoke 函数类型
typedef void (*art_interpreter_EnterInterpreterFromInvoke_func_type_t)(void *, void *, void *,
                                                                       uint32_t *, void *, bool);

// art_quick_invoke_stub 函数类型
// 参数: ArtMethod*, uint32_t*, uint32_t, Thread*, JValue*, const char*
typedef void (*art_quick_invoke_stub_func_type_t)(void *, uint32_t *, uint32_t, void *, void *,
                                                  const char *);

// art_quick_invoke_static_stub 函数类型
// 参数: ArtMethod*, uint32_t*, uint32_t, Thread*, JValue*, const char*
typedef void (*art_quick_invoke_static_stub_func_type_t)(void *, uint32_t *, uint32_t, void *,
                                                         void *, const char *);

// art::instrumentation::Instrumentation::InstallStubsForClass 函数类型
typedef void (*art_instrumentation_InstallStubsForClass_func_type_t)(void *, void *);

// 原始函数指针数组
void *orig_functions[20] = {NULL};
static art_Class_PrettyClass_func_type_t art_Class_PrettyClass = NULL;
static art_Thread_DumpFromGdb_func_type_t art_Thread_DumpFromGdb = NULL;
static art_quick_to_interpreter_bridge_func_type_t art_quick_to_interpreter_bridge = NULL;
static art_quick_resolution_trampoline_func_type_t art_quick_resolution_trampoline = NULL;
// art::ArtMethod::PrettyMethod 函数指针
static art_ArtMethod_PrettyMethod_func_type_t art_ArtMethod_PrettyMethod = NULL;
// art::ArtMethod::Invoke 函数指针
static art_ArtMethod_Invoke_func_type_t art_ArtMethod_Invoke = NULL;
// art::JNIEnvExt::AddLocalReference 函数指针
static art_JNIEnvExt_AddLocalReference_func_type_t art_JNIEnvExt_AddLocalReference = NULL;
// art::BoxPrimitive 函数指针
static art_BoxPrimitive_func_type_t art_BoxPrimitive = NULL;
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
static void *art_runtime_instance = NULL;
// DecorView 类的全局引用
static jclass g_decorViewClass = NULL;
// 主线程 ID
static pthread_t g_main_thread_id = 0;
// art::DumpNativeStack 函数指针
static art_DumpNativeStack_func_type_t art_DumpNativeStack = NULL;
// JavaVM 指针
static JavaVM *g_jvm = NULL;

// artQuickToInterpreterBridge 原始函数指针
static artQuickToInterpreterBridge_func_type_t orig_artQuickToInterpreterBridge = NULL;

// art::interpreter::DoCall<false> 原始函数指针
static art_interpreter_DoCall_f_func_type_t orig_art_interpreter_DoCall_f = NULL;

// art::interpreter::DoCall<false, false> 原始函数指针
static art_interpreter_DoCall_ff_func_type_t orig_art_interpreter_DoCall_ff = NULL;

// art::interpreter::DoCall<false, true> 原始函数指针
static art_interpreter_DoCall_ft_func_type_t orig_art_interpreter_DoCall_ft = NULL;

// art::interpreter::DoCall<true, false> 原始函数指针
static art_interpreter_DoCall_tf_func_type_t orig_art_interpreter_DoCall_tf = NULL;

// art::interpreter::DoCall<true, true> 原始函数指针
static art_interpreter_DoCall_tt_func_type_t orig_art_interpreter_DoCall_tt = NULL;

// art::ArtMethod::Invoke 原始函数指针
static art_ArtMethod_Invoke_func_type_t orig_art_ArtMethod_Invoke = NULL;

// art::interpreter::EnterInterpreterFromInvoke 原始函数指针
static art_interpreter_EnterInterpreterFromInvoke_func_type_t orig_art_interpreter_EnterInterpreterFromInvoke = NULL;

// art_quick_invoke_stub 原始函数指针
static art_quick_invoke_stub_func_type_t orig_art_quick_invoke_stub = NULL;

// art_quick_invoke_static_stub 原始函数指针
static art_quick_invoke_static_stub_func_type_t orig_art_quick_invoke_static_stub = NULL;

// art::instrumentation::Instrumentation::InstallStubsForClass 原始函数指针
static art_instrumentation_InstallStubsForClass_func_type_t orig_art_instrumentation_InstallStubsForClass = NULL;

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

void *DecodeJObject(void *thread_ptr, jobject obj);

enum { kSmallArgArraySize = 16 };
class ArgArray {
public:
    ArgArray(const char* shorty, uint32_t shorty_len)
            : shorty_(shorty), shorty_len_(shorty_len), num_bytes_(0) {
        size_t num_slots = shorty_len + 1;  // +1 in case of receiver.
        if (((num_slots * 2) < kSmallArgArraySize)) {
            // We can trivially use the small arg array.
            arg_array_ = small_arg_array_;
        } else {
            // Analyze shorty to see if we need the large arg array.
            for (size_t i = 1; i < shorty_len; ++i) {
                char c = shorty[i];
                if (c == 'J' || c == 'D') {
                    num_slots++;
                }
            }
            if (num_slots <= kSmallArgArraySize) {
                arg_array_ = small_arg_array_;
            } else {
                large_arg_array_.reset(new uint32_t[num_slots]);
                arg_array_ = large_arg_array_.get();
            }
        }
    }

    uint32_t* GetArray() {
        return arg_array_;
    }

    uint32_t GetNumBytes() {
        return num_bytes_;
    }

    void Append(uint32_t value) {
        arg_array_[num_bytes_ / 4] = value;
        num_bytes_ += 4;
    }

    void Append(void * obj) {
        Append((uint32_t)(uintptr_t)(obj));
    }

    void AppendWide(uint64_t value) {
        arg_array_[num_bytes_ / 4] = value;
        arg_array_[(num_bytes_ / 4) + 1] = value >> 32;
        num_bytes_ += 8;
    }

    void AppendFloat(float value) {
        jvalue jv;
        jv.f = value;
        Append(jv.i);
    }

    void AppendDouble(double value) {
        jvalue jv;
        jv.d = value;
        AppendWide(jv.j);
    }

    static void ThrowIllegalPrimitiveArgumentException(const char* expected,
                                                       const char* found_descriptor)
    {

    }

    bool BuildArgArrayFromObjectArray(jobject receiver,
                                      jobjectArray raw_args,
                                      void * m,
                                      void * self,
                                      JNIEnv * env)
    {
//            const dex::TypeList* classes = m->GetParameterTypeList();
        // Set receiver if non-null (method is not static)
        if (receiver != nullptr) {
            void * obj = DecodeJObject(self, receiver);
            Append(obj);
        }
//            StackHandleScope<2> hs(self);
//            MutableHandle<mirror::Object> arg(hs.NewHandle<mirror::Object>(nullptr));
//            Handle<mirror::ObjectArray<mirror::Object>> args(
//            hs.NewHandle<mirror::ObjectArray<mirror::Object>>(raw_args));
        for (size_t i = 1, args_offset = 0; i < shorty_len_; ++i, ++args_offset) {
            auto jobject_arg = env->GetObjectArrayElement(raw_args, args_offset);
            auto arg = DecodeJObject(self, jobject_arg);

//                arg.Assign(args->Get(args_offset));

//                if (((shorty_[i] == 'L') && (arg != nullptr)) ||
//                    ((arg == nullptr && shorty_[i] != 'L'))) {
//                    // TODO: The method's parameter's type must have been previously resolved, yet
//                    // we've seen cases where it's not b/34440020.
//                    ObjPtr<mirror::Class> dst_class(
//                            m->ResolveClassFromTypeIndex(classes->GetTypeItem(args_offset).type_idx_));
//                    if (dst_class == nullptr) {
//                        CHECK(self->IsExceptionPending());
//                        return false;
//                    }
//                    if (UNLIKELY(arg == nullptr || !arg->InstanceOf(dst_class))) {
//                        ThrowIllegalArgumentException(
//                                StringPrintf("method %s argument %zd has type %s, got %s",
//                                             m->PrettyMethod(false).c_str(),
//                                             args_offset + 1,  // Humans don't count from 0.
//                                             mirror::Class::PrettyDescriptor(dst_class).c_str(),
//                                             mirror::Object::PrettyTypeOf(arg.Get()).c_str()).c_str());
//                        return false;
//                    }
//                }

#define DO_FIRST_ARG(match_descriptor, get_fn, append) { \
          if (LIKELY(arg != nullptr && \
              arg->GetClass()->DescriptorEquals(match_descriptor))) { \
            ArtField* primitive_field = arg->GetClass()->GetInstanceField(0); \
            append(primitive_field-> get_fn(arg.Get()));

#define DO_ARG(match_descriptor, get_fn, append) \
          } else if (LIKELY(arg != nullptr && \
                            arg->GetClass<>()->DescriptorEquals(match_descriptor))) { \
            ArtField* primitive_field = arg->GetClass()->GetInstanceField(0); \
            append(primitive_field-> get_fn(arg.Get()));

#define DO_FAIL(expected) \
          } else { \
            if (arg->GetClass<>()->IsPrimitive()) { \
              std::string temp; \
              ThrowIllegalPrimitiveArgumentException(expected, \
                                                     arg->GetClass<>()->GetDescriptor(&temp)); \
            } else { \
              ThrowIllegalArgumentException(\
                  StringPrintf("method %s argument %zd has type %s, got %s", \
                      ArtMethod::PrettyMethod(m, false).c_str(), \
                      args_offset + 1, \
                      expected, \
                      mirror::Object::PrettyTypeOf(arg.Get()).c_str()).c_str()); \
            } \
            return false; \
          } }

            switch (shorty_[i]) {
                case 'L':
                    Append(arg);
                    break;
                case 'Z':
                {
                    auto clz = env->GetObjectClass(jobject_arg);
                    auto f = env->GetFieldID(clz, "value", "Z");
                    auto b = env->GetBooleanField(jobject_arg, f);
                    Append(b);
                    break;
                }
                case 'B':
                {
                    auto clz = env->GetObjectClass(jobject_arg);
                    auto f = env->GetFieldID(clz, "value", "B");
                    auto b = env->GetByteField(jobject_arg, f);
                    Append(b);
                    break;
                }
                case 'C':
                {
                    auto clz = env->GetObjectClass(jobject_arg);
                    auto f = env->GetFieldID(clz, "value", "C");
                    auto b = env->GetCharField(jobject_arg, f);
                    Append(b);
                    break;
                }
                case 'S':
                {
                    auto clz = env->GetObjectClass(jobject_arg);
                    auto f = env->GetFieldID(clz, "value", "S");
                    auto b = env->GetShortField(jobject_arg, f);
                    Append(b);
                    break;
                }
                case 'I':
                {
                    auto clz = env->GetObjectClass(jobject_arg);
                    auto f = env->GetFieldID(clz, "value", "I");
                    auto b = env->GetIntField(jobject_arg, f);
                    Append(b);
                    break;
                }
                case 'J':
                {
                    auto clz = env->GetObjectClass(jobject_arg);
                    auto f = env->GetFieldID(clz, "value", "J");
                    auto b = env->GetLongField(jobject_arg, f);
                    Append(b);
                    break;
                }
                case 'F':
                {
                    auto clz = env->GetObjectClass(jobject_arg);
                    auto f = env->GetFieldID(clz, "value", "F");
                    auto b = env->GetFloatField(jobject_arg, f);
                    Append(b);
                    break;
                }
                case 'D':
                {
                    auto clz = env->GetObjectClass(jobject_arg);
                    auto f = env->GetFieldID(clz, "value", "F");
                    auto b = env->GetDoubleField(jobject_arg, f);
                    Append(b);
                    break;
                }
//#ifndef NDEBUG
//                    default:
//                        LOG(FATAL) << "Unexpected shorty character: " << shorty_[i];
//                        UNREACHABLE();
//#endif
            }
#undef DO_FIRST_ARG
#undef DO_ARG
#undef DO_FAIL
        }
        return true;
    }

private:
    const char* const shorty_;
    const uint32_t shorty_len_;
    uint32_t num_bytes_;
    uint32_t* arg_array_;
    uint32_t small_arg_array_[kSmallArgArraySize];
    std::unique_ptr<uint32_t[]> large_arg_array_;
};

// 打印 ArtMethod 的入口点信息
void PrintEntryPoint(ArtMethod *art_method);

// 设置 Class 的访问标志
// 参数: class_ptr - Class 指针, access_flags - 新的访问标志
void SetClassAccessFlags(void *class_ptr, uint32_t access_flags) {
    if (art_Class_SetAccessFlags != NULL && class_ptr != NULL) {
        try {
            art_Class_SetAccessFlags(class_ptr, access_flags);
            LOGD("Successfully set access flags to 0x%x for class at %p", access_flags, class_ptr);
        } catch (const std::exception &e) {
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
void *DecodeJObject(void *thread_ptr, jobject obj) {
    if (art_Thread_DecodeJObject != NULL && thread_ptr != NULL) {
        try {
            void *result = art_Thread_DecodeJObject(thread_ptr, obj);
            LOGD("Successfully decoded jobject %p to %p", obj, result);
            return result;
        } catch (const std::exception &e) {
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
void *GetCurrentThreadFromGdb() {
    if (art_Thread_CurrentFromGdb != NULL) {
        try {
            void *result = art_Thread_CurrentFromGdb();
            LOGD("Successfully got current thread from GDB: %p", result);
            return result;
        } catch (const std::exception &e) {
            LOGE("Exception when calling art_Thread_CurrentFromGdb: %s", e.what());
            return NULL;
        }
    } else {
        LOGE("art_Thread_CurrentFromGdb is NULL");
        return NULL;
    }
}

// 打印本地堆栈
// 参数: os - 输出流, skip_count - 跳过的堆栈帧数, backtrace_map - 回溯映射, prefix - 前缀, method - ArtMethod, ucontext - 上下文, dump_native_stack - 是否打印本地堆栈
void
DumpNativeStack(void *os, int skip_count, void *backtrace_map, const char *prefix, void *method,
                void *ucontext, bool dump_native_stack) {
    if (art_DumpNativeStack != NULL) {
        try {
            art_DumpNativeStack(os, skip_count, backtrace_map, prefix, method, ucontext,
                                dump_native_stack);
            LOGD("Successfully called art_DumpNativeStack");
        } catch (const std::exception &e) {
            LOGE("Exception when calling art_DumpNativeStack: %s", e.what());
        }
    } else {
        LOGE("art_DumpNativeStack is NULL");
    }
}

// 获取对象的类型信息
// 参数: obj - 对象指针 (art::ObjPtr<art::mirror::Object>)
// 返回: 类型信息字符串
std::string PrettyTypeOf(void *obj) {
    if (art_Object_PrettyTypeOf != NULL && obj != NULL) {
        try {
            std::string result;
            art_Object_PrettyTypeOf(&result, obj);
            LOGD("Successfully got pretty type of object %p: %s", obj, result.c_str());
            return result;
        } catch (const std::exception &e) {
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


static __attribute__((noinline)) void
art_Class_PrettyClass_I(void *class_ptr, void *result, void *func) {
    __asm__ volatile (
            "mov x8, x1\n"
            "br x2\n"
            );
//    art_Class_PrettyClass();
}

// 获取 Class 的类型信息
// 参数: class_ptr - Class 指针
// 返回: 类型信息字符串
static __attribute__((noinline)) std::string PrettyClass(void *class_ptr) {
    if (art_Class_PrettyClass != NULL && class_ptr != NULL) {
        LOGD("start got pretty class of %p out of", class_ptr);
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

void *GetClass(void *obj) {
    return (void *) (*(uint32_t *) (obj));
}

// 代理函数: art::gc::Heap::AddFinalizerReference
static void proxy_art_gc_Heap_AddFinalizerReference(void *heap, void *thread, void **obj) {
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

        // 检查是否是 PhoneWindow 类的 Class 对象
        if (string == "java.lang.Class<com.android.internal.policy.PhoneWindow>") {
            LOGD("Found PhoneWindow Class object being finalized");

            // 获取 JNIEnv 指针
            JNIEnv *env = NULL;

            // 使用全局 JavaVM 指针
            if (g_jvm != NULL) {
                // 附加当前线程到 JVM
                jint result = g_jvm->AttachCurrentThread(&env, NULL);
                if (result == JNI_OK && env != NULL) {
                    LOGD("Successfully attached thread and got JNIEnv");

                    // 查找 com.android.internal.policy.PhoneWindow2 类
                    jclass phoneWindow2Class = env->FindClass(
                            "com/android/internal/policy/PhoneWindow2");
                    if (phoneWindow2Class != NULL) {
                        LOGD("Found PhoneWindow2 class: %p", phoneWindow2Class);

                        // 调用 DecodeJObject 获取内部指针
                        void *phoneWindow2Ptr = DecodeJObject(thread, phoneWindow2Class);
                        if (phoneWindow2Ptr != NULL) {
                            LOGD("Successfully decoded PhoneWindow2 class pointer: %p",
                                 phoneWindow2Ptr);

                            *(uint32_t *) ((uintptr_t) (*obj)) = (uint32_t) ((uintptr_t) (phoneWindow2Ptr));
                        } else {
                            LOGE("Failed to decode PhoneWindow2 class pointer");
                        }

                        // 释放本地引用
                        env->DeleteLocalRef(phoneWindow2Class);
                    } else {
                        LOGE("Failed to find com.android.internal.policy.PhoneWindow2 class");
                    }

                    // 清理可能的 pending 异常
                    if (env->ExceptionCheck()) {
                        LOGD("Clearing pending exception after finding PhoneWindow2 class");
                        env->ExceptionClear();
                    }

                    // 分离当前线程
                    g_jvm->DetachCurrentThread();
                } else {
                    LOGE("Failed to attach thread or get JNIEnv");
                }
            } else {
                LOGE("JavaVM pointer is NULL");
            }
        }
    }

    if (clazz == clz) {
        LOGE("===== CRITICAL: DecorView object is being finalized! =====");
        LOGE("  obj address: %p", *obj);
        LOGE("  obj class: %s", string.c_str());
        LOGE("  thread: %p", thread);
        LOGE("  heap: %p", heap);

        // 获取当前线程 ID
        pthread_t current_thread = pthread_self();
        LOGE("  Current thread ID: %lu", (unsigned long) current_thread);
        LOGE("  Main thread ID: %lu", (unsigned long) g_main_thread_id);

        // 检查是否是子线程
        if (current_thread != g_main_thread_id) {
            // 重点关注：如果是子线程执行
            LOGE("===== ATTENTION: This finalization is happening in a SUB-THREAD! =====");
            LOGE("  Thread address: %p", thread);
            LOGE("  Thread ID: %lu", (unsigned long) current_thread);
            LOGE("  ===== DecorView finalization in SUB-THREAD requires IMMEDIATE attention! =====");
        } else {
            // 如果是主线程执行，不重点关注
            LOGE("===== This finalization is happening in the MAIN thread =====");
            LOGE("  Thread address: %p", thread);
            LOGE("  Thread ID: %lu", (unsigned long) current_thread);
        }

        LOGE("===== This should not happen! DecorView should not be finalized! =====");
    }


    // 调用原始函数
    if (orig_art_gc_Heap_AddFinalizerReference != NULL) {
        try {
            SHADOWHOOK_CALL_PREV(proxy_art_gc_Heap_AddFinalizerReference, heap, thread, obj);
            LOGD("Successfully called original art_gc_Heap_AddFinalizerReference");
        } catch (const std::exception &e) {
            LOGE("Exception when calling original art_gc_Heap_AddFinalizerReference: %s", e.what());
        }
    } else {
        LOGE("orig_art_gc_Heap_AddFinalizerReference is NULL");
    }
}


// 代理函数: artQuickToInterpreterBridge
static uint64_t proxy_artQuickToInterpreterBridge(void *method, void *self, void **sp) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_artQuickToInterpreterBridge called %p", method);
    void * proxy = ArtJavaHook::getInstance().onMethodEnter(method);
    if (ArtJavaHook::getInstance().isHookEnabled(method)) {
        method = proxy;
    }
    return SHADOWHOOK_CALL_PREV(proxy_artQuickToInterpreterBridge, method, self, sp);
}

// 代理函数: art::interpreter::DoCall<false>
static bool proxy_art_interpreter_DoCall_f(void *method, void *thread, void *shadow_frame,
                                           const void *instruction, int32_t dex_pc, bool arg6,
                                           void *jvalue) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_art_interpreter_DoCall_f called %p", method);
    void * proxy = ArtJavaHook::getInstance().onMethodEnter(method);
    if (ArtJavaHook::getInstance().isHookEnabled(method)) {
        method = proxy;
    }
    return SHADOWHOOK_CALL_PREV(proxy_art_interpreter_DoCall_f, method, thread, shadow_frame,
                                instruction, dex_pc, arg6, jvalue);
}

// 代理函数: art::interpreter::DoCall<false, false>
static bool proxy_art_interpreter_DoCall_ff(void *method, void *thread, void *shadow_frame,
                                            const void *instruction, int32_t dex_pc, void *jvalue) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_art_interpreter_DoCall_ff called %p", method);
    void * proxy = ArtJavaHook::getInstance().onMethodEnter(method);
    if (ArtJavaHook::getInstance().isHookEnabled(method)) {
        method = proxy;
    }
    return SHADOWHOOK_CALL_PREV(proxy_art_interpreter_DoCall_ff, method, thread, shadow_frame,
                                instruction, dex_pc, jvalue);
}

// 代理函数: art::interpreter::DoCall<false, true>
static bool proxy_art_interpreter_DoCall_ft(void *method, void *thread, void *shadow_frame,
                                            const void *instruction, int32_t dex_pc, void *jvalue) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_art_interpreter_DoCall_ft called %p", method);
    void * proxy = ArtJavaHook::getInstance().onMethodEnter(method);
    if (ArtJavaHook::getInstance().isHookEnabled(proxy)) {
        method = proxy;
    }
    return SHADOWHOOK_CALL_PREV(proxy_art_interpreter_DoCall_ft, method, thread, shadow_frame,
                                instruction, dex_pc, jvalue);
}

// 代理函数: art::interpreter::DoCall<true, false>
static bool proxy_art_interpreter_DoCall_tf(void *method, void *thread, void *shadow_frame,
                                            const void *instruction, int32_t dex_pc, void *jvalue) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_art_interpreter_DoCall_tf called %p", method);
    void * proxy = ArtJavaHook::getInstance().onMethodEnter(method);
    if (ArtJavaHook::getInstance().isHookEnabled(proxy)) {
        method = proxy;
    }
    return SHADOWHOOK_CALL_PREV(proxy_art_interpreter_DoCall_tf, method, thread, shadow_frame,
                                instruction, dex_pc, jvalue);
}

// 代理函数: art::interpreter::DoCall<true, true>
static bool proxy_art_interpreter_DoCall_tt(void *method, void *thread, void *shadow_frame,
                                            const void *instruction, int32_t dex_pc, void *jvalue) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_art_interpreter_DoCall_tt called %p", method);
    void * proxy = ArtJavaHook::getInstance().onMethodEnter(method);
    if (ArtJavaHook::getInstance().isHookEnabled(proxy)) {
        method = proxy;
    }
    return SHADOWHOOK_CALL_PREV(proxy_art_interpreter_DoCall_tt, method, thread, shadow_frame,
                                instruction, dex_pc, jvalue);
}

// 代理函数: art::ArtMethod::Invoke
static void
proxy_art_ArtMethod_Invoke(void *method, void *thread, uint32_t *args, uint32_t args_size,
                           void *jvalue, const char *shorty) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_art_ArtMethod_Invoke called %p", method);
    void * proxy = ArtJavaHook::getInstance().onMethodEnter(method);
    if (ArtJavaHook::getInstance().isHookEnabled(proxy)) {
        method = proxy;
    }
    SHADOWHOOK_CALL_PREV(proxy_art_ArtMethod_Invoke, method, thread, args, args_size, jvalue,
                         shorty);
}

// 代理函数: art::interpreter::EnterInterpreterFromInvoke
static void
proxy_art_interpreter_EnterInterpreterFromInvoke(void *thread, void *method, void *receiver,
                                                 uint32_t *args, void *result, bool arg6) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_art_interpreter_EnterInterpreterFromInvoke called %p", method);
    void * proxy = ArtJavaHook::getInstance().onMethodEnter(method);
    if (ArtJavaHook::getInstance().isHookEnabled(proxy)) {
        method = proxy;
    }
    SHADOWHOOK_CALL_PREV(proxy_art_interpreter_EnterInterpreterFromInvoke, thread, method, receiver,
                         args, result, arg6);
}

// 代理函数: art_quick_invoke_stub
static void
proxy_art_quick_invoke_stub(void *method, uint32_t *args, uint32_t args_size, void *thread,
                            void *jvalue, const char *shorty) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_art_quick_invoke_stub called %p", method);
    void * proxy = ArtJavaHook::getInstance().onMethodEnter(method);
    if (ArtJavaHook::getInstance().isHookEnabled(proxy)) {
        method = proxy;
    }
    SHADOWHOOK_CALL_PREV(proxy_art_quick_invoke_stub, method, args, args_size, thread, jvalue,
                         shorty);
}

// 代理函数: art_quick_invoke_static_stub
static void
proxy_art_quick_invoke_static_stub(void *method, uint32_t *args, uint32_t args_size, void *thread,
                                   void *jvalue, const char *shorty) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_art_quick_invoke_static_stub called %p", method);
    void * proxy = ArtJavaHook::getInstance().onMethodEnter(method);
    if (ArtJavaHook::getInstance().isHookEnabled(proxy)) {
        method = proxy;
    }
    SHADOWHOOK_CALL_PREV(proxy_art_quick_invoke_static_stub, method, args, args_size, thread,
                         jvalue, shorty);
}

// 代理函数: art::instrumentation::Instrumentation::InstallStubsForClass
static void proxy_art_instrumentation_InstallStubsForClass(void *instrumentation, void *klass) {
    SHADOWHOOK_STACK_SCOPE();
    LOGD("proxy_art_instrumentation_InstallStubsForClass called");
    SHADOWHOOK_CALL_PREV(proxy_art_instrumentation_InstallStubsForClass, instrumentation, klass);
}

// 打印 ArtMethod 的详细信息
void PrintArtMethodInfo(ArtMethod *art_method) {
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
        } catch (const std::exception &e) {
            LOGE("Exception when calling art_ArtMethod_PrettyMethod: %s", e.what());
        }
    } else {
        LOGE("art_ArtMethod_PrettyMethod is NULL, cannot get detailed info");
    }

    // 打印入口点信息
    PrintEntryPoint(art_method);
}

void PrintEntryPoint(ArtMethod *art_method) {
    if (art_method == NULL) {
        LOGE("PrintEntryPoint: art_method is NULL");
        return;
    }

    uint64_t entry_point = art_method->ptr_sized_fields_;
    LOGD("ArtMethod entry point: 0x%lx", entry_point);

    // 检查是否等于 art_quick_resolution_trampoline
    if (art_quick_resolution_trampoline != NULL &&
        (uint64_t) art_quick_resolution_trampoline == entry_point) {
        LOGD("Execution type: Interpreted (art_quick_resolution_trampoline)");
        return;
    }

    // 检查是否等于 art_quick_to_interpreter_bridge
    if (art_quick_to_interpreter_bridge != NULL &&
        (uint64_t) art_quick_to_interpreter_bridge == entry_point) {
        LOGD("Execution type: Interpreted (art_quick_to_interpreter_bridge)");
        return;
    }

    // 使用 dladdr 查询内存区域信息
    Dl_info info;
    if (dladdr((void *) entry_point, &info)) {
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
ArtMethod *ToArtMethod(JNIEnv *env, jmethodID methodId) {
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
                ArtMethod *artMethod = reinterpret_cast<ArtMethod *>(art_method_ptr);

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
const char *function_names[] = {
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

// 静态成员初始化
ArtMethodInfo ArtJavaHook::s_artMethodInfo;
void* ArtJavaHook::s_jniCode = NULL;
thread_local std::unordered_map<void*, bool> ArtJavaHook::s_tls_enable_map;

    void ArtJavaHook::setHookEnabled(void *originMethod, bool enabled) {
        s_tls_enable_map[originMethod] = enabled;
    }

    bool ArtJavaHook::isHookEnabled(void *originMethod) {
        return s_tls_enable_map.find(originMethod) != s_tls_enable_map.end() && s_tls_enable_map[originMethod];
    }

ArtMethodInfo ArtJavaHook::calculateArtMethodInfo(JNIEnv* env) {
    ArtMethodInfo info;

    if (env == NULL) {
        LOGE("calculateArtMethodInfo: JNIEnv is NULL");
        return info;
    }

    jclass measureClass = env->FindClass("com/example/stability/MeasureArtMethodSize");
    if (measureClass == NULL) {
        LOGE("Failed to find MeasureArtMethodSize class");
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
        return info;
    }

    // 获取方法 a 和 b 的 ArtMethod 指针
    jmethodID methodA = env->GetStaticMethodID(measureClass, "a", "()V");
    jmethodID methodB = env->GetStaticMethodID(measureClass, "b", "()V");
    ArtMethod* artMethodA = NULL;
    ArtMethod* artMethodB = NULL;

    if (methodA != NULL) {
        artMethodA = ToArtMethod(env, methodA);
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
    }

    if (methodB != NULL) {
        artMethodB = ToArtMethod(env, methodB);
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
    }

    // 计算 ArtMethod 结构大小
    if (artMethodA != NULL && artMethodB != NULL) {
        info.artMethodSize = (char*)artMethodB - (char*)artMethodA;
        LOGD("ArtMethod structure size: %td bytes (0x%tx)", info.artMethodSize, info.artMethodSize);
    } else if (methodA != NULL && methodB != NULL) {
        info.artMethodSize = (char*)methodB - (char*)methodA;
        LOGD("ArtMethod structure size (using jmethodID): %td bytes (0x%tx)", info.artMethodSize, info.artMethodSize);
    } else {
        LOGE("Cannot calculate ArtMethod size: missing methods");
    }

    // 查找 jniCode 偏移量
    jmethodID methodReserve = env->GetStaticMethodID(measureClass, "reserveMethod", "()V");
    if (methodReserve != NULL) {
        ArtMethod* artMethodReserve = ToArtMethod(env, methodReserve);
        if (artMethodReserve != NULL) {
            void* expectedJniCode = s_jniCode;
            const size_t artMethodSize = sizeof(ArtMethod);

            // 以指针宽度为步长搜索
            for (size_t offset = 0; offset < artMethodSize; offset += sizeof(void*)) {
                void* currentValue = *reinterpret_cast<void**>(reinterpret_cast<char*>(artMethodReserve) + offset);
                if (currentValue == expectedJniCode) {
                    info.jniCodeOffset = offset;
                    LOGD("Found jniCode at offset: %td bytes (0x%tx)", info.jniCodeOffset, info.jniCodeOffset);
                    break;
                }
            }

            // 如果没有找到，尝试按字节搜索
            if (info.jniCodeOffset == -1) {
                uint8_t* expectedBytes = reinterpret_cast<uint8_t*>(&expectedJniCode);
                uint8_t* artMethodBytes = reinterpret_cast<uint8_t*>(artMethodReserve);

                for (size_t offset = 0; offset <= artMethodSize - sizeof(void*); offset++) {
                    bool match = true;
                    for (size_t i = 0; i < sizeof(void*); i++) {
                        if (artMethodBytes[offset + i] != expectedBytes[i]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        info.jniCodeOffset = offset;
                        LOGD("Found jniCode by byte search at offset: %td bytes (0x%tx)", info.jniCodeOffset, info.jniCodeOffset);
                        break;
                    }
                }
            }
        }
    }

    env->DeleteLocalRef(measureClass);

    // 验证结果
    info.isValid = (info.artMethodSize > 0 && info.jniCodeOffset >= 0);

    return info;
}

static void *proxy_art_Class_AllocObject(void *thiz, void *thread) {
    SHADOWHOOK_STACK_SCOPE();

    uintptr_t lr = (uintptr_t) __builtin_return_address(0);
    Dl_info info;
    dladdr((void *) lr, &info);
    LOGD("%s %d %s caller %s in %s", __FILE_NAME__, __LINE__, __FUNCTION__, info.dli_sname,
         info.dli_fname);

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

int ArtJavaHook::start(JNIEnv *env, jlong proxyMethodThreadStart, jlong originMethodThreadStart) {
    if (s_hookMethodMap.find((void *)originMethodThreadStart) == s_hookMethodMap.end()) {
        s_hookMethodMap[(void *)originMethodThreadStart].proxyMethod = (void *)proxyMethodThreadStart;
        s_hookMethodMap[(void *)originMethodThreadStart].originMethod = (void *)originMethodThreadStart;

        setHookEnabled((void *)originMethodThreadStart, true);
    }
    LOGD("ArtJavaHook::start - Patching libart.so functions with NativeFunctionPatcher");

    int patch_result;

    patch_result = NativeFunctionPatcher::getInstance().patchFunctionByName(
            "libart.so",
            "_ZN3art11ClassLinker29ValidateSuperClassDescriptorsENS_6HandleINS_6mirror5ClassEEE",
            1);
    if (patch_result == PATCH_SUCCESS) {
        LOGD("Successfully patched _ZN3art11ClassLinker29ValidateSuperClassDescriptorsENS_6HandleINS_6mirror5ClassEEE to return 1");
    } else {
        LOGE("Failed to patch _ZN3art11ClassLinker29ValidateSuperClassDescriptorsENS_6HandleINS_6mirror5ClassEEE, result: %d",
             patch_result);
    }

    patch_result = NativeFunctionPatcher::getInstance().patchFunctionByName(
            "libart.so",
            "_ZN3art41HasSameSignatureWithDifferentClassLoadersEPNS_6ThreadENS_6HandleINS_6mirror5ClassEEES5_PNS_9ArtMethodES7_",
            1);
    if (patch_result == PATCH_SUCCESS) {
        LOGD("Successfully patched _ZN3art41HasSameSignatureWithDifferentClassLoadersEPNS_6ThreadENS_6HandleINS_6mirror5ClassEEES5_PNS_9ArtMethodES7_ to return 1");
    } else {
        LOGE("Failed to patch _ZN3art41HasSameSignatureWithDifferentClassLoadersEPNS_6ThreadENS_6HandleINS_6mirror5ClassEEES5_PNS_9ArtMethodES7_, result: %d",
             patch_result);
    }

    // 初始化主线程 ID（start 方法通常在主线程中调用）
    g_main_thread_id = pthread_self();
    LOGD("Main thread ID: %lu", (unsigned long) g_main_thread_id);

    // 获取并保存 JavaVM 指针
    if (env != NULL) {
        jint result = env->GetJavaVM(&g_jvm);
        if (result == JNI_OK && g_jvm != NULL) {
            LOGD("Successfully got JavaVM pointer: %p", g_jvm);
        } else {
            LOGE("Failed to get JavaVM");
        }
    }

    // 使用 xdl 查找 PrettyClass 符号
    void *handle = xdl_open("libart.so", XDL_DEFAULT);
    if (handle != NULL) {
        size_t symbol_size = 0;
        art_Class_PrettyClass = (art_Class_PrettyClass_func_type_t) xdl_dsym(handle,
                                                                             "_ZN3art6mirror5Class11PrettyClassEv",
                                                                             &symbol_size);
        if (art_Class_PrettyClass != NULL) {
            LOGD("Found _ZN3art6mirror5Class11PrettyClassEv at %p, size=%zu", art_Class_PrettyClass,
                 symbol_size);
        } else {
            LOGE("Failed to find _ZN3art6mirror5Class11PrettyClassEv");
        }

        // 查找 Thread::DumpFromGdb 符号
        symbol_size = 0;
        art_Thread_DumpFromGdb = (art_Thread_DumpFromGdb_func_type_t) xdl_dsym(handle,
                                                                               "_ZNK3art6Thread11DumpFromGdbEv",
                                                                               &symbol_size);
        if (art_Thread_DumpFromGdb != NULL) {
            LOGD("Found _ZNK3art6Thread11DumpFromGdbEv at %p, size=%zu", art_Thread_DumpFromGdb,
                 symbol_size);
        } else {
            LOGE("Failed to find _ZNK3art6Thread11DumpFromGdbEv");
        }

        // 查找 art runtime 单例符号
        symbol_size = 0;
        void *runtime_instance_ptr = xdl_dsym(handle, "_ZN3art7Runtime9instance_E", &symbol_size);
        if (runtime_instance_ptr != NULL) {
            // 读取指针指向的值，即 runtime 实例的地址
            art_runtime_instance = *(void **) runtime_instance_ptr;
            LOGD("Found _ZN3art7Runtime9instance_E at %p, runtime instance at %p, size=%zu",
                 runtime_instance_ptr, art_runtime_instance, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art7Runtime9instance_E");
        }

        if (art_runtime_instance != NULL) {
            // 破解 hidden_api 限制
            *(int *) ((uintptr_t) (art_runtime_instance) + 0x544) = 0;
        }

        // 查找 art_quick_to_interpreter_bridge 符号
        symbol_size = 0;
        art_quick_to_interpreter_bridge = (art_quick_to_interpreter_bridge_func_type_t) xdl_dsym(
                handle, "art_quick_to_interpreter_bridge", &symbol_size);
        if (art_quick_to_interpreter_bridge != NULL) {
            LOGD("Found art_quick_to_interpreter_bridge at %p, size=%zu",
                 art_quick_to_interpreter_bridge, symbol_size);
        } else {
            LOGE("Failed to find art_quick_to_interpreter_bridge");
        }

        // 查找 art_quick_resolution_trampoline 符号
        symbol_size = 0;
        art_quick_resolution_trampoline = (art_quick_resolution_trampoline_func_type_t) xdl_dsym(
                handle, "art_quick_resolution_trampoline", &symbol_size);
        if (art_quick_resolution_trampoline != NULL) {
            LOGD("Found art_quick_resolution_trampoline at %p, size=%zu",
                 art_quick_resolution_trampoline, symbol_size);
        } else {
            LOGE("Failed to find art_quick_resolution_trampoline");
        }

        // 查找 art::ArtMethod::PrettyMethod 符号
        symbol_size = 0;
        art_ArtMethod_PrettyMethod = (art_ArtMethod_PrettyMethod_func_type_t) xdl_dsym(handle,
                                                                                       "_ZN3art9ArtMethod12PrettyMethodEPS0_b",
                                                                                       &symbol_size);
        if (art_ArtMethod_PrettyMethod != NULL) {
            LOGD("Found _ZN3art9ArtMethod12PrettyMethodEPS0_b at %p, size=%zu",
                 art_ArtMethod_PrettyMethod, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art9ArtMethod12PrettyMethodEPS0_b");
        }

        // 查找 art::ArtMethod::Invoke 符号
        symbol_size = 0;
        art_ArtMethod_Invoke = (art_ArtMethod_Invoke_func_type_t) xdl_dsym(handle,
                                                                           "_ZN3art9ArtMethod6InvokeEPNS_6ThreadEPjjPNS_6JValueEPKc",
                                                                           &symbol_size);
        if (art_ArtMethod_Invoke != NULL) {
            LOGD("Found _ZN3art9ArtMethod6InvokeEPNS_6ThreadEPjjPNS_6JValueEPKc at %p, size=%zu",
                 art_ArtMethod_Invoke, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art9ArtMethod6InvokeEPNS_6ThreadEPjjPNS_6JValueEPKc");
        }

        // 查找 art::JNIEnvExt::AddLocalReference 符号
        symbol_size = 0;
        art_JNIEnvExt_AddLocalReference = (art_JNIEnvExt_AddLocalReference_func_type_t) xdl_dsym(handle,
                                                                                              "_ZN3art9JNIEnvExt17AddLocalReferenceIP8_jstringEET_NS_6ObjPtrINS_6mirror6ObjectEEE",
                                                                                              &symbol_size);
        if (art_JNIEnvExt_AddLocalReference != NULL) {
            LOGD("Found _ZN3art9JNIEnvExt17AddLocalReferenceIP8_jstringEET_NS_6ObjPtrINS_6mirror6ObjectEEE at %p, size=%zu",
                 art_JNIEnvExt_AddLocalReference, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art9JNIEnvExt17AddLocalReferenceIP8_jstringEET_NS_6ObjPtrINS_6mirror6ObjectEEE");
        }

        // 查找 art::BoxPrimitive 符号
        symbol_size = 0;
        art_BoxPrimitive = (art_BoxPrimitive_func_type_t) xdl_dsym(handle,
                                                                   "_ZN3art12BoxPrimitiveENS_9Primitive4TypeERKNS_6JValueE",
                                                                   &symbol_size);
        if (art_BoxPrimitive != NULL) {
            LOGD("Found _ZN3art12BoxPrimitiveENS_9Primitive4TypeERKNS_6JValueE at %p, size=%zu",
                 art_BoxPrimitive, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art12BoxPrimitiveENS_9Primitive4TypeERKNS_6JValueE");
        }


        // 查找 art::mirror::Class::SetAccessFlags 符号
        symbol_size = 0;
        art_Class_SetAccessFlags = (art_Class_SetAccessFlags_func_type_t) xdl_dsym(handle,
                                                                                   "_ZN3art6mirror5Class14SetAccessFlagsEj",
                                                                                   &symbol_size);
        if (art_Class_SetAccessFlags != NULL) {
            LOGD("Found _ZN3art6mirror5Class14SetAccessFlagsEj at %p, size=%zu",
                 art_Class_SetAccessFlags, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art6mirror5Class14SetAccessFlagsEj");
        }

        // 查找 art::Thread::DecodeJObject 符号
        symbol_size = 0;
        art_Thread_DecodeJObject = (art_Thread_DecodeJObject_func_type_t) xdl_dsym(handle,
                                                                                   "_ZNK3art6Thread13DecodeJObjectEP8_jobject",
                                                                                   &symbol_size);
        if (art_Thread_DecodeJObject != NULL) {
            LOGD("Found _ZNK3art6Thread13DecodeJObjectEP8_jobject at %p, size=%zu",
                 art_Thread_DecodeJObject, symbol_size);
        } else {
            LOGE("Failed to find _ZNK3art6Thread13DecodeJObjectEP8_jobject");
        }

        // 查找 art::Thread::CurrentFromGdb 符号
        symbol_size = 0;
        art_Thread_CurrentFromGdb = (art_Thread_CurrentFromGdb_func_type_t) xdl_dsym(handle,
                                                                                     "_ZN3art6Thread14CurrentFromGdbEv",
                                                                                     &symbol_size);
        if (art_Thread_CurrentFromGdb != NULL) {
            LOGD("Found _ZN3art6Thread14CurrentFromGdbEv at %p, size=%zu",
                 art_Thread_CurrentFromGdb, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art6Thread14CurrentFromGdbEv");
        }

        // 查找 art::mirror::Object::PrettyTypeOf 符号
        symbol_size = 0;
        art_Object_PrettyTypeOf = (art_Object_PrettyTypeOf_func_type_t) xdl_dsym(handle,
                                                                                 "_ZN3art6mirror6Object12PrettyTypeOfENS_6ObjPtrIS1_EE",
                                                                                 &symbol_size);
        if (art_Object_PrettyTypeOf != NULL) {
            LOGD("Found _ZN3art6mirror6Object12PrettyTypeOfENS_6ObjPtrIS1_EE at %p, size=%zu",
                 art_Object_PrettyTypeOf, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art6mirror6Object12PrettyTypeOfENS_6ObjPtrIS1_EE");
        }

        // 查找 art::DumpNativeStack 符号
        symbol_size = 0;
        art_DumpNativeStack = (art_DumpNativeStack_func_type_t) xdl_dsym(handle,
                                                                         "_ZN3art15DumpNativeStackERNSt3__113basic_ostreamIcNS0_11char_traitsIcEEEEiP12BacktraceMapPKcPNS_9ArtMethodEPvb",
                                                                         &symbol_size);
        if (art_DumpNativeStack != NULL) {
            LOGD("Found _ZN3art15DumpNativeStackERNSt3__113basic_ostreamIcNS0_11char_traitsIcEEEEiP12BacktraceMapPKcPNS_9ArtMethodEPvb at %p, size=%zu",
                 art_DumpNativeStack, symbol_size);
        } else {
            LOGE("Failed to find _ZN3art15DumpNativeStackERNSt3__113basic_ostreamIcNS0_11char_traitsIcEEEEiP12BacktraceMapPKcPNS_9ArtMethodEPvb");
        }

        xdl_close(handle);
    } else {
        LOGE("Failed to open libart.so");
    }

    // 计算 ArtMethod 大小和 jniCode 偏移量
    if (env != NULL) {
        s_artMethodInfo = calculateArtMethodInfo(env);

        if (s_artMethodInfo.isValid) {
            LOGD("ArtMethod info calculated successfully: size=%td, jniCodeOffset=%td",
                 s_artMethodInfo.artMethodSize, s_artMethodInfo.jniCodeOffset);
        } else {
            LOGE("Failed to calculate ArtMethod info");
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
                g_decorViewClass = (jclass) env->NewGlobalRef(decorViewClass);
                if (g_decorViewClass != NULL) {
                    LOGD("Successfully created global reference for DecorView class");
                } else {
                    LOGE("Failed to create global reference for DecorView class");
                }
            }

            // TODO SOA 进入 虚拟机内部 running 状态
            void *thread_ptr = GetCurrentThreadFromGdb();
            void *internalClass = DecodeJObject(thread_ptr, decorViewClass);
            LOGD("DecorView class %p", internalClass);
            uint32_t old_flag = *(uint32_t *) (((uintptr_t) (internalClass) & 0xffffffff) + 0x40);
            uint32_t new_flag = old_flag | 0x80000000;
            art_Class_SetAccessFlags(internalClass, new_flag);

            // 反射打印 Class 对象的字段和偏移量
            LOGD("===== Reflecting Class fields for DecorView ====");

            // 获取 Class 类的 getDeclaredFields 方法
            jclass classClass = env->FindClass("java/lang/Class");
            if (classClass != NULL) {
                jmethodID getDeclaredFieldsMethod = env->GetMethodID(classClass,
                                                                     "getDeclaredFields",
                                                                     "()[Ljava/lang/reflect/Field;");
                if (getDeclaredFieldsMethod != NULL) {
                    // 调用 getDeclaredFields 获取所有字段
                    jobjectArray fieldsArray = (jobjectArray) env->CallObjectMethod(classClass,
                                                                                    getDeclaredFieldsMethod);
                    if (fieldsArray != NULL) {
                        jsize fieldsCount = env->GetArrayLength(fieldsArray);
                        LOGD("Found %d declared fields in DecorView class", fieldsCount);

                        // 获取 Field 类的 getName 方法
                        jclass fieldClass = env->FindClass("java/lang/reflect/Field");
                        if (fieldClass != NULL) {
                            jmethodID getNameMethod = env->GetMethodID(fieldClass, "getName",
                                                                       "()Ljava/lang/String;");
                            if (getNameMethod != NULL) {
                                // 尝试获取 Field 类的 getOffset 方法
                                jmethodID getOffsetMethod = env->GetMethodID(fieldClass,
                                                                             "getOffset", "()I");

                                // 遍历所有字段
                                for (jsize i = 0; i < fieldsCount; i++) {
                                    jobject field = env->GetObjectArrayElement(fieldsArray, i);
                                    if (field != NULL) {
                                        // 获取字段名称
                                        jstring fieldName = (jstring) env->CallObjectMethod(field,
                                                                                            getNameMethod);
                                        if (fieldName != NULL) {
                                            const char *fieldNameCStr = env->GetStringUTFChars(
                                                    fieldName, NULL);
                                            if (fieldNameCStr != NULL) {
                                                // 打印字段名称
                                                LOGD("Field %d: %s", i, fieldNameCStr);

                                                // 尝试调用 getOffset 方法
                                                if (getOffsetMethod != NULL) {
                                                    jint offset = env->CallIntMethod(field,
                                                                                     getOffsetMethod);
                                                    LOGD("  Offset: %d", offset);
                                                }

                                                // 重点关注 accessFlags 字段
                                                if (strcmp(fieldNameCStr, "accessFlags") == 0) {
                                                    LOGD("===== Found accessFlags field! =====");
                                                    if (getOffsetMethod != NULL) {
                                                        jint offset = env->CallIntMethod(field,
                                                                                         getOffsetMethod);
                                                        LOGD("  accessFlags offset: %d", offset);
                                                    }
                                                }

                                                // 重点关注 objectSizeAllocFastPath 字段
                                                if (strcmp(fieldNameCStr,
                                                           "objectSizeAllocFastPath") == 0) {
                                                    LOGD("===== Found objectSizeAllocFastPath field! =====");
                                                    if (getOffsetMethod != NULL) {
                                                        jint offset = env->CallIntMethod(field,
                                                                                         getOffsetMethod);
                                                        LOGD("  objectSizeAllocFastPath offset: %d",
                                                             offset);
                                                    }

                                                    // 尝试获取 Field 类的 setAccessible 方法
                                                    jmethodID setAccessibleMethod = env->GetMethodID(
                                                            fieldClass, "setAccessible", "(Z)V");
                                                    if (setAccessibleMethod != NULL) {
                                                        // 设置字段为可访问
                                                        env->CallVoidMethod(field,
                                                                            setAccessibleMethod,
                                                                            JNI_TRUE);
                                                        LOGD("  Set objectSizeAllocFastPath field as accessible");
                                                    } else {
                                                        LOGE("Failed to find Field.setAccessible method");
                                                    }

                                                    // 尝试获取 Field 类的 getInt 方法
                                                    jmethodID getIntMethod = env->GetMethodID(
                                                            fieldClass, "getInt",
                                                            "(Ljava/lang/Object;)I");
                                                    if (getIntMethod != NULL) {
                                                        // 读取当前值
                                                        jint currentValue = env->CallIntMethod(
                                                                field, getIntMethod,
                                                                decorViewClass);
                                                        LOGD("  Current objectSizeAllocFastPath value: %d",
                                                             currentValue);
                                                    } else {
                                                        LOGE("Failed to find Field.getInt method");
                                                    }

                                                    // 尝试设置字段值为 max int
                                                    jmethodID setIntMethod = env->GetMethodID(
                                                            fieldClass, "setInt",
                                                            "(Ljava/lang/Object;I)V");
                                                    if (setIntMethod != NULL) {
                                                        // 获取 int 的最大值
                                                        jint maxInt = 0x7FFFFFFF; // 2^31 - 1
                                                        // 设置字段值
                                                        env->CallVoidMethod(field, setIntMethod,
                                                                            decorViewClass, maxInt);
                                                        LOGD("  Setting objectSizeAllocFastPath to max int: %d",
                                                             maxInt);

                                                        // 再次读取值，验证是否设置成功
                                                        jmethodID getIntMethod = env->GetMethodID(
                                                                fieldClass, "getInt",
                                                                "(Ljava/lang/Object;)I");
                                                        if (getIntMethod != NULL) {
                                                            jint newValue = env->CallIntMethod(
                                                                    field, getIntMethod,
                                                                    decorViewClass);
                                                            LOGD("  New objectSizeAllocFastPath value: %d",
                                                                 newValue);
                                                            if (newValue == maxInt) {
                                                                LOGD("  SUCCESS: objectSizeAllocFastPath was successfully set to max int!");
                                                            } else {
                                                                LOGE("  FAILED: objectSizeAllocFastPath was not set correctly. Expected: %d, Actual: %d",
                                                                     maxInt, newValue);
                                                            }
                                                        } else {
                                                            LOGE("Failed to find Field.getInt method for verification");
                                                        }
                                                    } else {
                                                        LOGE("Failed to find Field.setInt method");
                                                    }
                                                }


                                                env->ReleaseStringUTFChars(fieldName,
                                                                           fieldNameCStr);
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
            const char *signatures[] = {
                    "(Landroid/content/Context;)V",
                    "(Landroid/content/Context;ILandroid/view/WindowManager$LayoutParams;)V",
                    "(Landroid/content/Context;ILcom/android/internal/policy/PhoneWindow;)V",
                    "(Landroid/content/Context;Landroid/view/WindowManager$LayoutParams;)V",
                    "(Landroid/content/Context;ILcom/android/internal/policy/PhoneWindow;Landroid/view/WindowManager$LayoutParams;)V"
            };

            int signature_count = sizeof(signatures) / sizeof(signatures[0]);

            int found_count = 0;
            for (int i = 0; i < signature_count; i++) {
                jmethodID temp_constructor = env->GetMethodID(decorViewClass, "<init>",
                                                              signatures[i]);
                if (temp_constructor != NULL) {
                    LOGD("Found DecorView constructor with signature %s: %p", signatures[i],
                         temp_constructor);

                    // 打印 ArtMethod 结构的信息
                    // 使用 ToArtMethod 函数获取 ArtMethod 指针
                    ArtMethod *art_method = ToArtMethod(env, temp_constructor);
                    LOGD("ArtMethod address: %p", art_method);
                    if (art_method != NULL) {
                        LOGD("ArtMethod ptr_sized_fields_: 0x%lx", art_method->ptr_sized_fields_);

                        // 打印 ArtMethod 详细信息
                        PrintArtMethodInfo(art_method);

                        // 改解释执行
                        art_method->ptr_sized_fields_ = (uintptr_t) (art_quick_to_interpreter_bridge);
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
            // TODO SOA 进入 虚拟机内部 running 状态
            void *thread_ptr = GetCurrentThreadFromGdb();
            void *internalClass = DecodeJObject(thread_ptr, phoneWindowClass);
            LOGD("DecorView class %p", internalClass);
            uint32_t old_flag = *(uint32_t *) (((uintptr_t) (internalClass) & 0xffffffff) + 0x40);
            uint32_t new_flag = old_flag | 0x80000000;
            art_Class_SetAccessFlags(internalClass, new_flag);


            jmethodID generateDecorMethod = env->GetMethodID(phoneWindowClass, "generateDecor",
                                                             "(I)Lcom/android/internal/policy/DecorView;");
            if (generateDecorMethod != NULL) {
                LOGD("Found PhoneWindow.generateDecor method: %p", generateDecorMethod);

                // 打印 ArtMethod 结构的信息
                ArtMethod *art_method = ToArtMethod(env, generateDecorMethod);
                LOGD("ArtMethod address: %p", art_method);
                if (art_method != NULL) {
                    LOGD("ArtMethod ptr_sized_fields_: 0x%lx", art_method->ptr_sized_fields_);

                    // 打印 ArtMethod 详细信息
                    PrintArtMethodInfo(art_method);

                    // 改解释执行
                    art_method->ptr_sized_fields_ = (uintptr_t) (art_quick_to_interpreter_bridge);
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

    // 查找 PhoneWindow 类的 setContentView(int layoutResID) 方法
    if (env != NULL) {
        jclass phoneWindowClass = env->FindClass("com/android/internal/policy/PhoneWindow");
        if (phoneWindowClass != NULL) {
            jmethodID setContentViewMethod = env->GetMethodID(phoneWindowClass, "setContentView",
                                                              "(I)V");
            if (setContentViewMethod != NULL) {
                LOGD("Found PhoneWindow.setContentView method: %p", setContentViewMethod);

                // 打印 ArtMethod 结构的信息
                ArtMethod *art_method = ToArtMethod(env, setContentViewMethod);
                LOGD("ArtMethod address: %p", art_method);
                if (art_method != NULL) {
                    LOGD("ArtMethod ptr_sized_fields_: 0x%lx", art_method->ptr_sized_fields_);

                    // 打印 ArtMethod 详细信息
                    PrintArtMethodInfo(art_method);
                    uint32_t kAccFinal = 0x0010;  // class, field, method, ic
                    art_method->field2 = art_method->field2 & ~kAccFinal;
                } else {
                    LOGE("Failed to convert setContentViewMethod to ArtMethod");
                }
            } else {
                LOGE("Failed to find PhoneWindow.setContentView method");
            }

            // 查找 PhoneWindow 类的 getDecorView() 方法
            jmethodID getDecorViewMethod = env->GetMethodID(phoneWindowClass, "getDecorView",
                                                            "()Landroid/view/View;");
            if (getDecorViewMethod != NULL) {
                LOGD("Found PhoneWindow.getDecorView method: %p", getDecorViewMethod);

                // 打印 ArtMethod 结构的信息
                ArtMethod *art_method = ToArtMethod(env, getDecorViewMethod);
                LOGD("ArtMethod address: %p", art_method);
                if (art_method != NULL) {
                    LOGD("ArtMethod ptr_sized_fields_: 0x%lx", art_method->ptr_sized_fields_);

                    // 打印 ArtMethod 详细信息
                    PrintArtMethodInfo(art_method);
                    uint32_t kAccFinal = 0x0010;  // class, field, method, ic
                    art_method->field2 = art_method->field2 & ~kAccFinal;
                } else {
                    LOGE("Failed to convert getDecorViewMethod to ArtMethod");
                }
            } else {
                LOGE("Failed to find PhoneWindow.getDecorView method");
            }

            // 反射设置 PhoneWindow 类的 Class 的 objectSizeAllocFastPath 字段为 max int
            jclass classClass = env->FindClass("java/lang/Class");
            if (classClass != NULL) {
                jfieldID objectSizeAllocFastPathField = env->GetFieldID(classClass,
                                                                        "objectSizeAllocFastPath",
                                                                        "I");
                if (objectSizeAllocFastPathField != NULL) {
                    // 保存设置前的值
                    jint originalValue = env->GetIntField(phoneWindowClass,
                                                          objectSizeAllocFastPathField);
                    LOGD("Original PhoneWindow class objectSizeAllocFastPath: %d", originalValue);

                    // 设置为 max int
                    env->SetIntField(phoneWindowClass, objectSizeAllocFastPathField, 0x7fffffff);

                    // 验证设置是否成功
                    jint newValue = env->GetIntField(phoneWindowClass,
                                                     objectSizeAllocFastPathField);
                    LOGD("New PhoneWindow class objectSizeAllocFastPath: %d", newValue);

                    if (newValue == 0x7fffffff) {
                        LOGD("Successfully set PhoneWindow class objectSizeAllocFastPath to max int");
                    } else {
                        LOGE("Failed to set PhoneWindow class objectSizeAllocFastPath");
                    }
                } else {
                    LOGE("Failed to find objectSizeAllocFastPath field in Class");
                }
                env->DeleteLocalRef(classClass);
            } else {
                LOGE("Failed to find java/lang/Class");
            }

            env->DeleteLocalRef(phoneWindowClass);
        } else {
            LOGE("Failed to find PhoneWindow class");
        }

        // 清理可能的 pending 异常
        if (env->ExceptionCheck()) {
            LOGD("Clearing pending exception after finding PhoneWindow methods");
            env->ExceptionClear();
        }
    }



    // 批量 hook 所有函数
    int function_count = sizeof(function_names) / sizeof(function_names[0]);
    for (int i = 0; i < function_count; i++) {
        const char *func_name = function_names[i];
        void *result = shadowhook_hook_sym_name("libart.so", func_name,
                                                (void *) proxy_art_Class_AllocObject,
                                                (void **) &orig_functions[i]);
        if (result != NULL) {
            LOGD("Successfully hooked %s", func_name);
        } else {
            LOGE("Failed to hook %s", func_name);
        }
    }

    // Hook art::gc::Heap::AddFinalizerReference 函数
    void *result = shadowhook_hook_sym_name("libart.so",
                                            "_ZN3art2gc4Heap21AddFinalizerReferenceEPNS_6ThreadEPNS_6ObjPtrINS_6mirror6ObjectEEE",
                                            (void *) proxy_art_gc_Heap_AddFinalizerReference,
                                            (void **) &orig_art_gc_Heap_AddFinalizerReference);
    if (result != NULL) {
        LOGD("Successfully hooked _ZN3art2gc4Heap21AddFinalizerReferenceEPNS_6ThreadEPNS_6ObjPtrINS_6mirror6ObjectEEE");
    } else {
        LOGE("Failed to hook _ZN3art2gc4Heap21AddFinalizerReferenceEPNS_6ThreadEPNS_6ObjPtrINS_6mirror6ObjectEEE");
    }

    // Hook artQuickToInterpreterBridge
    result = shadowhook_hook_sym_name("libart.so", "artQuickToInterpreterBridge",
                                      (void *) proxy_artQuickToInterpreterBridge,
                                      (void **) &orig_artQuickToInterpreterBridge);
    if (result != NULL) {
        LOGD("Successfully hooked artQuickToInterpreterBridge");
    } else {
        LOGE("Failed to hook artQuickToInterpreterBridge");
    }

    // Hook art::interpreter::DoCall<false>
    result = shadowhook_hook_sym_name("libart.so",
                                      "_ZN3art11interpreter6DoCallILb0EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtbPNS_6JValueE",
                                      (void *) proxy_art_interpreter_DoCall_f,
                                      (void **) &orig_art_interpreter_DoCall_f);
    if (result != NULL) {
        LOGD("Successfully hooked _ZN3art11interpreter6DoCallILb0EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtbPNS_6JValueE");
    } else {
        LOGE("Failed to hook _ZN3art11interpreter6DoCallILb0EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtbPNS_6JValueE");
    }

    // Hook art::interpreter::DoCall<false, false>
    result = shadowhook_hook_sym_name("libart.so",
                                      "_ZN3art11interpreter6DoCallILb0ELb0EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE",
                                      (void *) proxy_art_interpreter_DoCall_ff,
                                      (void **) &orig_art_interpreter_DoCall_ff);
    if (result != NULL) {
        LOGD("Successfully hooked _ZN3art11interpreter6DoCallILb0ELb0EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE");
    } else {
        LOGE("Failed to hook _ZN3art11interpreter6DoCallILb0ELb0EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE");
    }

    // Hook art::interpreter::DoCall<false, true>
    result = shadowhook_hook_sym_name("libart.so",
                                      "_ZN3art11interpreter6DoCallILb0ELb1EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE",
                                      (void *) proxy_art_interpreter_DoCall_ft,
                                      (void **) &orig_art_interpreter_DoCall_ft);
    if (result != NULL) {
        LOGD("Successfully hooked _ZN3art11interpreter6DoCallILb0ELb1EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE");
    } else {
        LOGE("Failed to hook _ZN3art11interpreter6DoCallILb0ELb1EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE");
    }

    // Hook art::interpreter::DoCall<true, false>
    result = shadowhook_hook_sym_name("libart.so",
                                      "_ZN3art11interpreter6DoCallILb1ELb0EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE",
                                      (void *) proxy_art_interpreter_DoCall_tf,
                                      (void **) &orig_art_interpreter_DoCall_tf);
    if (result != NULL) {
        LOGD("Successfully hooked _ZN3art11interpreter6DoCallILb1ELb0EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE");
    } else {
        LOGE("Failed to hook _ZN3art11interpreter6DoCallILb1ELb0EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE");
    }

    // Hook art::interpreter::DoCall<true, true>
    result = shadowhook_hook_sym_name("libart.so",
                                      "_ZN3art11interpreter6DoCallILb1ELb1EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE",
                                      (void *) proxy_art_interpreter_DoCall_tt,
                                      (void **) &orig_art_interpreter_DoCall_tt);
    if (result != NULL) {
        LOGD("Successfully hooked _ZN3art11interpreter6DoCallILb1ELb1EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE");
    } else {
        LOGE("Failed to hook _ZN3art11interpreter6DoCallILb1ELb1EEEbPNS_9ArtMethodEPNS_6ThreadERNS_11ShadowFrameEPKNS_11InstructionEtPNS_6JValueE");
    }

    // Hook art::ArtMethod::Invoke
    result = shadowhook_hook_sym_name("libart.so",
                                      "_ZN3art9ArtMethod6InvokeEPNS_6ThreadEPjjPNS_6JValueEPKc",
                                      (void *) proxy_art_ArtMethod_Invoke,
                                      (void **) &orig_art_ArtMethod_Invoke);
    if (result != NULL) {
        LOGD("Successfully hooked _ZN3art9ArtMethod6InvokeEPNS_6ThreadEPjjPNS_6JValueEPKc");
    } else {
        LOGE("Failed to hook _ZN3art9ArtMethod6InvokeEPNS_6ThreadEPjjPNS_6JValueEPKc");
    }

    // Hook art::interpreter::EnterInterpreterFromInvoke
    result = shadowhook_hook_sym_name("libart.so",
                                      "_ZN3art11interpreter26EnterInterpreterFromInvokeEPNS_6ThreadEPNS_9ArtMethodENS_6ObjPtrINS_6mirror6ObjectEEEPjPNS_6JValueEb",
                                      (void *) proxy_art_interpreter_EnterInterpreterFromInvoke,
                                      (void **) &orig_art_interpreter_EnterInterpreterFromInvoke);
    if (result != NULL) {
        LOGD("Successfully hooked _ZN3art11interpreter26EnterInterpreterFromInvokeEPNS_6ThreadEPNS_9ArtMethodENS_6ObjPtrINS_6mirror6ObjectEEEPjPNS_6JValueEb");
    } else {
        LOGE("Failed to hook _ZN3art11interpreter26EnterInterpreterFromInvokeEPNS_6ThreadEPNS_9ArtMethodENS_6ObjPtrINS_6mirror6ObjectEEEPjPNS_6JValueEb");
    }

    // Hook art_quick_invoke_stub
    result = shadowhook_hook_sym_name("libart.so", "art_quick_invoke_stub",
                                      (void *) proxy_art_quick_invoke_stub,
                                      (void **) &orig_art_quick_invoke_stub);
    if (result != NULL) {
        LOGD("Successfully hooked art_quick_invoke_stub");
    } else {
        LOGE("Failed to hook art_quick_invoke_stub");
    }

//     Hook art_quick_invoke_static_stub
    result = shadowhook_hook_sym_name("libart.so", "art_quick_invoke_static_stub",
                                      (void *) proxy_art_quick_invoke_static_stub,
                                      (void **) &orig_art_quick_invoke_static_stub);
    if (result != NULL) {
        LOGD("Successfully hooked art_quick_invoke_static_stub");
    } else {
        LOGE("Failed to hook art_quick_invoke_static_stub");
    }

    // Hook art::instrumentation::Instrumentation::InstallStubsForClass
    result = shadowhook_hook_sym_name("libart.so",
                                      "_ZN3art15instrumentation15Instrumentation20InstallStubsForClassEPNS_6mirror5ClassE",
                                      (void *) proxy_art_instrumentation_InstallStubsForClass,
                                      (void **) &orig_art_instrumentation_InstallStubsForClass);
    if (result != NULL) {
        LOGD("Successfully hooked _ZN3art15instrumentation15Instrumentation20InstallStubsForClassEPNS_6mirror5ClassE");
    } else {
        LOGE("Failed to hook _ZN3art15instrumentation15Instrumentation20InstallStubsForClassEPNS_6mirror5ClassE");
    }

    return 0;
}

void
ArtJavaHook::hookJavaNativeMethod(const char *className, const char *methodName, const char *sig,
                                  void *proxyMethod, void **originMethod) {
    // 1. 参数验证
    if (className == NULL || methodName == NULL || sig == NULL || proxyMethod == NULL || originMethod == NULL) {
        LOGE("hookJavaNativeMethod: invalid parameters");
        return;
    }

    // 2. 获取 JNIEnv
    JNIEnv* env = NULL;
    jint result = g_jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if (result != JNI_OK || env == NULL) {
        LOGE("hookJavaNativeMethod: failed to get JNIEnv");
        return;
    }

    // 3. 查找目标类
    jclass targetClass = env->FindClass(className);
    if (targetClass == NULL) {
        LOGE("hookJavaNativeMethod: failed to find class %s", className);
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
        return;
    }

    // 4. 获取方法 ID
    // 需要判断是实例方法还是静态方法（根据签名首字符判断）
    jmethodID methodId = NULL;

    methodId = env->GetStaticMethodID(targetClass, methodName, sig);
    if (methodId == NULL) {
        LOGE("hookJavaNativeMethod: failed to find method %s%s in class %s by GetStaticMethodID", methodName, sig, className);
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
        methodId = env->GetMethodID(targetClass, methodName, sig);
    }

    if (methodId == NULL) {
        LOGE("hookJavaNativeMethod: failed to find method %s%s in class %s", methodName, sig, className);
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
        env->DeleteLocalRef(targetClass);
        return;
    }

    // 5. 转换为 ArtMethod 指针
    ArtMethod* artMethod = ToArtMethod(env, methodId);
    if (artMethod == NULL) {
        LOGE("hookJavaNativeMethod: failed to convert methodId to ArtMethod");
        env->DeleteLocalRef(targetClass);
        return;
    }

    // 6. 检查 jniCodeOffset 是否有效
    if (s_artMethodInfo.jniCodeOffset < 0) {
        LOGE("hookJavaNativeMethod: jniCodeOffset is not initialized");
        env->DeleteLocalRef(targetClass);
        return;
    }

    // 7. 读取原 jniCode
    void** jniCodePtr = reinterpret_cast<void**>(
        reinterpret_cast<char*>(artMethod) + s_artMethodInfo.jniCodeOffset);
    *originMethod = *jniCodePtr;
    LOGD("hookJavaNativeMethod: origin jniCode at %p: %p", jniCodePtr, *originMethod);

    // 8. 写入代理函数
    *jniCodePtr = proxyMethod;
    LOGD("hookJavaNativeMethod: hooked %s.%s%s, proxy=%p, origin=%p",
         className, methodName, sig, proxyMethod, *originMethod);

    env->DeleteLocalRef(targetClass);
}

void *ArtJavaHook::onMethodEnter(void *originMethod) {
    void * result = nullptr;
    // 检查是否需要替换 method
    if (s_hookMethodMap.find(originMethod) != s_hookMethodMap.end()) {
        result = s_hookMethodMap[originMethod].proxyMethod;
        LOGD("onMethodEnter: %p -> %p", originMethod, result);
    }
    return result;
}

void ArtJavaHook::deopt(void *artMethod) {
    if (artMethod == NULL) {
        LOGE("deopt: invalid artMethod");
        return;
    }
    ArtMethod* artMethodPtr = reinterpret_cast<ArtMethod*>(artMethod);
    if (artMethodPtr == NULL) {
        LOGE("deopt: invalid artMethod");
        return;
    }
    // art::instrumentation::Instrumentation::DeoptimizeMethod(artMethodPtr);
    // 改解释执行
    artMethodPtr->ptr_sized_fields_ = (uintptr_t) (art_quick_to_interpreter_bridge);             
    LOGD("deopt: %p", artMethodPtr);
}

void ArtJavaHook::invokeArtMethod(void* artMethod, void* thread, uint32_t* args,
                                   uint32_t argsSize, void* result, const char* shorty) {
    if (art_ArtMethod_Invoke == NULL) {
        LOGE("invokeArtMethod: art_ArtMethod_Invoke is NULL");
        return;
    }
    if (artMethod == NULL) {
        LOGE("invokeArtMethod: invalid artMethod");
        return;
    }
    try {
        art_ArtMethod_Invoke(artMethod, thread, args, argsSize, result, shorty);
        LOGD("invokeArtMethod: called successfully for artMethod=%p", artMethod);
    } catch (const std::exception &e) {
        LOGE("invokeArtMethod: exception occurred: %s", e.what());
    }
}

jobject ArtJavaHook::addLocalReference(void* jniEnvExt, void* localRef) {
    if (art_JNIEnvExt_AddLocalReference == NULL) {
        LOGE("addLocalReference: art_JNIEnvExt_AddLocalReference is NULL");
        return NULL;
    }
    if (jniEnvExt == NULL) {
        LOGE("addLocalReference: invalid jniEnvExt");
        return NULL;
    }
    if (localRef == NULL) {
        LOGE("addLocalReference: invalid localRef");
        return NULL;
    }
    try {
        jobject result = art_JNIEnvExt_AddLocalReference(jniEnvExt, localRef);
        LOGD("addLocalReference: called successfully, localRef=%p, result=%p", localRef, result);
        return result;
    } catch (const std::exception &e) {
        LOGE("addLocalReference: exception occurred: %s", e.what());
        return NULL;
    }
}

void* ArtJavaHook::boxPrimitive(int type, void* jValue) {
    if (art_BoxPrimitive == NULL) {
        LOGE("boxPrimitive: art_BoxPrimitive is NULL");
        return NULL;
    }
    if (jValue == NULL) {
        LOGE("boxPrimitive: invalid jValue");
        return NULL;
    }
    try {
        void* result = art_BoxPrimitive(type, jValue);
        LOGD("boxPrimitive: called successfully, type=%d, result=%p", type, result);
        return result;
    } catch (const std::exception &e) {
        LOGE("boxPrimitive: exception occurred: %s", e.what());
        return NULL;
    }
}

class Primitive {
public:
    enum Type {
        kPrimNot = 0,
        kPrimBoolean,
        kPrimByte,
        kPrimChar,
        kPrimShort,
        kPrimInt,
        kPrimLong,
        kPrimFloat,
        kPrimDouble,
        kPrimVoid,
        kPrimLast = kPrimVoid
    };

    static constexpr Type GetType(char type) {
        switch (type) {
            case 'B':
                return kPrimByte;
            case 'C':
                return kPrimChar;
            case 'D':
                return kPrimDouble;
            case 'F':
                return kPrimFloat;
            case 'I':
                return kPrimInt;
            case 'J':
                return kPrimLong;
            case 'S':
                return kPrimShort;
            case 'Z':
                return kPrimBoolean;
            case 'V':
                return kPrimVoid;
            default:
                return kPrimNot;
        }
    }
};

jobject ArtJavaHook::Method_Invoke(const char * shorty,
                             JNIEnv* env,
                             jobject javaMethod,
                             jobject javaReceiver,
                             jobjectArray javaArgs) {
    ArgArray argArray(shorty, strlen(shorty));
    jmethodID m = env->FromReflectedMethod(javaMethod);
    argArray.BuildArgArrayFromObjectArray(javaReceiver, javaArgs, m, GetCurrentThreadFromGdb(), env);
    jvalue result;
    ArtJavaHook::invokeArtMethod(m,
                                 GetCurrentThreadFromGdb(),
                                 argArray.GetArray(),
                                 argArray.GetNumBytes(),
                                 &result, shorty);
    Primitive::Type type = Primitive::GetType(shorty[0]);
    void *primitive = ArtJavaHook::boxPrimitive(type, &result);
    if (primitive) {
        return ArtJavaHook::addLocalReference(env, primitive);
    }
    return nullptr;
}
