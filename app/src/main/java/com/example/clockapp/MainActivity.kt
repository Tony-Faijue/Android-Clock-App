package com.example.clockapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.clockapp.ui.theme.ClockAppTheme

//In this case extend AppCompatActivity() super class
//Instead of ComponentActivity()
class MainActivity : AppCompatActivity() {
    // 3 activities 1 service
    // MainActivity - display button for Time and StopWatch Activities

    // TimeActivity - Display the local time through a clock
    // Requirements: Permission for Location, UI component, Drawable (Clock),
    // StopWatchActivity - Start a Service: StopWatchService
    // Start, Stop , Reset

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Inflate layout, set the content view of the activity view
        setContentView(R.layout.activity_main);

        //Initialize UI Components
        val timer_btn: Button = findViewById(R.id.timer_btn);
        val stop_watch_btn: Button = findViewById(R.id.stop_watch_btn);

        //SetUp OnClickListeners
        //Start Activities Explicitly
        timer_btn.setOnClickListener({
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        })

        stop_watch_btn.setOnClickListener({
            val intent = Intent(this, StopWatchActivity::class.java)
            startActivity(intent)
        })

    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ClockAppTheme {
        Greeting("Android")
    }
}