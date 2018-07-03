package com.ch4vi.bottomnavigation

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.SystemClock

class BadgeDrawable(private val color: Int, private val size: Int) : Drawable() {
  private object Const {
    const val FADE_DURATION = 100f
    const val ALPHA_MAX = 255
  }

  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private var startTimeMillis: Long = 0
  var animating: Boolean = false

  override fun draw(canvas: Canvas?) {
    canvas ?: return
    if (!animating) {
      paint.alpha = Const.ALPHA_MAX
      drawInternal(canvas)
    } else {
      if (startTimeMillis == 0L) startTimeMillis = SystemClock.uptimeMillis()

      val normalized = (SystemClock.uptimeMillis() - startTimeMillis) / Const.FADE_DURATION
      if (normalized >= 1f) {
        animating = false
        paint.alpha = Const.ALPHA_MAX
        drawInternal(canvas)
      } else {
        val partialAlpha = (Const.ALPHA_MAX * normalized).toInt()
        alpha = partialAlpha
        drawInternal(canvas)
      }
    }
  }

  private fun drawInternal(canvas: Canvas) {
    val bounds = bounds
    val w = bounds.width()
    val h = bounds.height()
    canvas.drawCircle(
        (bounds.centerX() + w / 2).toFloat(),
        (bounds.centerY() - h / 2).toFloat(),
        (w / 2).toFloat(), paint)
  }

  override fun setAlpha(alpha: Int) {
    paint.alpha = alpha
    invalidateSelf()
  }

  override fun getAlpha(): Int {
    return paint.alpha
  }

  override fun isStateful(): Boolean {
    return false
  }

  override fun getOpacity(): Int {
    return PixelFormat.TRANSLUCENT
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    paint.colorFilter = colorFilter
    invalidateSelf()
  }

  override fun getIntrinsicHeight(): Int {
    return size
  }

  override fun getIntrinsicWidth(): Int {
    return size
  }
}