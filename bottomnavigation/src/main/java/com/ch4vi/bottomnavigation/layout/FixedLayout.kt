package com.ch4vi.bottomnavigation.layout

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ch4vi.bottomnavigation.BottomNavigation
import com.ch4vi.bottomnavigation.BottomNavigationFixedItemView
import com.ch4vi.bottomnavigation.BottomNavigationItemView
import com.ch4vi.bottomnavigation.R
import com.ch4vi.bottomnavigation.menu.MenuParser
import org.jetbrains.anko.sdk19.listeners.onClick
import org.jetbrains.anko.sdk19.listeners.onLongClick
import org.jetbrains.anko.sdk19.listeners.onTouch
import org.jetbrains.anko.toast

class FixedLayout(context: Context) : ViewGroup(context), ItemsLayoutContainer {
  private val maxActiveItemWidth =
      resources.getDimensionPixelSize(R.dimen.bbn_fixed_maxActiveItemWidth)
  private val minActiveItemWidth =
      resources.getDimensionPixelSize(R.dimen.bbn_fixed_minActiveItemWidth)
  private var totalChildrenSize = 0
  private var hasFrame = false
  private var itemFinalWidth = 0
  private var menu: MenuParser.Menu? = null

  override var selectedIndex = 0
  override var listener: OnItemClickListener? = null

  override fun removeAll() {
    removeAllViews()
    totalChildrenSize = 0
    itemFinalWidth = 0
    selectedIndex = 0
    menu = null
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    if (!hasFrame || childCount == 0) return

    if (totalChildrenSize == 0) {
      totalChildrenSize = itemFinalWidth * (childCount - 1) + itemFinalWidth
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

    val current = getChildAt(oldSelectedIndex) as? BottomNavigationFixedItemView
    val child = getChildAt(index) as? BottomNavigationFixedItemView

    current?.setExpanded(false, 0, animate)
    child?.setExpanded(true, 0, animate)
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

  private fun populateInternal(menu: MenuParser.Menu) {
    (parent as? BottomNavigation)?.let { parent ->
      val screenWidth = parent.width

      var proposedWidth = Math.min(Math.max(screenWidth / menu.getItemsCount(), minActiveItemWidth),
          maxActiveItemWidth)

      if (proposedWidth * menu.getItemsCount() > screenWidth) {
        proposedWidth = screenWidth / menu.getItemsCount()
      }

      this.itemFinalWidth = proposedWidth

      for (i in 0 until menu.getItemsCount()) {
        menu.getItemAtOrNull(i)?.let { item ->
          val params = LinearLayout.LayoutParams(proposedWidth, height)

          val view = BottomNavigationFixedItemView(parent, i == selectedIndex, menu)
          view.item = item
          view.layoutParams = params
          view.isClickable = true
          view.setTypeface(parent.typeface)
          view.onTouch { v, event ->
            val action = event.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
              listener?.onItemPressed(this@FixedLayout, v, true)
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
              listener?.onItemPressed(this@FixedLayout, v, false)
            }
            false
          }
          view.onClick {
            listener?.onItemClick(this@FixedLayout, view, i, true)
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