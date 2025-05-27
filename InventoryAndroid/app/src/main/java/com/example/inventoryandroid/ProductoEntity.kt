package com.example.inventoryandroid

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "producto_table")
data class ProductoEntity (
    @PrimaryKey
    val productoId: Long,  // Ahora es la clave primaria
    @ColumnInfo(name = "producto_nombre")
    val nombre: String,
    @ColumnInfo(name = "producto_apellidos")
    val foto: String,
    @ColumnInfo(name = "producto_telefono")
    var categoria: Int,
    @ColumnInfo(name = "producto_email")
    var precio: Float,
    @ColumnInfo(name = "producto_organizacion")
    var cantidadAñadida: Int,
    @ColumnInfo(name = "producto_ultima_actualizacion")
    var ultimaActualizacion: Long
)