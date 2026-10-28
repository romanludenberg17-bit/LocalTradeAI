package com.romanludenberg.localtradeai

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.romanludenberg.localtradeai.capture.ScreenCaptureService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var capturing = false

    private val capturePermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenCaptureService.EXTRA_DATA, result.data)
            }
            ContextCompat.startForegroundService(this, serviceIntent)
            capturing = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val frame by ScreenCaptureService.latestFrame.collectAsState()
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("LocalTradeAI", style = MaterialTheme.typography.headlineMedium)
                        Text("Screen analysis prototype", style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = {
                                val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                                capturePermission.launch(manager.createScreenCaptureIntent())
                            },
                            enabled = !capturing
                        ) { Text("Start capture") }
                        Button(
                            onClick = {
                                stopService(Intent(this@MainActivity, ScreenCaptureService::class.java).apply {
                                    action = ScreenCaptureService.ACTION_STOP
                                })
                                capturing = false
                            },
                            enabled = capturing
                        ) { Text("Stop capture") }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            if (frame != null) {
                                Image(
                                    bitmap = frame!!.asImageBitmap(),
                                    contentDescription = "Latest captured screen frame",
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    "No frame yet. Start capture and approve Android's system dialog.",
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
