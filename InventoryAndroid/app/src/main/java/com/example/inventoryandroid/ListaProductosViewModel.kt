package com.example.inventoryandroid

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListaProductosViewModel(context: Context): ViewModel() {
    private val productosRepository : ProductoRepository
    val productos: LiveData<List<Producto>>
    private var conexionActiva = true

    init {

        val myDAO = ProductosRoomDatabase.getInstance(context).productoDAO() // Inicializar DAO
        val apiService = RetrofitInstance.api // Retrofit configurado
        productosRepository = ProductoRepository(myDAO, apiService)
        productos = productosRepository.getAllProductos()// Asociamos la lista con la tabla de Peliculas de la BBDD
    }

    fun add_Producto(tarea: Producto?){
        viewModelScope.launch(Dispatchers.IO) {
            if (tarea != null) {
                tarea.ultimaActualizacion = System.currentTimeMillis() // <-- AÑADIR ESTO SI NO LO HACES YA
                productosRepository.addProducto(tarea.toEntity())
            }
        }

    }

    fun subirImagen(context: Context, uri: Uri, nombreFoto: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                productosRepository.subirImagen(uri, nombreFoto, context)
            } catch (e: Exception) {
                Log.e("ViewModel", "Error al subir la imagen: ${e.message}")
            }
        }
    }

    fun deleteProducto(contacto: Producto) {
        viewModelScope.launch(Dispatchers.IO) {
            productosRepository.delProducto(contacto.toEntity())
        }
    }

    fun restProducto(codigoBarras: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val producto = productosRepository.getProductoPorCodigoBarras(codigoBarras.toString())
            if (producto != null) {
                if (producto.cantidadAñadida > 0) {
                    producto.cantidadAñadida -= 1
                    productosRepository.updateProducto(producto)
                    Log.i("MainActivity", "Cantidad actualizada. Nuevo valor: ${producto.cantidadAñadida}")
                } else {
                    Log.e("MainActivity", "El producto con código $codigoBarras tiene cantidad 0 en el inventario.")
                }
            } else {
                Log.e("MainActivity", "No existe este código en nuestra BBDD")
            }
        }
    }

    fun getProducto(index: Int): Producto? {
        return productos.value?.get(index)
    }

    fun size(): Int {
        return productos.value?.size ?: 0
    }

    fun updateProducto(index: Long,producto: Producto) {
        viewModelScope.launch(Dispatchers.IO) {
            productosRepository.updateProducto(producto) }
    }

    fun setConexionActiva(conectado: Boolean) {
        conexionActiva = conectado
    }

    fun sincronizarConServidor(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            productosRepository.sincronizarBidireccional(context)
        }
    }
}
