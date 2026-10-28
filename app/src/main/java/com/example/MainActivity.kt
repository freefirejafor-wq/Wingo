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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Banner
                    EngineHeader(
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
                                onResetStats = viewModel::resetCurrentModeStats
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

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun EngineHeader(
    matrixEnabled: Boolean,
    onToggleMatrix: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "⚡ WINGO PATTERN ENGINE ⚡",
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = CyberCyan,
                modifier = Modifier.testTag("app_header_title")
            )
            Text(
                text = "10-LAYER AUTO-CALIBRATED WEBSITE SEED AI",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                color = CyberCyanDim
            )
        }

        IconButton(
            onClick = onToggleMatrix,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF081B2E))
                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .testTag("toggle_matrix_button")
        ) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = "Toggle Matrix Effect",
                tint = if (matrixEnabled) NeonGreen else CyberCyanDim
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
        Triple(EngineTab.LIVE_SIGNAL, "SIGNAL", Icons.Default.Bolt),
        Triple(EngineTab.ANALYTICS, "TRICKS", Icons.Default.Analytics),
        Triple(EngineTab.FUND_PLANNER, "PLANNER", Icons.Default.Calculate),
        Triple(EngineTab.WEB_ENGINE, "WEB VIEW", Icons.Default.Public)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF040D19).copy(alpha = 0.96f))
            .border(width = 1.dp, color = CyberCyan.copy(alpha = 0.22f))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { (tab, label, icon) ->
            val selected = selectedTab == tab
            val tint = if (selected) Color(0xFF00141A) else CyberCyan
            val bg = if (selected) CyberCyan else Color(0xFF081726)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .border(
                        1.dp,
                        if (selected) CyberCyan else CyberCyan.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectTab(tab) }
                    .padding(vertical = 10.dp, horizontal = 4.dp)
                    .testTag("nav_tab_${tab.name.lowercase()}"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.size(3.dp))
                Text(
                    text = label,
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = tint,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
