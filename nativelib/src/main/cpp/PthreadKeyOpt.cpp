//
// Created by liguang on 2026/4/3.
//

#include "PthreadKeyOpt.h"

#include <pthread.h>
#include <stdatomic.h>
#include <android/log.h>
#include "xdl.h"
#include "shadowhook.h"

#define LOG_TAG "PthreadKeyOpt"
#define LOGD(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

typedef void (*key_destructor_t)(void*);

#define SEQ_KEY_IN_USE_BIT     0

#define SEQ_INCREMENT_STEP  (1 << SEQ_KEY_IN_USE_BIT)

// pthread_key_internal_t records the use of each pthread key slot:
//   seq records the state of the slot.
//      bit 0 is 1 when the key is in use, 0 when it is unused. Each time we create or delete the
//      pthread key in the slot, we increse the seq by 1 (which inverts bit 0). The reason to use
//      a sequence number instead of a boolean value here is that when the key slot is deleted and
//      reused for a new key, pthread_getspecific will not return stale data.
//   key_destructor records the destructor called at thread exit.
struct pthread_key_internal_t {
    atomic_uintptr_t seq;
    atomic_uintptr_t key_destructor;
};

#define BIONIC_PTHREAD_KEY_COUNT 512

static pthread_key_internal_t key_map[BIONIC_PTHREAD_KEY_COUNT];

static inline bool SeqOfKeyInUse(uintptr_t seq) {
    return seq & (1 << SEQ_KEY_IN_USE_BIT);
}

#define KEY_VALID_FLAG (1 << 31)
#define KEY_VALID_FLAG2 (1 << 30)

static_assert(sizeof(pthread_key_t) == sizeof(int) && static_cast<pthread_key_t>(-1) < 0,
              "pthread_key_t should be typedef to int");

static inline bool KeyInValidRange(pthread_key_t key) {
    // key < 0 means bit 31 is set.
    // Then key < (2^31 | BIONIC_PTHREAD_KEY_COUNT) means the index part of key < BIONIC_PTHREAD_KEY_COUNT.
    return (key < (KEY_VALID_FLAG | KEY_VALID_FLAG2 | BIONIC_PTHREAD_KEY_COUNT));
}

static inline bool IsCustomKey(pthread_key_t key) {
    return key & KEY_VALID_FLAG2;
}

class pthread_key_data_t {
public:
    uintptr_t seq; // Use uintptr_t just for alignment, as we use pointer below.
    void* data;
};

typedef int (*pthread_key_create_func_type_t)(pthread_key_t*, void (*)(void*));
typedef int (*pthread_key_delete_func_type_t)(pthread_key_t);
typedef void* (*pthread_getspecific_func_type_t)(pthread_key_t);
typedef int (*pthread_setspecific_func_type_t)(pthread_key_t, const void*);

void * orig_pthread_key_create = NULL;
void * orig_pthread_key_delete = NULL;
void * orig_pthread_getspecific = NULL;
void * orig_pthread_setspecific = NULL;

pthread_key_t gKey;
static inline pthread_key_data_t* get_thread_key_data() {
//    return __get_bionic_tls().key_data;
    auto thread_key_data = ((pthread_getspecific_func_type_t)(orig_pthread_getspecific))(gKey);
    if (!thread_key_data) {
        size_t byteCount = BIONIC_PTHREAD_KEY_COUNT * sizeof(pthread_key_data_t);
        thread_key_data = malloc(byteCount);
        memset(thread_key_data, 0x0, byteCount);
        ((pthread_setspecific_func_type_t)orig_pthread_setspecific)(gKey, thread_key_data);
    }
    return (pthread_key_data_t*)(thread_key_data);
}

static int proxy_pthread_key_create(pthread_key_t* key, void (*key_destructor)(void*)) {
    SHADOWHOOK_STACK_SCOPE();

    uintptr_t  lr = (uintptr_t)__builtin_return_address(0);
    Dl_info info;
    dladdr((void *)lr, &info);
    LOGD("%s %d %s caller %s in %s", __FILE_NAME__, __LINE__, __FUNCTION__, info.dli_sname, info.dli_fname);

    int ret = SHADOWHOOK_CALL_PREV(proxy_pthread_key_create, key, key_destructor);
    if (!ret) {
        return ret;
    }

    for (size_t i = 0; i < BIONIC_PTHREAD_KEY_COUNT; ++i) {
        uintptr_t seq = atomic_load_explicit(&key_map[i].seq, memory_order_relaxed);
        while (!SeqOfKeyInUse(seq)) {
            if (atomic_compare_exchange_weak(&key_map[i].seq, &seq, seq + SEQ_INCREMENT_STEP)) {
                atomic_store(&key_map[i].key_destructor, reinterpret_cast<uintptr_t>(key_destructor));
                *key = i | KEY_VALID_FLAG | KEY_VALID_FLAG2;
                return 0;
            }
        }
    }
    return EAGAIN;
}

static int proxy_pthread_key_delete(pthread_key_t key) {
    SHADOWHOOK_STACK_SCOPE();

    uintptr_t  lr = (uintptr_t)__builtin_return_address(0);
    Dl_info info;
    dladdr((void *)lr, &info);
    LOGD("%s %d %s caller %s in %s", __FILE_NAME__, __LINE__, __FUNCTION__, info.dli_sname, info.dli_fname);

    if (!IsCustomKey(key)) {
        return SHADOWHOOK_CALL_PREV(proxy_pthread_key_delete, key);
    }

    if (__predict_false(!KeyInValidRange(key))) {
        return EINVAL;
    }
    key &= ~(KEY_VALID_FLAG | KEY_VALID_FLAG2);
    // Increase seq to invalidate values in all threads.
    uintptr_t seq = atomic_load_explicit(&key_map[key].seq, memory_order_relaxed);
    if (SeqOfKeyInUse(seq)) {
        if (atomic_compare_exchange_strong(&key_map[key].seq, &seq, seq + SEQ_INCREMENT_STEP)) {
            return 0;
        }
    }
    return EINVAL;
}

static void* proxy_pthread_getspecific(pthread_key_t key) {
    SHADOWHOOK_STACK_SCOPE();
//    uintptr_t  lr = (uintptr_t)__builtin_return_address(0);
//    Dl_info info;
//    dladdr((void *)lr, &info);
//    LOGD("%s %d %s caller %s in %s", __FILE_NAME__, __LINE__, __FUNCTION__, info.dli_sname, info.dli_fname);
    if (!IsCustomKey(key)) {
        return SHADOWHOOK_CALL_PREV(proxy_pthread_getspecific, key);
    }

    if (__predict_false(!KeyInValidRange(key))) {
        return nullptr;
    }
    key &= ~(KEY_VALID_FLAG | KEY_VALID_FLAG2);
    uintptr_t seq = atomic_load_explicit(&key_map[key].seq, memory_order_relaxed);
    pthread_key_data_t* data = &get_thread_key_data()[key];
    // It is the user's responsibility to synchronize between the creation and use of pthread keys,
    // so we use memory_order_relaxed when checking the sequence number.
    if (__predict_true(SeqOfKeyInUse(seq) && data->seq == seq)) {
        return data->data;
    }
    // We arrive here when the current thread holds the seq of a deleted pthread key.
    // The data is for the deleted pthread key, and should be cleared.
    data->data = nullptr;
    return nullptr;
}

static int proxy_pthread_setspecific(pthread_key_t key, const void* value) {
    SHADOWHOOK_STACK_SCOPE();
//    uintptr_t  lr = (uintptr_t)__builtin_return_address(0);
//    Dl_info info;
//    dladdr((void *)lr, &info);
//    LOGD("%s %d %s caller %s in %s", __FILE_NAME__, __LINE__, __FUNCTION__, info.dli_sname, info.dli_fname);
    if (!IsCustomKey(key)) {
        return SHADOWHOOK_CALL_PREV(proxy_pthread_setspecific, key, value);
    }

    if (__predict_false(!KeyInValidRange(key))) {
        return EINVAL;
    }
    key &= ~(KEY_VALID_FLAG | KEY_VALID_FLAG2);
    uintptr_t seq = atomic_load_explicit(&key_map[key].seq, memory_order_relaxed);
    if (__predict_true(SeqOfKeyInUse(seq))) {
        pthread_key_data_t* data = &get_thread_key_data()[key];
        data->seq = seq;
        data->data = const_cast<void*>(value);
        return 0;
    }
    return EINVAL;
}

// Called from pthread_exit() to remove all pthread keys. This must call the destructor of
// all keys that have a non-NULL data value and a non-NULL destructor.
__LIBC_HIDDEN__ void pthread_key_clean_all(pthread_key_data_t* key_data2) {
    // Because destructors can do funky things like deleting/creating other keys,
    // we need to implement this in a loop.
    pthread_key_data_t* key_data = key_data2;
    for (size_t rounds = PTHREAD_DESTRUCTOR_ITERATIONS; rounds > 0; --rounds) {
        size_t called_destructor_count = 0;
        for (size_t i = 0; i < BIONIC_PTHREAD_KEY_COUNT; ++i) {
            uintptr_t seq = atomic_load_explicit(&key_map[i].seq, memory_order_relaxed);
            if (SeqOfKeyInUse(seq) && seq == key_data[i].seq) {
                // POSIX explicitly says that the destructor is only called if the
                // thread has a non-null value for the key.
                if (key_data[i].data == nullptr) {
                    continue;
                }

                // Other threads can call pthread_key_delete()/pthread_key_create()
                // while this thread is exiting, so we need to ensure we read the right
                // key_destructor.
                // We can rely on a user-established happens-before relationship between the creation and
                // use of a pthread key to ensure that we're not getting an earlier key_destructor.
                // To avoid using the key_destructor of the newly created key in the same slot, we need to
                // recheck the sequence number after reading key_destructor. As a result, we either see the
                // right key_destructor, or the sequence number must have changed when we reread it below.
                key_destructor_t key_destructor = reinterpret_cast<key_destructor_t>(
                        atomic_load_explicit(&key_map[i].key_destructor, memory_order_relaxed));
                if (key_destructor == nullptr) {
                    continue;
                }
                atomic_thread_fence(memory_order_acquire);
                if (atomic_load_explicit(&key_map[i].seq, memory_order_relaxed) != seq) {
                    continue;
                }

                // We need to clear the key data now, this will prevent the destructor (or a later one)
                // from seeing the old value if it calls pthread_getspecific().
                // We don't do this if 'key_destructor == NULL' just in case another destructor
                // function is responsible for manually releasing the corresponding data.
                void* data = key_data[i].data;
                key_data[i].data = nullptr;
                (*key_destructor)(data);
                ++called_destructor_count;
            }
        }

        // If we didn't call any destructors, there is no need to check the pthread keys again.
        if (called_destructor_count == 0) {
            break;
        }
    }
}

void g_key_destructor(void* data) {
    // 析构
    pthread_key_clean_all((pthread_key_data_t*)(data));
    // 释放内存
    if (data) {
        free(data);
    }
}

int PthreadKeyOpt::start() {
    if (pthread_key_create(&gKey, g_key_destructor)) {
        return -1;
    }

    void * h = xdl_open("libc.so", XDL_DEFAULT);
    size_t symbol_size = 0;
    void *sym = xdl_dsym(h, "_ZL7key_map", &symbol_size);
    if (sym != nullptr) {
        size_t count = symbol_size / sizeof(pthread_key_internal_t);
        LOGD("_ZL7key_map: addr=%p, size=%zu, count=%zu", sym, symbol_size, count);
    }
    xdl_close(h);

    shadowhook_hook_sym_name("libc.so", "pthread_key_create", (void *)proxy_pthread_key_create, (void **)&orig_pthread_key_create);
    shadowhook_hook_sym_name("libc.so", "pthread_key_delete", (void *)proxy_pthread_key_delete, (void **)&orig_pthread_key_delete);
    shadowhook_hook_sym_name("libc.so", "pthread_getspecific", (void *)proxy_pthread_getspecific, (void **)&orig_pthread_getspecific);
    shadowhook_hook_sym_name("libc.so", "pthread_setspecific", (void *)proxy_pthread_setspecific, (void **)&orig_pthread_setspecific);
    return 0;
}
