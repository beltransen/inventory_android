package com.example.inventoryandroid

import retrofit2.Response
import retrofit2.http.*

interface ProductoApiService {

    @GET("productos/")
    suspend fun getAll(): List<Producto>

    @GET("productos/{id}")
    suspend fun getById(@Path("id") id: Int): Producto

    @GET("productos/codigo/{codigo}")
    suspend fun getByCodigoBarras(@Path("codigo") codigo: String): Producto?

    @POST("productos/")
    suspend fun insert(@Body producto: Producto): Producto

    @PUT("productos/{id}")
    suspend fun update(@Path("id") id: Int, @Body producto: Producto): Producto

    @DELETE("productos/{id}")
    suspend fun delete(@Path("id") id: Int)
}