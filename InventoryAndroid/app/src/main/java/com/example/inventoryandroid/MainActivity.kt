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
            val producto = result.data?.getSerializableExtra("producto") as Producto
            viewModel.add_Producto(producto)
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
            val intent = Intent(this, EscanearProductoActivity::class.java)
            getResult.launch(intent)
        }

        val lista_observer = Observer<List<Producto>>{ contacto ->
            miAdaptador.setProductos(contacto)
        }
        viewModel.productos.observe(this, lista_observer)

        barcodeScanner = BarcodeScanningActivity()
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
            obtenerFoto.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            true
        }
        return true
    }
}