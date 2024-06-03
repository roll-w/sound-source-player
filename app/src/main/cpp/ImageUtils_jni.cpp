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

#include <iostream>
#include <jni.h>
#include <string>
#include <unistd.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <android/bitmap.h>

#include "logging.h"
#include "image/image.h"

using namespace SoundSource::Image;

extern "C"
JNIEXPORT jintArray JNICALL
Java_tech_rollw_player_util_ImageUtils_blurPixels(
        JNIEnv *env, jobject thiz, jintArray image,
        jint w, jint h, jint radius) {
    jint *pixels;
    pixels = (env)->GetIntArrayElements(image, nullptr);
    if (pixels == nullptr) {
        LOGD("Input pixels is null.");
        return nullptr;
    }

    // TODO: blur and return values
    return env->NewIntArray(0);
}

extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_util_ImageUtils_blurBitmap(
        JNIEnv *env, jobject thiz,
        jobject bitmap, jint radius) {
    AndroidBitmapInfo info;
    void *pixels;

    // Get image Info
    if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGD("AndroidBitmap_getInfo failed!");
        return;
    }

    // Check image
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
        info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        LOGD("Only support ANDROID_BITMAP_FORMAT_RGBA_8888 and ANDROID_BITMAP_FORMAT_RGB_565");
        return;
    }

    // Lock all image pixels
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGD("AndroidBitmap_lockPixels failed!");
        return;
    }
    int h = info.height;
    int w = info.width;

    if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        pixels = ImageProcessor::BlurArgb8888((int32_t *) (pixels), w, h, radius);
    } else if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
        pixels = ImageProcessor::BlurRgb565((int16_t *) (pixels), w, h, radius);
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}
