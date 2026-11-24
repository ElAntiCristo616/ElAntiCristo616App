package com.junkfood.seal.ui.page.downloadv2.configure

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.HapticFeedback.longPressHapticFeedback
import com.junkfood.seal.ui.common.motion.materialSharedAxisX
import com.junkfood.seal.ui.component.DrawerSheetSubtitle
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.SealModalBottomSheet
import com.junkfood.seal.ui.component.SealModalBottomSheetM2Variant
import com.junkfood.seal.ui.component.SingleChoiceChip
import com.junkfood.seal.ui.component.SingleChoiceSegmentedButton
import com.junkfood.seal.ui.component.VideoFilterChip
import com.junkfood.seal.ui.page.downloadv2.configure.ActionButton.Download
import com.junkfood.seal.ui.page.downloadv2.configure.ActionButton.FetchInfo
import com.junkfood.seal.ui.page.downloadv2.configure.ActionButton.StartTask
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SelectionState
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState.Configure
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState.Error
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState.InputUrl
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState.Loading
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DownloadType
import com.junkfood.seal.util.DownloadType.Audio
import com.junkfood.seal.util.DownloadType.Command
import com.junkfood.seal.util.DownloadType.Playlist
import com.junkfood.seal.util.DownloadType.Video
import com.junkfood.seal.util.DownloadType.entries
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FORMAT_COMPATIBILITY
import com.junkfood.seal.util.FORMAT_SELECTION
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.RES_1080P
import com.junkfood.seal.util.RES_720P
import com.junkfood.seal.util.RES_480P
import com.junkfood.seal.util.RES_360P
import com.junkfood.seal.util.ToastUtil
import kotlinx.coroutines.launch

@Composable
private fun DownloadType.label(): String =
    stringResource(
        when (this) {
            Audio -> R.string.audio
            Video -> R.string.video
            Command -> R.string.commands
            Playlist -> R.string.playlist
        }
    )

val PreferencesMock = DownloadUtil.DownloadPreferences.EMPTY

data class Config(
    val downloadType: DownloadType? = PreferenceUtil.getDownloadType(),
    val typeEntries: List<DownloadType> =
        when (CUSTOM_COMMAND.getBoolean()) {
            true -> DownloadType.entries
            false -> DownloadType.entries - Command
        },
    val useFormatSelection: Boolean = FORMAT_SELECTION.getBoolean(),
    val savedLinks: Set<String> = PreferenceUtil.getSavedLinks(),
) {
    companion object {
        fun updatePreferences(newValue: Config, oldValue: Config) {
            with(newValue) {
                if (downloadType != oldValue.downloadType) {
                    downloadType?.let { PreferenceUtil.updateDownloadType(it) }
                }
                if (useFormatSelection != oldValue.useFormatSelection) {
                    FORMAT_SELECTION.updateBoolean(useFormatSelection)
                }
                if (savedLinks != oldValue.savedLinks) {
                    PreferenceUtil.updateSavedLinks(savedLinks)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadDialog(
    modifier: Modifier = Modifier,
    config: Config,
    sheetState: SheetState,
    preferences: DownloadUtil.DownloadPreferences,
    onPreferencesUpdate: (DownloadUtil.DownloadPreferences) -> Unit,
    state: DownloadDialogViewModel.SheetState = InputUrl,
    onActionPost: (Action) -> Unit = {},
) {
    // ElAntiCristo616 App (principiantes): sin diálogos de ajuste de formato.
    // El formato es fijo (MP4 compatible / MP3) y no se puede cambiar.
    SealModalBottomSheet(
        sheetState = sheetState,
        contentPadding = PaddingValues(),
        onDismissRequest = { onActionPost(Action.HideSheet) },
    ) {
        DownloadDialogContent(
            modifier = modifier,
            state = state,
            config = config,
            preferences = preferences,
            onPreferencesUpdate = onPreferencesUpdate,
            onActionPost = onActionPost,
        )
    }
}

@Composable
private fun ErrorPage(modifier: Modifier = Modifier, state: Error, onActionPost: (Action) -> Unit) {
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val url =
        state.action.run {
            when (this) {
                is Action.FetchFormats -> url
                is Action.FetchPlaylist -> url
                else -> {
                    throw IllegalArgumentException()
                }
            }
        }
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = stringResource(R.string.fetch_info_error_msg),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = state.throwable.message.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            maxLines = 20,
            overflow = TextOverflow.Clip,
        )

        Row(modifier = Modifier) {
            FilledTonalButton(onClick = { onActionPost(state.action) }) { Text("Retry") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    view.longPressHapticFeedback()
                    clipboardManager.setText(
                        AnnotatedString(
                            App.getVersionReport() + "\nURL: ${url}\n${state.throwable.message}"
                        )
                    )
                    ToastUtil.makeToast(R.string.error_copied)
                }
            ) {
                Text(stringResource(R.string.copy_error_report))
            }
        }
    }
}

@Composable
private fun DownloadDialogContent(
    modifier: Modifier = Modifier,
    state: DownloadDialogViewModel.SheetState,
    config: Config,
    preferences: DownloadUtil.DownloadPreferences,
    onPreferencesUpdate: (DownloadUtil.DownloadPreferences) -> Unit,
    onActionPost: (Action) -> Unit,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = state,
        label = "",
        transitionSpec = {
            materialSharedAxisX(initialOffsetX = { it / 4 }, targetOffsetX = { -it / 4 })
        },
    ) { state ->
        when (state) {
            is Configure -> {
                check(state.urlList.isNotEmpty())
                if (state.urlList.size == 1) {
                    ConfigurePage(
                        url = state.urlList.first(),
                        config = config,
                        preferences = preferences,
                        onConfigSave = {
                            Config.updatePreferences(newValue = it, oldValue = config)
                        },
                        onActionPost = { onActionPost(it) },
                    )
                } else {
                    ConfigurePagePlaylistVariant(
                        initialDownloadType = config.downloadType ?: Video,
                        preferences = preferences,
                        onPreferencesUpdate = onPreferencesUpdate,
                        onDismissRequest = { onActionPost(Action.HideSheet) },
                    ) { type, prefs ->
                        onActionPost(
                            Action.DownloadWithPreset(
                                urlList = state.urlList,
                                preferences = prefs.copy(extractAudio = type == Audio),
                            )
                        )
                    }
                }
            }

            is Error -> {
                ErrorPage(state = state, onActionPost = onActionPost)
            }

            is Loading -> {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 120.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            InputUrl -> {
                InputUrlPage(
                    config = config,
                    onConfigUpdate = { Config.updatePreferences(newValue = it, oldValue = config) },
                    onActionPost = onActionPost,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ErrorPreview() {
    SealModalBottomSheet(
        onDismissRequest = {},
        sheetState =
            with(LocalDensity.current) {
                SheetState(
                    initialValue = SheetValue.Expanded,
                    skipPartiallyExpanded = true,
                    velocityThreshold = { 56.dp.toPx() },
                    positionalThreshold = { 125.dp.toPx() },
                )
            },
    ) {
        ErrorPage(
            state =
                Error(
                    action =
                        Action.FetchFormats(
                            url = "",
                            audioOnly = true,
                            preferences = PreferencesMock,
                        ),
                    throwable = Exception("Not good"),
                ),
            onActionPost = {},
        )
    }
}

@Composable
fun FormatPage(
    modifier: Modifier = Modifier,
    state: SelectionState.FormatSelection,
    onDismissRequest: () -> Unit,
) {
    val sheetState =
        androidx.compose.material.rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true,
        )

    LaunchedEffect(state) { sheetState.show() }
    val scope = rememberCoroutineScope()
    BackHandler { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() } }

    SealModalBottomSheetM2Variant(sheetState = sheetState, sheetGesturesEnabled = false) {
        FormatPage(
            modifier = modifier,
            videoInfo = state.info,
            onNavigateBack = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/*@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)*/
@Composable
private fun ConfigurePagePreview() {
    SealTheme() {
        SealModalBottomSheet(
            sheetState =
                with(LocalDensity.current) {
                    SheetState(
                        initialValue = SheetValue.Expanded,
                        skipPartiallyExpanded = true,
                        velocityThreshold = { 56.dp.toPx() },
                        positionalThreshold = { 125.dp.toPx() },
                    )
                },
            onDismissRequest = {},
            contentPadding = PaddingValues(),
        ) {
            ConfigurePage(
                config =
                    Config(
                        downloadType = Audio,
                        useFormatSelection = true,
                        typeEntries = entries - Command,
                    ),
                preferences = PreferencesMock,
                onConfigSave = {},
            ) {}
        }
    }
}

@Composable
private fun ConfigurePage(
    modifier: Modifier = Modifier,
    url: String = "",
    config: Config,
    preferences: DownloadUtil.DownloadPreferences,
    onConfigSave: (Config) -> Unit,
    onActionPost: (Action) -> Unit,
) {
    // ElAntiCristo616 App (versión para principiantes):
    // solo dos elecciones — tipo (video o solo audio) y calidad del video.
    // El formato es FIJO: video siempre MP4 H.264 + AAC (el más compatible,
    // el de WhatsApp) y audio siempre MP3. Sin marca de agua, siempre:
    // yt-dlp baja el video en crudo y no existe ninguna opción que lo cambie.
    var selectedType by remember(config) {
        mutableStateOf(config.downloadType?.takeIf { it == Video || it == Audio } ?: Video)
    }
    var quickRes by remember(preferences) { mutableIntStateOf(preferences.videoResolution) }
    val effectivePrefs =
        preferences.copy(
            videoResolution = quickRes,
            videoFormat = FORMAT_COMPATIBILITY,
            extractAudio = selectedType == Audio,
        )

    // Toda la columna con desplazamiento vertical: en pantallas chicas nada
    // queda fuera de la pantalla (antes el botón DESCARGAR se cortaba abajo).
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Header(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                title = stringResource(R.string.settings_before_download),
                icon = Icons.Outlined.DoneAll,
            )
            // Garantía fija, no es un ajuste: siempre sin marca de agua.
            Text(
                text = stringResource(R.string.no_watermark),
                color = Color(0xFFCD2D32),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 12.dp),
            )
            DrawerSheetSubtitle(text = stringResource(id = R.string.download_type))
            DownloadTypeSelectionGroup(
                typeEntries = listOf(Video, Audio),
                selectedType = selectedType,
                onSelect = { selectedType = it },
            )
            if (selectedType == Video) {
                DrawerSheetSubtitle(text = stringResource(id = R.string.quality))
                Row(
                    modifier =
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp)
                ) {
                    VideoFilterChip(
                        selected = quickRes == RES_720P,
                        onClick = { quickRes = RES_720P },
                        label = "MP4 · 720p · WhatsApp",
                    )
                    VideoFilterChip(
                        selected = quickRes == RES_1080P,
                        onClick = { quickRes = RES_1080P },
                        label = "1080p",
                    )
                    VideoFilterChip(
                        selected = quickRes == RES_480P,
                        onClick = { quickRes = RES_480P },
                        label = "480p",
                    )
                    VideoFilterChip(
                        selected = quickRes == RES_360P,
                        onClick = { quickRes = RES_360P },
                        label = "360p",
                    )
                }
            } else {
                Row(
                    modifier =
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp)
                ) {
                    VideoFilterChip(
                        selected = true,
                        onClick = {},
                        label = "MP3 · Solo audio",
                    )
                }
            }
        }

        ActionButtons(
            modifier = Modifier.padding(horizontal = 20.dp),
            canProceed = true,
            selectedType = selectedType,
            useFormatSelection = false,
            onCancel = { onActionPost(Action.HideSheet) },
            onDownload = {
                onConfigSave(
                    config.copy(
                        useFormatSelection = false,
                        downloadType = selectedType,
                    )
                )
                onActionPost(
                    Action.DownloadWithPreset(
                        urlList = listOf(url),
                        preferences = effectivePrefs,
                    )
                )
            },
            onFetchInfo = {},
            onTaskStart = {},
        )
    }
}

@Composable
fun ConfigurePagePlaylistVariant(
    modifier: Modifier = Modifier,
    initialDownloadType: DownloadType,
    preferences: DownloadUtil.DownloadPreferences,
    onPreferencesUpdate: (DownloadUtil.DownloadPreferences) -> Unit,
    onDismissRequest: () -> Unit,
    onDownload: (DownloadType, DownloadUtil.DownloadPreferences) -> Unit,
) {
    // ElAntiCristo616 App (versión para principiantes): igual que ConfigurePage —
    // solo tipo (video/audio) y calidad. Formato fijo, sin marca de agua, siempre.
    var selectedType by remember(initialDownloadType) {
        mutableStateOf(initialDownloadType.takeIf { it == Video || it == Audio } ?: Video)
    }
    var quickRes by remember(preferences) { mutableIntStateOf(preferences.videoResolution) }
    val effectivePrefs =
        preferences.copy(
            videoResolution = quickRes,
            videoFormat = FORMAT_COMPATIBILITY,
            extractAudio = selectedType == Audio,
        )

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Header(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                title = stringResource(R.string.settings_before_download),
                icon = Icons.Outlined.DoneAll,
            )
            // Garantía fija, no es un ajuste: siempre sin marca de agua.
            Text(
                text = stringResource(R.string.no_watermark),
                color = Color(0xFFCD2D32),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 12.dp),
            )
            DrawerSheetSubtitle(text = stringResource(id = R.string.download_type))
            DownloadTypeSelectionGroup(
                typeEntries = listOf(Video, Audio),
                selectedType = selectedType,
                onSelect = { selectedType = it },
            )
            if (selectedType == Video) {
                DrawerSheetSubtitle(text = stringResource(id = R.string.quality))
                Row(
                    modifier =
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp)
                ) {
                    VideoFilterChip(
                        selected = quickRes == RES_720P,
                        onClick = { quickRes = RES_720P },
                        label = "MP4 · 720p · WhatsApp",
                    )
                    VideoFilterChip(
                        selected = quickRes == RES_1080P,
                        onClick = { quickRes = RES_1080P },
                        label = "1080p",
                    )
                    VideoFilterChip(
                        selected = quickRes == RES_480P,
                        onClick = { quickRes = RES_480P },
                        label = "480p",
                    )
                    VideoFilterChip(
                        selected = quickRes == RES_360P,
                        onClick = { quickRes = RES_360P },
                        label = "360p",
                    )
                }
            } else {
                Row(
                    modifier =
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp)
                ) {
                    VideoFilterChip(
                        selected = true,
                        onClick = {},
                        label = "MP3 · Solo audio",
                    )
                }
            }
        }

        ActionButtons(
            modifier = Modifier.padding(horizontal = 20.dp),
            canProceed = true,
            selectedType = selectedType,
            useFormatSelection = false,
            onCancel = onDismissRequest,
            onDownload = {
                onDownload(selectedType, effectivePrefs)
                onDismissRequest()
            },
            onFetchInfo = {},
            onTaskStart = {},
        )
    }
}

@Composable
internal fun Header(modifier: Modifier = Modifier, icon: ImageVector, title: String) {
    Column(modifier = modifier) {
        Icon(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            imageVector = icon,
            contentDescription = null,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier =
                Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp, bottom = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DownloadTypeSelectionGroup(
    modifier: Modifier = Modifier,
    typeEntries: List<DownloadType>,
    selectedType: DownloadType?,
    onSelect: (DownloadType) -> Unit,
) {
    val typeCount = typeEntries.size
    if (typeCount == DownloadType.entries.size) {
        LazyRow(modifier = modifier) {
            items(typeEntries) { type ->
                SingleChoiceChip(
                    selected = selectedType == type,
                    label = type.label(),
                    onClick = { onSelect(type) },
                )
            }
        }
    } else {
        SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
            typeEntries.forEachIndexed { index, type ->
                SingleChoiceSegmentedButton(
                    selected = selectedType == type,
                    onClick = { onSelect(type) },
                    shape = SegmentedButtonDefaults.itemShape(index, typeCount),
                ) {
                    Text(text = type.label())
                }
            }
        }
    }
}

private enum class ActionButton {
    FetchInfo,
    Download,
    StartTask,
}

@Composable
private fun ActionButton.Icon() {
    Icon(
        imageVector =
            when (this) {
                FetchInfo -> Icons.AutoMirrored.Filled.ArrowForward
                Download -> Icons.Outlined.FileDownload
                StartTask -> Icons.Filled.DownloadDone
            },
        contentDescription = null,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
private fun ActionButton.Label() {
    Text(
        stringResource(
            when (this) {
                FetchInfo -> R.string.proceed
                Download -> R.string.download
                StartTask -> R.string.start
            }
        ),
        modifier = Modifier.padding(start = 8.dp),
    )
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    canProceed: Boolean,
    selectedType: DownloadType?,
    useFormatSelection: Boolean,
    onCancel: () -> Unit,
    onFetchInfo: () -> Unit,
    onDownload: () -> Unit,
    onTaskStart: () -> Unit,
) {
    val action =
        if (selectedType == Command) {
            StartTask
        } else if (selectedType == Playlist || useFormatSelection) {
            FetchInfo
        } else {
            Download
        }

    // ElAntiCristo616 App: botón DESCARGAR grande rojo + cancelar como texto
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            onClick = {
                when (action) {
                    FetchInfo -> onFetchInfo()
                    Download -> onDownload()
                    StartTask -> onTaskStart()
                }
            },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCD2D32),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFCD2D32).copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f),
                ),
            shape = RoundedCornerShape(16.dp),
            enabled = canProceed,
        ) {
            AnimatedContent(
                targetState = action,
                label = "",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90))).togetherWith(
                        fadeOut(animationSpec = tween(90))
                    )
                },
            ) { action ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    action.Icon()
                    if (action == Download) {
                        Text(
                            "DESCARGAR",
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        action.Label()
                    }
                }
            }
        }
        TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
    }
}
