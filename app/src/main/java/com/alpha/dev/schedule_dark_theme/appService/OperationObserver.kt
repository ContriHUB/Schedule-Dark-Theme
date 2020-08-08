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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alpha.dev.schedule_dark_theme.*

class OperationObserver : BroadcastReceiver() {

    private var message = ""

    override fun onReceive(context: Context, intent: Intent) {
        val manager = ReceiverManager(context)
        val pref = PreferenceHelper(context)

        message = "Output : \n"
        manager.checkOnObserver()

        if (pref.getBoolean(ENABLE_FEATURE, false)) {
            val lightMilli = manager.getLatestMilli(when (pref.getInt(TRIGGER_TIME, TIME_SLOTS)) {
                TIME_SLOTS -> pref.getLong(TIME_DISABLE, DEFAULT_ENABLE_TIME)
                else -> pref.getLong(SUNRISE_TIME, DEFAULT_ENABLE_TIME)
            })
            val darkMilli = manager.getLatestMilli(when (pref.getInt(TRIGGER_TIME, TIME_SLOTS)) {
                TIME_SLOTS -> pref.getLong(TIME_ENABLE, DEFAULT_ENABLE_TIME)
                else -> pref.getLong(SUNSET_TIME, DEFAULT_ENABLE_TIME)
            })

            val currentMilli = System.currentTimeMillis()

            if (currentMilli in lightMilli..darkMilli) {    // current time is between light and dark time
                if (lightMilli > darkMilli) {   // light is still ahead of dark
                    // enable dark theme AND light theme
                    checkDark(manager, darkMilli)
                    checkLight(manager, lightMilli)
                } else {
                    if (darkMilli > lightMilli) {
                        // enable light theme AND dark theme
                        checkLight(manager, lightMilli)
                        checkDark(manager, darkMilli)
                    }
                }
            } else if (currentMilli > lightMilli && currentMilli > darkMilli) {    // light and dark, current time had already part both
                if (lightMilli > darkMilli) {
                    // enable light theme and postpone the dark theme to a day ahead
                    checkLight(manager, lightMilli)

                    // adding a day
                    checkDark(manager, darkMilli, true)
                } else {
                    if (darkMilli > lightMilli) {
                        // enable dark theme and postpone the light theme to a day ahead
                        checkDark(manager, darkMilli)

                        // adding a day
                        checkLight(manager, lightMilli, true)
                    }
                }
            } else {
                if (currentMilli < lightMilli && currentMilli < darkMilli) {
                    // enable both theme time normally
                    checkLight(manager, lightMilli)
                    checkDark(manager, darkMilli)
                }
            }
        }
        log("\r\n<== Operation Observer ==>\r\n", message, context)
    }

    private fun checkLight(manager: ReceiverManager, disableMilli: Long, needADay: Boolean = false) {
        if (disableMilli != DEFAULT_DISABLE_TIME) {
            if (!manager.isReceiverRunning(ReceiverManager.LIGHT_ID)) {
                message += "Light was not running\n"
                try {
                    message += "Light still tried to cancel\n"
                    manager.cancelReceiver(ReceiverManager.LIGHT_ID)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                message += "Light was recreated again\n"
                message += "Need a day = $needADay\n"
                manager.createOrRecreateOperation(ReceiverManager.LIGHT_ID, if (needADay) disableMilli + ReceiverManager.REPEAT_INTERVAL else disableMilli)
            } else {
                message += "Light was already running\n"
            }
        } else {
            message += "Light time not touched -> disable time was default = $disableMilli \n"
        }
    }

    private fun checkDark(manager: ReceiverManager, enableMilli: Long, needADay: Boolean = false) {
        if (enableMilli != DEFAULT_ENABLE_TIME) {
            if (!manager.isReceiverRunning(ReceiverManager.DARK_ID)) {
                message += "Dark was not running\n"
                try {
                    message += "Dark still tried to cancel\n"
                    manager.cancelReceiver(ReceiverManager.DARK_ID)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                message += "Dark was recreated again\n"
                message += "Needed a day = $needADay\n"
                manager.createOrRecreateOperation(ReceiverManager.DARK_ID, if (needADay) enableMilli + ReceiverManager.REPEAT_INTERVAL else enableMilli)
            } else {
                message += "Dark was already running\n"
            }
        } else {
            message += "Dark time not touched -> enable time was default = $enableMilli\n"
        }
    }
}