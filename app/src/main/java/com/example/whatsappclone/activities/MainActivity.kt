package com.example.whatsappclone.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import com.example.whatsappclone.R
import com.example.whatsappclone.adapters.ScreenSliderAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        viewPager.adapter = ScreenSliderAdapter(this)
        TabLayoutMediator(tabs, viewPager, TabLayoutMediator.TabConfigurationStrategy { tab, pos ->
            when(pos) {
                0 -> tab.text = "CHATS"
                1 -> tab.text = "PEOPLE"
            }
        }).attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }
}