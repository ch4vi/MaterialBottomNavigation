package com.ch4vi.bottomnavigation

import android.animation.Animator
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.support.annotation.AttrRes
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils
import android.view.WindowManager.LayoutParams
import android.view.animation.DecelerateInterpolator

/**
 * Returns the current theme defined color
 */
internal fun Context.getThemeColor(@AttrRes color: Int): Int {
  return TypedValue().apply {
    theme.resolveAttribute(color, this, true)
  }.data
}

internal fun Context?.getActivity(): Activity? {
  return when (this) {
    is Activity -> this
    is ContextWrapper -> this.baseContext.getActivity()
    else -> null
  }
}

/**
 * Returns if the current theme has the translucent status bar enabled
 *
 * @return true if the current theme has the translucent statusBar
 */
@TargetApi(19)
internal fun Activity.hasTranslucentStatusBar(): Boolean {
  return if (Build.VERSION.SDK_INT >= 19) {
    this.window.attributes.flags and LayoutParams.FLAG_TRANSLUCENT_STATUS == LayoutParams.FLAG_TRANSLUCENT_STATUS
  } else false
}

@TargetApi(21)
internal fun Drawable.setDrawableColor(color: Int) {
  if (Build.VERSION.SDK_INT >= 21) {
    if (this is RippleDrawable) {
      this.setColor(ColorStateList.valueOf(color))
    }
  } else DrawableCompat.setTint(this, color)
}

internal fun BottomNavigation.animate(
  v: View,
  backgroundOverlay: View,
  backgroundDrawable: ColorDrawable?,
  newColor: Int,
  duration: Long
) {

  val centerX = (v.x + v.width / 2).toInt()
  val centerY = this.paddingTop + v.height / 2
  backgroundOverlay.clearAnimation()
  val animator: Any

  if (Build.VERSION.SDK_INT >= 21) {

    val currentAnimator = backgroundOverlay.getTag(R.id.bbn_backgroundOverlay_animator) as? Animator
    currentAnimator?.cancel()

    val startRadius = 10f
    val finalRadius = (if (centerX > this.width / 2) centerX else this.width - centerX).toFloat()
    animator =
        ViewAnimationUtils.createCircularReveal(
            backgroundOverlay, centerX, centerY, startRadius, finalRadius
        )
    backgroundOverlay.setTag(R.id.bbn_backgroundOverlay_animator, animator)
  } else {
    backgroundOverlay.alpha = 0f
    animator = ViewCompat.animate(backgroundOverlay).alpha(1f)
  }

  backgroundOverlay.setBackgroundColor(newColor)
  backgroundOverlay.visibility = View.VISIBLE

  if (animator is ViewPropertyAnimatorCompat) {
    animator.setListener(object : ViewPropertyAnimatorListener {
      var cancelled: Boolean = false

      override fun onAnimationStart(view: View) {}

      override fun onAnimationEnd(view: View) {
        if (!cancelled) {
          backgroundDrawable?.color = newColor
          backgroundOverlay.visibility = View.INVISIBLE
          backgroundOverlay.alpha = 1f
        }
      }

      override fun onAnimationCancel(view: View) {
        cancelled = true
      }
    }).setDuration(duration).start()
  } else {
    (animator as? Animator)?.let {
      it.duration = duration
      it.interpolator = DecelerateInterpolator()
      it.addListener(object : Animator.AnimatorListener {
        var cancelled: Boolean = false

        override fun onAnimationStart(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {
          if (!cancelled) {
            backgroundDrawable?.color = newColor
            backgroundOverlay.visibility = View.INVISIBLE
            backgroundOverlay.alpha = 1f
          }
        }

        override fun onAnimationCancel(animation: Animator) {
          cancelled = true
        }

        override fun onAnimationRepeat(animation: Animator) {}
      })
      it.start()
    }
  }
}

internal fun switchColor(
  backgroundOverlay: View,
  backgroundDrawable: ColorDrawable?,
  newColor: Int
) {

  backgroundOverlay.clearAnimation()

  if (Build.VERSION.SDK_INT >= 21) {
    val currentAnimator = backgroundOverlay.getTag(R.id.bbn_backgroundOverlay_animator) as? Animator
    currentAnimator?.cancel()
  }

  backgroundDrawable?.color = newColor
  backgroundOverlay.visibility = View.INVISIBLE
  backgroundOverlay.alpha = 1f
}