package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.CategoryDto
import ar.guiaoficios.app.data.model.DiagnosticSuggestionResponseDto
import ar.guiaoficios.app.ui.theme.*

/**
 * Pantalla 3 de Stitch: `cliente_inicio_y_categor_as`
 * Respeta fielmente la gráfica generada con Stitch y conecta con la API real:
 * - Saludo "¡Hola, María!" + Pill "Vecina activa" + Dirección guardada + "Cambiar dirección" (PATCH /api/v1/users/me/address)
 * - Buscador "¿Qué necesitás solucionar hoy?" (GET /api/v1/professionals?q=...)
 * - Tarjeta de asistencia "¿No sabés qué profesional necesitás?" (POST /api/v1/diagnostic)
 * - Grilla de 2 columnas de oficios con badges ("Matriculados" en Gasista y "Urgencias" en Cerrajero)
 */
@Composable
fun HomeScreen(
    userName: String = "María",
    savedAddress: String = "Av. Rivadavia 5420, Caballito",
    categories: List<CategoryDto>,
    diagnosticResult: DiagnosticSuggestionResponseDto? = null,
    onSelectCategory: (CategoryDto) -> Unit,
    onSearchByQuery: (String) -> Unit = {},
    onRequestDiagnostic: (String) -> Unit = {},
    onUpdateAddress: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddressDialog by remember { mutableStateOf(false) }
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var addressDraft by remember(savedAddress) { mutableStateOf(savedAddress) }
    var problemDraft by remember { mutableStateOf("No enciende el calefón de la cocina") }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Cambiar tu dirección en el barrio", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Ingresá tu calle, altura y barrio para ordenar los profesionales más cercanos con PostGIS:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    OutlinedTextField(
                        value = addressDraft,
                        onValueChange = { addressDraft = it },
                        label = { Text("Dirección y barrio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addressDraft.isNotBlank()) {
                            onUpdateAddress(addressDraft.trim())
                        }
                        showAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                ) {
                    Text("Guardar dirección")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDiagnosticDialog) {
        AlertDialog(
            onDismissRequest = { showDiagnosticDialog = false },
            title = { Text("¿Qué está pasando en tu casa?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Describí con tus palabras el inconveniente y te sugerimos qué oficio corresponde:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    OutlinedTextField(
                        value = problemDraft,
                        onValueChange = { problemDraft = it },
                        label = { Text("Tu problema") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Button(
                        onClick = { onRequestDiagnostic(problemDraft) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                    ) {
                        Text("Consultar oficio recomendado")
                    }

                    if (diagnosticResult != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SecondaryFixed.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Recomendación: ${diagnosticResult.suggested_category.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSecondaryFixedVariant
                                )
                                Text(
                                    text = diagnosticResult.explanation,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(
                                    onClick = {
                                        showDiagnosticDialog = false
                                        onSelectCategory(diagnosticResult.suggested_category)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                                ) {
                                    Text("Ver ${diagnosticResult.suggested_category.name}s cerca →")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDiagnosticDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

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
                            text = "¡Hola, ${userName.substringBefore(" ")}!",
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
                        onClick = { showAddressDialog = true },
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                onSearchByQuery(searchQuery.trim())
                            }
                        }
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            TextButton(onClick = { onSearchByQuery(searchQuery.trim()) }) {
                                Text("Buscar", fontWeight = FontWeight.Bold, color = Primary)
                            }
                        }
                    }
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
                        onClick = { showDiagnosticDialog = true },
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
