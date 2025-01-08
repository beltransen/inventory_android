package com.example.inventoryandroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.map

class ProductoRepository(private val DAO: ProductoDAO)  {
    fun getAllProductos(): LiveData<List<Producto>> = DAO.getAll().map { listaProductos ->
        listaProductos.map { it.toDomain() }
    }

    suspend fun addProducto(productoEntity: ProductoEntity) {
        DAO.insert(productoEntity)
    }

    suspend fun delProducto(productoEntity: ProductoEntity) {
        DAO.delete(productoEntity)
    }

    suspend fun updateProducto(producto: Producto){
        DAO.update(producto.toEntity())
    }

    fun getProductoPorCodigoBarras(codigo: String): Producto? {
        val productoEntity = DAO.getByCodigoBarras(codigo)
        return productoEntity?.toDomain()
    }

}
