package com.prempk.personalai

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.min

/** Lightweight animated voice orb; no image assets or network dependencies. */
class SiriOrbView(context: Context) : View(context) {
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.2f * resources.displayMetrics.density
    }
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var phase = 0f
    private var listening = false
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 3200L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        animator.start()
    }

    fun setListening(active: Boolean) {
        listening = active
        animator.duration = if (active) 1500L else 3200L
        if (!animator.isStarted) animator.start()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val base = min(width, height) * 0.34f
        val pulse = if (listening) 0.07f else 0.025f
        val breathe = 1f + pulse * kotlin.math.sin(phase * Math.PI * 2).toFloat()
        val radius = base * breathe

        glowPaint.shader = RadialGradient(
            cx, cy, radius * 1.9f,
            intArrayOf(0x665D5BFF, 0x3333B8F2, 0x0033B8F2),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius * 1.9f, glowPaint)

        orbPaint.shader = RadialGradient(
            cx - radius * 0.28f, cy - radius * 0.38f, radius * 1.55f,
            intArrayOf(0xFFFFE8FF.toInt(), 0xFF9A7BFF.toInt(), 0xFF477CF4.toInt(), 0xFF142B72.toInt()),
            floatArrayOf(0f, 0.28f, 0.68f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, orbPaint)

        ringPaint.shader = null
        ringPaint.color = 0x99B6D7FF.toInt()
        ringPaint.alpha = if (listening) 190 else 110
        val ringRect = RectF(cx - radius * 1.32f, cy - radius * 1.32f,
            cx + radius * 1.32f, cy + radius * 1.32f)
        canvas.drawArc(ringRect, phase * 360f, 210f, false, ringPaint)
        ringPaint.color = 0x8875D9FF.toInt()
        val secondRect = RectF(cx - radius * 1.48f, cy - radius * 1.48f,
            cx + radius * 1.48f, cy + radius * 1.48f)
        canvas.drawArc(secondRect, 180f - phase * 360f, 115f, false, ringPaint)

        corePaint.shader = RadialGradient(
            cx - radius * 0.35f, cy - radius * 0.45f, radius * 0.85f,
            intArrayOf(0x99FFFFFF.toInt(), 0x22FFFFFF, 0x00FFFFFF),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius * 0.94f, corePaint)
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }
}
