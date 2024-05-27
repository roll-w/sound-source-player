#
#  Copyright (C) 2024 RollW
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

cmake_minimum_required(VERSION 3.22.1 FATAL_ERROR)

project("soundsource")

set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(project_SRCS
  ImageUtils_jni.cpp
  NativeLibAudioTag_jni.cpp
  logging.h
)

set(image_SRCS
  image/image.cpp
  image/image.h
)

set(tags_SRCS
  tags/tags.h
  tags/tags.cpp
)

add_subdirectory("taglib")

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/toolkit
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/asf
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/mpeg
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/ogg
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/ogg/flac
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/flac
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/mpc
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/mp4
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/ogg/vorbis
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/ogg/speex
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/ogg/opus
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/mpeg/id3v2
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/mpeg/id3v2/frames
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/mpeg/id3v1
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/ape
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/wavpack
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/trueaudio
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/riff
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/riff/aiff
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/riff/wav
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/mod
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/s3m
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/it
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/xm
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/dsf
        ${CMAKE_CURRENT_SOURCE_DIR}/taglib/taglib/dsdiff
)

include_directories(
  ${CMAKE_CURRENT_SOURCE_DIR}/image
  ${CMAKE_CURRENT_SOURCE_DIR}/tags
)

set(native-lib_SRCS
  ${project_SRCS}
  ${image_SRCS}
  ${tags_SRCS}
)

add_library(
        ${CMAKE_PROJECT_NAME}
        SHARED
        ${native-lib_SRCS}
)

find_library(jnigraphics-lib jnigraphics)
find_package(oboe REQUIRED CONFIG)

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        android
        log
        tag
        ${jnigraphics-lib}
        oboe::oboe
        OpenSLES
)
