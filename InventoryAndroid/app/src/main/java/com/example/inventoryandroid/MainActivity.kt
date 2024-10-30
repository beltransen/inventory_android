package com.example.inventoryandroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.example.inventoryandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)

        menu?.findItem(R.id.creatorInfo)?.setOnMenuItemClickListener {
            val intent = Intent(this, AcercaDeActivity::class.java)
            startActivity(intent)
            true
        }
        return true
    }
}