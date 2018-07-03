package com.econocom.bottomnavigationbar.custom.menu

import android.support.annotation.IdRes
import com.econocom.bottomnavigationbar.custom.BottomNavigation

interface OnMenuItemSelectionListener {
  fun onMenuItemSelect(@IdRes itemId: Int, position: Int, fromUser: Boolean)

  fun onMenuItemReselect(@IdRes itemId: Int, position: Int, fromUser: Boolean)
}

interface OnMenuChangedListener {
  fun onMenuChanged(parent: BottomNavigation)
}