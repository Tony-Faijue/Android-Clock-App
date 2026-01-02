package com.example.clockapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextClock
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

//In this case extend AppCompatActivity() super class
//Instead of ComponentActivity()
class MainActivity : AppCompatActivity() {
    // 3 activities
    // MainActivity - display button for Time and StopWatch Activities

    // TimeActivity - Display the local time through a clock
    // Requirements: Permission for Location, UI component, Drawable (Clock),
    // StopWatchActivity - Start a Service: StopWatchService
    // Start, Stop , Reset

    private lateinit var clockTextView: TextView
    private lateinit var currentLocalTime: TextClock
    private lateinit var bottomNavBar : BottomNavigationView

    //handler to run a task on the main UI thread
    private val handler = Handler(Looper.getMainLooper())

    //Function to be run on the main UI thread
    /**
     * Updates the small 24 hour clock time
     */
    private fun updateClock(){
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        clockTextView.text = currentTime.format(Date())
        handler.postDelayed(::updateClock, 1000)
    }

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Inflate layout, set the content view of the activity view
        setContentView(R.layout.activity_main);

        //Initialize UI Components

        //Current Time 12-hour format
        clockTextView = findViewById(R.id.curr_time)
        //Run the 24 hour clock
        updateClock()

        //Bottom Navigation Bar OnClick Listeners
        bottomNavBar = findViewById(R.id.bottom_nav_1)
        //Set the Default Item Selected
        bottomNavBar.selectedItemId = R.id.nav_clock

        //Bottom Navigation Bar onClick listeners
        bottomNavBar.setOnItemSelectedListener {
            item -> when(item.itemId) {
                //Navigate to Main Activity (Clock)
                R.id.nav_clock -> { true } //Already on MainActivity
                //Navigate to Timer Activity
                R.id.nav_timer -> {
                    val intent = Intent(this, TimerActivity::class.java)
                    startActivity(intent)
                    true
                }
                //Navigate to StopWatch Activity
                R.id.nav_stopwatch -> {
                    val intent = Intent(this, StopWatchActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


    }

    /**
     * Clean up resources of the activity when destroyed
     */
    @SuppressLint("ImplicitSamInstance")
    @Override
    override fun onDestroy() {
        //Destroy running tasks and clear resources
        super.onDestroy()
        handler.removeCallbacks(::updateClock)
    }

}

