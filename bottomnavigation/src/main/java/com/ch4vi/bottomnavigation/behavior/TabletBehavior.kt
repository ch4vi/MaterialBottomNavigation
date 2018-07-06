package com.ch4vi.bottomnavigation.behavior

import android.content.Context
import android.os.Build
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.ch4vi.bottomnavigation.BottomNavigation
import com.ch4vi.bottomnavigation.behavior.VerticalScrollingBehavior.Companion.ScrollDirection

class TabletBehavior(
  context: Context,
  attrs: AttributeSet? = null
) : VerticalScrollingBehavior<BottomNavigation>(context, attrs) {
  private var topInset: Int = 0
  private var enabled: Boolean = false
  private var width: Int = 0
  private var translucentStatus: Boolean = false

  fun setLayoutValues(bottomNavWidth: Int, topInset: Int, translucentStatus: Boolean) {
    this.translucentStatus = translucentStatus
    this.width = bottomNavWidth
    this.topInset = topInset
    this.enabled = true
  }

  override fun layoutDependsOn(
    parent: CoordinatorLayout, child: BottomNavigation, dependency: View
  ): Boolean {
    return dependency is AppBarLayout || dependency is Toolbar
  }

  override fun onDependentViewChanged(
    parent: CoordinatorLayout, child: BottomNavigation, dependency: View
  ): Boolean {
    (child.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
      val top = when {
        Build.VERSION.SDK_INT > 19 -> topInset
        translucentStatus -> topInset
        else -> 0
      }
      it.topMargin = Math.max(
          dependency.top + dependency.height - top,
          if (translucentStatus) 0 else -top
      )

      if (translucentStatus) {
        if (it.topMargin < top) child.setPadding(0, top - it.topMargin, 0, 0)
        else child.setPadding(0, 0, 0, 0)
      }

      child.requestLayout()
    }
    return true
  }

  override fun onNestedVerticalOverScroll(
    coordinatorLayout: CoordinatorLayout, child: BottomNavigation,
    @ScrollDirection direction: Int, currentOverScroll: Int, totalOverScroll: Int
  ) {
  }

  override fun onDirectionNestedPreScroll(
    coordinatorLayout: CoordinatorLayout, child: BottomNavigation, target: View,
    dx: Int, dy: Int, consumed: IntArray, @ScrollDirection scrollDirection: Int
  ) {
  }

  override fun onNestedDirectionFling(
    coordinatorLayout: CoordinatorLayout, child: BottomNavigation, target: View,
    velocityX: Float, velocityY: Float, @ScrollDirection scrollDirection: Int
  ): Boolean {
    return false
  }
}