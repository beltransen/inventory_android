package com.example.inventoryandroid

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.inventoryandroid.databinding.ActivityDetalleProductoBinding
import com.google.mlkit.vision.common.InputImage
import java.io.File

class DetalleProductoActivity : AppCompatActivity() {
    lateinit var binding: ActivityDetalleProductoBinding
    private var fotoUri: String? = null
    private var codigoBarrasActual: String? = null
    private lateinit var barcodeScanner: BarcodeScanningActivity
    lateinit var viewModel: ListaProductosViewModel

    private val categoriasMap = mapOf(
        "Electrónica" to 1,
        "Hogar" to 2,
        "Ropa" to 3,
        "Alimentos" to 4
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detalle_producto)
        setSupportActionBar(binding.toolbarMiperfil)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        viewModel = ViewModelProvider(this, ProductosViewModelFactory(applicationContext))
            .get(ListaProductosViewModel::class.java)

        barcodeScanner = BarcodeScanningActivity() // Inicialización correcta

        val position = intent.getIntExtra("posicionClick", -1)
        val producto = intent.getSerializableExtra("producto") as? Producto

        if (producto != null && position != -1) {
            binding.NombreProducto.setText(producto.nombre)
            binding.Precio.setText(producto.precio.toString())
            binding.CodigoBarras.setText(producto.productoId?.toString() ?: "")
            binding.Cantidad.setText(producto.cantidadAñadida.toString())

            fotoUri = producto.foto
            Glide.with(this)
                .load(fotoUri)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(binding.ImagenPreview)

            val categorias = categoriasMap.keys.toList()
            val categoriaSeleccionada = categoriasMap.entries.find { it.value == producto.categoria }?.key
            val index = categorias.indexOfFirst { it == categoriaSeleccionada }
            if (index != -1) {
                binding.Categoria.setSelection(index)
            }
        }

        binding.botonCancelUsuario.setOnClickListener {
            Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        binding.botonEscanearCodigo.setOnClickListener {
            obtenerCodigo.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        binding.botonSaveUsuario.setOnClickListener {
            guardarProducto(producto)
        }

        binding.ImagenPreview.setOnClickListener {
            mostrarOpcionesImagen()
        }
    }

    private fun guardarProducto(producto: Producto?) {
        val nombre = binding.NombreProducto.text.toString()
        val categoriaId = categoriasMap[binding.Categoria.selectedItem.toString()] ?: 0
        val precioFloat = binding.Precio.text.toString().toFloatOrNull()
        val cantidadInt = binding.Cantidad.text.toString().toIntOrNull()
        val productoId = binding.CodigoBarras.text.toString().toLongOrNull()


        if (nombre.isEmpty() || fotoUri.isNullOrEmpty() || categoriaId == 0 ||
            precioFloat == null || productoId == null || cantidadInt == null) {
            Toast.makeText(this, "Todos los campos deben estar rellenos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevoProducto = Producto(
            productoId = productoId,
            nombre = nombre,
            foto = fotoUri ?: "",
            categoria = categoriaId,
            precio = precioFloat,
            cantidadAñadida = cantidadInt,
            ultimaActualizacion = System.currentTimeMillis() ,
            activo = 1 // Activo por defecto al actualiizar
        )

        intent.putExtra("producto", nuevoProducto)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf("Seleccionar desde galería", "Tomar una foto")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Elige una opción")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> seleccionarFoto.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
                    1 -> obtenerFoto.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                }
            }
            .show()
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
        val fileName = "${codigoBarrasActual ?: System.currentTimeMillis()}.jpg"
        val file = File(filesDir, fileName)
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        return Uri.fromFile(file)
    }

    private val obtenerFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                fotoUri = saveImageToCache(it).toString()
                binding.ImagenPreview.setImageBitmap(it)
            } ?: Toast.makeText(this, "Error al capturar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    private val obtenerCodigo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { procesarImagen(it) }
        }
    }

    private fun procesarImagen(imageBitmap: Bitmap) {
        val image = InputImage.fromBitmap(imageBitmap, 0)
        barcodeScanner.scanBarcodes(image, object : BarcodeScanningActivity.BarcodeScanListener {
            override fun onRawValueDetected(rawValue: String?) {
                rawValue?.let {
                    binding.CodigoBarras.setText(it)
                    codigoBarrasActual = it
                }
            }

            override fun onBarcodeScanFailed(exception: Exception) {
                Log.e("DetalleProductoActivity", "Error: ${exception.message}")
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun saveImageToCache(bitmap: Bitmap): Uri {
        val fileName = "${codigoBarrasActual ?: System.currentTimeMillis()}.jpg"
        val file = File(filesDir, fileName)
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        return Uri.fromFile(file)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_info_producto, menu)

        menu?.findItem(R.id.eliminarContacto)?.setOnMenuItemClickListener {
            Log.d("MainActivity", "se ha hecho click en el boton del borrarContacto")
            val producto = intent.getSerializableExtra("producto") as Producto
            viewModel.deleteProducto(producto) // Eliminar el contacto
            finish() // Cerrar la actividad después de eliminar el contacto
            true
        }

        return true
    }
}
