package com.dash.mylibrary.weight

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.MainThread
import com.dash.mylibrary.R
import com.dash.mylibrary.utils.UiCommon
import com.dash.mylibrary.utils.UnitConstants
import com.dash.mylibrary.utils.UnitConstants.KB
import com.dash.mylibrary.utils.UnitConstants.MB
import com.dash.mylibrary.utils.getCurrentColor
import java.math.RoundingMode


/**
 * 刻度盘的自定义View
 */
class DashboardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = -1) : View(context, attrs, defStyleAttr) {
    private var mMaxCircle = UiCommon.dp2px(140).toInt()
    private var mNumberSize = UiCommon.dp2px(28).toInt()
    private var mUnitSize = UiCommon.dp2px(12).toInt()
    private var currentBytes: Long = 0//当前进度
    
    private var startDashAngle: Float //开始的圆心角度
    private var radianDashAngle = 270f //需要画多大的角度
    
    private var startColor = Color.parseColor("#FFCC0CED")
    private var endColor = Color.parseColor("#009BFF")
    
    private var scaleColor = Color.parseColor("#33ffffff")
    private var arcBgColor = Color.parseColor("#33ffffff")
    private var scaleTextColor = Color.parseColor("#80ffffff")
    
    private var speedUnitTextColor = Color.WHITE
    private var speedTextColor = Color.WHITE
    private var speedTextBgColor = Color.parseColor("#448EFF")
    
    private var scaleTextSize = UiCommon.dp2px(10).toInt()
    private var arcStrokeWidth = UiCommon.dp2px(8).toInt()
    private val mArcBgPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
    }
    
    private val speedTextPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            strokeWidth = 3f
        }
    }
    private val speedUnitTextPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            strokeWidth = 1f
        }
    }
    
    //刻度绘制的画笔
    private val scalePaint by lazy {
        Paint().apply {
            isAntiAlias = true
            strokeWidth = 1f
            style = Paint.Style.FILL
        }
    }
    private val scaleTextPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    private val mArcProgressPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
    }
    private val mPath by lazy { Path() }
    
    init {
        initAttrs(context, attrs)
        initPaint()
        /**
         * 计算圆盘开始角度
         */
        if (radianDashAngle > 360) {
            throw IndexOutOfBoundsException("圆角超出计算范围")
        }
        startDashAngle = (360f - radianDashAngle) / 2 + 90
    }
    
    /**
     * 初始化缓存参数
     */
    private fun initAttrs(context: Context, attrs: AttributeSet? = null) {
        if (attrs == null) return
        val a = context.obtainStyledAttributes(attrs, R.styleable.DashboardView)
        mMaxCircle = a.getDimensionPixelSize(R.styleable.DashboardView_show_circle_width, mMaxCircle)
        mNumberSize = a.getDimensionPixelSize(R.styleable.DashboardView_speed_number_size, mNumberSize)
        mUnitSize = a.getDimensionPixelSize(R.styleable.DashboardView_speed_unit_size, mUnitSize)
        startColor = a.getColor(R.styleable.DashboardView_speed_start_color, startColor)
        endColor = a.getColor(R.styleable.DashboardView_speed_end_color, endColor)
        arcBgColor = a.getColor(R.styleable.DashboardView_speed_bg_color, arcBgColor)
        scaleColor = a.getColor(R.styleable.DashboardView_speed_scale_color, scaleColor)
        scaleTextColor = a.getColor(R.styleable.DashboardView_speed_scale_unit_color, scaleTextColor)
        speedTextColor = a.getColor(R.styleable.DashboardView_speed_text_color, speedTextColor)
        speedUnitTextColor = a.getColor(R.styleable.DashboardView_speed_unit_text_color, speedUnitTextColor)
        speedTextBgColor = a.getColor(R.styleable.DashboardView_speed_text_bg_color, speedTextBgColor)
        
        arcStrokeWidth = a.getDimensionPixelSize(R.styleable.DashboardView_circle_stroke_width, arcStrokeWidth)
        scaleTextSize = a.getDimensionPixelSize(R.styleable.DashboardView_speed_scale_text_size, scaleTextSize)
    
        radianDashAngle = a.getFloat(R.styleable.DashboardView_speed_circle_radian, radianDashAngle)
        a.recycle()
    }
    
    private fun initPaint() {
        speedTextPaint.textSize = mNumberSize.toFloat()
        speedUnitTextPaint.textSize = mUnitSize.toFloat()
        
        scaleTextPaint.textSize = scaleTextSize.toFloat()
        mArcBgPaint.strokeWidth = arcStrokeWidth.toFloat()
        mArcProgressPaint.strokeWidth = arcStrokeWidth.toFloat()
        
        mArcBgPaint.color = arcBgColor
        scaleTextPaint.color = scaleTextColor
        
        speedTextPaint.color = speedTextColor
        speedUnitTextPaint.color = speedUnitTextColor
        speedTextPaint.setShadowLayer(4f, 1f, 1f, speedTextBgColor)
    }
    
    /**
     * 测量获取最大展示范围
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var widthResult = if (widthMode == MeasureSpec.EXACTLY) widthSize else mMaxCircle
        //每个矩形形的宽度
        //最终的高度
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var heightResult = if (heightMode == MeasureSpec.EXACTLY) heightSize else mMaxCircle
        if (heightResult > widthResult) {
            widthResult = heightResult
        }
        if (widthResult > heightResult) {
            heightResult = widthResult
        }
        setMeasuredDimension(widthResult, heightResult)
    }
    
    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        val progressAngle = loadProgressToAngle()
        drawLines(canvas, progressAngle)
        drawCircle(canvas, progressAngle)
        drawText(canvas)
    }
    
    /**
     * 绘制刻度
     */
    private fun drawLines(canvas: Canvas, progressAngle: Float) {
        //绘制刻度线,通过两次不同大小圆的遮罩,达到刻度的长短粗细效果
        val mx = (width / 2 - 1).toFloat()
        val my = (height / 2 - 1).toFloat()
        
        val start = scaleTextSize.toFloat()
    
        val startY = start + UiCommon.dp2px(15)
        val startLongY = start + UiCommon.dp2px(8)
        val endY = start + UiCommon.dp2px(25)
        
        val radianItem = radianDashAngle.toBigDecimal().divide(100.toBigDecimal(), 2, RoundingMode.HALF_DOWN).toFloat()
        Log.d("radianDashAngleTag", "radianItem:$radianItem")
        for (i in 0..100) {
            canvas.save()
            canvas.rotate(-startDashAngle + i * radianItem, mx, my)
        
            scalePaint.color = if (progressAngle > 0 && i * radianItem <= progressAngle) {
                radianItem.toBigDecimal().setScale(2, RoundingMode.HALF_DOWN)
                    .multiply(i.toBigDecimal())
                    .divide(radianDashAngle.toBigDecimal(), 2, RoundingMode.HALF_DOWN).toDouble()
                    .getCurrentColor(startColor, endColor)
            } else scaleColor
        
            canvas.drawLine(mx, if (i % 25 == 0) startLongY else startY, mx, endY, scalePaint)
            canvas.restore()
        }
        drawScaleText(canvas,start)
    }
    
    private fun drawScaleText(canvas: Canvas,start:Float) {
        val circleWidth = width.toFloat() - 1f
        val oval4 = RectF(start, start, circleWidth - start, circleWidth - start)
    
        val itemAngle = radianDashAngle.toBigDecimal().divide(4.toBigDecimal(), 2, RoundingMode.HALF_DOWN)
    
        for (i in 0 until 5) {
            val startAngle = itemAngle.multiply(i.toBigDecimal()).add(startDashAngle.toBigDecimal()).setScale(2, RoundingMode.HALF_DOWN).toFloat()
            Log.d("radianDashAngleTag", "drawScaleText-startAngle:$startAngle")
            drawPathText(canvas, oval4, startAngle, when (i) {
                0 -> "0K"
                1 -> "512K"
                2 -> "1M"
                3 -> "5M"
                else -> "10M"
            })
        }
    }
    
    /**
     * 绘制显示的刻度文字
     */
    private fun drawPathText(canvas: Canvas, rectF: RectF, startAngle: Float, value: String) {
        mPath.reset()
        mPath.addArc(rectF, startAngle - 20f, 40f)
        canvas.drawTextOnPath(value, mPath, 0f, 0f, scaleTextPaint)
    }
    
    /**
     * 绘制圆盘
     */
    private fun drawCircle(canvas: Canvas, progressAngle: Float) {
        Log.d("dashboardTag", "drawCircle:$progressAngle")
        val circleWidth = width.toFloat() - scaleTextSize.toFloat() - UiCommon.dp2px(40)
        val start = UiCommon.dp2px(40) + scaleTextSize.toFloat()
        val oval = RectF(start, start, circleWidth, circleWidth) //绘制区域
        //绘制背景圆弧
        canvas.drawArc(oval, startDashAngle, radianDashAngle, false, mArcBgPaint)
        //绘制进度
        mArcProgressPaint.shader = LinearGradient(start, circleWidth, circleWidth, circleWidth, intArrayOf(startColor, endColor), null, Shader.TileMode.CLAMP)
        canvas.drawArc(oval, startDashAngle, progressAngle, false, mArcProgressPaint)
    }
    
    /**
     * 更新进度
     */
    @MainThread
    fun updateProgress(currentSize: Long) {
        currentBytes = if (currentSize < 0) 0 else currentSize
        invalidate()
    }
    
    /**
     * 获取当前的进度圆角度
     */
    private fun loadProgressToAngle(): Float {
        val newItem = radianDashAngle.toBigDecimal().divide(4.toBigDecimal(), 2, RoundingMode.HALF_DOWN)
        Log.d("radianDashAngleTag", "newItem:$newItem,radianDashAngle:$radianDashAngle,startDashAngle:${startDashAngle}")
        return currentBytes.toBigDecimal().setScale(2, RoundingMode.HALF_DOWN).run {
            return@run if (currentBytes <= 512 * KB) {
                this.divide((512 * KB).toBigDecimal(), 2, RoundingMode.HALF_DOWN)
                    .multiply(newItem)
                    .setScale(2,RoundingMode.HALF_DOWN)
                    .toFloat()
            } else if (currentBytes <= 1 * MB) {
                this.divide((1 * MB).toBigDecimal(), 2, RoundingMode.HALF_DOWN)
                    .multiply(newItem)
                    .add(newItem)
                    .setScale(2,RoundingMode.HALF_DOWN)
                    .toFloat()
            } else if (currentBytes <= 5 * MB) {
                this.divide((5 * MB).toBigDecimal(), 2, RoundingMode.HALF_DOWN)
                    .multiply(newItem)
                    .add(newItem.multiply(2.toBigDecimal()))
                    .setScale(2,RoundingMode.HALF_DOWN)
                    .toFloat()
            } else if (currentBytes < 10 * MB) {
                this.divide((10 * MB).toBigDecimal(), 2, RoundingMode.HALF_DOWN)
                    .multiply(newItem)
                    .add(newItem.multiply(3.toBigDecimal()))
                    .setScale(2,RoundingMode.HALF_DOWN)
                    .toFloat()
            } else radianDashAngle
        }
    }
    
    /**
     * 绘制中心文字
     */
    private fun drawText(canvas: Canvas) {
        val inBits: String
        val value: Double
        if (currentBytes < UnitConstants.KB) {
            value = currentBytes.toBigDecimal().setScale(1, RoundingMode.HALF_DOWN).toDouble()
            inBits = "B/s"
        } else if (currentBytes < UnitConstants.MB) {
            value = currentBytes.toBigDecimal().setScale(1, RoundingMode.HALF_DOWN)
                .divide(UnitConstants.KB.toBigDecimal(), 1, RoundingMode.HALF_DOWN).toDouble()
            inBits = "KB/s"
        } else if (currentBytes < UnitConstants.GB) {
            value = currentBytes.toBigDecimal().setScale(1, RoundingMode.HALF_DOWN)
                .divide(UnitConstants.MB.toBigDecimal(), 1, RoundingMode.HALF_DOWN).toDouble()
            inBits = "MB/s"
        } else {
            value = currentBytes.toBigDecimal().setScale(1, RoundingMode.HALF_DOWN)
                .divide(UnitConstants.GB.toBigDecimal(), 1, RoundingMode.HALF_DOWN).toDouble()
            inBits = "GB/s"
        }
        
        canvas.drawText(value.toString(), (width / 2).toFloat(), (height / 2).toFloat(), speedTextPaint)
        canvas.drawText(inBits, (width / 2).toFloat(), (height / 2).toFloat() + mUnitSize + 10, speedUnitTextPaint)
        Log.d("dashboardTag", "inBits:$inBits,value:$value")
    }
}