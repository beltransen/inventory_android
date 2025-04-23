from fastapi import FastAPI, HTTPException, Depends
import sqlite3
from typing import List
from sql import create_table  # Asumiendo que la función create_table está en el archivo sql.py


# Llama a la función para crear la tabla si no existe
create_table()

app = FastAPI()

# Función para obtener una conexión a la base de datos
def get_db():
    conn = sqlite3.connect('productos.db')
    return conn

@app.get("/productos")
def get_productos():
    conn = sqlite3.connect('productos.db')
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM productos')
    productos = cursor.fetchall()
    conn.close()
    return {"productos": productos}

# Endpoint para agregar un nuevo producto
@app.post("/productos")
def add_producto(producto: dict):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
    INSERT INTO productos (nombre, foto, categoria, precio, codigoDeBarras, cantidadAñadida)
    VALUES (?, ?, ?, ?, ?, ?)
    """, (producto["nombre"], producto["foto"], producto["categoria"], producto["precio"], producto["codigoDeBarras"], producto["cantidadAñadida"]))
    conn.commit()
    conn.close()
    return producto

# Endpoint para actualizar un producto
@app.put("/productos/{producto_id}")
def update_producto(producto_id: int, producto: dict):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        UPDATE productos
        SET nombre = ?, foto = ?, categoria = ?, precio = ?, codigoDeBarras = ?, cantidadAñadida = ?
        WHERE id = ?
    """, (producto["nombre"], producto["foto"], producto["categoria"], producto["precio"], producto["codigoDeBarras"],
          producto["cantidadAñadida"], producto_id))
    conn.commit()
    conn.close()
    return producto

# Endpoint para eliminar un producto
@app.delete("/productos/{producto_id}")
def delete_producto(producto_id: int):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM productos WHERE id = ?", (producto_id,))
    conn.commit()
    conn.close()
    return {"message": "Producto eliminado"}
