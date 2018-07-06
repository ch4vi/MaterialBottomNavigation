package com.ch4vi.bottomnavigation.behavior

import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar.SnackbarLayout
import android.view.View
import android.view.ViewGroup
import com.ch4vi.bottomnavigation.BottomNavigation

internal abstract class DependentView<V : View> internal constructor(
  protected val child: V,
  protected var height: Int,
  protected val bottomInset: Int
) {
  protected val layoutParams: ViewGroup.MarginLayoutParams =
      child.layoutParams as ViewGroup.MarginLayoutParams
  private val bottomMargin: Int
  private val originalPosition = child.translationY

  init {
    this.bottomMargin = layoutParams.bottomMargin
  }

  internal fun onDestroy() {
    layoutParams.bottomMargin = bottomMargin
    child.translationY = originalPosition
    child.requestLayout()
  }

  internal abstract fun onDependentViewChanged(
    parent: CoordinatorLayout,
    navigation: BottomNavigation
  ): Boolean
}

internal class SnackBarDependentView internal constructor(
  child: SnackbarLayout,
  height: Int,
  bottomInset: Int
) : DependentView<SnackbarLayout>(child, height, bottomInset) {
  private var snackbarHeight = -1

  override fun onDependentViewChanged(
    parent: CoordinatorLayout,
    navigation: BottomNavigation
  ): Boolean {

    if (Build.VERSION.SDK_INT < 21) {
      val index1 = parent.indexOfChild(child)
      val index2 = parent.indexOfChild(navigation)
      if (index1 > index2) {
        navigation.bringToFront()
      }
    }

    if (snackbarHeight == -1) {
      snackbarHeight = child.height
    }

    val maxScroll = Math.max(0f, navigation.translationY - bottomInset)
    val newBottomMargin = (height - maxScroll).toInt()

    if (layoutParams.bottomMargin != newBottomMargin) {
      layoutParams.bottomMargin = newBottomMargin
      child.requestLayout()
      return true
    }
    return false
  }
}