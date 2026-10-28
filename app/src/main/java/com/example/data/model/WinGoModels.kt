package com.example.data.model

enum class GameMode(
    val key: String,
    val label: String,
    val periodMs: Long,
    val apiUrl: String
) {
    MODE_30S(
        key = "30s",
        label = "30S",
        periodMs = 30_000L,
        apiUrl = "https://draw.ar-lottery01.com/WinGo/WinGo_30S/GetHistoryIssuePage.json?ts="
    );

    companion object {
        fun fromKey(key: String): GameMode = MODE_30S
    }
}

data class DrawItem(
    val period: String,
    val number: Int,
    val premium: String = ""
) {
    val size: String
        get() = sizeOf(number)

    val color: String
        get() = colorOf(number)

    val shortPeriod: String
        get() = if (period.length > 7) period.takeLast(7) else period

    companion object {
        fun sizeOf(n: Int): String = if (n >= 5) "BIG" else "SMALL"

        fun colorOf(n: Int): String = when {
            n == 0 || n == 5 -> "VIOLET"
            n in listOf(2, 4, 6, 8) -> "RED"
            else -> "GREEN"
        }

        fun baseColorOf(n: Int): String = if (n % 2 == 0) "RED" else "GREEN"
    }
}

data class PatternTrigger(
    val title: String,
    val descriptionBn: String,
    val impactLabel: String,
    val isHighPriority: Boolean = false
)

data class LayerVote(
    val layerName: String,
    val layerNameBn: String,
    val favoredSize: String,
    val strengthPercent: Int,
    val detail: String
)

data class FormulaLeaderboardItem(
    val formulaName: String,
    val formulaNameBn: String,
    val liveWinRatePercent: Int,
    val nextVote: String,
    val isTopTrick: Boolean,
    val isAutoReversed: Boolean = false
)

data class ChartNumberStat(
    val number: Int,
    val frequency: Int,
    val currentMissing: Int,
    val avgMissing: Int,
    val maxConsecutive: Int
)

data class EnergyBallStat(
    val number: Int,
    val energyPercent: Int,
    val trendDelta: Int,
    val rankOrder: Int,
    val isSafeBoostPick: Boolean,
    val isCrowdTrap: Boolean
)

data class QuantumPrediction(
    val currentPeriod: String,
    val nextPeriod: String,
    val predictedSize: String,
    val primaryNumber: Int,
    val backupNumber: Int,
    val predictedColor: String,
    val confidence: Int,
    val riskBadge: String,
    val recommendedMultiplier: String,
    val marketRegimeTitle: String,
    val marketRegimeBn: String,
    val headlinePattern: String,
    val topWebsiteTrickBadge: String,
    val banglaAdvice: String,
    val repeatCandidate: Int,
    val tenComplementCandidate: Int,
    val fiveCycleEchoCandidate: Int,
    val diagonalCrossCandidate: Int,
    val skipOneMidpointCandidate: Int,
    val isRealEnergySynced: Boolean,
    val trainedDrawsCount: Int,
    val aiDiscoveredRuleBn: String,
    val autoReversedCount: Int,
    val triggers: List<PatternTrigger>,
    val layerVotes: List<LayerVote>,
    val formulaLeaderboard: List<FormulaLeaderboardItem>,
    val chartStats: List<ChartNumberStat>,
    val energyRanking: List<EnergyBallStat>,
    val numberFrequencies: Map<Int, Int>,
    val hotNumbers: List<Int>,
    val coldNumbers: List<Int>,
    val bigRatioPercent: Int,
    val smallRatioPercent: Int,
    val greenCount: Int,
    val redCount: Int,
    val violetCount: Int
)
