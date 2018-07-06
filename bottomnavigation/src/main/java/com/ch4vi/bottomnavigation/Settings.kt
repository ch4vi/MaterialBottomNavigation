package com.ch4vi.bottomnavigation

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.ViewConfiguration
import com.ch4vi.bottomnavigation.Settings.Const.ANDROID
import com.ch4vi.bottomnavigation.Settings.Const.BOOL
import com.ch4vi.bottomnavigation.Settings.Const.CONFIG_NAVIGATION
import com.ch4vi.bottomnavigation.Settings.Const.DIMEN
import com.ch4vi.bottomnavigation.Settings.Const.FALSE
import com.ch4vi.bottomnavigation.Settings.Const.KEY_NAVIGATION_HEIGHT
import com.ch4vi.bottomnavigation.Settings.Const.KEY_NAVIGATION_HEIGHT_LANDSCAPE
import com.ch4vi.bottomnavigation.Settings.Const.TRUE

class Settings(activity: Activity) {
  private object Const {
    const val SYSTEM_PATH = "android.os.SystemProperties"
    const val METHOD_NAME = "get"
    const val METHOD_INVOKE = "qemu.hw.mainkeys"

    const val KEY_STATUS_HEIGHT = "status_bar_height"
    const val KEY_NAVIGATION_HEIGHT = "navigation_bar_height"
    const val KEY_NAVIGATION_HEIGHT_LANDSCAPE = "navigation_bar_height_landscape"

    const val CONFIG_NAVIGATION = "config_showNavigationBar"
    const val BOOL = "bool"
    const val ANDROID = "android"
    const val DIMEN = "dimen"

    const val TRUE = "0"
    const val FALSE = "1"
  }

  private var flagIsNavBarOverride: String? = null
  private val smallestWidthDp: Float
  private val inPortrait: Boolean

  val navigationBarHeight: Int
  val hasNavigationBar: Boolean
  val statusBarHeight: Int

  init {
    this.smallestWidthDp = this.getSmallestWidthDp(activity)
    this.inPortrait = activity.resources.configuration.orientation == 1
    this.navigationBarHeight = this.getNavigationBarHeight(activity)
    this.hasNavigationBar = this.navigationBarHeight > 0
    this.statusBarHeight =
        this.getInternalDimensionSize(activity.resources, Const.KEY_STATUS_HEIGHT)

    try {
      val clazz = Class.forName(Const.SYSTEM_PATH)
      val method = clazz.getDeclaredMethod(Const.METHOD_NAME, String::class.java)
      method.isAccessible = true
      flagIsNavBarOverride = method.invoke(null as Any?, Const.METHOD_INVOKE) as String
    } catch (e: Throwable) {
      flagIsNavBarOverride = null
    }
  }

  fun isNavigationAtBottom(): Boolean {
    return this.smallestWidthDp >= 600.0f || this.inPortrait
  }

  private fun getSmallestWidthDp(activity: Activity): Float {
    val metrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getRealMetrics(metrics)

    val widthDp = metrics.widthPixels.toFloat() / metrics.density
    val heightDp = metrics.heightPixels.toFloat() / metrics.density
    return Math.min(widthDp, heightDp)
  }

  private fun getNavigationBarHeight(context: Context): Int {
    val res = context.resources
    val result = 0
    return if (this.hasNavBar(context)) {
      val key: String =
          if (this.inPortrait) KEY_NAVIGATION_HEIGHT
          else KEY_NAVIGATION_HEIGHT_LANDSCAPE

      this.getInternalDimensionSize(res, key)
    } else result
  }

  private fun hasNavBar(context: Context): Boolean {
    val res = context.resources
    val resourceId = res.getIdentifier(CONFIG_NAVIGATION, BOOL, ANDROID)
    return if (resourceId != 0) {
      var hasNav = res.getBoolean(resourceId)
      if (flagIsNavBarOverride == FALSE) hasNav = false
      else if (flagIsNavBarOverride == TRUE) hasNav = true
      hasNav
    } else !ViewConfiguration.get(context).hasPermanentMenuKey()
  }

  private fun getInternalDimensionSize(res: Resources, key: String): Int {
    val resourceId = res.getIdentifier(key, DIMEN, ANDROID)
    return if (resourceId > 0) res.getDimensionPixelSize(resourceId) else 0
  }
}