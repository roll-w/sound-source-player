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

#include "asfproperties.h"
#include "apeproperties.h"
#include "flacfile.h"
#include "mpegproperties.h"
#include "trueaudioproperties.h"
#include "mp4properties.h"
#include "dsdiffproperties.h"
#include "dsfproperties.h"
#include "mpcproperties.h"
#include "wavpackproperties.h"
#include "vorbisproperties.h"
#include "opusproperties.h"
#include "wavproperties.h"
#include "aiffproperties.h"

using namespace TagLib;

namespace SoundSource {
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

    int64_t AudioTagAccessor::size() {
        struct stat st;
        fstat(fileDescriptor, &st);
        return st.st_size;
    }

    int32_t AudioTagAccessor::bitDepth() {
        AudioProperties *properties = pfileRef->audioProperties();
        if (properties == nullptr) {
            return -1;
        }
        if (auto *flacProperties = dynamic_cast<TagLib::FLAC::Properties *>(properties)) {
            return flacProperties->bitsPerSample();
        } else if (auto *apeProperties = dynamic_cast<TagLib::APE::Properties *>(properties)) {
            return apeProperties->bitsPerSample();
        } else if (auto *mpegProperties = dynamic_cast<TagLib::MPEG::Properties *>(properties)) {
            return -1;
        } else if (auto *trueAudioProperties = dynamic_cast<TagLib::TrueAudio::Properties *>(properties)) {
            return trueAudioProperties->bitsPerSample();
        } else if (auto *mp4Properties = dynamic_cast<TagLib::MP4::Properties *>(properties)) {
            return mp4Properties->bitsPerSample();
        } else if (auto *asfProperties = dynamic_cast<TagLib::ASF::Properties *>(properties)) {
            return asfProperties->bitsPerSample();
        } else if (auto *dsdiffProperties = dynamic_cast<TagLib::DSDIFF::Properties *>(properties)) {
            return dsdiffProperties->bitsPerSample();
        } else if (auto *dsfProperties = dynamic_cast<TagLib::DSF::Properties *>(properties)) {
            return dsfProperties->bitsPerSample();
        } else if (auto *mpcProperties = dynamic_cast<TagLib::MPC::Properties *>(properties)) {
            return -1;
        } else if (auto *oggProperties = dynamic_cast<TagLib::Ogg::Opus::Properties *>(properties)) {
            return -1;
        } else if (auto *wavPackProperties = dynamic_cast<TagLib::WavPack::Properties *>(properties)) {
            return wavPackProperties->bitsPerSample();
        } else if (auto *wavProperties = dynamic_cast<TagLib::RIFF::WAV::Properties *>(properties)) {
            return wavProperties->bitsPerSample();
        } else if (auto *aiffProperties = dynamic_cast<TagLib::RIFF::AIFF::Properties *>(properties)) {
            return aiffProperties->bitsPerSample();
        } else if (auto *vorbisProperties = dynamic_cast<TagLib::Vorbis::Properties *>(properties)) {
            return -1;
        }
        return -1;
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

}
