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

package tech.rollw.player.ui.player.dialog

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import tech.rollw.player.R
import tech.rollw.player.audio.Audio
import tech.rollw.player.audio.AudioContent
import tech.rollw.player.audio.list.Playlist
import tech.rollw.player.audio.list.PlaylistType
import tech.rollw.player.audio.tag.Artwork
import tech.rollw.player.audio.tag.AudioTag
import tech.rollw.player.audio.tag.AudioTagField
import tech.rollw.player.audio.tag.TagUtils.openTag
import tech.rollw.player.data.database.repository.PlaylistItemRepository
import tech.rollw.player.data.database.repository.PlaylistRepository
import tech.rollw.player.getApplicationService
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.player.AudioUtils.formatDuration
import tech.rollw.player.ui.tools.ImageRequestUtils.cacheKey
import tech.rollw.player.util.formatTimestamp
import tech.rollw.support.io.ContentPath
import tech.rollw.support.io.Files.formatSize
import tech.rollw.support.io.PathType

private val TonalElevation = DialogDefaults.TonalElevation

@JvmInline
value class AudioInfoType private constructor(private val ordinal: Int) {
    companion object {
        val Info = AudioInfoType(0)

        val Lyric = AudioInfoType(1)

        val Cover = AudioInfoType(2)
    }
}

class AudioInfoDialogState(
    internal val initialPage: AudioInfoType = AudioInfoType.Info,
) {
    internal var page by mutableStateOf(initialPage)

    fun animateToPage(page: AudioInfoType) {
        this.page = page
    }
}

@Composable
fun rememberAudioInfoDialogState(
    initialPage: AudioInfoType = AudioInfoType.Info
): AudioInfoDialogState {
    return remember {
        AudioInfoDialogState(initialPage)
    }
}

@Composable
fun AudioInfoDialog(
    audioContent: AudioContent,
    modifier: Modifier = Modifier,
    audioInfoDialogState: AudioInfoDialogState = rememberAudioInfoDialogState(),
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    onDismissRequest: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var audioTag by remember { mutableStateOf<AudioTag?>(null) }

    val showBackButton = audioInfoDialogState.initialPage == AudioInfoType.Info

    LaunchedEffect(audioContent) {
        audioTag?.close()
        audioTag = audioContent.openTag(context, readonly = false)
    }

    // TODO: replace with ModalWindowedSheet when it's ready
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = {
            onDismissRequest()
        },
        dragHandle = null,
        containerColor = PlayerTheme.colorScheme.surface,
        tonalElevation = TonalElevation
    ) {
        AnimatedContent(
            targetState = audioInfoDialogState.page,
            transitionSpec = {
                if (audioInfoDialogState.page == AudioInfoType.Info) {
                    (fadeIn() + slideInHorizontally { -it })
                        .togetherWith(fadeOut() + slideOutHorizontally { it })
                } else {
                    (fadeIn() + slideInHorizontally { it })
                        .togetherWith(fadeOut() + slideOutHorizontally { -it })
                }
            }
        ) {
            when (it) {
                AudioInfoType.Info -> {
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        AudioInfoPage(
                            audioContent = audioContent,
                            audioTag = audioTag,
                            // TODO: apply padding internal, remove padding from Page
                            modifier = Modifier.padding(horizontal = 20.dp),
                            contentTypography = contentTypography,
                            onCoverClick = {
                                audioInfoDialogState.animateToPage(AudioInfoType.Cover)
                            }
                        )
                    }
                }

                AudioInfoType.Cover -> {
                    AudioCoverPage(
                        audioContent = audioContent,
                        audioTag = audioTag,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        contentTypography = contentTypography,
                        showBackButton = showBackButton,
                        onBackClick = {
                            audioInfoDialogState.animateToPage(AudioInfoType.Info)
                        }
                    )
                }

                AudioInfoType.Lyric -> {

                }
            }
        }
    }
}


@Composable
private fun AudioInfoPage(
    audioContent: AudioContent,
    audioTag: AudioTag?,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    onCoverClick: () -> Unit = {},
    onClickPlaylist: (Playlist) -> Unit = {}
) {
    var edit by remember { mutableStateOf(false) }

    BackHandler(edit) {
        edit = false
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        AudioNavigation(
            audioContent = audioContent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            contentTypography = contentTypography,
            onClickPlaylist = onClickPlaylist
        )

        Text(
            text = stringResource(R.string.audio_tags),
            modifier = Modifier.padding(bottom = 20.dp, top = 20.dp),
            style = contentTypography.title
        )
        PropertyRow(
            title = stringResource(R.string.title),
            content = audioContent.audio.title ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        PropertyRow(
            title = stringResource(R.string.artist),
            content = audioContent.audio.artist ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        PropertyRow(
            title = stringResource(R.string.album),
            content = audioContent.audio.album ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        PropertyRow(
            title = stringResource(R.string.album_artist),
            content = audioContent.audio.albumArtist ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        PropertyRow(
            title = stringResource(R.string.genre),
            content = audioContent.audio.genre ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        PropertyRow(
            title = stringResource(R.string.year),
            content = audioContent.audio.year ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        PropertyRow(
            title = stringResource(R.string.track_number),
            content = audioContent.audio.trackNo ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        PropertyRow(
            title = stringResource(R.string.disc_number),
            content = audioContent.audio.diskNo ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        PropertyRow(
            title = stringResource(R.string.composer),
            content = audioContent.audio.composer ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        PropertyRow(
            title = stringResource(R.string.lyricist),
            content = audioContent.audio.lyricist ?: "",
            contentTypography = contentTypography,
            editable = edit,
            onClick = {
            }
        )
        PropertyRow(
            title = stringResource(R.string.copyright),
            content = audioContent.audio.copyright ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        // TODO: load more comment and lyrics
        PropertyRow(
            title = stringResource(R.string.comment),
            content = audioTag?.getTagField(AudioTagField.COMMENT) ?: "",
            contentTypography = contentTypography,
            editable = edit
        )
        val artwork = audioTag?.getArtwork(includeData = false)
        PropertyRow(
            title = stringResource(R.string.cover),
            contentTypography = contentTypography
        ) {
            Text(
                text = artwork.format(),
                style = contentTypography.body,
                color = PlayerTheme.colorScheme.onSurface
            )

            TextButton(
                onClick = {
                    onCoverClick()
                },
                modifier = Modifier
                    .padding(top = 5.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.cover),
                    style = contentTypography.body,
                    color = PlayerTheme.colorScheme.onSurface,
                    textDecoration = TextDecoration.Underline
                )
            }
        }

        ActionButtons(
            modifier = Modifier.padding(top = 20.dp),
            isEdit = edit,
            onEditClick = { edit = true },
            onSaveClick = { edit = false },
            onCancelClick = { edit = false }
        )

        Text(
            text = stringResource(R.string.audio_info),
            modifier = Modifier.padding(vertical = 20.dp),
            style = contentTypography.title
        )
        PropertyRow(
            title = stringResource(R.string.duration),
            content = "${audioContent.audio.formatDuration()} (${audioContent.audio.duration / 1000} s)",
            contentTypography = contentTypography
        )
        PropertyRow(
            title = stringResource(R.string.sample_rate),
            content = formatSampleRate(audioContent.audio),
            contentTypography = contentTypography
        )
        PropertyRow(
            title = stringResource(R.string.bit_rate),
            content = formatBitRate(audioContent.audio),
            contentTypography = contentTypography
        )
        PropertyRow(
            title = stringResource(R.string.bit_depth),
            content = formatBitDepth(audioContent.audio),
            contentTypography = contentTypography
        )
        PropertyRow(
            title = stringResource(R.string.channels),
            content = audioContent.audio.channels.toString(),
            contentTypography = contentTypography
        )
        PropertyRow(
            title = stringResource(R.string.format),
            content = audioContent.audio.type.name,
            contentTypography = contentTypography
        )
        PropertyRow(
            title = stringResource(R.string.path),
            content = formatPath(audioContent.path),
            contentTypography = contentTypography
        )
        PropertyRow(
            title = stringResource(R.string.file_size),
            content = formatSize(audioContent.audio),
            contentTypography = contentTypography
        )
        PropertyRow(
            title = stringResource(R.string.last_modified),
            content = audioContent.audio.lastModified.formatTimestamp(),
            contentTypography = contentTypography
        )
        PropertyRow(
            title = stringResource(R.string.added_time),
            content = audioContent.audio.createTime.formatTimestamp(),
            contentTypography = contentTypography
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun AudioNavigation(
    audioContent: AudioContent,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    onClickPlaylist: (Playlist) -> Unit = {}
) {
    val context = LocalContext.current

    // TODO: move to view model
    val playlistRepository = context.getApplicationService<PlaylistRepository>()
    val playlistItemRepository = context.getApplicationService<PlaylistItemRepository>()

    val playlistItems by playlistItemRepository.getByAudioFlow(audioContent.audio.id!!)
        .collectAsState(initial = emptyList())
    val playlists by playlistRepository.getByIdsFlow(
        playlistItems.map { it.playlistId }
    ).collectAsState(initial = emptyList())

    val album = playlists.firstOrNull { it.type == PlaylistType.ALBUM }
    val artists = playlists.filter { it.type == PlaylistType.ARTIST }

    Column(
        modifier = modifier
    ) {
        Text(
            text = audioContent.audio.title ?: "",
            style = contentTypography.title,
            color = PlayerTheme.colorScheme.secondary
        )
        Text(
            text = stringResource(id = R.string.artist),
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
            style = contentTypography.body,
            color = PlayerTheme.colorScheme.secondary
        )
        for (artist in artists) {
            PlaylistDetail(
                playlist = artist,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                contentTypography = contentTypography,
                onClickPlaylist = onClickPlaylist
            )
        }

        Text(
            text = stringResource(id = R.string.album),
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
            style = contentTypography.body,
            color = PlayerTheme.colorScheme.secondary
        )

        PlaylistDetail(
            playlist = album ?: Playlist.EMPTY,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            contentTypography = contentTypography,
            onClickPlaylist = onClickPlaylist
        )
    }
}

@Composable
private fun PlaylistDetail(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    onClickPlaylist: (Playlist) -> Unit = {}
) {
    if (playlist == Playlist.EMPTY) {
        Column(
            modifier = modifier
        ) {
            Text(
                text = "Unknown",
                style = contentTypography.subtitle,
                color = PlayerTheme.colorScheme.onSurface
            )
        }
        return
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(15))
            .clickable {
                onClickPlaylist(playlist)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(playlist.coverPath)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .padding(5.dp)
                .size(60.dp)
                .clip(RoundedCornerShape(15)),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
        )
        Text(
            text = playlist.name,
            modifier = Modifier.padding(start = 16.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
            style = contentTypography.subtitle,
            color = PlayerTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AudioCoverPage(
    audioContent: AudioContent,
    audioTag: AudioTag?,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit = {}
) {
    BackHandler(
        enabled = showBackButton
    ) {
        onBackClick()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBackButton) {
                IconButton(
                    modifier = Modifier.padding(end = 10.dp),
                    onClick = {
                        onBackClick()
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_arrow_back_24),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
            Text(
                text = stringResource(R.string.cover),
                style = contentTypography.title
            )
        }

        if (audioTag != null) {
            val artwork = audioTag.getArtwork(includeData = true)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = artwork.format(),
                    style = contentTypography.body,
                    color = PlayerTheme.colorScheme.onSurface
                )
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(audioContent.path)
                        .apply {
                            memoryCacheKey(audioContent.cacheKey)
                            memoryCachePolicy(CachePolicy.ENABLED)
                            diskCachePolicy(CachePolicy.DISABLED)
                        }
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(20.dp)
                        .size(260.dp)
                        .clip(PlayerTheme.shapes.medium),
                )
            }
        }
    }
}

private fun Artwork?.format(): String {
    if (this == null) {
        return ""
    }
    return "${width}x$height $format (${length.formatSize(2)})"
}

private fun formatPath(contentPath: ContentPath): String {
    if (contentPath.type == PathType.FILE) {
        return contentPath.path
    }
    val lastPathSegment = contentPath.path.toUri().lastPathSegment
    return Uri.decode(lastPathSegment) ?: ""
}

private fun formatBitRate(audio: Audio): String = "${audio.bitRate} Kbps"

private fun formatBitDepth(audio: Audio): String {
    return if (audio.bitDepth <= 0) {
        "16 bit"
    } else {
        "${audio.bitDepth} bit"
    }
}

private fun formatSampleRate(audio: Audio): String = "${audio.sampleRate} Hz"

private fun formatSize(audio: Audio): String = audio.size.formatSize(2)

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    isEdit: Boolean = false,
    onEditClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        AnimatedVisibility(
            visible = isEdit,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it }
        ) {
            OutlinedButton(onClick = {
                onCancelClick()
            }) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = PlayerTheme.typography.contentNormal.body,
                    color = PlayerTheme.colorScheme.secondary
                )
            }
        }
        FilledTonalButton(
            onClick = {
                if (isEdit) {
                    onSaveClick()
                } else {
                    onEditClick()
                }
            },
            modifier = Modifier.padding(start = 20.dp)
        ) {
            AnimatedContent(
                targetState = isEdit,
            ) {
                when (it) {
                    true -> {
                        Text(
                            text = stringResource(R.string.save),
                            style = PlayerTheme.typography.contentNormal.body
                        )
                    }

                    false -> {
                        Text(
                            text = stringResource(R.string.edit),
                            style = PlayerTheme.typography.contentNormal.body
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyRow(
    title: String,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = title,
            modifier = Modifier
                .weight(2f)
                .wrapContentWidth(Alignment.Start),
            style = contentTypography.body,
            color = PlayerTheme.colorScheme.secondary
        )
        Column(
            modifier = Modifier
                .weight(8f)
                .wrapContentWidth(Alignment.End),
            horizontalAlignment = Alignment.End
        ) {
            content()
        }
    }
}


@Composable
private fun PropertyRow(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    onContentChange: (String) -> Unit = {},
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
    splitter: ((String) -> Collection<String>)? = null,
    onClick: ((String) -> Unit)? = null
) {
    var rowSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .onGloballyPositioned {
                rowSize = it.size
            },
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = title,
            modifier = Modifier
                .width(with(density) {
                    (rowSize.width * 0.2f).toDp()
                })
                .wrapContentWidth(Alignment.Start),
            style = contentTypography.body,
            color = PlayerTheme.colorScheme.secondary
        )

        val textColor = PlayerTheme.colorScheme.onSurface

        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        var textFieldFocused by remember { mutableStateOf(false) }

        BackHandler(textFieldFocused) {
            focusManager.clearFocus()
        }

        val contentWidth = rowSize.width * 0.8f

        AnimatedContent(
            targetState = editable,
            modifier = Modifier
                .width(with(density) {
                    (contentWidth).toDp()
                })
                .wrapContentWidth(Alignment.End),
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { it ->
            when (it) {
                false -> {
                    Text(
                        modifier = Modifier.then(
                            if (onClick != null) Modifier.clickable {
                                onClick(content)
                            } else Modifier
                        ),
                        text = content,
                        textAlign = TextAlign.End,
                        style = contentTypography.body,
                        color = textColor,
                        textDecoration = if (onClick != null) TextDecoration.Underline
                        else TextDecoration.None,
                    )
                }

                true -> {
                    var value by rememberSaveable { mutableStateOf(content) }
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                textFieldFocused = it.isFocused
                            },
                        value = value,
                        onValueChange = {
                            value = it
                            onContentChange(value)
                        },
                        textStyle = contentTypography.body.copy(
                            color = PlayerTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        ),
                        decorationBox = {
                            it()
                        }
                    )
                }
            }
        }
    }
}
