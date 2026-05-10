package com.beansebrrr.chesstimer

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.beansebrrr.chesstimer.databinding.ActivityTimerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.io.path.Path
import kotlin.properties.Delegates

class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding
    private var isPaused: Boolean = true

    private lateinit var timerOne: CountDownTimerExt
    private lateinit var timerTwo: CountDownTimerExt

    private var timerDuration by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        timerDuration = intent.getLongExtra("TIMER_DURATION", 300000)


        timerOne = object : CountDownTimerExt(timerDuration, 10) {
            override fun onTimerTick(millisUntilFinished: Long) {
                binding.displayTimerOne.text = millisToTimeFormat(millisUntilFinished)
            }

            override fun onTimerFinish() {
                binding.displayTimerOne.text = "00:00.00"
            }
        }
        timerTwo = object : CountDownTimerExt(timerDuration, 10) {
            override fun onTimerTick(millisUntilFinished: Long) {
                binding.displayTimerTwo.text = millisToTimeFormat(millisUntilFinished)
            }

            override fun onTimerFinish() {
                binding.displayTimerTwo.text = "00:00.00"
            }
        }

        binding.displayTimerOne.text = millisToTimeFormat(timerDuration)
        binding.displayTimerTwo.text = millisToTimeFormat(timerDuration)
        updatePauseBtnState()

        binding.btnPauseStart.setOnClickListener { togglePauseTimers() }
        binding.btnExit.setOnClickListener {
            togglePauseTimers(true)
            MaterialAlertDialogBuilder(this)
                .setTitle("Are you sure?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton("Stay") { _, _ -> }
                .setPositiveButton("Exit") { _, _ -> finish() }
                .show()
        }
        binding.btnReset.setOnClickListener {
            togglePauseTimers(true)
            MaterialAlertDialogBuilder(this)
                .setTitle("Are you sure?")
                .setMessage("Are you sure you want to restart this timer?")
                .setPositiveButton("Yes") { _, _ -> restartTimers() }
                .setNegativeButton("No") { _, _ -> }
                .show()
        }
    }

    private fun togglePauseTimers(shouldPause: Boolean? = null) {
        if (shouldPause != null) {
            if (shouldPause) {
                timerOne.pause()
                timerTwo.pause()
                isPaused = true
            } else {
                timerOne.start()
                timerTwo.start()
                isPaused = false
            }
        } else if (isPaused) {
            timerOne.start()
            timerTwo.start()
            isPaused = false
        } else {
            timerOne.pause()
            timerTwo.pause()
            isPaused = true
        }
        updatePauseBtnState()
    }

    private fun restartTimers() {
        try {
            timerOne.restart()
            timerTwo.restart()
        } catch (e: UninitializedPropertyAccessException) {
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
        binding.displayTimerOne.text = millisToTimeFormat(timerDuration)
        binding.displayTimerTwo.text = millisToTimeFormat(timerDuration)
    }

    private fun updatePauseBtnState() {
        binding.btnPauseStart.setIconResource(
            if (isPaused) R.drawable.baseline_play_arrow_24
            else R.drawable.baseline_pause_24
        )
    }

    @SuppressLint("DefaultLocale")
    private fun millisToTimeFormat(millis: Long): String {
        val remainderMillisToHundredths = (millis % 1000) / 10
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d.%02d",
            minutes,
            seconds,
            remainderMillisToHundredths
        )
    }
}