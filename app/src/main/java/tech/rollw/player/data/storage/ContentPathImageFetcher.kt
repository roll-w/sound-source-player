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

import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer
import tech.rollw.player.ui.applicationService
import tech.rollw.support.io.ContentPath

/**
 * @author RollW
 */
class ContentPathImageFetcher(
    private val path: ContentPath,
    private val options: Options
) : Fetcher {
    private val loader by options.context
        .applicationService<LocalImageLoader>()

    override suspend fun fetch(): FetchResult? {
        val data = loader.load(path) ?: return null
        val source = Buffer().apply {
            write(data)
        }
        return SourceResult(
            source = ImageSource(source = source, options.context),
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    class Factory : Fetcher.Factory<ContentPath> {
        override fun create(
            data: ContentPath,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return ContentPathImageFetcher(data, options)
        }
    }
}