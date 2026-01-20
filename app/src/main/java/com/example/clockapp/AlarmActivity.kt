package com.example.clockapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.clockapp.ui.theme.ClockAppTheme

class AlarmActivity: AppCompatActivity() {


    @Override
    override fun onCreate(savedInstance: Bundle?){
        super.onCreate(savedInstance)
        setContent {
            ClockAppTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    Greeting("Android")
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier){
        Text(
            text = "Hell $name",
            modifier = modifier
        )
    }

    @Preview
    @Composable
    fun GreetingPreview(){
        ClockAppTheme {
            Greeting("Android")
        }
    }

}