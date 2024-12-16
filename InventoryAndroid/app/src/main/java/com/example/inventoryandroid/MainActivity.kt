package com.example.inventoryandroid

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryandroid.databinding.ActivityMainBinding
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.mlkit.vision.common.InputImage

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ListaProductosViewModel
    lateinit var miAdaptador: AdaptadorElementos // Adaptador para el RecyclerView
    private lateinit var barcodeScanner: BarcodeScanningActivity


    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contacto = result.data?.getSerializableExtra("contacto") as Producto
            val posicion = result.data?.getIntExtra("posicionClick", -1) ?: -1

            if (contacto.productoId != null) {
                Log.println(Log.DEBUG, "Main", "LLegamos al update contacto = ${contacto}")
                viewModel.updateProducto(contacto.productoId!!,contacto)
            } else {
                Log.println(Log.DEBUG, "Main", "No se recibió un contacto válido o una posición válida")
            }
        } else {
            Log.println(Log.DEBUG, "Main", "Operación Cancelada")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
        viewModel = ViewModelProvider(this, ProductosViewModelFactory(applicationContext)).get(
            ListaProductosViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        barcodeScanner = BarcodeScanningActivity()

        miAdaptador = AdaptadorElementos(viewModel.productos.value ?: mutableListOf(), object : RVClickEvent {
            override fun onItemClick(position: Int) {
                val secondIntent = Intent(applicationContext, DetalleProductoActivity::class.java)
                secondIntent.putExtra("posicionClick", position)
                secondIntent.putExtra("contacto", viewModel.getProducto(position))
                getResult.launch(secondIntent)
            }
        }, object : RVLongClickEvent {
            override fun onItemLongClick(position: Int): Boolean {
                val context = binding.recyclerView.context
                val message = "Número total de tareas: ${viewModel.size()}"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                return true
            }
        })

        binding.recyclerView.adapter = miAdaptador

        binding.floatingActionButton4.setOnClickListener {
            obtenerFoto.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        val lista_observer = Observer<List<Producto>>{ contacto ->
            miAdaptador.setProductos(contacto)
        }
        viewModel.productos.observe(this, lista_observer)

        barcodeScanner = BarcodeScanningActivity()
    }

    private val obtenerFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageBitmap = data?.extras?.get("data") as Bitmap
            Log.e("MainActivity", "JOJOJJOJOJOJOJ $imageBitmap")

            //procesarImagen(imageBitmap)
        }
    }

    private fun procesarImagen(imageBitmap: Bitmap) {
        // Crear un objeto InputImage a partir del bitmap
        val image = InputImage.fromBitmap(imageBitmap, 0)

        // Escanear el código de barras
        barcodeScanner.scanBarcodes(image, object :
            BarcodeScanningActivity.BarcodeScanListener {
            override fun onRawValueDetected(rawValue: String?) {
                Log.e("MainActivity", "JOJOJOJOJOJOJAHORASI $rawValue")
                val crearcontacto = parsearCodigoBarras(rawValue)
                if (crearcontacto != null) {
                    viewModel.deleteProducto(crearcontacto)
                } else {
                    Log.e("MainActivity", "Error al parsear el código QR")
                }
            }

            override fun onBarcodeScanFailed(exception: Exception) {
                Log.e("MainActivity", "Error al escanear el código de barras: ${exception.message}")
            }
        })
    }

    // Método para parsear la información de contacto desde un código QR en formato vCard
    private fun parsearCodigoBarras(url: String?): Producto? {
        if (url.isNullOrEmpty()) {
            return null
        }

        val lines = url.split("\n") // Dividir el vCard en líneas
        var nombre = ""
        var apellidos = ""
        var telefono = ""
        var email = ""
        var empresa = ""
        var ocupacion = ""
        var conocidoEn = "\"${localizacion.latitude}, ${localizacion.longitude}\""

        for (line in lines) {
            // Analizar cada línea para extraer la información relevante
            when {
                line.startsWith("FN:") -> {
                    // Extraer nombre y apellidos
                    val fullName = line.substring(3)
                    val nombres = fullName.split(" ")
                    if (nombres.size > 1) {
                        nombre = nombres[0]
                        apellidos = nombres.subList(1, nombres.size).joinToString(" ")
                    } else {
                        nombre = fullName
                    }
                }
                line.startsWith("TEL;CELL:") -> {
                    // Extraer número de teléfono
                    telefono = line.substring(9)
                }
                line.startsWith("EMAIL;WORK;INTERNET:") -> {
                    // Extraer dirección de correo electrónico
                    email = line.substring(20)
                }
                line.startsWith("ORG:") -> {
                    // Extraer nombre de la empresa
                    empresa = line.substring(4)
                }
                line.startsWith("TITLE:") -> {
                    // Extraer ocupación
                    ocupacion = line.substring(6)
                }
            }
        }

        // Comprobar si se ha encontrado suficiente información para crear un contacto
        return if (nombre.isNotEmpty() && apellidos.isNotEmpty()) {
            // Crear y devolver un objeto Contacto
            Contacto(null, nombre, apellidos, telefono.toIntOrNull() ?: 0, email, empresa, ocupacion, conocidoEn)
        } else {
            null // Si no se encuentra suficiente información, devolver null
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)

        menu?.findItem(R.id.creatorInfo)?.setOnMenuItemClickListener {
            val intent = Intent(this, AcercaDeActivity::class.java)
            startActivity(intent)
            true
        }
        menu?.findItem(R.id.añadirProducto)?.setOnMenuItemClickListener {
            val intent = Intent(this, AcercaDeActivity::class.java)
            startActivity(intent)
            true
        }
        return true
    }
}