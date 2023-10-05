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
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import android.view.Display
import com.alpha.dev.schedule_dark_theme.DARK
import com.alpha.dev.schedule_dark_theme.DEFAULT_ENABLE_TIME
import com.alpha.dev.schedule_dark_theme.LIGHT
import com.alpha.dev.schedule_dark_theme.LOCK_PREF
import com.alpha.dev.schedule_dark_theme.PreferenceHelper
import com.alpha.dev.schedule_dark_theme.SUNRISE_TIME
import com.alpha.dev.schedule_dark_theme.SUNSET_TIME
import com.alpha.dev.schedule_dark_theme.TIME_DISABLE
import com.alpha.dev.schedule_dark_theme.TIME_ENABLE
import com.alpha.dev.schedule_dark_theme.TIME_SLOTS
import com.alpha.dev.schedule_dark_theme.TRIGGER_TIME
import com.alpha.dev.schedule_dark_theme.appService.NotificationHelper
import com.alpha.dev.schedule_dark_theme.log
import com.alpha.dev.schedule_dark_theme.makeToast
import com.alpha.dev.schedule_dark_theme.putTimeInMillis
import com.alpha.dev.schedule_dark_theme.toggleTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ThemeService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    private val helper by lazy { NotificationHelper(applicationContext) }
    private val pref by lazy { PreferenceHelper(applicationContext) }

    private val themeThread by lazy { HandlerThread("AutoThemeService", Process.THREAD_PRIORITY_BACKGROUND) }
    private var handler: Handler? = null
    private val runnable = object : Runnable {
        override fun run() {
            try {
                CoroutineScope(Dispatchers.Default).launch(Dispatchers.Default) {
                    val mode = pref.getInt(TRIGGER_TIME, TIME_SLOTS)
                    if (mode != -1) {
                        val dark = getClockHands(
                            when (mode) {
                                TIME_SLOTS -> pref.getLong(TIME_ENABLE, DEFAULT_ENABLE_TIME)
                                else -> pref.getLong(SUNSET_TIME, DEFAULT_ENABLE_TIME)
                            }
                        )

                        val light = getClockHands(
                            when (mode) {
                                TIME_SLOTS -> pref.getLong(TIME_DISABLE, DEFAULT_ENABLE_TIME)
                                else -> pref.getLong(SUNRISE_TIME, DEFAULT_ENABLE_TIME)
                            }
                        )

                        val current = getClockHands(System.currentTimeMillis())

                        // If current and dark hour hands and minute hands are same
                        if (dark[0] == current[0] && dark[1] == current[1]) {
                            withContext(Dispatchers.Main.immediate) { triggerDark() }
                            return@launch
                        }

                        // If current and light hour hands and minute hands are same
                        if (light[0] == current[0] && light[1] == current[1]) {
                            withContext(Dispatchers.Main.immediate) { triggerLight() }
                            return@launch
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                handler?.postDelayed(this, 60 * 1000 /* One minute */)
            }
        }
    }

    override fun onCreate() {
        themeThread.start()
        handler = Handler(themeThread.looper)
        ServiceObserver.themeService.postValue(true)
        startForeground(1, NotificationHelper(applicationContext).serviceNotification().build())

        makeToast(applicationContext, "Theme service started")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceObserver.themeService.postValue(true)
        handler?.post(runnable)
        return START_STICKY
    }

    /**
     * Takes ...
     * @param milli [Long]
     *
     * and ...
     * @return clock hands i.e Hour and Minute through [Array]
     */
    private fun getClockHands(milli: Long): Array<Int> {
        val c = Calendar.getInstance().putTimeInMillis(milli)
        return arrayOf(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }

    private fun triggerDark() {
        val displayManager = applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        if (applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_YES) {
            if (displayManager.getDisplay(0).state == Display.STATE_ON) {
                if (pref.getBoolean(LOCK_PREF, false)) {
                    helper.recreateChannel()
                    helper.getManager().notify(3, helper.darkRunningNotification().build())
                } else toggleTheme(applicationContext, DARK)
            } else toggleTheme(applicationContext, DARK)
        } else log("NotifyDarkTheme", "System theme was already DARK", applicationContext)
    }

    private fun triggerLight() {
        val displayManager = applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        if (applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_NO) {
            if (displayManager.getDisplay(0).state == Display.STATE_ON) {
                if (pref.getBoolean(LOCK_PREF, false)) {
                    helper.recreateChannel()
                    helper.getManager().notify(3, helper.lightRunningNotification().build())
                } else toggleTheme(applicationContext, LIGHT)
            } else toggleTheme(applicationContext, LIGHT)
        } else log("NotifyLightTheme", "System theme was already LIGHT", applicationContext)
    }

    override fun onDestroy() {
        try {
            handler?.removeCallbacks(runnable)
            themeThread.quitSafely()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        handler = null
        ServiceObserver.themeService.postValue(false)

        makeToast(applicationContext, "Theme service stopped")
        System.gc()
        super.onDestroy()
    }
}