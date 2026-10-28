package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "prediction_records")
data class PredictionRecordEntity(
    @PrimaryKey val id: String, // "${modeKey}_${targetPeriod}"
    val modeKey: String,
    val targetPeriod: String,
    val predictedSize: String,
    val primaryNumber: Int,
    val backupNumber: Int,
    val predictedColor: String,
    val confidence: Int,
    val patternHeadline: String,
    val actualNumber: Int? = null,
    val actualSize: String? = null,
    val isWin: Boolean? = null,
    val numberHit: Boolean? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Permanent AI Training Memory Vault:
 * Accumulates every historical draw across sessions so the Self-Learning AI Brain
 * can train on hundreds/thousands of past draws instead of only the last 30 API items.
 */
@Entity(tableName = "historical_draws")
data class HistoricalDrawEntity(
    @PrimaryKey val id: String, // "${modeKey}_${period}"
    val modeKey: String,
    val period: String,
    val number: Int,
    val premium: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface PredictionDao {
    @Query("SELECT * FROM prediction_records WHERE modeKey = :modeKey ORDER BY targetPeriod DESC LIMIT 60")
    fun observeRecordsByMode(modeKey: String): Flow<List<PredictionRecordEntity>>

    @Query("SELECT * FROM prediction_records WHERE modeKey = :modeKey AND isWin IS NULL")
    suspend fun getUnresolvedByMode(modeKey: String): List<PredictionRecordEntity>

    @Query("SELECT * FROM prediction_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: String): PredictionRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(record: PredictionRecordEntity)

    @Query("DELETE FROM prediction_records WHERE modeKey = :modeKey")
    suspend fun clearModeRecords(modeKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistoricalDraws(draws: List<HistoricalDrawEntity>)

    @Query("SELECT * FROM historical_draws WHERE modeKey = :modeKey ORDER BY period DESC LIMIT 600")
    suspend fun getHistoricalDrawsByMode(modeKey: String): List<HistoricalDrawEntity>
}

@Database(
    entities = [PredictionRecordEntity::class, HistoricalDrawEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PredictionDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao

    companion object {
        @Volatile
        private var INSTANCE: PredictionDatabase? = null

        fun getInstance(context: Context): PredictionDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PredictionDatabase::class.java,
                    "wingo_quantum_engine.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
