package com.ch4vi.bottomnavigation.layout

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ch4vi.bottomnavigation.BottomNavigation
import com.ch4vi.bottomnavigation.BottomNavigationItemView
import com.ch4vi.bottomnavigation.BottomNavigationTabletItemView
import com.ch4vi.bottomnavigation.R
import com.ch4vi.bottomnavigation.menu.MenuParser
import org.jetbrains.anko.sdk19.listeners.onClick
import org.jetbrains.anko.sdk19.listeners.onLongClick
import org.jetbrains.anko.sdk19.listeners.onTouch
import org.jetbrains.anko.toast

class TabletLayout(context: Context) : ViewGroup(context), ItemsLayoutContainer {
  private val itemHeight = context.resources.getDimensionPixelSize(R.dimen.bbn_tablet_item_height)
  private val topPadding: Int =
      context.resources.getDimensionPixelSize(R.dimen.bbn_tablet_layout_padding_top)
  private var hasFrame: Boolean = false
  private var menu: MenuParser.Menu? = null

  override var selectedIndex: Int = 0
  override var listener: OnItemClickListener? = null

  override fun removeAll() {
    removeAllViews()
    selectedIndex = 0
    menu = null
  }

  override fun findById(id: Int): View {
    return this.findViewById(id)
  }

  override fun asViewGroup(): ViewGroup{
    return this
  }

  override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
    if (!hasFrame || childCount == 0) return

    var top = topPadding
    for (i in 0 until childCount) {
      val child = getChildAt(i)
      val params = child.layoutParams
      setChildFrame(child, 0, top, params.width, params.height)
      top += child.height
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    hasFrame = true
    menu?.let {
      menu = null
      populateInternal(it)
    }
  }

  override fun setSelectedIndex(index: Int, animate: Boolean) {
    if (selectedIndex == index) return

    val oldSelectedIndex = this.selectedIndex
    this.selectedIndex = index

    if (!hasFrame || childCount == 0) return

    val current = getChildAt(oldSelectedIndex) as? BottomNavigationTabletItemView
    val child = getChildAt(index) as? BottomNavigationTabletItemView

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
    (parent as? BottomNavigation)?.let {
      for (i in 0 until menu.getItemsCount()) {
        val item = menu.getItemAtOrNull(i)
        val params = LinearLayout.LayoutParams(width, itemHeight)

        val view = BottomNavigationTabletItemView(it, i == selectedIndex, menu)
        view.item = item
        view.layoutParams = params
        view.isClickable = true
        view.setTypeface(it.typeface)
        view.onTouch { v, event ->
          when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
              listener?.onItemPressed(this@TabletLayout, v, true)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
              listener?.onItemPressed(this@TabletLayout, v, false)
            }
          }
          false
        }

        view.onClick {
          listener?.onItemClick(this@TabletLayout, view, i, true)
        }
        view.onLongClick {
          context.toast("${item?.title}")
          true
        }
        addView(view)
      }
    }
  }
}
