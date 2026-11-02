package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.*
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// PANTALLA 1 STITCH: bienvenida_y_selecci_n_de_rol
// =============================================================================
@Composable
fun WelcomeRoleScreen(
    onContinueWithRole: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    var selectedRole by remember { mutableStateOf("client") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = SurfaceContainerLow,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, PrimaryFixedDim)
            ) {
                Text(
                    text = "📍 Buenos Aires y Gran Buenos Aires",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Encontrá profesionales de confianza cerca de tu casa",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sin complicaciones, sin registros difíciles y con personas reales de tu barrio verificadas con DNI y oficio.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "¿Cómo querés usar Guía de Oficios hoy?",
                style = MaterialTheme.typography.headlineSmall
            )

            // Opción 1: Cliente / Vecino
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (selectedRole == "client") PrimaryContainer else OutlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRole = "client" }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏠 Necesito un oficio para mi casa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Busco plomeros, electricistas, gasistas y más en mi barrio.",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            // Opción 2: Profesional / Oficio
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (selectedRole == "professional") PrimaryContainer else OutlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRole = "professional" }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🛠️ Ofrezco mis servicios de oficio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quiero recibir solicitudes de vecinos de mi zona.",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onContinueWithRole(selectedRole) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
            ) {
                Text("Comenzar →", style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
                Text("¿Ya tenés una cuenta? Iniciar sesión", color = OnSurface)
            }
        }
    }
}

// =============================================================================
// PANTALLA 2 STITCH: registro_google_o_manual
// =============================================================================
@Composable
fun RegisterAccountScreen(
    initialRole: String = "client",
    onContinueGoogle: (String) -> Unit,
    onSubmitManual: (RegisterUserInputDto) -> Unit,
    onBack: () -> Unit
) {
    var role by remember { mutableStateOf(initialRole) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceContainerLowest)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Creá tu cuenta en Guía de Oficios",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Conectate con vecinos y profesionales de confianza de tu barrio en pocos minutos.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        }

        item {
            OutlinedButton(
                onClick = { onContinueGoogle(role) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, OutlineVariant)
            ) {
                Text("Continuar con Google", style = MaterialTheme.typography.labelLarge, color = OnSurface)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = role == "client",
                    onClick = { role = "client" },
                    label = { Text("Busco oficios (Cliente / Vecino)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = role == "professional",
                    onClick = { role = "professional" },
                    label = { Text("Soy profesional") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nombre y apellido completo") },
                placeholder = { Text("Ej. Carlos Alberto Gómez") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono celular con prefijo (Para coordinar trabajos)") },
                placeholder = { Text("Ej. 11 5544-3322 (para WhatsApp)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                placeholder = { Text("Ej. carlos@correo.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                placeholder = { Text("Al menos 6 números o letras") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Button(
                onClick = {
                    onSubmitManual(
                        RegisterUserInputDto(
                            full_name = fullName,
                            phone = phone,
                            email = email,
                            password = password,
                            role = role
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
            ) {
                Text("Crear mi cuenta →", style = MaterialTheme.typography.titleMedium, color = OnPrimary)
            }
        }
    }
}

// =============================================================================
// PANTALLA 4 STITCH: cliente_resultados_de_profesionales
// =============================================================================
@Composable
fun SearchResultsScreen(
    categoryName: String = "Plomeros cerca de vos",
    neighborhood: String = "Caballito, CABA",
    professionals: List<ProfessionalSummaryDto>,
    onSelectProfessional: (String) -> Unit,
    onCallNeighborhoodSupport: () -> Unit,
    onBack: () -> Unit
) {
    var activeFilter by remember { mutableStateOf("all") } // all | urgent | licensed | top

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
                    text = "● Mostrando ${professionals.size} profesionales verificados (a menos de 3 km)",
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
                    onClick = { activeFilter = "all" },
                    label = { Text("Todos (${professionals.size})") }
                )
                FilterChip(
                    selected = activeFilter == "urgent",
                    onClick = { activeFilter = "urgent" },
                    label = { Text("⚡ Urgencias hoy") }
                )
                FilterChip(
                    selected = activeFilter == "licensed",
                    onClick = { activeFilter = "licensed" },
                    label = { Text("✓ Matriculados") }
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

// =============================================================================
// PANTALLA 5 STITCH: cliente_perfil_del_profesional_y_contacto
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(professional.full_name, style = MaterialTheme.typography.headlineMedium)
                        Text(professional.trade_title, style = MaterialTheme.typography.titleMedium, color = Primary)
                        Text("📍 Barrios: ${professional.neighborhood}", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
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
        }
    }
}

// =============================================================================
// PANTALLA 6 & 7 STITCH: cliente_solicitud_guiada_a_roberto + confirmación
// =============================================================================
@Composable
fun GuidedRequestScreen(
    professionalName: String = "Roberto",
    onSubmitRequest: (urgency: String, description: String, photoName: String?) -> Unit
) {
    var urgency by remember { mutableStateOf("normal") }
    var description by remember { mutableStateOf("") }
    var photoAttached by remember { mutableStateOf("Foto_caño_cocina.jpg") }

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
                AssistChip(onClick = { description += " Gotea la canilla" }, label = { Text("+ Gotea la canilla") })
                AssistChip(onClick = { description += " Pérdida en el inodoro" }, label = { Text("+ Pérdida en el inodoro") })
            }
        }

        Button(
            onClick = { onSubmitRequest(urgency, description, photoAttached) },
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

// =============================================================================
// PANTALLA 8 STITCH: cliente_trabajo_terminado_y_calificaci_n
// =============================================================================
@Composable
fun JobCompletedReviewScreen(
    professionalName: String = "Roberto Carlos Gómez",
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
                label = { Text("Dejale unas palabras a Roberto y a los vecinos") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            Button(
                onClick = {
                    onSubmitReview(
                        CreateReviewInputDto(
                            author_name = "María Elena Castro",
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
        }
    }
}

// =============================================================================
// PANTALLA 10 STITCH: profesional_panel_y_solicitudes_recibidas
// =============================================================================
@Composable
fun ProfessionalDashboardScreen(
    dashboard: ProfessionalDashboardDto,
    onToggleAvailability: (Boolean) -> Unit,
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
            Text("¡Buen día, ${dashboard.professional.full_name.substringBefore(" ")}!", style = MaterialTheme.typography.labelSmall)
            Text(dashboard.professional.full_name, style = MaterialTheme.typography.headlineSmall)
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

        // Bento Grid: 4.9 (142 reseñas), 3 Nuevas hoy, 18 Contactos este mes
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
