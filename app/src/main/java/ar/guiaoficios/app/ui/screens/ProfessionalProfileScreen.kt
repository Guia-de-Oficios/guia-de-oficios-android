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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.ProfessionalDetailDto
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// PANTALLA 6 STITCH: cliente_perfil_del_profesional_y_contacto
// =============================================================================
@Composable
fun ProfessionalProfileScreen(
    professional: ProfessionalDetailDto,
    onContactWhatsApp: () -> Unit,
    onCallPhone: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Surface(
                color = SurfaceContainerLowest,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No hay intermediarios ni cargos extra. Tu contacto queda registrado para tu seguridad.",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                    Button(
                        onClick = onContactWhatsApp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppActionGreen)
                    ) {
                        Text("💬 Hablar por WhatsApp con ${professional.full_name.substringBefore(" ")}", color = Color.White)
                    }
                    Button(
                        onClick = onCallPhone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                    ) {
                        Text("📞 Llamar por teléfono a ${professional.full_name.substringBefore(" ")}", color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(professional.full_name, style = MaterialTheme.typography.headlineMedium)
                        Text(professional.trade_title, style = MaterialTheme.typography.titleMedium, color = Primary)
                        if (!professional.license_number.isNullOrBlank()) {
                            Text(
                                "✓ ${professional.license_issuer ?: "Matrícula"} #${professional.license_number}",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSecondaryFixedVariant
                            )
                        }
                        Text("📍 Barrios: ${professional.neighborhood}", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "★ ${professional.rating_avg} de 5 estrellas (${professional.reviews_count} opiniones)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("👋 Presentación", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "“${professional.bio}”",
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Text("+${professional.years_experience} Años en el oficio", fontWeight = FontWeight.Bold, color = Primary)
                            Text("${professional.guarantee_percentage}% Trabajos con garantía", fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    border = BorderStroke(2.dp, Primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💵 Valores orientativos claros", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Visita y diagnóstico en tu domicilio:", style = MaterialTheme.typography.titleMedium)
                        Text(professional.visit_price_label, style = MaterialTheme.typography.headlineMedium, color = Secondary)
                        Text("(Este monto se descuenta del total si se realiza el trabajo)", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                }
            }

            if (professional.portfolio.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("📸 Trabajos realizados en el barrio", style = MaterialTheme.typography.titleLarge)
                            professional.portfolio.forEach { itemPortfolio ->
                                Surface(
                                    color = SurfaceContainerLow,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(itemPortfolio.title, fontWeight = FontWeight.Bold)
                                        Text(itemPortfolio.subtitle, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (professional.reviews.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("💬 Opiniones de vecinos verificados", style = MaterialTheme.typography.titleLarge)
                            professional.reviews.forEach { rev ->
                                Surface(
                                    color = SurfaceContainerLow,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(rev.author_name, fontWeight = FontWeight.Bold)
                                            Text("★ ${rev.rating}/5", color = Primary, fontWeight = FontWeight.Bold)
                                        }
                                        if (rev.author_zone.isNotBlank()) {
                                            Text(rev.author_zone, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                        }
                                        Text("“${rev.comment}”", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
