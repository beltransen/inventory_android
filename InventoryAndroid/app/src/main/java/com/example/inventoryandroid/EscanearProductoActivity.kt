package com.example.inventoryandroid

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.inventoryandroid.databinding.ActivityEscanearProductoBinding

class EscanearProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEscanearProductoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_escanear_producto)
        setSupportActionBar(binding.toolbarMiperfil)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        binding.botonCancelUsuario.setOnClickListener {
            Toast.makeText(this, "Operación cancelada...", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        binding.botonSaveUsuario.setOnClickListener {
            val nombre = binding.NombreProducto.text.toString()
            val foto = binding.Foto.text.toString()
            val categoria = binding.Categoria.text.toString().toIntOrNull() ?: 0 // Convertir a Int
            val precio = binding.Precio.text.toString()
            val codigoBarras = binding.CodigoBarras.text.toString()
            val cantidad = binding.Cantidad.text.toString()

            // Comprobación de campos vacíos
            if (nombre.isEmpty() || foto.isEmpty() || categoria == 0 || precio.isEmpty() || codigoBarras.isEmpty() || cantidad.isEmpty()) {
                // Mostrar Toast
                Toast.makeText(this, "Todos los campos deben estar rellenos", Toast.LENGTH_SHORT).show()
            } else {
                val producto = Producto(null, nombre, foto, categoria, precio.toFloat(), codigoBarras, cantidad.toInt())

                intent.putExtra("producto", producto)

                setResult(Activity.RESULT_OK, intent)
                finish()

            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}