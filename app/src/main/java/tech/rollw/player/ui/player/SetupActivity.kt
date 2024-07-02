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

package tech.rollw.player.ui.player

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tech.rollw.player.R
import tech.rollw.player.data.setting.AppSettings
import tech.rollw.player.data.setting.SettingValue
import tech.rollw.player.data.setting.UserSettings
import tech.rollw.player.service.scanner.AudioScanWorker
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme
import tech.rollw.player.ui.theme.SoundSourceTheme
import tech.rollw.player.ui.tools.rememberSetting
import tech.rollw.support.Switch
import tech.rollw.support.appcompat.AppActivity
import tech.rollw.support.net.UriUtils.getLastPath

/**
 * Start if the user has not set up the app, the user will be asked to
 * set up the app (select the audio folder, etc.) and then enter
 * the [MainActivity].
 *
 * @author RollW
 */
@Suppress("AnimatedContentLabel")
class SetupActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerActivityLauncher(ActivityResultContracts.OpenDocumentTree())

        setStatusBar(colorBackground = 0, lightBar = Switch.NONE)
        setNavigationBar(colorBackground = 0, lightBar = Switch.NONE)
        val extras = intent.extras

        val navigateMainActivity = extras?.getBoolean(EXTRA_NAVIGATE_TO_MAIN)
            ?: false

        var appSetup by SettingValue(AppSettings.AppSetup, this)

        setContent {
            SoundSourceTheme {
                SetupScreen(
                    modifier = Modifier.fillMaxSize(),
                    contentTypography = PlayerTheme.typography.contentLarge
                ) {
                    appSetup = true

                    if (navigateMainActivity) {
                        startMainActivity()
                    }
                    finish()
                }
            }
        }
    }

    private fun startMainActivity() {
        val mainActivityIntent = getOrCreateActivityIntent(MainActivity::class.java)
        startActivity(mainActivityIntent)
    }

    companion object {
        private const val PAGER_COUNT = 5

        private const val SCAN_NONE = 0
        private const val SCAN_RUNNING = 1
        private const val SCAN_DONE = 2

        const val EXTRA_NAVIGATE_TO_MAIN = "tech.rollw.player.NAVIGATE_TO_MAIN"
    }

    @Composable
    private fun SetupScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onFinish: () -> Unit = {}
    ) {
        var currentPage by remember { mutableIntStateOf(0) }
        val pagerState = rememberPagerState(
            initialPage = 0
        ) {
            PAGER_COUNT
        }

        LaunchedEffect(currentPage) {
            pagerState.animateScrollToPage(currentPage)
        }

        val onClickPrev: () -> Unit = onClickPrev@{
            if (currentPage == 0) {
                onFinish()
                return@onClickPrev
            }

            if (currentPage > 0) {
                currentPage--
            }
        }

        BackHandler {
            onClickPrev()
        }

        val onClickContinue: () -> Unit = onClickContinue@{
            if (currentPage == PAGER_COUNT - 1) {
                onFinish()
                return@onClickContinue
            }

            if (currentPage < PAGER_COUNT - 1) {
                currentPage++
            }
        }

        val padding = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp, vertical = 40.dp)

        val screens: List<@Composable (() -> Unit)> = listOf(
            { WelcomeScreen(padding, contentTypography, onClickPrev, onClickContinue) },
            { ProfileScreen(padding, contentTypography, onClickPrev, onClickContinue) },
            { PermissionScreen(padding, contentTypography, onClickPrev, onClickContinue) },
            { AudioScanScreen(padding, contentTypography, onClickPrev, onClickContinue) },
            { EndSetupScreen(padding, contentTypography, onClickPrev, onClickContinue) }
        )

        HorizontalPager(
            state = pagerState,
            modifier = modifier,
            userScrollEnabled = false,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens[it]()
        }
    }

    /**
     * The Welcome screen shows the app logo and a button to start the setup process.
     */
    @Composable
    private fun WelcomeScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {}
    ) {
        SetupScreenLayout(
            modifier = modifier,
            onClickPrev = onClickPrev,
            onClickContinue = onClickContinue,
            backLabel = stringResource(R.string.skip)
        ) {
            Image(
                painter = BitmapPainter(
                    ResourcesCompat.getDrawable(resources, R.mipmap.ic_logo, null)!!
                        .toBitmap()
                        .asImageBitmap()
                ),
                contentDescription = "Logo",
                modifier = Modifier
                    .padding(top = 40.dp)
                    .size(200.dp)
            )

            TitleWithDescription(
                title = stringResource(R.string.setup_welcome_title),
                description = stringResource(R.string.setup_welcome_desc),
                contentTypography = contentTypography
            )
        }
    }

    /**
     * The Profile screen allows the user to set their name.
     */
    @Composable
    private fun ProfileScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {}
    ) {
        var username by rememberSetting(UserSettings.Username)
        var input by rememberSaveable { mutableStateOf("") }

        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        var textFieldFocus by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            input = username ?: ""
        }

        val onClickContinueExtend: () -> Unit = {
            username = input
            focusManager.clearFocus()
            onClickContinue()
        }

        val onClickPrevExtend: () -> Unit = {
            focusManager.clearFocus()
            onClickPrev()
        }

        BackHandler(enabled = textFieldFocus) {
            focusManager.clearFocus()
        }

        SetupScreenLayout(
            modifier = modifier,
            onClickPrev = onClickPrevExtend,
            onClickContinue = onClickContinueExtend
        ) {
            TitleWithDescription(
                title = stringResource(R.string.setup_profile_title),
                description = stringResource(R.string.setup_profile_desc),
                contentTypography = contentTypography
            )

            Column(
                modifier = Modifier.padding(top = 40.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            textFieldFocus = it.isFocused
                        },
                    value = input,
                    singleLine = true,
                    onValueChange = {
                        input = it
                    },
                    label = {
                        Text("Name")
                    },
                )
            }
        }
    }

    @Composable
    private fun PermissionScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {}
    ) {
        val buildTiramisu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

        val description = if (buildTiramisu) {
            stringResource(R.string.setup_permission_desc)
        } else {
            stringResource(R.string.setup_permission_desc_none)
        }

        SetupScreenLayout(
            modifier = modifier,
            onClickPrev = onClickPrev,
            onClickContinue = onClickContinue
        ) {
            TitleWithDescription(
                title = stringResource(R.string.setup_permission_title),
                description = description,
                contentTypography = contentTypography
            )

            Column(
                modifier = Modifier.padding(top = 40.dp),
            ) {
                if (buildTiramisu) {
                    NotificationPermissionItem()
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    private fun NotificationPermissionItem() {
        val permissionState = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        ) {
        }

        PermissionItem(
            title = stringResource(R.string.setup_permission_notification_title),
            description = stringResource(R.string.setup_permission_notification_desc),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            allowed = permissionState.status.isGranted,
            onClick = {
                permissionState.launchPermissionRequest()
            }
        )
    }

    @Composable
    private fun AudioScanScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {}
    ) {
        val permissions = remember {
            mutableStateListOf<Uri>()
        }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) {
            if (it == null) {
                return@rememberLauncherForActivityResult
            }
            contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            permissions.add(it)
        }

        LaunchedEffect(Unit) {
            contentResolver.persistedUriPermissions.forEach {
                permissions.add(it.uri)
            }
        }

        val scanWorkInfo by findScanWorkerInfo().collectAsState(initial = null)

        fun getScanState(): Int {
            if (scanWorkInfo == null) {
                return SCAN_NONE
            }
            return when (scanWorkInfo?.state) {
                WorkInfo.State.ENQUEUED,
                WorkInfo.State.BLOCKED,
                WorkInfo.State.RUNNING -> {
                    SCAN_RUNNING
                }

                WorkInfo.State.SUCCEEDED,
                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED -> {
                    SCAN_DONE
                }

                else -> SCAN_NONE
            }
        }

        var scanning by remember { mutableIntStateOf(getScanState()) }

        LaunchedEffect(scanWorkInfo) {
            scanning = getScanState()
        }

        SetupScreenLayout(
            modifier = modifier,
            onClickPrev = onClickPrev,
            onClickContinue = onClickContinue
        ) {
            TitleWithDescription(
                title = stringResource(R.string.setup_audio_scan_title),
                description = stringResource(R.string.setup_audio_scan_desc),
                contentTypography = contentTypography
            )

            Column(
                modifier = Modifier.padding(top = 20.dp),
            ) {
                permissions.forEach {
                    // TODO: add animation for adding and removing
                    UriPermissionItem(
                        uri = it,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth(),
                        contentTypography = contentTypography,
                        onClickRemove = {
                            contentResolver.releasePersistableUriPermission(
                                it,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            )
                            permissions.remove(it)
                        }
                    )
                }

                AddPermissionComponent(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentTypography = contentTypography,
                    scanning = scanning,
                    onClickAdd = {
                        launcher.launch(null)
                    },
                    onClickScan = {
                        AudioScanWorker.submitWork(this@SetupActivity)
                    },
                    onClickScanDone = {
                        scanning = SCAN_NONE
                    }
                )
            }
        }
    }

    private fun findScanWorkerInfo(): Flow<WorkInfo?> {
        return WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkFlow(AudioScanWorker.WORKER_SPEC.tag)
            .distinctUntilChanged()
            .map { it.firstOrNull() }
    }

    @Composable
    private fun AddPermissionComponent(
        modifier: Modifier = Modifier,
        scanning: Int = SCAN_NONE,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickAdd: () -> Unit = {},
        onClickScan: () -> Unit = {},
        onClickScanDone: () -> Unit = {}
    ) {
        @Composable
        fun TextIconButton(
            text: String,
            modifier: Modifier = Modifier,
            icon: @Composable () -> Unit = {},
            onClick: () -> Unit = {}
        ) {
            Row(
                modifier = modifier
                    .clickable {
                        onClick()
                    }
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                icon()
                Text(
                    text = text,
                    style = contentTypography.body,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Row(
            modifier = modifier
                .padding(vertical = 10.dp)
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.Start)
                    .clip(RoundedCornerShape(25))
            ) {
                TextIconButton(
                    text = stringResource(R.string.add),
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add),
                            modifier = Modifier
                                .padding(horizontal = 5.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    onClick = onClickAdd
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 10.dp)
            )

            var contentHeight by remember { mutableIntStateOf(0) }

            AnimatedContent(
                targetState = scanning,
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.End)
                    .clip(RoundedCornerShape(25))
                    .onGloballyPositioned {
                        contentHeight = it.size.height
                    }
            ) {
                val text = when (it) {
                    SCAN_NONE -> stringResource(R.string.scan)
                    SCAN_DONE -> "Done"
                    else -> ""
                }

                val icon = when (it) {
                    SCAN_NONE -> Icons.Filled.Refresh
                    SCAN_DONE -> Icons.Filled.Check
                    else -> Icons.Filled.Refresh
                }

                when (it) {
                    SCAN_NONE, SCAN_DONE -> {
                        TextIconButton(
                            text = text,
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = stringResource(R.string.scan),
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            onClick = {
                                if (it == SCAN_NONE) {
                                    onClickScan()
                                } else {
                                    onClickScanDone()
                                }
                            }
                        )
                    }

                    SCAN_RUNNING -> {
                        Row(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .progressSemantics()
                                    .size(with(LocalDensity.current) {
                                        20.sp.toDp()
                                    }),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = Color.Transparent,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }
            }
        }
    }


    @Composable
    private fun UriPermissionItem(
        uri: Uri,
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickRemove: () -> Unit = {}
    ) {
        Column(
            modifier = modifier
                .height(IntrinsicSize.Min)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = uri.getLastPath().removePrefix("primary:"),
                    textAlign = TextAlign.Start,
                    style = contentTypography.body,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                IconButton(onClick = onClickRemove) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_close_24),
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    @Composable
    private fun EndSetupScreen(
        modifier: Modifier = Modifier,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {}
    ) {
        SetupScreenLayout(
            modifier = modifier,
            onClickPrev = onClickPrev,
            onClickContinue = onClickContinue,
            continueLabel = stringResource(R.string.complete)
        ) {
            TitleWithDescription(
                title = stringResource(R.string.setup_complete_title) + "🎉",
                description = stringResource(R.string.setup_complete_desc),
                contentTypography = contentTypography
            )
        }
    }

    @Composable
    private fun SetupScreenLayout(
        modifier: Modifier = Modifier,
        onClickPrev: () -> Unit = {},
        onClickContinue: () -> Unit = {},
        backLabel: String = stringResource(R.string.back),
        continueLabel: String = stringResource(R.string.scontinue),
        content: @Composable ColumnScope.() -> Unit
    ) {
        ConstraintLayout(
            modifier = modifier
        ) {
            val (contentRef, bottomButtonRef) = createRefs()

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .constrainAs(contentRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(bottomButtonRef.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                content()
            }

            BottomNavigateButtons(
                onClickContinue = onClickContinue,
                onClickPrev = onClickPrev,
                backLabel = backLabel,
                continueLabel = continueLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .constrainAs(bottomButtonRef) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
        }
    }

    @Composable
    private fun TitleWithDescription(
        title: String,
        description: String,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(top = 40.dp),
            textAlign = TextAlign.Center,
            style = contentTypography.title,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = description,
            modifier = Modifier.padding(top = 20.dp),
            textAlign = TextAlign.Start,
            style = contentTypography.body,
            color = MaterialTheme.colorScheme.secondary
        )
    }

    @Composable
    private fun PermissionItem(
        title: String,
        description: String,
        modifier: Modifier = Modifier,
        allowed: Boolean = false,
        contentTypography: ContentTypography = PlayerTheme.typography.contentNormal,
        onClick: () -> Unit = {}
    ) {
        Row(
            modifier = modifier
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    textAlign = TextAlign.Start,
                    style = contentTypography.title,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = description,
                    modifier = Modifier.padding(top = 10.dp),
                    textAlign = TextAlign.Start,
                    style = contentTypography.body,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(60.dp)
                    .padding(horizontal = 10.dp)
            )

            AnimatedContent(
                targetState = allowed,
                // TODO: keep the width same as the button
                modifier = Modifier
                    .defaultMinSize(minWidth = 72.dp)
                    .padding(start = 10.dp)
            ) {
                when (it) {
                    true -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Allowed",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    false -> {
                        FilledTonalButton(
                            onClick = onClick
                        ) {
                            Text(
                                text = stringResource(R.string.allow)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomNavigateButtons(
        onClickContinue: () -> Unit,
        onClickPrev: () -> Unit,
        modifier: Modifier = Modifier,
        backLabel: String = stringResource(R.string.back),
        continueLabel: String = stringResource(R.string.scontinue)
    ) {
        Row(
            modifier = modifier.height(IntrinsicSize.Min)
        ) {
            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.Start),
                onClick = onClickPrev
            ) {
                Text(
                    text = backLabel,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            FilledTonalButton(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.End),
                onClick = onClickContinue
            ) {
                Text(
                    text = continueLabel
                )
            }
        }
    }
}