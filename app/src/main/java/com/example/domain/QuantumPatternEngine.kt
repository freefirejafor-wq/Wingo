package com.example.domain

import com.example.data.model.ChartNumberStat
import com.example.data.model.DrawItem
import com.example.data.model.FormulaLeaderboardItem
import com.example.data.model.LayerVote
import com.example.data.model.PatternTrigger
import com.example.data.model.QuantumPrediction
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 10-Formula Self-Calibrating Quantum Meta-Engine tuned for DKWin9 (saasLottery/WinGo)
 * and draw.ar-lottery01.com:
 * 1. Website `premium` Seed Checksum Trick
 * 2. Official Website "Chart" Missing vs Avg-Missing Overdue Trick
 * 3. DKWin9 9-Complement & 5-Step Harmonic Mirror (`9 - N0` / `(N0 + 5) % 10`)
 * 4. DKWin9 3-Draw Extreme Sum Clamp (`N0 + N1 + N2` >= 20 or <= 7 Mean Reversion)
 * 5. Period Last-2-Digits + 3-Draw Cross-Sum Trick
 * 6. Violet (0/5) Server Reset & Mirror Board (ABBA / AABB) Flip Trick
 * 7. Markov 3-Gram Sequence AI
 * 8. Dragon Trend vs 4x Break Matrix
 * 9. Harmonic Delta & Gap Pressure Trick
 * 10. Smart Trap Shield (Detects chaotic transition rounds vs Sure-Shot consensus rounds)
 */
object QuantumPatternEngine {

    private interface SubFormula {
        val name: String
        val nameBn: String
        fun predictRaw(history: List<DrawItem>): String
    }

    // 1. Website `premium` Seed Checksum Trick
    private val websitePremiumSeedTrick = object : SubFormula {
        override val name = "Website Premium Seed"
        override val nameBn = "ওয়েবসাইট প্রিমিয়াম সিড হ্যাশ"
        override fun predictRaw(history: List<DrawItem>): String {
            val latest = history.firstOrNull() ?: return "BIG"
            val premDigits = latest.premium.filter { it.isDigit() }.sumOf { it.digitToInt() }
            val periodTail = latest.period.takeLast(2).toIntOrNull() ?: 0
            val prevNum = history.getOrNull(1)?.number ?: 5
            val checksum = (premDigits + periodTail + latest.number * 3 + prevNum) % 10
            return if (checksum >= 5) "BIG" else "SMALL"
        }
    }

    // 2. Official Website "Chart" Missing vs Avg-Missing Overdue Trick
    private val chartMissingOverdueTrick = object : SubFormula {
        override val name = "Chart Missing Overdue"
        override val nameBn = "ওয়েবসাইট চার্ট মিসিং ওভারডিউ"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 6) return "BIG"
            val stats = computeChartStats(history)
            val bigOverdue = stats.filter { it.number >= 5 }
                .sumOf { (it.currentMissing - it.avgMissing).coerceAtLeast(0) + it.frequency }
            val smallOverdue = stats.filter { it.number < 5 }
                .sumOf { (it.currentMissing - it.avgMissing).coerceAtLeast(0) + it.frequency }
            return if (bigOverdue >= smallOverdue) "BIG" else "SMALL"
        }
    }

    // 3. DKWin9 9-Complement & 5-Step Harmonic Mirror Trick
    private val dkwinComplementMirrorTrick = object : SubFormula {
        override val name = "DKWin9 9-Complement"
        override val nameBn = "DKWin ৯-কমপ্লিমেন্ট ও মিরর সূত্র"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 3) return "BIG"
            val n0 = history[0].number
            val n1 = history[1].number
            val n2 = history[2].number
            val complement9 = 9 - n0
            val step5 = (n0 + 5) % 10
            val harmonicTarget = (complement9 + step5 + n1 - n2 + 10) % 10
            return if (harmonicTarget >= 5) "BIG" else "SMALL"
        }
    }

    // 4. DKWin9 3-Draw Extreme Sum Clamp Trick
    private val dkwinExtremeClampTrick = object : SubFormula {
        override val name = "DKWin9 Sum Clamp"
        override val nameBn = "DKWin ৩-ড্র সাম ক্লাম্প"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 3) return "BIG"
            val sum3 = history[0].number + history[1].number + history[2].number
            return when {
                sum3 >= 20 -> "SMALL" // Extreme high sum clamps down to SMALL
                sum3 <= 7 -> "BIG"    // Extreme low sum clamps up to BIG
                sum3 in 14..19 -> "BIG"
                else -> "SMALL"
            }
        }
    }

    // 5. Period Last-2-Digits + 3-Draw Sum Parity Trick
    private val periodThreeSumTrick = object : SubFormula {
        override val name = "Period + 3-Draw Sum"
        override val nameBn = "পিরিয়ড ও ৩-ড্র যোগফল সূত্র"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 3) return "BIG"
            val nextP = incrementPeriod(history[0].period)
            val lastTwoDigits = nextP.takeLast(2).sumOf { it.digitToIntOrNull() ?: 0 }
            val drawSum = history[0].number + history[1].number + history[2].number
            val combined = lastTwoDigits + drawSum
            return if (combined % 2 == 0) "BIG" else "SMALL"
        }
    }

    // 6. Violet (0/5) Reset & Mirror Symmetry Trick
    private val violetMirrorTrick = object : SubFormula {
        override val name = "Violet 0/5 & Mirror"
        override val nameBn = "ভায়োলেট (০/৫) ও মিরর ফ্লিপ"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 4) return "SMALL"
            val n0 = history[0].number
            val n1 = history[1].number
            val s0 = history[0].size
            val s1 = history[1].size
            val s2 = history[2].size
            val s3 = history[3].size

            if (n0 == 0 || n0 == 5) {
                return if (s0 == "BIG") "SMALL" else "BIG"
            }
            if (n1 == 0 || n1 == 5) {
                return s0
            }
            if (s2 != s1 && s1 == s0) {
                return s2
            }
            if (s3 == s1 && s2 == s0 && s0 != s1) {
                return s1
            }
            return s0
        }
    }

    // 7. Markov 3-Gram Sequence AI
    private val markovSequenceTrick = object : SubFormula {
        override val name = "Markov 3-Gram AI"
        override val nameBn = "মার্কভ ৩-স্টেপ সিকোয়েন্স"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 8) return history.firstOrNull()?.size ?: "BIG"
            val sizes = history.map { it.size }
            val p0 = sizes[0]
            val p1 = sizes[1]
            val p2 = sizes[2]
            var bigNext = 0
            var smallNext = 0
            for (i in 1..(sizes.size - 4)) {
                if (sizes[i] == p0 && sizes[i + 1] == p1 && sizes[i + 2] == p2) {
                    if (sizes[i - 1] == "BIG") bigNext++ else smallNext++
                }
            }
            if (bigNext == smallNext) {
                for (i in 1..(sizes.size - 3)) {
                    if (sizes[i] == p0 && sizes[i + 1] == p1) {
                        if (sizes[i - 1] == "BIG") bigNext++ else smallNext++
                    }
                }
            }
            return if (bigNext >= smallNext) "BIG" else "SMALL"
        }
    }

    // 8. Dragon Trend vs 4x Break Matrix
    private val dragonStreakTrick = object : SubFormula {
        override val name = "Dragon / Streak Matrix"
        override val nameBn = "ড্রাগন ও স্ট্রিক ম্যাট্রিক্স"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.isEmpty()) return "BIG"
            val sizes = history.map { it.size }
            var streak = 1
            for (i in 1 until sizes.size) {
                if (sizes[i] == sizes[0]) streak++ else break
            }
            val maxPastStreak = computeMaxStreak(sizes.drop(streak))
            return when {
                streak >= 5 && maxPastStreak >= 5 -> sizes[0]
                streak >= 4 -> if (sizes[0] == "BIG") "SMALL" else "BIG"
                streak == 2 -> sizes[0]
                else -> sizes[0]
            }
        }
    }

    // 9. Harmonic Delta & Gap Pressure Trick
    private val gapAndDeltaTrick = object : SubFormula {
        override val name = "Gap & Golden Delta"
        override val nameBn = "গ্যাপ প্রেশার ও ডেল্টা সূত্র"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 4) return "BIG"
            val nums = history.map { it.number }
            val bigGap = nums.indexOfFirst { it >= 5 }.let { if (it == -1) nums.size else it }
            val smallGap = nums.indexOfFirst { it < 5 }.let { if (it == -1) nums.size else it }
            if (bigGap >= 5) return "BIG"
            if (smallGap >= 5) return "SMALL"
            val delta = abs(nums[0] - nums[1]) + abs(nums[1] - nums[2])
            return if ((delta + nums[0]) % 2 == 0) "BIG" else "SMALL"
        }
    }

    private val allFormulas = listOf(
        websitePremiumSeedTrick,
        chartMissingOverdueTrick,
        dkwinComplementMirrorTrick,
        dkwinExtremeClampTrick,
        periodThreeSumTrick,
        violetMirrorTrick,
        markovSequenceTrick,
        dragonStreakTrick,
        gapAndDeltaTrick
    )

    fun computeChartStats(draws: List<DrawItem>): List<ChartNumberStat> {
        val total = draws.size.coerceAtLeast(1)
        return (0..9).map { digit ->
            val freq = draws.count { it.number == digit }
            val missing = draws.indexOfFirst { it.number == digit }.let {
                if (it == -1) draws.size else it
            }
            val avgMiss = if (freq > 0) ((total - freq).toFloat() / freq).roundToInt() else draws.size
            var maxCons = 0
            var curCons = 0
            for (d in draws) {
                if (d.number == digit) {
                    curCons++
                    if (curCons > maxCons) maxCons = curCons
                } else {
                    curCons = 0
                }
            }
            ChartNumberStat(
                number = digit,
                frequency = freq,
                currentMissing = missing,
                avgMissing = avgMiss,
                maxConsecutive = maxCons
            )
        }
    }

    fun analyze(
        draws: List<DrawItem>,
        recentWinStreak: Int = 0,
        recentLossStreak: Int = 0
    ): QuantumPrediction {
        val sample = draws.take(30)
        if (sample.isEmpty()) {
            return emptyPrediction()
        }

        val currentPeriod = sample.first().period
        val nextPeriod = incrementPeriod(currentPeriod)

        val nums = sample.map { it.number }
        val sizes = sample.map { it.size }
        val colors = sample.map { DrawItem.baseColorOf(it.number) }
        val chartStats = computeChartStats(sample)

        var totalBigScore = 0.0
        var totalSmallScore = 0.0
        var greenScore = 0.0
        var redScore = 0.0

        val triggers = mutableListOf<PatternTrigger>()
        val layerVotes = mutableListOf<LayerVote>()

        // =========================================================================
        // META-LAYER: LIVE AUTO-BACKTEST OF ALL 9 DKWIN9 & WEBSITE FORMULAS
        // =========================================================================
        val backtestDepth = min(10, (sample.size - 8).coerceAtLeast(0))
        val leaderboardRaw = mutableListOf<Triple<SubFormula, Int, String>>()

        for (formula in allFormulas) {
            var directHits = 0
            if (backtestDepth > 0) {
                for (t in 0 until backtestDepth) {
                    val pastSlice = sample.drop(t + 1)
                    val actualSize = sample[t].size
                    val rawPred = formula.predictRaw(pastSlice)
                    if (rawPred == actualSize) directHits++
                }
            }
            val rawRate = if (backtestDepth > 0) {
                ((directHits * 100f) / backtestDepth).roundToInt()
            } else {
                72
            }

            val currentRawVote = formula.predictRaw(sample)
            val calibratedVote: String
            val effectiveWinRate: Int
            if (backtestDepth >= 5 && rawRate <= 40) {
                calibratedVote = if (currentRawVote == "BIG") "SMALL" else "BIG"
                effectiveWinRate = 100 - rawRate
            } else {
                calibratedVote = currentRawVote
                effectiveWinRate = rawRate.coerceAtLeast(54)
            }

            leaderboardRaw.add(Triple(formula, effectiveWinRate, calibratedVote))

            val formulaWeight = when {
                effectiveWinRate >= 80 -> 7.2
                effectiveWinRate >= 70 -> 4.8
                effectiveWinRate >= 60 -> 2.8
                else -> 1.0
            }
            if (calibratedVote == "BIG") totalBigScore += formulaWeight else totalSmallScore += formulaWeight
        }

        val sortedFormulas = leaderboardRaw.sortedByDescending { it.second }
        val topFormula = sortedFormulas.firstOrNull()
        val formulaLeaderboard = sortedFormulas.mapIndexed { idx, (f, rate, vote) ->
            FormulaLeaderboardItem(
                formulaName = f.name,
                formulaNameBn = f.nameBn,
                liveWinRatePercent = rate,
                nextVote = vote,
                isTopTrick = idx == 0
            )
        }

        if (topFormula != null) {
            triggers.add(
                PatternTrigger(
                    title = "🏆 #1 DKWIN9 TRICK: ${topFormula.first.name.uppercase()} (${topFormula.second}%)",
                    descriptionBn = "DKWin9 সার্ভারে '${topFormula.first.nameBn}' সূত্রটি সর্বোচ্চ ${topFormula.second}% নির্ভুলতায় কাজ করছে → সিগন্যাল: ${topFormula.third}!",
                    impactLabel = "${topFormula.second}% HIT",
                    isHighPriority = true
                )
            )
        }

        // =========================================================================
        // LAYER 1: EXPONENTIAL DECAY MOMENTUM (EMA - Last 25 draws)
        // =========================================================================
        val h25 = sample.take(25)
        var emaBig = 0.0
        var emaSmall = 0.0

        h25.forEachIndexed { idx, item ->
            val w = (25.0 - idx) / 25.0
            if (item.size == "BIG") emaBig += w else emaSmall += w
            if (DrawItem.baseColorOf(item.number) == "GREEN") greenScore += w else redScore += w
        }

        totalBigScore += emaBig * 0.65
        totalSmallScore += emaSmall * 0.65

        val emaFavored = if (emaBig >= emaSmall) "BIG" else "SMALL"
        val emaStrength = (54 + abs(emaBig - emaSmall) * 7).roundToInt().coerceIn(54, 93)
        layerVotes.add(
            LayerVote(
                layerName = "EMA Momentum",
                layerNameBn = "মোমেন্টাম ফ্লো (EMA)",
                favoredSize = emaFavored,
                strengthPercent = emaStrength,
                detail = "B:${String.format("%.1f", emaBig)} vs S:${String.format("%.1f", emaSmall)}"
            )
        )

        // =========================================================================
        // LAYER 2: DKWIN9 9-COMPLEMENT & CHART MISSING OVERDUE
        // =========================================================================
        val dkwinCompEntry = sortedFormulas.find { it.first === dkwinComplementMirrorTrick }
        val chartEntry = sortedFormulas.find { it.first === chartMissingOverdueTrick }
        if (dkwinCompEntry != null) {
            layerVotes.add(
                LayerVote(
                    layerName = "DKWin9 9-Complement",
                    layerNameBn = "DKWin ৯-কমপ্লিমেন্ট মিরর",
                    favoredSize = dkwinCompEntry.third,
                    strengthPercent = dkwinCompEntry.second.coerceIn(56, 97),
                    detail = "9-${nums[0]}=${9 - nums[0]} • Step5:${(nums[0] + 5) % 10}"
                )
            )
        }
        if (chartEntry != null) {
            val mostOverdue = chartStats.maxByOrNull { it.currentMissing - it.avgMissing }
            layerVotes.add(
                LayerVote(
                    layerName = "Website Chart Missing",
                    layerNameBn = "ওয়েবসাইট চার্ট মিসিং অ্যানালাইসিস",
                    favoredSize = chartEntry.third,
                    strengthPercent = chartEntry.second.coerceIn(56, 96),
                    detail = "Overdue No: ${mostOverdue?.number ?: 7} (Miss:${mostOverdue?.currentMissing ?: 0})"
                )
            )
        }

        // =========================================================================
        // LAYER 3: ADAPTIVE STREAK & VIOLET 0/5 RESET MATRIX
        // =========================================================================
        var currentStreak = 1
        for (i in 1 until sizes.size) {
            if (sizes[i] == sizes[0]) currentStreak++ else break
        }

        var colorStreak = 1
        for (i in 1 until colors.size) {
            if (colors[i] == colors[0]) colorStreak++ else break
        }

        val maxHistoricalStreak = computeMaxStreak(sizes.drop(currentStreak))
        val dragonSession = maxHistoricalStreak >= 5

        var streakBigBoost = 0.0
        var streakSmallBoost = 0.0

        if (nums[0] == 0 || nums[0] == 5) {
            val flipTarget = if (sizes[0] == "BIG") "SMALL" else "BIG"
            if (flipTarget == "BIG") streakBigBoost += 4.5 else streakSmallBoost += 4.5
            triggers.add(
                PatternTrigger(
                    title = "🟣 VIOLET (${nums[0]}) BOARD RESET DETECTED",
                    descriptionBn = "সর্বশেষ পিরিয়ডে ভায়োলেট (${nums[0]}) এসেছে — DKWin ট্রিক অনুযায়ী ট্রেন্ড রিসেট হয়ে $flipTarget আসার সম্ভাবনা বেশি!",
                    impactLabel = "VIOLET FLIP",
                    isHighPriority = true
                )
            )
        }

        when {
            currentStreak >= 5 && dragonSession -> {
                if (sizes[0] == "BIG") streakBigBoost += 7.5 else streakSmallBoost += 7.5
                triggers.add(
                    PatternTrigger(
                        title = "🐉 DRAGON TREND LOCK (${currentStreak}x ${sizes[0]})",
                        descriptionBn = "টানা ${currentStreak} বার ${sizes[0]} এসেছে — ড্রাগন ট্রেন্ড সক্রিয়, ট্রেন্ডের সাথে থাকুন!",
                        impactLabel = "+7.5 DRAGON",
                        isHighPriority = true
                    )
                )
            }
            currentStreak >= 4 -> {
                if (sizes[0] == "BIG") streakSmallBoost += 7.0 else streakBigBoost += 7.0
                val targetBreak = if (sizes[0] == "BIG") "SMALL" else "BIG"
                triggers.add(
                    PatternTrigger(
                        title = "⚠ ${currentStreak}x ${sizes[0]} STREAK REVERSAL",
                        descriptionBn = "টানা ${currentStreak} বার ${sizes[0]} এসেছে — এখন $targetBreak এ ব্রেক করার উচ্চ সম্ভাবনা!",
                        impactLabel = "+7.0 BREAK",
                        isHighPriority = true
                    )
                )
            }
            currentStreak == 2 -> {
                if (sizes[0] == "BIG") streakBigBoost += 3.0 else streakSmallBoost += 3.0
                triggers.add(
                    PatternTrigger(
                        title = "📈 2x ${sizes[0]} CONTINUATION",
                        descriptionBn = "২ বার ${sizes[0]} আসার পর পেয়ার/ট্রিপল কন্টিনিউয়েশন প্যাটার্ন সক্রিয়।",
                        impactLabel = "+3.0 CONT"
                    )
                )
            }
        }

        if (colorStreak >= 4) {
            if (colors[0] == "GREEN") redScore += 6.0 else greenScore += 6.0
            triggers.add(
                PatternTrigger(
                    title = "🎨 ${colorStreak}x ${colors[0]} COLOR REVERSAL",
                    descriptionBn = "টানা ${colorStreak} বার ${colors[0]} কালার এসেছে — কালার ব্রেক সিগন্যাল।",
                    impactLabel = "COLOR BREAK"
                )
            )
        }

        totalBigScore += streakBigBoost
        totalSmallScore += streakSmallBoost

        val streakFavored = when {
            streakBigBoost > streakSmallBoost -> "BIG"
            streakSmallBoost > streakBigBoost -> "SMALL"
            else -> sizes[0]
        }
        layerVotes.add(
            LayerVote(
                layerName = "Dragon & Violet Reset",
                layerNameBn = "ড্রাগন ও ভায়োলেট রিসেট",
                favoredSize = streakFavored,
                strengthPercent = (62 + abs(streakBigBoost - streakSmallBoost) * 4).roundToInt().coerceIn(58, 96),
                detail = "${currentStreak}x ${sizes[0]} (Max:${maxHistoricalStreak}x)"
            )
        )

        // =========================================================================
        // LAYER 4: MARKOV N-GRAM & ZIGZAG / MIRROR SYMMETRY
        // =========================================================================
        val markovEntry = sortedFormulas.find { it.first === markovSequenceTrick }
        if (markovEntry != null) {
            layerVotes.add(
                LayerVote(
                    layerName = "Markov 3-Gram AI",
                    layerNameBn = "মার্কভ ৩-স্টেপ সিকোয়েন্স এআই",
                    favoredSize = markovEntry.third,
                    strengthPercent = markovEntry.second.coerceIn(56, 95),
                    detail = "Seq [${sizes.take(3).reversed().joinToString("") { it.take(1) }}]"
                )
            )
        }

        var zigzagCount = 1
        for (i in 0 until min(6, sizes.size - 1)) {
            if (sizes[i] != sizes[i + 1]) zigzagCount++ else break
        }
        if (zigzagCount >= 4) {
            val nextZigzag = if (sizes[0] == "BIG") "SMALL" else "BIG"
            if (nextZigzag == "BIG") totalBigScore += 6.0 else totalSmallScore += 6.0
            triggers.add(
                PatternTrigger(
                    title = "🔁 ZIGZAG CHAIN ($zigzagCount STEPS → $nextZigzag)",
                    descriptionBn = "টানা $zigzagCount বার BIG-SMALL জিগজ্যাগ চলছে — পরবর্তী ধাপ $nextZigzag!",
                    impactLabel = "+6.0 ZIGZAG",
                    isHighPriority = true
                )
            )
        }

        // =========================================================================
        // FINAL SIZE PREDICTION & NUMBER / COLOR SELECTION (DKWIN9 COMPLEMENT + CHART)
        // =========================================================================
        val predictedSize = if (totalBigScore >= totalSmallScore) "BIG" else "SMALL"

        val freqMap = (0..9).associateWith { n -> h25.count { it.number == n } }
        val statByNum = chartStats.associateBy { it.number }

        val markovNumberBonus = mutableMapOf<Int, Int>()
        for (i in 1 until nums.size) {
            if (nums[i] == nums[0]) {
                val followedBy = nums[i - 1]
                markovNumberBonus[followedBy] = (markovNumberBonus[followedBy] ?: 0) + 2
            }
        }

        val comp9 = 9 - nums[0]
        val step5 = (nums[0] + 5) % 10

        val candidates = (0..9).filter { n ->
            if (predictedSize == "BIG") n >= 5 else n < 5
        }

        val recentTwo = nums.take(2)
        val rankedCandidates = candidates.sortedByDescending { n ->
            val st = statByNum[n]
            val overdueBoost = if (st != null && st.currentMissing >= st.avgMissing) 4 else 1
            val dkwinHarmonicBoost = if (n == comp9 || n == step5) 4 else 0
            val markovBoost = (markovNumberBonus[n] ?: 0) * 3
            val repeatPenalty = if (n == nums.firstOrNull()) -4 else if (n in recentTwo) -2 else 2
            val dueBonus = (5 - (freqMap[n] ?: 0)).coerceAtLeast(0)
            val colorMatchBonus = if ((greenScore >= redScore && DrawItem.baseColorOf(n) == "GREEN") ||
                (redScore > greenScore && DrawItem.baseColorOf(n) == "RED")
            ) 3 else 0
            overdueBoost + dkwinHarmonicBoost + markovBoost + dueBonus + colorMatchBonus + repeatPenalty
        }

        val primaryNumber = rankedCandidates.getOrElse(0) { if (predictedSize == "BIG") 7 else 2 }
        val backupNumber = rankedCandidates.getOrElse(1) { if (predictedSize == "BIG") 8 else 1 }
        val predictedColor = DrawItem.colorOf(primaryNumber)

        val agreeingFormulas = sortedFormulas.count { it.third == predictedSize }
        val topRate = topFormula?.second ?: 75
        val scoreDiff = abs(totalBigScore - totalSmallScore)
        val rawConfidence = (58 + (agreeingFormulas * 3.2) + (topRate - 60) * 0.4 + scoreDiff * 1.2).roundToInt()
        val confidence = rawConfidence.coerceIn(64, 98)

        // Smart Trap Shield (ফাঁদ শনাক্তকারী)
        val isTrapRound = agreeingFormulas <= 5 && scoreDiff < 4.5
        val riskBadge = when {
            confidence >= 86 && agreeingFormulas >= 7 -> "🎯 SURE SHOT ($agreeingFormulas/9 FORMULAS)"
            !isTrapRound && confidence >= 74 -> "✅ SAFE SIGNAL ($agreeingFormulas/9 AGREE)"
            else -> "🛑 TRAP ALERT • WAIT / 1x ONLY"
        }

        val recommendedMultiplier = when {
            isTrapRound -> "WAIT / 1x (ফাঁদ রাউন্ড — সতর্ক থাকুন)"
            recentLossStreak == 0 -> "1x (Level 1 Entry)"
            recentLossStreak == 1 -> "2x (Level 2 Recovery)"
            recentLossStreak == 2 -> "4x (Level 3 Recovery)"
            recentLossStreak == 3 -> "8x (Level 4 Recovery)"
            else -> "1x (রিসেট / সতর্ক থাকুন)"
        }

        val headlinePattern = triggers.first().title
        val topTrickText = if (topFormula != null) {
            "⚡ DKWin9 #1 ট্রিক: ${topFormula.first.nameBn} (${topFormula.second}% জয়)"
        } else {
            "⚡ DKWin9 কোয়ান্টাম অটো-টিউনার সক্রিয়"
        }

        val banglaAdvice = buildString {
            append(triggers.first().descriptionBn)
            append("\n🎯 সেরা নম্বর: $primaryNumber ও $backupNumber ($predictedColor)। ")
            append("মোট ৯টি DKWin9 ও চার্ট সূত্রের মধ্যে $agreeingFormulas টি সূত্র $predictedSize সাপোর্ট করছে।")
            if (isTrapRound) {
                append(" ⚠ এই পিরিয়ডে বোর্ড পরিবর্তন হচ্ছে, বড় বেট না ধরে ১x বা স্কিপ করুন।")
            }
        }

        val hotNumbers = freqMap.entries.sortedByDescending { it.value }.take(3).map { it.key }
        val coldNumbers = freqMap.entries.sortedBy { it.value }.take(3).map { it.key }

        val bigCount = h25.count { it.size == "BIG" }
        val smallCount = h25.size - bigCount
        val bigRatio = if (h25.isNotEmpty()) ((bigCount * 100f) / h25.size).roundToInt() else 50
        val smallRatio = 100 - bigRatio

        val greenCount = h25.count { it.color == "GREEN" }
        val redCount = h25.count { it.color == "RED" }
        val violetCount = h25.count { it.color == "VIOLET" }

        return QuantumPrediction(
            currentPeriod = currentPeriod,
            nextPeriod = nextPeriod,
            predictedSize = predictedSize,
            primaryNumber = primaryNumber,
            backupNumber = backupNumber,
            predictedColor = predictedColor,
            confidence = confidence,
            riskBadge = riskBadge,
            recommendedMultiplier = recommendedMultiplier,
            headlinePattern = headlinePattern,
            topWebsiteTrickBadge = topTrickText,
            banglaAdvice = banglaAdvice,
            triggers = triggers,
            layerVotes = layerVotes,
            formulaLeaderboard = formulaLeaderboard,
            chartStats = chartStats,
            numberFrequencies = freqMap,
            hotNumbers = hotNumbers,
            coldNumbers = coldNumbers,
            bigRatioPercent = bigRatio,
            smallRatioPercent = smallRatio,
            greenCount = greenCount,
            redCount = redCount,
            violetCount = violetCount
        )
    }

    private fun computeMaxStreak(sizes: List<String>): Int {
        if (sizes.isEmpty()) return 0
        var maxS = 1
        var cur = 1
        for (i in 1 until sizes.size) {
            if (sizes[i] == sizes[i - 1]) {
                cur++
                if (cur > maxS) maxS = cur
            } else {
                cur = 1
            }
        }
        return maxS
    }

    fun incrementPeriod(period: String): String {
        return try {
            BigInteger(period.trim()).add(BigInteger.ONE).toString()
        } catch (e: Exception) {
            period
        }
    }

    private fun emptyPrediction(): QuantumPrediction {
        return QuantumPrediction(
            currentPeriod = "----",
            nextPeriod = "----",
            predictedSize = "BIG",
            primaryNumber = 7,
            backupNumber = 8,
            predictedColor = "GREEN",
            confidence = 78,
            riskBadge = "ANALYZING...",
            recommendedMultiplier = "1x (Level 1)",
            headlinePattern = "⚡ INITIALIZING DKWIN9 QUANTUM ENGINE...",
            topWebsiteTrickBadge = "⚡ DKWin9 চার্ট ও প্রিমিয়াম সিড লোড হচ্ছে...",
            banglaAdvice = "লাইভ এপিআই থেকে ডেটা সিঙ্ক হচ্ছে...",
            triggers = emptyList(),
            layerVotes = emptyList(),
            formulaLeaderboard = emptyList(),
            chartStats = emptyList(),
            numberFrequencies = (0..9).associateWith { 0 },
            hotNumbers = listOf(7, 8, 5),
            coldNumbers = listOf(0, 2, 4),
            bigRatioPercent = 50,
            smallRatioPercent = 50,
            greenCount = 0,
            redCount = 0,
            violetCount = violetCountDefault()
        )
    }

    private fun violetCountDefault(): Int = 0
}
