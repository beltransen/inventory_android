package com.example.inventoryandroid

import java.io.Serializable

data class Producto(
    val productoId: Int?,          // Si el ID puede ser null, es correcto que sea Int?.
    val nombre: String,            // Correcto, el nombre es un texto.
    val foto: String,              // Correcto si vas a almacenar una URL o un path como String.
    var categoria: Int,            // Correcto si categorizas por números (e.g., IDs de categorías).
    var precio: Float,             // Correcto para manejar precios, aunque Double también podría usarse para mayor precisión.
    var codigoDeBarras: String,    // Correcto, los códigos de barras suelen representarse como String.
    var cantidadAñadida: Int       //Con este INT representaremos la cantidad de producto que tenemos
) : Serializable
