package com.example.clockapp

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
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class TimerActivity: AppCompatActivity() {
    private  val currentTime: LocalDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }

//    val endDateTime = LocalDateTime.of(2023, 10, 31, 23, 59)
//    val currentDateTime = LocalDateTime.now()

//    private val myCounter: CountDownTimer()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        //Inflate the layout, set the content view
        setContentView(R.layout.timer_activity)

    }

}