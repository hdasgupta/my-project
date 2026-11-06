package com.example.loudspeakingalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "LoudSpeakingAlarm::AlarmWakeLock"
        )
        wakeLock.acquire(15_000L)

        val snoozeCount = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0)
        val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Preset Alarm"

        val ringIntent = Intent(context, AlarmRingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
        }
        context.startActivity(ringIntent)
    }
}
