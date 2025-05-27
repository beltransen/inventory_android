from fastapi import FastAPI, HTTPException
import sqlite3
from sql import create_table  # Asegúrate de que apunte a tu archivo sql.py

create_table()

app = FastAPI()

def get_db():
    conn = sqlite3.connect('productos.db')
    return conn

@app.get("/productos")
def get_productos():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM productos')
    productos = cursor.fetchall()
    conn.close()
    return {"productos": productos}

@app.post("/productos")
def add_producto(producto: dict):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO productos (id, nombre, foto, categoria, precio, cantidadAñadida, ultimaActualizacion)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """, (
        producto["productoId"],  # Este es el código de barras
        producto["nombre"],
        producto["foto"],
        producto["categoria"],
        producto["precio"],
        producto["cantidadAñadida"],
        producto["ultimaActualizacion"]
    ))
    conn.commit()
    conn.close()
    return producto

@app.put("/productos/{producto_id}")
def update_producto(producto_id: int, producto: dict):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        UPDATE productos
        SET nombre = ?, foto = ?, categoria = ?, precio = ?, cantidadAñadida = ?, ultimaActualizacion = ?
        WHERE id = ?
    """, (
        producto["nombre"],
        producto["foto"],
        producto["categoria"],
        producto["precio"],
        producto["cantidadAñadida"],
        producto["ultimaActualizacion"],
        producto_id
    ))
    conn.commit()
    conn.close()
    return producto

@app.delete("/productos/{producto_id}")
def delete_producto(producto_id: int):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM productos WHERE id = ?", (producto_id,))
    conn.commit()
    conn.close()
    return {"message": "Producto eliminado"}
