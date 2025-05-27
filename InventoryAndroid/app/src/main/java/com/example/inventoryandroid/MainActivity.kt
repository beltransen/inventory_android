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
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryandroid.databinding.ActivityMainBinding
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.navigation.NavigationView
import com.google.mlkit.vision.common.InputImage

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ListaProductosViewModel
    lateinit var miAdaptador: AdaptadorElementos // Adaptador para el RecyclerView
    private lateinit var barcodeScanner: BarcodeScanningActivity
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout:DrawerLayout
    private lateinit var networkReceiver: NetworkChangeReceiver

    private val categoriasMap = mapOf(
        "Electrónica" to 1,
        "Hogar" to 2,
        "Ropa" to 3,
        "Alimentos" to 4
    )


    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val producto = result.data?.getSerializableExtra("producto") as Producto
            viewModel.add_Producto(producto)
        } else {
            Log.println(Log.DEBUG, "Main", "Operación Cancelada")
        }
    }

    private val getResult2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val producto = result.data?.getSerializableExtra("producto") as Producto
            // Log para ver el producto recibido
            Log.d("MainActivity", "Producto recibido: ${producto.nombre}, Código: ${producto.productoId}, Cantidad: ${producto.cantidadAñadida}")
            if (producto.productoId != null) {
                Log.println(Log.DEBUG, "Main", "LLegamos al update contacto = ${producto}")
                viewModel.updateProducto(producto.productoId!!,producto)
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
        navView = binding.navigationView
        drawerLayout = binding.drawerLayout

        // Configurar el SearchView
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return true
            }
        })

        // Configurar el ícono de cierre para ocultar el SearchView
        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setOnClickListener {
            searchView.setQuery("", false) // Limpiar la búsqueda
            searchView.visibility = View.GONE // Ocultar el SearchView
        }

        miAdaptador = AdaptadorElementos(viewModel.productos.value ?: mutableListOf(), object : RVClickEvent {
            override fun onItemClick(position: Int) {
                val secondIntent = Intent(applicationContext, DetalleProductoActivity::class.java)
                secondIntent.putExtra("posicionClick", position)
                secondIntent.putExtra("producto", viewModel.getProducto(position))
                getResult2.launch(secondIntent)
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
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.buscarProducto -> {
                    searchView.visibility = View.VISIBLE
                    searchView.isIconified = false
                    drawerLayout.closeDrawer(navView)
                    true
                }
                R.id.categoria_todas -> {
                    filterByCategory(null) // Muestra todos los productos
                    drawerLayout.closeDrawer(navView)
                    true
                }
                R.id.categoria_electronica -> {
                    filterByCategory(categoriasMap["Electrónica"]) // Electrónica (1)
                    drawerLayout.closeDrawer(navView)
                    true
                }
                R.id.categoria_hogar -> {
                    filterByCategory(categoriasMap["Hogar"]) // Hogar (2)
                    drawerLayout.closeDrawer(navView)
                    true
                }
                R.id.categoria_ropa -> {
                    filterByCategory(categoriasMap["Ropa"]) // Ropa (3)
                    drawerLayout.closeDrawer(navView)
                    true
                }
                R.id.categoria_alimentos -> {
                    filterByCategory(categoriasMap["Alimentos"]) // Alimentos (4)
                    drawerLayout.closeDrawer(navView)
                    true
                }
                else -> false
            }
        }

        // Registrar el receptor de red
        networkReceiver = NetworkChangeReceiver { isConnected ->
            if (isConnected) {
                Log.d("NetworkChange", "Conectado a internet")
                Toast.makeText(this, "Conectado a Internet", Toast.LENGTH_SHORT).show()

                // Llama a setConnectionState y sincronizarBidireccional
                viewModel.setConexionActiva(true)
                viewModel.sincronizarConServidor()
            } else {
                Log.d("NetworkChange", "Sin conexión")
                Toast.makeText(this, "Sin conexión a Internet", Toast.LENGTH_SHORT).show()
                viewModel.setConexionActiva(false)
            }

            // Opcional: actualizar estado en el ViewModel si lo necesitas
            viewModel.setConexionActiva(isConnected)
        }

        val filter = android.content.IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

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

    private fun filterByCategory(categoryId: Int?) {
        val filteredList = if (categoryId == null) {
            viewModel.productos.value ?: emptyList() // Mostrar todos los productos
        } else {
            viewModel.productos.value?.filter { producto ->
                producto.categoria == categoryId
            } ?: emptyList()
        }

        miAdaptador.setProductos(filteredList) // Actualizar el RecyclerView con la lista filtrada
    }

    private fun filter(text: String) {
        val filteredList = viewModel.productos.value?.filter { producto ->
            producto.nombre.contains(text, ignoreCase = true) || producto.productoId?.toString()?.contains(text, ignoreCase = true) == true

        } ?: emptyList()

        // Actualizar el adaptador con la lista filtrada
        miAdaptador.setProductos(filteredList)
    }

    private fun procesarImagen(imageBitmap: Bitmap) {
        val image = InputImage.fromBitmap(imageBitmap, 0)

        barcodeScanner.scanBarcodes(image, object : BarcodeScanningActivity.BarcodeScanListener {
            override fun onRawValueDetected(rawValue: String?) {
                if (rawValue != null) {
                    Log.e("MainActivity", "ALGO ES ALGO")
                    viewModel.restProducto(rawValue.toLong())
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (drawerLayout.isDrawerOpen(navView)) {
                    drawerLayout.closeDrawer(navView)
                } else {
                    drawerLayout.openDrawer(navView)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}