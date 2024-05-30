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

#ifndef SOUNDSOURCE_TAGS_H
#define SOUNDSOURCE_TAGS_H

#include <sys/types.h>
#include <taglib/taglib/toolkit/tpropertymap.h>
#include <taglib/taglib/fileref.h>
#include <taglib/taglib/tag.h>

namespace SoundSource {
    class AudioTagAccessor {
    public:
        AudioTagAccessor(int32_t fileDescriptor, bool readonly);

        ~AudioTagAccessor();

        TagLib::Tag *tag();

        TagLib::FileRef *fileRef();

        int64_t lastModified();

        int64_t size();

        /**
         * @return -1 if no bit depth information is available
         */
        int32_t bitDepth();

        void open();

        void close();

        bool isNull();

        bool isOpened();

    private:
        TagLib::FileRef *pfileRef;
        int fileDescriptor;
        bool readonly;

        void internalOpen(int fileDescriptor, bool readonly);
    };
}

#endif //SOUNDSOURCE_TAGS_H