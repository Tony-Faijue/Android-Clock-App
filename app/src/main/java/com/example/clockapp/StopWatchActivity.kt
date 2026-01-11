package com.example.clockapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class StopWatchActivity: AppCompatActivity() {

    private lateinit var stopWatchTimer: TextView
    private lateinit var bottomNavBar : BottomNavigationView

    /**
     * BroadcastReceiver to listen for updates from the service
     */
    private val updateReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("StopWatchActivity", "Received broadcast: ${intent?.action}")

            when (intent?.action){
                //update the ui with new time
                StopWatchService.STOPWATCH_TICK -> {
                    val timeElapsed = intent.getIntExtra(StopWatchService.TIME_ELAPSED, 0)
                    Log.d("StopWatchActivity", "Tick received: $timeElapsed")
                    updateTimerDisplay(timeElapsed)
                }
                //update the ui with status
                StopWatchService.STOPWATCH_STATUS -> {
                    val timeElapsed = intent.getIntExtra(StopWatchService.TIME_ELAPSED, 0)
                    Log.d("StopWatchActivity", "Status received: $timeElapsed")
                    updateTimerDisplay(timeElapsed)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){

        // Request notification permission for Android 13+
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
        //Inflate the layout, set content view
        setContentView(R.layout.stopwatch_activity)

        //Initialize the stopwatch view
        stopWatchTimer = findViewById(R.id.stopwatch_time)
        //Initialize time display
        updateTimerDisplay(0)

        //Initialize the onClick Listeners for the buttons
        val startButton: Button = findViewById(R.id.start_btn)
        val pauseButton: Button = findViewById(R.id.pause_btn)
        val resetButton: Button = findViewById(R.id.reset_btn)

        startButton.setOnClickListener { onClickStart(it) }
        pauseButton.setOnClickListener { onClickPause(it) }
        resetButton.setOnClickListener { onClickReset(it) }

        //Bottom Navigation View OnClick Listeners
        bottomNavBar = findViewById(R.id.bottom_nav_1)
        //Set the Default Selected Menu Item Highlighted
        bottomNavBar.post {
            bottomNavBar.menu.findItem(R.id.nav_stopwatch).isChecked = true
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
                R.id.nav_timer -> {
                    val intent = Intent(this, TimerActivity::class.java)
                    startActivity(intent)
                    true
                }
                //Navigate to StopWatch Activity
                R.id.nav_stopwatch -> { true } //Already at StopWatch Activity
                else -> false
            }
        }
        //Register broadcast receiver for service updates
        registerReceiver()
        //Request current status from service
        sendCommandToService(StopWatchService.GET_STATUS)

        Log.d("StopWatchActivity", "Manually starting service")
        val testIntent = Intent(this, StopWatchService::class.java)
        testIntent.putExtra(StopWatchService.STOPWATCH_ACTION, StopWatchService.GET_STATUS)
        startService(testIntent)
    }

    /**
     * Send a command to the Stopwatch Service
     */
    private fun sendCommandToService(action: String){
        try {
            val intent = Intent(this, StopWatchService::class.java)
            intent.putExtra(StopWatchService.STOPWATCH_ACTION, action)
            val result = startService(intent)
            if (result == null) {
                Log.e("StopWatchActivity", "startService returned Null")
            } else {
                Log.d("StopWatchActivity", "Service Started Successfully")
            }
        }
            catch(e: Exception){
                Log.e("StopWatchActivity", "Exception starting service: ${e.message}", e)
            }
    }

    /**
     * Tell service to move to background which stops foreground service
     */
    @Override
    override fun onStart() {
        super.onStart()
        sendCommandToService(StopWatchService.MOVE_TO_BACKGROUND)
    }

    /**
     * Tell service to move to the foreground
     */
    @Override
    override fun onStop() {
        super.onStop()
        sendCommandToService(StopWatchService.MOVE_TO_FOREGROUND)
    }

    /**
     * Unregister the receiver
     */
    @Override
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(updateReceiver)
        } catch (e: IllegalArgumentException){
            Log.w("StopWatchActivity","Receiver was already unregistered")
        }
    }

    /**
     * Register BroadcastReceiver to listen for service updates
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerReceiver(){
        val filter = IntentFilter().apply{
            addAction(StopWatchService.STOPWATCH_TICK)
            addAction(StopWatchService.STOPWATCH_STATUS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            registerReceiver(updateReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(updateReceiver, filter)
        }
    }
    //Start the running state for the stopwatch
    private fun onClickStart(view: View){
        Log.d("StopWatchActivity", "Start button clicked")
        sendCommandToService(StopWatchService.START)
    }
    //Stop the running state for the stopwatch
    private fun onClickPause(view: View){
        Log.d("StopWatchActivity", "Pause button clicked")
        sendCommandToService(StopWatchService.PAUSE)
    }
    //Reset the stopwatch
    private fun onClickReset(view: View){
        Log.d("StopWatchActivity", "Reset button clicked")
        sendCommandToService(StopWatchService.RESET)
    }

    //Update the timer display with the formatted time
    private fun updateTimerDisplay(timeElapsed: Int){
        // time units
        val hours: Int = timeElapsed / 3600
        val minutes: Int = (timeElapsed % 3600) / 60
        val secs : Int = timeElapsed % 60
        //Format the time
        val time : String  = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
        stopWatchTimer.text = time
    }
}