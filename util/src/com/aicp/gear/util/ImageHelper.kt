/*
 * SPDX-FileCopyrightText: 2013-2017 SlimRoms Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.util

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.Config
import android.graphics.PorterDuff.Mode
import android.graphics.Shader.TileMode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.graphics.drawable.Drawable
import android.util.Log

class ImageHelper {

    companion object {

        private const val TAG = "ImageHelper"

        @JvmStatic
        fun getColoredDrawable(d: Drawable?, color: Int): Drawable? {
            if (d == null) {
                return null
            }

            if (d is VectorDrawable) {
                d.setTint(color)
                return d
            }

            if (d !is BitmapDrawable) {
                Log.e(TAG, "Tinting not implemented for type " + d.javaClass.simpleName)
                return d
            }

            val colorBitmap = d.bitmap
            val grayscaleBitmap = toGrayscale(colorBitmap)

            val pp = Paint()
            pp.isAntiAlias = true

            val frontFilter = PorterDuffColorFilter(color, Mode.MULTIPLY)
            pp.colorFilter = frontFilter

            val cc = Canvas(grayscaleBitmap)

            val rect = Rect(
                0,
                0,
                grayscaleBitmap.width,
                grayscaleBitmap.height
            )

            cc.drawBitmap(grayscaleBitmap, rect, rect, pp)

            return BitmapDrawable(grayscaleBitmap)
        }

        @JvmStatic
        fun drawableToBitmap(drawable: Drawable?): Bitmap? {
            if (drawable == null) {
                return null
            } else if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }

        @JvmStatic
        fun getResizedIconDrawable(
            source: Drawable?,
            context: Context,
            iconSizeId: Int,
            scaleFactor: Float
        ): Drawable? {

            if (source == null) {
                return null
            }

            val iconSize = (
                context.resources.getDimensionPixelSize(iconSizeId) * scaleFactor
            ).toInt()

            val bitmap = drawableToBitmap(source)

            val scaledBitmap = Bitmap.createBitmap(
                iconSize,
                iconSize,
                Config.ARGB_8888
            )

            val ratioX = iconSize / bitmap!!.width.toFloat()
            val ratioY = iconSize / bitmap.height.toFloat()

            val middleX = iconSize / 2.0f
            val middleY = iconSize / 2.0f

            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            paint.isAntiAlias = true

            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

            val canvas = Canvas(scaledBitmap)
            canvas.concat(scaleMatrix)

            canvas.drawBitmap(
                bitmap,
                Rect(0, 0, bitmap.width, bitmap.height),
                Rect(0, 0, iconSize, iconSize),
                paint
            )

            return BitmapDrawable(context.resources, scaledBitmap)
        }

        @JvmStatic
        fun drawableToShortcutIconBitmap(
            context: Context,
            drawable: Drawable?,
            dp: Int
        ): Bitmap? {

            if (drawable == null) {
                return null
            } else if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            var size = Converter.dpToPx(context, dp)

            while (size < drawable.intrinsicHeight ||
                size < drawable.intrinsicWidth
            ) {
                size += 12
            }

            val bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888)

            val canvas = Canvas(bitmap)

            drawable.setBounds(
                (size - drawable.intrinsicWidth) / 2,
                (size - drawable.intrinsicHeight) / 2,
                (size + drawable.intrinsicWidth) / 2,
                (size + drawable.intrinsicHeight) / 2
            )

            drawable.draw(canvas)

            return bitmap
        }

        private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {

            val width = bmpOriginal.width
            val height = bmpOriginal.height

            val bmpGrayscale = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )

            val c = Canvas(bmpGrayscale)

            val paint = Paint()
            paint.isAntiAlias = true

            val cm = ColorMatrix()
            cm.setSaturation(0f)

            val rect = Rect(0, 0, width, height)

            val f = ColorMatrixColorFilter(cm)
            paint.colorFilter = f

            c.drawBitmap(bmpOriginal, rect, rect, paint)

            return bmpGrayscale
        }

        @JvmStatic
        fun resize(context: Context?, image: Drawable?, size: Int): Drawable? {

            if (image == null || context == null) {
                return null
            }

            if (image !is BitmapDrawable) {
                return image
            }

            val newSize = Converter.dpToPx(context, size)

            val bitmap = image.bitmap

            val scaledBitmap = Bitmap.createBitmap(
                newSize,
                newSize,
                Config.ARGB_8888
            )

            val ratioX = newSize / bitmap.width.toFloat()
            val ratioY = newSize / bitmap.height.toFloat()

            val middleX = newSize / 2.0f
            val middleY = newSize / 2.0f

            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            paint.isAntiAlias = true

            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

            val canvas = Canvas(scaledBitmap)
            canvas.concat(scaleMatrix)

            canvas.drawBitmap(
                bitmap,
                Rect(0, 0, bitmap.width, bitmap.height),
                Rect(0, 0, newSize, newSize),
                paint
            )

            return BitmapDrawable(context.resources, scaledBitmap)
        }

        @JvmStatic
        fun getRoundedCornerBitmap(bitmap: Bitmap?): Bitmap? {

            if (bitmap == null) {
                return null
            }

            val output = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                Config.ARGB_8888
            )

            val canvas = Canvas(output)

            val color = -0xbdbdbe

            val paint = Paint()

            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            val rectF = RectF(rect)

            val roundPx = 24f

            paint.isAntiAlias = true

            canvas.drawARGB(0, 0, 0, 0)

            paint.color = color

            canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

            paint.xfermode = PorterDuffXfermode(Mode.SRC_IN)

            canvas.drawBitmap(bitmap, rect, rect, paint)

            return output
        }

        @JvmStatic
        fun getCircleBitmap(bitmap: Bitmap?): Bitmap? {

            if (bitmap == null) {
                return null
            }

            val width = bitmap.width
            val height = bitmap.height

            val output = Bitmap.createBitmap(
                width,
                height,
                Config.ARGB_8888
            )

            val canvas = Canvas(output)

            val shader = BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP)

            val paint = Paint()
            paint.isAntiAlias = true
            paint.shader = shader

            canvas.drawCircle(
                width / 2f,
                height / 2f,
                width / 2f,
                paint
            )

            return output
        }
    }
}

