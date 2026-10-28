package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.PredictionDatabase
import com.example.data.repository.WinGoRepository
import com.example.ui.EngineTab
import com.example.ui.WinGoViewModel
import com.example.ui.components.ChromiumWebApiBridge
import com.example.ui.components.DeepAnalyticsTabContent
import com.example.ui.components.FundPlannerTabContent
import com.example.ui.components.InteractiveWebEngineTab
import com.example.ui.components.LiveSignalTabContent
import com.example.ui.components.MatrixRainBackground
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MyApplicationTheme
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import com.example.service.WinGoNotificationHelper
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OrbitronFamily

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WinGoPatternEngineApp()
            }
        }
    }
}

@Composable
fun WinGoPatternEngineApp() {
    val context = LocalContext.current
    val repository = remember(context) {
        val db = PredictionDatabase.getInstance(context)
        WinGoRepository(db.predictionDao())
    }
    val viewModel: WinGoViewModel = viewModel(
        factory = WinGoViewModel.provideFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val records by viewModel.modeRecords.collectAsStateWithLifecycle()

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            WinGoNotificationHelper.setWinNotificationEnabled(context, true)
            viewModel.setWinNotificationEnabled(true)
            WinGoNotificationHelper.startBackgroundEngine(context)
        }
    }

    LaunchedEffect(Unit) {
        val initialPref = WinGoNotificationHelper.isWinNotificationEnabled(context)
        viewModel.setWinNotificationEnabled(initialPref)
        WinGoNotificationHelper.ensureChannels(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !WinGoNotificationHelper.hasNotificationPermission(context)
        ) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            WinGoNotificationHelper.startBackgroundEngine(context)
        }
    }

    // Trigger compact WIN notification whenever a new round wins!
    LaunchedEffect(uiState.latestWinRecord?.targetPeriod) {
        val winRec = uiState.latestWinRecord
        if (winRec != null && winRec.isWin == true && uiState.winNotificationEnabled) {
            val resolved = records.filter { it.isWin != null }
            val winCount = resolved.count { it.isWin == true }.coerceAtLeast(1)
            val lossCount = resolved.count { it.isWin == false }
            var streak = 0
            for (r in resolved) {
                if (r.isWin == true) streak++ else break
            }
            WinGoNotificationHelper.notifyWinIfNew(
                context = context,
                winRecord = winRec,
                winCount = winCount,
                lossCount = lossCount,
                streak = streak.coerceAtLeast(1)
            )
        }
    }

    if (uiState.selectedTab != EngineTab.LIVE_SIGNAL) {
        BackHandler {
            viewModel.selectTab(EngineTab.LIVE_SIGNAL)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberObsidian)
    ) {
        // Real Chromium WebView API Bridge so draw.ar-lottery01.com is fetched via browser TLS
        ChromiumWebApiBridge(
            selectedMode = uiState.selectedMode,
            syncTriggerCount = uiState.syncTriggerCount,
            onJsonReceived = viewModel::onWebViewJsonReceived
        )

        // Matrix Digital Rain + Scanline Canvas
        MatrixRainBackground(
            enabled = uiState.matrixAnimationEnabled
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                CyberBottomNavBar(
                    selectedTab = uiState.selectedTab,
                    onSelectTab = viewModel::selectTab
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 620.dp)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Ultra-Slim Header Bar
                    EngineHeader(
                        trainedCount = uiState.prediction.trainedDrawsCount.coerceAtLeast(uiState.draws.size),
                        matrixEnabled = uiState.matrixAnimationEnabled,
                        onToggleMatrix = viewModel::toggleMatrixAnimation
                    )

                    when (uiState.selectedTab) {
                        EngineTab.LIVE_SIGNAL -> {
                            LiveSignalTabContent(
                                uiState = uiState,
                                records = records,
                                onSelectMode = viewModel::selectMode,
                                onForceSync = viewModel::forceSync,
                                onResetStats = viewModel::resetCurrentModeStats,
                                onToggleWinNotif = {
                                    val nextState = !uiState.winNotificationEnabled
                                    if (nextState &&
                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !WinGoNotificationHelper.hasNotificationPermission(context)
                                    ) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        WinGoNotificationHelper.setWinNotificationEnabled(context, nextState)
                                        viewModel.setWinNotificationEnabled(nextState)
                                        if (nextState) {
                                            WinGoNotificationHelper.startBackgroundEngine(context)
                                        }
                                    }
                                }
                            )
                        }

                        EngineTab.ANALYTICS -> {
                            DeepAnalyticsTabContent(uiState = uiState)
                        }

                        EngineTab.FUND_PLANNER -> {
                            FundPlannerTabContent(
                                uiState = uiState,
                                onCapitalChange = viewModel::updateCapitalInput,
                                onStepCountChange = viewModel::updateStepCount,
                                onMultiplierChange = viewModel::updateMultiplierScale
                            )
                        }

                        EngineTab.WEB_ENGINE -> {
                            InteractiveWebEngineTab(
                                uiState = uiState,
                                onSelectMode = viewModel::selectMode,
                                onJsonReceived = viewModel::onWebViewJsonReceived
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun EngineHeader(
    trainedCount: Int,
    matrixEnabled: Boolean,
    onToggleMatrix: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "👑 DKWIN9 VIP 30S PRO",
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = com.example.ui.theme.VipGold,
                modifier = Modifier.testTag("app_header_title")
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonGreen.copy(alpha = 0.14f))
                    .border(1.dp, NeonGreen.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🧠 $trainedCount",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = NeonGreen
                )
            }
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0E172A))
                .border(1.dp, com.example.ui.theme.VipGold.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .clickable(onClick = onToggleMatrix)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("toggle_matrix_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = "Toggle Aura FX",
                tint = if (matrixEnabled) com.example.ui.theme.VipGold else CyberCyanDim,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (matrixEnabled) "✨ VIP FX" else "FX OFF",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = if (matrixEnabled) com.example.ui.theme.VipGold else CyberCyanDim
            )
        }
    }
}

@Composable
private fun CyberBottomNavBar(
    selectedTab: EngineTab,
    onSelectTab: (EngineTab) -> Unit
) {
    val items = listOf(
        Triple(EngineTab.LIVE_SIGNAL, "VIP 30S", Icons.Default.Bolt),
        Triple(EngineTab.ANALYTICS, "AI MATRIX", Icons.Default.Analytics),
        Triple(EngineTab.FUND_PLANNER, "VIP FUND", Icons.Default.Calculate),
        Triple(EngineTab.WEB_ENGINE, "LIVE GAME", Icons.Default.Public)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF060B16).copy(alpha = 0.97f))
            .border(width = 1.dp, color = com.example.ui.theme.VipGold.copy(alpha = 0.25f))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { (tab, label, icon) ->
            val selected = selectedTab == tab
            val tint = if (selected) Color(0xFF090D16) else com.example.ui.theme.VipGold
            Row(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) {
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(com.example.ui.theme.VipGold, com.example.ui.theme.VipGoldDeep)
                            )
                        } else {
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF0E172A), Color(0xFF0A1120))
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (selected) com.example.ui.theme.VipGold else com.example.ui.theme.VipGold.copy(alpha = 0.22f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectTab(tab) }
                    .padding(vertical = 7.dp, horizontal = 4.dp)
                    .testTag("nav_tab_${tab.name.lowercase()}"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.size(3.dp))
                Text(
                    text = label,
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.5.sp,
                    color = tint,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
