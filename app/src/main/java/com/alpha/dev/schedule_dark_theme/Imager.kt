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

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileInputStream

/**
 * The public accessibly method that executes [getAsyncThumbImage] [async] asynchronously
 *
 * @param context [Context] to access private files
 * @param type [Int] - [LIGHT] [DARK] [WALL_LIGHT] [WALL_DARK]
 * @return [Bitmap]
 */
fun getThumbImage(context: Context, type: Int): Bitmap? = async { getAsyncThumbImage(context, type) }

/**
 * Method to retrieve thumb image
 */
private fun getAsyncThumbImage(context: Context, type: Int): Bitmap? {
    val file = File(
        context.getDir(DIR, Context.MODE_PRIVATE), when (type) {
            LIGHT -> COMPRESS_LIGHT
            DARK -> COMPRESS_DARK
            WALL_LIGHT -> WALL_COMPRESS_LIGHT
            else -> WALL_COMPRESS_DARK
        }
    )

    var fis: FileInputStream? = null
    return try {
        fis = FileInputStream(file)
        BitmapFactory.decodeStream(fis)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        fis?.close()
    }
}

/**
 * The public accessibly method that executes [getAsyncImage] [async] asynchronously
 *
 * @param context [Context] to access private files
 * @param type [Int] - [LIGHT] [DARK] [WALL_LIGHT] [WALL_DARK]
 * @return [Bitmap]
 */
fun getImage(context: Context, type: Int): Bitmap? = async { getAsyncImage(context, type) }

/**
 * Method to retrieve main image
 */
private fun getAsyncImage(context: Context, type: Int): Bitmap? {
    val file = File(
        context.getDir(DIR, Context.MODE_PRIVATE), when (type) {
            LIGHT -> FILE_LIGHT
            DARK -> FILE_DARK
            WALL_LIGHT -> FILE_WALL_LIGHT
            else -> FILE_WALL_DARK
        }
    )

    var fis: FileInputStream? = null
    return try {
        fis = FileInputStream(file)
        BitmapFactory.decodeStream(fis)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        fis?.close()
    }
}

/**
 * The public accessibly method that executes [getAsyncBitmap] [async] asynchronously
 *
 * @param cr [ContentResolver]
 * @param uri [Uri] for file path
 * @return [Bitmap]
 */
fun getBitmap(cr: ContentResolver, uri: Uri): Bitmap = async { getAsyncBitmap(cr, uri) }

/**
 * Method to retrieve bitmap out of image uri
 */
private fun getAsyncBitmap(cr: ContentResolver, uri: Uri): Bitmap {
    val ins = cr.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(ins)
    ins?.close()
    return bitmap
}

/**
 * The public accessibly method that executes [compressAsyncBitmap] [async] asynchronously
 *
 * @param file [File] the file to be compressed
 * @return [Bitmap]
 */
fun compressBitmap(file: File): Bitmap? = async { compressAsyncBitmap(file) }

/**
 * Method to compress bitmap from image file
 */
private fun compressAsyncBitmap(file: File): Bitmap? {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true

    val ins = FileInputStream(file)
    BitmapFactory.decodeStream(ins, null, options)
    ins.close()

    var scale = 1
    while (options.outWidth / scale / 2 >= 100 && options.outHeight / scale / 2 >= 100) {
        scale *= 2
    }

    val finalOptions = BitmapFactory.Options()
    finalOptions.inSampleSize = scale

    val inputStream = FileInputStream(file)
    val out = BitmapFactory.decodeStream(inputStream, null, finalOptions)
    inputStream.close()
    return out
}