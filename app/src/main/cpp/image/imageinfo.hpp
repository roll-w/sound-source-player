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

//
// Created by xiaozhuai on 2021/4/1, and modified by RollW on 2024 to fit the project codestyle.
// https://github.com/xiaozhuai/imageinfo
//
//
// MIT License
//
// Copyright (c) 2021 xiaozhuai
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

#pragma once
#ifndef IMAGEINFO_IMAGEINFO_H
#define IMAGEINFO_IMAGEINFO_H

#include <algorithm>
#include <array>
#include <cassert>
#include <cstdint>
#include <cstdio>
#include <cstring>
#include <fstream>
#include <functional>
#include <regex>
#include <set>
#include <string>
#include <tuple>
#include <unordered_map>
#include <unordered_set>
#include <utility>
#include <vector>

#ifdef ANDROID

#include <android/asset_manager.h>

#endif

#ifndef II_HEADER_CACHE_SIZE
#define II_HEADER_CACHE_SIZE (1024)
#endif

// #define II_DISABLE_HEADER_CACHE

static_assert(sizeof(uint8_t) == 1, "sizeof(uint8_t) != 1");
static_assert(sizeof(int8_t) == 1, "sizeof(int8_t) != 1");
static_assert(sizeof(uint16_t) == 2, "sizeof(uint16_t) != 2");
static_assert(sizeof(int16_t) == 2, "sizeof(int16_t) != 2");
static_assert(sizeof(uint32_t) == 4, "sizeof(uint32_t) != 4");
static_assert(sizeof(int32_t) == 4, "sizeof(int32_t) != 4");
static_assert(sizeof(uint64_t) == 8, "sizeof(uint64_t) != 8");
static_assert(sizeof(int64_t) == 8, "sizeof(int64_t) != 8");

#ifdef __clang__
#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCUnusedStructInspection"
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"
#endif

namespace SoundSource::Image {
    enum Format {
        UNKNOWN = 0,
        AVIF,
        BMP,
        CUR,
        DDS,
        GIF,
        HDR,
        HEIC,
        ICNS,
        ICO,
        JP2,
        JPEG,
        JPX,
        KTX,
        PNG,
        PSD,
        QOI,
        TIFF,
        WEBP,
        TAG,
        //
        FORMAT_END,
        FORMAT_COUNT = FORMAT_END - 1,
    };

    enum Error {
        NO_ERROR = 0,
        UNRECOGNIZED_FORMAT,
    };

    class FileReader {
    public:
        explicit FileReader(FILE *file) : file_(file) {}

        inline size_t size() {
            if (file_ != nullptr) {
                fseek(file_, 0, SEEK_END);
                return ftell(file_);
            } else {
                return 0;
            }
        }

        inline void read(void *buf, off_t offset, size_t size) {
            fseek(file_, offset, SEEK_SET);
            fread(buf, 1, size, file_);
        }

    private:
        FILE *file_ = nullptr;
    };

    class FilePathReader {
    public:
        explicit FilePathReader(const std::string &path) : file_(path, std::ios::in |
                                                                       std::ios::binary) {}

        ~FilePathReader() {
            if (file_.is_open()) {
                file_.close();
            }
        }

        inline size_t size() {
            if (file_.is_open()) {
                file_.seekg(0, std::ios::end);
                return (size_t) file_.tellg();
            } else {
                return 0;
            }
        }

        inline void read(void *buf, off_t offset, size_t size) {
            file_.seekg(offset, std::ios::beg);
            file_.read((char *) buf, (std::streamsize) size);
        }

    private:
        std::ifstream file_;
    };

    class FileStreamReader {
    public:
        explicit FileStreamReader(std::ifstream &file) : file_(file) {}

        inline size_t size() {
            if (file_.is_open()) {
                file_.seekg(0, std::ios::end);
                return (size_t) file_.tellg();
            } else {
                return 0;
            }
        }

        inline void read(void *buf, off_t offset, size_t size) {
            file_.seekg(offset, std::ios::beg);
            file_.read((char *) buf, (std::streamsize) size);
        }

    private:
        std::ifstream &file_;
    };

#ifdef ANDROID

    class AndroidAssetFileReader {
    public:
        explicit AndroidAssetFileReader(AAsset *file) : file_(file) {}

        inline size_t size() const {
            if (file_ != nullptr) {
                return AAsset_getLength(file_);
            } else {
                return 0;
            }
        }

        inline void read(void *buf, off_t offset, size_t size) {
            AAsset_seek(file_, offset, SEEK_SET);
            AAsset_read(file_, buf, size);
        }

    private:
        AAsset *file_ = nullptr;
    };

#endif

    struct RawData {
        RawData(const void *d, size_t s) : data(d), length(s) {}

        const void *data = nullptr;
        size_t length = 0;
    };

    class RawDataReader {
    public:
        explicit RawDataReader(RawData data) : data_(data) {}

        inline size_t size() const { return data_.length; }

        inline void read(void *buf, off_t offset, size_t size) const {
            memcpy(buf, ((char *) data_.data) + offset, size);
        }

    private:
        RawData data_;
    };

    class Buffer {
    public:
        Buffer() = default;

        explicit Buffer(size_t size) { alloc(size); }

        inline void alloc(size_t size) {
            size_ = size;
            data_ = std::shared_ptr<uint8_t>(new uint8_t[size],
                                             std::default_delete<uint8_t[]>());
        }

        inline const uint8_t *data() const { return data_.get(); }

        inline uint8_t *data() { return data_.get(); }

        inline size_t size() const { return size_; }

        inline uint8_t &operator[](int offset) { return data_.get()[offset]; }

        inline uint8_t operator[](int offset) const { return data_.get()[offset]; }

    public:
        inline uint8_t readU8(off_t offset) { return readInt<uint8_t>(offset, false); }

        inline int8_t readS8(off_t offset) { return readInt<int8_t>(offset, false); }

        inline uint16_t readU16Le(off_t offset) { return readInt<uint16_t>(offset, false); }

        inline uint16_t readU16Be(off_t offset) { return readInt<uint16_t>(offset, true); }

        inline int16_t readS16Le(off_t offset) { return readInt<int16_t>(offset, false); }

        inline int16_t readS16Be(off_t offset) { return readInt<int16_t>(offset, true); }

        inline uint32_t readU32Le(off_t offset) { return readInt<uint32_t>(offset, false); }

        inline uint32_t readU32Be(off_t offset) { return readInt<uint32_t>(offset, true); }

        inline int32_t readS32Le(off_t offset) { return readInt<int32_t>(offset, false); }

        inline int32_t readS32Be(off_t offset) { return readInt<int32_t>(offset, true); }

        inline uint64_t readU64Le(off_t offset) { return readInt<uint64_t>(offset, false); }

        inline uint64_t readU64Be(off_t offset) { return readInt<uint64_t>(offset, true); }

        inline int64_t readS64Le(off_t offset) { return readInt<int64_t>(offset, false); }

        inline int64_t readS64Be(off_t offset) { return readInt<int64_t>(offset, true); }

        template<typename T>
        inline T readInt(off_t offset, bool swap_endian = false) {
            T val = *((T *) (data() + offset));
            return swap_endian ? swapE<T>(val) : val;
        }

        inline std::string readString(off_t offset, size_t size) {
            return std::string((char *) data() + offset, size);
        }

        inline std::string toString() { return std::string((char *) data(), size()); }

        inline bool cmp(off_t offset, size_t size, const void *buf) {
            return memcmp(data() + offset, buf, size) == 0;
        }

        inline bool
        cmpAnyOf(off_t offset, size_t size, const std::initializer_list<const void *> &bufs) {
            return std::any_of(bufs.begin(), bufs.end(),
                               [this, offset, size](const void *buf) {
                                   return memcmp(data() + offset, buf, size) == 0;
                               });
        }

    private:
        template<typename T>
        static inline T swapE(T u) {
            union {
                T u;
                uint8_t u8[sizeof(T)];
            } src{}, dst{};
            src.u = u;
            for (size_t k = 0; k < sizeof(T); k++) {
                dst.u8[k] = src.u8[sizeof(T) - k - 1];
            }
            return dst.u;
        }

    private:
        std::shared_ptr<uint8_t> data_ = nullptr;
        size_t size_ = 0;
    };

    using ReadFunc = std::function<void(void *buf, off_t offset, size_t size)>;

    class ReadInterface {
    public:
        ReadInterface() = delete;

        ReadInterface(ReadFunc &read_func, size_t length) : read_func_(read_func),
                                                            length_(length) {
#ifndef II_DISABLE_HEADER_CACHE
            header_cache_.alloc((std::min)((size_t) II_HEADER_CACHE_SIZE, length));
            read(header_cache_.data(), 0, header_cache_.size());
#endif
        }

        inline Buffer readBuffer(off_t offset, size_t size) {
            assert(offset >= 0);
            assert(offset + size <= length_);
            Buffer buffer(size);
#ifndef II_DISABLE_HEADER_CACHE
            if (offset + size <= header_cache_.size()) {
                memcpy(buffer.data(), header_cache_.data() + offset, size);
            } else if (offset < header_cache_.size() &&
                       header_cache_.size() - offset >= (II_HEADER_CACHE_SIZE / 4)) {
                size_t head = header_cache_.size() - offset;
                memcpy(buffer.data(), header_cache_.data() + offset, head);
                read(buffer.data() + head, offset + (off_t) head, size - head);
            } else {
                read(buffer.data(), offset, size);
            }
#else
            read(buffer.data(), offset, size);
#endif
            return buffer;
        }

        inline size_t length() const { return length_; }

    private:
        inline void read(void *buf, off_t offset, size_t size) {
            read_func_(buf, offset, size);
        }

    private:
        ReadFunc &read_func_;
        size_t length_ = 0;
#ifndef II_DISABLE_HEADER_CACHE
        Buffer header_cache_;
#endif
    };

    class ImageSize {
    public:
        ImageSize() = default;

        ImageSize(int64_t width, int64_t height) : width(width), height(height) {}

        inline bool operator==(const ImageSize &rhs) const {
            return width == rhs.width && height == rhs.height;
        }

        inline int64_t operator[](int index) const {
            assert(index >= 0 && index < 2);
            return index == 0 ? width : height;
        }

        int64_t width = -1;
        int64_t height = -1;
    };

    using EntrySizes = std::vector<ImageSize>;

    class ImageInfo {
    public:
        ImageInfo() = default;

        explicit ImageInfo(Error error) : error_(error) {}

        ImageInfo(Format format, const char *ext, const char *full_ext, const char *mimetype)
                : format_(format), ext_(ext), full_ext_(full_ext), mimetype_(mimetype) {}

    public:
        inline void setSize(const ImageSize &size) { size_ = size; }

        inline void setSize(int64_t width, int64_t height) {
            size_ = ImageSize(width, height);
        }

        inline void
        setEntrySizes(const EntrySizes &entry_sizes) { entry_sizes_ = entry_sizes; }

        inline void addEntrySize(const ImageSize &size) { entry_sizes_.emplace_back(size); }

        inline void addEntrySize(int64_t width, int64_t height) {
            entry_sizes_.emplace_back(width, height);
        }

    public:
        inline explicit operator bool() const { return error_ == NO_ERROR; }

        inline bool ok() const { return error_ == NO_ERROR; }

        inline Error error() const { return error_; }

        inline const char *errorMessage() const {
            switch (error_) {
                case NO_ERROR:
                    return "No error";
                case UNRECOGNIZED_FORMAT:
                    return "Unrecognized format";
                default:
                    return "Unknown error";
            }
        }

        inline Format format() const { return format_; }

        inline const char *ext() const { return ext_; }

        inline const char *fullExt() const { return full_ext_; }

        inline const char *mimetype() const { return mimetype_; }

        inline const ImageSize &size() const { return size_; }

        inline const EntrySizes &entrySizes() const { return entry_sizes_; }

    private:
        Format format_ = UNKNOWN;
        const char *ext_ = "";
        const char *full_ext_ = "";
        const char *mimetype_ = "";
        ImageSize size_;
        EntrySizes entry_sizes_;
        Error error_ = NO_ERROR;
    };

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// https://nokiatech.github.io/heif/technical.html
// https://www.jianshu.com/p/b016d10a087d
    inline bool try_avif_heic(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 4) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 4);
        uint32_t ftyp_box_length = buffer.readU32Be(0);
        if (length < ftyp_box_length + 12) {
            return false;
        }
        buffer = ri.readBuffer(0, ftyp_box_length + 12);
        if (!buffer.cmp(4, 4, "ftyp")) {
            return false;
        }

        /**
         * Major Brand
         *
         * AVIF: "avif", "avis"
         * HEIF: "mif1", "msf1"
         * HEIC: "heic", "heix", "hevc", "hevx"
         *
         */
        if (!buffer.cmpAnyOf(8, 4, {"avif", "avis", "mif1", "msf1", "heic", "heix", "hevc",
                                      "hevx"})) {
            return false;
        }

        uint32_t compatible_brand_size = (ftyp_box_length - 16) / 4;
        std::unordered_set<std::string> compatible_brands;
        for (uint32_t i = 0; i < compatible_brand_size; ++i) {
            compatible_brands.insert(buffer.readString(16 + i * 4, 4));
        }

        bool is_avif;
        if (compatible_brands.find("avif") != compatible_brands.end() ||
            buffer.cmp(8, 4, "avif")) {
            is_avif = true;
        } else if (compatible_brands.find("heic") != compatible_brands.end() ||
                   buffer.cmp(8, 4, "heic")) {
            is_avif = false;
        } else {
            return false;
        }

        if (!buffer.cmp(ftyp_box_length + 4, 4, "meta")) {
            return false;
        }

        uint32_t meta_length = buffer.readU32Be(ftyp_box_length);

        if (length < ftyp_box_length + 12 + meta_length) {
            return false;
        }

        buffer = ri.readBuffer(ftyp_box_length + 12, meta_length);

        off_t offset = 0;
        off_t end = meta_length;

        /**
         * find ispe box
         *
         * meta
         *   - ...
         *   - iprp
         *       - ...
         *       - ipco
         *           - ...
         *           - ispe
         */
        while (offset < end) {
            uint32_t box_size = buffer.readU32Be(offset);
            if (buffer.cmpAnyOf(offset + 4, 4, {"iprp", "ipco"})) {
                end = offset + box_size;
                offset += 8;
            } else if (buffer.cmp(offset + 4, 4, "ispe")) {
                if (is_avif) {
                    info = ImageInfo(AVIF, "avif", "avif", "image/avif");
                } else {
                    info = ImageInfo(HEIC, "heic", "heic", "image/heic");
                }
                info.setSize(                        //
                        buffer.readU32Be(offset + 12),  //
                        buffer.readU32Be(offset + 16)   //
                );
                return true;
            } else {
                offset += box_size;
            }
        }

        return false;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// https://www.fileformat.info/format/bmp/corion.htm
    inline bool try_bmp(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 26) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 26);
        if (!buffer.cmp(0, 2, "BM")) {
            return false;
        }

        info = ImageInfo(BMP, "bmp", "bmp", "image/bmp");
        // bmp height can be negative, it means flip Y
        info.setSize(                        //
                buffer.readS32Le(18),           //
                std::abs(buffer.readS32Le(22))  //
        );
        return true;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    inline bool try_cur_ico(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 6) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 6);

        bool is_cur;
        if (buffer.cmp(0, 4, "\x00\x00\x02\x00")) {
            is_cur = true;
        } else if (buffer.cmp(0, 4, "\x00\x00\x01\x00")) {
            is_cur = false;
        } else {
            return false;
        }

        uint16_t entry_count = buffer.readU16Le(4);
        if (entry_count == 0) {
            return false;
        }
        const int entry_size = 16;
        off_t entry_total_size = entry_count * entry_size;

        off_t offset = 6;
        if (length < offset + entry_total_size) {
            return false;
        }
        buffer = ri.readBuffer(offset, entry_total_size);
        offset += entry_total_size;

        EntrySizes sizes;

        for (int i = 0; i < entry_count; ++i) {
            uint8_t w1 = buffer.readU8(i * entry_size);
            uint8_t h1 = buffer.readU8(i * entry_size + 1);
            int64_t w2 = w1 == 0 ? 256 : w1;
            int64_t h2 = h1 == 0 ? 256 : h1;
            sizes.emplace_back(w2, h2);

            uint32_t bytes = buffer.readS32Le(i * entry_size + 8);
            offset += bytes;
        }

        if (length < (size_t) offset) {
            return false;
        }

        if (is_cur) {
            info = ImageInfo(CUR, "cur", "cur", "image/cur");
        } else {
            info = ImageInfo(ICO, "ico", "ico", "image/ico");
        }
        info.setEntrySizes(sizes);
        info.setSize(sizes.front());
        return true;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    inline bool try_dds(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 20) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 20);
        if (!buffer.cmp(0, 4, "DDS ")) {
            return false;
        }

        info = ImageInfo(DDS, "dds", "dds", "image/dds");
        info.setSize(               //
                buffer.readU32Le(16),  //
                buffer.readU32Le(12)   //
        );
        return true;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// https://www.fileformat.info/format/gif/corion.htm
    inline bool try_gif(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 10) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 10);
        if (!buffer.cmpAnyOf(0, 6, {"GIF87a", "GIF89a"})) {
            return false;
        }

        info = ImageInfo(GIF, "gif", "gif", "image/gif");
        info.setSize(              //
                buffer.readU16Le(6),  //
                buffer.readU16Le(8)   //
        );
        return true;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// http://paulbourke.net/dataformats/pic/
    inline bool try_hdr(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 6) {
            return false;
        }
        off_t offset = 6;
        auto buffer = ri.readBuffer(0, 6);
        if (!buffer.cmpAnyOf(0, 6, {"#?RGBE", "#?XYZE"})) {
            if (length < 10) {
                return false;
            }
            offset = 10;
            buffer = ri.readBuffer(0, 10);
            if (!buffer.cmp(0, 10, "#?RADIANCE")) {
                return false;
            }
        }

        const size_t piece = 64;
        std::string header;
        static const std::regex x_pattern(R"(\s[+-]X\s(\d+)\s)");
        static const std::regex y_pattern(R"(\s[+-]Y\s(\d+)\s)");
        while (offset < length) {
            buffer = ri.readBuffer(offset, std::min<size_t>(length - offset, piece));
            offset += (off_t) buffer.size();
            header += buffer.toString();
            std::smatch x_results;
            std::smatch y_results;
            std::regex_search(header, x_results, x_pattern);
            std::regex_search(header, y_results, y_pattern);
            if (x_results.size() >= 2 && y_results.size() >= 2) {
                info = ImageInfo(HDR, "hdr", "hdr", "image/vnd.radiance");
                info.setSize(                    //
                        std::stol(x_results.str(1)),  //
                        std::stol(y_results.str(1))   //
                );
                return true;
            }
        }
        return false;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    inline bool try_icns(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 8) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 8);
        uint32_t file_length = buffer.readU32Be(4);
        if (!buffer.cmp(0, 4, "icns") || file_length != length) {
            return false;
        }

        static const std::unordered_map<std::string, int64_t> size_map = {
                {"ICON", 32},
                {"ICN#", 32},
                {"icm#", 16},
                {"icm4", 16},
                {"icm8", 16},
                {"ics#", 16},
                {"ics4", 16},
                {"ics8", 16},
                {"is32", 16},
                {"s8mk", 16},
                {"icl4", 32},
                {"icl8", 32},
                {"il32", 32},
                {"l8mk", 32},
                {"ich#", 48},
                {"ich4", 48},
                {"ich8", 48},
                {"ih32", 48},
                {"h8mk", 48},
                {"it32", 128},
                {"t8mk", 128},
                {"icp4", 16},
                {"icp5", 32},
                {"icp6", 64},
                {"ic07", 128},
                {"ic08", 256},
                {"ic09", 512},
                {"ic10", 1024},
                {"ic11", 32},
                {"ic12", 64},
                {"ic13", 256},
                {"ic14", 512},
                {"ic04", 16},
                {"ic05", 32},
                {"icsB", 36},
                {"icsb", 18},
        };

        int64_t max_size = 0;
        EntrySizes entry_sizes;

        off_t offset = 8;
        while (offset + 8 <= length) {
            buffer = ri.readBuffer(offset, 8);
            auto type = buffer.readString(0, 4);
            uint32_t entry_size = buffer.readU32Be(4);
            int64_t s = size_map.at(type);
            entry_sizes.emplace_back(s, s);
            max_size = (std::max)(max_size, s);
            offset += entry_size;
        }

        info = ImageInfo(ICNS, "icns", "icns", "image/icns");
        info.setSize(max_size, max_size);
        info.setEntrySizes(entry_sizes);
        return true;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// https://docs.fileformat.com/image/jp2/
// https://docs.fileformat.com/image/jpx/
    inline bool try_jp2_jpx(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 8) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 8);

        if (!buffer.cmp(4, 4, "jP  ")) {
            return false;
        }

        uint32_t signature_length = buffer.readU32Be(0);
        off_t offset = signature_length;

        if (length < offset + 12) {
            return false;
        }

        buffer = ri.readBuffer(offset, 12);
        if (!buffer.cmp(4, 4, "ftyp")) {
            return false;
        }

        bool is_jp2;
        if (buffer.cmp(8, 4, "jp2 ")) {
            is_jp2 = true;
        } else if (buffer.cmp(8, 4, "jpx ")) {
            is_jp2 = false;
        } else {
            return false;
        }

        uint32_t ftyp_length = buffer.readU32Be(0);
        offset += ftyp_length;

        while (offset + 24 <= length) {
            buffer = ri.readBuffer(offset, 24);
            if (buffer.cmp(4, 4, "jp2h")) {
                if (buffer.cmp(12, 4, "ihdr")) {
                    if (is_jp2) {
                        info = ImageInfo(JP2, "jp2", "jp2", "image/jp2");
                    } else {
                        info = ImageInfo(JPX, "jpx", "jpx", "image/jpx");
                    }
                    info.setSize(               //
                            buffer.readU32Be(20),  //
                            buffer.readU32Be(16)   //
                    );
                    return true;
                } else {
                    return false;
                }
            }
            uint32_t box_length = buffer.readU32Be(0);
            offset += box_length;
        }
        return false;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// https://www.fileformat.info/format/jpeg/corion.htm
    inline bool try_jpg(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 2) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 2);
        if (!buffer.cmp(0, 2, "\xFF\xD8")) {
            return false;
        }

        off_t offset = 2;
        while (offset + 9 <= length) {
            buffer = ri.readBuffer(offset, 9);
            uint16_t section_size = buffer.readU16Be(2);
            if (!buffer.cmp(0, 1, "\xFF")) {
                // skip garbage bytes
                offset += 1;
                continue;
            }

            // 0xFFC0 is baseline standard (SOF0)
            // 0xFFC1 is baseline optimized (SOF1)
            // 0xFFC2 is progressive (SOF2)
            if (buffer.cmpAnyOf(0, 2, {"\xFF\xC0", "\xFF\xC1", "\xFF\xC2"})) {
                info = ImageInfo(JPEG, "jpg", "jpeg", "image/jpeg");
                info.setSize(              //
                        buffer.readU16Be(7),  //
                        buffer.readU16Be(5)   //
                );
                return true;
            }
            offset += section_size + 2;
        }

        return false;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// https://www.khronos.org/registry/KTX/specs/1.0/ktxspec_v1.html
    inline bool try_ktx(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 44) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 44);
        if (!buffer.cmp(0, 12, "\xABKTX 11\xBB\r\n\x1A\n")) {
            return false;
        }

        info = ImageInfo(KTX, "ktx", "ktx", "image/ktx");
        info.setSize(               //
                buffer.readU32Le(36),  //
                buffer.readU32Le(40)   //
        );
        return true;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// https://www.fileformat.info/format/png/corion.htm
    inline bool try_png(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 4) {
            return false;
        }

        auto buffer = ri.readBuffer(0, std::min<size_t>(length, 40));
        if (!buffer.cmp(0, 4, "\x89PNG")) {
            return false;
        }

        std::string first_chunk_type = buffer.readString(12, 4);
        if (first_chunk_type == "IHDR" && buffer.size() >= 24) {
            info = ImageInfo(PNG, "png", "png", "image/png");
            info.setSize(               //
                    buffer.readU32Be(16),  //
                    buffer.readU32Be(20)   //
            );
            return true;
        } else if (first_chunk_type == "CgBI") {
            if (buffer.readString(28, 4) == "IHDR" && buffer.size() >= 40) {
                info = ImageInfo(PNG, "png", "png", "image/png");
                info.setSize(               //
                        buffer.readU32Be(32),  //
                        buffer.readU32Be(36)   //
                );
                return true;
            }
        }

        return false;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    inline bool try_psd(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 22) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 22);
        if (!buffer.cmp(0, 6, "8BPS\x00\x01")) {
            return false;
        }

        info = ImageInfo(PSD, "psd", "psd", "image/psd");
        info.setSize(               //
                buffer.readU32Be(18),  //
                buffer.readU32Be(14)   //
        );
        return true;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    inline bool try_qoi(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 12) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 12);
        if (!buffer.cmp(0, 4, "qoif")) {
            return false;
        }

        info = ImageInfo(QOI, "qoi", "qoi", "image/qoi");
        info.setSize(              //
                buffer.readU32Be(4),  //
                buffer.readU32Be(8)   //
        );
        return true;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// https://www.fileformat.info/format/tiff/corion.htm
    inline bool try_tiff(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 8) {
            return false;
        }
        auto buffer = ri.readBuffer(0, 8);
        if (!buffer.cmpAnyOf(0, 4, {"\x49\x49\x2A\x00", "\x4D\x4D\x00\x2A"})) {
            return false;
        }

        bool swap_endian = buffer[0] == 0x4D;

        auto offset = buffer.readInt<uint32_t>(4, swap_endian);
        if (length < offset + 2) {
            return false;
        }

        buffer = ri.readBuffer(offset, 2);

        auto num_entry = buffer.readInt<uint16_t>(0, swap_endian);
        offset += 2;

        int64_t width = -1;
        int64_t height = -1;
        for (uint16_t i = 0; i < num_entry && length >= offset + 12 &&
                             (width == -1 || height == -1); ++i, offset += 12) {
            buffer = ri.readBuffer(offset, 12);

            auto tag = buffer.readInt<uint16_t>(0, swap_endian);
            auto type = buffer.readInt<uint16_t>(2, swap_endian);

            if (tag == 256) {  // Found ImageWidth entry
                if (type == 3) {
                    width = buffer.readInt<uint16_t>(8, swap_endian);
                } else if (type == 4) {
                    width = buffer.readInt<uint32_t>(8, swap_endian);
                }
            } else if (tag == 257) {  // Found ImageHeight entry
                if (type == 3) {
                    height = buffer.readInt<uint16_t>(8, swap_endian);
                } else if (type == 4) {
                    height = buffer.readInt<uint32_t>(8, swap_endian);
                }
            }
        }

        bool ok = width != -1 && height != -1;
        if (ok) {
            info = ImageInfo(TIFF, "tiff", "tiff", "image/tiff");
            info.setSize(width, height);
        }
        return ok;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// https://developers.google.com/speed/webp/docs/riff_container
    inline bool try_webp(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 16) {
            return false;
        }
        auto buffer = ri.readBuffer(0, std::min<size_t>(length, 30));
        if (!buffer.cmp(0, 4, "RIFF") || !buffer.cmp(8, 4, "WEBP")) {
            return false;
        }

        std::string type = buffer.readString(12, 4);
        if (type == "VP8 " && buffer.size() >= 30) {
            info = ImageInfo(WEBP, "webp", "webp", "image/webp");
            info.setSize(                        //
                    buffer.readU16Le(26) & 0x3FFF,  //
                    buffer.readU16Le(28) & 0x3FFF   //
            );
            return true;
        } else if (type == "VP8L" && buffer.size() >= 25) {
            uint32_t n = buffer.readU32Le(21);
            info = ImageInfo(WEBP, "webp", "webp", "image/webp");
            info.setSize(                //
                    (n & 0x3FFF) + 1,         //
                    ((n >> 14) & 0x3FFF) + 1  //
            );
            return true;
        } else if (type == "VP8X" && buffer.size() >= 30) {
            uint8_t extended_header = buffer.readU8(20);
            bool valid_start = (extended_header & 0xc0) == 0;
            bool valid_end = (extended_header & 0x01) == 0;
            if (valid_start && valid_end) {
                info = ImageInfo(WEBP, "webp", "webp", "image/webp");
                info.setSize(                                        //
                        (buffer.readU32Le(24) & 0x00FFFFFF) + 1,        //
                        ((buffer.readU32Le(26) & 0xFFFFFF00) >> 8) + 1  //
                );
                return true;
            }
        }

        return false;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// TODO Not rigorous enough, keep it as last detector
// https://www.fileformat.info/format/tga/corion.htm
    inline bool try_tga(ReadInterface &ri, size_t length, ImageInfo &info) {
        if (length < 18) {
            return false;
        }

        auto buffer = ri.readBuffer((off_t) (length - 18), 18);

        if (buffer.cmp(0, 18, "TRUEVISION-XFILE.\x00")) {
            if (length < 18 + 16) {
                return false;
            }
            buffer = ri.readBuffer(0, 18);
            info = ImageInfo(TAG, "tga", "tga", "image/tga");
            info.setSize(               //
                    buffer.readU16Le(12),  //
                    buffer.readU16Le(14)   //
            );
            return true;
        }

        buffer = ri.readBuffer(0, 18);

        uint8_t id_len = buffer.readU8(0);
        if (length < (size_t) id_len + 18) {
            return false;
        }

        uint8_t color_map_type = buffer.readU8(1);
        uint8_t image_type = buffer.readU8(2);
        uint16_t first_color_map_entry_index = buffer.readU16Le(3);
        uint16_t color_map_length = buffer.readU16Le(5);
        uint8_t color_map_entry_size = buffer.readU8(7);
        // uint16_t x_origin = buffer.read_u16_le(8);
        // uint16_t y_origin = buffer.read_u16_le(10);
        uint16_t w = buffer.readU16Le(12);
        uint16_t h = buffer.readU16Le(14);
        // uint8_t pixel_depth = buffer.read_u8(16);
        // uint8_t flags = buffer.read_u8(17);

        if (color_map_type == 0) {  // no color map
            if (image_type == 0 || image_type == 2 || image_type == 3 || image_type == 10 ||
                image_type == 11 ||
                image_type == 32 || image_type == 33) {
                if (first_color_map_entry_index == 0 && color_map_length == 0 &&
                    color_map_entry_size == 0) {
                    info = ImageInfo(TAG, "tga", "tga", "image/tga");
                    info.setSize(w, h);
                    return true;
                }
            }
        } else if (color_map_type == 1) {  // 256 entry palette
            if (image_type == 1 || image_type == 9) {
                info = ImageInfo(TAG, "tga", "tga", "image/tga");
                info.setSize(w, h);
                return true;
            }
        }

        return false;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    enum DetectorIndex {
        kDetectorIndexAvifHeic = 0,
        kDetectorIndexBmp,
        kDetectorIndexCurIco,
        kDetectorIndexDds,
        kDetectorIndexGif,
        kDetectorIndexHdr,
        kDetectorIndexIcns,
        kDetectorIndexJp2Jpx,
        kDetectorIndexJpg,
        kDetectorIndexKtx,
        kDetectorIndexPng,
        kDetectorIndexPsd,
        kDetectorIndexQoi,
        kDetectorIndexTiff,
        kDetectorIndexWebp,
        kDetectorIndexTga,
        //
        DETECTOR_COUNT
    };

    using Detector = bool (*)(ReadInterface &ri, size_t length, ImageInfo &info);

    template<typename T, size_t N>
    inline constexpr size_t countof(T (&)[N]) noexcept {
        return N;
    }

    struct DetectorInfo {
        Format format;
        DetectorIndex index;
        Detector detect;
    };

    template<size_t N, int I = N - 1>
    struct check_format_order_ {
        static constexpr bool check(const DetectorInfo (&dl)[N]) {
            return (dl[I].format == static_cast<Format>(I + 1)) &&
                   check_format_order_<N, I - 1>::check(dl);
        }
    };

    template<size_t N>
    struct check_format_order_<N, 0> {
        static constexpr bool check(const DetectorInfo (&dl)[N]) {
            return dl[0].format == static_cast<Format>(0 + 1);
        }
    };

    template<size_t N>
    constexpr bool check_format_order(const DetectorInfo (&dl)[N]) {
        return check_format_order_<N>::check(dl);
    }

    inline ImageInfo parse(ReadInterface &ri,                               //
                           Format most_likely_format,                       //
                           const std::vector<Format> &likely_formats = {},  //
                           bool must_be_one_of_likely_formats = false) {    //
        size_t length = ri.length();

        constexpr DetectorInfo dl[] = {
                {AVIF, kDetectorIndexAvifHeic, try_avif_heic},
                {BMP,  kDetectorIndexBmp,      try_bmp},
                {CUR,  kDetectorIndexCurIco,   try_cur_ico},
                {DDS,  kDetectorIndexDds,      try_dds},
                {GIF,  kDetectorIndexGif,      try_gif},
                {HDR,  kDetectorIndexHdr,      try_hdr},
                {HEIC, kDetectorIndexAvifHeic, try_avif_heic},
                {ICNS, kDetectorIndexIcns,     try_icns},
                {ICO,  kDetectorIndexCurIco,   try_cur_ico},
                {JP2,  kDetectorIndexJp2Jpx,   try_jp2_jpx},
                {JPEG, kDetectorIndexJpg,      try_jpg},
                {JPX,  kDetectorIndexJp2Jpx,   try_jp2_jpx},
                {KTX,  kDetectorIndexKtx,      try_ktx},
                {PNG,  kDetectorIndexPng,      try_png},
                {PSD,  kDetectorIndexPsd,      try_psd},
                {QOI,  kDetectorIndexQoi,      try_qoi},
                {TIFF, kDetectorIndexTiff,     try_tiff},
                {WEBP, kDetectorIndexWebp,     try_webp},
                {TAG,  kDetectorIndexTga,      try_tga},
        };
        static_assert(FORMAT_COUNT == countof(dl), "FORMAT_COUNT != countof(dl)");
        static_assert(check_format_order(dl), "Format order is incorrect");

        bool tried[DETECTOR_COUNT] = {false};

        ImageInfo info;

        if (most_likely_format != Format::UNKNOWN) {
            auto detector = dl[most_likely_format - 1];
            if (detector.detect(ri, length, info)  //
                && (!must_be_one_of_likely_formats || info.format() == most_likely_format)) {
                return info;
            }
            tried[detector.index] = true;
        }

        for (auto format: likely_formats) {
            if (format == Format::UNKNOWN) {
                continue;
            }
            auto detector = dl[format - 1];
            if (tried[detector.index]) {
                continue;
            }
            if (detector.detect(ri, length, info)  //
                && (!must_be_one_of_likely_formats || info.format() == format)) {
                return info;
            }
            tried[detector.index] = true;
        }

        if (must_be_one_of_likely_formats) {
            return ImageInfo(UNRECOGNIZED_FORMAT);
        }

        for (auto &detector: dl) {
            if (tried[detector.index]) {
                continue;
            }
            if (detector.detect(ri, length, info)) {
                return info;
            }
            tried[detector.index] = true;
        }

        return ImageInfo(UNRECOGNIZED_FORMAT);
    }

    inline ImageInfo parse(ReadInterface &ri,                               //
                           const std::vector<Format> &likely_formats = {},  //
                           bool must_be_one_of_likely_formats = false) {    //
        return parse(ri, Format::UNKNOWN, likely_formats, must_be_one_of_likely_formats);
    }

    template<typename ReaderType, typename InputType>
    inline ImageInfo parse(const InputType &input,                          //
                           Format most_likely_format,                       //
                           const std::vector<Format> &likely_formats = {},  //
                           bool must_be_one_of_likely_formats = false) {    //
        ReaderType reader(input);
        size_t length = reader.size();
        ReadFunc read_func = [&reader](void *buf, off_t offset, size_t size) {
            reader.read(buf, offset, size);
        };
        ReadInterface ri(read_func, length);
        return parse(ri, most_likely_format, likely_formats, must_be_one_of_likely_formats);
    }

    template<typename ReaderType, typename InputType>
    inline ImageInfo parse(const InputType &input,                          //
                           const std::vector<Format> &likely_formats = {},  //
                           bool must_be_one_of_likely_formats = false) {    //
        return parse<ReaderType>(input, Format::UNKNOWN, likely_formats,
                                 must_be_one_of_likely_formats);
    }

}  // namespace SoundSource::Image

#ifdef __clang__
#pragma clang diagnostic pop
#endif

#endif  // IMAGEINFO_IMAGEINFO_H