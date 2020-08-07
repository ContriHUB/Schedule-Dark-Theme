/*
 * Copyright (c) 2020, Shashank Verma <shashank.verma2002@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.alpha.dev.schedule_dark_theme.appService

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.alpha.dev.schedule_dark_theme.R

class NotificationHelper(ctx: Context) : ContextWrapper(ctx) {

    private val channelID = "Running Service"
    private val channelID2 = "Theme Notification"
    private var mManager: NotificationManager? = null

    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(channelID, channelID, NotificationManager.IMPORTANCE_LOW)
        channel.lightColor = getColor(R.color.pixel)
        channel.setSound(null, null)
        channel.enableVibration(false)

        val channel2 = NotificationChannel(channelID2, "Theme Notification", NotificationManager.IMPORTANCE_HIGH)
        channel2.lightColor = Color.BLUE
        channel2.enableVibration(true)

        getManager().createNotificationChannel(channel)
        getManager().createNotificationChannel(channel2)
    }

    fun getManager(): NotificationManager {
        if (mManager == null) {
            mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return mManager!!
    }

    fun serviceNotification(): NotificationCompat.Builder {

        val openSettings = PendingIntent.getActivity(
                applicationContext,
                -1,
                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
                        Settings.EXTRA_APP_PACKAGE,
                        applicationContext.packageName
                ).putExtra(Settings.EXTRA_CHANNEL_ID, channelID), 0
        )

        return NotificationCompat.Builder(applicationContext, channelID)
                .setContentTitle("Running Service")
                .setSmallIcon(R.drawable.ic_brightness_4_black_24dp)
                .setColorized(true)
                .setContentText("Tap here and disable this notifications :P")
                .setStyle(NotificationCompat.BigTextStyle())
                .setColor(getColor(R.color.pixel))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setSound(null)
                .setContentIntent(openSettings)
        /*.addAction(
            R.drawable.ic_brightness_2_black_24dp,
            "Enable Dark Theme",
            PendingIntent.getBroadcast(applicationContext, 3, Intent(applicationContext, NotifyDarkTheme::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        )
        .addAction(
            R.drawable.ic_close_black_24dp,
            "Cancel",
            PendingIntent.getBroadcast(applicationContext, 4, Intent(applicationContext, CancelSwitch::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        )*/
    }

//    fun generalNotification(head: String, text: String): NotificationCompat.Builder {
//        return NotificationCompat.Builder(applicationContext, channelID2)
//                .setContentTitle(head)
//                .setSmallIcon(R.drawable.ic_brightness_4_black_24dp)
//                .setColorized(true)
//                .setContentText(text)
//                .setStyle(NotificationCompat.BigTextStyle())
//                .setColor(getColor(R.color.pixel))
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setSound(null)
//    }

    fun darkRunningNotification(): NotificationCompat.Builder {
        /*  val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
          val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, 0)*/

        return NotificationCompat.Builder(applicationContext, channelID2)
                .setContentTitle("Change Theme (Dark)")
                .setSmallIcon(R.drawable.ic_brightness_4_black_24dp)
                .setColorized(true)
                .setContentText("It's time to shift to Dark theme. We don\'t mean to scare you by suddenly changing to dark theme. Tap below to switch to Dark theme :P")
                .setStyle(NotificationCompat.BigTextStyle())
                .setColor(getColor(R.color.pixel))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(null)
                .addAction(
                        R.drawable.ic_brightness_2_black_24dp,
                        "Enable Dark Theme",
                        PendingIntent.getBroadcast(applicationContext, 3, Intent(applicationContext, ToggleDarkTheme::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .addAction(
                        R.drawable.ic_close_black_24dp,
                        "Cancel",
                        PendingIntent.getBroadcast(applicationContext, 4, Intent(applicationContext, CancelSwitch::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
                )
    }

    fun lightRunningNotification(): NotificationCompat.Builder {
        /*val intent = Intent(applicationContext, HideNotificationAction::class.java)
        val actionIntent = PendingIntent.getBroadcast(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, PendingIntent.FLAG_UPDATE_CURRENT)*/

        return NotificationCompat.Builder(applicationContext, channelID2)
                .setContentTitle("Change Theme (Light)")
                .setSmallIcon(R.drawable.ic_brightness_4_black_24dp)     //create icon
                .setColorized(true)
                .setContentText("It's time to shift to Light theme. We don\'t mean to scare you by suddenly changing to light theme. Tap below to switch to Light theme :P")
                .setStyle(NotificationCompat.BigTextStyle())
                .setColor(getColor(R.color.pixel))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(null)
                .addAction(
                        R.drawable.ic_brightness_7_black_24dp,
                        "Enable Light Theme",
                        PendingIntent.getBroadcast(applicationContext, 3, Intent(applicationContext, ToggleLightTheme::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .addAction(
                        R.drawable.ic_close_black_24dp,
                        "Cancel",
                        PendingIntent.getBroadcast(applicationContext, 4, Intent(applicationContext, CancelSwitch::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
                )
    }

    fun recreateChannel() {
        getManager().deleteNotificationChannel(channelID2)

        val channel2 = NotificationChannel(channelID2, "Theme Notification", NotificationManager.IMPORTANCE_HIGH)
        channel2.lightColor = Color.BLUE
        channel2.enableVibration(true)

        getManager().createNotificationChannel(channel2)
    }

    /*@TargetApi(Build.VERSION_CODES.O)

    fun generalNotification(title: String, message: String): NotificationCompat.Builder {
        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(applicationContext, channelID2)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
            .setColor(Color.parseColor("#2C7DE8"))
            .setAutoCancel(true)
            .setContentIntent(openIntent)
    }

    fun lowerGeneralNotification(title: String, message: String): Notification.Builder {
        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return Notification.Builder(applicationContext)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
            .setColor(Color.parseColor("#2C7DE8"))
            .setPriority(Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
    }

    fun forceStartServiceNotification(): NotificationCompat.Builder {
        val intent = Intent(applicationContext, ForceStartService::class.java)
        val actionIntent = PendingIntent.getBroadcast(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(applicationContext, channelID2)
            .setContentTitle("Error Flip Service")
            .setContentText("User required to restart Flip Service. Tap on \"Force Start\" to try again")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
            .setColor(Color.parseColor("#2C7DE8"))
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_fiber_smart_record_black_24dp, "Force Start", actionIntent)
    }

    fun lowerForceStartServiceNotification(): Notification.Builder {
        val intent = Intent(applicationContext, ForceStartService::class.java)
        val actionIntent = PendingIntent.getBroadcast(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val actIntent = Intent(applicationContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openIntent = PendingIntent.getActivity(applicationContext, 0, actIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return Notification.Builder(applicationContext)
            .setContentTitle("Error Flip Service")
            .setContentText("User required to restart Flip Service. Tap on \"Force Start\" to try again")
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
            .setColor(Color.parseColor("#2C7DE8"))
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_fiber_smart_record_black_24dp, "Force Start", actionIntent)
    }*/
}