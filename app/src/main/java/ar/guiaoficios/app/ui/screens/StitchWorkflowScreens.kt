package ar.guiaoficios.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
// PANTALLA 2 STITCH: registro_google_o_manual (Registro + Login Real JWT)
// =============================================================================
@Composable
fun RegisterAccountScreen(
    initialRole: String = "client",
    initialLoginMode: Boolean = false,
    onContinueGoogle: (String) -> Unit,
    onSubmitManual: (RegisterUserInputDto) -> Unit,
    onSubmitLogin: (email: String, password: String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(initialLoginMode) }
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
                text = if (isLoginMode) "Ingresá a tu cuenta" else "Creá tu cuenta en Guía de Oficios",
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

        if (!isLoginMode) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = role == "client",
                        onClick = { role = "client" },
                        label = { Text("Busco oficios (Vecino)") },
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
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono celular con prefijo (Para coordinar trabajos)") },
                    placeholder = { Text("Ej. 11 5544-3322 (para WhatsApp)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                placeholder = { Text("Ej. maria.elena@correo.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                placeholder = { Text("Al menos 6 números o letras") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            Button(
                onClick = {
                    if (isLoginMode) {
                        onSubmitLogin(email.trim(), password)
                    } else {
                        onSubmitManual(
                            RegisterUserInputDto(
                                full_name = fullName.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                password = password,
                                role = role
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
            ) {
                Text(
                    text = if (isLoginMode) "Iniciar sesión →" else "Crear mi cuenta →",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnPrimary
                )
            }
        }

        item {
            TextButton(
                onClick = { isLoginMode = !isLoginMode },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isLoginMode) "¿No tenés cuenta? Crear una cuenta nueva" else "¿Ya tenés una cuenta? Iniciar sesión",
                    color = Primary
                )
            }
        }
    }
}

// =============================================================================
// PANTALLA 4 STITCH: cliente_resultados_de_profesionales (Filtros PostGIS Reales)
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

// =============================================================================
// PANTALLA 6 STITCH: cliente_solicitud_guiada_a_roberto (PhotoPicker + Upload Real)
// =============================================================================
@Composable
fun GuidedRequestScreen(
    professionalName: String = "Roberto",
    uploadedPhotoUrl: String? = null,
    isUploadingPhoto: Boolean = false,
    onPickPhotoUri: (Uri) -> Unit = {},
    onSubmitRequest: (urgency: String, description: String, photoUrl: String?) -> Unit
) {
    var urgency by remember { mutableStateOf("normal") }
    var description by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickPhotoUri(uri)
        }
    }

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
                AssistChip(onClick = { description = (description + " Gotea la canilla").trim() }, label = { Text("+ Gotea la canilla") })
                AssistChip(onClick = { description = (description + " Pérdida en el inodoro").trim() }, label = { Text("+ Pérdida en el inodoro") })
            }

            Text("3. Foto del inconveniente (Opcional)", style = MaterialTheme.typography.titleMedium)
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isUploadingPhoto -> "Subiendo foto al servidor..."
                                !uploadedPhotoUrl.isNullOrBlank() -> "✓ Foto adjuntada correctamente"
                                else -> "Sacá o elegí una foto de tu galería"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!uploadedPhotoUrl.isNullOrBlank()) {
                            Text(
                                text = uploadedPhotoUrl.substringAfterLast("/"),
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text(if (uploadedPhotoUrl.isNullOrBlank()) "📷 Adjuntar foto" else "Cambiar foto")
                    }
                }
            }
        }

        Button(
            onClick = {
                val finalDesc = description.ifBlank { "Necesito revisión y presupuesto en mi domicilio" }
                onSubmitRequest(urgency, finalDesc, uploadedPhotoUrl)
            },
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
// PANTALLA 7 STITCH: cliente_elegir_contacto_y_confirmaci_n (GDO-24)
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

// =============================================================================
// PANTALLA 8 STITCH: cliente_trabajo_terminado_y_calificaci_n
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

// =============================================================================
// PANTALLA 9 STITCH: profesional_registro_y_verificaci_n_dni (GDO-18 / GDO-26)
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

// =============================================================================
// PANTALLA 10 STITCH: profesional_panel_y_solicitudes_recibidas
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
