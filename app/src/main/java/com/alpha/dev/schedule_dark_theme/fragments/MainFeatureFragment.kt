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

package com.alpha.dev.schedule_dark_theme.fragments

import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.alpha.dev.fastscroller.FastScrollScrollView
import com.alpha.dev.fastscroller.FastScrollerBuilder
import com.alpha.dev.fastscroller.ScrollingViewOnApplyWindowInsetsListener
import com.alpha.dev.materialdialog.MaterialAlertDialog
import com.alpha.dev.materialdialog.MaterialDialogInterface
import com.alpha.dev.schedule_dark_theme.*
import com.alpha.dev.schedule_dark_theme.appService.Interfaces
import com.alpha.dev.schedule_dark_theme.appService.services.ServiceObserver
import com.alpha.dev.schedule_dark_theme.appService.services.ThemeService
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.util.*

class MainFeatureFragment(context: Context, private val activity: AppCompatActivity) : Fragment() {

    private val lTag = "MainFeatureFragment"

    private val ctx = context

    private val pref by lazy { PreferenceHelper(context) }
    private lateinit var scv: FastScrollScrollView

    private lateinit var enableTime: AppCompatTextView
    private lateinit var disableTime: AppCompatTextView
    private lateinit var currentTrigger: AppCompatTextView
    private lateinit var currentTheme: AppCompatTextView

    private lateinit var dTime: MaterialCardView
    private lateinit var lTime: MaterialCardView
    private lateinit var sunRSet: MaterialCardView
    private lateinit var changeTheme: MaterialCardView
    private lateinit var triggerChanger: MaterialCardView
    private lateinit var lRemove: MaterialCardView
    private lateinit var dRemove: MaterialCardView

    private lateinit var lockSet: SwitchCompat
    private lateinit var switchFeature: SwitchCompat
    private lateinit var toastToggle: SwitchCompat
    private lateinit var wallCheck: SwitchCompat

    private lateinit var lightWallpaper: AppCompatImageView
    private lateinit var darkWallpaper: AppCompatImageView
    private lateinit var lEmpty: AppCompatImageView
    private lateinit var dEmpty: AppCompatImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_feature, container, false)

        initiateViews(view)

        enableTime.text = getTimeString(pref.getLong(TIME_ENABLE, DEFAULT_ENABLE_TIME))
        disableTime.text = getTimeString(pref.getLong(TIME_DISABLE, DEFAULT_DISABLE_TIME))

        currentTheme.text = when (pref.getInt(THEME, defTheme)) {
            AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.dark)
            AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.light)
            else -> getString(R.string.system_default)
        }
        val mode = pref.getInt(TRIGGER_TIME, TIME_SLOTS)
        currentTrigger.text = when (mode) {
            TIME_SLOTS -> getString(R.string.time_slots)
            else -> getString(R.string.sunrise_sunset)
        }
        when (mode) {
            TIME_SLOTS -> {
                dTime.visibility = View.VISIBLE
                lTime.visibility = View.VISIBLE
                sunRSet.visibility = View.GONE
            }
            SUN_SET_RISE -> {
                dTime.visibility = View.GONE
                lTime.visibility = View.GONE
                sunRSet.visibility = View.VISIBLE
            }
        }

        lockSet.isChecked = pref.getBoolean(LOCK_PREF, false)
        switchFeature.isChecked = pref.getBoolean(ENABLE_FEATURE, false)
        toastToggle.isChecked = pref.getBoolean(TOAST_PREF, true)
        wallCheck.isChecked = pref.getBoolean(WALLPAPER_ENABLED, false)

        if (switchFeature.isChecked) {
            if (!ServiceObserver.getThemeRunning()) {
                ctx.startService(Intent(ctx, ThemeService::class.java))
            }
        }

        lockSet.setOnCheckedChangeListener { _, isChecked -> toggleLockPref(isChecked) }
        switchFeature.setOnCheckedChangeListener { _, isChecked -> toggleFeature(isChecked) }
        toastToggle.setOnCheckedChangeListener { _, isChecked -> pref.putBoolean(TOAST_PREF, isChecked) }
        wallCheck.setOnCheckedChangeListener { _, isChecked -> pref.putBoolean(WALLPAPER_ENABLED, isChecked) }

        scv.setOnApplyWindowInsetsListener(ScrollingViewOnApplyWindowInsetsListener())
        FastScrollerBuilder(scv).useMd2Style().build()

        changeTheme.setOnClickListener {
            ThemeDialog(ctx).show()
        }

        dTime.setOnClickListener {
            TimePicker(ctx, DARK, Interfaces.OnTimeChangeListener {
                enableTime.text = getTimeString(it)
            }).show()
        }

        lTime.setOnClickListener {
            TimePicker(ctx, LIGHT, Interfaces.OnTimeChangeListener {
                disableTime.text = getTimeString(it)
            }).show()
        }

        triggerChanger.setOnClickListener {
            TimeTrigger(ctx, Interfaces.OnTriggerChangeListener {
                currentTrigger.text = it
            }, dTime, lTime, sunRSet).show()
        }

        lightWallpaper.setOnClickListener {
            if (!storagePermissionGranted(ctx)) {
                getStoragePermission(ctx, activity)
            } else {
                sImgView = lightWallpaper
                sEmptyImg = lEmpty
                sCardV = lRemove
                sCheck = wallCheck

                activity.startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), IMAGE_RETRIEVE_LIGHT)
            }
        }

        darkWallpaper.setOnClickListener {
            if (!storagePermissionGranted(ctx)) {
                getStoragePermission(ctx, activity)
            } else {
                sImgView = darkWallpaper
                sEmptyImg = dEmpty
                sCardV = dRemove
                sCheck = wallCheck

                activity.startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), IMAGE_RETRIEVE_DARK)
            }
        }

        if (imageExists(ctx, LIGHT)) {
            val lB = getThumbImage(ctx, LIGHT)
            if (lB != null) {
                log(lTag, "$FILE_LIGHT exist -> $COMPRESS_LIGHT is loaded", ctx)
                lightWallpaper.setImageBitmap(lB)
                lEmpty.visibility = View.GONE
                lRemove.visibility = View.VISIBLE
            }
        }
        if (imageExists(ctx, DARK)) {
            val dB = getThumbImage(ctx, DARK)
            if (dB != null) {
                log(lTag, "$FILE_DARK exist -> $COMPRESS_DARK is loaded", ctx)
                darkWallpaper.setImageBitmap(dB)
                dEmpty.visibility = View.GONE
                dRemove.visibility = View.VISIBLE
            }
        }

        lRemove.setOnClickListener {
            MaterialAlertDialog.Builder(ctx)
                    .setTitle("Confirmation")
                    .setMessage("Remove wallpaper for light theme ?")
                    .setPositiveButton("Remove", MaterialDialogInterface.OnPositiveClickListener {
                        if (imageExists(ctx, LIGHT)) {
                            if (File(ctx.getDir(DIR, Context.MODE_PRIVATE), FILE_LIGHT).delete()) {
                                log("MainFrag/LightRemove", "Clicked for positive", ctx)
                                lightWallpaper.setImageBitmap(null)
                                lRemove.visibility = View.GONE
                                lEmpty.visibility = View.VISIBLE

                                wallCheck.isChecked = imageExists(ctx, LIGHT) || imageExists(ctx, DARK)
                            }
                        }
                        it.dismiss()
                    })
                    .setNegativeButton("Cancel", MaterialDialogInterface.OnNegativeClickListener {
                        log("MainFrag/LightRemove", "Clicked for negative", ctx)
                        it.dismiss()
                    }).build()
        }
        dRemove.setOnClickListener {
            MaterialAlertDialog.Builder(ctx)
                    .setTitle("Confirmation")
                    .setMessage("Remove wallpaper for dark theme ?")
                    .setPositiveButton("Remove", MaterialDialogInterface.OnPositiveClickListener {
                        if (imageExists(ctx, DARK)) {
                            if (File(ctx.getDir(DIR, Context.MODE_PRIVATE), FILE_DARK).delete()) {
                                log("MainFrag/DarkRemove", "Clicked for positive", ctx)
                                darkWallpaper.setImageBitmap(null)
                                dRemove.visibility = View.GONE
                                dEmpty.visibility = View.VISIBLE

                                wallCheck.isChecked = imageExists(ctx, LIGHT) || imageExists(ctx, DARK)
                            }
                        }
                        it.dismiss()
                    })
                    .setNegativeButton("Cancel", MaterialDialogInterface.OnNegativeClickListener {
                        log("MainFrag/DarkRemove", "Clicked for negative", ctx)
                        it.dismiss()
                    }).build()
        }

        return view
    }

    private fun initiateViews(view: View) {
        //--> FastScroll
        scv = view.findViewById(R.id.m_f_scv)

        //--> TextView
        enableTime = view.findViewById(R.id.enableTime)
        disableTime = view.findViewById(R.id.disableTime)
        currentTheme = view.findViewById(R.id.currentTheme)
        currentTrigger = view.findViewById(R.id.currentTrigger)

        //--> CardView
        sunRSet = view.findViewById(R.id.sunR_set)
        dTime = view.findViewById(R.id.dTime)
        lTime = view.findViewById(R.id.lTime)
        changeTheme = view.findViewById(R.id.changeTheme)
        triggerChanger = view.findViewById(R.id.triggerChanger)
        lRemove = view.findViewById(R.id.l_remove)
        dRemove = view.findViewById(R.id.d_remove)

        //--> CheckBox
        lockSet = view.findViewById(R.id.lockSet)
        switchFeature = view.findViewById(R.id.switchFeature)
        toastToggle = view.findViewById(R.id.toastToggle)
        wallCheck = view.findViewById(R.id.wallCheck)

        //--> ImageView
        lightWallpaper = view.findViewById(R.id.light_wallpaper_img)
        darkWallpaper = view.findViewById(R.id.dark_wallpaper_img)
        lEmpty = view.findViewById(R.id.l_empty)
        dEmpty = view.findViewById(R.id.d_empty)
    }

    private fun toggleFeature(boolean: Boolean) {
        log("CHECK", "$boolean", ctx)
        if (boolean) {
            if (pref.getBoolean(WALL_FEATURE, false)) {
                systemToast(ctx, "Both features cannot be enabled at once")
                switchFeature.isChecked = false
                return
            }
            ctx.startService(Intent(ctx, ThemeService::class.java))
            pref.putBoolean(ENABLE_FEATURE, boolean)
        } else {
            ctx.stopService(Intent(ctx, ThemeService::class.java))
            pref.putBoolean(ENABLE_FEATURE, boolean)
        }
    }

    private fun toggleLockPref(boolean: Boolean) {
        pref.putBoolean(LOCK_PREF, boolean)
        makeToast(ctx, "Settings Applied")
    }

    private fun getTimeString(time: Long): String {
        val usual = DateFormat.getTimeInstance(DateFormat.SHORT).format(time)

        return if (android.text.format.DateFormat.is24HourFormat(ctx)) {
            try {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(SimpleDateFormat("hh:mm aa", Locale.getDefault()).parse(usual))
            } catch (e: Exception) {
                e.printStackTrace()
                val c = Calendar.getInstance().putTimeInMillis(time)
                val hour = c[Calendar.HOUR_OF_DAY]
                val min = c[Calendar.MINUTE]
                var toggleTime = ""
                toggleTime += if (hour < 10) {
                    "0$hour"
                } else "$hour"
                toggleTime += ":"
                toggleTime += if (min < 10) {
                    "0$min"
                } else "$min"
                toggleTime
            }
        } else {
            usual
        }
    }
}