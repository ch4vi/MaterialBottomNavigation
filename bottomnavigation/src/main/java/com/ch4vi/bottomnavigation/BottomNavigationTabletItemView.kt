package com.ch4vi.bottomnavigation

import android.animation.ObjectAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.view.ViewCompat
import android.view.animation.DecelerateInterpolator
import com.ch4vi.bottomnavigation.menu.MenuParser

class BottomNavigationTabletItemView(
  parent: BottomNavigation,
  expanded: Boolean,
  menu: MenuParser.Menu
) : BottomNavigationItemView(parent, expanded, menu) {

  private val interpolator = DecelerateInterpolator()
  private val iconSize = resources.getDimensionPixelSize(R.dimen.bbn_tablet_item_icon_size)
  private val animationDuration: Long = menu.itemAnimationDuration.toLong()
  private val colorActive = menu.getColorActive()
  private val colorInactive = menu.getColorInactive()
  private val colorDisabled = menu.getColorDisabled()

  override fun onStatusChanged(expanded: Boolean, size: Int, animate: Boolean) {
    if (!animate) {
      updateLayoutOnAnimation(1f, expanded)
      return
    }

    val animator = ObjectAnimator.ofFloat(0f, 1f)
    animator.addUpdateListener { animation ->
      updateLayoutOnAnimation(animation.animatedFraction, expanded)
    }
    animator.duration = animationDuration
    animator.interpolator = interpolator
    animator.start()
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)

    if (icon == null) {
      this.icon = item?.getIcon(context)?.mutate()
      this.icon?.setColorFilter(
          if (isExpanded)
            if (isEnabled) colorActive else colorDisabled
          else
            if (isEnabled) colorInactive else colorDisabled,
          PorterDuff.Mode.SRC_ATOP
      )
      this.icon?.alpha = Color.alpha(
          if (isExpanded)
            if (isEnabled) colorActive else colorDisabled
          else
            if (isEnabled) colorInactive else colorDisabled)
      this.icon?.setBounds(0, 0, iconSize, iconSize)
    }

    if (changed) {
      val w = right - left
      val h = bottom - top
      val centerX = (w - iconSize) / 2
      val centerY = (h - iconSize) / 2
      icon?.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize)
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    icon?.draw(canvas)
    drawBadge(canvas)
  }

  private fun updateLayoutOnAnimation(fraction: Float, expanded: Boolean) {
    val dstColor = if (isEnabled) if (expanded) colorActive else colorInactive else colorDisabled
    val srcColor = if (isEnabled) if (expanded) colorInactive else colorActive else colorDisabled
    val color = evaluator.evaluate(fraction, srcColor, dstColor) as Int

    icon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    icon?.alpha = Color.alpha(color)

    ViewCompat.postInvalidateOnAnimation(this)
  }
}