package com.ch4vi.bottomnavigation.layout

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ch4vi.bottomnavigation.BottomNavigation
import com.ch4vi.bottomnavigation.BottomNavigationItemView
import com.ch4vi.bottomnavigation.BottomNavigationShiftingItemView
import com.ch4vi.bottomnavigation.R
import com.ch4vi.bottomnavigation.menu.MenuParser
import org.jetbrains.anko.sdk19.listeners.onClick
import org.jetbrains.anko.sdk19.listeners.onLongClick
import org.jetbrains.anko.sdk19.listeners.onTouch
import org.jetbrains.anko.toast

class ShiftingLayout(context: Context) : ViewGroup(context), ItemsLayoutContainer {
  private object Const {
    const val ROUND_DECIMALS = 10.0
    const val RATIO_MIN_INCREASE = 0.05f

  }

  private val maxActiveItemWidth: Int
  private val minActiveItemWidth: Int
  private val maxInactiveItemWidth: Int
  private val minInactiveItemWidth: Int
  private var totalChildrenSize: Int = 0
  private var minSize: Int = 0
  private var maxSize: Int = 0
  private var hasFrame: Boolean = false
  private var menu: MenuParser.Menu? = null

  override var listener: OnItemClickListener? = null
  override var selectedIndex: Int = 0

  init {
    totalChildrenSize = 0
    maxActiveItemWidth = resources.getDimensionPixelSize(R.dimen.bbn_shifting_maxActiveItemWidth)
    minActiveItemWidth = resources.getDimensionPixelSize(R.dimen.bbn_shifting_minActiveItemWidth)
    maxInactiveItemWidth =
        resources.getDimensionPixelSize(R.dimen.bbn_shifting_maxInactiveItemWidth)
    minInactiveItemWidth =
        resources.getDimensionPixelSize(R.dimen.bbn_shifting_minInactiveItemWidth)
  }

  override fun removeAll() {
    removeAllViews()
    totalChildrenSize = 0
    selectedIndex = 0
    menu = null
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    if (!hasFrame || childCount == 0) return

    if (totalChildrenSize == 0) {
      totalChildrenSize =
          if (selectedIndex < 0) minSize * childCount
          else minSize * (childCount - 1) + maxSize
    }

    val width = r - l
    var left = (width - totalChildrenSize) / 2

    for (i in 0 until childCount) {
      val child = getChildAt(i)
      val params = child.layoutParams
      setChildFrame(child, left, 0, params.width, params.height)
      left += child.width
    }
  }

  override fun findById(id: Int): View {
    return this.findViewById(id)
  }

  override fun asViewGroup(): ViewGroup {
    return this
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    hasFrame = true
    menu?.let {
      populateInternal(it)
      menu = null
    }
  }

  override fun setSelectedIndex(index: Int, animate: Boolean) {
    if (selectedIndex == index) return

    val oldSelectedIndex = this.selectedIndex
    this.selectedIndex = index

    if (!hasFrame || childCount == 0) return

    val current = getChildAt(oldSelectedIndex) as? BottomNavigationItemView
    val child = getChildAt(index) as? BottomNavigationItemView

    val willAnimate = current != null && child != null

    if (!willAnimate) {
      totalChildrenSize = 0
      requestLayout()
    }
    current?.setExpanded(false, minSize, willAnimate)
    child?.setExpanded(true, maxSize, willAnimate)
  }

  override fun setItemEnabled(index: Int, enabled: Boolean) {
    (getChildAt(index) as? BottomNavigationItemView)?.let {
      it.isEnabled = enabled
      it.postInvalidate()
      requestLayout()
    }
  }

  override fun populate(menu: MenuParser.Menu) {
    if (hasFrame) populateInternal(menu)
    else this.menu = menu
  }

  private fun setChildFrame(child: View, left: Int, top: Int, width: Int, height: Int) {
    child.layout(left, top, left + width, top + height)
  }

  private fun setTotalSize(minSize: Int, maxSize: Int) {
    this.minSize = minSize
    this.maxSize = maxSize
  }

  private fun populateInternal(menu: MenuParser.Menu) {
    (parent as? BottomNavigation)?.let { parent ->
      val screenWidth = parent.width
      val totalWidth = maxInactiveItemWidth * (menu.getItemsCount() - 1) + maxActiveItemWidth
      var itemWidthMin: Int
      var itemWidthMax: Int

      if (totalWidth > screenWidth) {
        val wRatio = screenWidth / totalWidth
        val ratio = (Math.round(
            wRatio * Const.ROUND_DECIMALS) / Const.ROUND_DECIMALS) + Const.RATIO_MIN_INCREASE

        itemWidthMin =
            Math.max(maxInactiveItemWidth * ratio, minInactiveItemWidth.toDouble()).toInt()
        itemWidthMax = (maxActiveItemWidth * ratio).toInt()


        if (itemWidthMin * (menu.getItemsCount() - 1) + itemWidthMax > screenWidth) {
          itemWidthMax = screenWidth - itemWidthMin * (menu.getItemsCount() - 1)
          if (itemWidthMax == itemWidthMin) {
            itemWidthMin = minInactiveItemWidth
            itemWidthMax = screenWidth - itemWidthMin * (menu.getItemsCount() - 1)
          }
        }
      } else {
        itemWidthMax = maxActiveItemWidth
        itemWidthMin = maxInactiveItemWidth
      }

      setTotalSize(itemWidthMin, itemWidthMax)
      for (i in 0 until menu.getItemsCount()) {
        menu.getItemAtOrNull(i)?.let { item ->
          val params = LinearLayout.LayoutParams(itemWidthMin, height)
          if (i == selectedIndex) params.width = itemWidthMax
          val view = BottomNavigationShiftingItemView(parent, i == selectedIndex, menu)
          view.item = item
          view.layoutParams = params
          view.isClickable = true
          view.setTypeface(parent.typeface)
          view.onTouch { v, event ->
            val action = event.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
              listener?.onItemPressed(this@ShiftingLayout, v, true)
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
              listener?.onItemPressed(this@ShiftingLayout, v, false)
            }
            false
          }
          view.onClick {
            listener?.onItemClick(this@ShiftingLayout, view, i, true)
          }
          view.onLongClick {
            context.toast(item.title)
            true
          }
          addView(view)
        }
      }
    }
  }
}