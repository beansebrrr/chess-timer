package com.beansebrrr.chesstimer

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.beansebrrr.chesstimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isPaused: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val countdown = object : CountDownTimerExt(300000, 10) {
            override fun onTimerTick(millisUntilFinished: Long) {
                binding.tvTimer.text = millisToTimeFormat(millisUntilFinished)
            }
            override fun onTimerFinish() {
                binding.tvTimer.text = millisToTimeFormat(0)
            }
        }

        binding.btnPauseStart.setOnClickListener {
            if (isPaused) {
                countdown.start()
                isPaused = false
                binding.btnPauseStart.text = "Pause"
                binding.btnPauseStart.setIconResource(R.drawable.baseline_pause_24)
            } else {
                countdown.pause()
                isPaused = true
                binding.btnPauseStart.text = "Play"
                binding.btnPauseStart.setIconResource(R.drawable.baseline_play_arrow_24)
            }
        }
    }

    @SuppressLint("DefaultLocale")
   private fun millisToTimeFormat(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, (millis % 1000) / 10)
    }

}