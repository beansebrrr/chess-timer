package com.beansebrrr.chesstimer

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.beansebrrr.chesstimer.databinding.ActivityTimerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.properties.Delegates

class TimerActivity : AppCompatActivity() {

    enum class Timer {
        ONE, TWO
    }
    var currentTimer: Timer = Timer.ONE


    private lateinit var binding: ActivityTimerBinding
    private var isPaused: Boolean = false
    private var isEnded: Boolean = false
    private var firstClick: Boolean = true

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
                isEnded = true
                onEnd()
            }
        }
        timerTwo = object : CountDownTimerExt(timerDuration, 10) {
            override fun onTimerTick(millisUntilFinished: Long) {
                binding.displayTimerTwo.text = millisToTimeFormat(millisUntilFinished)
            }

            override fun onTimerFinish() {
                binding.displayTimerTwo.text = "00:00.00"
                isEnded = true
                onEnd()
            }
        }

        resetTimers()

        binding.displayTimerOne.setOnClickListener {
            if (firstClick) {
                toggleCurrentTimer(Timer.TWO)
            } else if (isEnded) {
                onEnd()
            }
            toggleCurrentTimer()
            startTimer()
        }
        binding.displayTimerTwo.setOnClickListener {
            if (firstClick) {
                toggleCurrentTimer(Timer.ONE)
            } else if (isEnded) {
                onEnd()
            }
            toggleCurrentTimer()
            startTimer()
        }

        binding.btnPauseStart.setOnClickListener {
            if (firstClick) {
                Toast.makeText(
                    this,
                    "Press one side to start the timer",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                togglePause()
            }
        }
        binding.btnReset.setOnClickListener {
            pause()
            MaterialAlertDialogBuilder(this)
                .setTitle("Are you sure?")
                .setMessage("Are you sure you want to restart this timer?")
                .setPositiveButton("Yes") { _, _ -> resetTimers() }
                .setNegativeButton("No") { _, _ -> unpause() }
                .show()
        }

        binding.btnExit.setOnClickListener { onExitAction() }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { onExitAction() }
        }
        onBackPressedDispatcher.addCallback(callback)
    }

    private fun toggleCurrentTimer(timer: Timer? = null) {
        // Toggles between the two timers
        // Allows for the currentTimer to be set manually
        currentTimer = timer
            ?: when (currentTimer) {
                Timer.ONE -> Timer.TWO
                Timer.TWO -> Timer.ONE
            }
    }

    private fun startTimer(shouldIncrement: Boolean = true) {

        when (currentTimer) {
            Timer.ONE -> {
                timerOne.start()
                timerTwo.pause()
                if (firstClick) {
                    firstClick = false
                } else if (shouldIncrement) {
                    val inc = timerTwo.incrementTime(timerIncrement)
                    binding.displayTimerTwo.text = millisToTimeFormat(inc)
                }
                updateClickableTimers()
            }
            Timer.TWO -> {
                timerTwo.start()
                timerOne.pause()
                if (firstClick) {
                    firstClick = false
                } else if (shouldIncrement) {
                    val inc = timerOne.incrementTime(timerIncrement)
                    binding.displayTimerOne.text = millisToTimeFormat(inc)
                }
                updateClickableTimers()
            }
        }
    }

    private fun togglePause() {
        if (isPaused) { unpause() }
        else { pause() }
    }

    private fun pause() {
        timerOne.pause()
        timerTwo.pause()
        binding.displayTimerOne.alpha = 0.5f
        binding.displayTimerTwo.alpha = 0.5f
        binding.displayTimerOne.isClickable = false
        binding.displayTimerTwo.isClickable = false
        isPaused = true
        updatePauseBtnState()
    }

    private fun unpause() {
        if (firstClick) { return }
        isPaused = false
        startTimer(shouldIncrement = false)
        updateClickableTimers()
        updatePauseBtnState()
    }

    private fun updateClickableTimers() {
        if (firstClick) {
            binding.displayTimerOne.alpha = 1f
            binding.displayTimerTwo.alpha = 1f
            binding.displayTimerOne.isClickable = true
            binding.displayTimerTwo.isClickable = true
        } else if (currentTimer == Timer.ONE) {
            binding.displayTimerOne.alpha = 1f
            binding.displayTimerTwo.alpha = 0.5f
            binding.displayTimerOne.isClickable = true
            binding.displayTimerTwo.isClickable = false
        } else {
            binding.displayTimerOne.alpha = 0.5f
            binding.displayTimerTwo.alpha = 1f
            binding.displayTimerOne.isClickable = false
            binding.displayTimerTwo.isClickable = true
        }
    }
    private fun resetTimers() {
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
        binding.displayTimerOne.isClickable = true
        binding.displayTimerTwo.isClickable = true
        firstClick = true
        isPaused = false
        updatePauseBtnState()
    }

    private fun updatePauseBtnState() {
        binding.btnPauseStart.setIconResource(
            if (isPaused) R.drawable.baseline_play_arrow_24
            else R.drawable.baseline_pause_24
        )
    }

    private fun onExitAction() {
        pause()
        MaterialAlertDialogBuilder(this@TimerActivity)
            .setTitle("Are you sure?")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit") { _, _ -> finish() }
            .setNegativeButton("Stay") { _, _ -> unpause() }
            .show()
    }

    private fun onEnd() {
        binding.displayTimerOne.isClickable = false
        binding.displayTimerTwo.isClickable = false
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            prepare()
            start()
        }
        Toast.makeText(
            this,
            "The game has ended",
            Toast.LENGTH_SHORT
        ).show()
    }


    @SuppressLint("DefaultLocale")
    private fun millisToTimeFormat(millis: Long): String {
        val remainderMillis = (millis % 1000)
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60))
        return String.format("%02d:%02d.%03d",
            minutes,
            seconds,
            remainderMillis
        )
    }
}
