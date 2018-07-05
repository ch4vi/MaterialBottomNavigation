package com.ch4vi.bottomnavigation.menu

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Xml
import com.ch4vi.bottomnavigation.R
import com.ch4vi.bottomnavigation.getThemeColor
import org.xmlpull.v1.XmlPullParser
import java.util.ArrayList

class MenuParser {

  data class Menu(
    private val context: Context,
    private val background: Int,
    private var rippleColor: Int,
    private var colorActive: Int,
    private var colorInactive: Int,
    private var colorDisabled: Int,
    val badgeColor: Int,
    var tablet: Boolean = false,
    var forceFixed: Boolean,
    var itemAnimationDuration: Int
  ) {
    var shifting: Boolean = false
      private set

    var items: Array<BottomNavigationItem> = emptyArray()
      set(value) {
        field = value
        this.shifting = items.size > 3 && !forceFixed
      }

    // region Getters

    fun getBackground(): Int {
      return if (background == 0) {
        if (shifting && !tablet) context.getThemeColor(R.attr.colorPrimary)
        else context.getThemeColor(android.R.attr.windowBackground)
      } else background
    }

    fun getColorActive(): Int {
      if (colorActive == 0) {
        colorActive =
            if (shifting && !tablet) context.getThemeColor(android.R.attr.colorForegroundInverse)
            else context.getThemeColor(android.R.attr.colorForeground)
      }
      return colorActive
    }

    fun getColorInactive(): Int {
      if (colorInactive == 0) {
        colorInactive = getColorActive().apply {
          Color.argb(Color.alpha(this) / 2, Color.red(this), Color.green(this),
              Color.blue(this))
        }
      }
      return colorInactive
    }

    fun getColorDisabled(): Int {
      if (colorDisabled == 0) {
        colorDisabled = getColorActive().apply {
          Color.argb(Color.alpha(this) / 2, Color.red(this), Color.green(this),
              Color.blue(this))
        }
      }
      return colorDisabled
    }

    fun getRippleColor(): Int {
      if (rippleColor == 0) {
        rippleColor =
            if (shifting && !tablet) ContextCompat.getColor(context,
                R.color.bbn_shifting_item_ripple_color)
            else ContextCompat.getColor(context, R.color.bbn_fixed_item_ripple_color)
      }
      return rippleColor
    }

    fun getItemAtOrNull(index: Int): BottomNavigationItem? {
      return items.getOrNull(index)
    }

    fun getItemsCount(): Int {
      return items.size
    }

    /**
     * Returns true if the first item of the menu
     * has a color defined
     */
    fun hasChangingColor(): Boolean {
      return items[0].hasColor()
    }

    // endregions

    override fun toString(): String {
      return String.format(
          "Menu{background:%x, colorActive:%x, colorInactive:%x, colorDisabled: %s, shifting:%b, tablet:%b}",
          background, colorActive, colorInactive, colorDisabled, shifting, tablet
      )
    }

  }

  class MenuItem(
    val id: Int,
    val title: CharSequence,
    val iconResId: Int,
    val enabled: Boolean = true,
    val color: Int
  )

  private object Tag {
    const val NAME = "name"
    const val ITEM = "item"
    const val MENU = "menu"
  }

  private var menu: Menu? = null
  private var item: MenuItem? = null

  internal fun inflateMenu(context: Context, menuRes: Int): Menu? {
    val list = ArrayList<BottomNavigationItem>()
    val menuParser = MenuParser()

    try {
      val parser = context.resources.getLayout(menuRes)
      val attrs = Xml.asAttributeSet(parser)

      var tagName: String
      var eventType = parser.eventType
      var lookingForEndOfUnknownTag = false
      var unknownTagName: String? = null

      do {
        if (eventType == XmlPullParser.START_TAG) {
          tagName = parser.name
          if (tagName == Tag.NAME) {
            menuParser.readMenu(context, attrs)
            eventType = parser.next()
            break
          }
          throw RuntimeException("Expecting menu, got $tagName")
        }
        eventType = parser.next()
      } while (eventType != XmlPullParser.END_DOCUMENT)

      var reachedEndOfMenu = false

      while (!reachedEndOfMenu) {
        when (eventType) {
          XmlPullParser.START_TAG -> {
            if (!lookingForEndOfUnknownTag) {
              tagName = parser.name
              if (tagName == Tag.ITEM) {
                menuParser.readItem(context, attrs)
              } else {
                lookingForEndOfUnknownTag = true
                unknownTagName = tagName
              }
            }
          }

          XmlPullParser.END_TAG -> {
            tagName = parser.name
            if (lookingForEndOfUnknownTag && tagName == unknownTagName) {
              lookingForEndOfUnknownTag = false
              unknownTagName = null
            } else if (tagName == Tag.ITEM) {
              item?.let {
                item = null
                val tab = BottomNavigationItem(it.id, it.iconResId, "${it.title}")
                tab.enabled = it.enabled
                tab.color = it.color
                list.add(tab)
              }
            } else if (tagName == Tag.MENU) {
              reachedEndOfMenu = true
            }
          }

          XmlPullParser.END_DOCUMENT -> throw RuntimeException("Unexpected end of document")

          else -> {
          }
        }
        eventType = parser.next()
      }
    } catch (e: Exception) {
      return null
    }

    menu?.let {
      menu = null
      it.items = list.toTypedArray()
      return it
    }
    return null
  }

  private fun readMenu(context: Context, attrs: AttributeSet) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationMenu)
    typedArray?.let {
      menu = Menu(
          context = context,
          background = typedArray.getColor(R.styleable.BottomNavigationMenu_android_background, 0),
          rippleColor = typedArray.getColor(R.styleable.BottomNavigationMenu_bbn_rippleColor, 0),
          colorActive =
          typedArray.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorActive, 0),
          colorInactive =
          typedArray.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorInactive, 0),
          colorDisabled =
          typedArray.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorDisabled, 0),
          badgeColor =
          typedArray.getColor(R.styleable.BottomNavigationMenu_bbn_badgeColor, Color.RED),
          forceFixed =
          typedArray.getBoolean(R.styleable.BottomNavigationMenu_bbn_alwaysShowLabels, false),
          itemAnimationDuration =
          typedArray.getInt(R.styleable.BottomNavigationMenu_bbn_itemAnimationDuration,
              context.resources.getInteger(R.integer.bbn_item_animation_duration))
      )
    }
    typedArray?.recycle()
  }

  /**
   * Called when the parser is pointing to an item tag.
   */
  private fun readItem(context: Context, attrs: AttributeSet) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationMenuItem)
    typedArray?.let {
      item = MenuItem(
          id = typedArray.getResourceId(R.styleable.BottomNavigationMenuItem_android_id, 0),
          title = typedArray.getText(R.styleable.BottomNavigationMenuItem_android_title),
          iconResId =
          typedArray.getResourceId(R.styleable.BottomNavigationMenuItem_android_icon, 0),
          enabled =
          typedArray.getBoolean(R.styleable.BottomNavigationMenuItem_android_enabled, true),
          color =
          typedArray.getColor(R.styleable.BottomNavigationMenuItem_android_color, 0)
      )
    }
    typedArray.recycle()
  }
}