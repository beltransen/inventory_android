package com.example.inventoryandroid

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeScanningActivity {

    interface BarcodeScanListener {
        fun onRawValueDetected(rawValue: String?)
        fun onBarcodeScanFailed(exception: Exception)
    }

    fun scanBarcodes(image: InputImage, listener: BarcodeScanListener){
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8)
            .build()

        val scanner = BarcodeScanning.getClient()

        val result = scanner.process(image)
            .addOnSuccessListener { barcodes ->
                Log.e("MainActivity", "BIEN al detectar códigos de barras: $barcodes")
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue // Asignar el valor de rawValue
                    val valueType = barcode.valueType
                    listener.onRawValueDetected(rawValue)
                    Log.e("MainActivity", "JEJEJEJEJEJEJ $valueType")
                    Log.e("MainActivity", "JEJEJEJEJEJJEJAJAJAJAJA: $rawValue")
                }
            }
            .addOnFailureListener {
                Log.e("MainActivity", "Error al detectar códigos de barras: $it")
                listener.onBarcodeScanFailed(it)
            }
    }
}
