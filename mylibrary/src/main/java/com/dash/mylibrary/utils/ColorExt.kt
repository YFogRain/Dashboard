package com.dash.mylibrary.utils

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * 颜色处理扩展类
 */
@ColorInt
fun Double.getCurrentColor(@ColorInt startColor: Int, @ColorInt endColor: Int): Int {
	val redStart = Color.red(startColor)
	val blueStart = Color.blue(startColor)
	val greenStart = Color.green(startColor)
	val redEnd = Color.red(endColor)
	val blueEnd = Color.blue(endColor)
	val greenEnd = Color.green(endColor)
	val red = (redStart + ((redEnd - redStart) * this + 0.5)).toInt()
	val greed = (greenStart + ((greenEnd - greenStart) * this + 0.5)).toInt()
	val blue = (blueStart + ((blueEnd - blueStart) * this + 0.5)).toInt()
	return Color.argb(255, red, greed, blue)
}

@ColorInt
fun Float.getCurrentColor(@ColorInt startColor: Int, @ColorInt endColor: Int): Int {
	val redStart = Color.red(startColor)
	val blueStart = Color.blue(startColor)
	val greenStart = Color.green(startColor)
	val redEnd = Color.red(endColor)
	val blueEnd = Color.blue(endColor)
	val greenEnd = Color.green(endColor)
	val red = (redStart + ((redEnd - redStart) * this + 0.5)).toInt()
	val greed = (greenStart + ((greenEnd - greenStart) * this + 0.5)).toInt()
	val blue = (blueStart + ((blueEnd - blueStart) * this + 0.5)).toInt()
	return Color.argb(255, red, greed, blue)
}

@ColorInt
fun Int.getCurrentColor(@ColorInt startColor: Int, @ColorInt endColor: Int): Int {
	val redStart = Color.red(startColor)
	val blueStart = Color.blue(startColor)
	val greenStart = Color.green(startColor)
	val redEnd = Color.red(endColor)
	val blueEnd = Color.blue(endColor)
	val greenEnd = Color.green(endColor)
	val red = (redStart + ((redEnd - redStart) * this + 0.5)).toInt()
	val greed = (greenStart + ((greenEnd - greenStart) * this + 0.5)).toInt()
	val blue = (blueStart + ((blueEnd - blueStart) * this + 0.5)).toInt()
	return Color.argb(255, red, greed, blue)
}

@ColorInt
fun Long.getCurrentColor(@ColorInt startColor: Int, @ColorInt endColor: Int): Int {
	val redStart = Color.red(startColor)
	val blueStart = Color.blue(startColor)
	val greenStart = Color.green(startColor)
	val redEnd = Color.red(endColor)
	val blueEnd = Color.blue(endColor)
	val greenEnd = Color.green(endColor)
	val red = (redStart + ((redEnd - redStart) * this + 0.5)).toInt()
	val greed = (greenStart + ((greenEnd - greenStart) * this + 0.5)).toInt()
	val blue = (blueStart + ((blueEnd - blueStart) * this + 0.5)).toInt()
	return Color.argb(255, red, greed, blue)
}