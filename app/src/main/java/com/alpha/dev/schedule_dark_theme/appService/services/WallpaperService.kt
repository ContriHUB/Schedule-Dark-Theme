/*
 * Copyright (c) 2023, Shashank Verma <shashank.verma2002@gmail.com>
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

package com.alpha.dev.schedule_dark_theme.appService.services

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import com.alpha.dev.schedule_dark_theme.WALL_DARK
import com.alpha.dev.schedule_dark_theme.WALL_LIGHT
import com.alpha.dev.schedule_dark_theme.appService.NotificationHelper
import com.alpha.dev.schedule_dark_theme.makeToast
import com.alpha.dev.schedule_dark_theme.updateWallpaper

class WallpaperService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    private var currentTheme = -1
    private val wallpaperThread by lazy { HandlerThread("AutoWallpaperService", Process.THREAD_PRIORITY_BACKGROUND) }
    private var handler: Handler? = null
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            try {
                when (applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        if (currentTheme != Configuration.UI_MODE_NIGHT_YES) {
                            updateWallpaper(applicationContext, WALL_DARK)
                            makeToast(applicationContext, "Wallpaper changed")

                            currentTheme = Configuration.UI_MODE_NIGHT_YES
                        }
                    }

                    Configuration.UI_MODE_NIGHT_NO -> {
                        if (currentTheme != Configuration.UI_MODE_NIGHT_NO) {
                            updateWallpaper(applicationContext, WALL_LIGHT)
                            makeToast(applicationContext, "Wallpaper changed")

                            currentTheme = Configuration.UI_MODE_NIGHT_NO
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                handler?.postDelayed(this, 500)
            }
        }
    }

    override fun onCreate() {
        wallpaperThread.start()
        handler = Handler(wallpaperThread.looper)
        ServiceObserver.wallpaperService.postValue(true)
        startForeground(1, NotificationHelper(applicationContext).serviceNotification().build())

        makeToast(applicationContext, "Wallpaper Service started")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceObserver.wallpaperService.postValue(true)
        handler?.post(runnable)
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            handler?.removeCallbacks(runnable)
            wallpaperThread.quitSafely()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        handler = null
        ServiceObserver.wallpaperService.postValue(false)

        makeToast(applicationContext, "Wallpaper Service stopped")
        System.gc()
        super.onDestroy()
    }
}