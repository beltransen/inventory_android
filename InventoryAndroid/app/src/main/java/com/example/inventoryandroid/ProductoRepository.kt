package com.example.inventoryandroid

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductoRepository(private val DAO: ProductoDAO, private val API: ProductoApiService)  {
    fun getAllProductos(): LiveData<List<Producto>> = DAO.getAll().map { listaProductos ->
        listaProductos.map { it.toDomain() }
    }

    private var isConnected = true

    fun setConnectionState(state: Boolean) {
        isConnected = state
    }


    suspend fun addProducto(productoEntity: ProductoEntity) {
        DAO.insert(productoEntity)
        if (isConnected) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    API.insert(productoEntity.toDomain().toDTO())
                } catch (e: Exception) {
                    Log.e("ProductoRepository", "Error insertando en API", e)
                }
            }
        }
    }

    suspend fun delProducto(productoEntity: ProductoEntity) {
        DAO.delete(productoEntity)
        productoEntity.productoId?.let {
            if (isConnected) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        API.delete(it)
                    } catch (e: Exception) {
                        Log.e("ProductoRepository", "Error eliminando en API", e)
                    }
                }
            }
        }
    }

    suspend fun updateProducto(producto: Producto) {
        DAO.update(producto.toEntity())
        producto.productoId?.let {
            if (isConnected) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        API.update(it, producto.toDTO())
                    } catch (e: Exception) {
                        Log.e("ProductoRepository", "Error actualizando en API", e)
                    }
                }
            }
        }
    }

    fun getProductoPorCodigoBarras(codigo: String): Producto? {
        val productoEntity = DAO.getByCodigoBarras(codigo.toLong())
        return productoEntity?.toDomain()
    }

    fun sincronizarBidireccional() {
        if (!isConnected) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Obtener productos del servidor
                val productosRemotos = API.getAll().map { it.toDomain() }

                // 2. Obtener productos locales
                val productosLocales = DAO.getAllNow().map { it.toDomain() }

                // 3. Crear un mapa local por ID
                val mapaLocales = productosLocales.associateBy { it.productoId }

                // 4. Iterar por cada producto remoto
                for (remoto in productosRemotos) {
                    val local = mapaLocales[remoto.productoId]
                    when {
                        // No existe localmente → insertar
                        local == null -> DAO.insert(remoto.toEntity())

                        // Existe localmente y el remoto es más reciente → reemplazar
                        remoto.ultimaActualizacion > local.ultimaActualizacion -> {
                            DAO.update(remoto.toEntity())
                        }

                        // Existe localmente y el local es más reciente → subir al servidor
                        remoto.ultimaActualizacion < local.ultimaActualizacion -> {
                            remoto.productoId?.let { id ->
                                API.update(id, local.toDTO())
                            }
                        }
                    }
                }

                // 5. Subir productos locales que no existen en remoto
                val idsRemotos = productosRemotos.mapNotNull { it.productoId }.toSet()
                productosLocales.filter { it.productoId !in idsRemotos }.forEach {
                    API.insert(it.toDTO())
                }

                Log.d("ProductoRepository", "Sincronización bidireccional completada.")
            } catch (e: Exception) {
                Log.e("ProductoRepository", "Error en sincronización bidireccional", e)
            }
        }
    }


}
