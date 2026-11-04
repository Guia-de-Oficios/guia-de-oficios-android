package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.ProfessionalDashboardDto
import ar.guiaoficios.app.data.model.ServiceRequestDto
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// PANTALLA 14 STITCH: profesional_panel_y_solicitudes_recibidas
// =============================================================================
@Composable
fun ProfessionalDashboardScreen(
    dashboard: ProfessionalDashboardDto,
    onToggleAvailability: (Boolean) -> Unit,
    onOpenVerificationScreen: () -> Unit = {},
    onReplyWhatsApp: (ServiceRequestDto) -> Unit,
    onCallNeighbor: (ServiceRequestDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("¡Buen día, ${dashboard.professional.full_name.substringBefore(" ")}!", style = MaterialTheme.typography.labelSmall)
                    Text(dashboard.professional.full_name, style = MaterialTheme.typography.headlineSmall)
                }
                TextButton(onClick = onOpenVerificationScreen) {
                    Text("✓ DNI / Matrícula", color = Primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Control destacado de disponibilidad vecinal
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryFixed.copy(alpha = 0.3f)),
                border = BorderStroke(2.dp, Secondary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (dashboard.professional.is_available_now) "● Estás DISPONIBLE" else "⏸ Disponibilidad PAUSADA",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSecondaryFixedVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Los vecinos de ${dashboard.professional.neighborhood} pueden ver tu perfil y contactarte hoy mismo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = { onToggleAvailability(!dashboard.professional.is_available_now) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (dashboard.professional.is_available_now) "Pausar disponibilidad" else "Activar disponibilidad")
                    }
                }
            }
        }

        // Bento Grid: 4.9 (reseñas), Nuevas hoy, Contactos este mes
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("★ ${dashboard.professional.rating_avg}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${dashboard.professional.reviews_count} reseñas", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${dashboard.new_today_count}", style = MaterialTheme.typography.headlineMedium, color = Primary)
                        Text("Nuevas hoy", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${dashboard.month_contacts}", style = MaterialTheme.typography.headlineMedium)
                        Text("Contactos", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Lista de Solicitudes de Vecinos
        items(dashboard.active_requests, key = { it.id }) { req ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(req.client_name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (req.urgency == "urgente") {
                            Surface(color = ErrorContainer, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    "URGENTE",
                                    color = OnErrorContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text("📍 ${req.client_distance_label} · ${req.client_address}", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Text("“${req.problem_description}”", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                    Button(
                        onClick = { onReplyWhatsApp(req) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppActionGreen)
                    ) {
                        Text("💬 Responder por WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { onCallNeighbor(req) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, Primary)
                    ) {
                        Text("📞 Llamar a ${req.client_name.substringBefore(" ")}", color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
