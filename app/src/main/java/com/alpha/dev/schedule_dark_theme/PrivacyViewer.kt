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

import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.alpha.dev.fastscroller.FastScrollerBuilder
import com.alpha.dev.fastscroller.ScrollingViewOnApplyWindowInsetsListener
import kotlinx.android.synthetic.main.activity_privacy_viewer.*

class PrivacyViewer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_viewer)

        privacyInfo.text = Html.fromHtml(PRIVACY_STATEMENT, Html.FROM_HTML_MODE_LEGACY)
        p_svc.setOnApplyWindowInsetsListener(ScrollingViewOnApplyWindowInsetsListener())
        FastScrollerBuilder(p_svc).useMd2Style().build()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}
