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

#include "image.h"
#include <malloc.h>
#include <algorithm>

#define ABS(a) ((a)<(0)?(-(a)):(a))
#define MAX(a, b) ((a)>(b)?(a):(b))
#define MIN(a, b) ((a)<(b)?(a):(b))

namespace SoundSource::Image {
    int16_t *ImageProcessor::BlurRgb565(int16_t *pix, int32_t w, int32_t h, int32_t radius) {
        int32_t wm = w - 1;
        int32_t hm = h - 1;
        int32_t wh = w * h;
        // pixel size
        int32_t div = radius + radius + 1;

        auto *r = (int16_t *) malloc(wh * sizeof(int16_t));
        auto *g = (int16_t *) malloc(wh * sizeof(int16_t));
        auto *b = (int16_t *) malloc(wh * sizeof(int16_t));

        int32_t r_sum, g_sum, b_sum, x, y, p, i, yp, yi, yw;

        auto *v_min = (int32_t *) malloc(MAX(w, h) * sizeof(int32_t));

        int32_t div_sum = (div + 1) >> 1;
        div_sum *= div_sum;

        auto *dv = (int16_t *) malloc(256 * div_sum * sizeof(int16_t));

        for (i = 0; i < 256 * div_sum; i++) {
            dv[i] = (int16_t) (i / div_sum);
        }

        yw = yi = 0;

        auto stack = (int32_t (*)[3]) malloc(div * 3 * sizeof(int32_t));
        int32_t stackpointer;
        int32_t stackstart;
        int32_t *sir;
        int32_t rbs;
        int32_t r1 = radius + 1;
        int32_t routsum, goutsum, boutsum;
        int32_t rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = r_sum = g_sum = b_sum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + (MIN(wm, MAX(i, 0)))];
                sir = stack[i + radius];
                sir[0] = (((p) & 0xF800) >> 11) << 3;
                sir[1] = (((p) & 0x7E0) >> 5) << 2;
                sir[2] = ((p) & 0x1F) << 3;

                rbs = r1 - ABS(i);
                r_sum += sir[0] * rbs;
                g_sum += sir[1] * rbs;
                b_sum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[r_sum];
                g[yi] = dv[g_sum];
                b[yi] = dv[b_sum];

                r_sum -= routsum;
                g_sum -= goutsum;
                b_sum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    v_min[x] = MIN(x + radius + 1, wm);
                }
                p = pix[yw + v_min[x]];

                sir[0] = (((p) & 0xF800) >> 11) << 3;
                sir[1] = (((p) & 0x7E0) >> 5) << 2;
                sir[2] = ((p) & 0x1F) << 3;

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                r_sum += rinsum;
                g_sum += ginsum;
                b_sum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = r_sum = g_sum = b_sum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = MAX(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - ABS(i);

                r_sum += r[yi] * rbs;
                g_sum += g[yi] * rbs;
                b_sum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Not have alpha channel
                pix[yi] = ((((dv[r_sum]) >> 3) << 11) | (((dv[g_sum]) >> 2) << 5) |
                           ((dv[b_sum]) >> 3));

                r_sum -= routsum;
                g_sum -= goutsum;
                b_sum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    v_min[y] = MIN(y + r1, hm) * w;
                }
                p = x + v_min[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                r_sum += rinsum;
                g_sum += ginsum;
                b_sum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        free(r);
        free(g);
        free(b);
        free(v_min);
        free(dv);
        free(stack);
        return (pix);
    }

    int32_t *ImageProcessor::BlurArgb8888(int32_t *pix, int32_t w, int32_t h, int32_t radius) {
        int32_t wm = w - 1;
        int32_t hm = h - 1;
        int32_t wh = w * h;
        int32_t div = radius + radius + 1;

        auto *r = (int16_t *) malloc(wh * sizeof(int16_t));
        auto *g = (int16_t *) malloc(wh * sizeof(int16_t));
        auto *b = (int16_t *) malloc(wh * sizeof(int16_t));
        int32_t rsum, gsum, bsum, x, y, i, p, yp, yi, yw;

        auto *vmin = (int32_t *) malloc(MAX(w, h) * sizeof(int32_t));

        int32_t divsum = (div + 1) >> 1;
        divsum *= divsum;
        auto *dv = (int16_t *) malloc(256 * divsum * sizeof(int16_t));
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (int16_t) (i / divsum);
        }

        yw = yi = 0;

        auto stack = (int32_t (*)[3]) malloc(div * 3 * sizeof(int32_t));
        int32_t stack_pointer;
        int32_t stack_start;
        int32_t *sir;
        int32_t rbs;
        int32_t r1 = radius + 1;
        int32_t routsum, goutsum, boutsum;
        int32_t rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + (MIN(wm, MAX(i, 0)))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rbs = r1 - ABS(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stack_pointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stack_start = stack_pointer - radius + div;
                sir = stack[stack_start % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = MIN(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stack_pointer = (stack_pointer + 1) % div;
                sir = stack[(stack_pointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = MAX(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - ABS(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stack_pointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stack_start = stack_pointer - radius + div;
                sir = stack[stack_start % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = MIN(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stack_pointer = (stack_pointer + 1) % div;
                sir = stack[stack_pointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        free(r);
        free(g);
        free(b);
        free(vmin);
        free(dv);
        free(stack);
        return (pix);
    }

    ImageInfo getImageInfo(const char *path) {
        ImageInfo info = parse<FilePathReader>(path);
        return info;
    }

    ImageInfo getImageInfo(const void *data, size_t size) {
        ImageInfo info = parse<RawDataReader>(RawData(data, size));
        return info;
    }
}
