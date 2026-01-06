package com.example.clockapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.savedstate.serialization.saved
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class StopWatchActivity: AppCompatActivity() {
    //----------------Make Sure add logic so that when the same state activity is open when navigating the function menus

    /**
     * Number of seconds to display on the stopwatch
     */
    private var seconds: Int = 0

    /**
     * State of the stopwatch
     */
    private var running: Boolean = false
    private var wasRunning: Boolean = false


    private lateinit var stopWatchTimer: TextView
    private lateinit var bottomNavBar : BottomNavigationView
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        //Inflate the layout, set content view
        setContentView(R.layout.stopwatch_activity)

        //Initialize the stopwatch view
        stopWatchTimer = findViewById(R.id.stopwatch_time)

        //Initialize the onClick Listeners for the buttons
        val startButton: Button = findViewById(R.id.start_btn)
        val pauseButton: Button = findViewById(R.id.pause_btn)
        val resetButton: Button = findViewById(R.id.reset_btn)

        startButton.setOnClickListener { onClickStart(it) }
        pauseButton.setOnClickListener { onClickPause(it) }
        resetButton.setOnClickListener { onClickReset(it) }

        //Get the savedInstanceState if exist
        if(savedInstanceState != null){
            seconds = savedInstanceState.getInt("seconds")
            running = savedInstanceState.getBoolean("running")
            wasRunning = savedInstanceState.getBoolean("wasRunning")
        }

        //Call the Stopwatch function
        runStopWatch()

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
    }

    // Save the state of the stopwatch
    @Override
     override fun onSaveInstanceState(outState: Bundle){
        outState.putInt("seconds", seconds)
        outState.putBoolean("running", running)
        outState.putBoolean("wasRunning", wasRunning)
        //Always call super method
         super.onSaveInstanceState(outState)
    }
    //If the activity is paused, stop the stopwatch
    @Override
    override fun onStop() {
        super.onStop()
        wasRunning = running
        running = false
    }
    //If the activity is resumed start the stop watch
    //if it was running previously start the stopwatch
    @Override
    override fun onResume() {
        super.onResume()
        if (wasRunning){
            running = true
        }
    }
    //Start the running state for the stopwatch
    private fun onClickStart(view: View){
        running = true
    }
    //Stop the running state for the stopwatch
    private fun onClickPause(view: View){
        running = false
    }
    //Reset the stopwatch
    private fun onClickReset(view: View){
        running = false
        seconds = 0
    }
    //Runs the StopWatch
    private fun runStopWatch(){
        // time units
        val hours: Int = seconds / 3600
        val minutes: Int = (seconds % 3600) / 60
        val secs : Int = seconds % 60

        //Format the time
        val time : String  = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
        stopWatchTimer.text = time

        //If stopwatch in running state increment the seconds
        if(running){
            seconds++
        }
        //Post the function with 1 second delay
        handler.postDelayed(::runStopWatch, 1000)
    }

}