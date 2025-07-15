package com.example.inventoryandroid

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

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

    suspend fun subirImagen(fotoUri: Uri, nombre: String, context: Context) {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(fotoUri)
        val requestBody = inputStream?.readBytes()?.let {
            okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/*"), it)
        } ?: return

        val part = MultipartBody.Part.createFormData("file", nombre, requestBody)

        try {
            val response = API.uploadImage(nombre, part)
            if (response.isSuccessful) {
                Log.d("ProductoRepository", "Imagen subida con éxito")
            } else {
                Log.e("ProductoRepository", "Error subiendo imagen: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Excepción subiendo imagen", e)
        }
    }

    suspend fun delProducto(productoEntity: ProductoEntity) {
        val updatedProducto = productoEntity.copy(
            activo = 0,
            ultimaActualizacion = System.currentTimeMillis()
        )
        DAO.desactivar(productoEntity.productoId, productoEntity.ultimaActualizacion)
        // Si hay conexión, actualizamos en la API remota
        if (isConnected && updatedProducto.productoId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    API.update(updatedProducto.toDomain().productoId, updatedProducto.toDomain().toDTO())
                } catch (e: Exception) {
                    Log.e("ProductoRepository", "Error actualizando producto como inactivo en API", e)
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
                // 1. Obtener productos remotos (ya filtrados solo activos)
                val productosRemotos = API.getAll().map { it.toDomain() }

                // 2. Obtener productos locales, activos o no
                val productosLocales = DAO.getAllNow().map { it.toDomain() }

                val mapaLocales = productosLocales.associateBy { it.productoId }
                val mapaRemotos = productosRemotos.associateBy { it.productoId }

                // 3. Actualizar registros existentes
                for (remoto in productosRemotos) {
                    val local = mapaLocales[remoto.productoId]
                    when {
                        local == null -> DAO.insert(remoto.toEntity()) // nuevo remoto → insertar localmente
                        remoto.ultimaActualizacion > local.ultimaActualizacion -> DAO.update(remoto.toEntity())
                        remoto.ultimaActualizacion < local.ultimaActualizacion -> {
                            remoto.productoId?.let { id -> API.update(id, local.toDTO()) }
                        }
                    }
                }

                // 4. Subir productos locales que no existen en remoto y están activos
                val idsRemotos = productosRemotos.mapNotNull { it.productoId }.toSet()
                productosLocales.filter {
                    it.productoId !in idsRemotos && it.activo == 1
                }.forEach {
                    API.insert(it.toDTO())
                }

                // 5. Desactivar remotamente los que están inactivos localmente
                productosLocales.filter { it.activo == 0 }.forEach {
                    it.productoId?.let { id ->
                        API.update(id, it.toDTO())
                    }
                }

                Log.d("ProductoRepository", "Sincronización bidireccional completada.")
            } catch (e: Exception) {
                Log.e("ProductoRepository", "Error en sincronización bidireccional", e)
            }
        }
    }




}
