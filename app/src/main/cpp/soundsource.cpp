#include <iostream>
#include <jni.h>
#include <string>
#include <unistd.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/stat.h>

#include "logging.h"

#include "taglib/taglib/tag.h"
#include "taglib/taglib/flac/flacfile.h"
#include "taglib/taglib/ogg/xiphcomment.h"
#include "taglib/taglib/fileref.h"
#include "tfilestream.h"
#include "tpropertymap.h"


using namespace std;
using namespace TagLib;

// TODO: move to a class

extern "C"
JNIEXPORT jlong JNICALL
Java_tech_rollw_player_audio_tag_LibAudioTag_openFile(JNIEnv *env, jobject thiz,
                                                      jint file_descriptor,
                                                      jboolean jreadonly) {
    bool readonly = jreadonly;
    FileStream *fs = new FileStream(file_descriptor, readonly);
    FileRef *f = new FileRef(fs);

    if (f->isNull()) {
        LOGD("file is null");
        return 0;
    }
    return (jlong) f;
}

extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_LibAudioTag_closeFile(JNIEnv *env,
                                                       jobject thiz,
                                                       jlong fileRef) {
    FileRef *f = (FileRef *) fileRef;

    free(f);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_tech_rollw_player_audio_tag_LibAudioTag_getTagField(JNIEnv *env,
                                                         jobject thiz,
                                                         jlong fileRef,
                                                         jstring jTagField) {
    FileRef *file = (FileRef *) fileRef;
    Tag *t = file->tag();
    auto fieldName = env->GetStringUTFChars(jTagField, 0);

    if (t == nullptr) {
        LOGD("Tag is null of fileRef*(=%lld), field=%s", fileRef, fieldName);
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
Java_tech_rollw_player_audio_tag_LibAudioTag_getArtwork(JNIEnv *env,
                                                        jobject thiz,
                                                        jlong fileRef) {
    // FIXME: cannot read picture from some flac files
    FileRef *ref = (FileRef *) fileRef;
    File *f = ref->file();

    const List<VariantMap> &pictures = f->complexProperties("PICTURE");
    // LOGD("fileRef=%lld: pictures.size()=%d", fileRef, pictures.size());
    if (pictures.isEmpty()) {
        return nullptr;
    }
    auto map = pictures.front();
    // LOGD("fileRef=%lld: map.size()=%d", fileRef, map.size());
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
Java_tech_rollw_player_audio_tag_LibAudioTag_setTagField(JNIEnv *env, jobject thiz,
                                                         jlong file_ref,
                                                         jstring tag_field,
                                                         jstring value) {
    // TODO: implement nativeSetTagField()
}
extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_LibAudioTag_setArtwork(JNIEnv *env,
                                                        jobject thiz,
                                                        jlong file_ref,
                                                        jbyteArray artwork) {
    // TODO: implement nativeSetArtwork()
}
extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_LibAudioTag_saveFile(JNIEnv *env,
                                                      jobject thiz,
                                                      jlong file_ref) {
    // TODO: implement nativeSave()
}

extern "C"
JNIEXPORT void JNICALL
Java_tech_rollw_player_audio_tag_LibAudioTag_deleteTagField(JNIEnv *env,
                                                            jobject thiz,
                                                            jlong file_ref,
                                                            jstring tag_field) {

}

extern "C"
JNIEXPORT jlong JNICALL
Java_tech_rollw_player_audio_tag_LibAudioTag_lastModified(JNIEnv *env,
                                                          jobject thiz,
                                                          jint fd) {

    struct stat st;
    fstat(fd, &st);
    timespec ts = st.st_mtim;
    long mtime_ms = ts.tv_sec * 1000 + ts.tv_nsec / 1000000;

    return mtime_ms;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_tech_rollw_player_audio_tag_LibAudioTag_getAudioProperties(JNIEnv *env,
                                                                jobject thiz,
                                                                jlong file_ref) {
    FileRef *ref = (FileRef *) file_ref;
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