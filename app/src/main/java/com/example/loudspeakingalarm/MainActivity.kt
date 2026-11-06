package com.example.loudspeakingalarm

import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 72, 48, 48)
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val titleView = TextView(this).apply {
            text = "Loud Speaking Alarm"
            textSize = 26f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        rootLayout.addView(titleView)

        val subtitleView = TextView(this).apply {
            text = "Speaks current time at max volume • 10-min snooze (max 3x)"
            textSize = 14f
            setTextColor(Color.parseColor("#B0BEC5"))
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 48)
        }
        rootLayout.addView(subtitleView)

        statusTextView = TextView(this).apply {
            text = "No alarm currently set"
            textSize = 18f
            setTextColor(Color.parseColor("#80CBC4"))
            gravity = Gravity.CENTER
            setPadding(24, 32, 24, 48)
        }
        rootLayout.addView(statusTextView)

        val presets = listOf(
            Triple(6, 0, "Early Morning Preset (6:00 AM)"),
            Triple(7, 0, "Wake Up Preset (7:00 AM)"),
            Triple(8, 0, "Workday Preset (8:00 AM)"),
            Triple(21, 0, "Night Reminder Preset (9:00 PM)")
        )

        presets.forEach { (hour, min, label) ->
            val btn = Button(this).apply {
                text = "Set $label"
                textSize = 16f
                setPadding(24, 28, 24, 28)
                setOnClickListener {
                    val cal = AlarmScheduler.schedulePresetTime(this@MainActivity, hour, min, label)
                    val formatted = SimpleDateFormat("hh:mm a, EEE MMM d", Locale.getDefault()).format(cal.time)
                    statusTextView.text = "Active: $label\nScheduled for $formatted\nSnoozes available: 3 x 10 min"
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            rootLayout.addView(btn, params)
        }

        val customBtn = Button(this).apply {
            text = "Pick Custom Alarm Time"
            textSize = 16f
            setOnClickListener {
                val now = Calendar.getInstance()
                TimePickerDialog(
                    this@MainActivity,
                    { _, selectedHour, selectedMinute ->
                        val cal = AlarmScheduler.schedulePresetTime(
                            this@MainActivity,
                            selectedHour,
                            selectedMinute,
                            "Custom Alarm"
                        )
                        val formatted = SimpleDateFormat("hh:mm a, EEE MMM d", Locale.getDefault()).format(cal.time)
                        statusTextView.text = "Active: Custom Alarm\nScheduled for $formatted\nSnoozes available: 3 x 10 min"
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
                ).show()
            }
        }
        val customParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 16, 0, 24)
        }
        rootLayout.addView(customBtn, customParams)

        val cancelBtn = Button(this).apply {
            text = "Cancel Active Alarm"
            textSize = 16f
            setBackgroundColor(Color.parseColor("#B71C1C"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                AlarmScheduler.cancelAlarm(this@MainActivity)
                statusTextView.text = "All alarms cancelled"
                Toast.makeText(this@MainActivity, "Alarm cancelled", Toast.LENGTH_SHORT).show()
            }
        }
        rootLayout.addView(cancelBtn, customParams)

        setContentView(rootLayout)
    }
}
