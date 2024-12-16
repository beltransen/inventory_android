package com.example.inventoryandroid

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "producto_table")
data class ProductoEntity (
    @PrimaryKey(autoGenerate = true)
    val productoId: Int? = null,
    @ColumnInfo(name = "producto_nombre")
    val nombre: String,
    @ColumnInfo(name = "producto_apellidos")
    val foto: String,
    @ColumnInfo(name = "producto_telefono")
    var categoria: Int,
    @ColumnInfo(name = "producto_email")
    var precio: Float,
    @ColumnInfo(name = "producto_empresa")
    var codigoDeBarras: String,
    @ColumnInfo(name = "producto_organizacion")
    var cantidadAñadida:Int
)