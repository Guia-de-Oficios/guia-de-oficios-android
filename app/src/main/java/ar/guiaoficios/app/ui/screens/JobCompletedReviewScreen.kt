package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.CreateReviewInputDto
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// PANTALLA 12 STITCH: cliente_trabajo_terminado_y_calificaci_n
// =============================================================================
@Composable
fun JobCompletedReviewScreen(
    professionalName: String = "Roberto Carlos Gómez",
    authorName: String = "María Elena Castro",
    onSubmitReview: (CreateReviewInputDto) -> Unit,
    onLater: () -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    val allHighlights = listOf(
        "Puntualidad",
        "Precio justo",
        "Honestidad y prolijidad",
        "Excelente atención",
        "Solución en el acto"
    )
    val selectedHighlights = remember { mutableStateListOf(*allHighlights.toTypedArray()) }
    var comment by remember {
        mutableStateOf("Impecable trabajo. Roberto vino en el horario acordado, cambió el tramo de cañería con repuestos de calidad y dejó la cocina limpia e impecable.")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLowest)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("✓ Trabajo finalizado con éxito", style = MaterialTheme.typography.titleMedium, color = Secondary)
            Text(professionalName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            Text("¿Cuántas estrellas merece su atención?", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    FilterChip(
                        selected = rating >= star,
                        onClick = { rating = star },
                        label = { Text("★ $star") }
                    )
                }
            }
        }

        item {
            Text("¿Qué destacarías de su servicio?", style = MaterialTheme.typography.titleMedium)
            allHighlights.forEach { itemText ->
                val isSelected = selectedHighlights.contains(itemText)
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            if (isSelected) selectedHighlights.remove(itemText) else selectedHighlights.add(itemText)
                        },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected) SecondaryContainer.copy(alpha = 0.4f) else SurfaceContainerLowest
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(itemText, fontWeight = FontWeight.Bold)
                        Text(if (isSelected) "✓" else "")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Dejale unas palabras a ${professionalName.substringBefore(" ")} y a los vecinos") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            Button(
                onClick = {
                    onSubmitReview(
                        CreateReviewInputDto(
                            author_name = authorName,
                            rating = rating,
                            highlights = selectedHighlights.toList(),
                            comment = comment
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                Text("Publicar calificación y finalizar ✓", color = OnSecondary)
            }
            TextButton(onClick = onLater, modifier = Modifier.fillMaxWidth()) {
                Text("Calificar más tarde", color = OnSurfaceVariant)
            }
        }
    }
}
