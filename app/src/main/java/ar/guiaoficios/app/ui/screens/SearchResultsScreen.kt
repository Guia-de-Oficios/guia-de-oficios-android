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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.ProfessionalSummaryDto
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// PANTALLA 5 STITCH: cliente_resultados_de_profesionales (Filtros PostGIS Reales)
// =============================================================================
@Composable
fun SearchResultsScreen(
    categoryName: String = "Plomeros cerca de vos",
    neighborhood: String = "Caballito, CABA",
    professionals: List<ProfessionalSummaryDto>,
    activeFilter: String = "all",
    onFilterChange: (String) -> Unit = {},
    onSelectProfessional: (String) -> Unit,
    onCallNeighborhoodSupport: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        item {
            Column {
                Text(text = "📍 $neighborhood", style = MaterialTheme.typography.labelMedium, color = Primary)
                Text(text = categoryName, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "● Mostrando ${professionals.size} profesionales verificados ordenados por cercanía",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }

        // Chips horizontales: Todos, Urgencias hoy, Matriculados, Mejor valorados
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = activeFilter == "all",
                    onClick = { onFilterChange("all") },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = activeFilter == "urgent",
                    onClick = { onFilterChange("urgent") },
                    label = { Text("⚡ Urgencias hoy") }
                )
                FilterChip(
                    selected = activeFilter == "licensed",
                    onClick = { onFilterChange("licensed") },
                    label = { Text("✓ Matriculados") }
                )
                FilterChip(
                    selected = activeFilter == "top_rated",
                    onClick = { onFilterChange("top_rated") },
                    label = { Text("★ Mejor valorados") }
                )
            }
        }

        items(professionals, key = { it.id }) { pro ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = pro.full_name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = pro.trade_title, style = MaterialTheme.typography.labelMedium, color = Primary)
                    Text(text = "📍 ${pro.distance_label}", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (pro.is_dni_verified) {
                            Surface(color = SecondaryFixed.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = "✓ DNI verificado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSecondaryFixedVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (pro.is_licensed) {
                            Surface(color = SecondaryFixed.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = "✓ Matrícula habilitada",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSecondaryFixedVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        } else if (pro.attends_emergencies) {
                            Surface(color = TertiaryFixed, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = "⚡ Atiende urgencias hoy",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnTertiaryFixed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "★ ${pro.rating_avg} · ${pro.rating_label}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${pro.jobs_completed_count} trabajos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }

                    Surface(color = SurfaceContainerLow, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Visita y presupuesto", style = MaterialTheme.typography.bodyMedium)
                            Text(pro.visit_price_label, style = MaterialTheme.typography.titleLarge, color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { onSelectProfessional(pro.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                    ) {
                        Text("Ver perfil y disponibilidad →", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Senior Helpline Card
        item {
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, OutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("¿Necesitás ayuda para coordinar?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Llamanos de lunes a viernes de 8 a 18 hs y te conectamos gratis con un oficio del barrio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(onClick = onCallNeighborhoodSupport) {
                        Text("📞 Llamar a asistencia barrial", color = Primary)
                    }
                }
            }
        }
    }
}
