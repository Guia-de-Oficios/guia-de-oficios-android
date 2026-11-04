package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.ServiceRequestDto
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// PANTALLA 10 STITCH: cliente_elegir_contacto_y_confirmaci_n (GDO-24)
// =============================================================================
@Composable
fun RequestConfirmationScreen(
    request: ServiceRequestDto,
    onOpenWhatsApp: (ServiceRequestDto) -> Unit,
    onCallProfessional: (ServiceRequestDto) -> Unit,
    onGoToJobCompletedReview: (String) -> Unit,
    onBackHome: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                color = SecondaryFixed.copy(alpha = 0.5f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = "✓ Solicitud registrada · Paso 2 de 2",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSecondaryFixedVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¡Listo! Ya podés hablar con ${request.professional_name.substringBefore(" ")}",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Tu pedido quedó guardado en Guía de Oficios para tu tranquilidad. Elegí cómo preferís comunicarte ahora:",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resumen de tu solicitud", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Profesional: ${request.professional_name} (${request.professional_license})")
                    Text("Dirección: ${request.client_address}")
                    Text("Urgencia: ${if (request.urgency == "urgente") "⚡ Urgente hoy" else "📅 Normal"}")
                    Text("Problema: “${request.problem_description}”", fontWeight = FontWeight.SemiBold)
                    if (!request.photo_url.isNullOrBlank()) {
                        Text("📷 Foto adjunta subida al servidor", style = MaterialTheme.typography.labelSmall, color = Primary)
                    }
                }
            }
        }

        item {
            Button(
                onClick = { onOpenWhatsApp(request) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppActionGreen)
            ) {
                Text("💬 Abrir WhatsApp con mensaje listo →", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Button(
                onClick = { onCallProfessional(request) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
            ) {
                Text("📞 Llamar por teléfono a ${request.professional_name.substringBefore(" ")}", color = Color.White)
            }
        }

        item {
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¿${request.professional_name.substringBefore(" ")} ya finalizó el trabajo en tu domicilio?",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onGoToJobCompletedReview(request.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, Secondary)
            ) {
                Text("⭐ Calificar trabajo terminado →", color = Secondary, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al inicio", color = OnSurfaceVariant)
            }
        }
    }
}
