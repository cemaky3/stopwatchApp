package org.hyperskill.stopwatch

import android.app.*
import android.app.PendingIntent.*
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService


const val CHANNEL_ID = "org.hyperskill"
const val NOTIFICATION_ID = 393939
class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this , MainActivity::class.java)
        val pendingIntent = getActivity(this,0,intent,FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Name"
            val descriptionText = "Description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_access_time_24)
            .setContentTitle("Some Title")
            .setContentText("Some Text")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationBuilder.flags = notificationBuilder.flags or Notification.FLAG_INSISTENT

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val startBtn = findViewById<Button>(R.id.startButton)
        val resetBtn = findViewById<Button>(R.id.resetButton)
        val settingsBtn = findViewById<Button>(R.id.settingsButton)
        val textView = findViewById<TextView>(R.id.textView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val timerTV = findViewById<TextView>(R.id.textView)

         var isRunning = false
         val handler = Handler(Looper.getMainLooper())
         var sec = 0
         var colorCount = 0
         val colors = intArrayOf(
            Color.BLACK,
            Color.RED,
            Color.GREEN,
            Color.BLUE
        )
        var timeLimitValue = -1
        val updateTime: Runnable = object : Runnable {

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun run() {

                progressBar.visibility = View.VISIBLE

                val minutes = sec % 3600 / 60
                val secs = sec % 60

                val time = String.format("%02d:%02d", minutes, secs)

                timerTV.text = time

                if(0 < timeLimitValue && timeLimitValue == sec-1) {
                    timerTV.setTextColor(Color.RED)
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder)
                }

                if(isRunning) sec++

                if (colorCount > 3) colorCount = 0

                progressBar.indeterminateTintList = ColorStateList.valueOf(colors[colorCount])
                colorCount++

                handler.postDelayed(this, 1000)
            }
        }
         fun runTimer() {
            isRunning = true
            sec = 0
            handler.post(updateTime)
        }
         fun setLimit() {
            val contentView = LayoutInflater.from(this).inflate(R.layout.settings_dialog, null, false)
            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(contentView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                    if(editText.text.toString().isNotEmpty()) {
                        timeLimitValue = editText.text.toString().toInt()
                    }
                }
                .setNegativeButton(android.R.string.cancel,null)
                .show()
        }

        startBtn.setOnClickListener {
            if (!isRunning){
                runTimer()
                settingsBtn.isEnabled = false
            }
        }
        resetBtn.setOnClickListener {
            isRunning = false
            settingsBtn.isEnabled = true
            textView.text = getString(R.string.zero)
            textView.setTextColor(Color.BLACK)
            progressBar.visibility = View.INVISIBLE
            handler.removeCallbacks(updateTime)
        }
        settingsBtn.setOnClickListener {
            if(!isRunning) setLimit()
        }
    }
}