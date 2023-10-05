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

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialog
import com.alpha.dev.schedule_dark_theme.databinding.ThemeDialogBinding

class ThemeDialog(context: Context) : AppCompatDialog(context) {

    private val ctx = context

    private lateinit var binding: ThemeDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ThemeDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(R.drawable.bg_recent)

        when (PreferenceHelper(ctx).getInt(THEME, defTheme)) {
            AppCompatDelegate.MODE_NIGHT_YES -> binding.darkCheck.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> binding.lightCheck.isChecked = true
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.defaultCheck.isChecked = true
        }

        binding.lightCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                log("Theme", "App Theme -> MODE_NIGHT_NO", ctx)
                saveTheme(AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                dismiss()
            }
        }

        binding.darkCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                log("Theme", "App Theme -> MODE_NIGHT_YES", ctx)
                saveTheme(AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                dismiss()
            }
        }

        binding.defaultCheck.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                log("Theme", "App Theme -> MODE_NIGHT_FOLLOW_SYSTEM", ctx)
                saveTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                dismiss()
            }
        }
    }

    private fun saveTheme(int: Int) {
        PreferenceHelper(ctx).putInt(THEME, int)
    }
}