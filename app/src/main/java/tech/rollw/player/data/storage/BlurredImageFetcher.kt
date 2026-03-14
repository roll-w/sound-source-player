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

package tech.rollw.player.data.storage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import tech.rollw.player.data.media.BlurredContentImage
import tech.rollw.player.ui.applicationService
import tech.rollw.player.util.ImageUtils
import kotlin.math.min

/**
 * @author RollW
 */
class BlurredImageFetcher(
    private val blurredContentImage: BlurredContentImage,
    private val options: Options
) : Fetcher {
    private val loader by options.context
        .applicationService<LocalImageLoader>()

    override suspend fun fetch(): FetchResult? {
        val data = loader.load(blurredContentImage.contentPath)
            ?: return null
        val read = BitmapFactory.decodeByteArray(
            data, 0, data.size
        )
        val scaled = scaleBitmap(read)
        val blurred = ImageUtils.blur(
            scaled,
            blurredContentImage.radius,
            copy = false
        )

        try {
            return DrawableResult(
                drawable = blurred.toDrawable(
                    options.context.resources
                ),
                isSampled = false,
                dataSource = DataSource.DISK
            )
        } finally {
            read.recycle()
        }
    }

    class Factory : Fetcher.Factory<BlurredContentImage> {
        override fun create(
            data: BlurredContentImage,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return BlurredImageFetcher(data, options)
        }
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scaleWidth = min(width, 200)
        val scaleHeight = (scaleWidth * height / width)
        return bitmap.scale(scaleWidth, scaleHeight)
    }
}