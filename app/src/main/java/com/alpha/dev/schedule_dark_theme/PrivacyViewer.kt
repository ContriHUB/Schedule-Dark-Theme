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

import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.alpha.dev.schedule_dark_theme.databinding.ActivityPrivacyViewerBinding

class PrivacyViewer : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.privacyInfo.text = Html.fromHtml(PRIVACY_STATEMENT, Html.FROM_HTML_MODE_LEGACY)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}
