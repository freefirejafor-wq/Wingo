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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoGraph
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.OrbitronFamily
import com.example.ui.theme.TextSecondary

@Composable
fun DeepAnalyticsTabContent(
    uiState: WinGoUiState,
    modifier: Modifier = Modifier
) {
    val pred = uiState.prediction

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. WEBSITE & MATH TRICK LEADERBOARD (LIVE 10-ROUND AUTO-BACKTEST)
        if (pred.formulaLeaderboard.isNotEmpty()) {
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "LIVE WEBSITE TRICK RANKING (সেরা ট্রিকস)",
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CyberCyan
                        )
                    }

                    pred.formulaLeaderboard.forEach { item ->
                        val isBig = item.nextVote == "BIG"
                        val voteColor = if (isBig) NeonGreen else NeonRed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (item.isTopTrick) NeonGreen.copy(alpha = 0.12f) else Color(0xFF040E1A))
                                .border(
                                    1.dp,
                                    if (item.isTopTrick) NeonGreen.copy(alpha = 0.5f) else CyberCyan.copy(alpha = 0.18f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (item.isTopTrick) "🏆 #1 ${item.formulaNameBn}" else item.formulaNameBn,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (item.isTopTrick) NeonGreen else Color.White
                                )
                                Text(
                                    text = "${item.formulaName} • Live Hit: ${item.liveWinRatePercent}%",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp,
                                    color = CyberCyanDim
                                )
                            }
                            Text(
                                text = item.nextVote,
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = voteColor
                            )
                        }
                    }
                }
            }
        }

        // 1. 5-LAYER AI CONSENSUS MATRIX
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI LAYER CONSENSUS (৫-লেয়ার বিশ্লেষণ)",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CyberCyan
                    )
                }

                pred.layerVotes.forEach { vote ->
                    val isBig = vote.favoredSize == "BIG"
                    val voteColor = if (isBig) NeonGreen else NeonRed
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF040E1A))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = vote.layerNameBn,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${vote.layerName} • ${vote.detail}",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "${vote.favoredSize} (${vote.strengthPercent}%)",
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = voteColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF102030))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((vote.strengthPercent / 100f).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(voteColor)
                            )
                        }
                    }
                }
            }
        }

        // 2. HOT & COLD NUMBERS + 0-9 FREQUENCY CHART
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "HOT / COLD NUMBERS (গত ২৫ পিরিয়ড)",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CyberCyan
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonGreen.copy(alpha = 0.1f))
                            .border(1.dp, NeonGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("🔥 HOT NUMBERS", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = pred.hotNumbers.joinToString(" • "),
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonBlue.copy(alpha = 0.1f))
                            .border(1.dp, NeonBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("❄️ COLD (DUE)", fontSize = 11.sp, color = NeonBlue, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = pred.coldNumbers.joinToString(" • "),
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // 0-9 Frequency Bars
                val maxFreq = (pred.numberFrequencies.values.maxOrNull() ?: 1).coerceAtLeast(1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    (0..9).forEach { num ->
                        val count = pred.numberFrequencies[num] ?: 0
                        val ratio = (count.toFloat() / maxFreq.toFloat()).coerceIn(0.08f, 1f)
                        val barColor = if (num >= 5) NeonGreen else NeonRed
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                text = "${count}x",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = CyberCyanDim
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height((56 * ratio).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(barColor)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = num.toString(),
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 3. BIG/SMALL & COLOR DISTRIBUTION
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "BOARD PRESSURE & TRIGGERS",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CyberCyan
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("BIG: ${pred.bigRatioPercent}%", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("SMALL: ${pred.smallRatioPercent}%", color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    val bWeight = pred.bigRatioPercent.coerceAtLeast(5).toFloat()
                    val sWeight = pred.smallRatioPercent.coerceAtLeast(5).toFloat()
                    Box(Modifier.weight(bWeight).fillMaxHeight().background(NeonGreen))
                    Box(Modifier.weight(sWeight).fillMaxHeight().background(NeonRed))
                }

                pred.triggers.forEach { trig ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF040E1A))
                            .border(
                                1.dp,
                                if (trig.isHighPriority) NeonGreen.copy(alpha = 0.4f) else CyberCyan.copy(alpha = 0.2f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = trig.title,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (trig.isHighPriority) NeonGreen else CyberCyan
                            )
                            Text(
                                text = trig.impactLabel,
                                fontFamily = OrbitronFamily,
                                fontSize = 10.sp,
                                color = NeonAmber
                            )
                        }
                        Text(
                            text = trig.descriptionBn,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = CyberCyanDim
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "SMART FUND & STEP PLANNER (ফান্ড ম্যানেজমেন্ট)",
                        fontFamily = OrbitronFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CyberCyan
                    )
                }

                Text(
                    text = "লস এড়াতে আপনার মোট ব্যালেন্স অনুযায়ী ৫ থেকে ৭ স্টেপের প্ল্যান করে খেলুন। নিচে আপনার মোট ফান্ড লিখুন:",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.sp,
                    color = CyberCyanDim
                )

                OutlinedTextField(
                    value = uiState.totalCapitalInput,
                    onValueChange = onCapitalChange,
                    label = { Text("Total Balance / Capital (৳ / $)", fontFamily = JetBrainsMonoFamily) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberCyan.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = CyberCyanDim
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capital_input_field")
                )

                // Step Count Selector (4, 5, 6, 7)
                Text(
                    text = "NUMBER OF SAFETY LEVELS (স্টেপ সংখ্যা): ${uiState.selectedStepCount} STEPS",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CyberCyan
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(4, 5, 6, 7).forEach { step ->
                        val active = uiState.selectedStepCount == step
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) CyberCyan else Color(0xFF081521))
                                .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .clickable { onStepCountChange(step) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${step}L",
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.Black else CyberCyan
                            )
                        }
                    }
                }

                // Multiplier Selector (2.0x, 2.2x, 2.5x, 3.0x)
                Text(
                    text = "MULTIPLIER RATIO (গুণিতক): ${uiState.multiplierScale}x",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CyberCyan
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2.0f, 2.2f, 2.5f, 3.0f).forEach { mult ->
                        val active = uiState.multiplierScale == mult
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) NeonGreen else Color(0xFF081521))
                                .border(1.dp, NeonGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .clickable { onMultiplierChange(mult) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mult}x",
                                fontFamily = OrbitronFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (active) Color.Black else NeonGreen
                            )
                        }
                    }
                }

                // Step Table
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCyan.copy(alpha = 0.12f))
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                ) {
                    Text("LEVEL", Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    Text("BET STAKE", Modifier.weight(1.1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan, textAlign = TextAlign.Center)
                    Text("TOTAL USED", Modifier.weight(1.1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan, textAlign = TextAlign.Center)
                    Text("NET PROFIT", Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan, textAlign = TextAlign.End)
                }

                uiState.fundSteps.forEach { stepItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF040E1A))
                            .padding(vertical = 9.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LVL ${stepItem.level}",
                            modifier = Modifier.weight(0.9f),
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyberCyan
                        )
                        Text(
                            text = "${stepItem.betAmount}",
                            modifier = Modifier.weight(1.1f),
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = NeonAmber,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${stepItem.cumulativeCost}",
                            modifier = Modifier.weight(1.1f),
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = CyberCyanDim,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "+${stepItem.netProfitIfWin}",
                            modifier = Modifier.weight(1f),
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeonGreen,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
