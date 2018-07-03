package com.econocom.bottomnavigationbar.custom

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.ch4vi.bottomnavigation.menu.MenuParser
import com.econocom.bottomnavigationbar.custom.menu.OnMenuChangedListener
import com.econocom.bottomnavigationbar.custom.menu.OnMenuItemSelectionListener
import java.lang.ref.SoftReference

class BottomNavigation : FrameLayout, OnItemClickListener {

  private object Const {
    const val PENDING_ACTION_NONE = 0x0
    const val PENDING_ACTION_EXPANDED = 0x1
    const val PENDING_ACTION_COLLAPSED = 0x2
    const val PENDING_ACTION_ANIMATE_ENABLED = 0x4
  }

  // region parameters

  /**
   * Current pending action (used inside the BottomBehavior instance)
   */
  private var mPendingAction = Const.PENDING_ACTION_NONE

  /**
   * This is the amount of space we have to cover in case there's a translucent navigation
   * enabled.
   */
  private var bottomInset: Int = 0

  /**
   * This is the amount of space we have to cover in case there's a translucent status
   * enabled.
   */
  private var topInset: Int = 0

  /**
   * This is the current view height. It does take into account the extra space
   * used in case we have to cover the navigation translucent area, and neither the shadow height.
   */
  private var defaultHeight: Int = 0

  /**
   * Same as defaultHeight, but for tablet mode.
   */
  private var defaultWidth: Int = 0

  /**
   * Shadow is created above the widget background. It simulates the
   * elevation.
   */
  private var shadowHeight: Int = 0

  /**
   * Layout container used to create and manage the UI items.
   * It can be either Fixed or Shifting, based on the widget `mode`
   */
  private var itemsContainer: ItemsLayoutContainer? = null

  /**
   * This is where the color animation is happening
   */
  private var backgroundOverlay: View? = null

  /**
   * View used to show the press ripple overlay. I don't use the drawable in item view itself
   * because the ripple background will be clipped inside its bounds
   */
  private var rippleOverlay: View? = null

  /**
   * Toggle the ripple background animation on item press
   */
  private var enabledRippleBackground: Boolean = false

  /**
   * current menu
   */
  internal var menu: MenuParser.Menu? = null

  private var pendingMenu: MenuParser.Menu? = null

  /**
   * Default selected index.
   * After the items are populated changing this
   * won't have any effect
   */
  private var defaultSelectedIndex = 0

  /**
   * View visible background color
   */
  private var backgroundDrawable: ColorDrawable? = null

  /**
   * Animation duration for the background color change
   */
  private var backgroundColorAnimation: Long = 0

  /**
   * Optional typeface used for the items' text labels
   */
  internal var typeface: SoftReference<Typeface>? = null

  /**
   * Current BottomBehavior assigned from the CoordinatorLayout
   */
  private var mBehavior: CoordinatorLayout.Behavior<*>? = null

  /**
   * Menu selection listener
   */
  private var listener: OnMenuItemSelectionListener? = null

  /**
   * Menu changed listener
   */
  private var menuChangedListener: OnMenuChangedListener? = null

  /**
   * The user defined layout_gravity
   */
  private var gravity: Int = 0

  /**
   * View is attached
   */
  private var attached: Boolean = false

  private var badgeProvider: BadgeProvider? = null

  // endregion

  constructor(context: Context) : super(context, null)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
//    initialize(context, attrs, 0, 0)
  }

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
      super(context, attrs, defStyleAttr) {

//    initialize(context, attrs, defStyleAttr, 0)
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int,
    defStyleRes: Int
  ) : super(context, attrs, defStyleAttr, defStyleRes) {
//    initialize(context, attrs, defStyleAttr, defStyleRes)
  }

















}