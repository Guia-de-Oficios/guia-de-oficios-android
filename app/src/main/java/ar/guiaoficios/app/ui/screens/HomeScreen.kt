package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.guiaoficios.app.data.model.CategoryDto
import ar.guiaoficios.app.data.model.DiagnosticSuggestionResponseDto
import ar.guiaoficios.app.data.model.UserDto
import ar.guiaoficios.app.ui.theme.*

/**
 * Pantalla 3 de Stitch: `cliente_inicio_y_categor_as`
 * Respeta 1:1 `cliente_inicio_y_categor_as/code.html` y `screen.png`:
 * - Saludo "¡Hola, [Nombre]!" + Pill "Vecino activo" + Ubicación con ícono LocationOn + botón "Cambiar dirección" (56dp)
 * - Buscador "¿Qué necesitás solucionar hoy?" con ícono Search y botón limpiar
 * - Tarjeta de asistencia "¿No sabés qué profesional necesitás?" con ícono ContactSupport y botón "¿Qué está pasando?"
 * - Grilla de 2 columnas con los 8 rubros de PostgreSQL, íconos circulares #EAEDFF y badges ("Matriculados" / "Urgencias")
 * - Tarjeta "Destacado Barrial · 🏆 Top Profesionales del Mes"
 * - Sello inferior de garantía de verificación con DNI
 */
@Composable
fun HomeScreen(
    userName: String,
    savedAddress: String,
    categories: List<CategoryDto>,
    diagnosticResult: DiagnosticSuggestionResponseDto? = null,
    onSelectCategory: (CategoryDto) -> Unit,
    onSearchByQuery: (String) -> Unit = {},
    onRequestDiagnostic: (String) -> Unit = {},
    onUpdateAddress: (String) -> Unit = {},
    onOpenTopMonth: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddressDialog by remember { mutableStateOf(false) }
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var addressDraft by remember(savedAddress) { mutableStateOf(savedAddress) }
    var problemDraft by remember { mutableStateOf("No enciende el calefón de la cocina") }

    val firstName = remember(userName) {
        userName.trim().substringBefore(" ").ifBlank { "Vecino" }
    }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            containerColor = SurfaceContainerLowest,
            title = {
                Text(
                    "Cambiar tu dirección en el barrio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Ingresá tu calle, altura y barrio para ordenar los profesionales más cercanos con PostGIS:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    OutlinedTextField(
                        value = addressDraft,
                        onValueChange = { addressDraft = it },
                        label = { Text("Dirección y barrio") },
                        placeholder = { Text("Ej. Av. Rivadavia 5420, Caballito") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar dirección", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }

    if (showDiagnosticDialog) {
        AlertDialog(
            onDismissRequest = { showDiagnosticDialog = false },
            containerColor = SurfaceContainerLowest,
            title = {
                Text(
                    "¿Qué está pasando en tu casa?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
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
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )
                    Button(
                        onClick = { onRequestDiagnostic(problemDraft) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                    ) {
                        Text("Consultar oficio recomendado", fontWeight = FontWeight.Bold)
                    }

                    if (diagnosticResult != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SecondaryFixed.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SecondaryContainer.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Recomendación: ${diagnosticResult.suggested_category.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OnSecondaryFixedVariant
                                )
                                Text(
                                    text = diagnosticResult.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurface
                                )
                                Button(
                                    onClick = {
                                        showDiagnosticDialog = false
                                        onSelectCategory(diagnosticResult.suggested_category)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                                ) {
                                    Text("Ver ${diagnosticResult.suggested_category.name}s cerca →", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDiagnosticDialog = false }) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp)
    ) {
        // 1. Greeting & Neighborhood Context Anchor (`cliente_inicio_y_categor_as/code.html`)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                            text = "¡Hola, $firstName!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurface
                        )
                        Surface(
                            color = SurfaceContainer,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "Vecino/a activo/a",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = SurfaceContainer)

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ubicación",
                                tint = Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tu ubicación guardada",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = savedAddress.ifBlank { "Av. Rivadavia 5420, Caballito" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showAddressDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, OutlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditLocationAlt,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cambiar dirección",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                }
            }
        }

        // 2. Main Query / Callout Block (`¿Qué necesitás solucionar hoy?`)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "¿Qué necesitás solucionar hoy?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
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
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Primary,
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = OnSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { onSearchByQuery(searchQuery.trim()) }) {
                                    Text("Buscar", fontWeight = FontWeight.ExtraBold, color = PrimaryContainer)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLowest,
                        unfocusedContainerColor = SurfaceContainerLowest,
                        focusedBorderColor = PrimaryContainer,
                        unfocusedBorderColor = OutlineVariant
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                onSearchByQuery(searchQuery.trim())
                            }
                        }
                    )
                )
            }
        }

        // 3. Contextual Diagnostic Help Card (`¿No sabés qué profesional necesitás?`)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactSupport,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¿No sabés qué profesional necesitás?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Contanos qué pasa en tu hogar y te ayudamos a encontrar al indicado sin complicaciones.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { showDiagnosticDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpCenter,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¿Qué está pasando?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 4. Trade Categories 2-Column Grid (Con íconos circulares #EAEDFF y badges exactos de Stitch)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Oficios recomendados en tu barrio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )
                    Text(
                        text = "${categories.size} disponibles",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant
                    )
                }

                categories.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        pair.forEach { cat ->
                            StitchCategoryGridCard(
                                category = cat,
                                onClick = { onSelectCategory(cat) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 5. Destacado Barrial: Top Profesionales del Mes (`cliente_inicio_y_categor_as/code.html`)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                border = BorderStroke(2.dp, PrimaryFixedDim),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTopMonth() }
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Surface(
                            color = Color(0xFFE4E7F9),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "DESTACADO BARRIAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "🏆 Top Profesionales del Mes de Caballito",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )
                    Text(
                        text = "Conocé a los vecinos con mejor puntaje, asistencia perfecta y precios justos votados este mes por el barrio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

                    Surface(
                        onClick = onOpenTopMonth,
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLowest,
                        border = BorderStroke(1.5.dp, PrimaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Ver podio y cuadro de honor",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = PrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Neighborhood Trust Guarantee Strip
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SecondaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verificados",
                            tint = OnSecondaryFixedVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "Todos los profesionales fueron verificados con DNI y referencias de vecinos de la zona.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = OnSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StitchCategoryGridCard(
    category: CategoryDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconVector = remember(category.slug, category.icon_name) {
        when (category.slug.lowercase()) {
            "plomero" -> Icons.Default.WaterDrop
            "electricista" -> Icons.Default.Bolt
            "gasista" -> Icons.Default.LocalFireDepartment
            "aire-acondicionado" -> Icons.Default.AcUnit
            "pintor" -> Icons.Default.FormatPaint
            "albanil" -> Icons.Default.Handyman
            "jardinero" -> Icons.Default.Yard
            "cerrajero" -> Icons.Default.Key
            else -> Icons.Default.Build
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .heightIn(min = 186.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Fila superior: Círculo azul #EAEDFF con ícono + Badge "Matriculados" o "Urgencias"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = category.name,
                            tint = Primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    if (!category.badge_text.isNullOrBlank()) {
                        val isUrgent = category.badge_type == "urgent"
                        Surface(
                            color = if (isUrgent) ErrorContainer else SecondaryFixed,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = if (isUrgent) Icons.Default.WarningAmber else Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = if (isUrgent) OnErrorContainer else OnSecondaryFixedVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = category.badge_text,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUrgent) OnErrorContainer else OnSecondaryFixedVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
                )

                Text(
                    text = category.helper_examples,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = "Buscar vecinos",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
