package com.example.inventoryandroid

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.inventoryandroid.databinding.ActivityEscanearProductoBinding
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.mlkit.vision.common.InputImage

class EscanearProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEscanearProductoBinding
    private lateinit var barcodeScanner: BarcodeScanningActivity

    // Lanzador para seleccionar una foto de la galería
    private val seleccionarFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data // URI de la imagen seleccionada
            if (uri != null) {
                binding.Foto.setText(uri.toString()) // Establece la URI en el campo de texto
                binding.ImagenPreview.setImageURI(uri) // Muestra la vista previa de la imagen
            } else {
                Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_escanear_producto)
        setSupportActionBar(binding.toolbarMiperfil)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        barcodeScanner = BarcodeScanningActivity()

        // Botón para cancelar la operación
        binding.botonCancelUsuario.setOnClickListener {
            Toast.makeText(this, "Operación cancelada...", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        binding.botonEscanearCodigo.setOnClickListener {
            obtenerFoto.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        // Botón para guardar el producto
        binding.botonSaveUsuario.setOnClickListener {
            val nombre = binding.NombreProducto.text.toString()
            val foto = binding.Foto.text.toString()
            val categoria = binding.Categoria.text.toString().toIntOrNull() ?: 0 // Convertir a Int
            val precio = binding.Precio.text.toString()
            val codigoBarras = binding.CodigoBarras.text.toString()
            val cantidad = binding.Cantidad.text.toString()

            // Comprobación de campos vacíos
            if (nombre.isEmpty() || foto.isEmpty() || categoria == 0 || precio.isEmpty() || codigoBarras.isEmpty() || cantidad.isEmpty()) {
                Toast.makeText(this, "Todos los campos deben estar rellenos", Toast.LENGTH_SHORT).show()
            } else {
                val producto = Producto(null, nombre, foto, categoria, precio.toFloat(), codigoBarras, cantidad.toInt())
                intent.putExtra("producto", producto)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        // Botón para seleccionar una foto de la galería
        binding.botonSeleccionarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            seleccionarFoto.launch(intent)
        }
    }

    private val obtenerFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.e("MainActivity", "JOJOJJOJOJOJOJ")
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageBitmap = data?.extras?.get("data") as Bitmap
            Log.e("MainActivity", "BIENBIENBIEN $imageBitmap")

            procesarImagen(imageBitmap)
        }
    }

    private fun procesarImagen(imageBitmap: Bitmap) {
        val image = InputImage.fromBitmap(imageBitmap, 0)

        barcodeScanner.scanBarcodes(image, object : BarcodeScanningActivity.BarcodeScanListener {
            override fun onRawValueDetected(rawValue: String?) {
                if (rawValue != null) {
                    Log.e("MainActivity", "ALGO ES ALGO")
                    binding.CodigoBarras.setText(rawValue)
                } else {
                    Log.e("MainActivity", "Código de barras no detectado o inválido")
                }
            }

            override fun onBarcodeScanFailed(exception: Exception) {
                Log.e("MainActivity", "Error al escanear el código de barras: ${exception.message}")
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
