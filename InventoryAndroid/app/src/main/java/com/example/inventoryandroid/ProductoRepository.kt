package com.example.inventoryandroid

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map

class ProductoRepository(private val DAO: ProductoDAO, private val API: ProductoApiService)  {
    fun getAllProductos(): LiveData<List<Producto>> = DAO.getAll().map { listaProductos ->
        listaProductos.map { it.toDomain() }
    }


    suspend fun addProducto(productoEntity: ProductoEntity) {
        DAO.insert(productoEntity)
        try {
            API.insert(productoEntity.toDomain().toDTO())
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error insertando en API", e)
        }
    }

    suspend fun delProducto(productoEntity: ProductoEntity) {
        DAO.delete(productoEntity)
        productoEntity.productoId?.let {
            try {
                Log.d("ProductoRepository", "Intentando borrar producto remoto con ID: $it")
                API.delete(it)
            } catch (e: Exception) {
                Log.e("ProductoRepository", "Error borrando en API", e)
            }
        }
    }

    suspend fun updateProducto(producto: Producto) {
        DAO.update(producto.toEntity())
        producto.productoId?.let {
            try {
                API.update(it, producto.toDTO())
            } catch (e: Exception) {
                Log.e("ProductoRepository", "Error actualizando en API", e)
            }
        }
    }

    fun getProductoPorCodigoBarras(codigo: String): Producto? {
        val productoEntity = DAO.getByCodigoBarras(codigo)
        return productoEntity?.toDomain()
    }

    // Sincroniza todos los productos del servidor con Room (útil al iniciar la app)
    suspend fun syncFromServer() {
        val productosServidor = API.getAll()
        productosServidor.forEach { producto ->
            DAO.insert(producto.toDomain().toEntity()) // Reemplaza si existe
        }
    }

}
