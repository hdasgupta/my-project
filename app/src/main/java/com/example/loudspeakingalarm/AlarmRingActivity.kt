package com.example.loudspeakingalarm

import android.app.KeyguardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmRingActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private lateinit var audioManager: AudioManager
    private var previousAlarmVolume: Int = 0
    private var audioFocusRequest: AudioFocusRequest? = null

    private val handler = Handler(Looper.getMainLooper())
    private var snoozeCount = 0
    private var alarmLabel = "Preset Alarm"

    private val speakLoopRunnable = object : Runnable {
        override fun run() {
            if (isTtsReady) {
                forceMaxAlarmVolume()
                speakCurrentTimeLoudly()
            }
            handler.postDelayed(this, 6_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndUnlock()

        snoozeCount = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0)
        alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Preset Alarm"

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        previousAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        requestAudioFocusAndMaxVolume()

        tts = TextToSpeech(this, this)

        buildRingScreenUi()
    }

    private fun buildRingScreenUi() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 64, 48, 64)
            setBackgroundColor(Color.parseColor("#1B0000"))
        }

        val currentTimeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val timeView = TextView(this).apply {
            text = currentTimeFormatted
            textSize = 56f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        container.addView(timeView)

        val labelView = TextView(this).apply {
            text = "$alarmLabel\nSnooze used: $snoozeCount / ${AlarmScheduler.MAX_SNOOZE_COUNT}"
            textSize = 20f
            setTextColor(Color.parseColor("#FFCDD2"))
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 72)
        }
        container.addView(labelView)

        val remainingSnoozes = AlarmScheduler.MAX_SNOOZE_COUNT - snoozeCount
        val snoozeButton = Button(this).apply {
            if (remainingSnoozes > 0) {
                text = "SNOOZE 10 MINUTES ($remainingSnoozes LEFT)"
                isEnabled = true
                setBackgroundColor(Color.parseColor("#FFA000"))
                setTextColor(Color.BLACK)
            } else {
                text = "MAX 3 SNOOZES REACHED"
                isEnabled = false
                setBackgroundColor(Color.DKGRAY)
                setTextColor(Color.LTGRAY)
            }
            textSize = 18f
            setPadding(32, 36, 32, 36)
            setOnClickListener {
                triggerTenMinuteSnooze()
            }
        }
        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 36)
        }
        container.addView(snoozeButton, btnParams)

        val dismissButton = Button(this).apply {
            text = "DISMISS ALARM"
            textSize = 20f
            setBackgroundColor(Color.parseColor("#43A047"))
            setTextColor(Color.WHITE)
            setPadding(32, 40, 32, 40)
            setOnClickListener {
                stopSpeakingAndFinish()
            }
        }
        container.addView(dismissButton, btnParams)

        setContentView(container)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            tts?.setPitch(1.05f)
            tts?.setSpeechRate(0.95f)
            tts?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            isTtsReady = true
            handler.post(speakLoopRunnable)
        }
    }

    private fun forceMaxAlarmVolume() {
        val maxAlarmVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            maxAlarmVol,
            0
        )
    }

    private fun requestAudioFocusAndMaxVolume() {
        forceMaxAlarmVolume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioFocusRequest = req
            audioManager.requestAudioFocus(req)
        }
    }

    private fun speakCurrentTimeLoudly() {
        val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val announcement = "Attention! Wake up! The time is now $timeString."

        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        tts?.speak(
            announcement,
            TextToSpeech.QUEUE_FLUSH,
            params,
            "LOUD_ALARM_UTTERANCE_ID"
        )
    }

    private fun triggerTenMinuteSnooze() {
        if (snoozeCount >= AlarmScheduler.MAX_SNOOZE_COUNT) {
            Toast.makeText(
                this,
                "Maximum 3 snoozes used! Please dismiss the alarm.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val nextSnoozeCount = snoozeCount + 1
        val triggerAtMillis = System.currentTimeMillis() +
                AlarmScheduler.SNOOZE_DURATION_MINUTES * 60 * 1000L

        AlarmScheduler.scheduleAlarmAt(
            context = this,
            triggerAtMillis = triggerAtMillis,
            snoozeCount = nextSnoozeCount,
            label = alarmLabel
        )

        val remaining = AlarmScheduler.MAX_SNOOZE_COUNT - nextSnoozeCount
        Toast.makeText(
            this,
            "Snoozed for 10 minutes ($nextSnoozeCount/${AlarmScheduler.MAX_SNOOZE_COUNT} used, $remaining left)",
            Toast.LENGTH_LONG
        ).show()

        stopSpeakingAndFinish()
    }

    private fun stopSpeakingAndFinish() {
        handler.removeCallbacks(speakLoopRunnable)
        tts?.stop()
        tts?.shutdown()
        tts = null

        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previousAlarmVolume, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        }
        finish()
    }

    private fun turnScreenOnAndUnlock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        handler.removeCallbacks(speakLoopRunnable)
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
