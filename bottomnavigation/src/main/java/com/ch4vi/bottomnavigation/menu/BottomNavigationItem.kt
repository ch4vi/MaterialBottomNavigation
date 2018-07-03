package com.ch4vi.bottomnavigation.menu

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources

data class BottomNavigationItem(
  val id: Int,
  private val iconRes: Int,
  private val title: String
) {
  var color: Int = 0
  var enabled: Boolean = true

  fun getIcon(context: Context): Drawable? {
    return AppCompatResources.getDrawable(context, this.iconRes)
  }

  fun hasColor(): Boolean {
    return color != 0
  }

  override fun toString(): String {
    return ("BottomNavigationItem{"
        + "id=$id"
        + ", iconResource=" + String.format("%x", iconRes)
        + ", title='$title'"
        + ", color=" + String.format("%x", color)
        + ", enabled=$enabled"
        + '}')
  }
}
