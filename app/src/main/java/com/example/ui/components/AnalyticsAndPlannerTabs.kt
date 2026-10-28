package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WinGoUiState
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldDeep

@Composable
fun DeepAnalyticsTabContent(
    uiState: WinGoUiState,
    modifier: Modifier = Modifier
) {
    val pred = uiState.prediction

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. VIP SELF-LEARNING AI VAULT & 2-COLUMN FORMULA MATRIX
        if (pred.formulaLeaderboard.isNotEmpty()) {
            CyberCard(
                paddingDp = 10.dp,
                borderColor = VipGold.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
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
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = VipGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "👑 15-FORMULA AI MATRIX",
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = VipGold
                            )
                        }
                        Text(
                            text = "🧠 VAULT: ${pred.trainedDrawsCount} • 🔄 REV: ${pred.autoReversedCount}",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = NeonGreen
                        )
                    }

                    // 2-Column Grid of Formula Tiles (Compact, zero endless scrolling!)
                    pred.formulaLeaderboard.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            pair.forEach { item ->
                                val isBig = item.nextVote == "BIG"
                                val voteColor = if (isBig) NeonGreen else NeonRed
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (item.isTopTrick) VipGold.copy(alpha = 0.12f)
                                            else Color(0xFF070D19)
                                        )
                                        .border(
                                            1.dp,
                                            when {
                                                item.isTopTrick -> VipGold.copy(alpha = 0.65f)
                                                item.isAutoReversed -> NeonAmber.copy(alpha = 0.5f)
                                                else -> CyberCyan.copy(alpha = 0.2f)
                                            },
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (item.isTopTrick) "🏆 ${item.formulaNameBn}" else item.formulaNameBn,
                                                fontFamily = JetBrainsMonoFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = if (item.isTopTrick) VipGold else Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Hit: ${item.liveWinRatePercent}%",
                                                fontFamily = JetBrainsMonoFamily,
                                                fontSize = 8.sp,
                                                color = if (item.isAutoReversed) NeonAmber else CyberCyanDim
                                            )
                                        }
                                        Text(
                                            text = item.nextVote,
                                            fontFamily = OrbitronFamily,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = voteColor
                                        )
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 2. COMPACT 0-9 MISSING & OVERDUE CHART + HOT/COLD STRIP
        CyberCard(
            paddingDp = 10.dp,
            borderColor = CyberCyan.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊 0–9 MISSING & HOT/COLD CHART",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = VipGold
                    )
                    Text(
                        text = "🔥HOT: ${pred.hotNumbers.joinToString(",")} • ❄️COLD: ${pred.coldNumbers.joinToString(",")}",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = NeonGreen
                    )
                }

                if (pred.chartStats.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        pred.chartStats.forEach { st ->
                            val isOverdue = st.currentMissing > st.avgMissing && st.currentMissing >= 6
                            val numColor = if (st.number >= 5) NeonGreen else NeonRed
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isOverdue) NeonAmber.copy(alpha = 0.14f) else Color(0xFF070D19))
                                    .border(
                                        1.dp,
                                        if (isOverdue) NeonAmber else numColor.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "#${st.number}",
                                    fontFamily = OrbitronFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = numColor
                                )
                                Text(
                                    text = "${st.currentMissing}m",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    color = if (isOverdue) NeonAmber else Color.White
                                )
                                Text(
                                    text = "${st.frequency}x",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 7.sp,
                                    color = CyberCyanDim
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. COMPACT 5-LAYER AI CONSENSUS + ACTIVE TRIGGERS
        CyberCard(
            paddingDp = 10.dp,
            borderColor = VipGold.copy(alpha = 0.32f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = VipGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "⚡ 5-LAYER CONSENSUS & TRIGGERS",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = VipGold
                    )
                }

                pred.layerVotes.forEach { vote ->
                    val isBig = vote.favoredSize == "BIG"
                    val voteColor = if (isBig) NeonGreen else NeonRed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070D19))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = vote.layerNameBn,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                            Text(
                                text = vote.detail,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 8.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${vote.favoredSize} (${vote.strengthPercent}%)",
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = voteColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FundPlannerTabContent(
    uiState: WinGoUiState,
    onCapitalChange: (String) -> Unit,
    onStepCountChange: (Int) -> Unit,
    onMultiplierChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeStep = uiState.prediction.recommendedMultiplier
        .filter { it.isDigit() }
        .toIntOrNull()
        ?.coerceIn(1, uiState.selectedStepCount)
        ?: 1

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CyberCard(
            paddingDp = 10.dp,
            borderColor = VipGold.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = VipGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "💎 VIP 30S FUND PLANNER",
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = VipGold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonGreen.copy(alpha = 0.16f))
                            .border(1.dp, NeonGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "👉 ACTIVE: LVL $activeStep",
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = NeonGreen
                        )
                    }
                }

                // 1-Tap Quick Balance Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    listOf("500", "1000", "3000", "5000", "10000").forEach { preset ->
                        val selected = uiState.totalCapitalInput == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) Brush.horizontalGradient(listOf(VipGold, VipGoldDeep))
                                    else Brush.horizontalGradient(listOf(Color(0xFF0B1426), Color(0xFF0B1426)))
                                )
                                .border(
                                    1.dp,
                                    if (selected) VipGold else VipGold.copy(alpha = 0.25f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onCapitalChange(preset) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "৳$preset",
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (selected) Color.Black else VipGold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.totalCapitalInput,
                    onValueChange = onCapitalChange,
                    label = { Text("Total Capital (৳ / $)", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VipGold,
                        unfocusedBorderColor = VipGold.copy(alpha = 0.35f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = VipGold,
                        unfocusedLabelColor = CyberCyanDim
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capital_input_field")
                )

                // Compact 1-Row Step Count + Multiplier Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Steps (4L..7L)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(4, 5, 6, 7).forEach { step ->
                            val active = uiState.selectedStepCount == step
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) VipGold else Color(0xFF081224))
                                    .border(1.dp, VipGold.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .clickable { onStepCountChange(step) }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${step}L",
                                    fontFamily = OrbitronFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (active) Color.Black else VipGold
                                )
                            }
                        }
                    }

                    // Multiplier (2x..3x)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(2.0f, 2.2f, 2.5f, 3.0f).forEach { mult ->
                            val active = uiState.multiplierScale == mult
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) NeonGreen else Color(0xFF081224))
                                    .border(1.dp, NeonGreen.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .clickable { onMultiplierChange(mult) }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mult}x",
                                    fontFamily = OrbitronFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (active) Color.Black else NeonGreen
                                )
                            }
                        }
                    }
                }

                // Step Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(7.dp))
                        .background(VipGold.copy(alpha = 0.14f))
                        .padding(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    Text("STEP", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VipGold)
                    Text("BET STAKE", Modifier.weight(1.1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VipGold, textAlign = TextAlign.Center)
                    Text("USED", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VipGold, textAlign = TextAlign.Center)
                    Text("PROFIT", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VipGold, textAlign = TextAlign.End)
                }

                uiState.fundSteps.forEach { stepItem ->
                    val isRecommendedNow = stepItem.level == activeStep
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isRecommendedNow) NeonGreen.copy(alpha = 0.14f)
                                else Color(0xFF070D19)
                            )
                            .border(
                                1.dp,
                                if (isRecommendedNow) NeonGreen else VipGold.copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 7.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRecommendedNow) "👉 LVL ${stepItem.level}" else "LVL ${stepItem.level}",
                            modifier = Modifier.weight(1f),
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isRecommendedNow) NeonGreen else VipGold
                        )
                        Text(
                            text = "৳${stepItem.betAmount}",
                            modifier = Modifier.weight(1.1f),
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = NeonAmber,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "৳${stepItem.cumulativeCost}",
                            modifier = Modifier.weight(1f),
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            color = CyberCyanDim,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "+৳${stepItem.netProfitIfWin}",
                            modifier = Modifier.weight(1f),
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeonGreen,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
