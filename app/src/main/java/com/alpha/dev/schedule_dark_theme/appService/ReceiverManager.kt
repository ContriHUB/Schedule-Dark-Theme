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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.alpha.dev.schedule_dark_theme.*
import java.util.*

class ReceiverManager(context: Context) {

    private val ctx = context

    companion object {
        const val TAG = "Receiver Manager"

        const val LIGHT_ID = -0x45
        const val DARK_ID = -0x1A4
        const val OBSERVER_ID = -0x30F
        const val REPEAT_INTERVAL = 24 * 60 * 60 * 1000L
    }

    private fun getPendingIntent(id: Int): PendingIntent = PendingIntent.getBroadcast(ctx, id, intent(id), PendingIntent.FLAG_UPDATE_CURRENT)

    private fun intent(id: Int): Intent = Intent(ctx, when (id) {
        LIGHT_ID -> NotifyLightTheme::class.java
        DARK_ID -> NotifyDarkTheme::class.java
        else -> OperationObserver::class.java
    })

    fun isReceiverRunning(id: Int): Boolean = PendingIntent.getBroadcast(ctx, id, intent(id), PendingIntent.FLAG_NO_CREATE) != null

    fun cancelReceiver(id: Int) {
        val receiver = getPendingIntent(id)
        receiver.cancel()
        getAlarmManager(ctx).cancel(receiver)
    }

    fun createOrRecreateOperation(id: Int, triggerTime: Long) {
        if (isReceiverRunning(id)) {
            log(TAG, "createOperation: id -> $id ; was running", ctx)
            cancelReceiver(id)
            log(TAG, "createOperation: id -> $id ; was cancelled", ctx)
        }
        Handler().postDelayed({
            getAlarmManager(ctx).setRepeating(AlarmManager.RTC_WAKEUP, getLatestMilli(triggerTime), REPEAT_INTERVAL, getPendingIntent(id))
            log(TAG, "createOperation: id -> $id ; was scheduled", ctx)
            checkOnObserver()
        }, 700)
    }

    fun checkOnObserver() {
        log(TAG, "checkOnObserve: starting", ctx)
        if (!isReceiverRunning(OBSERVER_ID)) {
            log(TAG, "checkOnObserve: Operation Observer not running", ctx)
            getAlarmManager(ctx).setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000, getPendingIntent(OBSERVER_ID))
            log(TAG, "checkOnObserve: Operation Observer scheduled", ctx)
            makeToast(ctx, "Dark theme scheduled")
        } else log(TAG, "checkOnObserver: already running ...", ctx)
    }

    fun getLatestMilli(oldMilli: Long): Long {
        val oldC = Calendar.getInstance().putTimeInMillis(oldMilli)
        val hour = oldC.get(Calendar.HOUR_OF_DAY)
        val minute = oldC.get(Calendar.MINUTE)

        val newMilli = Calendar.getInstance().put(Calendar.HOUR_OF_DAY, hour).put(Calendar.MINUTE, minute).timeInMillis
        log(TAG, "getLatestMilli: time milli updated from $oldMilli -> $newMilli ; hour => $hour , min => $minute", ctx)
        return newMilli
    }
}