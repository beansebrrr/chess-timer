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

    enum class Timer {
        ONE, TWO
    }

    private lateinit var binding: ActivityTimerBinding
    private var isPaused: Boolean = false
    private var isTimerOnePaused: Boolean = true
    private var isTimerTwoPaused: Boolean = true
    private var timerStarted: Boolean = false

    private lateinit var timerOne: CountDownTimerExt
    private lateinit var timerTwo: CountDownTimerExt
    private var timerDuration by Delegates.notNull<Long>()
    private var timerIncrement by Delegates.notNull<Long>()


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
        timerIncrement = intent.getLongExtra("TIMER_INCREMENT", 5000)


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

        binding.btnPauseStart.setOnClickListener {
            if (!timerStarted) {
                Toast.makeText(
                    this,
                    "Press one side to start the timer",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                togglePauseTimers()
            }
        }
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

        binding.displayTimerOne.setOnClickListener {
            if (!isPaused) switchTimerRunning(Timer.ONE)
            else Toast.makeText(this, "Timer is still paused!", Toast.LENGTH_SHORT).show()
        }
        binding.displayTimerTwo.setOnClickListener {
            if (!isPaused) switchTimerRunning(Timer.TWO)
            else Toast.makeText(this, "Timer is still paused!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun switchTimerRunning(timer: Timer) {
        if (!timerStarted) {
            timerStarted = true
            when (timer) {
                Timer.ONE -> {
                    toggleTimerOne(false)
                    toggleTimerTwo(true)
                }
                Timer.TWO -> {
                    toggleTimerOne(true)
                    toggleTimerTwo(false)
                }
            }
        } else {
            when (timer) {
                Timer.ONE -> {
                    val inc = timerOne.incrementTime(timerIncrement)
                    binding.displayTimerOne.text = millisToTimeFormat(inc)
                    toggleTimerOne(true)
                    toggleTimerTwo(false)
                }
                Timer.TWO -> {
                    val inc = timerTwo.incrementTime(timerIncrement)
                    binding.displayTimerTwo.text = millisToTimeFormat(inc)
                    toggleTimerOne(false)
                    toggleTimerTwo(true)
                }
            }
        }
        updatePauseBtnState()
    }

    private fun togglePauseTimers(pause: Boolean? = null) {
        // 90 if-statements lawd save me
        if (pause != null) {
            if (pause) { pauseAction() }
            else { unpauseAction() }
        } else if (isPaused) { unpauseAction()
        } else { pauseAction() }
        updatePauseBtnState()
    }
    private fun pauseAction() {
        timerOne.pause()
        timerTwo.pause()
        isPaused = true
    }
    private fun unpauseAction() {
        if (!isTimerTwoPaused && isTimerOnePaused) {
            switchTimerRunning(Timer.ONE)
        } else if (!isTimerOnePaused && isTimerTwoPaused) {
            switchTimerRunning(Timer.TWO)
        } else {
            Toast.makeText(
                this,
                "Something went wrong and I'm honestly not sure what",
                Toast.LENGTH_SHORT
            ).show()
        }
        isPaused = false
    }

    private fun toggleTimerOne(pause: Boolean? = null) {
        if (pause != null) {
            if (pause) {
                timerOne.pause()
                isTimerOnePaused = true
            } else {
                timerOne.start()
                isTimerOnePaused = false
            }
        } else if (isTimerOnePaused) {
            timerOne.start()
            isTimerOnePaused = false
        } else {
            timerOne.pause()
            isTimerOnePaused = true
        }
        updateTimerOneClickable()
    }
    private fun toggleTimerTwo(pause: Boolean? = null) {
        if (pause != null) {
            if (pause) {
                timerTwo.pause()
                isTimerTwoPaused = true
            } else {
                timerTwo.start()
                isTimerTwoPaused = false
            }
        } else if (isTimerTwoPaused) {
            timerTwo.start()
            isTimerTwoPaused = false
        } else {
            timerTwo.pause()
            isTimerTwoPaused = true
        }
        updateTimerTwoClickable()
    }

    private fun updateTimerOneClickable() {
        binding.displayTimerOne.alpha = if (isTimerOnePaused) 0.5f else 1f
    }
    private fun updateTimerTwoClickable() {
        binding.displayTimerTwo.alpha = if (isTimerTwoPaused) 0.5f else 1f
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
        binding.displayTimerOne.alpha = 1f
        binding.displayTimerTwo.alpha = 1f
        timerStarted = false
        isPaused = false
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
