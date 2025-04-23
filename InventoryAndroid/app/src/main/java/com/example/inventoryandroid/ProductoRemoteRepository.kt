package com.example.inventoryandroid


class ProductoRemoteRepository {

    suspend fun getAllProductos(): List<Producto> {
        return RetrofitInstance.api.getProductos()
    }

    suspend fun addProducto(producto: Producto) {
        RetrofitInstance.api.addProducto(producto)
    }

    suspend fun updateProducto(producto: Producto) {
        producto.productoId?.let {
            RetrofitInstance.api.updateProducto(it, producto)
        }
    }

    suspend fun deleteProducto(id: Int) {
        RetrofitInstance.api.deleteProducto(id)
    }
}
