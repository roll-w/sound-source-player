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

#include "tags/tags.h"

using namespace std;
using namespace TagLib;
using namespace SoundSource;

extern "C"
JNIEXPORT jlong JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_openFile(JNIEnv *env, jobject thiz,
                                                            jint file_descriptor,
                                                            jboolean jreadonly) {
    bool readonly = jreadonly;
    AudioTagAccessor *accessor = new AudioTagAccessor(file_descriptor, readonly);
    if (accessor->isNull()) {
        LOGD("file is null");
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
    accessor->close();
    delete accessor;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_getTagField(JNIEnv *env,
                                                               jobject thiz,
                                                               jlong accessorRef,
                                                               jstring jTagField) {
    AudioTagAccessor *tagAccessor = (AudioTagAccessor *) accessorRef;
    Tag *t = tagAccessor->tag();
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

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_getArtwork(JNIEnv *env,
                                                              jobject thiz,
                                                              jlong accessorRef) {
    // FIXME: cannot read picture from some flac files
    AudioTagAccessor *tagAccessor = (AudioTagAccessor *) accessorRef;
    File *f = tagAccessor->fileRef()->file();

    const List<VariantMap> &pictures = f->complexProperties("PICTURE");
    if (pictures.isEmpty()) {
        return nullptr;
    }
    auto map = pictures.front();
    auto data = map["data"].toByteVector();
    if (data.isEmpty()) {
        return nullptr;
    }
    int size = data.size();
    jbyteArray result = env->NewByteArray(size);
    env->SetByteArrayRegion(result, 0, size,
                            (jbyte *) data.data());
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_setTagField(JNIEnv *env, jobject thiz,
                                                               jlong accessorRef,
                                                               jstring tag_field,
                                                               jstring value) {
    // TODO: implement nativeSetTagField()
}
extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_setArtwork(JNIEnv *env,
                                                              jobject thiz,
                                                              jlong accessorRef,
                                                              jbyteArray artwork) {
    // TODO: implement nativeSetArtwork()
}
extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_saveFile(JNIEnv *env,
                                                            jobject thiz,
                                                            jlong accessorRef) {
    // TODO: implement nativeSave()
}

extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_deleteTagField(JNIEnv *env,
                                                                  jobject thiz,
                                                                  jlong accessorRef,
                                                                  jstring tag_field) {

}

extern "C"
JNIEXPORT jlong JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_lastModified(JNIEnv *env,
                                                                jobject thiz,
                                                                jlong accessorRef) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    return accessor->lastModified();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_tech_rollw_player_audio_tag_NativeLibAudioTag_getAudioProperties(JNIEnv *env,
                                                                      jobject thiz,
                                                                      jlong accessorRef) {
    AudioTagAccessor *accessor = (AudioTagAccessor *) accessorRef;
    FileRef *ref = accessor->fileRef();
    AudioProperties *properties = ref->audioProperties();

    jclass propertiesClass = env->FindClass(
            "tech/rollw/player/audio/tag/AudioProperties"
    );
    jmethodID constructor = env->GetMethodID(
            propertiesClass,
            "<init>", "(IIIJ)V");

    return env->NewObject(
            propertiesClass, constructor,
            properties->channels(),
            properties->bitrate(),
            properties->sampleRate(),
            (jlong) properties->lengthInMilliseconds()
    );
}