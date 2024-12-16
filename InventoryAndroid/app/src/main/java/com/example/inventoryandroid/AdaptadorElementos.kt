package com.example.inventoryandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


interface RVClickEvent {
    fun onItemClick(position: Int) // Permite a la función usar la vista y el dato de la fila concreta
}

interface RVLongClickEvent {
    fun onItemLongClick(position: Int):Boolean // Permite a la función usar únicamente la posición del elemento en la colección de datos
}

class AdaptadorElementos (var data: List<Producto>, val clickListener: RVClickEvent? = null, val longClickListener: RVLongClickEvent? = null) : RecyclerView.Adapter<AdaptadorElementos.MyViewHolder>() {

    inner class MyViewHolder(val row: View) : RecyclerView.ViewHolder(row), View.OnClickListener, View.OnLongClickListener {
        val txtNombre: TextView = row.findViewById(R.id.nombre_producto)
        val imgProducto: ImageView = row.findViewById(R.id.imagen_producto)
        val txtCantidad: TextView = row.findViewById(R.id.cantidad_disponible)
        val txtPrecio: TextView = row.findViewById(R.id.precio_producto)

        init {
            // Listener para el clic en toda la fila
            row.setOnClickListener(this)
            // Listener para el clic largo en toda la fila
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
                return longClickListener?.onItemLongClick(position) ?: false // Retornar el resultado del método
            }
            return false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.elemento_lista, parent, false)
        return MyViewHolder(layout)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val producto = data[position]

        // Asignar datos a los elementos del ViewHolder
        holder.txtNombre.text = producto.nombre
        holder.txtCantidad.text = "Cantidad: ${producto.cantidadAñadida}"
        holder.txtPrecio.text = "Precio: $${producto.precio}"

        // Cargar la imagen usando Glide
        Glide.with(holder.itemView.context)
            .load(producto.foto) // URL o recurso local
            .placeholder(R.drawable.ic_placeholder_image) // Placeholder mientras carga la imagen
            .into(holder.imgProducto)

    }

    override fun getItemCount(): Int = data.size


    fun setProductos(nuevaLista: List<Producto>) {
        data = nuevaLista
        notifyDataSetChanged()
    }
}