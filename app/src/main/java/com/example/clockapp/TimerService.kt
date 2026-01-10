package com.example.clockapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.timer

class TimerService: Service() {

    companion object {
        //Channel ID for notifications
        const val CHANNEL_ID = "Timer_Notifications"

        //Service Actions
        const val START = "START"
        const val PAUSE = "PAUSE"
        const val RESET = "RESET"
        const val GET_STATUS = "GET_STATUS"
        const val MOVE_TO_FOREGROUND = "MOVE_TO_FOREGROUND"
        const val MOVE_TO_BACKGROUND = "MOVE_TO_BACKGROUND"

        //Intent Extras
        const val TIMER_ACTION = "TIMER_ACTION"
        const val TIME_REMAINING = "TIME_REMAINING"
        const val IS_TIMER_RUNNING = "IS_TIMER_RUNNING"

        //Intent Actions
        const val TIMER_TICK = "TIMER_TICK"
        const val TIMER_STATUS = "TIMER_STATUS"
    }


    private lateinit var notificationManager: NotificationManager

    /**
     * Job for the timer
     */
    private var timerJob: Job? = null

    /**
     * Job for the notification
     */
    private var notificationJob: Job? = null
    private var isTimerRunning: Boolean = false
    private var timeRemaining: Int = 0

    //need to set onBind function to null for the service
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate(){
        super.onCreate()
        Log.d("TimerService", "TimerService OnCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TimerService", "========== ONSTARTCOMMAND ==========")
        Log.d("TimerService", "Action: ${intent?.getStringExtra(TIMER_ACTION)}")

        createChannel()
        getNotificationManager()

        val action = intent?. getStringExtra(TIMER_ACTION)
        //Set the initial time
        val initialTime = intent?. getIntExtra(TIME_REMAINING, 0) ?: 0

        when (action){
            //actions
            START -> startTimer(initialTime)
            PAUSE -> pauseTimer()
            RESET -> resetTimer()
            GET_STATUS -> sendStatus()
            MOVE_TO_FOREGROUND -> moveToForeground()
            MOVE_TO_BACKGROUND -> moveToBackground()
        }
        return START_STICKY
    }

    //Need for notification channel since Android 8.0
    private fun createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Timer",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setSound(null, null)
            notificationChannel.setShowBadge(true)
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun getNotificationManager(){
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
    }
    /**
     * Function for broadcasting the status of the timer
     */
    private fun sendStatus(){
        val statusIntent = Intent()
        statusIntent.action = TIMER_STATUS
        statusIntent.setPackage(packageName) //add package name to broadcast
        statusIntent.putExtra(IS_TIMER_RUNNING, isTimerRunning)
        statusIntent.putExtra(TIME_REMAINING, timeRemaining)
        sendBroadcast(statusIntent)
    }
    /**
     * Function to start the timer
     * Sets the status isTimerRunning = true
     */
    private fun startTimer(initialTime: Int){
        Log.d("TimerService", "========== START TIMER ==========")
        Log.d("TimerService", "isTimerRunning: $isTimerRunning")

        if (isTimerRunning) {return} // already true

        //Initialize the timeRemaining for a new start, otherwise use the current value
        // for timeRemaining
        if(timeRemaining == 0) {
            timeRemaining = initialTime
        }

        isTimerRunning = true
        //Send Broadcast to the activity
        sendStatus()

        Log.d("TimerService", "Starting timer coroutine...")

        //Cancel any existing timer job
        timerJob?.cancel()
        //Start new time job
        timerJob = CoroutineScope(Dispatchers.Main).launch{
            while(isTimerRunning) {
                delay(1000)
                if(timeRemaining > 0) {
                    //Decrease the time
                    timeRemaining--
                    Log.d("TimerService", "TICK: $timeRemaining seconds")

                    //Broadcast this intent
                    //Set the action of the intent TIMER_TICK
                    val timerIntent = Intent()
                    timerIntent.action = TIMER_TICK
                    //Broadcast this intent to receive it in the timer activity
                    timerIntent.setPackage(packageName) // add package name to broadcast
                    timerIntent.putExtra(TIME_REMAINING, timeRemaining)
                    sendBroadcast(timerIntent)
                } else {
                    //When timeRemaining reaches 0 exit loop
                    // Stop the timer
                    resetTimer()
                    break
                }
            }
            Log.d("TimerService", "Timer coroutine ENDED")
        }
    }
    /**
     * Pauses the timer
     * Sets isTimerRunning to false
     */
    private fun pauseTimer(){
        isTimerRunning = false
        timerJob?.cancel() // stop the timer
        timerJob = null
        notificationJob?.cancel()
        notificationJob = null
        sendStatus()
    }
    /**
     * Reset the timer
     */
    private fun resetTimer(){
        isTimerRunning = false
        timerJob?.cancel() //stop the timer
        timerJob = null
        notificationJob?.cancel() //stop the notification
        notificationJob = null
        timeRemaining = 0
        sendStatus()
    }
    /**
     * This function is responsible for building and returning
     * the state of timer along with timeRemaining
     */
    private fun buildNotification(): Notification{
        val title = if (isTimerRunning){
            "Timer is running!"
        } else {
            "Timer is paused!"
        }
        //time units
        val hours: Int = timeRemaining / 3600
        val minutes: Int = (timeRemaining % 3600) /60
        val secs: Int = timeRemaining % 60

        //Set the intent for the TimerActivity
        val intent = Intent(this, TimerActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setOngoing(true)
            .setContentText(
                "${"%02d".format(hours)}: ${"%02d".format(minutes)}: ${"%02d".format(secs)}"
            )
            .setColorized(true)
            .setColor("#BEAEE2".toColorInt())
            .setSmallIcon(R.drawable.bell_solid)
            .setOnlyAlertOnce(true)
            .setContentIntent(pIntent)
            .setAutoCancel(true)
            .build()
    }
    /**
     * Function to update the existing notification with the new notification
     */
    private fun updateNotification(){
        notificationManager.notify(1, buildNotification())
    }

    /**
     * Function is triggered when the app is not visible to the user anymore
     * When app goes to background start the foreground service
     * It checks if the timer is running, if it is then starts a foreground service with the notification
     */
    private fun moveToForeground(){
        if(!isTimerRunning){return} //if false skip

        val notification = buildNotification()
        ServiceCompat.startForeground(
            this,
            1,
            notification,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else 0
        )
            startNotificationUpdates()
    }
    /**
     * When app comes to Background stop foreground service
     */
    private fun moveToBackground(){
        //Stop notification updates, the timer still runs
        notificationJob?.cancel()
        notificationJob = null

        //Stop the foreground state
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }
    /**
     * Launches a notification job that uses Kotlin Coroutine for handling jobs/taske
     * Here waits 1 second and calls the update notification function
     */
    private fun startNotificationUpdates(){
        //Stop notification job
        notificationJob?.cancel()

        //Launch a new coroutine on the Main thread
        notificationJob = CoroutineScope(Dispatchers.Main).launch {
            while (isTimerRunning){
                updateNotification()
                delay(1000)
            }
        }
    }
    /**
     * Clear resources when service is destroyed
     * Cancels both Coroutines
     */
    override fun onDestroy(){
        super.onDestroy()
        timerJob?.cancel()
        notificationJob?.cancel()
    }
}