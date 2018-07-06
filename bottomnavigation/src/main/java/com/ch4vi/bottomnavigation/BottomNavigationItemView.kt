package com.ch4vi.bottomnavigation

import android.animation.ArgbEvaluator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import com.ch4vi.bottomnavigation.badge.BadgeDrawable
import com.ch4vi.bottomnavigation.badge.BadgeProvider
import com.ch4vi.bottomnavigation.menu.BottomNavigationItem
import com.ch4vi.bottomnavigation.menu.MenuParser
import java.lang.ref.SoftReference

abstract class BottomNavigationItemView(
  parent: BottomNavigation,
  expanded: Boolean,
  menu: MenuParser.Menu
) : View(parent.context) {

  internal object Const {
    const val ALPHA_MAX = 255f
  }

  private val rippleColor: Int = menu.getRippleColor()
  private val provider: BadgeProvider
  private var badge: Drawable? = null
  internal var textDirty: Boolean = false
  internal val evaluator: ArgbEvaluator = ArgbEvaluator()
  internal val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
  internal var icon: Drawable? = null

  var isExpanded: Boolean
    private set
  var item: BottomNavigationItem? = null
    set(value) {
      field = value
      value?.let {
        this.id = value.id
        this.isEnabled = value.enabled
      }
      invalidateBadge()
    }

  init {
    this.textDirty = true
    this.provider = parent.badgeProvider
    this.isExpanded = expanded
  }

  protected abstract fun onStatusChanged(expanded: Boolean, size: Int, animate: Boolean)

  override fun invalidateDrawable(drawable: Drawable) {
    super.invalidateDrawable(drawable)
    if (drawable === badge) invalidate()
  }

  fun invalidateBadge() {
    val drawable = provider.getBadge(id)
    if (badge !== drawable) {
      badge?.callback = null
      badge = null
      badge = drawable
      badge?.callback = this
      (badge as? BadgeDrawable)?.animating = false
      parent?.let { invalidate() }
    }
  }

  fun setExpanded(expanded: Boolean, newSize: Int, animate: Boolean) {
    if (this.isExpanded != expanded) {
      this.isExpanded = expanded
      onStatusChanged(expanded, newSize, animate)
    }
  }

  fun setTypeface(typeface: SoftReference<Typeface>?) {
    typeface ?: return
    textPaint.typeface = typeface.get() ?: Typeface.DEFAULT

    textDirty = true
    requestLayout()
  }

  internal fun drawBadge(canvas: Canvas) {
    val bounds = icon?.bounds ?: return
    badge?.let {
      it.setBounds(bounds.right - it.intrinsicWidth, bounds.top, bounds.right,
          bounds.top + it.intrinsicHeight)
      it.draw(canvas)
    }
  }

}