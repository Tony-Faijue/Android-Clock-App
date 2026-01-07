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

class StopWatchService: Service() {
    //Use of the companion object which acts
    // like static members of the class
    companion object {
        //Channel ID for notifications
        const val CHANNEL_ID = "Stopwatch_Notifications"

        //Service Actions
        const val START = "START"
        const val PAUSE = "PAUSE"
        const val RESET = "RESET"
        const val GET_STATUS = "GET_STATUS"
        const val MOVE_TO_FOREGROUND = "MOVE_TO_FOREGROUND"
        const val MOVE_TO_BACKGROUND = "MOVE_TO_BACKGROUND"

        //Intent Extras
        const val STOPWATCH_ACTION = "STOPWATCH_ACTION"
        const val TIME_ELAPSED = "TIME_ELAPSED"
        const val IS_STOPWATCH_RUNNING = "IS_STOPWATCH_RUNNING"

        //Intent Actions
        const val STOPWATCH_TICK = "STOPWATCH_TICK"
        const val STOPWATCH_STATUS = "STOPWATCH_STATUS"
    }
    private lateinit var notificationManager: NotificationManager

    /**
     * Job for the stopwatch
     */
    private var timerJob: Job? = null

    /**
     * Job for the notification
     */
    private var notificationJob: Job? = null
    private var isStopWatchRunning: Boolean = false
    private var timeElapsed: Int = 0



    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate(){
        super.onCreate()
        Log.d("StopWatchService", "Service OnCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("StopWatchService", "========== ONSTARTCOMMAND ==========")
        Log.d("StopWatchService", "Action: ${intent?.getStringExtra(STOPWATCH_ACTION)}")

        createChannel()
        getNotificationManager()

        val action = intent?. getStringExtra(STOPWATCH_ACTION)

        when (action){
            START -> startStopWatch()
            PAUSE -> pauseStopWatch()
            RESET -> resetStopWatch()
            GET_STATUS -> sendStatus()
            MOVE_TO_FOREGROUND -> moveToForeground()
            MOVE_TO_BACKGROUND -> moveToBackground()
        }
        return START_STICKY
    }

    //Need to create a notification channel since Android 8.0
    private fun createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Stopwatch",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setSound(null, null)
            notificationChannel.setShowBadge(true)
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun getNotificationManager() {
         notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
    }
    /**
     * Function for broadcasting the status of the stopwatch
     */
    private fun sendStatus() {
        val statusIntent = Intent()
        statusIntent.action = STOPWATCH_STATUS
        statusIntent.setPackage(packageName) //add package name to broadcast
        statusIntent.putExtra(IS_STOPWATCH_RUNNING, isStopWatchRunning)
        statusIntent.putExtra(TIME_ELAPSED, timeElapsed)
        sendBroadcast(statusIntent)
    }
    /**
     * Function to start the stop watch
     * Sets the status isStopWatchRunning = true
     */
    private fun startStopWatch(){
        Log.d("StopWatchService", "========== START STOPWATCH ==========")
        Log.d("StopWatchService", "isStopWatchRunning: $isStopWatchRunning")

        if (isStopWatchRunning) {return} //already true

        isStopWatchRunning = true
        //Send Broadcast to the activity
        sendStatus()

        Log.d("StopWatchService", "Starting timer coroutine...")

        //Cancel any existing timer jobs
        timerJob?.cancel()
        //Start new time job
        timerJob = CoroutineScope(Dispatchers.Main).launch{
            while (isStopWatchRunning){
                delay(1000)
                //Increment the time
                timeElapsed++
                Log.d("StopWatchService", "TICK: $timeElapsed seconds")

                //Broadcast this intent
                //Set the action of the intent to STOPWATCH_TICK
                val stopwatchIntent = Intent()
                stopwatchIntent.action = STOPWATCH_TICK
                //Broadcast this intent to receive it the stopwatch activity
                stopwatchIntent.setPackage(packageName) //add package name to broadcast
                stopwatchIntent.putExtra(TIME_ELAPSED, timeElapsed)
                sendBroadcast(stopwatchIntent)
            }
            Log.d("StopWatchService", "Timer coroutine ENDED")
        }
    }
    /**
     * Pauses the stop watch
     * Sets isStopWatchRunning to false
     */
    private fun pauseStopWatch(){
        isStopWatchRunning = false
        timerJob?.cancel() // Stop the timer
        timerJob = null
        notificationJob?.cancel()
        notificationJob = null
        sendStatus()
    }
    /**
     * Reset the stop watch
     */
    private fun resetStopWatch(){
        isStopWatchRunning = false
        timerJob?.cancel() //Stop the timer
        timerJob = null
        notificationJob?.cancel() //Stop the notification
        notificationJob = null
        timeElapsed = 0
        sendStatus()
    }
    /**
     * This function is responsible for building and returning a Notification with the current
     * state of the stopwatch along with timeElapsed
     */
    private fun buildNotification(): Notification {
        val title = if (isStopWatchRunning){
            "Stopwatch is running!"
        } else {
            "Stopwatch is paused!"
        }
        //time units
        val hours: Int = timeElapsed / 3600
        val minutes: Int = (timeElapsed % 3600) / 60
        val secs : Int = timeElapsed % 60

        //Should be Main or StopWatch Activity?
        val intent = Intent(this, StopWatchActivity::class.java)
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
            .setSmallIcon(R.drawable.stopwatch_solid)
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
     * It checks if the stopwatch is running, if it is then starts a foreground service with the notification
     */
    private fun moveToForeground(){
        if (!isStopWatchRunning){return} //if false skip

        val notification = buildNotification()
        ServiceCompat.startForeground(
            this,
            1,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else 0
        )
            startNotificationUpdates()
    }
    /**
     * When app comes Background stop foreground service
     */
    private fun moveToBackground(){
        //Stop notification updates, the timer still runs
        notificationJob?.cancel()
        notificationJob = null

        //Stop the foreground state
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }
    /**
     * Timer loop that uses Kotlin Coroutine for handling jobs/tasks
     */
    private fun startNotificationUpdates(){
        //Stop notification job
        notificationJob?.cancel()

        //Launch a new coroutine on the Main thread
        notificationJob = CoroutineScope(Dispatchers.Main).launch{
            while (isStopWatchRunning){
                updateNotification()
                delay(1000)
            }
        }
    }

    /**
     * Clear resources when service is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        notificationJob?.cancel()
    }

}