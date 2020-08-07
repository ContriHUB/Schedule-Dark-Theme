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
import android.hardware.display.DisplayManager
import android.view.Display
import com.alpha.dev.schedule_dark_theme.DARK
import com.alpha.dev.schedule_dark_theme.LOCK_PREF
import com.alpha.dev.schedule_dark_theme.PreferenceHelper
import com.alpha.dev.schedule_dark_theme.toggleTheme

class NotifyDarkTheme : BroadcastReceiver() {

    override fun onReceive(context: Context, p1: Intent) {
        val helper = NotificationHelper(context)
        val displayManager = context.applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        if (displayManager.getDisplay(0).state == Display.STATE_ON) {
            if (PreferenceHelper(context).getBoolean(LOCK_PREF, false)) {
                helper.recreateChannel()
                helper.getManager().notify(3, helper.darkRunningNotification().build())
            } else {
                toggleTheme(context, DARK)
            }
        } else {
            toggleTheme(context, DARK)
        }
    }
}