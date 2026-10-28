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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PredictionRecordEntity
import com.example.data.model.DrawItem
import com.example.data.model.EnergyBallStat
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
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun LiveSignalTabContent(
    uiState: WinGoUiState,
    records: List<PredictionRecordEntity>,
    onSelectMode: (GameMode) -> Unit,
    onForceSync: () -> Unit,
    onResetStats: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // MAIN SIGNAL ENGINE CARD
        CyberCard(
            borderColor = CyberCyan.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 1. Mode Selector Row (30S, 1M, 3M, 5M)
                ModeSelectorRow(
                    selectedMode = uiState.selectedMode,
                    onSelectMode = onSelectMode
                )

                // 2. Timer + Period Telemetry Row
                TimerAndPeriodRow(
                    remainingSeconds = uiState.remainingSeconds,
                    timerProgress = uiState.timerProgress,
                    currentPeriod = prediction.currentPeriod,
                    nextPeriod = prediction.nextPeriod,
                    apiStatusText = uiState.apiStatusText,
                    isLiveApi = uiState.isLiveApiConnected,
                    onForceSync = onForceSync
                )

                // 3. Quantum Signal Hero Display (with Market Regime + 10-Comp / 5-Echo chips)
                QuantumSignalHero(prediction = prediction)

                // 4. Confidence Bar
                ConfidenceMeter(confidence = prediction.confidence)

                // 5. Active Pattern & Bengali Strategy Box
                PatternMatrixBox(prediction = prediction)

                // 6. Win / Loss / Win Rate / Streak Grid
                StatsGrid(
                    winCount = winCount,
                    lossCount = lossCount,
                    winRatePercent = winRate,
                    signalCount = totalSignals,
                    streakCount = currentStreak,
                    onResetStats = onResetStats
                )
            }
        }

        // NEW: DKWIN9 ENERGY BOOST & HOT3 / COLD3 / 10-COMPLEMENT RANKING CARD
        if (prediction.energyRanking.isNotEmpty()) {
            DkwinEnergyBoostCard(
                prediction = prediction,
                modeLabel = uiState.selectedMode.label
            )
        }

        // VERIFIED SIGNAL ACCURACY LOG CARD
        if (records.isNotEmpty()) {
            VerifiedSignalLogCard(records = records.take(8))
        }

        // LIVE HISTORY TABLE CARD
        LiveHistoryTableCard(draws = uiState.draws.take(12))
    }
}

@Composable
private fun ModeSelectorRow(
    selectedMode: GameMode,
    onSelectMode: (GameMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GameMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            val bg = if (isSelected) CyberCyan else Color(0xFF081521)
            val contentColor = if (isSelected) Color(0xFF00141A) else CyberCyan
            Box(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) CyberCyan else CyberCyan.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectMode(mode) }
                    .padding(vertical = 12.dp)
                    .testTag("mode_btn_${mode.key}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun TimerAndPeriodRow(
    remainingSeconds: Int,
    timerProgress: Float,
    currentPeriod: String,
    nextPeriod: String,
    apiStatusText: String,
    isLiveApi: Boolean,
    onForceSync: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularCountdownTimer(
            remainingSeconds = remainingSeconds,
            progress = timerProgress,
            modifier = Modifier.testTag("countdown_timer")
        )

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isLiveApi) NeonGreen else NeonAmber)
                )
                Text(
                    text = apiStatusText,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    color = if (isLiveApi) NeonGreen else NeonAmber
                )
                IconButton(
                    onClick = onForceSync,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sync_api_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Live API",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "CURRENT ISSUE",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                color = CyberCyanDim
            )
            Text(
                text = currentPeriod.takeLast(7),
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White,
                modifier = Modifier.testTag("current_period_text")
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "NEXT TARGET ISSUE",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                color = CyberCyanDim
            )
            Text(
                text = nextPeriod.takeLast(7),
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = CyberCyan,
                modifier = Modifier.testTag("next_period_text")
            )
        }
    }
}

@Composable
private fun QuantumSignalHero(prediction: QuantumPrediction) {
    val isBig = prediction.predictedSize == "BIG"
    val mainSignalColor = if (isBig) NeonGreen else NeonRed

    val pulseTransition = rememberInfiniteTransition(label = "signalPulse")
    val scale by pulseTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val colorDotTint = when (prediction.predictedColor) {
        "GREEN" -> NeonGreen
        "RED" -> NeonRed
        else -> NeonViolet
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF040E1A))
            .border(
                width = 1.dp,
                color = mainSignalColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Risk & Recommended Multiplier Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(mainSignalColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = prediction.riskBadge,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = mainSignalColor
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCyan.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "STAKE: ${prediction.recommendedMultiplier}",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = CyberCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Market Regime Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(NeonAmber.copy(alpha = 0.12f))
                .border(1.dp, NeonAmber.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = prediction.marketRegimeTitle,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = NeonAmber,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "NEXT DKWIN9 12-FORMULA SIGNAL",
            fontFamily = JetBrainsMonoFamily,
            fontSize = 12.sp,
            color = CyberCyanDim
        )

        // BIG / SMALL Giant Glowing Display
        Text(
            text = prediction.predictedSize,
            fontFamily = OrbitronFamily,
            fontWeight = FontWeight.Black,
            fontSize = 54.sp,
            color = mainSignalColor,
            modifier = Modifier
                .scale(scale)
                .testTag("prediction_main_size")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dual Number Badges + Color Prediction
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primary Number
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MAIN NO",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(NeonBlue.copy(alpha = 0.16f))
                        .border(2.dp, NeonBlue, CircleShape)
                        .testTag("prediction_primary_number"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = prediction.primaryNumber.toString(),
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        color = NeonBlue
                    )
                }
            }

            // Color Badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "COLOR",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorDotTint.copy(alpha = 0.18f))
                        .border(1.dp, colorDotTint, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("prediction_color_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = prediction.predictedColor,
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colorDotTint
                    )
                }
            }

            // Backup Number
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BACKUP NO",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.12f))
                        .border(1.5.dp, CyberCyan.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = prediction.backupNumber.toString(),
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = CyberCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // DKWin9 Special Formula Quick Strip: 10-Complement | 5-Cycle Echo | Repeat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SpecialFormulaMiniChip(
                label = "10-COMP (10-N)",
                value = "#${prediction.tenComplementCandidate}",
                tint = NeonGreen,
                modifier = Modifier.weight(1f)
            )
            SpecialFormulaMiniChip(
                label = "5-CYCLE ECHO",
                value = "#${prediction.fiveCycleEchoCandidate}",
                tint = CyberCyan,
                modifier = Modifier.weight(1f)
            )
            SpecialFormulaMiniChip(
                label = "LAST REPEAT",
                value = "#${prediction.repeatCandidate}",
                tint = NeonAmber,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SpecialFormulaMiniChip(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = 0.08f))
            .border(1.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFamily,
            fontSize = 9.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontFamily = OrbitronFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = tint
        )
    }
}

/**
 * Reconstructs DKWin9's logged-in `⚡ Energy Boost` modal (from the user's screenshot)
 * with Top 1 🥇 (+100%), Top 2 🥈 (+80%), Top 3 🥉 (+50%), Energy %, ↑/↓ Rank Delta,
 * and interactive filter pills (`⚡ Safe Boost`, `🔥 Hot 3`, `❄️ Cold 3`, `🔄 10-Comp`).
 */
@Composable
private fun DkwinEnergyBoostCard(
    prediction: QuantumPrediction,
    modeLabel: String
) {
    var selectedFilter by remember { mutableStateOf("SAFE") } // SAFE, HOT, COLD, COMP

    val highlightedNumbers: Set<Int> = when (selectedFilter) {
        "HOT" -> prediction.hotNumbers.toSet()
        "COLD" -> prediction.coldNumbers.toSet()
        "COMP" -> setOf(
            prediction.tenComplementCandidate,
            prediction.fiveCycleEchoCandidate,
            prediction.repeatCandidate
        )
        else -> setOf(prediction.primaryNumber, prediction.backupNumber) +
            prediction.energyRanking.filter { it.isSafeBoostPick }.map { it.number }.take(2)
    }

    CyberCard(
        borderColor = NeonAmber.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header matching DKWin9 Energy Boost bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "⚡ DKWIN9 ENERGY BOOST • $modeLabel",
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = NeonAmber
                        )
                        Text(
                            text = "Top 3 Bonus Ranking + Anti-Crowd Trap Scanner",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = CyberCyanDim
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .border(1.dp, NeonGreen.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PICK: #${prediction.primaryNumber} / #${prediction.backupNumber}",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NeonGreen
                    )
                }
            }

            // 4 Interactive Filter Buttons (matching Energy Boost modal: Safe Boost, Repeat/10-Comp, Hot 3, Cold 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EnergyFilterButton(
                    label = "⚡ Safe Boost",
                    active = selectedFilter == "SAFE",
                    activeColor = NeonGreen,
                    onClick = { selectedFilter = "SAFE" },
                    modifier = Modifier.weight(1f)
                )
                EnergyFilterButton(
                    label = "🔄 10-Comp (${prediction.tenComplementCandidate})",
                    active = selectedFilter == "COMP",
                    activeColor = CyberCyan,
                    onClick = { selectedFilter = "COMP" },
                    modifier = Modifier.weight(1f)
                )
                EnergyFilterButton(
                    label = "🔥 Hot 3",
                    active = selectedFilter == "HOT",
                    activeColor = NeonAmber,
                    onClick = { selectedFilter = "HOT" },
                    modifier = Modifier.weight(0.9f)
                )
                EnergyFilterButton(
                    label = "❄️ Cold 3",
                    active = selectedFilter == "COLD",
                    activeColor = NeonBlue,
                    onClick = { selectedFilter = "COLD" },
                    modifier = Modifier.weight(0.9f)
                )
            }

            // Row 1: Balls 0..4 (SMALL)
            val row1 = prediction.energyRanking.filter { it.number in 0..4 }.sortedBy { it.number }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row1.forEach { stat ->
                    EnergyBallItem(
                        stat = stat,
                        isHighlighted = stat.number in highlightedNumbers
                    )
                }
            }

            // Row 2: Balls 5..9 (BIG)
            val row2 = prediction.energyRanking.filter { it.number in 5..9 }.sortedBy { it.number }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row2.forEach { stat ->
                    EnergyBallItem(
                        stat = stat,
                        isHighlighted = stat.number in highlightedNumbers
                    )
                }
            }

            // Bonus Legend + Crowd Trap Warning
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BonusPill(text = "🥇 #1 +100% (Crowd Risk)", tint = NeonRed, modifier = Modifier.weight(1f))
                BonusPill(text = "🥈 #2 +80% (Safe Value)", tint = NeonGreen, modifier = Modifier.weight(1f))
                BonusPill(text = "🥉 #3 +50% (Safe Value)", tint = CyberCyan, modifier = Modifier.weight(1f))
            }

            // Crowd Bet Load (SMALL 0-4 vs BIG 5-9 Energy %) -> House Liquidity Direction
            val smallCrowdPct = row1.sumOf { it.energyPercent }
            val bigCrowdPct = row2.sumOf { it.energyPercent }
            val lighterSide = if (bigCrowdPct <= smallCrowdPct) "BIG (কম ভিড় • হাউস সেফ)" else "SMALL (কম ভিড় • হাউস সেফ)"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF040D18))
                    .border(1.dp, CyberCyan.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "👥 CROWD BET (SMALL 0-4): $smallCrowdPct%",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = NeonRed
                    )
                    Text(
                        text = "CROWD BET (BIG 5-9): $bigCrowdPct%",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = NeonGreen
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(smallCrowdPct.coerceAtLeast(10).toFloat())
                            .fillMaxHeight()
                            .background(NeonRed)
                    )
                    Box(
                        modifier = Modifier
                            .weight(bigCrowdPct.coerceAtLeast(10).toFloat())
                            .fillMaxHeight()
                            .background(NeonGreen)
                    )
                }
                Text(
                    text = "🏦 হাউস লিকুইডিটি টার্গেট: $lighterSide | #1 নম্বর এড়িয়ে #2/#3 বা সিগন্যাল নম্বর ধরুন",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    color = NeonAmber
                )
            }
        }
    }
}

@Composable
private fun EnergyFilterButton(
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) activeColor.copy(alpha = 0.22f) else Color(0xFF061322))
            .border(
                width = 1.dp,
                color = if (active) activeColor else CyberCyan.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 7.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = if (active) activeColor else CyberCyanDim,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EnergyBallItem(
    stat: EnergyBallStat,
    isHighlighted: Boolean
) {
    val ballColor = when {
        stat.number == 0 || stat.number == 5 -> NeonViolet
        stat.number % 2 == 0 -> NeonRed
        else -> NeonGreen
    }

    val medalBadge = when (stat.rankOrder) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> null
    }

    val deltaPrefix = if (stat.trendDelta >= 0) "↑${stat.trendDelta}" else "↓${abs(stat.trendDelta)}"
    val deltaColor = if (stat.trendDelta >= 0) NeonGreen else NeonRed

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isHighlighted) NeonAmber.copy(alpha = 0.14f) else Color.Transparent
            )
            .border(
                width = if (isHighlighted) 1.5.dp else 0.dp,
                color = if (isHighlighted) NeonAmber else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ballColor.copy(alpha = 0.22f))
                    .border(
                        width = if (isHighlighted) 2.dp else 1.5.dp,
                        color = if (isHighlighted) NeonAmber else ballColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stat.number.toString(),
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
            if (medalBadge != null) {
                Text(
                    text = medalBadge,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = deltaPrefix,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = deltaColor
            )
            Text(
                text = "${stat.energyPercent}%",
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (stat.rankOrder <= 3) NeonAmber else Color.White
            )
        }
    }
}

@Composable
private fun BonusPill(
    text: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tint.copy(alpha = 0.1f))
            .border(1.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = tint,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConfidenceMeter(confidence: Int) {
    val animatedFill by animateFloatAsState(
        targetValue = (confidence / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "confidenceFill"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SIGNAL CONFIDENCE",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CyberCyanDim
            )
            Text(
                text = "$confidence%",
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = NeonGreen,
                modifier = Modifier.testTag("confidence_text")
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF101C2B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFill)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(CyberCyan, NeonGreen)
                        )
                    )
            )
        }
    }
}

@Composable
private fun PatternMatrixBox(prediction: QuantumPrediction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF05101E))
            .border(1.dp, CyberCyan.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "LIVE DKWIN9 MARKET & বাংলা গাইড",
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CyberCyan
            )
        }

        Text(
            text = prediction.topWebsiteTrickBadge,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = NeonAmber
        )

        Text(
            text = prediction.headlinePattern,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = NeonGreen,
            modifier = Modifier.testTag("pattern_headline_text")
        )

        Text(
            text = prediction.banglaAdvice,
            fontFamily = JetBrainsMonoFamily,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = CyberCyanDim
        )
    }
}

@Composable
private fun StatsGrid(
    winCount: Int,
    lossCount: Int,
    winRatePercent: Int,
    signalCount: Int,
    streakCount: Int,
    onResetStats: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SESSION ACCURACY (WIN RATE: $winRatePercent%)",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = CyberCyanDim
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onResetStats() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("reset_stats_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Reset Stats",
                    tint = TextSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "RESET",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                label = "WIN",
                value = winCount.toString(),
                valueColor = NeonGreen,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_win_count")
            )
            StatTile(
                label = "LOSS",
                value = lossCount.toString(),
                valueColor = NeonRed,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_loss_count")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                label = "SIGNAL",
                value = signalCount.toString(),
                valueColor = CyberCyan,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_signal_count")
            )
            StatTile(
                label = "STREAK",
                value = "${streakCount}🔥",
                valueColor = NeonAmber,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_streak_count")
            )
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF050F1D))
            .border(1.dp, valueColor.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFamily,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = OrbitronFamily,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = valueColor
        )
    }
}

@Composable
private fun VerifiedSignalLogCard(records: List<PredictionRecordEntity>) {
    CyberCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "SIGNAL VERIFICATION LOG (লাইভ ফলাফল)",
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CyberCyan
                )
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCyan.copy(alpha = 0.12f))
                    .padding(vertical = 8.dp, horizontal = 8.dp)
            ) {
                Text("ISSUE", Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                Text("PREDICT", Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan, textAlign = TextAlign.Center)
                Text("ACTUAL", Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan, textAlign = TextAlign.Center)
                Text("STATUS", Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan, textAlign = TextAlign.End)
            }

            records.forEach { rec ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rec.targetPeriod.takeLast(7),
                        modifier = Modifier.weight(1.2f),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${rec.predictedSize} (${rec.primaryNumber}/${rec.backupNumber})",
                        modifier = Modifier.weight(1.2f),
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (rec.predictedSize == "BIG") NeonGreen else NeonRed,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (rec.actualNumber != null) "${rec.actualSize} (${rec.actualNumber})" else "WAITING",
                        modifier = Modifier.weight(1f),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 12.sp,
                        color = CyberCyanDim,
                        textAlign = TextAlign.Center
                    )

                    val badgeText = when (rec.isWin) {
                        true -> if (rec.numberHit == true) "JACKPOT 🎯" else "WIN ✅"
                        false -> "LOSS ❌"
                        null -> "LIVE ⏳"
                    }
                    val badgeColor = when (rec.isWin) {
                        true -> NeonGreen
                        false -> NeonRed
                        null -> NeonAmber
                    }
                    Text(
                        text = badgeText,
                        modifier = Modifier.weight(1f),
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = badgeColor,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveHistoryTableCard(draws: List<DrawItem>) {
    CyberCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "LIVE API HISTORY",
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CyberCyan
                )
            }

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCyan.copy(alpha = 0.12f))
                    .padding(vertical = 8.dp, horizontal = 10.dp)
            ) {
                Text("PERIOD", Modifier.weight(1.4f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                Text("NO", Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan, textAlign = TextAlign.Center)
                Text("SIZE", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan, textAlign = TextAlign.Center)
                Text("CLR", Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan, textAlign = TextAlign.End)
            }

            if (draws.isEmpty()) {
                Text(
                    text = "LOADING LIVE TELEMETRY...",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 13.sp,
                    color = CyberCyanDim,
                    modifier = Modifier.padding(12.dp)
                )
            } else {
                draws.forEachIndexed { idx, item ->
                    val rowBg = if (idx % 2 == 1) CyberCyan.copy(alpha = 0.04f) else Color.Transparent
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(rowBg)
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.shortPeriod,
                            modifier = Modifier.weight(1.4f),
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = item.number.toString(),
                            modifier = Modifier.weight(0.8f),
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeonBlue,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = item.size,
                            modifier = Modifier.weight(1f),
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (item.size == "BIG") NeonGreen else NeonRed,
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier.weight(0.8f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            val dotColor = when (item.color) {
                                "GREEN" -> NeonGreen
                                "RED" -> NeonRed
                                else -> NeonViolet
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
