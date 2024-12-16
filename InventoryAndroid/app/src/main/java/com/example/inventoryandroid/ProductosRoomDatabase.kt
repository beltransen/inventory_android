package com.example.inventoryandroid

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProductoEntity::class], version = 1)
abstract class ProductosRoomDatabase:RoomDatabase() {
    abstract fun productoDAO(): ProductoDAO

    companion object{
        @Volatile
        private var INSTANCE: ProductosRoomDatabase? = null
        fun getInstance(context: Context): ProductosRoomDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProductosRoomDatabase::class.java, "producto_database"
                ).allowMainThreadQueries().build() // no se utiliza solo para pruebas
                INSTANCE = instance
                return instance
            }
        }
    }
}