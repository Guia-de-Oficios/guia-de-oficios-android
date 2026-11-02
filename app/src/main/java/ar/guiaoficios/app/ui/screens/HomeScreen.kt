package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.CategoryDto
import ar.guiaoficios.app.ui.theme.*

/**
 * Pantalla 3 de Stitch: `cliente_inicio_y_categor_as`
 * Respeta fielmente la gráfica generada con Stitch:
 * - Saludo "¡Hola, María!" + Pill "Vecina activa" + "Av. Rivadavia 5420, Caballito" + Botón "Cambiar dirección" (56dp)
 * - Buscador "¿Qué necesitás solucionar hoy?" ("Ej. Pierde agua la canilla")
 * - Tarjeta de asistencia "¿No sabés qué profesional necesitás?" con botón "¿Qué está pasando?"
 * - Grilla de 2 columnas de oficios con badges ("Matriculados" en Gasista y "Urgencias" en Cerrajero)
 * - Franja de garantía vecinal y BottomNavBar de 4 pestañas (Inicio, Mis contactos, Ayuda, Mi cuenta)
 */
@Composable
fun HomeScreen(
    userName: String = "María",
    savedAddress: String = "Av. Rivadavia 5420, Caballito",
    categories: List<CategoryDto>,
    onSelectCategory: (CategoryDto) -> Unit,
    onOpenDiagnosticHelp: () -> Unit,
    onChangeAddress: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 112.dp)
    ) {
        // 1. Greeting & Neighborhood Context Anchor
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¡Hola, $userName!",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Surface(
                            color = SurfaceContainer,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "Vecina activa",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = SurfaceContainer)

                    Column {
                        Text(
                            text = "Tu ubicación guardada",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = savedAddress,
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface
                        )
                    }

                    OutlinedButton(
                        onClick = onChangeAddress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, OutlineVariant)
                    ) {
                        Text(
                            text = "Cambiar dirección",
                            style = MaterialTheme.typography.labelLarge,
                            color = OnSurface
                        )
                    }
                }
            }
        }

        // 2. Main Query / Callout Block
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "¿Qué necesitás solucionar hoy?",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Elegí un oficio o buscá directamente el inconveniente de tu casa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Ej. Pierde agua la canilla") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        // 3. Contextual Diagnostic Help Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "¿No sabés qué profesional necesitás?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Contanos qué pasa en tu hogar y te ayudamos a encontrar al indicado sin complicaciones.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenDiagnosticHelp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                    ) {
                        Text(
                            text = "¿Qué está pasando?",
                            style = MaterialTheme.typography.labelLarge,
                            color = OnPrimary
                        )
                    }
                }
            }
        }

        // 4. Trade Categories 2-Column Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Oficios recomendados en tu barrio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${categories.size} disponibles",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }

                categories.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        pair.forEach { cat ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 180.dp)
                                    .clickable { onSelectCategory(cat) }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        if (!cat.badge_text.isNullOrEmpty()) {
                                            val isUrgent = cat.badge_type == "urgent"
                                            Surface(
                                                color = if (isUrgent) ErrorContainer else SecondaryFixed,
                                                shape = RoundedCornerShape(999.dp),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            ) {
                                                Text(
                                                    text = cat.badge_text,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isUrgent) OnErrorContainer else OnSecondaryFixedVariant,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = cat.helper_examples,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = OnSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "Buscar vecinos ›",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Neighborhood Trust Guarantee Strip
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ Todos los profesionales fueron verificados con DNI y referencias de vecinos de la zona.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
