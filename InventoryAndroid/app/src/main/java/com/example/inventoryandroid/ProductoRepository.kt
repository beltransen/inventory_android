package com.example.inventoryandroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.map

class ProductoRepository(private val DAO: ProductoDAO, private val API: ProductoApiService)  {
    fun getAllProductos(): LiveData<List<Producto>> = DAO.getAll().map { listaProductos ->
        listaProductos.map { it.toDomain() }
    }

    suspend fun addProducto(productoEntity: ProductoEntity) {
        DAO.insert(productoEntity)
        API.insert(productoEntity.toDomain().toDTO()) // Remoto
    }

    suspend fun delProducto(productoEntity: ProductoEntity) {
        DAO.delete(productoEntity)
        //productoEntity.productoId?.let { API.delete(it) } // Remoto
    }

    suspend fun updateProducto(producto: Producto){
        DAO.update(producto.toEntity())
        //producto.productoId?.let { API.update(it, producto.toDTO()) } // Remoto
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
