package com.example.inventoryandroid

data class ProductoDTO(
    val productoId: Long,
    val nombre: String,
    val foto: String,
    val categoria: Int,
    val precio: Float,
    val cantidadAñadida: Int,
    val ultimaActualizacion: Long
)