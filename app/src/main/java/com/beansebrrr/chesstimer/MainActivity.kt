package com.beansebrrr.chesstimer

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.beansebrrr.chesstimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
        val preferences = getSharedPreferences("prefs", MODE_PRIVATE)
        setContentView(binding.root)

        val savedDuration = preferences.getLong("TIMER_DURATION_MINUTES", 10).toString()
        val savedIncrement = preferences.getLong("TIMER_INCREMENT_SECONDS", 5).toString()

        binding.inputTimerDuration.editText?.text = SpannableStringBuilder(savedDuration)
        binding.inputTimerIncrement.editText?.text = SpannableStringBuilder(savedIncrement)

        binding.btnSubmit.setOnClickListener {
            var timerDuration: Long?
            var timerIncrement: Long?

            try {
                timerDuration = binding.inputTimerDuration.editText?.text.toString().toLong()
                timerIncrement = binding.inputTimerIncrement.editText?.text.toString().toLong()
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    this,
                    "Please enter a valid duration -_-",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            preferences.edit {
                putLong("TIMER_DURATION_MINUTES", timerDuration)
                putLong("TIMER_INCREMENT_SECONDS", timerIncrement)
            }

            if (timerDuration < 0 || timerIncrement < 0) {
                Toast.makeText(
                    this,
                    "Please enter a valid duration -_-",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val timerIntent = Intent(
                this,
                TimerActivity::class.java
            )
            timerIntent.putExtra("TIMER_DURATION", timerDuration * 60000L)
            timerIntent.putExtra("TIMER_INCREMENT", timerIncrement * 1000L)
            startActivity(timerIntent)
        }
    }
}