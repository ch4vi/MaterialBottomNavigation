package com.ch4vi.bottomnavigation.layout

import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import com.ch4vi.bottomnavigation.menu.MenuParser

interface ItemsLayoutContainer {
  var selectedIndex: Int
  var listener: OnItemClickListener?

  fun setSelectedIndex(index: Int, animate: Boolean)

  fun populate(menu: MenuParser.Menu)

  fun setLayoutParams(params: ViewGroup.LayoutParams)

  fun findById(@IdRes id: Int): View

  fun removeAll()

  fun requestLayout()

  fun setItemEnabled(index: Int, enabled: Boolean)

  fun asViewGroup(): ViewGroup
}

interface OnItemClickListener {
  fun onItemClick(parent: ItemsLayoutContainer, view: View, index: Int, animate: Boolean)

  fun onItemPressed(parent: ItemsLayoutContainer, view: View, pressed: Boolean)
}