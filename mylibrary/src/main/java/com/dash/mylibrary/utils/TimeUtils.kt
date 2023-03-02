package com.dash.mylibrary.utils

import android.annotation.SuppressLint
import android.text.TextUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
	@SuppressLint("SimpleDateFormat")
	@JvmStatic
	fun isTimesStr(time: String, pattern: String): Boolean {
		if (time.isEmpty()) {
			return false
		}
		return runCatching { SimpleDateFormat(pattern).parse(time) }.isSuccess
		
	}
	
	@JvmStatic
	fun countDownTimeToStr(time: Long, pattern: String): String {
		@SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat(pattern)
		//        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return simpleDateFormat.format(Date(time))
	}
	
	@JvmStatic
	fun timeToStr(time: Long, pattern: String): String {
		var times = time
		@SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat(pattern)
		if (times == 0L) {
			times = System.currentTimeMillis()
		}
		return simpleDateFormat.format(Date(times))
	}
	
	@JvmStatic
	fun dataToStr(date: Date?, pattern: String): String? {
		if (date == null) return null
		@SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat(pattern)
		//        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		return simpleDateFormat.format(date)
	}
	
	@JvmStatic
	fun strToLong(time: String?, pattern: String): Long {
		
		if (TextUtils.isEmpty(time)) {
			return 0
		}
		@SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat(pattern)
		try {
			val parse = simpleDateFormat.parse(time!!)
			return parse!!.time
		} catch (e: ParseException) {
			e.printStackTrace()
		}
		
		return 0
	}
	
	/**
	 * 返回文字描述的日期
	 *
	 * @param date
	 * @return
	 */
	@JvmStatic
	fun getTimeFormatText(date: Date): String {
		
		val calendar = Calendar.getInstance()
		val currentDayIndex = calendar[Calendar.DAY_OF_YEAR]
		val currentYear = calendar[Calendar.YEAR]
		calendar.time = date
		val msgYear = calendar[Calendar.YEAR]
		val msgDayIndex = calendar[Calendar.DAY_OF_YEAR]
		val msgMinute = calendar[Calendar.MINUTE]
		var msgTimeStr = calendar[Calendar.HOUR_OF_DAY].toString() + ":"
		msgTimeStr = if (msgMinute < 10) {
			msgTimeStr + "0" + msgMinute
		} else {
			msgTimeStr + msgMinute
		}
		msgTimeStr = if (currentDayIndex == msgDayIndex) {
			return msgTimeStr
		} else {
			if (currentDayIndex - msgDayIndex == 1 && currentYear == msgYear) {
				"昨天 $msgTimeStr"
			} else if (currentDayIndex - msgDayIndex > 1 && currentYear == msgYear) { //本年消息
				//不同周显示具体月，日，注意函数：calendar.get(Calendar.MONTH) 一月对应0，十二月对应11
				Integer.valueOf(calendar[Calendar.MONTH] + 1)
					.toString() + "月" + calendar[Calendar.DAY_OF_MONTH] + "日 " + msgTimeStr + " "
			} else { // 1、非正常时间，如currentYear < msgYear，或者currentDayIndex < msgDayIndex
				//2、非本年消息（currentYear > msgYear），如：历史消息是2018，今年是2019，显示年、月、日
				msgYear.toString() + "年" + Integer.valueOf(calendar[Calendar.MONTH] + 1) + "月" + calendar[Calendar.DAY_OF_MONTH] + "日" + msgTimeStr + " "
			}
		}
		return msgTimeStr
	}
	
	@JvmStatic
	fun strToDate(time: String?, pattern: String): Date? {
		if (time.isNullOrEmpty()) return null
		try {
			@SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat(pattern)
			return simpleDateFormat.parse(time)
		} catch (e: ParseException) {
		}
		return null
	}
	
	@JvmStatic
	fun getLongToTime(time: Long): String {
		var timeLong = time
		if (timeLong <= 0) {
			timeLong = System.currentTimeMillis()
		}
		@SuppressLint("SimpleDateFormat") val simpleDateFormat =
			SimpleDateFormat("yyyy-MM-dd HH:mm")
		return simpleDateFormat.format(Date(timeLong))
	}
	
	@JvmStatic
	fun formatSeconds(seconds: Long): String {
		var timeStr = seconds.toString() + "秒"
		if (seconds > 60) {
			val second = seconds % 60
			var min = seconds / 60
			timeStr = min.toString() + "分" + second + "秒"
			if (min > 60) {
				min = seconds / 60 % 60
				var hour = seconds / 60 / 60
				timeStr = hour.toString() + "小时" + min + "分" + second + "秒"
				if (hour % 24 == 0L) {
					val day = seconds / 60 / 60 / 24
					timeStr = day.toString() + "天"
				} else if (hour > 24) {
					hour = seconds / 60 / 60 % 24
					val day = seconds / 60 / 60 / 24
					timeStr = day.toString() + "天" + hour + "小时" + min + "分" + second + "秒"
				}
			}
		}
		return timeStr
	}
}