package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PredictionRecordEntity
import com.example.data.model.DrawItem
import com.example.data.model.GameMode
import com.example.data.model.QuantumPrediction
import com.example.data.repository.WinGoRepository
import com.example.domain.QuantumPatternEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class EngineTab {
    LIVE_SIGNAL,
    ANALYTICS,
    FUND_PLANNER,
    WEB_ENGINE
}

data class FundStepItem(
    val level: Int,
    val multiplier: Int,
    val betAmount: Int,
    val cumulativeCost: Int,
    val netProfitIfWin: Int
)

data class WinGoUiState(
    val selectedMode: GameMode = GameMode.MODE_30S,
    val selectedTab: EngineTab = EngineTab.LIVE_SIGNAL,
    val remainingSeconds: Int = 30,
    val timerProgress: Float = 1f,
    val draws: List<DrawItem> = emptyList(),
    val prediction: QuantumPrediction = QuantumPatternEngine.analyze(emptyList()),
    val isLiveApiConnected: Boolean = true,
    val apiStatusText: String = "CONNECTING TO AR-LOTTERY API...",
    val isSyncing: Boolean = false,
    val syncTriggerCount: Int = 0,
    val matrixAnimationEnabled: Boolean = true,
    val totalCapitalInput: String = "1000",
    val selectedStepCount: Int = 5,
    val multiplierScale: Float = 2.0f
) {
    val fundSteps: List<FundStepItem>
        get() {
            val capital = totalCapitalInput.toIntOrNull()?.coerceAtLeast(50) ?: 1000
            val steps = selectedStepCount.coerceIn(3, 8)
            val weights = (0 until steps).map { idx ->
                Math.pow(multiplierScale.toDouble(), idx.toDouble())
            }
            val totalWeight = weights.sum()
            val baseUnit = (capital / totalWeight).coerceAtLeast(1.0)
            var cumulative = 0
            return weights.mapIndexed { idx, w ->
                val bet = (baseUnit * w).roundToInt().coerceAtLeast(1)
                cumulative += bet
                val grossReturn = (bet * 1.96).roundToInt()
                val netProfit = grossReturn - cumulative
                FundStepItem(
                    level = idx + 1,
                    multiplier = w.roundToInt(),
                    betAmount = bet,
                    cumulativeCost = cumulative,
                    netProfitIfWin = netProfit
                )
            }
        }
}

class WinGoViewModel(
    private val repository: WinGoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WinGoUiState())
    val uiState: StateFlow<WinGoUiState> = _uiState.asStateFlow()

    private val _selectedModeFlow = MutableStateFlow(GameMode.MODE_30S)
    private var lastWebViewUpdateMs: Long = 0L

    @OptIn(ExperimentalCoroutinesApi::class)
    val modeRecords: StateFlow<List<PredictionRecordEntity>> = _selectedModeFlow
        .flatMapLatest { mode -> repository.observeRecordsByMode(mode.key) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        startCountdownLoop()
        startPollingLoop()
    }

    private fun startCountdownLoop() {
        viewModelScope.launch {
            while (isActive) {
                val mode = _uiState.value.selectedMode
                val periodMs = mode.periodMs
                val now = System.currentTimeMillis()
                val remainMs = periodMs - (now % periodMs)
                val remainSec = ((remainMs + 999L) / 1000L).toInt()
                val progress = (remainMs.toFloat() / periodMs.toFloat()).coerceIn(0f, 1f)

                _uiState.update {
                    it.copy(
                        remainingSeconds = remainSec,
                        timerProgress = progress
                    )
                }
                delay(250L)
            }
        }
    }

    private fun startPollingLoop() {
        viewModelScope.launch {
            while (isActive) {
                // Only use OkHttp fallback if WebView bridge hasn't delivered fresh JSON in last 3.5s
                if (System.currentTimeMillis() - lastWebViewUpdateMs > 3500L) {
                    pollNow()
                }
                delay(2000L)
            }
        }
    }

    fun onWebViewJsonReceived(json: String) {
        viewModelScope.launch {
            val parsed = repository.parseWebViewJson(json)
            if (parsed.isNotEmpty()) {
                lastWebViewUpdateMs = System.currentTimeMillis()
                applyDrawsToEngine(
                    draws = parsed,
                    isLive = true,
                    statusMessage = "LIVE API (${_uiState.value.selectedMode.label} • CHROMIUM)"
                )
            }
        }
    }

    fun selectMode(mode: GameMode) {
        if (_uiState.value.selectedMode == mode) return
        _selectedModeFlow.value = mode
        lastWebViewUpdateMs = 0L
        _uiState.update {
            it.copy(
                selectedMode = mode,
                isSyncing = true,
                syncTriggerCount = it.syncTriggerCount + 1
            )
        }
        viewModelScope.launch {
            pollNow()
        }
    }

    fun selectTab(tab: EngineTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleMatrixAnimation() {
        _uiState.update { it.copy(matrixAnimationEnabled = !it.matrixAnimationEnabled) }
    }

    fun updateCapitalInput(value: String) {
        val filtered = value.filter { it.isDigit() }.take(7)
        _uiState.update { it.copy(totalCapitalInput = filtered) }
    }

    fun updateStepCount(steps: Int) {
        _uiState.update { it.copy(selectedStepCount = steps.coerceIn(3, 7)) }
    }

    fun updateMultiplierScale(scale: Float) {
        _uiState.update { it.copy(multiplierScale = scale) }
    }

    fun resetCurrentModeStats() {
        val mode = _uiState.value.selectedMode
        viewModelScope.launch {
            repository.clearModeStats(mode.key)
            pollNow()
        }
    }

    fun forceSync() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSyncing = true,
                    syncTriggerCount = it.syncTriggerCount + 1
                )
            }
            pollNow()
        }
    }

    private suspend fun pollNow() {
        try {
            val mode = _uiState.value.selectedMode
            val result = repository.fetchLiveDraws(mode)
            if (result.draws.isNotEmpty()) {
                applyDrawsToEngine(
                    draws = result.draws,
                    isLive = result.isLiveApi,
                    statusMessage = result.statusMessage
                )
            }
        } catch (_: Throwable) {
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    private suspend fun applyDrawsToEngine(
        draws: List<DrawItem>,
        isLive: Boolean,
        statusMessage: String
    ) {
        val mode = _uiState.value.selectedMode
        repository.resolveAndStorePrediction(mode, draws)

        val resolvedRecords = modeRecords.value.filter { it.isWin != null }
        var currentWinStreak = 0
        var currentLossStreak = 0
        for (r in resolvedRecords) {
            if (r.isWin == true) {
                if (currentLossStreak == 0) currentWinStreak++ else break
            } else if (r.isWin == false) {
                if (currentWinStreak == 0) currentLossStreak++ else break
            }
        }

        val quantumPred = QuantumPatternEngine.analyze(
            draws = draws,
            recentWinStreak = currentWinStreak,
            recentLossStreak = currentLossStreak
        )

        _uiState.update {
            it.copy(
                draws = draws,
                prediction = quantumPred,
                isLiveApiConnected = isLive,
                apiStatusText = statusMessage,
                isSyncing = false
            )
        }
    }

    companion object {
        fun provideFactory(repository: WinGoRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WinGoViewModel(repository) as T
                }
            }
        }
    }
}
