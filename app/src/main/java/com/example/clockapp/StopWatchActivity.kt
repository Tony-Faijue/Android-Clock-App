package com.example.clockapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class StopWatchActivity: AppCompatActivity() {

    private lateinit var stopWatchTimer: TextView
    private lateinit var bottomNavBar : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        //Inflate the layout, set content view
        setContentView(R.layout.stopwatch_activity)

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
}