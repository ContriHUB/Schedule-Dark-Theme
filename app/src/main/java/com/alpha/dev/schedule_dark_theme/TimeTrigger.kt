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

package com.alpha.dev.schedule_dark_theme

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import com.alpha.dev.schedule_dark_theme.appService.Interfaces
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.trigger_layout.*
import java.util.*

class TimeTrigger(context: Context, private val listener: Interfaces.OnTriggerChangeListener,
                  private val dCard: MaterialCardView, private val lCard: MaterialCardView, private val sunCard: MaterialCardView) : AppCompatDialog(context) {

    private val ctx = context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trigger_layout)
        window!!.setBackgroundDrawableResource(R.drawable.bg_recent)

        val pref = PreferenceHelper(ctx)
        when (pref.getInt(TRIGGER_TIME, TIME_SLOTS)) {
            TIME_SLOTS -> timeCheck.isChecked = true
            SUN_SET_RISE -> sunCheck.isChecked = true
        }

        timeCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                pref.putInt(TRIGGER_TIME, TIME_SLOTS)
                listener.onChange(ctx.getString(R.string.time_slots))

                sunCard.visibility = View.GONE
                dCard.visibility = View.VISIBLE
                lCard.visibility = View.VISIBLE

                dismiss()
            }
        }

        sunCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                pref.putInt(TRIGGER_TIME, SUN_SET_RISE)
                listener.onChange(ctx.getString(R.string.sunrise_sunset))

                sunCard.visibility = View.VISIBLE
                dCard.visibility = View.GONE
                lCard.visibility = View.GONE

                val enableMilli = Calendar.getInstance().put(Calendar.HOUR_OF_DAY, 19).put(Calendar.MINUTE, 0).put(Calendar.SECOND, 0).timeInMillis
                val disableMilli = Calendar.getInstance().put(Calendar.HOUR_OF_DAY, 7).put(Calendar.MINUTE, 0).put(Calendar.SECOND, 0).timeInMillis

                pref.putLong(SUNSET_TIME, enableMilli)
                pref.putLong(SUNRISE_TIME, disableMilli)

                dismiss()
            }
        }
    }
}