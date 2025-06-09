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
    cursor.execute('SELECT * FROM productos WHERE activo = 1')
    productos = cursor.fetchall()
    conn.close()

    # Convertimos a objetos con claves que coincidan con tu DTO
    lista_productos = []
    for p in productos:
        lista_productos.append({
            "productoId": p[0],
            "nombre": p[1],
            "foto": p[2],
            "precio": p[3],
            "categoria": p[4],
            "cantidadAñadida": p[5],
            "ultimaActualizacion": p[6],
            "activo": p[7]
        })

    return lista_productos  # Devolvemos directamente una lista

@app.post("/productos")
def add_producto(producto: dict):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO productos (id, nombre, foto, categoria, precio, cantidadAñadida, ultimaActualizacion, activo)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """, (
        producto["productoId"],  # Este es el código de barras
        producto["nombre"],
        producto["foto"],
        producto["categoria"],
        producto["precio"],
        producto["cantidadAñadida"],
        producto["ultimaActualizacion"],
        producto["activo"]
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
        SET nombre = ?, foto = ?, categoria = ?, precio = ?, cantidadAñadida = ?, ultimaActualizacion = ?, activo = ?
        WHERE id = ?
    """, (
        producto["nombre"],
        producto["foto"],
        producto["categoria"],
        producto["precio"],
        producto["cantidadAñadida"],
        producto["ultimaActualizacion"],
        producto["activo"],
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

@app.delete("/productos/{producto_id}")
def delete_producto_logico(producto_id: int):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        UPDATE productos
        SET activo = 0, ultimaActualizacion = strftime('%s','now') * 1000
        WHERE id = ?
    """, (producto_id,))
    conn.commit()
    conn.close()
    return {"message": "Producto marcado como inactivo"}
