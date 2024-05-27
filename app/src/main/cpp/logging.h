/*
 * Copyright (C) 2024 RollW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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