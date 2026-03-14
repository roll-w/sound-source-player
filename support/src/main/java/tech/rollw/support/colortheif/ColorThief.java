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

/*
 * Java Color Thief
 * by Sven Woltmann, Fonpit AG
 * 
 * https://www.androidpit.com
 * https://www.androidpit.de
 *
 * License
 * -------
 * Creative Commons Attribution 2.5 License:
 * http://creativecommons.org/licenses/by/2.5/
 *
 * Thanks
 * ------
 * Lokesh Dhakar - for the original Color Thief JavaScript version
 * available at http://lokeshdhakar.com/projects/color-thief/
 */

package tech.rollw.support.colortheif;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class ColorThief {

    private static final int DEFAULT_QUALITY = 10;
    private static final boolean DEFAULT_IGNORE_WHITE = true;

    /**
     * Use the median cut algorithm to cluster similar colors and return the base color from the
     * largest cluster.
     *
     * @param sourceImage
     *            the source image
     *
     * @return the dominant color as RGB array
     */
    public static int[] getColor(Bitmap sourceImage) {
        int[][] palette = getPalette(sourceImage, 5);
        if (palette == null) {
            return null;
        }
        return palette[0];
    }

    /**
     * Use the median cut algorithm to cluster similar colors and return the base color from the
     * largest cluster.
     *
     * @param sourceImage
     *            the source image
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster a color will be returned but
     *            the greater the likelihood that it will not be the visually most dominant color.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     *
     * @return the dominant color as RGB array
     * @throws IllegalArgumentException
     *             if quality is &lt; 1
     */
    public static int[] getColor(Bitmap sourceImage, int quality, boolean ignoreWhite) {
        int[][] palette = getPalette(sourceImage, 5, quality, ignoreWhite);
        if (palette == null) {
            return null;
        }
        return palette[0];
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     * 
     * @param sourceImage
     *            the source image
     * @param colorCount
     *            the size of the palette; the number of colors returned
     * 
     * @return the palette as array of RGB arrays
     */
    public static int[][] getPalette(Bitmap sourceImage, int colorCount) {
        ThiefHelper.ColorMap cmap = getColorMap(sourceImage, colorCount);
        if (cmap == null) {
            return null;
        }
        return cmap.palette();
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     * 
     * @param sourceImage
     *            the source image
     * @param colorCount
     *            the size of the palette; the number of colors returned
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster the palette generation but
     *            the greater the likelihood that colors will be missed.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     * 
     * @return the palette as array of RGB arrays
     * @throws IllegalArgumentException
     *             if quality is &lt; 1
     */
    public static int[][] getPalette(
            Bitmap sourceImage,
            int colorCount,
            int quality,
            boolean ignoreWhite) {
        ThiefHelper.ColorMap cmap = getColorMap(sourceImage, colorCount, quality, ignoreWhite);
        if (cmap == null) {
            return null;
        }
        return cmap.palette();
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     * 
     * @param sourceImage
     *            the source image
     * @param colorCount
     *            the size of the palette; the number of colors returned (minimum 2, maximum 256)
     * 
     * @return the color map
     */
    public static ThiefHelper.ColorMap getColorMap(Bitmap sourceImage, int colorCount) {
        return getColorMap(sourceImage, colorCount, DEFAULT_QUALITY, DEFAULT_IGNORE_WHITE);
    }

    /**
     * Use the median cut algorithm to cluster similar colors.
     * 
     * @param sourceImage
     *            the source image
     * @param colorCount
     *            the size of the palette; the number of colors returned (minimum 2, maximum 256)
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster the palette generation but
     *            the greater the likelihood that colors will be missed.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     * 
     * @return the color map
     * @throws IllegalArgumentException
     *             if quality is &lt; 1
     */
    public static ThiefHelper.ColorMap getColorMap(
            Bitmap sourceImage,
            int colorCount,
            int quality,
            boolean ignoreWhite) {
        if (colorCount < 2 || colorCount > 256) {
            throw new IllegalArgumentException("Specified colorCount must be between 2 and 256.");
        }
        if (quality < 1) {
            throw new IllegalArgumentException("Specified quality should be greater then 0.");
        }

        int[][] pixelArray;
        pixelArray = getPixelsSlow(sourceImage, quality, ignoreWhite);

        // Send array to quantize function which clusters values using median cut algorithm
        return ThiefHelper.quantize(pixelArray, colorCount);
    }

    private static byte[] bitmapToBytes(Bitmap bitmap) {
        if (bitmap == null){
            return null;
        }
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        bitmap.copy(bitmap.getConfig(), false)
                .compress(Bitmap.CompressFormat.PNG, 100, bas);
        return bas.toByteArray();
    }

    /**
     * Gets the image's pixels. Fast, but doesn't work
     * for all color models.
     * 
     * @param sourceImage
     *            the source image
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster the palette generation but
     *            the greater the likelihood that colors will be missed.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     * 
     * @return an array of pixels (each an RGB int array)
     */
    private static int[][] getPixelsFast(
            Bitmap sourceImage,
            int quality,
            boolean ignoreWhite) {
        byte[] pixels = bitmapToBytes(sourceImage);
        int pixelCount = sourceImage.getWidth() * sourceImage.getHeight();

        int colorDepth;
        Bitmap.Config type = sourceImage.getConfig();
        switch (type) {
            case ALPHA_8:
                colorDepth = 1;
                break;
            case ARGB_4444:
                colorDepth = 2;
                break;
            case RGB_565:
                colorDepth = 3;
                break;
            case HARDWARE:
            case ARGB_8888:
                colorDepth = 4;
                break;
            case RGBA_F16:
                colorDepth = 8;
                break;
            default:
                throw new IllegalArgumentException("Unhandled type: " + type);
        }

        int expectedDataLength = pixelCount * colorDepth;
        if (expectedDataLength != pixels.length) {
            throw new IllegalArgumentException(
                    "(expectedDataLength = " + expectedDataLength + ") != (pixels.length = "
                            + pixels.length + ")" + sourceImage.getConfig());
        }

        // Store the RGB values in an array format suitable for quantize function

        // numRegardedPixels must be rounded up to avoid an ArrayIndexOutOfBoundsException if all
        // pixels are good.
        int numRegardedPixels = (pixelCount + quality - 1) / quality;

        int numUsedPixels = 0;
        int[][] pixelArray = new int[numRegardedPixels][];
        int offset, r, g, b, a;

        // Do the switch outside of the loop, that's much faster
        for (int i = 0; i < pixelCount; i += quality) {
            offset = i * colorDepth;
            a = pixels[offset] & 0xFF;
            b = pixels[offset + 1] & 0xFF;
            g = pixels[offset + 2] & 0xFF;
            r = pixels[offset + 3] & 0xFF;

            // If pixel is mostly opaque and not white
            if (a >= 125 && !(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                pixelArray[numUsedPixels] = new int[] {r, g, b};
                numUsedPixels++;
            }
        }

        // Remove unused pixels from the array
        return Arrays.copyOfRange(pixelArray, 0, numUsedPixels);
    }

    /**
     * Gets the image's pixels. Slow, but the fast method doesn't work
     * for all color models.
     * 
     * @param sourceImage
     *            the source image
     * @param quality
     *            1 is the highest quality settings. 10 is the default. There is a trade-off between
     *            quality and speed. The bigger the number, the faster the palette generation but
     *            the greater the likelihood that colors will be missed.
     * @param ignoreWhite
     *            if <code>true</code>, white pixels are ignored
     * 
     * @return an array of pixels (each an RGB int array)
     */
    private static int[][] getPixelsSlow(
            Bitmap sourceImage,
            int quality,
            boolean ignoreWhite) {
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        int pixelCount = width * height;

        // numRegardedPixels must be rounded up to avoid an ArrayIndexOutOfBoundsException if all
        // pixels are good.
        int numRegardedPixels = (pixelCount + quality - 1) / quality;
        int numUsedPixels = 0;

        int[][] res = new int[numRegardedPixels][];
        int r, g, b;

        int[] pixels = new int[width * height];
        sourceImage.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i += quality) {
            int rgb = pixels[i];
            r = (rgb >> 16) & 0xff;
            g = (rgb >>  8) & 0xff;
            b = (rgb      ) & 0xff;

            if (!(ignoreWhite && r > 250 && g > 250 && b > 250)) {
                res[numUsedPixels] = new int[] {r, g, b};
                numUsedPixels++;
            }
        }

        return Arrays.copyOfRange(res, 0, numUsedPixels);
    }

}
