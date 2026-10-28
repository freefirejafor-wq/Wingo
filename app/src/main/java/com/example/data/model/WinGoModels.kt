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
    ),
    MODE_1M(
        key = "1m",
        label = "1M",
        periodMs = 60_000L,
        apiUrl = "https://draw.ar-lottery01.com/WinGo/WinGo_1M/GetHistoryIssuePage.json?ts="
    ),
    MODE_3M(
        key = "3m",
        label = "3M",
        periodMs = 180_000L,
        apiUrl = "https://draw.ar-lottery01.com/WinGo/WinGo_3M/GetHistoryIssuePage.json?ts="
    ),
    MODE_5M(
        key = "5m",
        label = "5M",
        periodMs = 300_000L,
        apiUrl = "https://draw.ar-lottery01.com/WinGo/WinGo_5M/GetHistoryIssuePage.json?ts="
    );

    companion object {
        fun fromKey(key: String): GameMode = entries.find { it.key == key } ?: MODE_30S
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
    val isTopTrick: Boolean
)

/**
 * Matches the logged-in WinGo Website "Chart" tab telemetry:
 * Frequency, Missing (current draws since last seen), Avg Missing, and Max Consecutive
 */
data class ChartNumberStat(
    val number: Int,
    val frequency: Int,
    val currentMissing: Int,
    val avgMissing: Int,
    val maxConsecutive: Int
)

/**
 * Matches the logged-in DKWin9 "Energy Boost (WinGo 30sec)" modal telemetry:
 * Energy Ranking % (e.g., 17%, 12%, 11%), rank trend delta (e.g., ↑2, ↑6, ↓1),
 * Top 3 Medal rank (1 = +100%, 2 = +80%, 3 = +50%), and whether our engine marks it as a Safe Pick.
 */
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
