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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.alpha.dev.materialdialog.MaterialProgressDialog
import com.alpha.dev.schedule_dark_theme.fragments.MainFeatureFragment
import com.alpha.dev.schedule_dark_theme.fragments.WallpaperFeatureFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var mainFrag: MainFeatureFragment
    private lateinit var wallFrag: WallpaperFeatureFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        log("\r\nSTARTUP", "<== MAIN ACTIVITY STARTED ==>\n", this)

        checkAndSetTimes()

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.contact_dev -> {
                    log("Main/Toolbar", "Clicked item -> Contact Dev", this)
                    val intent = Intent(Intent.ACTION_SENDTO).setData(Uri.parse("mailto:shashank.verma2002@gmail.com"))
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    }
                    true
                }

                R.id.privacy_viewer -> {
                    log("Main/Toolbar", "Clicked item -> Privacy viewer", this)
                    startActivity(Intent(this@MainActivity, PrivacyViewer::class.java))
                    true
                }

                R.id.rate -> {
                    log("Main/Toolbar", "Clicked item -> Rate", this)
                    val goToMarket = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

                    try {
                        startActivity(goToMarket)
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
                    }

                    true
                }

                R.id.share -> {
                    log("Main/Toolbar", "Clicked item -> Share", this)
                    val shareIntent =
                            Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, "Hey check out this app at: https://play.google.com/store/apps/details?id=$packageName").setType("text/plain")
                    startActivity(shareIntent)

                    true
                }

                /*R.id.checkLog -> {
                    log("Main/Toolbar", "Clicked item -> Check Log", this)
                    startActivity(Intent(this, LogActivity::class.java))
                    true
                }*/
                else -> false
            }
        }

        val adapter = ViewPagerAdapter(supportFragmentManager)
        mainFrag = MainFeatureFragment(this, this)
        wallFrag = WallpaperFeatureFragment(this, this)

        adapter.addItem(mainFrag, "Auto Theme")
        adapter.addItem(wallFrag, "Auto Wallpaper")

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            data ?: return

            val imgUri = data.data
            imgUri ?: return

            val dialog = MaterialProgressDialog(this)
            dialog.setTitle("Processing")
            dialog.setMessage("Optimising sample")
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            log("onActivityResult", "Starting process ...", this)
            Handler().postDelayed({
                val bitmap = getBitmap(contentResolver, imgUri)

                val dir = getDir(DIR, Context.MODE_PRIVATE)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                log(TAG, "REQUEST CODE => $requestCode", this)
                val path = File(dir, when (requestCode) {
                    IMAGE_RETRIEVE_LIGHT -> {
                        log(TAG, "path -> $FILE_LIGHT", this)
                        FILE_LIGHT
                    }
                    IMAGE_RETRIEVE_DARK -> {
                        log(TAG, "path -> $FILE_DARK", this)
                        FILE_DARK
                    }
                    WALL_RETRIEVE_LIGHT -> {
                        log(TAG, "path -> $FILE_WALL_LIGHT", this)
                        FILE_WALL_LIGHT
                    }
                    else -> {
                        log(TAG, "path -> $FILE_WALL_DARK", this)
                        FILE_WALL_DARK
                    }
                })
                val thumbPath = File(dir, when (requestCode) {
                    IMAGE_RETRIEVE_LIGHT -> {
                        log(TAG, "thumb path -> $COMPRESS_LIGHT", this)
                        COMPRESS_LIGHT
                    }
                    IMAGE_RETRIEVE_DARK -> {
                        log(TAG, "thumb path -> $COMPRESS_DARK", this)
                        COMPRESS_DARK
                    }
                    WALL_RETRIEVE_LIGHT -> {
                        log(TAG, "thumb path -> $WALL_COMPRESS_LIGHT", this)
                        WALL_COMPRESS_LIGHT
                    }
                    else -> {
                        log(TAG, "thumb path -> $WALL_COMPRESS_DARK", this)
                        WALL_COMPRESS_DARK
                    }
                })
                saveImage(path, thumbPath, bitmap)

                when (requestCode) {
                    IMAGE_RETRIEVE_LIGHT -> {
                        sImgView?.setImageBitmap(getThumbImage(this, LIGHT))
                        sEmptyImg?.visibility = View.GONE
                        sCardV?.visibility = View.VISIBLE

                        sCheck?.isChecked = imageExists(this, LIGHT) || imageExists(this, DARK)
                    }
                    IMAGE_RETRIEVE_DARK -> {
                        sImgView?.setImageBitmap(getThumbImage(this, DARK))
                        sEmptyImg?.visibility = View.GONE
                        sCardV?.visibility = View.VISIBLE

                        sCheck?.isChecked = imageExists(this, LIGHT) || imageExists(this, DARK)
                    }
                    WALL_RETRIEVE_LIGHT -> {
                        sImgView?.setImageBitmap(getThumbImage(this, WALL_LIGHT))
                        sEmptyImg?.visibility = View.GONE
                        sCardV?.visibility = View.VISIBLE

                        sCheck?.isChecked = imageExists(this, WALL_LIGHT) || imageExists(this, WALL_DARK)
                    }
                    WALL_RETRIEVE_DARK -> {
                        sImgView?.setImageBitmap(getThumbImage(this, WALL_DARK))
                        sEmptyImg?.visibility = View.GONE
                        sCardV?.visibility = View.VISIBLE

                        sCheck?.isChecked = imageExists(this, WALL_LIGHT) || imageExists(this, WALL_DARK)
                    }
                }
                resetStatic()
                dialog.dismiss()
            }, 50)
        }
    }

    private fun saveImage(path: File, thumbPath: File, bitmap: Bitmap) = async {
        var fos: FileOutputStream? = null
        launch(Dispatchers.IO) {
            try {
                fos = FileOutputStream(path)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                bitmap.recycle()

                var thumbFos: FileOutputStream? = null
                try {
                    thumbFos = FileOutputStream(thumbPath)
                    val thumbBitmap = compressBitmap(path)
                    thumbBitmap?.compress(Bitmap.CompressFormat.PNG, 50, thumbFos)
                    thumbBitmap?.recycle()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        thumbFos?.close()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fos?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun resetStatic() {
        sImgView = null
        sEmptyImg = null
        sCardV = null
        sCheck = null
        log("Static Mem", "Reset release stat => ${sImgView == null && sEmptyImg == null && sCardV == null && sCheck == null}", this)
    }

    private fun checkAndSetTimes() {
        val pref = PreferenceHelper(this)
        log("Check Main Times", "checking", this)
        val defEnable = pref.getLong(TIME_ENABLE, DEFAULT_ENABLE_TIME)
        val defDisable = pref.getLong(TIME_DISABLE, DEFAULT_DISABLE_TIME)

        if (defEnable == DEFAULT_ENABLE_TIME) {
            log("Check Main Times", "value for ENABLE was default", this)
            val time = Calendar.getInstance().put(Calendar.HOUR_OF_DAY, 19).put(Calendar.MINUTE, 0).put(Calendar.SECOND, 0).timeInMillis
            pref.putLong(TIME_ENABLE, time)
            pref.putLong(SUNSET_TIME, time)
            log("Check Main Times", "value for ENABLE updated; hour -> 19, min -> 0, sec -> 0", this)
        } else {
            log("Check Main Times", "value for ENABLE was -> NOT <- default", this)
        }
        if (defDisable == DEFAULT_DISABLE_TIME) {
            log("Check Main Times", "value for DISABLE was default", this)
            val time = Calendar.getInstance().put(Calendar.HOUR_OF_DAY, 7).put(Calendar.MINUTE, 0).put(Calendar.SECOND, 0).timeInMillis
            pref.putLong(TIME_DISABLE, time)
            pref.putLong(SUNRISE_TIME, time)
            log("Check Main Times", "value for DISABLE updated; hour -> 7, min -> 0, sec -> 0", this)
        } else {
            log("Check Main Times", "value for DISABLE was -> NOT <- default", this)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    inner class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val list = ArrayList<Item>()

        fun addItem(fragment: Fragment, title: String) {
            list.add(Item(fragment, title))
        }

        override fun getItem(position: Int): Fragment = list[position].fragment

        override fun getCount(): Int = list.size

        override fun getPageTitle(position: Int): CharSequence = list[position].title
    }

    data class Item(var fragment: Fragment, var title: String)
}