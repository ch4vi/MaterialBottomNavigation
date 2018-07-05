package com.ch4vi.bottomnavigation.behavior

import android.content.Context
import android.support.annotation.IntDef
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View

abstract class VerticalScrollingBehavior<V : View> @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : CoordinatorLayout.Behavior<V>(context, attrs) {

  private var mTotalDyUnconsumed = 0
  private var mTotalDy = 0
  @ScrollDirection
  private var mOverScrollDirection = SCROLL_NONE
  @ScrollDirection
  private var mScrollDirection = SCROLL_NONE

  companion object {

    @IntDef(SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ScrollDirection

    const val SCROLL_DIRECTION_UP = 1
    const val SCROLL_DIRECTION_DOWN = -1
    const val SCROLL_NONE = 0
  }

  /**
   * @return Over scroll direction: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN, SCROLL_NONE
   */
  @ScrollDirection
  fun getOverScrollDirection(): Int {
    return mOverScrollDirection
  }

  /**
   * @return Scroll direction: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN, SCROLL_NONE
   */

  @ScrollDirection
  fun getScrollDirection(): Int {
    return mScrollDirection
  }

  /**
   * @param direction Direction of the over scroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
   * @param currentOverScroll Unconsumed value, negative or positive based on the direction;
   * @param totalOverScroll Cumulative value for current direction
   */
  abstract fun onNestedVerticalOverScroll(
    coordinatorLayout: CoordinatorLayout, child: V, @ScrollDirection direction: Int,
    currentOverScroll: Int, totalOverScroll: Int
  )

  /**
   * @param scrollDirection Direction of the over scroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
   */
  abstract fun onDirectionNestedPreScroll(
    coordinatorLayout: CoordinatorLayout,
    child: V,
    target: View,
    dx: Int,
    dy: Int,
    consumed: IntArray,
    @ScrollDirection scrollDirection: Int
  )

  override fun onStartNestedScroll(
    coordinatorLayout: CoordinatorLayout, child: V,
    directTargetChild: View, target: View, axes: Int, type: Int
  ): Boolean {
    return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
  }

  override fun onNestedScroll(
    coordinatorLayout: CoordinatorLayout, child: V,
    target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
    type: Int
  ) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
        dyUnconsumed, type)
    if (dyUnconsumed > 0 && mTotalDyUnconsumed < 0) {
      mTotalDyUnconsumed = 0
      mOverScrollDirection = SCROLL_DIRECTION_UP
    } else if (dyUnconsumed < 0 && mTotalDyUnconsumed > 0) {
      mTotalDyUnconsumed = 0
      mOverScrollDirection = SCROLL_DIRECTION_DOWN
    }
    mTotalDyUnconsumed += dyUnconsumed
    onNestedVerticalOverScroll(coordinatorLayout, child, mOverScrollDirection, dyConsumed,
        mTotalDyUnconsumed)
  }

  override fun onNestedPreScroll(
    coordinatorLayout: CoordinatorLayout, child: V,
    target: View, dx: Int, dy: Int, consumed: IntArray, type: Int
  ) {
    super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    if (dy > 0 && mTotalDy < 0) {
      mTotalDy = 0
      mScrollDirection = SCROLL_DIRECTION_UP
    } else if (dy < 0 && mTotalDy > 0) {
      mTotalDy = 0
      mScrollDirection = SCROLL_DIRECTION_DOWN
    }
    mTotalDy += dy
    onDirectionNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed,
        mScrollDirection)
  }

  override fun onNestedFling(
    coordinatorLayout: CoordinatorLayout, child: V,
    target: View, velocityX: Float, velocityY: Float, consumed: Boolean
  ): Boolean {
    super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    mScrollDirection =
        if (velocityY > 0) SCROLL_DIRECTION_UP else SCROLL_DIRECTION_DOWN
    return onNestedDirectionFling(coordinatorLayout, child, target, velocityX, velocityY,
        mScrollDirection)
  }

  protected abstract fun onNestedDirectionFling(
    coordinatorLayout: CoordinatorLayout,
    child: V,
    target: View,
    velocityX: Float,
    velocityY: Float,
    @ScrollDirection scrollDirection: Int
  ): Boolean
}