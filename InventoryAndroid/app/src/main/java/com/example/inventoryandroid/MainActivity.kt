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

        //viewModel.add_Producto(
        //    Producto(
        //        productoId = null,
        //        nombre = "Lechita",
        //        foto = "data:image/jpeg;base64,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",
        //        categoria = 1, // Cambia según la categoría que quieras asignar
        //        precio = 1.5f, // Precio inicial (puedes ajustar según lo necesario)
        //        codigoDeBarras = "8480000114686",
        //        cantidadAñadida = 5
        //    )
        //)
    }

    private val obtenerFoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.e("MainActivity", "JOJOJJOJOJOJOJ")
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageBitmap = data?.extras?.get("data") as Bitmap
            Log.e("MainActivity", "JOJOJJOJOJOJOJ $imageBitmap")

            procesarImagen(imageBitmap)
        }
    }

    private fun procesarImagen(imageBitmap: Bitmap) {
        val image = InputImage.fromBitmap(imageBitmap, 0)

        barcodeScanner.scanBarcodes(image, object : BarcodeScanningActivity.BarcodeScanListener {
            override fun onRawValueDetected(rawValue: String?) {
                if (rawValue != null) {
                    Log.e("MainActivity", "ALGO ES ALGO")
                    viewModel.restProducto(rawValue)
                } else {
                    Log.e("MainActivity", "Código de barras no detectado o inválido")
                }
            }

            override fun onBarcodeScanFailed(exception: Exception) {
                Log.e("MainActivity", "Error al escanear el código de barras: ${exception.message}")
            }
        })
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