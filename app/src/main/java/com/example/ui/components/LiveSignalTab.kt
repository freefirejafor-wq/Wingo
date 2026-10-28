package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PredictionRecordEntity
import com.example.data.model.DrawItem
import com.example.data.model.GameMode
import com.example.data.model.QuantumPrediction
import com.example.ui.WinGoUiState
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldDeep
import kotlin.math.roundToInt

/**
 * Luxury VIP Bento Grid Single-Screen Cockpit (Dedicated to WinGo 30S):
 * Features Dual-Pane Hero Signal + 3D Glossy Casino Number Spheres + 4 Sniper Formula Tiles +
 * Unified Self-Training AI & 0-9 Energy Boost Dock + 1-Row Verified History Strip.
 */
@Composable
fun LiveSignalTabContent(
    uiState: WinGoUiState,
    records: List<PredictionRecordEntity>,
    onSelectMode: (GameMode) -> Unit,
    onForceSync: () -> Unit,
    onResetStats: () -> Unit,
    onToggleWinNotif: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val prediction = uiState.prediction
    val resolved = records.filter { it.isWin != null }
    val winCount = resolved.count { it.isWin == true }
    val lossCount = resolved.count { it.isWin == false }
    val totalSignals = records.size
    val winRate = if (resolved.isNotEmpty()) {
        ((winCount * 100f) / resolved.size).roundToInt()
    } else {
        0
    }

    var currentStreak = 0
    for (r in resolved) {
        if (r.isWin == true) currentStreak++ else break
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // 1. VIP TELEMETRY & CIRCULAR TIMER BAR
        VipTopTelemetryBar(
            remainingSeconds = uiState.remainingSeconds,
            timerProgress = uiState.timerProgress,
            currentPeriod = prediction.currentPeriod,
            nextPeriod = prediction.nextPeriod,
            isLiveApi = uiState.isLiveApiConnected,
            winNotifEnabled = uiState.winNotificationEnabled,
            onToggleWinNotif = onToggleWinNotif,
            onForceSync = onForceSync
        )

        // 2. DUAL-PANE VIP BENTO HERO (Left: BIG/SMALL Signal, Right: 3D Casino Number Spheres)
        VipDualPaneHeroBento(prediction = prediction)

        // 3. 4-FORMULA SNIPER BENTO STRIP (DIAG P+N, SKIP-1 MID, 10-COMP, 5-ECHO)
        VipSniperFormulaBentoStrip(prediction = prediction)

        // 4. UNIFIED AI VAULT + 0-9 ENERGY BOOST & CROWD METER DOCK
        VipAiVaultAndEnergyDock(prediction = prediction)

        // 5. VIP SCOREBOARD + LAST 8 3D BALLS + 1-ROW VERIFIED LOG PILLS
        VipScoreboardAndHistoryDock(
            winCount = winCount,
            lossCount = lossCount,
            winRate = winRate,
            signalCount = totalSignals,
            streakCount = currentStreak,
            draws = uiState.draws.take(8),
            records = records.take(3),
            onResetStats = onResetStats
        )
    }
}

@Composable
private fun VipTopTelemetryBar(
    remainingSeconds: Int,
    timerProgress: Float,
    currentPeriod: String,
    nextPeriod: String,
    isLiveApi: Boolean,
    winNotifEnabled: Boolean,
    onToggleWinNotif: () -> Unit,
    onForceSync: () -> Unit
) {
    CyberCard(
        paddingDp = 8.dp,
        borderColor = VipGold.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: VIP 30S Crown Badge + Win Notification Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(VipGold, VipGoldDeep)
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("mode_btn_30s"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👑 30S VIP",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Color(0xFF090D16)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (winNotifEnabled) NeonGreen.copy(alpha = 0.14f)
                            else Color(0xFF0A1222)
                        )
                        .border(
                            1.dp,
                            if (winNotifEnabled) NeonGreen.copy(alpha = 0.65f)
                            else CyberCyanDim.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleWinNotif() }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                        .testTag("win_notif_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (winNotifEnabled) "🔔 WIN ALERT" else "🔕 MUTED",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = if (winNotifEnabled) NeonGreen else CyberCyanDim
                    )
                }
            }

            // Center: Live Period Transition
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isLiveApi) NeonGreen else NeonAmber)
                    )
                    Text(
                        text = "ISSUE #${currentPeriod.takeLast(4)}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        color = CyberCyanDim,
                        modifier = Modifier.testTag("current_period_text")
                    )
                }
                Text(
                    text = "NEXT #${nextPeriod.takeLast(5)}",
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = VipGold,
                    modifier = Modifier.testTag("next_period_text")
                )
            }

            // Right: Circular Countdown Ring + Sync Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CircularCountdownTimer(
                    remainingSeconds = remainingSeconds,
                    progress = timerProgress,
                    sizeDp = 36.dp,
                    modifier = Modifier.testTag("countdown_timer")
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color(0xFF131F38))
                        .border(1.dp, VipGold.copy(alpha = 0.4f), RoundedCornerShape(9.dp))
                        .clickable { onForceSync() }
                        .testTag("sync_api_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Live API",
                        tint = VipGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VipDualPaneHeroBento(prediction: QuantumPrediction) {
    val isBig = prediction.predictedSize == "BIG"
    val mainColor = if (isBig) NeonGreen else NeonRed
    val colorPillTint = when (prediction.predictedColor) {
        "GREEN" -> NeonGreen
        "RED" -> NeonRed
        else -> NeonViolet
    }

    val pulseTransition = rememberInfiniteTransition(label = "vipSignalPulse")
    val scale by pulseTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val animatedConf by animateFloatAsState(
        targetValue = (prediction.confidence / 100f).coerceIn(0f, 1f),
        animationSpec = tween(450),
        label = "conf"
    )

    val isSkipRound = prediction.riskBadge.contains("SKIP", ignoreCase = true)
    val safeNumber = prediction.energyRanking.firstOrNull { it.isSafeBoostPick }?.number ?: prediction.primaryNumber

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // LEFT BENTO (58%): Primary Signal + Confidence + Action Guide
        CyberCard(
            paddingDp = 10.dp,
            borderColor = if (isSkipRound) NeonAmber.copy(alpha = 0.7f) else mainColor.copy(alpha = 0.65f),
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Action Pill + Confidence %
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSkipRound) NeonRed.copy(alpha = 0.2f)
                                else mainColor.copy(alpha = 0.16f)
                            )
                            .border(
                                1.dp,
                                if (isSkipRound) NeonRed else mainColor.copy(alpha = 0.6f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isSkipRound) "🛑 SKIP TRAP" else "${prediction.riskBadge} • ${prediction.recommendedMultiplier}",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (isSkipRound) NeonAmber else mainColor
                        )
                    }

                    Text(
                        text = "${prediction.confidence}% CONF",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = VipGold,
                        modifier = Modifier.testTag("confidence_value")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Center: Glowing BIG / SMALL Hero Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    mainColor.copy(alpha = 0.22f),
                                    Color(0xFF060B16)
                                )
                            )
                        )
                        .border(1.dp, mainColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "VIP AI SIGNAL",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 8.sp,
                            color = CyberCyanDim
                        )
                        Text(
                            text = prediction.predictedSize,
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            color = mainColor,
                            modifier = Modifier
                                .scale(scale)
                                .testTag("hero_signal_size")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom: Animated Progress Bar + Action Guidance
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF091324))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedConf)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(mainColor, VipGold)
                                    )
                                )
                        )
                    }
                    Text(
                        text = prediction.banglaAdvice,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        color = if (isSkipRound) NeonAmber else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // RIGHT BENTO (42%): 3D Casino Number Spheres + Color Target
        CyberCard(
            paddingDp = 10.dp,
            borderColor = VipGold.copy(alpha = 0.55f),
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Color Target Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "JACKPOT",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = VipGold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorPillTint.copy(alpha = 0.18f))
                            .border(1.dp, colorPillTint, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = prediction.predictedColor,
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            color = colorPillTint,
                            modifier = Modifier.testTag("hero_signal_color")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Center: Two 3D Glossy Casino Lottery Spheres (MAIN & BACKUP)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_signal_numbers"),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Casino3DBall(
                            number = prediction.primaryNumber,
                            sizeDp = 42.dp,
                            isHighlightedGold = true
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "MAIN",
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = VipGold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Casino3DBall(
                            number = prediction.backupNumber,
                            sizeDp = 36.dp,
                            isHighlightedGold = false
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "BACKUP",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = CyberCyanDim
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom: Anti-Crowd Safe Pick
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFF081224))
                        .border(1.dp, VipGold.copy(alpha = 0.3f), RoundedCornerShape(7.dp))
                        .padding(vertical = 3.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ SAFE PICK: #$safeNumber",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = NeonGreen
                    )
                }
            }
        }
    }
}

@Composable
fun Casino3DBall(
    number: Int,
    sizeDp: Dp,
    isHighlightedGold: Boolean = false
) {
    val baseColor = when {
        number == 0 || number == 5 -> NeonViolet
        number in listOf(2, 4, 6, 8) -> NeonRed
        else -> NeonGreen
    }
    val fontSize = if (sizeDp >= 40.dp) 18.sp else if (sizeDp >= 30.dp) 14.sp else 11.sp

    Box(
        modifier = Modifier
            .size(sizeDp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        baseColor,
                        baseColor.copy(alpha = 0.45f),
                        Color(0xFF050914)
                    ),
                    center = Offset(25f, 25f),
                    radius = 95f
                )
            )
            .border(
                width = if (isHighlightedGold) 2.dp else 1.dp,
                color = if (isHighlightedGold) VipGold else Color.White.copy(alpha = 0.55f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp * 0.64f)
                .clip(CircleShape)
                .background(Color(0xFF070D19).copy(alpha = 0.82f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                color = if (isHighlightedGold) VipGold else Color.White
            )
        }
    }
}

@Composable
private fun VipSniperFormulaBentoStrip(prediction: QuantumPrediction) {
    val items = listOf(
        Triple("🎯 P+N CROSS", prediction.diagonalCrossCandidate, VipGold),
        Triple("📐 SKIP-1 MID", prediction.skipOneMidpointCandidate, NeonGreen),
        Triple("🔟 10-COMP", prediction.tenComplementCandidate, CyberCyan),
        Triple("🌊 5-ECHO", prediction.fiveCycleEchoCandidate, NeonViolet)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { (title, num, accent) ->
            val sizeTag = if (num >= 5) "BIG" else "SML"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF111C33), Color(0xFF080E1C))
                        )
                    )
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(vertical = 6.dp, horizontal = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        color = CyberCyanDim,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "#$num",
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = accent
                        )
                        Text(
                            text = sizeTag,
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = if (num >= 5) NeonGreen else NeonRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VipAiVaultAndEnergyDock(prediction: QuantumPrediction) {
    val bigCrowdPct = prediction.energyRanking.filter { it.number >= 5 }.sumOf { it.energyPercent }
    val smallCrowdPct = (100 - bigCrowdPct).coerceIn(1, 99)

    CyberCard(
        paddingDp = 9.dp,
        borderColor = VipGold.copy(alpha = 0.38f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Top Row: Self-Training AI Brain Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "🧠 SELF-LEARNING AI",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = VipGold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "VAULT: ${prediction.trainedDrawsCount}",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = NeonGreen
                        )
                    }
                }

                if (prediction.autoReversedCount > 0) {
                    Text(
                        text = "🔄 ${prediction.autoReversedCount} AUTO-REVERSED",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = NeonAmber
                    )
                } else {
                    Text(
                        text = "⚡ ANTI-CROWD SHIELD",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = CyberCyan
                    )
                }
            }

            // Discovered Transition Rule Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF070D1A))
                    .border(1.dp, VipGold.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = prediction.aiDiscoveredRuleBn,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 1-Row 0-9 Energy Boost Balls + Live %
            if (prediction.energyRanking.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    prediction.energyRanking.forEach { stat ->
                        val isTrap = stat.isCrowdTrap
                        val isSafe = stat.isSafeBoostPick
                        val ballColor = when (DrawItem.colorOf(stat.number)) {
                            "GREEN" -> NeonGreen
                            "RED" -> NeonRed
                            else -> NeonViolet
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ballColor.copy(alpha = if (isSafe) 0.35f else 0.16f))
                                    .border(
                                        width = if (isSafe || isTrap) 1.5.dp else 1.dp,
                                        color = when {
                                            isSafe -> VipGold
                                            isTrap -> NeonAmber
                                            else -> ballColor.copy(alpha = 0.55f)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${stat.number}",
                                    fontFamily = OrbitronFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = if (isSafe) VipGold else Color.White
                                )
                            }
                            Text(
                                text = "${stat.energyPercent}%",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = when {
                                    isTrap -> NeonAmber
                                    isSafe -> NeonGreen
                                    else -> CyberCyanDim
                                }
                            )
                        }
                    }
                }

                // Slim BIG vs SMALL Crowd Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CROWD BIG $bigCrowdPct%",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        color = if (bigCrowdPct >= 54) NeonAmber else NeonGreen
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(bigCrowdPct.coerceAtLeast(5).toFloat())
                                .fillMaxHeight()
                                .background(if (bigCrowdPct >= 54) NeonAmber else NeonGreen)
                        )
                        Box(
                            modifier = Modifier
                                .weight(smallCrowdPct.coerceAtLeast(5).toFloat())
                                .fillMaxHeight()
                                .background(if (smallCrowdPct >= 54) NeonAmber else NeonRed)
                        )
                    }
                    Text(
                        text = "SML $smallCrowdPct%",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        color = if (smallCrowdPct >= 54) NeonAmber else NeonRed
                    )
                }
            }
        }
    }
}

@Composable
private fun VipScoreboardAndHistoryDock(
    winCount: Int,
    lossCount: Int,
    winRate: Int,
    signalCount: Int,
    streakCount: Int,
    draws: List<DrawItem>,
    records: List<PredictionRecordEntity>,
    onResetStats: () -> Unit
) {
    CyberCard(
        paddingDp = 9.dp,
        borderColor = VipGold.copy(alpha = 0.32f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // 1. VIP Scoreboard Row (WIN, LOSS, STREAK, RATE, RESET)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VipStatBadge(
                    label = "🏆 WIN",
                    value = "$winCount",
                    color = NeonGreen,
                    tag = "stat_win",
                    modifier = Modifier.weight(1f)
                )
                VipStatBadge(
                    label = "❌ LOSS",
                    value = "$lossCount",
                    color = NeonRed,
                    tag = "stat_loss",
                    modifier = Modifier.weight(1f)
                )
                VipStatBadge(
                    label = "🔥 STREAK",
                    value = "${streakCount}W",
                    color = VipGold,
                    tag = "stat_streak",
                    modifier = Modifier.weight(1f)
                )
                VipStatBadge(
                    label = "📊 WIN %",
                    value = "$winRate%",
                    color = CyberCyan,
                    tag = "stat_signal",
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonRed.copy(alpha = 0.15f))
                        .border(1.dp, NeonRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onResetStats() }
                        .testTag("reset_stats_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Reset Session Stats",
                        tint = NeonRed,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // 2. Last 8 Draws Strip (3D Mini Balls)
            if (draws.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LAST 8:",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = VipGold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        draws.forEach { d ->
                            val isBig = d.number >= 5
                            val c = when (d.color) {
                                "GREEN" -> NeonGreen
                                "RED" -> NeonRed
                                else -> NeonViolet
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(c.copy(alpha = 0.22f))
                                        .border(1.dp, c, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${d.number}",
                                        fontFamily = OrbitronFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "${d.period.takeLast(2)}${if (isBig) "B" else "S"}",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 7.sp,
                                    color = CyberCyanDim
                                )
                            }
                        }
                    }
                }
            }

            // 3. 1-Row Last 3 Verified Logs (Zero vertical bloat!)
            if (records.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    records.take(3).forEach { rec ->
                        val statusText = when (rec.isWin) {
                            true -> if (rec.numberHit == true) "🎯WIN" else "✅WIN"
                            false -> "❌LOSS"
                            null -> "⏳WAIT"
                        }
                        val statusColor = when (rec.isWin) {
                            true -> NeonGreen
                            false -> NeonRed
                            null -> VipGold
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFF070D19))
                                .border(1.dp, statusColor.copy(alpha = 0.38f), RoundedCornerShape(7.dp))
                                .padding(horizontal = 5.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${rec.targetPeriod.takeLast(3)} ${rec.predictedSize.take(1)}(${rec.primaryNumber})",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = statusText,
                                    fontFamily = OrbitronFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 8.sp,
                                    color = statusColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VipStatBadge(
    label: String,
    value: String,
    color: Color,
    tag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF070D1A))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 7.sp,
                color = CyberCyanDim
            )
            Text(
                text = value,
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = color
            )
        }
    }
}
