cmake_minimum_required(VERSION 3.22.1 FATAL_ERROR)

project("soundsource")

set(CMAKE_INCLUDE_CURRENT_DIR ON)

aux_source_directory(${CMAKE_CURRENT_SOURCE_DIR} PROJECT_SOURCE_FILES)

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

add_library(
        ${CMAKE_PROJECT_NAME}
        SHARED
        ${PROJECT_SOURCE_FILES}
)

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        android
        log
        tag
        OpenSLES
)
