package com.ch4vi.app.bottomnavigation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ch4vi.bottomnavigation.OnMenuItemClickListener
import kotlinx.android.synthetic.main.activity_main.bottom_navigation
import org.jetbrains.anko.design.snackbar

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (savedInstanceState == null) {
      val provider = bottom_navigation.badgeProvider
      provider.show(R.id.bbn_item1)
      provider.show(R.id.bbn_item2)
      provider.show(R.id.bbn_item3)
      provider.show(R.id.bbn_item4)
      bottom_navigation.menuChangedListener = {

      }

      bottom_navigation.getSelectedIndex()
      bottom_navigation.itemClickListener = object : OnMenuItemClickListener {
        override fun onMenuItemSelect(itemId: Int, position: Int, fromUser: Boolean) {
          snackbar(bottom_navigation, "id $itemId, position $position, from user $fromUser")
          provider.remove(itemId)

          when(itemId){
            R.id.bbn_item1 ->{
              provider.show(R.id.bbn_item2)
            }
            R.id.bbn_item2 ->{

            }
            R.id.bbn_item3 ->{

            }
            R.id.bbn_item4 ->{

            }
          }
        }

        override fun onMenuItemReselect(itemId: Int, position: Int, fromUser: Boolean) {
          snackbar(bottom_navigation, "REPEAT id $itemId, position $position, from user $fromUser")
        }
      }
    }
  }
}
