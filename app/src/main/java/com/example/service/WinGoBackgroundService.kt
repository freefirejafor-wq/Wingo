package com.example.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.PredictionDatabase
import com.example.data.local.PredictionRecordEntity
import com.example.data.model.GameMode
import com.example.data.repository.WinGoRepository
import com.example.domain.QuantumPatternEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object WinGoNotificationHelper {
    const val CHANNEL_BG_ENGINE = "wingo_30s_bg_engine"
    const val CHANNEL_WIN_ALERT = "wingo_30s_win_alert"
    const val FG_NOTIFICATION_ID = 3001
    private const val PREFS_NAME = "wingo_notif_prefs"
    private const val KEY_LAST_WIN_PERIOD = "last_win_period"
    private const val KEY_WIN_NOTIF_ENABLED = "win_notif_enabled"

    @Volatile
    private var lastNotifiedPeriodMem: String = ""

    fun isWinNotificationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_WIN_NOTIF_ENABLED, true)
    }

    fun setWinNotificationEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_WIN_NOTIF_ENABLED, enabled)
            .apply()
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val bgChannel = NotificationChannel(
                CHANNEL_BG_ENGINE,
                "WinGo 30S Background Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "অ্যাপ বন্ধ বা মিনিমাইজ থাকলেও ৩০ সেকেন্ডের ড্র ও Win/Loss ডাটা জমা রাখে"
                setShowBadge(false)
            }

            val winChannel = NotificationChannel(
                CHANNEL_WIN_ALERT,
                "WinGo 30S WIN Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "প্রেডিকশন WIN হলে ছোট নোটিফিকেশন দেখায়"
                enableVibration(true)
            }

            nm.createNotificationChannel(bgChannel)
            nm.createNotificationChannel(winChannel)
        }
    }

    fun buildForegroundNotification(
        context: Context,
        nextPeriod: String,
        predictedSize: String,
        primaryNo: Int,
        backupNo: Int,
        winCount: Int,
        lossCount: Int,
        vaultSize: Int
    ): Notification {
        ensureChannels(context)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val shortNext = if (nextPeriod.length > 5) nextPeriod.takeLast(5) else nextPeriod
        return NotificationCompat.Builder(context, CHANNEL_BG_ENGINE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚡ 30S NEXT #$shortNext ➔ $predictedSize ($primaryNo/$backupNo)")
            .setContentText("W:$winCount • L:$lossCount • 🧠 Vault:$vaultSize Draws (Auto-Sync ON)")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    @Synchronized
    fun notifyWinIfNew(
        context: Context,
        winRecord: PredictionRecordEntity,
        winCount: Int,
        lossCount: Int,
        streak: Int
    ) {
        if (!isWinNotificationEnabled(context) || !hasNotificationPermission(context)) return

        val period = winRecord.targetPeriod
        if (period.isBlank() || period == lastNotifiedPeriodMem) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLast = prefs.getString(KEY_LAST_WIN_PERIOD, "") ?: ""
        if (period == savedLast) {
            lastNotifiedPeriodMem = period
            return
        }

        lastNotifiedPeriodMem = period
        prefs.edit().putString(KEY_LAST_WIN_PERIOD, period).apply()

        ensureChannels(context)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val shortPeriod = if (period.length > 5) period.takeLast(5) else period
        val jackpotTag = if (winRecord.numberHit == true) " 🎯 নাম্বার হিট!" else ""
        val title = "✅ WIN! #$shortPeriod ➔ ${winRecord.actualSize} (${winRecord.actualNumber})$jackpotTag"
        val body = "AI: ${winRecord.predictedSize} (${winRecord.primaryNumber}/${winRecord.backupNumber}) • W:$winCount L:$lossCount • Streak:${streak}🔥"

        val notif = NotificationCompat.Builder(context, CHANNEL_WIN_ALERT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(period.takeLast(5).toIntOrNull() ?: 3099, notif)
    }

    fun startBackgroundEngine(context: Context) {
        try {
            val intent = Intent(context, WinGoBackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Throwable) {
            try {
                context.startService(Intent(context, WinGoBackgroundService::class.java))
            } catch (_: Throwable) {}
        }
    }
}

class WinGoBackgroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundSafely()
        start30sLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundSafely()
        if (pollingJob?.isActive != true) {
            start30sLoop()
        }
        return START_STICKY
    }

    private fun startForegroundSafely() {
        try {
            val notif = WinGoNotificationHelper.buildForegroundNotification(
                context = this,
                nextPeriod = "SYNC",
                predictedSize = "30S AI",
                primaryNo = 6,
                backupNo = 4,
                winCount = 0,
                lossCount = 0,
                vaultSize = 30
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    WinGoNotificationHelper.FG_NOTIFICATION_ID,
                    notif,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(WinGoNotificationHelper.FG_NOTIFICATION_ID, notif)
            }
        } catch (_: Throwable) {}
    }

    private fun start30sLoop() {
        val db = PredictionDatabase.getInstance(applicationContext)
        val repository = WinGoRepository(db.predictionDao())

        pollingJob = serviceScope.launch {
            while (isActive) {
                try {
                    val result = repository.fetchLiveDraws(GameMode.MODE_30S)
                    if (result.draws.isNotEmpty()) {
                        val newlyWon = repository.resolveAndStorePrediction(
                            mode = GameMode.MODE_30S,
                            draws = result.draws
                        )
                        val records = repository.observeRecordsByMode(GameMode.MODE_30S.key).first()
                        val resolved = records.filter { it.isWin != null }
                        val winCount = resolved.count { it.isWin == true }
                        val lossCount = resolved.count { it.isWin == false }
                        var streak = 0
                        for (r in resolved) {
                            if (r.isWin == true) streak++ else break
                        }

                        // Send compact WIN notification if the latest round won!
                        if (newlyWon.isNotEmpty()) {
                            WinGoNotificationHelper.notifyWinIfNew(
                                context = applicationContext,
                                winRecord = newlyWon.first(),
                                winCount = winCount,
                                lossCount = lossCount,
                                streak = streak
                            )
                        }

                        // Update ongoing background status notification
                        if (WinGoNotificationHelper.hasNotificationPermission(applicationContext)) {
                            val pred = QuantumPatternEngine.analyze(result.draws)
                            val updatedNotif = WinGoNotificationHelper.buildForegroundNotification(
                                context = applicationContext,
                                nextPeriod = pred.nextPeriod,
                                predictedSize = pred.predictedSize,
                                primaryNo = pred.primaryNumber,
                                backupNo = pred.backupNumber,
                                winCount = winCount,
                                lossCount = lossCount,
                                vaultSize = result.draws.size
                            )
                            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            nm.notify(WinGoNotificationHelper.FG_NOTIFICATION_ID, updatedNotif)
                        }
                    }
                } catch (_: Throwable) {}
                delay(4000L)
            }
        }
    }

    override fun onDestroy() {
        pollingJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
