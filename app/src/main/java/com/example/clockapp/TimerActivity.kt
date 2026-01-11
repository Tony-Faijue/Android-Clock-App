package com.example.clockapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
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
import java.util.Locale


class TimerActivity: AppCompatActivity() {
    //need to add logic to handle user input for setting initial timer
    //possible solution using number picker for user to select initial time

    private lateinit var timer: TextView
    private lateinit var bottomNavBar : BottomNavigationView

    /**
     * BroadcastReceiver to listen for updates from the service
     */
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("TimerActivity", "Received broadcast: ${intent?.action}")

            when (intent?.action){
                //update the ui with new time
                TimerService.TIMER_TICK -> {
                    val timeRemaining = intent.getIntExtra(TimerService.TIME_REMAINING, 0)
                    Log.d("TimerActivity", "Tick received: $timeRemaining")
                    updateTimerDisplay(timeRemaining)
                }
                //update the ui with status
                TimerService.TIMER_STATUS -> {
                    val timeRemaining = intent.getIntExtra(TimerService.TIME_REMAINING, 0)
                    Log.d("TimeActivity", "Status received: $timeRemaining")
                    updateTimerDisplay(timeRemaining)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?){

        //Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        super.onCreate(savedInstanceState)
        //Inflate the layout, set the content view
        setContentView(R.layout.timer_activity)


        //Initialize the timer view
        timer = findViewById(R.id.timer_time)


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

    /**
     * Send a command to the Timer Service
     */
    private fun sendCommandToService(action: String){
        try {
            val intent = Intent(this, TimerService::class.java)
            intent.putExtra(TimerService.TIMER_ACTION, action)
            val result = startService(intent)
            if (result == null){
                Log.e("TimerService", "startService returned Null")
            } else {
                Log.d("TimerService", "Service Started Successfully")
            }
        } catch (e: Exception){
            Log.e("TimerActivity", "Exception starting service: ${e.message}", e)
        }
    }

    /**
     * Tell service to move to background which stops foreground service
     */
    @Override
    override  fun onStart(){
        super.onStart()
        sendCommandToService(TimerService.MOVE_TO_BACKGROUND)
    }

    /**
     * Tell service to move to the foreground
     */
    @Override
    override fun onStop(){
        super.onStop()
        sendCommandToService(TimerService.MOVE_TO_FOREGROUND)
    }

    /**
     * Unregister the receiver
     */
    @Override
    override fun onDestroy() {
        super.onDestroy()
        try{
            unregisterReceiver(updateReceiver)
        } catch (e: IllegalArgumentException){
            Log.w("TimerActivity", "Receiver was already unregistered")
        }
    }

    /**
     * Register BroadcastReceiver to listen for service updates
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerReceiver(){
        val filter = IntentFilter().apply{
            addAction(TimerService.TIMER_ACTION)
            addAction(TimerService.TIMER_STATUS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            registerReceiver(updateReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(updateReceiver, filter)
        }
    }
    //Start the running state for the timer
    private fun onClickStart(view: View){
        Log.d("TimerActivity", "Start button clicked")
        sendCommandToService(TimerService.START)
    }
    //Stop the running state for the timer
    private fun onClickPause(view: View){
        Log.d("TimerActivity", "Pause button clicked")
        sendCommandToService(TimerService.PAUSE)
    }
    //Reset the timer
    private fun onClickReset(view: View){
        Log.d("TimerActivity", "Reset button clicked")
        sendCommandToService(TimerService.RESET)
    }

    //Update the timer display with formatted time
    private fun updateTimerDisplay(timeRemaining: Int){
        //time units
        val hours: Int = timeRemaining / 3600
        val minutes: Int = (timeRemaining % 3600) / 60
        val secs : Int = timeRemaining % 60
        //Format the time
        val time : String = String.format(Locale.getDefault(), "%02:%02:%02d", hours, minutes, secs)
        timer.text = time
    }

}