import sqlite3

def create_table():
    conn = sqlite3.connect('productos.db')  # Asegúrate de usar el nombre correcto de tu base de datos
    cursor = conn.cursor()

    # Crear la tabla 'productos' si no existe
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS productos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            foto TEXT NOT NULL,
            precio REAL NOT NULL,
            categoria TEXT,
            cantidadAñadida INTEGER NOT NULL,
            codigoDeBarras TEXT NOT NULL
        )
    ''')

    conn.commit()
    conn.close()

if __name__ == '__main__':
    create_table()  # Llama esta función para crear las tablas al iniciar
