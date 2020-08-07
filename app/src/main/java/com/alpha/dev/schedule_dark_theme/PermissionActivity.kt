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

import android.content.*
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_permission.*

class PermissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTheme = PreferenceHelper(this).getInt(THEME, defTheme)
        AppCompatDelegate.setDefaultNightMode(mTheme)
        check()
        setContentView(R.layout.activity_permission)

        devOptionSteps.text = Html.fromHtml(enableDeveloperOptions, Html.FROM_HTML_MODE_COMPACT)
        adbConnectSteps.text = Html.fromHtml(connectToUSB, Html.FROM_HTML_MODE_COMPACT)
        commandSteps.text = Html.fromHtml(commandStep, Html.FROM_HTML_MODE_COMPACT)


        checkPermission.setOnClickListener {
            if (permissionGranted(this)) {
                makeToast(this, "Granted", Toast.LENGTH_LONG)
                log("Permission Activity", "Permission Granted (-> this was first time for app)", this)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                makeToast(this, "Not granted yet", Toast.LENGTH_LONG)
            }
        }

        downloadADB.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ADBDownloadLink)).setPackage("com.android.chrome"))
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                makeToast(this, "No application found to open link", Toast.LENGTH_LONG)
            }
        }

        copyCommand.setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("adb command", "adb shell pm grant com.alpha.dev.schedule_dark_theme android.permission.WRITE_SECURE_SETTINGS")

            clipboardManager.setPrimaryClip(clipData)
            systemToast(this, "Command Copied", imageResource = R.drawable.ic_content_copy_black_24dp)
        }
    }

    private fun check() {
        if (permissionGranted(this)) {
            log("Permission Activity", "Already Granted (-> moving to main)", this)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}
