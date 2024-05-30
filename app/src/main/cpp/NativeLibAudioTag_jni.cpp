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

#include "logging.h"

#include "taglib/taglib/tag.h"
#include "taglib/taglib/flac/flacfile.h"
#include "taglib/taglib/ogg/xiphcomment.h"
#include "taglib/taglib/fileref.h"
#include "tfilestream.h"
#include "tpropertymap.h"

#include <tags/tags.h>
#include <image/image.h>

using namespace std;
using namespace TagLib;
using namespace SoundSource;

void throwAccessorNullException(JNIEnv *env) {
    env->ThrowNew(env->FindClass("java/lang/NullPointerException"), "accessor is null");
}

jstring toJString(JNIEnv *env, String string) {
    auto cString = string.toCString(true);
    if (cString == nullptr) {
        return nullptr;
    }
    return env->NewStringUTF(cString);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_openFile(JNIEnv *env, jobject thiz,
                                                            jint file_descriptor,
                                                            jboolean jreadonly) {
    bool readonly = jreadonly;
    AudioTagAccessor *accessor = new AudioTagAccessor(file_descriptor, readonly);
    if (accessor->isNull()) {
        env->ThrowNew(
                env->FindClass("java/io/IOException"),
                "Cannot open native TagAccessor with given file descriptor."
        );
        return 0;
    }
    return (jlong) accessor;
}

extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_closeFile(JNIEnv *env,
                                                             jobject thiz,
                                                             jlong accessorRef) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    if (accessor == nullptr) {
        throwAccessorNullException(env);
        return;
    }

    accessor->close();
    delete accessor;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_getTagField(JNIEnv *env,
                                                               jobject thiz,
                                                               jlong accessorRef,
                                                               jstring jTagField) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    if (accessor == nullptr) {
        throwAccessorNullException(env);
        return nullptr;
    }
    Tag *t = accessor->tag();
    auto fieldName = env->GetStringUTFChars(jTagField, 0);

    if (t == nullptr) {
        LOGD("Tag is null of accessor*(=%ld), field=%s", accessorRef, fieldName);
        return nullptr;
    }

    PropertyMap propertyMap = t->properties();
    String tagField(fieldName);

    auto field = propertyMap[tagField];
    if (field.isEmpty()) {
        return nullptr;
    }
    auto cString = field.front().toCString(true);
    return env->NewStringUTF(cString);
}

jbyteArray toJByteArray(JNIEnv *env, const ByteVector &byteVector) {
    if (byteVector.isEmpty()) {
        return nullptr;
    }
    auto size = byteVector.size();
    jbyteArray jbytes = env->NewByteArray(size);
    env->SetByteArrayRegion(jbytes, 0, size, (jbyte *) byteVector.data());
    return jbytes;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_getArtwork(JNIEnv *env,
                                                              jobject thiz,
                                                              jlong accessorRef,
                                                              jboolean includeData) {
    // FIXME: cannot read picture from some flac files
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    if (accessor == nullptr) {
        throwAccessorNullException(env);
        return nullptr;
    }

    File *f = accessor->fileRef()->file();
    const List<VariantMap> &pictures = f->complexProperties("PICTURE");
    if (pictures.isEmpty()) {
        return nullptr;
    }

    auto map = pictures.front();
    if (map.isEmpty()) {
        return nullptr;
    }

    auto description = toJString(env, map["description"].toString());
    auto type = toJString(env, map["pictureType"].toString());

    ByteVector byteVector = map["data"].toByteVector();
    const void *imageRaw = byteVector.data();

    Image::ImageInfo imageInfo = Image::getImageInfo(
            imageRaw, byteVector.size()
    );

    jbyteArray jbytesData = nullptr;

    if (includeData) {
        jbytesData = toJByteArray(env, byteVector);
    }

    jclass artworkClass = env->FindClass(
            "tech/rollw/player/audio/tag/NativeLibAudioTag$NativeArtwork");

    jmethodID constructor = env->GetMethodID(
            artworkClass,
            "<init>", "(Ljava/lang/String;[BIIJLjava/lang/String;Ljava/lang/String;)V"
    );

    auto mimeType = env->NewStringUTF(imageInfo.mimetype());
    return env->NewObject(
            artworkClass, constructor,
            mimeType, jbytesData,
            (jint) imageInfo.size().width,
            (jint) imageInfo.size().height,
            (jlong) byteVector.size(),
            description, type
    );
}

extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_setTagField(JNIEnv *env, jobject thiz,
                                                               jlong accessorRef,
                                                               jstring tag_field,
                                                               jstring value) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    if (accessor == nullptr) {
        throwAccessorNullException(env);
        return;
    }
    auto fieldName = env->GetStringUTFChars(tag_field, 0);
    auto fieldValue = env->GetStringUTFChars(value, 0);

    Tag *tag = accessor->tag();
    PropertyMap propertyMap = tag->properties();
    propertyMap[fieldName] = String(fieldValue);
}

extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_setArtwork(JNIEnv *env,
                                                              jobject thiz,
                                                              jlong accessorRef,
                                                              jbyteArray artwork) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    if (accessor == nullptr) {
        throwAccessorNullException(env);
        return;
    }
    File *f = accessor->fileRef()->file();
    jbyte *data = env->GetByteArrayElements(artwork, nullptr);
    auto size = env->GetArrayLength(artwork);
    ByteVector byteVector((const char *) data, size);

    f->setComplexProperties("PICTURE", List<VariantMap>{
            VariantMap{
                    {"data",        byteVector},
                    {"pictureType", "Front Cover"}
            }
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_saveFile(JNIEnv *env,
                                                            jobject thiz,
                                                            jlong accessorRef) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    if (accessor == nullptr) {
        throwAccessorNullException(env);
        return;
    }
    File *f = accessor->fileRef()->file();
    f->save();
}

extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_deleteTagField(JNIEnv *env,
                                                                  jobject thiz,
                                                                  jlong accessorRef,
                                                                  jstring tag_field) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    if (accessor == nullptr) {
        throwAccessorNullException(env);
        return;
    }
    string fieldName = env->GetStringUTFChars(tag_field, 0);
    if (fieldName.empty()) {
        return;
    }
    if (fieldName == "PICTURE") {
        File *f = accessor->fileRef()->file();
        f->setComplexProperties("PICTURE", {});
        return;
    }

    Tag *tag = accessor->tag();
    PropertyMap propertyMap = tag->properties();
    propertyMap.erase(fieldName);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_lastModified(JNIEnv *env,
                                                                jobject thiz,
                                                                jlong accessorRef) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    if (accessor == nullptr) {
        throwAccessorNullException(env);
        return 0;
    }
    return accessor->lastModified();
}


extern "C"
JNIEXPORT jlong JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_getSize(JNIEnv *env, jobject thiz,
                                                           jlong accessorRef) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    if (accessor == nullptr) {
        throwAccessorNullException(env);
        return -1;
    }

    return accessor->size();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_getAudioProperties(JNIEnv *env,
                                                                      jobject thiz,
                                                                      jlong accessorRef) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    if (accessor == nullptr) {
        throwAccessorNullException(env);
        return nullptr;
    }

    FileRef *ref = accessor->fileRef();
    AudioProperties *properties = ref->audioProperties();

    jclass propertiesClass = env->FindClass(
            "tech/rollw/player/audio/tag/AudioProperties"
    );
    jmethodID constructor = env->GetMethodID(
            propertiesClass,
            "<init>", "(IIIIJ)V");

    return env->NewObject(
            propertiesClass, constructor,
            properties->channels(),
            properties->bitrate(),
            accessor->bitDepth(),
            properties->sampleRate(),
            (jlong) properties->lengthInMilliseconds()
    );
}
