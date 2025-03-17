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
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.inventoryandroid.databinding.ActivityEscanearProductoBinding
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.mlkit.vision.common.InputImage
import java.io.File

class EscanearProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEscanearProductoBinding
    private lateinit var barcodeScanner: BarcodeScanningActivity
    private var fotoUri: String? = null // Variable para almacenar la URI de la foto seleccionada
    private val categoriasMap = mapOf(
        "Electrónica" to 1,
        "Hogar" to 2,
        "Ropa" to 3,
        "Alimentos" to 4
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_escanear_producto)
        setSupportActionBar(binding.toolbarMiperfil)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        barcodeScanner = BarcodeScanningActivity()
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }


        // Configura el Spinner con un adaptador dinámico si no usas android:entries
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categoriasMap.keys.toList() // Muestra solo los nombres de las categorías
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.Categoria.adapter = adapter

        // Botón para cancelar la operación
        binding.botonCancelUsuario.setOnClickListener {
            Toast.makeText(this, "Operación cancelada...", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        binding.botonEscanearCodigo.setOnClickListener {
            obtenerCodigo.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        // Botón para guardar el producto
        binding.botonSaveUsuario.setOnClickListener {
            val nombre = binding.NombreProducto.text.toString()
            val categoriaSeleccionada = binding.Categoria.selectedItem.toString()
            val categoriaId = categoriasMap[categoriaSeleccionada] ?: 0 // ID de la categoría
            val precio = binding.Precio.text.toString()
            val codigoBarras = binding.CodigoBarras.text.toString()
            val cantidad = binding.Cantidad.text.toString()

            // Validaciones de campos vacíos y tipos
            val precioFloat = precio.toFloatOrNull()
            val cantidadInt = cantidad.toIntOrNull()

            if (nombre.isEmpty() || fotoUri.isNullOrEmpty() || categoriaId == 0 ||
                precio.isEmpty() || precioFloat == null ||
                codigoBarras.isEmpty() ||
                cantidad.isEmpty() || cantidadInt == null) {
                Toast.makeText(this, "Todos los campos deben estar rellenos correctamente", Toast.LENGTH_SHORT).show()
            } else {
                // Si todo está bien, crea el producto
                val producto = Producto(null, nombre, fotoUri ?: "", categoriaId, precioFloat, codigoBarras, cantidadInt)
                intent.putExtra("producto", producto)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        binding.ImagenPreview.setOnClickListener {
            val opciones = arrayOf("Seleccionar desde galería", "Tomar una foto")
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Elige una opción")
            builder.setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        // Seleccionar desde galería
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        intent.type = "image/*"
                        seleccionarFoto.launch(intent)
                    }
                    1 -> {
                        // Tomar una foto
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        obtenerFoto.launch(intent)
                    }
                }
            }
            builder.show()
        }

    }


    private val obtenerFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                // Muestra la imagen capturada en el ImageView
                binding.ImagenPreview.scaleType = ImageView.ScaleType.FIT_CENTER
                binding.ImagenPreview.setImageBitmap(imageBitmap)

                // Opcional: Guarda la foto en el almacenamiento local o en una variable
                val uri = saveImageToCache(imageBitmap)
                fotoUri = uri.toString()
            } else {
                Toast.makeText(this, "Error al capturar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val seleccionarFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data // URI de la imagen seleccionada
            if (uri != null) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri) // Convertir URI a Bitmap
                    binding.ImagenPreview.scaleType = ImageView.ScaleType.FIT_CENTER
                    binding.ImagenPreview.setImageBitmap(bitmap) // Muestra la vista previa de la imagen

                    // Guardar el bitmap en almacenamiento interno
                    val nuevaUri = saveImageToInternalStorage(bitmap)
                    fotoUri = nuevaUri.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val file = File(filesDir, "image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        return Uri.fromFile(file)
    }


    private val obtenerCodigo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

    private fun saveImageToCache(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        return Uri.fromFile(file)
    }
}
