# inventory_android
Aplicación Android para control de inventario en una tienda (TFG de Yeray Terradillos)

##Para lanzar el servidor.
instalaciones necesarias:

En terminal de linux instalamos curl.
  sudo apt-get install curl

En el entorno virtual del proyecto, no se si tendras que instalarlos o vendran ya por estar en entorno virtual
  pip install uvicorn
  pip install fastapi 
  pip install sqlite3
  

He creado un entorno virtual y desde pycharm desde el proyecto hago lo siguiente.

primero lanzo el siguiente comando para abrir el servidor con la base de datos.

      uvicorn api:app --reload

Esto se haria desde dentro del venv del proyecto. Ahora desde otro terminal podemos hacer un curl

      curl http://127.0.0.1:8000/productos
      
Ahora mismo nos daria una tabla sin datos pero con esto ya podemos comprobar que funciona la base de
datos y nos esta sirviendo cosas.
