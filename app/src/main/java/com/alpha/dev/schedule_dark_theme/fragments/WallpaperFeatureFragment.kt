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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.alpha.dev.fastscroller.FastScrollScrollView
import com.alpha.dev.fastscroller.FastScrollerBuilder
import com.alpha.dev.fastscroller.ScrollingViewOnApplyWindowInsetsListener
import com.alpha.dev.materialdialog.MaterialAlertDialog
import com.alpha.dev.schedule_dark_theme.*
import com.alpha.dev.schedule_dark_theme.appService.services.ServiceObserver
import com.alpha.dev.schedule_dark_theme.appService.services.WallpaperService
import com.google.android.material.card.MaterialCardView
import java.io.File

class WallpaperFeatureFragment(context: Context, private val activity: AppCompatActivity) : Fragment() {

    private val ctx = context

    private val pref by lazy { PreferenceHelper(ctx) }
    private lateinit var scv: FastScrollScrollView

    private lateinit var featureToggle: SwitchCompat

    private lateinit var lightWallpaper: AppCompatImageView
    private lateinit var lEmpty: AppCompatImageView
    private lateinit var darkWallpaper: AppCompatImageView
    private lateinit var dEmpty: AppCompatImageView

    private lateinit var lRemove: MaterialCardView
    private lateinit var dRemove: MaterialCardView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_wallpaper_feature, container, false)

        initiateViews(view)

        featureToggle.setOnCheckedChangeListener { _, isChecked -> togglePref(isChecked) }
        featureToggle.isChecked = pref.getBoolean(WALL_FEATURE, false)

        lightWallpaper.setOnClickListener {
            if (!storagePermissionGranted(ctx)) {
                getStoragePermission(ctx, activity)
            } else {
                sImgView = lightWallpaper
                sEmptyImg = lEmpty
                sCardV = lRemove
                sCheck = featureToggle

                activity.startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), WALL_RETRIEVE_LIGHT)
            }
        }
        darkWallpaper.setOnClickListener {
            if (!storagePermissionGranted(ctx)) {
                getStoragePermission(ctx, activity)
            } else {
                sImgView = darkWallpaper
                sEmptyImg = dEmpty
                sCardV = dRemove
                sCheck = featureToggle

                activity.startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), WALL_RETRIEVE_DARK)
            }
        }

        if (imageExists(ctx, WALL_LIGHT)) {
            val lB = getThumbImage(ctx, WALL_LIGHT)
            if (lB != null) {
                lightWallpaper.setImageBitmap(lB)
                lEmpty.visibility = View.GONE
                lRemove.visibility = View.VISIBLE
            }
        }
        if (imageExists(ctx, WALL_DARK)) {
            val dB = getThumbImage(ctx, WALL_DARK)
            if (dB != null) {
                darkWallpaper.setImageBitmap(dB)
                dEmpty.visibility = View.GONE
                dRemove.visibility = View.VISIBLE
            }
        }

        scv.setOnApplyWindowInsetsListener(ScrollingViewOnApplyWindowInsetsListener())
        FastScrollerBuilder(scv).useMd2Style().build()

        dRemove.setOnClickListener {
            MaterialAlertDialog.Builder(ctx)
                    .setTitle("Confirmation")
                    .setMessage("Remove wallpaper for dark theme ?")
                    .setPositiveButton("Remove") {
                        if (imageExists(ctx, WALL_DARK)) {
                            if (File(ctx.getDir(DIR, Context.MODE_PRIVATE), FILE_WALL_DARK).delete()) {
                                darkWallpaper.setImageBitmap(null)
                                dRemove.visibility = View.GONE
                                dEmpty.visibility = View.VISIBLE

                                featureToggle.isChecked = imageExists(ctx, WALL_LIGHT) || imageExists(ctx, WALL_DARK)
                            }
                        }
                        it.dismiss()
                    }
                .setNegativeButton("Cancel") { it.dismiss() }.build()
        }
        lRemove.setOnClickListener {
            MaterialAlertDialog.Builder(ctx)
                    .setTitle("Confirmation")
                    .setMessage("Remove wallpaper for light theme ?")
                    .setPositiveButton("Remove") {
                        if (imageExists(ctx, WALL_LIGHT)) {
                            if (File(ctx.getDir(DIR, Context.MODE_PRIVATE), FILE_WALL_LIGHT).delete()) {
                                lightWallpaper.setImageBitmap(null)
                                lRemove.visibility = View.GONE
                                lEmpty.visibility = View.VISIBLE

                                featureToggle.isChecked = imageExists(ctx, WALL_LIGHT) || imageExists(ctx, WALL_DARK)
                            }
                        }
                        it.dismiss()
                    }
                .setNegativeButton("Cancel") { it.dismiss() }.build()
        }

        return view
    }

    private fun togglePref(checked: Boolean) {
        if (checked) {
            if (pref.getBoolean(ENABLE_FEATURE, false)) {
                systemToast(ctx, "Both feature cannot be enabled at once")
                featureToggle.isChecked = false
                return
            }
            if (!imageExists(ctx, WALL_LIGHT) && !imageExists(ctx, WALL_DARK)) {
                systemToast(ctx, "Please select wallpaper(s)")
                featureToggle.isChecked = false
                return
            }
            pref.putBoolean(WALL_FEATURE, checked)
            if (!ServiceObserver.getWallpaperRunning()) {
                ctx.startService(Intent(ctx, WallpaperService::class.java))
            }
        } else {
            pref.putBoolean(WALL_FEATURE, checked)
            if (ServiceObserver.getWallpaperRunning()) {
                ctx.stopService(Intent(ctx, WallpaperService::class.java))
            }
        }
    }

    private fun initiateViews(view: View) {
        featureToggle = view.findViewById(R.id.enableFeature)

        lightWallpaper = view.findViewById(R.id.light_wallpaper_img)
        darkWallpaper = view.findViewById(R.id.dark_wallpaper_img)
        lEmpty = view.findViewById(R.id.l_empty)
        dEmpty = view.findViewById(R.id.d_empty)

        lRemove = view.findViewById(R.id.l_remove)
        dRemove = view.findViewById(R.id.d_remove)

        scv = view.findViewById(R.id.w_f_scv)
    }
}