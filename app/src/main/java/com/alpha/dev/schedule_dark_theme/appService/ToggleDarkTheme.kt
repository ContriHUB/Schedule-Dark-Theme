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
import com.alpha.dev.schedule_dark_theme.DARK
import com.alpha.dev.schedule_dark_theme.toggleTheme

class ToggleDarkTheme: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        toggleTheme(context, DARK)
        NotificationHelper(context).getManager().cancel(3)
    }
}