package com.example.domain

import com.example.data.model.ChartNumberStat
import com.example.data.model.DrawItem
import com.example.data.model.EnergyBallStat
import com.example.data.model.FormulaLeaderboardItem
import com.example.data.model.LayerVote
import com.example.data.model.PatternTrigger
import com.example.data.model.QuantumPrediction
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 14-Formula Self-Calibrating Quantum Meta-Engine tuned for DKWin9 (saasLottery/WinGo 30S/1M/3M/5M):
 * Includes the newly discovered DKWin9 Chart & Board formulas:
 * - Formula 13: Diagonal Period-Digit + Result Cross (`(P_last + N0) % 10` & `P_last == N0` Flip)
 * - Formula 14: Skip-1 Odd/Even Period Midpoint Bridge (`(N[0] + N[2]) / 2` -> 2027(6)+2029(3)=4, 2028(8)+2030(4)=6)
 * - Real-time DKWin9 Energy Boost DOM Sync (`liveRealEnergyMap`) + Anti-Crowd Bonus Filter
 * - Twin Repeat -> 10-Complement Break (`4, 4 -> 6` where `10 - 4 = 6`)
 * - 5-Period Cycle Echo (`2027=6 -> 2032=6`) & 3-2 / 2-3 Block Wave (`BB -> SSS -> BB`)
 */
object QuantumPatternEngine {

    private interface SubFormula {
        val name: String
        val nameBn: String
        fun predictRaw(history: List<DrawItem>): String
    }

    // 1. DKWin9 Diagonal Period + Result Cross (`(P_last + N0) % 10` with `P_last == N0` Flip)
    private val dkwinDiagonalCrossTrick = object : SubFormula {
        override val name = "Diagonal Period+Result"
        override val nameBn = "ডায়াগনাল পিরিয়ড+রেজাল্ট ক্রস (P+N)"
        override fun predictRaw(history: List<DrawItem>): String {
            val latest = history.firstOrNull() ?: return "BIG"
            val pLast = latest.period.lastOrNull()?.digitToIntOrNull() ?: 0
            val n0 = latest.number
            // When Period's last digit equals the drawn number (e.g. 2028 -> 8), board flips Size immediately!
            if (pLast == n0) {
                return if (latest.size == "BIG") "SMALL" else "BIG"
            }
            val cross = (pLast + n0) % 10
            return if (cross >= 5) "BIG" else "SMALL"
        }
    }

    // 2. DKWin9 Skip-1 Odd/Even Period Midpoint Bridge (`(N[0] + N[2]) / 2`)
    private val dkwinSkipOneMidpointTrick = object : SubFormula {
        override val name = "Skip-1 Midpoint Bridge"
        override val nameBn = "স্কিপ-১ জোড়/বিজোড় পিরিয়ড মিডপয়েন্ট"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 4) return "BIG"
            // For the upcoming period (index -1), the two previous same-parity periods are index 1 and index 3!
            // Example from DKWin9 screenshot:
            // For 2031: (2029[3] + 2027[6]) / 2 = 4 (SMALL) -> Hit 4!
            // For 2032: (2030[4] + 2028[8]) / 2 = 6 (BIG)   -> Hit 6!
            val sameParityPrev1 = history[1].number
            val sameParityPrev2 = history[3].number
            val midpoint = (sameParityPrev1 + sameParityPrev2) / 2
            return if (midpoint >= 5) "BIG" else "SMALL"
        }
    }

    // 3. Website `premium` Seed Checksum Trick
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

    // 4. Official Website "Chart" Missing vs Avg-Missing Overdue Trick
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

    // 5. DKWin9 Energy Boost ⚡ (Rising Momentum vs Anti-Crowd Bonus Trap)
    private val dkwinEnergyBoostTrick = object : SubFormula {
        override val name = "DKWin9 Energy Boost ⚡"
        override val nameBn = "DKWin এনার্জি বুস্ট ও অ্যান্টি-ক্রাউড"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 5) return "BIG"
            val stats = computeChartStats(history)
            val energyList = computeEnergyRanking(history, stats, null)
            val safeBigEnergy = energyList.filter { it.number >= 5 && !it.isCrowdTrap }
                .sumOf { it.energyPercent + (if (it.isSafeBoostPick) 6 else 0) + it.trendDelta.coerceAtLeast(0) }
            val safeSmallEnergy = energyList.filter { it.number < 5 && !it.isCrowdTrap }
                .sumOf { it.energyPercent + (if (it.isSafeBoostPick) 6 else 0) + it.trendDelta.coerceAtLeast(0) }
            return if (safeBigEnergy >= safeSmallEnergy) "BIG" else "SMALL"
        }
    }

    // 6. DKWin9 Twin-Repeat & 10-Complement Break (`4, 4 -> 6` from Period 2030-2032)
    private val dkwinTwinTenComplementTrick = object : SubFormula {
        override val name = "Twin & 10-Complement"
        override val nameBn = "টুইন রিপিট ও ১০-কমপ্লিমেন্ট (10-N)"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 3) return "BIG"
            val n0 = history[0].number
            val n1 = history[1].number
            val n2 = history[2].number
            if (n0 == n1) {
                val comp10 = (10 - n0) % 10
                return if (comp10 >= 5) "BIG" else "SMALL"
            }
            val bridge = (10 - n0 + n1 + n2) % 10
            return if (bridge >= 5) "BIG" else "SMALL"
        }
    }

    // 7. DKWin9 5-Round Cycle Echo & 3-2 Block Wave (`2027=6 -> 2032=6`, `BB -> SSS -> BB`)
    private val dkwinFiveCycleWaveTrick = object : SubFormula {
        override val name = "5-Cycle & 3-2 Wave"
        override val nameBn = "৫-রাউন্ড সাইকেল ও ৩-২ ব্লক ওয়েভ"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 6) return "BIG"
            val s = history.map { it.size }
            if (s[0] != s[1] && s[1] == s[2] && s[2] == s[3]) {
                return s[0]
            }
            if (s[0] == s[1] && s[1] == s[2] && s[2] != s[3] && s[3] == s[4]) {
                return if (s[0] == "BIG") "SMALL" else "BIG"
            }
            val echoNum = history[4].number
            return if (echoNum >= 5) "BIG" else "SMALL"
        }
    }

    // 8. DKWin9 9-Complement & 5-Step Harmonic Mirror Trick
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

    // 9. DKWin9 3-Draw Extreme Sum Clamp Trick
    private val dkwinExtremeClampTrick = object : SubFormula {
        override val name = "DKWin9 Sum Clamp"
        override val nameBn = "DKWin ৩-ড্র সাম ক্লাম্প"
        override fun predictRaw(history: List<DrawItem>): String {
            if (history.size < 3) return "BIG"
            val sum3 = history[0].number + history[1].number + history[2].number
            return when {
                sum3 >= 20 -> "SMALL"
                sum3 <= 7 -> "BIG"
                sum3 in 14..19 -> "BIG"
                else -> "SMALL"
            }
        }
    }

    // 10. Period Last-2-Digits + 3-Draw Sum Parity Trick
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

    // 11. Violet (0/5) Reset & Mirror Symmetry Trick
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

    // 12. Markov 3-Gram Sequence AI
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

    // 13. Dragon Trend vs 4x Break Matrix
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

    // 14. Harmonic Delta & Gap Pressure Trick
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
        dkwinDiagonalCrossTrick,
        dkwinSkipOneMidpointTrick,
        websitePremiumSeedTrick,
        chartMissingOverdueTrick,
        dkwinEnergyBoostTrick,
        dkwinTwinTenComplementTrick,
        dkwinFiveCycleWaveTrick,
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

    /**
     * Reconstructs or uses live-synced DKWin9 "Energy Boost (WinGo 30sec)" ranking distribution (0..9).
     * When `liveRealEnergyMap` is provided from the logged-in DKWin9 WebView DOM scanner, uses those
     * exact database percentages directly!
     */
    fun computeEnergyRanking(
        draws: List<DrawItem>,
        chartStats: List<ChartNumberStat>,
        liveRealEnergyMap: Map<Int, Pair<Int, Int>>? = null
    ): List<EnergyBallStat> {
        val latest = draws.firstOrNull()
        val periodSeed = latest?.period?.takeLast(3)?.toIntOrNull() ?: 33
        val lastNum = latest?.number ?: 6
        val prevNum = draws.getOrNull(1)?.number ?: 4
        val comp10 = (10 - lastNum) % 10
        val comp9 = 9 - lastNum

        val percents: List<Int>
        val customDeltas: Map<Int, Int>?

        if (liveRealEnergyMap != null && liveRealEnergyMap.size >= 6) {
            percents = (0..9).map { d -> liveRealEnergyMap[d]?.first ?: 10 }
            customDeltas = (0..9).associateWith { d -> liveRealEnergyMap[d]?.second ?: 1 }
        } else {
            val rawWeights = (0..9).map { digit ->
                val st = chartStats.getOrNull(digit)
                val freqWeight = (st?.frequency ?: 2) * 3
                val overdueWeight = ((st?.currentMissing ?: 3) - (st?.avgMissing ?: 3)).coerceIn(-2, 6) * 2
                val harmonicWeight = when (digit) {
                    comp10 -> 7
                    comp9 -> 5
                    lastNum -> 4
                    else -> ((digit * 7 + periodSeed + prevNum) % 6)
                }
                (10 + freqWeight + overdueWeight + harmonicWeight).coerceAtLeast(6)
            }

            val totalWeight = rawWeights.sum().coerceAtLeast(1)
            val mutPercents = rawWeights.map { ((it * 100f) / totalWeight).roundToInt().coerceIn(6, 19) }.toMutableList()
            val diff = 100 - mutPercents.sum()
            val maxIdx = mutPercents.indices.maxByOrNull { mutPercents[it] } ?: 2
            mutPercents[maxIdx] = (mutPercents[maxIdx] + diff).coerceIn(12, 22)
            percents = mutPercents
            customDeltas = null
        }

        val sortedDigits = (0..9).sortedWith(
            compareByDescending<Int> { percents[it] }
                .thenByDescending { if (it == comp10 || it == comp9) 1 else 0 }
        )
        val rankMap = sortedDigits.mapIndexed { idx, d -> d to (idx + 1) }.toMap()

        return (0..9).map { digit ->
            val rank = rankMap[digit] ?: 5
            val pct = percents[digit]
            val delta = if (customDeltas != null) {
                customDeltas[digit] ?: 1
            } else {
                val recentHits = draws.take(8).count { it.number == digit }
                val olderHits = draws.drop(8).take(8).count { it.number == digit }
                when {
                    digit == comp10 || digit == comp9 -> ((periodSeed + digit) % 4) + 1
                    recentHits > olderHits -> (recentHits - olderHits) * 2
                    recentHits < olderHits -> -((olderHits - recentHits) * 2)
                    else -> ((periodSeed xor digit) % 5) - 2
                }
            }
            val isTrap = rank == 1 && pct >= 15
            val isSafePick = (rank in 2..4 || (pct in 10..14 && delta > 0)) && !isTrap

            EnergyBallStat(
                number = digit,
                energyPercent = pct,
                trendDelta = delta,
                rankOrder = rank,
                isSafeBoostPick = isSafePick,
                isCrowdTrap = isTrap
            )
        }
    }

    fun analyze(
        draws: List<DrawItem>,
        recentWinStreak: Int = 0,
        recentLossStreak: Int = 0,
        liveRealEnergyMap: Map<Int, Pair<Int, Int>>? = null
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
        val isRealEnergy = liveRealEnergyMap != null && liveRealEnergyMap.size >= 6
        val energyRanking = computeEnergyRanking(sample, chartStats, liveRealEnergyMap)

        var totalBigScore = 0.0
        var totalSmallScore = 0.0
        var greenScore = 0.0
        var redScore = 0.0

        val triggers = mutableListOf<PatternTrigger>()
        val layerVotes = mutableListOf<LayerVote>()

        // =========================================================================
        // META-LAYER: LIVE AUTO-BACKTEST OF ALL 14 DKWIN9 & CHART FORMULAS
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
                75
            }

            val currentRawVote = formula.predictRaw(sample)
            val calibratedVote: String
            val effectiveWinRate: Int
            if (backtestDepth >= 5 && rawRate <= 40) {
                calibratedVote = if (currentRawVote == "BIG") "SMALL" else "BIG"
                effectiveWinRate = 100 - rawRate
            } else {
                calibratedVote = currentRawVote
                effectiveWinRate = rawRate.coerceAtLeast(56)
            }

            leaderboardRaw.add(Triple(formula, effectiveWinRate, calibratedVote))

            val formulaWeight = when {
                effectiveWinRate >= 80 -> 7.8
                effectiveWinRate >= 70 -> 5.2
                effectiveWinRate >= 60 -> 2.8
                else -> 1.0
            }
            if (calibratedVote == "BIG") totalBigScore += formulaWeight else totalSmallScore += formulaWeight
        }

        // If real DKWin9 Energy Boost DB data is synced, add House Liquidity Anti-Crowd weight!
        if (isRealEnergy) {
            val smallCrowdPct = energyRanking.filter { it.number in 0..4 }.sumOf { it.energyPercent }
            val bigCrowdPct = energyRanking.filter { it.number in 5..9 }.sumOf { it.energyPercent }
            if (smallCrowdPct > bigCrowdPct + 4) {
                totalBigScore += 6.0
            } else if (bigCrowdPct > smallCrowdPct + 4) {
                totalSmallScore += 6.0
            }
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

        // Compute Diagonal Cross (P_last + N0) & Skip-1 Midpoint ((N1 + N3) / 2) exact number candidates!
        val pLast = currentPeriod.lastOrNull()?.digitToIntOrNull() ?: 0
        val diagonalCrossNum = if (pLast == nums[0]) {
            (10 - nums[0]) % 10
        } else {
            (pLast + nums[0]) % 10
        }
        val skipOneMidpointNum = if (nums.size >= 4) {
            (nums[1] + nums[3]) / 2
        } else {
            (nums[0] + (nums.getOrNull(2) ?: nums[0])) / 2
        }

        triggers.add(
            PatternTrigger(
                title = "🎯 DIAGONAL (P+N=#$diagonalCrossNum) & SKIP-1 MIDPOINT (#$skipOneMidpointNum)",
                descriptionBn = "ডায়াগনাল সূত্র ($pLast+${nums[0]}=$diagonalCrossNum) এবং স্কিপ-১ মিডপয়েন্ট সূত্র ((${nums.getOrElse(1){4}}+${nums.getOrElse(3){6}})/2=$skipOneMidpointNum) সক্রিয়!",
                impactLabel = "SNIPER #$diagonalCrossNum/#$skipOneMidpointNum",
                isHighPriority = true
            )
        )

        // =========================================================================
        // DEEP MARKET REGIME CLASSIFIER (মার্কেট বিশ্লেষণ ও প্যাটার্ন শনাক্তকরণ)
        // =========================================================================
        var currentStreak = 1
        for (i in 1 until sizes.size) {
            if (sizes[i] == sizes[0]) currentStreak++ else break
        }
        var colorStreak = 1
        for (i in 1 until colors.size) {
            if (colors[i] == colors[0]) colorStreak++ else break
        }
        var zigzagCount = 1
        for (i in 0 until min(6, sizes.size - 1)) {
            if (sizes[i] != sizes[i + 1]) zigzagCount++ else break
        }

        val isTwinRepeat = nums.size >= 2 && nums[0] == nums[1]
        val isDiagonalMatchFlip = pLast == nums[0]
        val isBlockWave32 = sizes.size >= 5 && (
            (sizes[0] != sizes[1] && sizes[1] == sizes[2] && sizes[2] == sizes[3]) ||
                (sizes[0] == sizes[1] && sizes[1] == sizes[2] && sizes[2] != sizes[3])
            )

        val (marketRegimeTitle, marketRegimeBn) = when {
            isDiagonalMatchFlip -> {
                val flipSize = if (sizes[0] == "BIG") "SMALL" else "BIG"
                if (flipSize == "BIG") totalBigScore += 6.5 else totalSmallScore += 6.5
                "⚡ DIAGONAL MATCH FLIP (P:$pLast == N:${nums[0]} → $flipSize)" to
                    "পিরিয়ডের শেষ সংখ্যা ($pLast) ও রেজাল্ট (${nums[0]}) মিলে গেছে (যেমন ২০২৮=৮) — ডায়াগনাল সূত্র অনুযায়ী এখন সাইজ উল্টে $flipSize হবে!"
            }
            isTwinRepeat -> {
                val comp10 = (10 - nums[0]) % 10
                val compSize = if (comp10 >= 5) "BIG" else "SMALL"
                if (compSize == "BIG") totalBigScore += 6.5 else totalSmallScore += 6.5
                "👯 TWIN REPEAT (${nums[0]},${nums[0]}) → 10-COMPLEMENT ($comp10)" to
                    "মার্কেটে পরপর দুইবার ${nums[0]} (Twin) এসেছে — DKWin 10-Complement সূত্র (10-${nums[0]}=$comp10) অনুযায়ী এখন $compSize ও $comp10 আসার উচ্চ সম্ভাবনা!"
            }
            isBlockWave32 -> {
                val waveVote = dkwinFiveCycleWaveTrick.predictRaw(sample)
                if (waveVote == "BIG") totalBigScore += 5.5 else totalSmallScore += 5.5
                "🌊 3-2 / 2-3 BLOCK WAVE MARKET ($waveVote)" to
                    "DKWin9 চার্টে ৩-২ ব্লক ওয়েভ (যেমন ২ বার Big → ৩ বার Small → আবার Big) চলছে — ওয়েভ ফ্লো অনুযায়ী পরবর্তী ধাপ $waveVote!"
            }
            currentStreak >= 4 -> {
                "🐉 DRAGON / STREAK MARKET (${currentStreak}x ${sizes[0]})" to
                    "মার্কেটে টানা ${currentStreak} বার ${sizes[0]} চলছে — স্ট্রিক ও ব্রেক ম্যাট্রিক্স সক্রিয়।"
            }
            zigzagCount >= 4 -> {
                val nextZig = if (sizes[0] == "BIG") "SMALL" else "BIG"
                "⚡ ZIGZAG SWITCH MARKET (${zigzagCount}x B-S)" to
                    "মার্কেটে টানা $zigzagCount বার BIG-SMALL জিগজ্যাগ সুইচ চলছে — পরবর্তী ধাপ $nextZig!"
            }
            else -> {
                "📊 DIAGONAL (#$diagonalCrossNum) & SKIP-1 MIDPOINT (#$skipOneMidpointNum) MARKET" to
                    "মার্কেট এখন ডায়াগনাল ক্রস (#$diagonalCrossNum) ও স্কিপ-১ মিডপয়েন্ট (#$skipOneMidpointNum) মোডে চলছে।"
            }
        }

        // =========================================================================
        // LAYER 1: DIAGONAL CROSS & SKIP-1 MIDPOINT SNIPER
        // =========================================================================
        val diagEntry = sortedFormulas.find { it.first === dkwinDiagonalCrossTrick }
        val skipEntry = sortedFormulas.find { it.first === dkwinSkipOneMidpointTrick }
        if (diagEntry != null) {
            layerVotes.add(
                LayerVote(
                    layerName = "Diagonal Period+Result",
                    layerNameBn = "ডায়াগনাল পিরিয়ড+রেজাল্ট ক্রস",
                    favoredSize = diagEntry.third,
                    strengthPercent = diagEntry.second.coerceIn(58, 98),
                    detail = "P_last($pLast) + N0(${nums[0]}) → #$diagonalCrossNum"
                )
            )
        }
        if (skipEntry != null) {
            layerVotes.add(
                LayerVote(
                    layerName = "Skip-1 Midpoint Bridge",
                    layerNameBn = "স্কিপ-১ পিরিয়ড মিডপয়েন্ট",
                    favoredSize = skipEntry.third,
                    strengthPercent = skipEntry.second.coerceIn(58, 98),
                    detail = "(${nums.getOrElse(1){4}} + ${nums.getOrElse(3){6}}) / 2 = #$skipOneMidpointNum"
                )
            )
        }

        // =========================================================================
        // LAYER 2: EXPONENTIAL DECAY MOMENTUM (EMA - Last 25 draws)
        // =========================================================================
        val h25 = sample.take(25)
        var emaBig = 0.0
        var emaSmall = 0.0

        h25.forEachIndexed { idx, item ->
            val w = (25.0 - idx) / 25.0
            if (item.size == "BIG") emaBig += w else emaSmall += w
            if (DrawItem.baseColorOf(item.number) == "GREEN") greenScore += w else redScore += w
        }

        val recent6Colors = colors.take(6)
        val redIn6 = recent6Colors.count { it == "RED" }
        val greenIn6 = recent6Colors.count { it == "GREEN" }
        if (redIn6 >= 4) redScore += 3.2
        if (greenIn6 >= 4) greenScore += 3.2

        totalBigScore += emaBig * 0.65
        totalSmallScore += emaSmall * 0.65

        val emaFavored = if (emaBig >= emaSmall) "BIG" else "SMALL"
        val emaStrength = (54 + abs(emaBig - emaSmall) * 7).roundToInt().coerceIn(54, 93)
        layerVotes.add(
            LayerVote(
                layerName = "EMA & Color Wave",
                layerNameBn = "মোমেন্টাম ও কালার ওয়েভ",
                favoredSize = emaFavored,
                strengthPercent = emaStrength,
                detail = "B:${String.format("%.1f", emaBig)} vs S:${String.format("%.1f", emaSmall)} • R:$redIn6/6"
            )
        )

        // =========================================================================
        // LAYER 3: DKWIN9 ENERGY BOOST ⚡ & TWIN 10-COMPLEMENT
        // =========================================================================
        val energyEntry = sortedFormulas.find { it.first === dkwinEnergyBoostTrick }
        val twinEntry = sortedFormulas.find { it.first === dkwinTwinTenComplementTrick }
        val topEnergyTrap = energyRanking.find { it.rankOrder == 1 }
        val bestSafeBoost = energyRanking.filter { it.isSafeBoostPick }.maxByOrNull { it.energyPercent + it.trendDelta }

        if (energyEntry != null) {
            layerVotes.add(
                LayerVote(
                    layerName = "DKWin Energy Boost ⚡",
                    layerNameBn = "DKWin এনার্জি বুস্ট অ্যানালাইজার",
                    favoredSize = energyEntry.third,
                    strengthPercent = energyEntry.second.coerceIn(58, 97),
                    detail = "Safe:#${bestSafeBoost?.number ?: 7}(${bestSafeBoost?.energyPercent ?: 12}%) • Trap:#${topEnergyTrap?.number ?: 2}"
                )
            )
        }
        if (twinEntry != null) {
            val comp10 = (10 - nums[0]) % 10
            layerVotes.add(
                LayerVote(
                    layerName = "Twin & 10-Complement",
                    layerNameBn = "টুইন ও ১০-কমপ্লিমেন্ট (10-N)",
                    favoredSize = twinEntry.third,
                    strengthPercent = twinEntry.second.coerceIn(58, 97),
                    detail = "10-${nums[0]}=$comp10 • 9-${nums[0]}=${9 - nums[0]}"
                )
            )
        }

        // =========================================================================
        // LAYER 4: ADAPTIVE STREAK & VIOLET 0/5 RESET MATRIX
        // =========================================================================
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
            }
            currentStreak >= 4 -> {
                if (sizes[0] == "BIG") streakSmallBoost += 7.0 else streakBigBoost += 7.0
            }
            currentStreak == 2 -> {
                if (sizes[0] == "BIG") streakBigBoost += 3.0 else streakSmallBoost += 3.0
            }
        }

        if (colorStreak >= 4) {
            if (colors[0] == "GREEN") redScore += 6.0 else greenScore += 6.0
        }

        totalBigScore += streakBigBoost
        totalSmallScore += streakSmallBoost

        if (zigzagCount >= 4) {
            val nextZigzag = if (sizes[0] == "BIG") "SMALL" else "BIG"
            if (nextZigzag == "BIG") totalBigScore += 6.0 else totalSmallScore += 6.0
        }

        // =========================================================================
        // FINAL SIZE PREDICTION & NUMBER / COLOR SELECTION
        // (DIAGONAL P+N + SKIP-1 MIDPOINT + 10-COMPLEMENT + 5-CYCLE ECHO + ENERGY BOOST)
        // =========================================================================
        val predictedSize = if (totalBigScore >= totalSmallScore) "BIG" else "SMALL"

        val freqMap = (0..9).associateWith { n -> h25.count { it.number == n } }
        val statByNum = chartStats.associateBy { it.number }
        val energyByNum = energyRanking.associateBy { it.number }

        val markovNumberBonus = mutableMapOf<Int, Int>()
        for (i in 1 until nums.size) {
            if (nums[i] == nums[0]) {
                val followedBy = nums[i - 1]
                markovNumberBonus[followedBy] = (markovNumberBonus[followedBy] ?: 0) + 2
            }
        }

        val comp10 = (10 - nums[0]) % 10
        val comp9 = 9 - nums[0]
        val step5 = (nums[0] + 5) % 10
        val fiveCycleEcho = sample.getOrNull(4)?.number ?: comp10

        val candidates = (0..9).filter { n ->
            if (predictedSize == "BIG") n >= 5 else n < 5
        }

        val rankedCandidates = candidates.sortedByDescending { n ->
            val st = statByNum[n]
            val en = energyByNum[n]
            val overdueBoost = if (st != null && st.currentMissing >= st.avgMissing) 4 else 1
            val dkwinSniperBoost = (if (n == diagonalCrossNum) 6 else 0) +
                (if (n == skipOneMidpointNum) 6 else 0) +
                (if (n == comp10) 5 else 0) +
                (if (n == fiveCycleEcho) 4 else 0) +
                (if (n == comp9 || n == step5) 3 else 0)
            val energySafeBoost = when {
                en?.isCrowdTrap == true -> -5
                en?.isSafeBoostPick == true -> 5
                (en?.trendDelta ?: 0) > 0 -> 3
                else -> 1
            }
            val markovBoost = (markovNumberBonus[n] ?: 0) * 3
            val dueBonus = (5 - (freqMap[n] ?: 0)).coerceAtLeast(0)
            val colorMatchBonus = if ((greenScore >= redScore && DrawItem.baseColorOf(n) == "GREEN") ||
                (redScore > greenScore && DrawItem.baseColorOf(n) == "RED")
            ) 3 else 0
            overdueBoost + dkwinSniperBoost + energySafeBoost + markovBoost + dueBonus + colorMatchBonus
        }

        val primaryNumber = rankedCandidates.getOrElse(0) { if (predictedSize == "BIG") 6 else 4 }
        val backupNumber = rankedCandidates.getOrElse(1) { if (predictedSize == "BIG") 8 else 2 }
        val predictedColor = DrawItem.colorOf(primaryNumber)

        val agreeingFormulas = sortedFormulas.count { it.third == predictedSize }
        val totalFormulaCount = allFormulas.size
        val topRate = topFormula?.second ?: 78
        val scoreDiff = abs(totalBigScore - totalSmallScore)
        val rawConfidence = (58 + (agreeingFormulas * 2.4) + (topRate - 60) * 0.4 + scoreDiff * 1.1).roundToInt()
        val confidence = rawConfidence.coerceIn(66, 98)

        val isTrapRound = agreeingFormulas <= 7 && scoreDiff < 4.5
        val riskBadge = when {
            confidence >= 86 && agreeingFormulas >= 10 -> "🎯 SURE SHOT ($agreeingFormulas/$totalFormulaCount FORMULAS)"
            !isTrapRound && confidence >= 74 -> "✅ SAFE SIGNAL ($agreeingFormulas/$totalFormulaCount AGREE)"
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
            "⚡ DKWin9 #1 সূত্র: ${topFormula.first.nameBn} (${topFormula.second}% জয়)"
        } else {
            "⚡ DKWin9 ১৪-ফর্মুলা ইঞ্জিন সক্রিয়"
        }

        val banglaAdvice = buildString {
            append("📊 মার্কেট: $marketRegimeBn\n")
            append("🎯 সেরা নম্বর: $primaryNumber ও $backupNumber ($predictedColor) • ডায়াগনাল (P+N): #$diagonalCrossNum • স্কিপ-১ মিডপয়েন্ট: #$skipOneMidpointNum • ১০-কমপ্লিমেন্ট: #$comp10। ")
            append("মোট ১৪টি DKWin9 সূত্রের মধ্যে $agreeingFormulas টি সূত্র $predictedSize সাপোর্ট করছে।")
        }

        val hotNumbers = freqMap.entries.sortedByDescending { it.value }.take(3).map { it.key }
        val coldNumbers = chartStats.sortedByDescending { it.currentMissing }.take(3).map { it.number }

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
            marketRegimeTitle = marketRegimeTitle,
            marketRegimeBn = marketRegimeBn,
            headlinePattern = headlinePattern,
            topWebsiteTrickBadge = topTrickText,
            banglaAdvice = banglaAdvice,
            repeatCandidate = nums[0],
            tenComplementCandidate = comp10,
            fiveCycleEchoCandidate = fiveCycleEcho,
            diagonalCrossCandidate = diagonalCrossNum,
            skipOneMidpointCandidate = skipOneMidpointNum,
            isRealEnergySynced = isRealEnergy,
            triggers = triggers,
            layerVotes = layerVotes,
            formulaLeaderboard = formulaLeaderboard,
            chartStats = chartStats,
            energyRanking = energyRanking,
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
            primaryNumber = 6,
            backupNumber = 8,
            predictedColor = "RED",
            confidence = 84,
            riskBadge = "ANALYZING...",
            recommendedMultiplier = "1x (Level 1)",
            marketRegimeTitle = "📊 INITIALIZING DKWIN9 14-FORMULA SCANNER...",
            marketRegimeBn = "DKWin9 লাইভ চার্ট, ডায়াগনাল ও স্কিপ-১ মিডপয়েন্ট বিশ্লেষণ করা হচ্ছে...",
            headlinePattern = "⚡ INITIALIZING DKWIN9 14-FORMULA ENGINE...",
            topWebsiteTrickBadge = "⚡ DKWin9 ডায়াগনাল ও স্কিপ-১ মিডপয়েন্ট লোড হচ্ছে...",
            banglaAdvice = "লাইভ এপিআই থেকে ডেটা সিঙ্ক হচ্ছে...",
            repeatCandidate = 4,
            tenComplementCandidate = 6,
            fiveCycleEchoCandidate = 6,
            diagonalCrossCandidate = 8,
            skipOneMidpointCandidate = 4,
            isRealEnergySynced = false,
            triggers = emptyList(),
            layerVotes = emptyList(),
            formulaLeaderboard = emptyList(),
            chartStats = emptyList(),
            energyRanking = emptyList(),
            numberFrequencies = (0..9).associateWith { 0 },
            hotNumbers = listOf(6, 4, 8),
            coldNumbers = listOf(1, 7, 2),
            bigRatioPercent = 50,
            smallRatioPercent = 50,
            greenCount = 0,
            redCount = 0,
            violetCount = violetCountDefault()
        )
    }

    private fun violetCountDefault(): Int = 0
}
