package com.example.inventoryandroid

data class ProductoDTO(
    val productoId: Int?,
    val nombre: String,
    val foto: String,
    val categoria: Int,
    val precio: Float,
    val codigoDeBarras: String,
    val cantidadAñadida: Int
)