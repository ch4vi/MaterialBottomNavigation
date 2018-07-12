package com.ch4vi.bottomnavigation.badge

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.SystemClock

class BadgeDrawable(
  color: Int,
  private val size: Int,
  private val position: Int = 0
) : Drawable() {
  private object Const {
    const val FADE_DURATION = 100f
    const val ALPHA_MAX = 255

    const val TOP_RIGHT = 0
    const val TOP_LEFT = 1
  }

  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private var startTimeMillis: Long = 0
  var animating: Boolean = false

  init {
    paint.color = color
  }

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

    when (position) {
      Const.TOP_LEFT -> {
        canvas.drawCircle(
            (bounds.centerX() - w * 2.5f),
            (bounds.centerY() - h / 2f),
            (w / 2f), paint)
      }
      Const.TOP_RIGHT -> {
        canvas.drawCircle(
            (bounds.centerX() + w / 2f),
            (bounds.centerY() - h / 2f),
            (w / 2f), paint)
      }
    }
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