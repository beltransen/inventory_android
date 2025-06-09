package com.example.inventoryandroid

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductoDAO {
    @Insert
    suspend fun insert(vararg contactoEntity: ProductoEntity)

    @Update
    suspend fun update(contactoEntity: ProductoEntity)

    // Ya no se usa DELETE real
    //@Delete
    //suspend fun delete(contactoEntity: ProductoEntity)

    @Query("UPDATE producto_table SET producto_activo = 0, producto_ultima_actualizacion = :timestamp WHERE productoId = :id")
    suspend fun desactivar(id: Long, timestamp: Long)

    @Query("SELECT * FROM producto_table WHERE producto_activo = 1")
    fun getAll(): LiveData<List<ProductoEntity>>

    @Query("SELECT * FROM producto_table WHERE producto_nombre = :nombre AND producto_activo = 1")
    fun getByName(nombre: String): LiveData<List<ProductoEntity>>

    @Query("SELECT * FROM producto_table WHERE productoId = :id AND producto_activo = 1")
    fun getById(id: Int): LiveData<ProductoEntity>

    @Query("SELECT * FROM producto_table WHERE productoId = :codigo")
    fun getByCodigoBarras(codigo: Long): ProductoEntity?

    @Query("SELECT * FROM producto_table")
    fun getAllNow(): List<ProductoEntity>
}
