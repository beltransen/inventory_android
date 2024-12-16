package com.example.inventoryandroid

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListaProductosViewModel(context: Context): ViewModel() {
    private val productosRepository : ProductoRepository
    val productos: LiveData<List<Producto>>

    init {
        val myDAO = ProductosRoomDatabase.getInstance(context).productoDAO() // Inicializar DAO
        productosRepository = ProductoRepository(myDAO)
        productos = productosRepository.getAllProductos()// Asociamos la lista con la tabla de Peliculas de la BBDD

    }

    fun add_Producto(tarea: Producto?){
        viewModelScope.launch(Dispatchers.IO) {
            if (tarea != null) {
                productosRepository.addProducto(tarea.toEntity())
            }
        }

    }

    fun deleteProducto(contacto: Producto) {
        viewModelScope.launch(Dispatchers.IO) {
            productosRepository.delProducto(contacto.toEntity())
        }
    }

    fun getProducto(index: Int): Producto? {
        return productos.value?.get(index)
    }

    fun size(): Int {
        return productos.value?.size ?: 0
    }

    fun updateProducto(index: Int, producto: Producto){
        viewModelScope.launch(Dispatchers.IO) {
            productosRepository.updateProducto(producto)
        }
    }
}
