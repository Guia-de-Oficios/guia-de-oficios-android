package ar.guiaoficios.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// PANTALLA 9 STITCH: cliente_solicitud_guiada_a_roberto (PhotoPicker + Upload Real)
// =============================================================================
@Composable
fun GuidedRequestScreen(
    professionalName: String = "Roberto",
    uploadedPhotoUrl: String? = null,
    isUploadingPhoto: Boolean = false,
    onPickPhotoUri: (Uri) -> Unit = {},
    onSubmitRequest: (urgency: String, description: String, photoUrl: String?) -> Unit
) {
    var urgency by remember { mutableStateOf("normal") }
    var description by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickPhotoUri(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLowest)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Solicitud directa · Paso 1 de 2", style = MaterialTheme.typography.labelSmall, color = Outline)
            Text("Contale qué necesitás a $professionalName", style = MaterialTheme.typography.headlineSmall)

            Text("1. ¿Con cuánta urgencia lo necesitás?", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = urgency == "normal",
                    onClick = { urgency = "normal" },
                    label = { Text("📅 Normal (Sin apuro)") }
                )
                FilterChip(
                    selected = urgency == "urgente",
                    onClick = { urgency = "urgente" },
                    label = { Text("⚡ Urgente hoy") }
                )
            }

            Text("2. ¿Qué está pasando en tu casa?", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Ej. Pierde agua la canilla de la cocina debajo de la mesada...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { description = (description + " Gotea la canilla").trim() }, label = { Text("+ Gotea la canilla") })
                AssistChip(onClick = { description = (description + " Pérdida en el inodoro").trim() }, label = { Text("+ Pérdida en el inodoro") })
            }

            Text("3. Foto del inconveniente (Opcional)", style = MaterialTheme.typography.titleMedium)
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isUploadingPhoto -> "Subiendo foto al servidor..."
                                !uploadedPhotoUrl.isNullOrBlank() -> "✓ Foto adjuntada correctamente"
                                else -> "Sacá o elegí una foto de tu galería"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!uploadedPhotoUrl.isNullOrBlank()) {
                            Text(
                                text = uploadedPhotoUrl.substringAfterLast("/"),
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text(if (uploadedPhotoUrl.isNullOrBlank()) "📷 Adjuntar foto" else "Cambiar foto")
                    }
                }
            }
        }

        Button(
            onClick = {
                val finalDesc = description.ifBlank { "Necesito revisión y presupuesto en mi domicilio" }
                onSubmitRequest(urgency, finalDesc, uploadedPhotoUrl)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
        ) {
            Text("Continuar a elegir contacto (WhatsApp o llamada) →", color = OnSecondary)
        }
    }
}
