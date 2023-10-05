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

package com.alpha.dev.schedule_dark_theme

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alpha.dev.schedule_dark_theme.databinding.ActivityLogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class LogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var logsTxt = ""
        val file = File(filesDir, "sch_log.txt")
        binding.lPr.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            file.forEachLine { line -> logsTxt += "$line\n" }
            withContext(Dispatchers.Main) {
                binding.showLog.text = logsTxt
                binding.lPr.visibility = View.GONE
            }
        }

        binding.copyLog.setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Logs", logsTxt)

            clipboardManager.setPrimaryClip(clipData)
            systemToast(this, "Logs Copied", Toast.LENGTH_LONG, R.drawable.ic_content_copy_black_24dp)
        }

        binding.clearLog.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Confirmation")
                .setMessage("Clear and delete logs ?")
                .setPositiveButton("Delete") { dialog, _ ->
                    try {
                        if (file.delete()) {
                            dialog.dismiss()
                            super.onBackPressed()
                        } else {
                            dialog.dismiss()
                            systemToast(this, "Error clearing logs")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        dialog.dismiss()
                        systemToast(this, "Error clearing logs\n${e.message}")
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}