package com.example.qrcodescanner

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.qrcodescanner.ui.theme.QrCodeScannerTheme
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class MainActivity : ComponentActivity() {
    private lateinit var btnScan: Button
    private lateinit var tvResult: TextView
    private lateinit var btnAction: Button
    private var scannedResult: String? = null

    private lateinit var barcodeLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        btnScan = findViewById(R.id.btnScan)
        tvResult = findViewById(R.id.tvResult)
        btnAction = findViewById(R.id.btnAction)

        barcodeLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    processImageFromIntent(intent)
                }
            }
        }

        btnScan.setOnClickListener {
            openCamera()
        }

        btnAction.setOnClickListener {
            scannedResult?.let {
                if (it.startsWith("http://") || it.startsWith("https://")) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    startActivity(browserIntent)
                } else {
                    // You can add more specific actions here based on the QR code content
                    tvResult.text = "No action available for this content"
                }
            }
        }
}
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        barcodeLauncher.launch(intent)
    }

    private fun processImageFromIntent(intent: Intent) {
        val image = intent.extras?.get("data") as Bitmap
        val inputImage = InputImage.fromBitmap(image, 0)

        val scanner = BarcodeScanning.getClient()
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    when (barcode.valueType) {
                        Barcode.TYPE_URL -> {
                            scannedResult = barcode.url?.url
                        }
                        Barcode.TYPE_TEXT -> {
                            scannedResult = barcode.displayValue
                        }
                        else -> {
                            scannedResult = barcode.displayValue
                        }
                    }
                }
                displayResult(scannedResult)
            }
            .addOnFailureListener {
                Log.e("QR Scan", "Error processing barcode", it)
                Toast.makeText(this, "Failed to scan QR code", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayResult(result: String?) {
        if (result != null) {
            tvResult.text = result
            if (result.startsWith("http://") || result.startsWith("https://")) {
                btnAction.visibility = View.VISIBLE
                btnAction.text = "Open Link"
            } else {
                btnAction.visibility = View.GONE
            }
        } else {
            tvResult.text = "No QR code detected"
            btnAction.visibility = View.GONE
        }
    }
}