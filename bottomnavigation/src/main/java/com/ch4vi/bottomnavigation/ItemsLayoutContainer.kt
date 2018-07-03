package com.ch4vi.bottomnavigation

import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import com.ch4vi.bottomnavigation.menu.MenuParser

interface ItemsLayoutContainer {
  fun setSelectedIndex(index: Int, animate: Boolean)

  fun getSelectedIndex(): Int

  fun populate(menu: MenuParser.Menu)

  fun setLayoutParams(params: ViewGroup.LayoutParams)

  fun setOnItemClickListener(listener: OnItemClickListener)

  fun findById(@IdRes id: Int): View

  fun removeAll()

  fun requestLayout()

  fun setItemEnabled(index: Int, enabled: Boolean)
}

interface OnItemClickListener {
  fun onItemClick(parent: ItemsLayoutContainer, view: View, index: Int, animate: Boolean)

  fun onItemPressed(parent: ItemsLayoutContainer, view: View, pressed: Boolean)
}