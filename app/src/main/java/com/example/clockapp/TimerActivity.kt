package com.example.clockapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.clockapp.ui.theme.ClockAppTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class TimerActivity: AppCompatActivity() {
    private lateinit var timer: TextView
    private lateinit var bottomNavBar : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        //Inflate the layout, set the content view
        setContentView(R.layout.timer_activity)

        bottomNavBar = findViewById(R.id.bottom_nav_1)
        //Set default selected menu item
        bottomNavBar.post {
            bottomNavBar.menu.findItem(R.id.nav_timer).isChecked = true
        }
        bottomNavBar.setOnItemSelectedListener {
                item -> when(item.itemId) {
            //Navigate to Main Activity (Clock)
            R.id.nav_clock -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            //Navigate to Timer Activity
            R.id.nav_timer -> { true } //Already on Timer Activity
            //Navigate to StopWatch Activity
            R.id.nav_stopwatch -> {
                val intent = Intent (this, StopWatchActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }
        }

    }

}