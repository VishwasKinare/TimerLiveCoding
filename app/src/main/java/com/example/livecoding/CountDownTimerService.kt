package com.example.livecoding

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.livecoding.Constants.ACTION_PAUSE_SERVICE
import com.example.livecoding.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.livecoding.Constants.ACTION_STOP_SERVICE
import com.example.livecoding.Constants.NOTIFICATION_CHANNEL_ID
import com.example.livecoding.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.livecoding.Constants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CountDownTimerService: LifecycleService() {

    private var serviceKilled = false
    private var isFirstRun = true

    private val timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    companion object{
        val isTracking = MutableLiveData<Boolean>()
        val timeRunInMillis = MutableLiveData<Long>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        timeRunInMillis.postValue(0L)
        timeRunInSeconds.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()

        postInitialValues()
        isTracking.observe(this) {
            updateNotificationTrackingState(it)
        }
    }

    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking){
            val pauseIntent = Intent(this, CountDownTimerService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, TrackingUtility.getPendingIntentFlag())
        } else {
            val resumeIntent = Intent(this, CountDownTimerService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, TrackingUtility.getPendingIntentFlag())
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        baseNotificationBuilder.clearActions()
        if (!serviceKilled){
            baseNotificationBuilder
                .addAction(R.drawable.ic_launcher_foreground, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, baseNotificationBuilder.build())
        }
    }

    private var isTimerEnabled = false
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer(){
        isTracking.postValue(true)
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!! && timeStarted > 0){
                timeStarted -= 1000
                timeRunInMillis.postValue(timeStarted)
                if (timeStarted > 0){
                    timeRunInSeconds.postValue((timeStarted / 1000) % 60 + 1)
                    lastSecondTimestamp -= 1000L
                }
                delay(1000L)
            }
            killService()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun){
                        timeStarted = it.getLongExtra("Time", 0L)
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)

    }

    private fun startForegroundService(){
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInMillis.observe(this) {
            if (!serviceKilled) {
                baseNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStpWatchTime(it))
                notificationManager.notify(NOTIFICATION_ID, baseNotificationBuilder.build())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}