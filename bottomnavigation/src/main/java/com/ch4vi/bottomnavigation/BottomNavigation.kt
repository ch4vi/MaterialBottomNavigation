package com.ch4vi.bottomnavigation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IdRes
import android.support.annotation.MenuRes
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ch4vi.bottomnavigation.badge.BadgeProvider
import com.ch4vi.bottomnavigation.behavior.BottomBehavior
import com.ch4vi.bottomnavigation.behavior.TabletBehavior
import com.ch4vi.bottomnavigation.layout.FixedLayout
import com.ch4vi.bottomnavigation.layout.ItemsLayoutContainer
import com.ch4vi.bottomnavigation.layout.OnItemClickListener
import com.ch4vi.bottomnavigation.layout.ShiftingLayout
import com.ch4vi.bottomnavigation.layout.TabletLayout
import com.ch4vi.bottomnavigation.menu.MenuParser
import com.ch4vi.bottomnavigation.menu.OnMenuChangedListener
import com.ch4vi.bottomnavigation.menu.OnMenuItemSelectionListener
import java.lang.ref.SoftReference
import com.ch4vi.bottomnavigation.layout.OnLayoutChangeListener as LayoutChangeListener

class BottomNavigation @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    OnItemClickListener {
  private val tag = BottomNavigation::class.java.simpleName

  object Const {
    const val PENDING_ACTION_NONE = 0x0
    const val PENDING_ACTION_EXPANDED = 0x1
    const val PENDING_ACTION_COLLAPSED = 0x2
    const val PENDING_ACTION_ANIMATE_ENABLED = 0x4
  }

  // region parameters

  /**
   * Current pending action (used inside the BottomBehavior instance)
   */
  var mPendingAction = Const.PENDING_ACTION_NONE
    private set

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
  private var backgroundOverlay: View

  /**
   * View used to show the press ripple overlay. I don't use the drawable in item view itself
   * because the ripple background will be clipped inside its bounds
   */
  internal var rippleOverlay: View

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
  private var defaultSelectedIndex: Int

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
  // TODO internal
   var typeface: SoftReference<Typeface> = SoftReference(Typeface.DEFAULT)

  /**
   * Current BottomBehavior assigned from the CoordinatorLayout
   */
  private var mBehavior: CoordinatorLayout.Behavior<*>? = null

  /**
   * Menu selection listener
   */
  var listener: OnMenuItemSelectionListener? = null

  /**
   * Menu changed listener
   */
  var menuChangedListener: OnMenuChangedListener? = null

  /**
   * The user defined layout_gravity
   */
  private var gravity: Int = 0

  /**
   * View is attached
   */
  private var attached: Boolean = false

  val badgeProvider: BadgeProvider

  private val mLayoutChangedListener: LayoutChangeListener

  // endregion

  // region Init

  init {
    val typedArray =
        context.obtainStyledAttributes(attrs, R.styleable.BottomNavigation)
    typedArray?.let {
      val menuResId = typedArray.getResourceId(R.styleable.BottomNavigation_bbn_entries, 0)
      pendingMenu = MenuParser().inflateMenu(context, menuResId)
    }
    typedArray.recycle()

    badgeProvider = BadgeProvider(this)
    defaultSelectedIndex = 0
    backgroundColorAnimation =
        resources.getInteger(R.integer.bbn_background_animation_duration).toLong()
    defaultHeight = resources.getDimensionPixelSize(R.dimen.bbn_bottom_navigation_height)
    defaultWidth = resources.getDimensionPixelSize(R.dimen.bbn_bottom_navigation_width)
    shadowHeight = resources.getDimensionPixelOffset(R.dimen.bbn_top_shadow_height)

    // check if the bottom navigation is translucent
    if (!isInEditMode) {
      context.getActivity()?.let {
        val settings = Settings(it)
        bottomInset = if (it.hasTranslucentStatusBar()
            && settings.isNavigationAtBottom()
            && settings.hasNavigationBar) {
          settings.navigationBarHeight
        } else 0
        topInset = settings.statusBarHeight
      }
    }

    backgroundOverlay = View(getContext())
    backgroundOverlay.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    addView(backgroundOverlay)

    val drawable = ContextCompat.getDrawable(getContext(), R.drawable.bbn_ripple_selector)
    drawable?.mutate()
    drawable?.setDrawableColor(Color.WHITE)

    rippleOverlay = View(getContext())
    rippleOverlay.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    rippleOverlay.background = drawable
    rippleOverlay.isClickable = false
    rippleOverlay.isFocusable = false
    rippleOverlay.isFocusableInTouchMode = false
    addView(rippleOverlay)

    mLayoutChangedListener = LayoutChangeListener(this)
  }

  // endregion

  // region InstanceState

  override fun onSaveInstanceState(): Parcelable {
    val parcelable = super.onSaveInstanceState()
    val savedState = SavedState(parcelable)

    if (null == menu) savedState.selectedIndex = 0
    else savedState.selectedIndex = getSelectedIndex()

    badgeProvider.let { savedState.badgeBundle = it.save() }

    return savedState
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    (state as? SavedState)?.let {
      super.onRestoreInstanceState(it.superState)
      defaultSelectedIndex = it.selectedIndex
      it.badgeBundle?.let { badgeProvider.restore(it) }
    }
  }

  internal class SavedState : View.BaseSavedState {
    var selectedIndex: Int = 0
    var badgeBundle: Bundle? = null

    constructor(inner: Parcel) : super(inner) {
      selectedIndex = inner.readInt()
      badgeBundle = inner.readBundle(javaClass.classLoader)
    }

    constructor(superState: Parcelable) : super(superState) {}

    override fun writeToParcel(out: Parcel, flags: Int) {
      super.writeToParcel(out, flags)
      out.writeInt(selectedIndex)
      out.writeBundle(badgeBundle)
    }

    companion object {
      val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
        override fun createFromParcel(inner: Parcel): SavedState {
          return SavedState(inner)
        }

        override fun newArray(size: Int): Array<SavedState?> {
          return arrayOfNulls(size)
        }
      }
    }
  }

  // endregion

  // region heritage

  override fun onItemPressed(parent: ItemsLayoutContainer, view: View, pressed: Boolean) {
    if (Build.VERSION.SDK_INT < 21) return

    if (!pressed) {
      if (enabledRippleBackground) rippleOverlay.isPressed = false
      rippleOverlay.isHovered = false
      return
    }

    mLayoutChangedListener.forceLayout(view)
    rippleOverlay.isHovered = true
    if (enabledRippleBackground) rippleOverlay.isPressed = true
  }

  override fun onItemClick(parent: ItemsLayoutContainer, view: View, index: Int, animate: Boolean) {
    setSelectedItemInternal(parent, view, index, animate, true)
    mLayoutChangedListener.forceLayout(view)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    when {
      isGravityBottom(gravity) -> {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

        if (widthMode == View.MeasureSpec.AT_MOST) {
          throw IllegalArgumentException("layout_width must be equal to `match_parent`")
        }
        setMeasuredDimension(widthSize, defaultHeight + bottomInset + shadowHeight)
      }
      isGravityLeftOrRight(gravity) -> {
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        if (heightMode == View.MeasureSpec.AT_MOST) {
          throw IllegalArgumentException("layout_height must be equal to `match_parent`")
        }
        setMeasuredDimension(defaultWidth, heightSize)
      }
      else -> throw IllegalArgumentException(
          "invalid layout_gravity. Only one start, end, left, right or bottom is allowed")
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    (layoutParams as? ViewGroup.MarginLayoutParams)?.let {
      it.bottomMargin = -bottomInset
    }
  }

  override fun isAttachedToWindow(): Boolean {
    return if (Build.VERSION.SDK_INT >= 19) super.isAttachedToWindow() else attached
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    attached = true

    val params = layoutParams
    val layoutParams: CoordinatorLayout.LayoutParams? =
        if (params is CoordinatorLayout.LayoutParams) {
          this.gravity = GravityCompat.getAbsoluteGravity(params.gravity,
              ViewCompat.getLayoutDirection(this))
          params
        } else {
          this.gravity = Gravity.BOTTOM
          null
        }

    initializeUI(gravity)
    pendingMenu?.let {
      setItems(pendingMenu)
      pendingMenu = null
    }

    if (mBehavior == null) {
      layoutParams?.let {
        mBehavior = layoutParams.behavior
        if (isInEditMode) return
        mBehavior?.let {
          if (it is BottomBehavior) it.setLayoutValues(defaultHeight, bottomInset)
          else if (it is TabletBehavior) {
            val translucentStatus = context.getActivity()?.hasTranslucentStatusBar() ?: false
            it.setLayoutValues(defaultWidth, topInset, translucentStatus)
          }
        }
      }
    }
  }

  // endregion

  // region User API

  fun setSelectedIndex(position: Int, animate: Boolean) {
    itemsContainer.let {
      if (it == null) {
        defaultSelectedIndex = position
        return
      }
      it.asViewGroup().let { view ->
        setSelectedItemInternal(it, view.getChildAt(position), position, animate, false)
      }
    }
  }

  fun getSelectedIndex(): Int {
    return itemsContainer?.selectedIndex ?: -1
  }

  fun setExpanded(expanded: Boolean, animate: Boolean) {
    Log.i(tag, String.format("setExpanded(%b, %b)", expanded, animate))
    mPendingAction =
        (if (expanded) Const.PENDING_ACTION_EXPANDED else Const.PENDING_ACTION_COLLAPSED) or
        (if (animate) Const.PENDING_ACTION_ANIMATE_ENABLED else 0)
    requestLayout()
  }

  fun isExpanded(): Boolean {
    mBehavior.let {
      it ?: return false
      return (it as? BottomBehavior)?.isExpanded() ?: false
    }
  }

  /**
   * Inflate a menu resource into this navigation component
   *
   * @param menuResId the menu resource id
   */
  fun inflateMenu(@MenuRes menuResId: Int) {
    defaultSelectedIndex = 0
    pendingMenu = if (isAttachedToWindow) {
      setItems(MenuParser().inflateMenu(context, menuResId))
      null
    } else MenuParser().inflateMenu(context, menuResId)
  }

  /**
   * Returns the current menu items count
   *
   * @return number of items in the current menu
   */
  fun getMenuItemCount(): Int {
    return menu?.getItemsCount() ?: 0
  }

  /**
   * Returns the id of the item at the specified position
   *
   * @param position the position inside the menu
   * @return the item ID
   */
  @IdRes
  fun getMenuItemId(position: Int): Int {
    return menu?.getItemAtOrNull(position)?.id ?: 0
  }

  fun setMenuItemEnabled(index: Int, enabled: Boolean) {
    menu?.getItemAtOrNull(index)?.enabled = enabled
    itemsContainer?.setItemEnabled(index, enabled)
  }

  fun getMenuItemEnabled(index: Int): Boolean {
    return menu?.getItemAtOrNull(index)?.enabled ?: false
    // menu has not been parsed yet
  }

  fun getMenuItemTitle(index: Int): String? {
    return menu?.getItemAtOrNull(index)?.title
    // menu has not been parsed yet
  }

  fun getBehavior(): CoordinatorLayout.Behavior<*>? {
    if (mBehavior == null) {
      (layoutParams as? CoordinatorLayout.LayoutParams)?.let {
        return it.behavior
      }
    }
    return mBehavior
  }

  fun invalidateBadge(itemId: Int) {
    (itemsContainer?.findById(itemId) as? BottomNavigationItemView)?.let {
      it.invalidateBadge()
    }
  }

  // endregion

  // region Config

  internal fun resetPendingAction() {
    mPendingAction = Const.PENDING_ACTION_NONE
  }

  @SuppressLint("RtlHardcoded")
  private fun isGravityLeftOrRight(gravity: Int): Boolean {
    return gravity == Gravity.LEFT || gravity == Gravity.RIGHT
  }

  @SuppressLint("RtlHardcoded")
  private fun isGravityRight(gravity: Int): Boolean {
    return gravity == Gravity.RIGHT
  }

  @SuppressLint("RtlHardcoded")
  private fun isGravityBottom(gravity: Int): Boolean {
    return gravity == Gravity.BOTTOM
  }

  private fun isTablet(gravity: Int): Boolean {
    return isGravityLeftOrRight(gravity)
  }

  private fun setSelectedItemInternal(
    layoutContainer: ItemsLayoutContainer,
    view: View, index: Int,
    animate: Boolean,
    fromUser: Boolean
  ) {

    val item = menu?.getItemAtOrNull(index)
    if (layoutContainer.selectedIndex != index) {
      layoutContainer.setSelectedIndex(index, animate)
      item?.let {
        if (it.hasColor() && menu?.tablet == false) {
          if (animate) {
            this.animate(
                view,
                backgroundOverlay,
                backgroundDrawable,
                item.color,
                backgroundColorAnimation
            )
          } else {
            switchColor(
                backgroundOverlay,
                backgroundDrawable,
                item.color
            )
          }
        }
        listener?.onMenuItemSelect(it.id, index, fromUser)
      }
    } else {
      listener?.onMenuItemReselect(item?.id ?: -1, index, fromUser)
    }
  }

  private fun setItems(menu: MenuParser.Menu?) {
    this.menu = menu
    menu?.let {
      if (it.getItemsCount() < 3 || it.getItemsCount() > 5) {
        throw IllegalArgumentException(
            "BottomNavigation expects 3 to 5 items. ${it.getItemsCount()} found")
      }
      enabledRippleBackground = it.getItemAtOrNull(0)?.hasColor() == false || it.tablet
      it.tablet = isTablet(gravity)
      initializeBackgroundColor(menu)
      initializeContainer(menu)
      initializeItems(menu)

      menuChangedListener?.onMenuChanged(this)
    }
    requestLayout()
  }

  private fun initializeBackgroundColor(menu: MenuParser.Menu) {
    val color = menu.getBackground()
    backgroundDrawable?.color = color
  }

  private fun initializeContainer(menu: MenuParser.Menu) {
    itemsContainer?.let {
      // remove the layout listener
      it.asViewGroup().removeOnLayoutChangeListener(mLayoutChangedListener)

      if (menu.tablet && it !is TabletLayout) {
        removeView(it.asViewGroup())
        itemsContainer = null
      } else if (menu.shifting && it !is ShiftingLayout || !menu.shifting && it !is FixedLayout) {
        removeView(it.asViewGroup())
        itemsContainer = null
      } else {
        it.removeAll()
      }
    }

    if (itemsContainer == null) {
      val params = LinearLayout.LayoutParams(
          if (menu.tablet) defaultWidth else MATCH_PARENT,
          if (menu.tablet) MATCH_PARENT else defaultHeight
      )

      itemsContainer = when {
        menu.tablet -> TabletLayout(context)
        menu.shifting -> ShiftingLayout(context)
        else -> FixedLayout(context)
      }

      // force the layout manager ID
      itemsContainer?.let {
        it.asViewGroup().id = R.id.bbn_layoutManager
        it.setLayoutParams(params)
        addView(it.asViewGroup())
      }
    }

    // add the layout listener
    itemsContainer?.asViewGroup()?.addOnLayoutChangeListener(mLayoutChangedListener)
  }

  private fun initializeItems(menu: MenuParser.Menu) {
    itemsContainer?.setSelectedIndex(defaultSelectedIndex, false)
    itemsContainer?.populate(menu)
    itemsContainer?.listener = this

    menu.getItemAtOrNull(defaultSelectedIndex)?.let {
      if (defaultSelectedIndex > -1 && it.hasColor()) {
        backgroundDrawable?.color = it.color
      }
    }

    rippleOverlay.background?.setDrawableColor(menu.getRippleColor())
  }

  private fun initializeUI(gravity: Int) {
    val tablet = isTablet(gravity)
    val elevation = resources.getDimensionPixelSize(
        if (!tablet) R.dimen.bbn_elevation else R.dimen.bbn_elevation_tablet)
    val bgResId =
        if (!tablet) R.drawable.bbn_background
        else
          if (isGravityRight(gravity)) R.drawable.bbn_background_tablet_right
          else R.drawable.bbn_background_tablet_left
    val paddingBottom = if (!tablet) shadowHeight else 0

    // View elevation
    ViewCompat.setElevation(this, elevation.toFloat())

    // Main background
    val layerDrawable = ContextCompat.getDrawable(context, bgResId) as? LayerDrawable
    layerDrawable?.mutate()
    backgroundDrawable = layerDrawable?.findDrawableByLayerId(R.id.bbn_background) as? ColorDrawable
    background = layerDrawable

    // Padding bottom
    setPadding(0, paddingBottom, 0, 0)
  }

  // endregion

}

