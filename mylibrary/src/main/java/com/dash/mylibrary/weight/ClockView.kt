package com.dash.mylibrary.weight

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.MainThread
import com.dash.mylibrary.R
import com.dash.mylibrary.utils.UiCommon
import com.dash.mylibrary.utils.appLifecycleScope
import kotlinx.coroutines.*
import java.math.RoundingMode
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * 时钟显示的view
 */
class ClockView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = -1) : View(context, attrs, defStyleAttr) {
    private var circleWidth = UiCommon.dp2px(300) //圆直径
    private var circleWallWidth = UiCommon.dp2px(2) //时装外壁宽度
    private var wallColor = Color.parseColor("#333333") //时钟外壁的颜色
    private var scaleSmallColor = Color.parseColor("#60333333") //时钟小刻度的颜色
    private var scaleTimeColor = Color.parseColor("#333333") //时钟准点时刻刻度的颜色
    private var handHourColor = Color.parseColor("#333333") //时钟小时指针的颜色
    private var handMinuteColor = Color.parseColor("#2ACC5E") //时钟分钟指针的颜色
    private var handSecondColor = Color.parseColor("#009BFF") //时钟秒指针的颜色

    private val scaleTimeWidth = UiCommon.dp2px(15) //刻度准点的长度
    private val scaleSmallWidth = UiCommon.dp2px(8) //刻度其余点的长度

    private var scaleTextSize = UiCommon.dp2px(18).toInt()

    private var currentHour: Int = 0 //当时小时 0-24
    private var currentMinute: Int = 0 //当前分钟 0-60
    private var currentSecond: Int = 0 //当前秒 0-60

    //当前绘制的画笔
    private val mPaint by lazy {
        Paint().apply {
            this@apply.isAntiAlias = true
        }
    }

    //当前绘制文字的画笔
    private val mTextPaint by lazy {
        Paint().apply {
            this@apply.isAntiAlias = true
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
        }
    }

    init {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet? = null) {
        if (attrs == null) return
        val a = context.obtainStyledAttributes(attrs, R.styleable.ClockView)
        circleWidth = a.getFloat(R.styleable.ClockView_clock_circle_width, circleWidth)
        circleWallWidth = a.getFloat(R.styleable.ClockView_clock_wall_width, circleWallWidth)
        wallColor = a.getColor(R.styleable.ClockView_clock_wall_color, wallColor)
        scaleSmallColor = a.getColor(R.styleable.ClockView_clock_scale_small_color, scaleSmallColor)
        scaleTimeColor = a.getColor(R.styleable.ClockView_clock_scale_time_color, scaleTimeColor)
        handHourColor = a.getColor(R.styleable.ClockView_clock_hand_hour_color, handHourColor)
        handMinuteColor = a.getColor(R.styleable.ClockView_clock_hand_minute_color, handMinuteColor)
        handSecondColor = a.getColor(R.styleable.ClockView_clock_hand_second_color, handSecondColor)
        scaleTextSize = a.getDimensionPixelSize(R.styleable.ClockView_clock_scale_time_size, scaleTextSize)
        a.recycle()
    }

    /**
     * 重新测量，获取最大宽高
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //每个矩形形的宽度
        var widthResult = if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) MeasureSpec.getSize(widthMeasureSpec) else circleWidth.toInt() //获取实际宽度
        //最终的高度
        var heightResult = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) MeasureSpec.getSize(heightMeasureSpec) else circleWidth.toInt() //获取实际高度
        //校验宽高，如果不等于，则给最小值
        if (heightResult != widthResult) {
            val min = widthResult.coerceAtMost(heightResult)
            widthResult = min
            heightResult = min
        }
        setMeasuredDimension(widthResult, heightResult)
    }


    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        drawWall(canvas)
        drawScale(canvas)
        drawScaleText(canvas)
        drawHand(canvas)
    }

    private fun drawHand(canvas: Canvas) {
        val mx = (width / 2).toFloat()
        val my = (height / 2).toFloat()
        val radius = circleWidth / 2 - circleWallWidth / 2
        drawSecond(canvas, mx, my, radius)
        drawMinute(canvas, mx, my, radius)
        drawHour(canvas, mx, my, radius)
    }

    /**
     * 绘制钟壁
     */
    private fun drawWall(canvas: Canvas) {
        mPaint.strokeWidth = circleWallWidth
        mPaint.color = wallColor
        mPaint.style = Paint.Style.STROKE
        val circleWidth = width.toFloat() - circleWallWidth / 2
        val start = circleWallWidth / 2
        canvas.drawArc(RectF(start, start, circleWidth, circleWidth), 0f, 360f, false, mPaint)
    }

    /**
     * 绘制刻度
     */
    private fun drawScale(canvas: Canvas) {
        mPaint.strokeWidth = 2f
        mPaint.style = Paint.Style.FILL
        val mx = (width / 2).toFloat()
        val my = (height / 2).toFloat()

        for (i in 1..60) {
            canvas.save()
            val startDashAngle = -90f + 6f * i
            Log.d("radianDashAngleTag", "drawScaleText-startDashAngle:$startDashAngle")
            canvas.rotate(startDashAngle, mx, my)
            val scaleWidth = if (i % 5 == 0) {
                mPaint.color = scaleTimeColor
                scaleTimeWidth
            } else {
                mPaint.color = scaleSmallColor
                scaleSmallWidth
            }
            canvas.drawLine(mx, circleWallWidth, mx, circleWallWidth.toBigDecimal().add(scaleWidth.toBigDecimal()).setScale(2, RoundingMode.HALF_DOWN).toFloat(), mPaint)
            canvas.restore()
        }
    }

    /**
     * 绘制刻度文字
     */
    private fun drawScaleText(canvas: Canvas) {
        mTextPaint.textSize = scaleTextSize.toFloat()
        val mx = (width / 2).toFloat()
        val my = (height / 2).toFloat()
        val radius = circleWidth / 2 - circleWallWidth / 2 - scaleTextSize - UiCommon.dp2px(2)
        Log.d("scaleTextTag", "radius:$radius")
        for (i in 1..12) {
            Log.d("scaleTextTag", "---------------------$i--------------------")
            val textWidth = mTextPaint.measureText(i.toString())
            val fl = 30f * i
            val angle = fl * Math.PI / 180  //当前旋转到的角度
            var sx = mx + sin(angle) * radius
            var sy = my - cos(angle) * radius
            if (fl > 0f && fl <= 90f) {
                sx -= (textWidth / 2)
                sy += (scaleTextSize / 2)
            }else if (fl > 90f && fl < 180f) {
                sx -= (textWidth / 2)
            } else if (fl > 180f && fl < 270f) {
                sx += (textWidth / 2)
            } else if (fl >= 270 && fl < 360f) {
                sx += (textWidth / 2)
                sy += (scaleTextSize / 2)
            } else if (fl == 360f) {
                sy += (scaleTextSize / 2)
            }
            canvas.drawText(i.toString(), sx.toFloat(), sy.toFloat(), mTextPaint)
        }
    }


    /**
     * 绘制时针
     */
    private fun drawHour(canvas: Canvas, mx: Float, my: Float, radius: Float) {
        val hour = if (currentHour > 12) {
            24 - currentHour.toFloat()
        } else currentHour.toFloat()
//        canvas.drawLine((width / 2).toFloat(), (height / 2).toFloat(), hx, hy, mPaint) // 小时
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = 3f
        mPaint.color = handHourColor
        Log.d("handTag", "hour:$hour,time:${360 / 12 * hour * (currentMinute / 60)}")
        val angle = (30 * hour + 30 * (currentMinute.toFloat() / 60)) * Math.PI / 180  //当前旋转到的角度
        Log.d("handTag", "angle:$angle")
        val sx = mx + sin(angle) * (radius - 80)
        val sy = my - cos(angle) * (radius - 80)
        Log.d("handTag", "sx:$sx,sy:$sy")
        canvas.drawLine((width / 2).toFloat(), (height / 2).toFloat(), sx.toFloat(), sy.toFloat(), mPaint)
    }

    /**
     * 绘制时针
     */
    private fun drawMinute(canvas: Canvas, mx: Float, my: Float, radius: Float) {
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = 3f
        mPaint.color = handMinuteColor

        val angle = (6 * currentMinute.toFloat() + 6 * (currentSecond.toFloat() / 60)) * Math.PI / 180  //当前旋转到的角度
        val sx = mx + sin(angle) * (radius - 50)
        val sy = my - cos(angle) * (radius - 50)
        canvas.drawLine((width / 2).toFloat(), (height / 2).toFloat(), sx.toFloat(), sy.toFloat(), mPaint)
    }

    /**
     * 绘制时针
     */
    private fun drawSecond(canvas: Canvas, mx: Float, my: Float, radius: Float) {
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = 3f
        mPaint.color = handSecondColor
        val angle = 6 * currentSecond * Math.PI / 180  //当前旋转到的角度
        val sx = mx + sin(angle) * (radius - 10)
        val sy = my - cos(angle) * (radius - 10)
        canvas.drawLine((width / 2).toFloat(), (height / 2).toFloat(), sx.toFloat(), sy.toFloat(), mPaint)
    }

    private var timeJob: Job? = null

    @Synchronized
    @MainThread
    fun startUpdateTime() {
        timeJob?.cancel()
        timeJob = appLifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)
                val second = cal.get(Calendar.SECOND)
                Log.d("CalendarTag", "hour:$hour,minute:$minute,second:$second")
                currentHour = hour
                currentMinute = minute
                currentSecond = second
                postInvalidate()
                delay(1000)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startUpdateTime()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopTime()
    }

    @Synchronized
    @MainThread
    fun stopTime() {
        timeJob?.cancel()
        timeJob = null
    }
}