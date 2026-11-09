package ru.mlbb.blocker.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mlbb.blocker.BuildConfig
import ru.mlbb.blocker.R
import ru.mlbb.blocker.UiState
import ru.mlbb.blocker.data.BlockPreset
import ru.mlbb.blocker.data.BlockerStatus
import ru.mlbb.blocker.data.ConnectionStat
import ru.mlbb.blocker.data.HealthIssue
import ru.mlbb.blocker.data.MatchPhase
import ru.mlbb.blocker.data.RegionProfile
import ru.mlbb.blocker.data.RegionProfiles
import ru.mlbb.blocker.data.UpdateDownloadState
import ru.mlbb.blocker.data.UpdateInfo
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockerApp(
    ui: UiState,
    onToggle: () -> Unit,
    onDiscoveryChange: (Boolean) -> Unit,
    onSelectProfile: (String) -> Unit,
    onAddRule: (String, String) -> Boolean,
    onAddRules: (String) -> Int,
    onAddPreset: (BlockPreset) -> Unit,
    onRemoveRule: (String) -> Unit,
    onSetRuleEnabled: (String, Boolean) -> Unit,
    onClearRules: () -> Unit,
    onAddConnection: (ConnectionStat, Boolean) -> Unit,
    onPing: (ConnectionStat) -> Unit,
    onPingAll: () -> Unit,
    onClearConnections: () -> Unit,
    onClearLogs: () -> Unit,
    onFastRejectChange: (Boolean) -> Unit,
    onMatchAlertsChange: (Boolean) -> Unit,
    onHideStartupAlertsChange: (Boolean) -> Unit,
    onAutoStartChange: (Boolean) -> Unit,
    onAllowStartupServerChange: (Boolean) -> Unit,
    onFinishOnboarding: () -> Unit,
    onShowMatchDemo: () -> Unit,
    onRefreshDiagnostics: () -> Unit,
    onDownloadGeoIp: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onApplyImport: (Boolean) -> Unit,
    onCancelImport: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
    onStartGame: (String) -> Unit,
    onMatchResult: (String) -> Unit,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onAllow: (String, Int) -> Unit,
    onRemoveAllow: (String) -> Unit,
    onMessageShown: () -> Unit,
    onShowMessage: (String) -> Unit,
    onHealthAction: () -> Unit,
    onRefreshRules: () -> Unit,
    onUnknownPortAlertsChange: (Boolean) -> Unit,
    onStableOnlyAlertsChange: (Boolean) -> Unit,
    onAnalyticsEnabledChange: (Boolean) -> Unit,
    onRunPingDiagnostics: () -> Unit,
    onRequestBatteryExemption: () -> Unit,
    onFilterModeChange: (String) -> Unit,
    onDelayMsChange: (Int) -> Unit,
    onDebugTraceChange: (Boolean) -> Unit,
    onExportTrace: () -> Unit,
    onRunHypothesisCheck: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var updateDismissed by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val tabs = remember(ui.featureMetaEnabled) {
        buildList {
            add(AppTab.Home)
            add(AppTab.Match)
            add(AppTab.Monitor)
            add(AppTab.Blocklist)
            if (ui.featureMetaEnabled) add(AppTab.Meta)
            add(AppTab.Settings)
        }
    }
    val currentTab = tabs.getOrElse(tab) { AppTab.Home }

    LaunchedEffect(ui.message) {
        val message = ui.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessageShown()
    }

    if (ui.maintenance) {
        MaintenanceScreen(notice = ui.remoteNotice)
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = AppColors.SurfaceHigh,
                    contentColor = AppColors.Text,
                    actionColor = AppColors.CyanLight,
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (showAbout) "О приложении" else stringResource(R.string.main_title),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    if (showAbout) {
                        IconButton(onClick = { showAbout = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                },
                actions = {
                    if (!showAbout) {
                        IconButton(onClick = { showAbout = true }) {
                            Icon(Icons.Default.Info, contentDescription = "О приложении")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            if (!showAbout) {
                NavigationBar(
                    containerColor = AppColors.Surface,
                    tonalElevation = 0.dp,
                ) {
                    tabs.forEachIndexed { index, item ->
                        NavigationItem(index, tab, item.label, item.icon) {
                            showAbout = false
                            tab = it
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (showAbout) {
                AboutScreen()
            } else {
                when (currentTab) {
                    AppTab.Home -> HomeScreen(
                        ui = ui,
                        onToggle = onToggle,
                        onDiscoveryChange = onDiscoveryChange,
                        onSelectProfile = onSelectProfile,
                        onFinishOnboarding = onFinishOnboarding,
                        onDownloadUpdate = onDownloadUpdate,
                        onInstallUpdate = onInstallUpdate,
                        onOpenAbout = { showAbout = true },
                        onHealthAction = onHealthAction,
                        onFilterModeChange = onFilterModeChange,
                        onDelayMsChange = onDelayMsChange,
                    )
                    AppTab.Match -> MatchScreen(
                        ui = ui,
                        onStartGame = onStartGame,
                        onStartDiscovery = onStartDiscovery,
                        onStopDiscovery = onStopDiscovery,
                        onMatchResult = onMatchResult,
                        onAllow = onAllow,
                        onRemoveAllow = onRemoveAllow,
                    )
                    AppTab.Monitor -> ConnectionsScreen(
                        connections = ui.connections,
                        selectedProfile = ui.selectedProfile,
                        pingingKeys = ui.pingingKeys,
                        pingReport = ui.pingReport,
                        pingRunning = ui.pingRunning,
                        onRunPingDiagnostics = onRunPingDiagnostics,
                        onPing = onPing,
                        onPingAll = onPingAll,
                        onBlock = onAddConnection,
                        onClear = onClearConnections,
                        onShowMessage = onShowMessage,
                    )
                    AppTab.Blocklist -> BlocklistScreen(
                        rules = ui.rules,
                        onAddRule = onAddRule,
                        onAddRules = onAddRules,
                        onAddPreset = onAddPreset,
                        onRemoveRule = onRemoveRule,
                        onSetRuleEnabled = onSetRuleEnabled,
                        onClearRules = onClearRules,
                        onShowMessage = onShowMessage,
                    )
                    AppTab.Meta -> MetaScreen()
                    AppTab.Settings -> SettingsScreen(
                        ui = ui,
                        onDiscoveryChange = onDiscoveryChange,
                        onFastRejectChange = onFastRejectChange,
                        onMatchAlertsChange = onMatchAlertsChange,
                        onHideStartupAlertsChange = onHideStartupAlertsChange,
                        onAutoStartChange = onAutoStartChange,
                        onAllowStartupServerChange = onAllowStartupServerChange,
                        onShowMatchDemo = onShowMatchDemo,
                        onRefreshDiagnostics = onRefreshDiagnostics,
                        onDownloadGeoIp = onDownloadGeoIp,
                        onExportBackup = onExportBackup,
                        onImportBackup = onImportBackup,
                        onApplyImport = onApplyImport,
                        onCancelImport = onCancelImport,
                        onClearLogs = onClearLogs,
                        onRefreshRules = onRefreshRules,
                        onUnknownPortAlertsChange = onUnknownPortAlertsChange,
                        onStableOnlyAlertsChange = onStableOnlyAlertsChange,
                        onAnalyticsEnabledChange = onAnalyticsEnabledChange,
                        onRequestBatteryExemption = onRequestBatteryExemption,
                        onDebugTraceChange = onDebugTraceChange,
                        onExportTrace = onExportTrace,
                        onRunHypothesisCheck = onRunHypothesisCheck,
                    )
                }
            }
        }
    }

    if (ui.updateRequired && !updateDismissed) {
        UpdateRequiredDialog(
            currentVersion = BuildConfig.VERSION_NAME,
            minVersion = ui.minAppVersion,
            onDismiss = { updateDismissed = true },
        )
    }
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    Home("Главная", Icons.Default.Home),
    Match("Матч", Icons.Default.PlayArrow),
    Monitor("Монитор", Icons.Default.Search),
    Blocklist("Блок", Icons.Default.Lock),
    Meta("Мета", Icons.Default.Star),
    Settings("Настройки", Icons.Default.Settings),
}

@Composable
private fun MaintenanceScreen(notice: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        EsportsCard {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AppColors.Warning.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = AppColors.Warning,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    "Идут технические работы",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    notice.ifBlank { "Скоро вернёмся. Приложение обновится автоматически." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun UpdateRequiredDialog(
    currentVersion: String,
    minVersion: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Пожалуйста, обновитесь") },
        text = {
            Text(
                "Установлена версия $currentVersion, требуется $minVersion или новее. " +
                    "Новая версия уже доступна на GitHub.",
                style = MaterialTheme.typography.bodySmall,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    openUrl(context, "https://github.com/mrkoronalol/mlbb-network-doctor/releases/latest")
                },
            ) {
                Text("Скачать обновление")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Позже") }
        },
    )
}

@Composable
private fun RowScope.NavigationItem(
    index: Int,
    selected: Int,
    label: String,
    icon: ImageVector,
    onClick: (Int) -> Unit,
) {
    NavigationBarItem(
        selected = selected == index,
        onClick = { onClick(index) },
        icon = { Icon(icon, contentDescription = label) },
        label = {
            Text(
                label,
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = AppColors.BlueLight,
            selectedTextColor = AppColors.BlueLight,
            indicatorColor = AppColors.Blue.copy(alpha = 0.18f),
            unselectedIconColor = AppColors.TextMuted,
            unselectedTextColor = AppColors.TextMuted,
        ),
    )
}

@Composable
private fun HomeScreen(
    ui: UiState,
    onToggle: () -> Unit,
    onDiscoveryChange: (Boolean) -> Unit,
    onSelectProfile: (String) -> Unit,
    onFinishOnboarding: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
    onOpenAbout: () -> Unit,
    onHealthAction: () -> Unit,
    onFilterModeChange: (String) -> Unit,
    onDelayMsChange: (Int) -> Unit,
) {
    var infoProfile by remember { mutableStateOf<RegionProfile?>(null) }
    var noticeDismissed by rememberSaveable { mutableStateOf(false) }
    var profileMenu by remember { mutableStateOf(false) }
    val error = ui.status as? BlockerStatus.Error
    val selectedProfile = ui.selectedProfile?.let(RegionProfiles::byId)
    val regionLabel = when {
        selectedProfile == null -> stringResource(R.string.region_auto)
        selectedProfile.id == RegionProfiles.ID_CUSTOM ->
            stringResource(R.string.region_custom)
        else -> selectedProfile.title
    }
    val currentPing = ui.currentMatch?.pingMs?.let { "$it мс" } ?: "—"
    val regionShort = if (selectedProfile?.id == RegionProfiles.ID_CUSTOM) {
        stringResource(R.string.region_custom_short)
    } else {
        regionLabel
    }
    val routeStatus = when {
        !ui.isRunning -> stringResource(R.string.route_off)
        ui.discovery -> stringResource(R.string.route_direct)
        else -> stringResource(R.string.route_filtered)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.profile_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(8.dp))
            Box {
                OutlinedButton(onClick = { profileMenu = true }) {
                    Text(regionLabel, style = MaterialTheme.typography.labelLarge)
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = profileMenu,
                    onDismissRequest = { profileMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.region_auto)) },
                        onClick = {
                            profileMenu = false
                            onSelectProfile("auto")
                        },
                    )
                    RegionProfiles.selectable.forEach { profile ->
                        DropdownMenuItem(
                            text = { Text(profile.title) },
                            onClick = {
                                profileMenu = false
                                onSelectProfile(profile.id)
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = {
                    infoProfile = selectedProfile ?: RegionProfiles.all.firstOrNull()
                },
                modifier = Modifier.size(30.dp),
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Описание профилей",
                    tint = AppColors.CyanLight,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        if (ui.remoteNotice.isNotBlank() && !noticeDismissed) {
            EsportsCard(
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.Cyan.copy(alpha = 0.10f),
                ),
                border = BorderStroke(1.dp, AppColors.Cyan.copy(alpha = 0.35f)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = AppColors.CyanLight,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        ui.remoteNotice,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextButton(onClick = { noticeDismissed = true }) {
                        Text("Скрыть", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (error != null) {
            NoticeCard(
                icon = Icons.Default.Warning,
                color = MaterialTheme.colorScheme.error,
                title = "Ошибка",
                text = error.message,
            )
        } else if (ui.updateInfo != null) {
            UpdateBanner(
                update = ui.updateInfo,
                download = ui.updateDownload,
                installing = ui.updateInstalling,
                onDownload = onDownloadUpdate,
                onInstall = onInstallUpdate,
            )
        } else {
            ui.healthIssues.firstOrNull()?.let { issue ->
                HealthBanner(issue = issue, onFix = onHealthAction)
            }
        }

        if (ui.showOnboarding) {
            OnboardingCard(onDone = onFinishOnboarding)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                stringResource(R.string.dashboard_ping),
                currentPing,
                Modifier.weight(1f),
            )
            StatCard(
                stringResource(R.string.dashboard_route),
                routeStatus,
                Modifier.weight(1f),
            )
            StatCard(
                stringResource(R.string.dashboard_region),
                regionShort,
                Modifier.weight(1f),
            )
        }

        EsportsCard {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(R.string.filter_mode_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = ui.filterMode == "block",
                        onClick = { onFilterModeChange("block") },
                        label = {
                            Text(
                                stringResource(R.string.filter_mode_block),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                    FilterChip(
                        selected = ui.filterMode == "delay",
                        onClick = { onFilterModeChange("delay") },
                        label = {
                            Text(
                                stringResource(R.string.filter_mode_delay),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
                if (ui.filterMode == "delay") {
                    Text(
                        stringResource(R.string.delay_label, ui.delayMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Slider(
                        value = ui.delayMs.toFloat(),
                        onValueChange = { value ->
                            onDelayMsChange((value / 50f).roundToInt() * 50)
                        },
                        valueRange = 100f..500f,
                        steps = 7,
                    )
                }
                Text(
                    stringResource(R.string.dodge_warning),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Warning,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.phase_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.size(8.dp))
                    listOf(
                        MatchPhase.IDLE to R.string.phase_idle,
                        MatchPhase.QUEUE to R.string.phase_queue,
                        MatchPhase.MATCH_FOUND to R.string.phase_match_found,
                        MatchPhase.IN_GAME to R.string.phase_in_game,
                    ).forEach { (phase, labelRes) ->
                        Text(
                            stringResource(labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (ui.phase == phase) FontWeight.Bold else null,
                            color = if (ui.phase == phase) {
                                AppColors.CyanLight
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }
                Text(
                    ui.shaperStats.summary,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        EsportsCard {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(statusColor(ui.status)),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        statusText(ui.status),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(R.string.rules_count, ui.effectiveRuleCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CompactSwitchRow(
                    title = stringResource(R.string.discovery_mode),
                    checked = ui.discovery,
                    onCheckedChange = onDiscoveryChange,
                )
                if (ui.discovery) {
                    Text(
                        "Правила не применяются — только сбор IP",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Warning,
                    )
                }
            }
        }

        if (ui.isRunning) {
            OutlinedButton(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Выключить блокировку", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Button(
                onClick = onToggle,
                enabled = !ui.isBusy && ui.mlbbInstalled,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                if (ui.isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.size(10.dp))
                    Text("Запуск…")
                } else {
                    Text("Включить блокировку", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(stringResource(R.string.rules_stat), ui.effectiveRuleCount.toString(), Modifier.weight(1f))
            StatCard("Собрано IP", ui.connections.size.toString(), Modifier.weight(1f))
        }

        OutlinedButton(
            onClick = onOpenAbout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("О приложении")
        }
    }

    infoProfile?.let { profile ->
        ProfileInfoSheet(profile = profile, onDismiss = { infoProfile = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileInfoSheet(profile: RegionProfile, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.SurfaceHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                profile.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                profile.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("разрешено: ${profile.allowedCidrs.size}", AppColors.Success)
                Pill("блокируется: ${profile.rules.size}", AppColors.Danger)
            }
            if (profile.blockOtherMatchServers) {
                Text(
                    "Остальные матч-серверы блокируются автоматически.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Понятно")
            }
        }
    }
}

@Composable
private fun OnboardingCard(onDone: () -> Unit) {
    EsportsCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Разрешите VPN и включите блокировку.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(onClick = onDone) { Text("Ок") }
        }
    }
}

@Composable
private fun HealthBanner(issue: HealthIssue, onFix: () -> Unit) {
    val title = when (issue) {
        HealthIssue.SERVICE_STOPPED -> stringResource(R.string.health_service_stopped_title)
        HealthIssue.GEOIP_FAILED -> stringResource(R.string.health_geoip_failed_title)
        HealthIssue.RULES_FALLBACK -> stringResource(R.string.health_rules_failed_title)
    }
    val text = when (issue) {
        HealthIssue.SERVICE_STOPPED -> stringResource(R.string.health_service_stopped_text)
        HealthIssue.GEOIP_FAILED -> stringResource(R.string.health_geoip_failed_text)
        HealthIssue.RULES_FALLBACK -> stringResource(R.string.health_rules_failed_text)
    }
    EsportsCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                )
                Text(
                    text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onFix) {
                Text(
                    stringResource(R.string.health_fix),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun UpdateBanner(
    update: UpdateInfo,
    download: UpdateDownloadState,
    installing: Boolean,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
) {
    val context = LocalContext.current
    EsportsCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        stringResource(R.string.update_available_title, update.version),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.update_available_text, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    update.size?.let { bytes ->
                        Text(
                            "Размер APK: ${bytes / 1024 / 1024} МБ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            when (download) {
                UpdateDownloadState.Idle -> {
                    Button(onClick = onDownload) {
                        Text("Скачать и установить")
                    }
                }

                is UpdateDownloadState.Downloading -> {
                    Text(
                        "Загрузка: ${download.progress}% · ${download.received / 1024 / 1024} МБ",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                is UpdateDownloadState.Ready -> {
                    Button(onClick = onInstall, enabled = !installing) {
                        Text(if (installing) "Открываем установщик…" else "Установить")
                    }
                    Text(
                        "APK проверен по SHA256 и подписи",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                is UpdateDownloadState.Error -> {
                    Text(
                        "Не удалось скачать: ${download.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Button(onClick = onDownload) {
                        Text("Повторить")
                    }
                }
            }

            TextButton(onClick = { openUrl(context, update.releaseUrl) }) {
                Text(stringResource(R.string.update_release_page))
            }
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

private fun profileAccent(profile: RegionProfile): Color = when (profile.id) {
    RegionProfiles.ID_MOSCOW -> Color(0xFF4C7DFF)
    RegionProfiles.ID_FAR_EAST -> Color(0xFF2FA36B)
    else -> Color(0xFFE08A2E)
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    EsportsCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun NoticeCard(
    icon: ImageVector,
    color: Color,
    title: String,
    text: String,
) {
    EsportsCard(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                )
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
