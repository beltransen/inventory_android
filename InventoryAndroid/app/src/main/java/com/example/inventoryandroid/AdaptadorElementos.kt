package com.example.inventoryandroid

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File


interface RVClickEvent {
    fun onItemClick(position: Int) // Permite a la función usar la vista y el dato de la fila concreta
}

interface RVLongClickEvent {
    fun onItemLongClick(position: Int):Boolean // Permite a la función usar únicamente la posición del elemento en la colección de datos
}

class AdaptadorElementos(
    var data: List<Producto>,
    val clickListener: RVClickEvent? = null,
    val longClickListener: RVLongClickEvent? = null
) : RecyclerView.Adapter<AdaptadorElementos.MyViewHolder>() {

    private var isConnected: Boolean = true  // por defecto true

    fun setConnectionState(connected: Boolean) {
        isConnected = connected
        notifyDataSetChanged()
    }

    inner class MyViewHolder(val row: View) : RecyclerView.ViewHolder(row),
        View.OnClickListener, View.OnLongClickListener {
        val txtNombre: TextView = row.findViewById(R.id.nombre_producto)
        val imgProducto: ImageView = row.findViewById(R.id.imagen_producto)
        val txtCantidad: TextView = row.findViewById(R.id.cantidad_disponible)
        val txtPrecio: TextView = row.findViewById(R.id.precio_producto)

        init {
            row.setOnClickListener(this)
            row.setOnLongClickListener(this)
        }
        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                clickListener?.onItemClick(position)
            }
        }
        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                return longClickListener?.onItemLongClick(position) ?: false
            }
            return false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.elemento_lista, parent, false)
        return MyViewHolder(layout)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val producto = data[position]

        holder.txtNombre.text = producto.nombre
        holder.txtCantidad.text = "Cantidad: ${producto.cantidadAñadida}"
        holder.txtPrecio.text = "Precio: $${producto.precio}"

        val BASE_URL = "http://192.168.0.170:8000/images/"
        val BASE_URL2 = "/data/data/com.example.inventoryandroid/"
        // Elegir la ruta según si hay conexión
        Log.d("NetworkChange", producto.foto)
        val imagenUrl = if (isConnected) {
            BASE_URL + producto.foto
        } else {
            BASE_URL2 + producto.foto // desde servidor        // asumimos que aquí está la ruta local
        }
        Log.d("NetworkChange", imagenUrl)
        Glide.with(holder.itemView.context)
            .load(imagenUrl)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.ic_placeholder_image)
            .error(R.drawable.ic_placeholder_image)
            .into(holder.imgProducto)
    }

    override fun getItemCount(): Int = data.size

    fun setProductos(nuevaLista: List<Producto>) {
        data = nuevaLista
        notifyDataSetChanged()
    }
}
