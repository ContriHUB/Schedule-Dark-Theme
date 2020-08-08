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

import android.Manifest
import android.app.AlarmManager
import android.app.UiModeManager
import android.app.WallpaperManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.DateFormat
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alpha.dev.schedule_dark_theme.appService.ReceiverManager
import com.google.android.material.card.MaterialCardView
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

const val enableDeveloperOptions = "&#8226; Go to Settings > About Phone.<br>&#8226; Tap on the Build Number 7 times."
const val connectToUSB = "&#8226; Go to Settings > System > Developer Options.<br>&#8226; Check the USB Debugging box.<br>&#8226; If you don't have adb on your computer, Tap below to download and " +
        "install it. Otherwise, once you have adb, connect your device to computer and Allow the Allow USB Debugging Dialog on your device.<br>NOTE: Developer Options is NOT rooting"
const val commandStep =
        "&#8226; Open command prompt on computer and type the below command.<br>&#8226; <strong>adb shell pm grant com.alpha.dev.schedule_dark_theme android.permission.WRITE_SECURE_SETTINGS</strong>"
const val ADBDownloadLink = "https://drive.google.com/file/d/1TXesESvDEATvVchPDFYmvEkKanXmJzFQ/view?usp=sharing"
const val PRIVACY_STATEMENT =
        "Shashank Verma built the Schedule Dark Theme app as a Free app. This SERVICE is provided by me at no cost and is intended for use as is.<br><br>This page is used " +
                "to inform visitors regarding my policies with the collection and use if anyone decided to use my Service.<br>" +
                "<br>" +
                "The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which is accessible at Schedule Dark Theme unless otherwise defined in this Privacy Policy.<br>" +
                "<br>" +
                "<strong>Information Collection and Use</strong><br>" +
                "<br>" +
                "NO Information collected, This is offline app.<br>" +
                "<br>" +
                "<strong>Security</strong><br>" +
                "<br>" +
                "This app is scanned by Google Play Protect.<br>" +
                "<br>" +
                "<strong>Links to Other Sites</strong><br>" +
                "<br>" +
                "This app contains link to developer's Google drive's folder for resource as mentioned in app.<br>" +
                "<br>" +
                "<strong>Changes to This Privacy Policy</strong><br>" +
                "<br>" +
                "I may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new Privacy Policy on this page. These changes are effective immediately after they are posted on this page.<br>" +
                "<br>" +
                "<strong>Contact Us</strong><br>" +
                "<br>" +
                "If you have any questions or suggestions about my Privacy Policy, do not hesitate to contact me at shashank.verma2002@gmail.com."


const val LIGHT = 1
const val DARK = 2
const val WALL_LIGHT = 5
const val WALL_DARK = 6
const val IMAGE_RETRIEVE_LIGHT = 101
const val IMAGE_RETRIEVE_DARK = 102
const val WALL_RETRIEVE_LIGHT = 103
const val WALL_RETRIEVE_DARK = 104
const val SUN_SET_RISE = 3
const val TIME_SLOTS = 4

const val PREF_NAME = "autoDarkTheme"
const val THEME = "theme"
const val LOCK_PREF = "notify_lock"
const val ENABLE_FEATURE = "enabled_feature"
const val WALL_FEATURE = "wall_feature"
const val TIME_ENABLE = "time_enable"
const val TIME_DISABLE = "time_disable"
const val TRIGGER_TIME = "trigger"
const val TOAST_PREF = "toast"
const val SUNSET_TIME = "sunset"
const val SUNRISE_TIME = "sunrise"

const val DIR = "images"
const val FILE_LIGHT = "light.png"
const val FILE_DARK = "dark.png"
const val COMPRESS_LIGHT = "com_light.png"
const val COMPRESS_DARK = "com_dark.png"
const val FILE_WALL_LIGHT = "wall_light.png"
const val FILE_WALL_DARK = "wall_dark.png"
const val WALL_COMPRESS_LIGHT = "wall_com_light.png"
const val WALL_COMPRESS_DARK = "wall_com_dark.png"
const val WALLPAPER_ENABLED = "wall"

const val DEFAULT_ENABLE_TIME = 108L
const val DEFAULT_DISABLE_TIME = 108L

const val defTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
var mTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

@Volatile
var sImgView: AppCompatImageView? = null

@Volatile
var sEmptyImg: AppCompatImageView? = null

@Volatile
var sCardV: MaterialCardView? = null

@Volatile
var sCheck: SwitchCompat? = null

@Volatile
private var uiModeManager: UiModeManager? = null

@Volatile
private var preference: Preferences? = null

@Volatile
private var layoutInflater: LayoutInflater? = null

@Volatile
private var alarmManager: AlarmManager? = null

fun getUiManager(context: Context): UiModeManager {
    if (uiModeManager == null) {
        uiModeManager = context.applicationContext.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    }
    return uiModeManager!!
}

fun getLayoutInflater(context: Context): LayoutInflater {
    if (layoutInflater == null) {
        layoutInflater = context.applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
    return layoutInflater!!
}

fun getAlarmManager(context: Context): AlarmManager {
    if (alarmManager == null) {
        alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    return alarmManager!!
}

fun permissionGranted(context: Context): Boolean = context.applicationContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED
fun storagePermissionGranted(context: Context): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

fun getStoragePermission(context: Context, activity: AppCompatActivity) {
    if (!storagePermissionGranted(context)) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
    }
}

fun imageExists(context: Context, type: Int): Boolean = File(context.getDir(DIR, Context.MODE_PRIVATE), when (type) {
    LIGHT -> FILE_LIGHT
    DARK -> FILE_DARK
    WALL_LIGHT -> FILE_WALL_LIGHT
    else -> FILE_WALL_DARK
}).exists()

fun getThumbImage(context: Context, type: Int): Bitmap? = async {
    val file = File(context.getDir(DIR, Context.MODE_PRIVATE), when (type) {
        LIGHT -> COMPRESS_LIGHT
        DARK -> COMPRESS_DARK
        WALL_LIGHT -> WALL_COMPRESS_LIGHT
        else -> WALL_COMPRESS_DARK
    })

    withContext(Dispatchers.IO) {
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            BitmapFactory.decodeStream(fis)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                fis?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun getImage(context: Context, type: Int): Bitmap? = async {
    val file = File(context.getDir(DIR, Context.MODE_PRIVATE), when (type) {
        LIGHT -> FILE_LIGHT
        DARK -> FILE_DARK
        WALL_LIGHT -> FILE_WALL_LIGHT
        else -> FILE_WALL_DARK
    })

    withContext(Dispatchers.IO) {
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            BitmapFactory.decodeStream(fis)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                fis?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun getBitmap(cr: ContentResolver, uri: Uri): Bitmap = async {
    withContext(Dispatchers.IO) {
        val ins = cr.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(ins)
        ins?.close()
        bitmap
    }
}

fun compressBitmap(file: File): Bitmap? = async {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true

    withContext(Dispatchers.IO) {
        val ins = FileInputStream(file)
        BitmapFactory.decodeStream(ins, null, options)
        ins.close()
    }

    var scale = 1
    while (options.outWidth / scale / 2 >= 100 && options.outHeight / scale / 2 >= 100) {
        scale *= 2
    }

    val finalOptions = BitmapFactory.Options()
    finalOptions.inSampleSize = scale

    withContext(Dispatchers.IO) {
        val inputStream = FileInputStream(file)
        val out = BitmapFactory.decodeStream(inputStream, null, finalOptions)
        inputStream.close()
        out
    }
}

fun toggleTheme(context: Context, bin: Int) {
    Settings.Secure.putInt(context.contentResolver, "ui_night_mode", if (bin == LIGHT) LIGHT else DARK)
    getUiManager(context).enableCarMode(0)
    getUiManager(context).disableCarMode(0)

    log("Theme", "Switched to ${if (bin == LIGHT) "Light" else "Dark"} Theme", context)

    makeToast(
            context, "Switched to ${if (bin == LIGHT) "Light" else "Dark"} Theme", Toast.LENGTH_LONG,
            if (bin == LIGHT) R.drawable.ic_brightness_7_black_24dp else R.drawable.ic_brightness_2_black_24dp
    )

    if (PreferenceHelper(context).getBoolean(WALLPAPER_ENABLED, false)) {
        updateWallpaper(context, bin)
    }
}

fun updateWallpaper(context: Context, type: Int) {
    if (imageExists(context, type)) {
        val bitmap = getImage(context, type)
        bitmap ?: return
        WallpaperManager.getInstance(context).setBitmap(bitmap)
        bitmap.recycle()
    }
}

fun makeToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT, imageResource: Int = R.drawable.ic_brightness_4_black_24dp) {
    if (PreferenceHelper(context).getBoolean(TOAST_PREF, true)) {
        Handler().post {
            val view = getLayoutInflater(context).inflate(R.layout.toast_layout, null)

            view.findViewById<AppCompatTextView>(R.id.text).text = message
            view.findViewById<AppCompatImageView>(R.id.tIcon).setImageResource(imageResource)

            val toast = Toast(context.applicationContext)
            toast.duration = duration
            toast.setGravity(Gravity.BOTTOM, 0, 200)
            toast.view = view
            toast.show()
        }
    }
}

fun systemToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT, imageResource: Int = R.drawable.ic_brightness_4_black_24dp) {
    if (PreferenceHelper(context).getBoolean(TOAST_PREF, true)) {
        Handler().post {
            val view = getLayoutInflater(context).inflate(R.layout.toast_layout, null)

            view.findViewById<AppCompatTextView>(R.id.text).text = message
            view.findViewById<AppCompatImageView>(R.id.tIcon).setImageResource(imageResource)

            val toast = Toast(context.applicationContext)
            toast.duration = duration
            toast.setGravity(Gravity.BOTTOM, 0, 200)
            toast.view = view
            toast.show()
        }
    }
}

//fun getClosestMode(enableMilli: Long, disableMilli: Long): Int {
// Current time is more than both enableDarkMilli AND disableDarkMilli
// i.e. enableDarkMilli and disableDarkMilli is past with reference of current time
//    if (System.currentTimeMillis() > enableMilli && System.currentTimeMillis() > disableMilli) {
//        if ()
//    }
//    return 1
//}

/**
 * The ...
 * @param darkMilli AND
 * @param lightMilli
 * -> should be latest milliseconds
 */
fun scheduleTimely(manager: ReceiverManager, darkMilli: Long, lightMilli: Long) = lazy {
    val currentMilli = System.currentTimeMillis()

    if (currentMilli in lightMilli..darkMilli) {    // current time is between light and dark time
        if (lightMilli > darkMilli) {   // light is still ahead of dark
            // enable dark theme AND light theme
            manager.createOrRecreateOperation(ReceiverManager.DARK_ID, darkMilli)
            manager.createOrRecreateOperation(ReceiverManager.LIGHT_ID, lightMilli)
        } else {
            if (darkMilli > lightMilli) {
                // enable light theme AND dark theme
                manager.createOrRecreateOperation(ReceiverManager.LIGHT_ID, lightMilli)
                manager.createOrRecreateOperation(ReceiverManager.DARK_ID, darkMilli)
            }
        }
    } else if (currentMilli > lightMilli && currentMilli > darkMilli) {    // light and dark, current time had already part both
        if (lightMilli > darkMilli) {
            // enable light theme and postpone the dark theme to a day ahead
            manager.createOrRecreateOperation(ReceiverManager.LIGHT_ID, lightMilli)

            // adding a day
            manager.createOrRecreateOperation(ReceiverManager.DARK_ID, darkMilli + ReceiverManager.REPEAT_INTERVAL)
        } else {
            if (darkMilli > lightMilli) {
                // enable dark theme and postpone the light theme to a day ahead
                manager.createOrRecreateOperation(ReceiverManager.DARK_ID, darkMilli)

                // adding a day
                manager.createOrRecreateOperation(ReceiverManager.LIGHT_ID, lightMilli + ReceiverManager.REPEAT_INTERVAL)
            }
        }
    } else {
        if (currentMilli < lightMilli && currentMilli < darkMilli) {
            // enable both theme time normally
            manager.createOrRecreateOperation(ReceiverManager.LIGHT_ID, lightMilli)
            manager.createOrRecreateOperation(ReceiverManager.DARK_ID, darkMilli)
        }
    }
}

fun log(tag: String, message: String, context: Context) {
    Log.d(tag, message)
    val file = File(context.filesDir, "sch_log.txt")
    var fos: FileOutputStream? = null
    try {
        fos = context.openFileOutput("sch_log.txt", if (file.exists()) Context.MODE_APPEND else Context.MODE_PRIVATE)
        fos?.write("${DateFormat.getTimeInstance(DateFormat.LONG).format(System.currentTimeMillis())} $tag: $message \r\n".toByteArray())
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            fos?.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}

class PreferenceHelper(private val context: Context) {

    private fun getPreference(): Preferences {
        if (preference == null) {
            preference = BinaryPreferencesBuilder(context.applicationContext)
                    .name(PREF_NAME)
                    .build()
        }
        return preference!!
    }

    fun putBoolean(key: String, bool: Boolean) {
        val editor = getPreference().edit()

        editor.putBoolean(key, bool)
        editor.apply()
    }

    fun getBoolean(key: String, def: Boolean): Boolean = getPreference().getBoolean(key, def)

    fun putInt(key: String, int: Int) {
        val editor = getPreference().edit()

        editor.putInt(key, int)
        editor.apply()
    }

    fun getInt(key: String, def: Int): Int = getPreference().getInt(key, def)

    fun putLong(key: String, lng: Long) {
        val editor = getPreference().edit()

        editor.putLong(key, lng)
        editor.apply()
    }

    fun getLong(key: String, def: Long): Long = getPreference().getLong(key, def)
}

fun Calendar.putTimeInMillis(value: Long): Calendar {
    this.timeInMillis = value
    return this
}

fun Calendar.put(field: Int, value: Int): Calendar {
    this.set(field, value)
    return this
}

/**
 * Simplifying coroutine call
 */
fun <T> async(block: suspend CoroutineScope.() -> T): T = runBlocking { withContext(Dispatchers.Default) { block() } }