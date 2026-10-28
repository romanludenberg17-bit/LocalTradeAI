package com.romanludenberg.localtradeai

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.romanludenberg.localtradeai.capture.CropProfile
import com.romanludenberg.localtradeai.capture.ScreenCaptureService

/**
 * First-pass OCR preprocessing using a configurable UI crop profile.
 * Pocket Option is the current target; GENERIC remains available as fallback.
 * This stage only removes known UI bands and upscales the remaining frame.
 */
private fun preprocessBitmap(
    original: Bitmap,
    profile: CropProfile = CropProfile.POCKET_OPTION
): Bitmap {
    val w = original.width
    val h = original.height

    val top = (h * profile.topFraction).toInt().coerceIn(0, h - 1)
    val bottom = (h * profile.bottomFraction).toInt().coerceIn(top + 1, h)
    val left = (w * profile.leftFraction).toInt().coerceIn(0, w - 1)
    val right = (w * profile.rightFraction).toInt().coerceIn(left + 1, w)

    val cropW = right - left
    val cropH = bottom - top

    Log.d(
        "LocalTradeAI.Crop",
        "profile=${profile.name} frame=${w}x${h} crop=left:$left top:$top right:$right bottom:$bottom size=${cropW}x$cropH"
    )

    val cropped = Bitmap.createBitmap(original, left, top, cropW, cropH)

    val scale = minOf(
        2f,
        4096f / cropped.width,
        4096f / cropped.height
    ).coerceAtLeast(1f)

    if (scale == 1f) return cropped

    val scaled = Bitmap.createScaledBitmap(
        cropped,
        (cropped.width * scale).toInt().coerceAtLeast(1),
        (cropped.height * scale).toInt().coerceAtLeast(1),
        true
    )
    if (scaled !== cropped) cropped.recycle()
    return scaled
}

class MainActivity : ComponentActivity() {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var capturing by mutableStateOf(false)

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

    override fun onDestroy() {
        textRecognizer.close()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val frame by ScreenCaptureService.latestFrame.collectAsState()
            var recognizedText by remember { mutableStateOf("No text recognized yet.") }
            var recognizing by remember { mutableStateOf(false) }

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
                                stopService(Intent(this@MainActivity, ScreenCaptureService::class.java))
                                capturing = false
                            },
                            enabled = capturing
                        ) { Text("Stop capture") }
                        Button(
                            onClick = {
                                val bitmap = frame
                                if (bitmap == null) {
                                    recognizedText = "No captured frame available. Start capture first."
                                } else if (!recognizing) {
                                    recognizing = true
                                    recognizedText = "Recognizing…"
                                    textRecognizer.process(
                                        InputImage.fromBitmap(
                                            preprocessBitmap(bitmap, CropProfile.POCKET_OPTION),
                                            0
                                        )
                                    )
                                        .addOnSuccessListener { visionText ->
                                            recognizedText = visionText.text.ifBlank {
                                                "No text found in the cropped Pocket Option frame."
                                            }
                                        }
                                        .addOnFailureListener { error ->
                                            recognizedText = "OCR failed: ${error.localizedMessage ?: "unknown error"}"
                                        }
                                        .addOnCompleteListener {
                                            recognizing = false
                                        }
                                }
                            },
                            enabled = frame != null && !recognizing
                        ) { Text(if (recognizing) "Recognizing…" else "Recognize text") }

                        Card(modifier = Modifier.fillMaxWidth()) {
                            if (frame != null) {
                                Image(
                                    bitmap = frame!!.asImageBitmap(),
                                    contentDescription = "Latest captured screen frame",
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    "No frame yet. Start capture and approve Android's system dialog.",
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Recognized text", style = MaterialTheme.typography.titleMedium)
                                Text(recognizedText, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                        Text(
                            "Pocket Option crop is a calibration starting point. Use GENERIC for the previous 10%/90% vertical fallback when testing other apps.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "On-device OCR uses Google ML Kit. Its terms may permit model-download and diagnostic metrics; see the app privacy notice before public release.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
