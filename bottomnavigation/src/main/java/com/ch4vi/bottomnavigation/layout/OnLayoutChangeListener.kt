package com.ch4vi.bottomnavigation.layout

import android.graphics.Rect
import android.view.View
import com.ch4vi.bottomnavigation.BottomNavigation
import com.ch4vi.bottomnavigation.BottomNavigationItemView

class OnLayoutChangeListener(
  private val bottomNavigation: BottomNavigation
) : View.OnLayoutChangeListener {
  var view: BottomNavigationItemView? = null
  private val outRect = Rect()

  override fun onLayoutChange(
    unused: View,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    oldLeft: Int,
    oldTop: Int,
    oldRight: Int,
    oldBottom: Int
  ) {
    view?.let {
      it.getHitRect(outRect)

      val centerX = bottomNavigation.rippleOverlay.width / 2
      val centerY = bottomNavigation.rippleOverlay.height / 2
      bottomNavigation.rippleOverlay.translationX = (outRect.centerX() - centerX).toFloat()
      bottomNavigation.rippleOverlay.translationY = (outRect.centerY() - centerY).toFloat()
    }
  }

  fun forceLayout(v: View) {
    (v as? BottomNavigationItemView)?.let {
      view = it
      onLayoutChange(it, it.left, it.top, it.right, it.bottom, 0, 0, 0, 0)
    }
  }
}