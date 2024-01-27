#ifndef SOUNDSOURCE_LOGGING_H
#define SOUNDSOURCE_LOGGING_H

#include <android/log.h>

#if 1
#ifndef GLOBAL_TAG
#define GLOBAL_TAG  "SoundSourceNative"
#endif

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, GLOBAL_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, GLOBAL_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, GLOBAL_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, GLOBAL_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, GLOBAL_TAG, __VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL, GLOBAL_TAG, __VA_ARGS__)

#define ASSERT(cond, ...) if (!(cond)) {__android_log_assert(#cond, MODULE_NAME, __VA_ARGS__);}
#else
#define LOGV(...)
#define LOGD(...)
#define LOGI(...)
#define LOGW(...)
#define LOGE(...)
#define LOGF(...)
#define ASSERT(cond, ...)
#endif
#endif //SOUNDSOURCE_LOGGING_H