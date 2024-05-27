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

#include <sys/stat.h>
#include "tags.h"
#include "tfilestream.h"

using namespace SoundSource;
using namespace TagLib;

AudioTagAccessor::AudioTagAccessor(int32_t fileDescriptor, bool readonly) {
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

int64_t AudioTagAccessor::lastModified() {
    struct stat st;
    fstat(fileDescriptor, &st);
    timespec ts = st.st_mtim;
    long mtime_ms = ts.tv_sec * 1000 + ts.tv_nsec / 1000000;
    return mtime_ms;
}

void AudioTagAccessor::open() {
    internalOpen(fileDescriptor, readonly);
}

void AudioTagAccessor::internalOpen(int32_t fileDescriptor, bool readonly) {
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
