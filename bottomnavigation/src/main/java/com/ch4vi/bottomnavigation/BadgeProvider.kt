package com.econocom.bottomnavigationbar.custom

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.IdRes
import com.econocom.bottomnavigationbar.R
import java.util.HashSet

class BadgeProvider(private val navigation: BottomNavigation) {
  private object Const {
    const val KEY_MAP = "Map"
  }

  private val badgeSize = navigation.context.resources.getDimensionPixelSize(R.dimen.bbn_badge_size)
  private val map = HashSet<Int>()

  private fun save(): Bundle {
    val bundle = Bundle()
    bundle.putSerializable(Const.KEY_MAP, map)
    return bundle
  }

  @Suppress("UNCHECKED_CAST")
  fun restore(bundle: Bundle) {
    val set = bundle.getSerializable(Const.KEY_MAP) as? HashSet<Int>
    set?.let { map.addAll(it) }
  }

  /**
   * Returns if the menu item will require a badge
   *
   * @param itemId the menu item id
   * @return true if the menu item has to draw a badge
   */
  fun hasBadge(@IdRes itemId: Int): Boolean {
    return map.contains(itemId)
  }

  internal fun getBadge(@IdRes itemId: Int): Drawable? {
    return if (map.contains(itemId)) {
      newDrawable(navigation.menu.getBadgeColor())
    } else null
  }

  private fun newDrawable(preferredColor: Int): Drawable {
    return BadgeDrawable(preferredColor, badgeSize)
  }

  /**
   * Request to display a new badge over the passed menu item id
   *
   * @param itemId the menu item id
   */
  fun show(@IdRes itemId: Int) {
    map.add(itemId)
    navigation.invalidateBadge(itemId)
  }

  /**
   * Remove the currently displayed badge
   *
   * @param itemId the menu item id
   */
  fun remove(@IdRes itemId: Int) {
    if (map.remove(itemId)) {
      navigation.invalidateBadge(itemId)
    }
  }


}