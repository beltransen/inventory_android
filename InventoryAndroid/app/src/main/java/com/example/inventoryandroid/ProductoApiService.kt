package com.example.inventoryandroid

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProductoApiService {

    @GET("productos/")
    suspend fun getAll(): List<ProductoDTO>

    @GET("productos/{id}")
    suspend fun getById(@Path("id") id: Int): ProductoDTO

    @GET("productos/codigo/{codigo}")
    suspend fun getByCodigoBarras(@Path("codigo") codigo: String): ProductoDTO?

    @POST("productos")
    suspend fun insert(@Body producto: ProductoDTO)

    @PUT("productos/{id}")
    suspend fun update(@Path("id") id: Long, @Body producto: ProductoDTO): Producto

    @DELETE("productos/{id}")
    suspend fun delete(@Path("id") id: Long)

    @Multipart
    @POST("upload/{filename}")
    suspend fun uploadImage(
        @Path("filename") filename: String,
        @Part image: MultipartBody.Part
    ): Response<Map<String, String>>
}