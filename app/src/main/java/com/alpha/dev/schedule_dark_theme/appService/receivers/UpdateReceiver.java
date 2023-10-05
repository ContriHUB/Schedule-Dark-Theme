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

package com.alpha.dev.schedule_dark_theme.appService.receivers;

import static com.alpha.dev.schedule_dark_theme.AppHelperKt.ENABLE_FEATURE;
import static com.alpha.dev.schedule_dark_theme.AppHelperKt.WALL_FEATURE;
import static com.alpha.dev.schedule_dark_theme.AppHelperKt.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alpha.dev.schedule_dark_theme.PreferenceHelper;
import com.alpha.dev.schedule_dark_theme.appService.services.ThemeService;
import com.alpha.dev.schedule_dark_theme.appService.services.WallpaperService;

public class UpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "UpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
                log(TAG, "onReceive: Update Called", context);
                PreferenceHelper pref = new PreferenceHelper(context);
                if (pref.getBoolean(ENABLE_FEATURE, false)) {
                    context.startForegroundService(new Intent(context, ThemeService.class));
                } else if (pref.getBoolean(WALL_FEATURE, false)) {
                    context.startForegroundService(new Intent(context, WallpaperService.class));
                }
            }
        }
    }
}
