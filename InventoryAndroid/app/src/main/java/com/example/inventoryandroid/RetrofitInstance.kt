package com.example.inventoryandroid

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://127.0.0.1" // ← Cambia esto si usas IP real

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ProductoApiService by lazy {
        retrofit.create(ProductoApiService::class.java)
    }
}
