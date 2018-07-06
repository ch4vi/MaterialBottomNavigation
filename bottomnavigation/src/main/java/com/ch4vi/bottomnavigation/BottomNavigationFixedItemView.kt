package com.ch4vi.bottomnavigation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.support.v4.view.ViewCompat
import android.view.animation.DecelerateInterpolator
import com.ch4vi.bottomnavigation.menu.MenuParser

class BottomNavigationFixedItemView(
  parent: BottomNavigation,
  expanded: Boolean,
  menu: MenuParser.Menu
) : BottomNavigationItemView(parent, expanded, menu) {
  private object Const {
    const val TEXT_SCALE_ACTIVE = 1.1666666667f
  }

  private val iconSize: Int = resources.getDimensionPixelSize(R.dimen.bbn_fixed_item_icon_size)
  private val paddingTopActive =
      resources.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_top_active)
  private val paddingTopInactive =
      resources.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_top_inactive)
  private val bottomPadding = resources.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_bottom)
  private val paddingHorizontal =
      resources.getDimensionPixelSize(R.dimen.bbn_fixed_item_padding_horizontal)
  private val textSizeInactive =
      resources.getDimensionPixelSize(R.dimen.bbn_fixed_text_size_inactive)

  private val interpolator = DecelerateInterpolator()
  private val animationDuration = menu.itemAnimationDuration.toLong()
  private val colorActive = menu.getColorActive()
  private val colorInactive = menu.getColorInactive()
  private val colorDisabled = menu.getColorDisabled()

  private var centerY = paddingTopActive
    set(value) {
      field = value
      ViewCompat.postInvalidateOnAnimation(this)
    }

  private var canvasTextScale = if (expanded) Const.TEXT_SCALE_ACTIVE else 1f
    set(value) {
      field = value
      ViewCompat.postInvalidateOnAnimation(this)
    }

  private var iconTranslation =
      (if (expanded) 0 else paddingTopInactive - paddingTopActive).toFloat()
    set(value) {
      field = value
      ViewCompat.postInvalidateOnAnimation(this)
    }

  private var textWidth: Float = 0f
  private var textCenterX: Int = 0
  private var textCenterY: Int = 0
  private var centerX: Int = 0
  private var textX: Float = 0f
  private var textY: Float = 0f

  init {
    this.textPaint.color = Color.WHITE
    this.textPaint.hinting = Paint.HINTING_ON
    this.textPaint.isLinearText = true
    this.textPaint.isSubpixelText = true
    this.textPaint.textSize = textSizeInactive.toFloat()
    this.textPaint.color = if (expanded) colorActive else colorInactive
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    this.textPaint.color =
        if (isExpanded) if (enabled) colorActive else colorDisabled
        else if (enabled) colorInactive else colorDisabled
    icon?.let { updateLayoutOnAnimation(1f, isExpanded) }
    requestLayout()
  }

  override fun onStatusChanged(expanded: Boolean, size: Int, animate: Boolean) {

    if (!animate) {
      updateLayoutOnAnimation(1f, expanded)
      iconTranslation = ((if (expanded) 0 else paddingTopInactive - paddingTopActive).toFloat())
      return
    }

    val set = AnimatorSet()
    set.duration = animationDuration
    set.interpolator = interpolator

    val animator1 =
        ObjectAnimator.ofFloat(this, "canvasTextScale",
            if (expanded) Const.TEXT_SCALE_ACTIVE else 1f)

    animator1.addUpdateListener { animation ->
      updateLayoutOnAnimation(animation.animatedFraction, expanded)
    }

    val animator2 = ObjectAnimator.ofFloat(this, "iconTranslation",
        (if (expanded) 0 else paddingTopInactive - paddingTopActive).toFloat())

    set.playTogether(animator1, animator2)
    set.start()
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)

    if (this.icon == null) {
      this.icon = item?.getIcon(context)?.mutate()
      val color =
          if (isExpanded) if (isEnabled) colorActive else colorDisabled
          else if (isEnabled) colorInactive else colorDisabled

      this.icon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
      this.icon?.setBounds(0, 0, iconSize, iconSize)
      this.icon?.alpha = Color.alpha(color)
    }

    if (changed) {
      val w = right - left
      centerX = (w - iconSize) / 2
      icon?.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize)
    }

    if (textDirty || changed) {
      measureText()
      textDirty = false
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    canvas.save()
    canvas.translate(0f, iconTranslation)
    icon?.draw(canvas)
    drawBadge(canvas)
    canvas.restore()

    canvas.save()
    canvas.scale(canvasTextScale, canvasTextScale, textCenterX.toFloat(), textCenterY.toFloat())

    item?.let {
      canvas.drawText(
          it.title,
          textX,
          textY,
          textPaint
      )
    }

    canvas.restore()

  }

  private fun measureText() {
    val width = width
    val height = height
    item?.let {
      textWidth = textPaint.measureText(it.title)
      textX = paddingHorizontal + (width - paddingHorizontal * 2 - textWidth) / 2
      textY = (height - bottomPadding).toFloat()
      textCenterX = width / 2
      textCenterY = height - bottomPadding
    }
  }

  private fun updateLayoutOnAnimation(fraction: Float, expanded: Boolean) {
    val dstColor = if (isEnabled) if (expanded) colorActive else colorInactive else colorDisabled
    val srcColor = if (isEnabled) if (expanded) colorInactive else colorActive else colorDisabled
    val color = evaluator.evaluate(fraction, srcColor, dstColor) as Int

    icon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    icon?.alpha = Color.alpha(color)
    textPaint.color = color
    ViewCompat.postInvalidateOnAnimation(this)
  }
}