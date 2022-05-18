package com.example.livecoding

import android.app.PendingIntent
import android.os.Build
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun getFormattedStpWatchTime(ms: Long, includeMillis: Boolean = false): String{
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if (!includeMillis){
            return "${if (hours < 10) "0" else ""} $hours:" +
                    "${if (minutes < 10) "0" else ""} $minutes:" +
                    "${if (seconds < 10) "0" else ""} $seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if (hours < 10) "0" else ""} $hours:" +
                "${if (minutes < 10) "0" else ""} $minutes:" +
                "${if (seconds < 10) "0" else ""} $seconds:" +
                "${if (milliseconds < 10) "0" else ""} $milliseconds"
    }

    fun getPendingIntentFlag() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else
            PendingIntent.FLAG_UPDATE_CURRENT
}