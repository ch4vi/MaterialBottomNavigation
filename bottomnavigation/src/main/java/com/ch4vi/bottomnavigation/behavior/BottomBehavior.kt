package com.ch4vi.bottomnavigation.behavior

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorCompat
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import com.ch4vi.bottomnavigation.BottomNavigation
import com.ch4vi.bottomnavigation.BottomNavigation.Const.PENDING_ACTION_ANIMATE_ENABLED
import com.ch4vi.bottomnavigation.BottomNavigation.Const.PENDING_ACTION_COLLAPSED
import com.ch4vi.bottomnavigation.BottomNavigation.Const.PENDING_ACTION_EXPANDED
import com.ch4vi.bottomnavigation.BottomNavigation.Const.PENDING_ACTION_NONE
import com.ch4vi.bottomnavigation.R
import com.ch4vi.bottomnavigation.behavior.VerticalScrollingBehavior.Companion.ScrollDirection

interface OnExpandStatusChangeListener {
  fun onExpandStatusChanged(expanded: Boolean, animate: Boolean)
}

class BottomBehavior(
  context: Context,
  attrs: AttributeSet? = null
) : VerticalScrollingBehavior<BottomNavigation>(context, attrs) {

  var scrollable: Boolean
  private val scrollEnabled: Boolean
  private var enabled: Boolean = false

  /**
   * default hide/show interpolator
   */
  private val interpolator = LinearOutSlowInInterpolator()

  /**
   * show/hide animation duration
   */
  private val animationDuration: Int

  /**
   * bottom inset when TRANSLUCENT_NAVIGATION is turned on
   */
  private var bottomInset: Int = 0

  /**
   * bottom navigation real height
   */
  private var height: Int = 0

  /**
   * maximum scroll offset
   */
  private var maxOffset: Int = 0

  /**
   * true if the current configuration has the TRANSLUCENT_NAVIGATION turned on
   */
  private var translucentNavigation: Boolean = false

  /**
   * Minimum touch distance
   */
  private val scaledTouchSlop: Int

  /**
   * hide/show animator
   */
  private var animator: ViewPropertyAnimatorCompat? = null

  /**
   * current visibility status
   */
  private var hidden = false

  /**
   * current Y offset
   */
  private var offset: Int

  var listener: OnExpandStatusChangeListener? = null
  private var snackbarDependentView: SnackBarDependentView? = null

  init {
    val typedArray = context.obtainStyledAttributes(attrs,
        R.styleable.BottomNavigationBehavior)
    this.scrollable = typedArray.getBoolean(
        R.styleable.BottomNavigationBehavior_bbn_scrollEnabled, true)
    this.scrollEnabled = true
    this.animationDuration = typedArray.getInt(
        R.styleable.BottomNavigationBehavior_bbn_animationDuration,
        context.resources.getInteger(
            R.integer.bbn_hide_animation_duration)
    )
    this.scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop * 2
    this.offset = 0
    typedArray?.recycle()
  }

  fun isExpanded(): Boolean {
    return !hidden
  }

  fun setLayoutValues(bottomNavHeight: Int, bottomInset: Int) {
    this.height = bottomNavHeight
    this.bottomInset = bottomInset
    this.translucentNavigation = bottomInset > 0
    this.maxOffset = height + if (translucentNavigation) bottomInset else 0
    this.enabled = true
  }

  override fun layoutDependsOn(
    parent: CoordinatorLayout,
    child: BottomNavigation,
    dependency: View
  ): Boolean {
    return if (!enabled) false
    else isSnackbar(dependency)
  }

  override fun onLayoutChild(
    parent: CoordinatorLayout,
    child: BottomNavigation,
    layoutDirection: Int
  ): Boolean {
    val handled = super.onLayoutChild(parent, child, layoutDirection)

    val pendingAction = child.pendingAction
    if (pendingAction != PENDING_ACTION_NONE) {
      val animate = pendingAction and PENDING_ACTION_ANIMATE_ENABLED != 0
      if (pendingAction and PENDING_ACTION_COLLAPSED != 0) {
        setExpanded(child, false, animate)
      } else if (pendingAction and PENDING_ACTION_EXPANDED != 0) {
        setExpanded(child, true, animate)
      }

      // Finally reset the pending state
      child.resetPendingAction()
    }

    return handled
  }

  override fun onDependentViewRemoved(
    parent: CoordinatorLayout,
    child: BottomNavigation,
    dependency: View
  ) {
    if (isSnackbar(dependency)) {
      snackbarDependentView?.onDestroy()
      snackbarDependentView = null
    }
  }

  override fun onDependentViewChanged(
    parent: CoordinatorLayout,
    child: BottomNavigation,
    dependency: View
  ): Boolean {
    val result = super.onDependentViewChanged(parent, child, dependency)
    if (isSnackbar(dependency)) {
      if (snackbarDependentView == null) {
        snackbarDependentView =
            SnackBarDependentView(dependency as Snackbar.SnackbarLayout, height, bottomInset)
      }
      return snackbarDependentView?.onDependentViewChanged(parent, child) ?: result
    }
    return result
  }

  override fun onStartNestedScroll(
    coordinatorLayout: CoordinatorLayout,
    child: BottomNavigation,
    directTargetChild: View,
    target: View,
    axes: Int,
    type: Int
  ): Boolean {
    offset = 0
    if (!scrollable || !scrollEnabled) {
      return false
    }

    if (axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0) {
      if (target.isScrollContainer
          && !target.canScrollVertically(-1)
          && !target.canScrollVertically(1)) {
        return false
      }
    }

    return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes,
        type)
  }

  override fun onStopNestedScroll(
    coordinatorLayout: CoordinatorLayout,
    child: BottomNavigation,
    target: View,
    type: Int
  ) {
    super.onStopNestedScroll(coordinatorLayout, child, target, type)
    offset = 0
  }

  override fun onDirectionNestedPreScroll(
    coordinatorLayout: CoordinatorLayout,
    child: BottomNavigation,
    target: View, dx: Int, dy: Int, consumed: IntArray,
    @ScrollDirection scrollDirection: Int
  ) {

    // stop nested scroll if target is not scrollable
    if (target.isScrollContainer && !target.canScrollVertically(scrollDirection)) {
      ViewCompat.stopNestedScroll(target)
    }

    offset += dy
    if (offset > scaledTouchSlop) {
      handleDirection(child, SCROLL_DIRECTION_UP)
      offset = 0
    } else if (offset < -scaledTouchSlop) {
      handleDirection(child, SCROLL_DIRECTION_DOWN)
      offset = 0
    }
  }

  override fun onNestedDirectionFling(
    coordinatorLayout: CoordinatorLayout, child: BottomNavigation,
    target: View, velocityX: Float, velocityY: Float,
    @ScrollDirection scrollDirection: Int
  ): Boolean {

    if (Math.abs(velocityY) > 1000) handleDirection(child, scrollDirection)
    return true
  }

  override fun onNestedVerticalOverScroll(
    coordinatorLayout: CoordinatorLayout, child: BottomNavigation,
    @ScrollDirection direction: Int, currentOverScroll: Int, totalOverScroll: Int
  ) {
  }

  private fun isSnackbar(view: View): Boolean {
    return view is Snackbar.SnackbarLayout
  }

  private fun handleDirection(child: BottomNavigation, scrollDirection: Int) {
    if (!enabled || !scrollable || !scrollEnabled) {
      return
    }
    if (scrollDirection == SCROLL_DIRECTION_DOWN && hidden) {
      setExpanded(child, true, true)
    } else if (scrollDirection == SCROLL_DIRECTION_UP && !hidden) {
      setExpanded(child, false, true)
    }
  }

  private fun setExpanded(
    child: BottomNavigation, expanded: Boolean,
    animate: Boolean
  ) {
    animateOffset(child, if (expanded) 0 else maxOffset, animate)
    listener?.onExpandStatusChanged(expanded, animate)
  }

  private fun animateOffset(
    child: BottomNavigation,
    offset: Int,
    animate: Boolean
  ) {
    hidden = offset != 0
    ensureOrCancelAnimator(child)

    if (animate) animator?.translationY(offset.toFloat())?.start()
    else child.translationY = offset.toFloat()
  }

  private fun ensureOrCancelAnimator(child: BottomNavigation) {
    if (animator == null) {
      animator = ViewCompat.animate(child)
      animator?.duration = animationDuration.toLong()
      animator?.interpolator = interpolator
    } else {
      animator?.cancel()
    }
  }
}