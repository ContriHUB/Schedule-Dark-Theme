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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alpha.dev.fastscroller.FastScrollerBuilder
import com.alpha.dev.fastscroller.ScrollingViewOnApplyWindowInsetsListener
import com.alpha.dev.materialdialog.MaterialAlertDialog
import com.alpha.dev.materialdialog.MaterialDialogInterface
import kotlinx.android.synthetic.main.activity_log.*
import java.io.File
import java.io.IOException

class LogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        scv_log.setOnApplyWindowInsetsListener(ScrollingViewOnApplyWindowInsetsListener())
        FastScrollerBuilder(scv_log).useMd2Style().build()

        var logsTxt = ""
        val file = File(filesDir, "sch_log.txt")
        file.forEachLine { line -> logsTxt += "$line\n" }

        showLog.text = logsTxt

        copyLog.setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Logs", logsTxt)

            clipboardManager.setPrimaryClip(clipData)
            systemToast(this, "Logs Copied", Toast.LENGTH_LONG, R.drawable.ic_content_copy_black_24dp)
        }

        clearLog.setOnClickListener {
            MaterialAlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Clear and delete logs ?")
                    .setPositiveButton("Delete", MaterialDialogInterface.OnPositiveClickListener {
                        try {
                            if (file.delete()) {
                                it.dismiss()
                                super.onBackPressed()
                            } else {
                                it.dismiss()
                                systemToast(this, "Error clearing logs")
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            it.dismiss()
                            systemToast(this, "Error clearing logs\n${e.message}")
                        }
                    })
                    .setNegativeButton("Cancel", MaterialDialogInterface.OnNegativeClickListener {
                        it.dismiss()
                    }).build()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}