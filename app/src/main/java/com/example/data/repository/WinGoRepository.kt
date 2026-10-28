package com.example.data.repository

import com.example.data.local.PredictionDao
import com.example.data.local.PredictionRecordEntity
import com.example.data.model.DrawItem
import com.example.data.model.GameMode
import com.example.data.remote.ApiFetchResult
import com.example.data.remote.WinGoApiClient
import com.example.domain.QuantumPatternEngine
import kotlinx.coroutines.flow.Flow

class WinGoRepository(
    private val predictionDao: PredictionDao,
    private val apiClient: WinGoApiClient = WinGoApiClient()
) {
    fun observeRecordsByMode(modeKey: String): Flow<List<PredictionRecordEntity>> {
        return predictionDao.observeRecordsByMode(modeKey)
    }

    suspend fun fetchLiveDraws(mode: GameMode): ApiFetchResult {
        return apiClient.fetchHistory(mode)
    }

    fun parseWebViewJson(jsonStr: String): List<DrawItem> {
        return apiClient.parseHistoryJson(jsonStr)
    }

    suspend fun resolveAndStorePrediction(
        mode: GameMode,
        draws: List<DrawItem>
    ) {
        if (draws.isEmpty()) return

        val drawByPeriod = draws.associateBy { it.period }

        // 1. Resolve any pending predictions whose targetPeriod has now appeared in draws
        val unresolved = predictionDao.getUnresolvedByMode(mode.key)
        for (pending in unresolved) {
            val actualDraw = drawByPeriod[pending.targetPeriod]
            if (actualDraw != null) {
                val isSizeWin = actualDraw.size == pending.predictedSize
                val isNumberHit = actualDraw.number == pending.primaryNumber ||
                    actualDraw.number == pending.backupNumber
                predictionDao.upsertRecord(
                    pending.copy(
                        actualNumber = actualDraw.number,
                        actualSize = actualDraw.size,
                        isWin = isSizeWin,
                        numberHit = isNumberHit
                    )
                )
            }
        }

        // 2. Backtest last 6 completed issues if mode has no history yet
        val latestPeriod = draws.first().period
        val nextPeriod = QuantumPatternEngine.incrementPeriod(latestPeriod)
        val existingNextId = "${mode.key}_$nextPeriod"

        if (draws.size >= 16) {
            val secondId = "${mode.key}_${draws[0].period}"
            if (predictionDao.getRecordById(secondId) == null) {
                for (targetIdx in 5 downTo 0) {
                    val historicalSlice = draws.drop(targetIdx + 1)
                    if (historicalSlice.size >= 8) {
                        val targetDraw = draws[targetIdx]
                        val btId = "${mode.key}_${targetDraw.period}"
                        if (predictionDao.getRecordById(btId) == null) {
                            val btPred = QuantumPatternEngine.analyze(historicalSlice)
                            val win = targetDraw.size == btPred.predictedSize
                            val numHit = targetDraw.number == btPred.primaryNumber ||
                                targetDraw.number == btPred.backupNumber
                            predictionDao.upsertRecord(
                                PredictionRecordEntity(
                                    id = btId,
                                    modeKey = mode.key,
                                    targetPeriod = targetDraw.period,
                                    predictedSize = btPred.predictedSize,
                                    primaryNumber = btPred.primaryNumber,
                                    backupNumber = btPred.backupNumber,
                                    predictedColor = btPred.predictedColor,
                                    confidence = btPred.confidence,
                                    patternHeadline = btPred.headlinePattern,
                                    actualNumber = targetDraw.number,
                                    actualSize = targetDraw.size,
                                    isWin = win,
                                    numberHit = numHit,
                                    timestamp = System.currentTimeMillis() - (targetIdx * 1000L)
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Ensure prediction for the upcoming nextPeriod is saved
        if (predictionDao.getRecordById(existingNextId) == null) {
            val pred = QuantumPatternEngine.analyze(draws)
            predictionDao.upsertRecord(
                PredictionRecordEntity(
                    id = existingNextId,
                    modeKey = mode.key,
                    targetPeriod = pred.nextPeriod,
                    predictedSize = pred.predictedSize,
                    primaryNumber = pred.primaryNumber,
                    backupNumber = pred.backupNumber,
                    predictedColor = pred.predictedColor,
                    confidence = pred.confidence,
                    patternHeadline = pred.headlinePattern
                )
            )
        }
    }

    suspend fun clearModeStats(modeKey: String) {
        predictionDao.clearModeRecords(modeKey)
    }
}
