package com.example.inventoryandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.inventoryandroid.databinding.ActivityAcercaDeBinding


class AcercaDeActivity : AppCompatActivity() {
    lateinit var binding: ActivityAcercaDeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acerca_de)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_acerca_de)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}