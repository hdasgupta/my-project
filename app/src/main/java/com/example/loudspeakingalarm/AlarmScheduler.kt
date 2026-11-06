package com.example.loudspeakingalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import java.util.Calendar

object AlarmScheduler {
    const val EXTRA_SNOOZE_COUNT = "EXTRA_SNOOZE_COUNT"
    const val EXTRA_ALARM_LABEL = "EXTRA_ALARM_LABEL"
    const val MAX_SNOOZE_COUNT = 3
    const val SNOOZE_DURATION_MINUTES = 10
    private const val ALARM_REQUEST_CODE = 1001

    fun scheduleAlarmAt(
        context: Context,
        triggerAtMillis: Long,
        snoozeCount: Int = 0,
        label: String = "Preset Alarm"
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
            Toast.makeText(
                context,
                "Please grant Exact Alarm permission to schedule alarms.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
            putExtra(EXTRA_ALARM_LABEL, label)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun schedulePresetTime(context: Context, hourOfDay: Int, minute: Int, label: String): Calendar {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        scheduleAlarmAt(context, calendar.timeInMillis, snoozeCount = 0, label = label)
        return calendar
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
