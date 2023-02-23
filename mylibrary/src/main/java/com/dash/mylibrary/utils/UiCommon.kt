package com.dash.mylibrary.utils

import com.dash.mylibrary.provider.OverallContext

/**
 * ui的工具类
 */
object UiCommon {
	@JvmStatic
	fun dp2px(dpValue: Int): Float {
		return dpValue * OverallContext.baseContext.resources.displayMetrics.density + 0.5f
	}
}