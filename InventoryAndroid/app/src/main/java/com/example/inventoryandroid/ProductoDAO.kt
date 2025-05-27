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

    @Delete
    suspend fun delete(contactoEntity: ProductoEntity)

    @Query("SELECT * FROM producto_table")
    fun getAll(): LiveData<List<ProductoEntity>>

    @Query("SELECT * FROM producto_table WHERE producto_nombre = :nombre")
    fun getByName(nombre: String): LiveData<List<ProductoEntity>>

    @Query("SELECT * FROM producto_table WHERE productoId = :id")
    fun getById(id: Int): LiveData<ProductoEntity> // Método para obtener una tarea por su ID

    @Query("SELECT * FROM producto_table WHERE productoId = :codigo")
    fun getByCodigoBarras(codigo: Long): ProductoEntity?

    @Query("SELECT * FROM producto_table")
    fun getAllNow(): List<ProductoEntity>
}