package com.ch4vi.bottomnavigation

import android.content.Context
import android.support.annotation.AttrRes
import android.util.TypedValue

/**
 * Returns the current theme defined color
 */
fun Context.getThemeColor(@AttrRes color: Int): Int {
  return TypedValue().apply {
    theme.resolveAttribute(color, this, true)
  }.data
}