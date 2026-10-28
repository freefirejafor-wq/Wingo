package com.example.data.repository

import com.example.data.local.HistoricalDrawEntity
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
        val rawResult = apiClient.fetchHistory(mode)
        if (rawResult.draws.isNotEmpty()) {
            val accumulated = persistAndGetAccumulatedDraws(mode.key, rawResult.draws)
            return rawResult.copy(draws = accumulated)
        }
        return rawResult
    }

    fun parseWebViewJson(jsonStr: String): List<DrawItem> {
        return apiClient.parseHistoryJson(jsonStr)
    }

    suspend fun persistAndGetAccumulatedDraws(
        modeKey: String,
        freshDraws: List<DrawItem>
    ): List<DrawItem> {
        if (freshDraws.isNotEmpty()) {
            val entities = freshDraws.map { d ->
                HistoricalDrawEntity(
                    id = "${modeKey}_${d.period}",
                    modeKey = modeKey,
                    period = d.period,
                    number = d.number,
                    premium = d.premium
                )
            }
            predictionDao.upsertHistoricalDraws(entities)
        }
        val stored = predictionDao.getHistoricalDrawsByMode(modeKey)
        return if (stored.isNotEmpty()) {
            stored.map {
                DrawItem(
                    period = it.period,
                    number = it.number,
                    premium = it.premium
                )
            }
        } else {
            freshDraws
        }
    }

    /**
     * Resolves pending predictions, catches up any rounds missed while the app was closed/offline,
     * saves the upcoming nextPeriod prediction, and returns any newly verified WIN records.
     */
    suspend fun resolveAndStorePrediction(
        mode: GameMode,
        draws: List<DrawItem>
    ): List<PredictionRecordEntity> {
        if (draws.isEmpty()) return emptyList()

        val newlyResolvedWins = mutableListOf<PredictionRecordEntity>()
        val drawByPeriod = draws.associateBy { it.period }
        val latestPeriod = draws.first().period

        // 1. Resolve any pending predictions whose targetPeriod has now appeared in draws
        val unresolved = predictionDao.getUnresolvedByMode(mode.key)
        for (pending in unresolved) {
            val actualDraw = drawByPeriod[pending.targetPeriod]
            if (actualDraw != null) {
                val isSizeWin = actualDraw.size == pending.predictedSize
                val isNumberHit = actualDraw.number == pending.primaryNumber ||
                    actualDraw.number == pending.backupNumber
                val updated = pending.copy(
                    actualNumber = actualDraw.number,
                    actualSize = actualDraw.size,
                    isWin = isSizeWin,
                    numberHit = isNumberHit
                )
                predictionDao.upsertRecord(updated)
                if (isSizeWin && actualDraw.period == latestPeriod) {
                    newlyResolvedWins.add(updated)
                }
            }
        }

        // 2. Offline / Background Catch-Up Backfill:
        // If any recent completed draws (up to last 12 rounds) were missed while the user was offline
        // or away from the app, reconstruct the AI prediction from the historical slice prior to that draw
        // and store the verified WIN/LOSS record so no 30S round is ever missing!
        if (draws.size >= 14) {
            val maxCatchUpIdx = (draws.size - 9).coerceAtMost(11)
            for (targetIdx in maxCatchUpIdx downTo 0) {
                val targetDraw = draws[targetIdx]
                val recId = "${mode.key}_${targetDraw.period}"
                val existing = predictionDao.getRecordById(recId)
                if (existing == null) {
                    val historicalSlice = draws.drop(targetIdx + 1)
                    if (historicalSlice.size >= 8) {
                        val btPred = QuantumPatternEngine.analyze(historicalSlice)
                        val win = targetDraw.size == btPred.predictedSize
                        val numHit = targetDraw.number == btPred.primaryNumber ||
                            targetDraw.number == btPred.backupNumber
                        val backfilled = PredictionRecordEntity(
                            id = recId,
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
                        predictionDao.upsertRecord(backfilled)
                        if (win && targetIdx == 0) {
                            newlyResolvedWins.add(backfilled)
                        }
                    }
                }
            }
        }

        // 3. Ensure prediction for the upcoming nextPeriod is saved
        val nextPeriod = QuantumPatternEngine.incrementPeriod(latestPeriod)
        val existingNextId = "${mode.key}_$nextPeriod"
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

        return newlyResolvedWins
    }

    suspend fun clearModeStats(modeKey: String) {
        predictionDao.clearModeRecords(modeKey)
    }
}
