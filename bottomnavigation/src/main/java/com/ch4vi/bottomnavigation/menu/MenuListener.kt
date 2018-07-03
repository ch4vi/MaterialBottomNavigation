package com.ch4vi.bottomnavigation.menu

import android.support.annotation.IdRes
import com.ch4vi.bottomnavigation.BottomNavigation

interface OnMenuItemSelectionListener {
  fun onMenuItemSelect(@IdRes itemId: Int, position: Int, fromUser: Boolean)

  fun onMenuItemReselect(@IdRes itemId: Int, position: Int, fromUser: Boolean)
}

interface OnMenuChangedListener {
  fun onMenuChanged(parent: BottomNavigation)
}