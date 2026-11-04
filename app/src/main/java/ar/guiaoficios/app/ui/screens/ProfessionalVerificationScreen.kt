package ar.guiaoficios.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.OnboardingVerificationInputDto
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// PANTALLA 13 STITCH: profesional_registro_y_verificaci_n_dni (GDO-18 / GDO-26)
// =============================================================================
@Composable
fun ProfessionalVerificationScreen(
    professionalId: String,
    dniFrontUrl: String? = null,
    dniBackUrl: String? = null,
    licenseDocUrl: String? = null,
    onUploadDocumentUri: (uri: Uri, category: String) -> Unit = { _, _ -> },
    onSubmitVerification: (OnboardingVerificationInputDto) -> Unit,
    onSkipToDashboard: () -> Unit
) {
    var tradeTitle by remember { mutableStateOf("Gasista y Plomero Matriculado") }
    var neighborhood by remember { mutableStateOf("Caballito, Flores y Almagro") }
    var licenseNumber by remember { mutableStateOf("18.492") }
    var licenseIssuer by remember { mutableStateOf("ENARGAS") }

    val dniFrontPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onUploadDocumentUri(uri, "dni_front")
    }
    val dniBackPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onUploadDocumentUri(uri, "dni_back")
    }
    val licensePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onUploadDocumentUri(uri, "license_doc")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Surface(color = SurfaceContainerLow, shape = RoundedCornerShape(999.dp)) {
                Text(
                    text = "🔒 Verificación de Seguridad Vecinal",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Verificá tu DNI y Matrícula", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Para generar confianza en los vecinos mayores de tu barrio, validamos tu identidad y credencial profesional.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = tradeTitle,
                onValueChange = { tradeTitle = it },
                label = { Text("Especialidad principal / Título") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = neighborhood,
                onValueChange = { neighborhood = it },
                label = { Text("Barrios donde trabajás") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. Fotos de tu DNI (Frente y Dorso)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                dniFrontPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (dniFrontUrl.isNullOrBlank()) "📷 DNI Frente" else "✓ DNI Frente OK")
                        }
                        OutlinedButton(
                            onClick = {
                                dniBackPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (dniBackUrl.isNullOrBlank()) "📷 DNI Dorso" else "✓ DNI Dorso OK")
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("2. Matrícula Profesional (Opcional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = licenseIssuer,
                            onValueChange = { licenseIssuer = it },
                            label = { Text("Entidad (ENARGAS/COPIME)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            label = { Text("N° Matrícula") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            licensePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (licenseDocUrl.isNullOrBlank()) "📷 Subir foto de credencial / matrícula" else "✓ Credencial subida (${licenseDocUrl.substringAfterLast("/")})")
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSubmitVerification(
                        OnboardingVerificationInputDto(
                            primary_category_id = 1,
                            trade_title = tradeTitle,
                            specialties = listOf("Urgencias 24hs", "Pérdidas de agua y gas", "Termotanques"),
                            neighborhood = neighborhood,
                            coverage_radius_km = 5,
                            dni_front_url = dniFrontUrl ?: "https://guiaoficios.ar/uploads/dni_frente_verificado.jpg",
                            dni_back_url = dniBackUrl ?: "https://guiaoficios.ar/uploads/dni_dorso_verificado.jpg",
                            license_number = licenseNumber,
                            license_issuer = licenseIssuer,
                            license_doc_url = licenseDocUrl ?: "https://guiaoficios.ar/uploads/matricula_enargas.jpg"
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                Text("Guardar verificación e ir a mi panel →", color = OnSecondary, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onSkipToDashboard, modifier = Modifier.fillMaxWidth()) {
                Text("Ir directo al Panel Profesional", color = Primary)
            }
        }
    }
}
