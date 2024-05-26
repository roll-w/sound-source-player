#include <sys/stat.h>
#include "tags.h"
#include "tfilestream.h"

using namespace SoundSource;
using namespace TagLib;

AudioTagAccessor::AudioTagAccessor(int fileDescriptor, bool readonly) {
    this->pfileRef = nullptr;
    this->fileDescriptor = fileDescriptor;
    this->readonly = readonly;
    internalOpen(fileDescriptor, readonly);
}

AudioTagAccessor::~AudioTagAccessor() {
    close();
}

TagLib::Tag *AudioTagAccessor::tag() {
    if (pfileRef == nullptr) {
        open();
    }

    return pfileRef->tag();
}

TagLib::FileRef *AudioTagAccessor::fileRef() {
    return pfileRef;
}

long AudioTagAccessor::lastModified() {
    struct stat st;
    fstat(fileDescriptor, &st);
    timespec ts = st.st_mtim;
    long mtime_ms = ts.tv_sec * 1000 + ts.tv_nsec / 1000000;
    return mtime_ms;
}

void AudioTagAccessor::open() {
    internalOpen(fileDescriptor, readonly);
}

void AudioTagAccessor::internalOpen(int fileDescriptor, bool readonly) {
    if (pfileRef != nullptr) {
        return;
    }
    FileStream *fs = new FileStream(fileDescriptor, readonly);
    pfileRef = new FileRef(fs);
}

void AudioTagAccessor::close() {
    if (pfileRef == nullptr) {
        return;
    }
    free(pfileRef);
    pfileRef = nullptr;
}

bool AudioTagAccessor::isNull() {
    return pfileRef == nullptr || pfileRef->isNull();
}

bool AudioTagAccessor::isOpened() {
    return pfileRef != nullptr;
}
