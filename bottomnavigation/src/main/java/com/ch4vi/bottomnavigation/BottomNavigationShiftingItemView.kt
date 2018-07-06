package com.ch4vi.bottomnavigation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.animation.DecelerateInterpolator
import com.ch4vi.bottomnavigation.BottomNavigationItemView.Const.ALPHA_MAX
import com.ch4vi.bottomnavigation.menu.MenuParser

class BottomNavigationShiftingItemView(
  parent: BottomNavigation,
  expanded: Boolean,
  menu: MenuParser.Menu
) : BottomNavigationItemView(parent, expanded, menu) {
  private val topPadding = resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_top)
  private val paddingBottomActive =
      resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_active)
  private val iconSize = resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_icon_size)
  private val paddingBottomInactive =
      resources.getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_inactive)
  private val textSize = resources.getDimensionPixelSize(R.dimen.bbn_shifting_text_size)

  private val interpolator = DecelerateInterpolator()
  private val animationDuration = menu.itemAnimationDuration.toLong()
  private val colorActive = menu.getColorActive()
  private val colorInactive = menu.getColorInactive()
  private val colorDisabled = menu.getColorDisabled()

  private val alphaActive: Float
  private val alphaInactive: Float
  private val alphaDisabled: Float
  private var textWidth: Float = 0f
  private var textX: Float = 0f
  private var textY: Int = 0

  private var centerY = 0
    set(value) {
      field = value
      requestLayout()
    }

  init {
    this.alphaInactive = Color.alpha(this.colorInactive) / ALPHA_MAX
    this.alphaDisabled = Color.alpha(this.colorDisabled) / ALPHA_MAX
    this.alphaActive = Math.max(Color.alpha(colorActive).toFloat() / ALPHA_MAX, alphaInactive)

    this.centerY = if (expanded) topPadding else paddingBottomInactive
    this.textPaint.hinting = Paint.HINTING_ON
    this.textPaint.isLinearText = true
    this.textPaint.isSubpixelText = true
    this.textPaint.textSize = textSize.toFloat()
    this.textPaint.color = colorActive

    if (!expanded) this.textPaint.alpha = 0
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)

    textPaint.alpha =
        ((if (isExpanded) if (enabled) alphaActive else alphaDisabled else 0f) * ALPHA_MAX).toInt()
    icon?.let {
      updateLayoutOnAnimation(layoutParams.width, 1f, isExpanded)
    }
    requestLayout()
  }

  override fun onStatusChanged(expanded: Boolean, size: Int, animate: Boolean) {
    if (!animate) {
      updateLayoutOnAnimation(size, 1f, expanded)
      centerY = (if (expanded) topPadding else paddingBottomInactive)
      return
    }

    val set = AnimatorSet()
    set.duration = animationDuration * 2
    set.interpolator = interpolator
    val animator1 = ValueAnimator.ofInt(layoutParams.width, size)
    val animator2 =
        ObjectAnimator.ofInt(this, "centerY",
            if (expanded) paddingBottomInactive else topPadding,
            if (expanded) topPadding else paddingBottomInactive
        )

    animator1.addUpdateListener { animation ->
      (animation.animatedValue as? Int)?.let {
        val fraction = animation.animatedFraction
        updateLayoutOnAnimation(it, fraction, expanded)
      }
    }
    set.playTogether(animator1, animator2)
    set.start()
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)

    if (this.icon == null) {
      this.icon = item?.getIcon(context)?.mutate()
      icon?.setBounds(0, 0, iconSize, iconSize)
      icon?.setColorFilter(
          if (isExpanded)
            if (isEnabled) colorActive else colorDisabled
          else
            if (isEnabled) colorInactive else colorDisabled,
          PorterDuff.Mode.SRC_ATOP
      )

      icon?.alpha = (
          if (isExpanded)
        (if (isEnabled) alphaActive else alphaDisabled) * ALPHA_MAX
      else
        (if (isEnabled) alphaInactive else alphaDisabled) * ALPHA_MAX).toInt()
    }

    if (textDirty) {
      measureText()
      textDirty = false
    }

    if (changed) {
      val w = right - left
      val h = bottom - top
      val centerX = (w - iconSize) / 2
      this.textY = h - paddingBottomActive
      this.textX = (w - textWidth) / 2
      icon?.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize)
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    icon?.draw(canvas)
    item?.let {
      canvas.drawText(
          it.title,
          textX,
          textY.toFloat(),
          textPaint
      )
    }
    drawBadge(canvas)
  }

  private fun updateLayoutOnAnimation(
    size: Int, fraction: Float,
    expanded: Boolean
  ) {
    layoutParams.width = size
    val enabled = isEnabled
    val color: Int

    val srcColor = if (enabled) if (expanded) colorInactive else colorActive else colorDisabled
    val dstColor = if (enabled) if (expanded) colorActive else colorInactive else colorDisabled
    val srcAlpha = if (enabled) alphaInactive else alphaDisabled
    val dstAlpha = if (enabled) alphaActive else alphaDisabled
    if (expanded) {
      color = (evaluator.evaluate(fraction, srcColor, dstColor) as Number).toInt()
      icon?.alpha = ((srcAlpha + fraction * (dstAlpha - srcAlpha)) * ALPHA_MAX).toInt()
      textPaint.alpha = (fraction * dstAlpha * ALPHA_MAX).toInt()
    } else {
      color = evaluator.evaluate(fraction, srcColor, dstColor) as Int
      val alpha = 1f - fraction
      icon?.alpha = ((srcAlpha + alpha * (dstAlpha - srcAlpha)) * ALPHA_MAX).toInt()
      textPaint.alpha = (alpha * dstAlpha * ALPHA_MAX).toInt()
    }

    icon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
  }

  private fun measureText() {
    item?.let {
      this.textWidth = textPaint.measureText(it.title)
    }
  }

}