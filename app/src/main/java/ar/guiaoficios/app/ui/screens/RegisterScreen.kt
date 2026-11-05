package ar.guiaoficios.app.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.guiaoficios.app.data.model.CABA_GBA_NEIGHBORHOODS
import ar.guiaoficios.app.data.model.NeighborhoodOption
import ar.guiaoficios.app.data.model.RegisterUserInputDto
import ar.guiaoficios.app.data.model.normalizeArgentineMobilePhone
import ar.guiaoficios.app.ui.components.GdoBrandLogo
import ar.guiaoficios.app.ui.components.GoogleColorIcon
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// VISTA #3 STITCH (`[AND-2.3]` & `[AND-2.4]`): registro_google_o_manual
// - Selector de Rol ('Soy Vecino / Cliente' vs 'Soy Profesional') con borde azul
//   #1E40AF, fondo #EFF6FF y tilde verde #15803D
// - Validación individual por campo con `isError` + `supportingText`:
//   (1) Nombre y apellido (>= 3 letras)
//   (2) Teléfono celular argentino (normalizando +54 9 11)
//   (3) Email válido (Patterns.EMAIL_ADDRESS)
//   (4) Barrio (selector desplegable CABA/GBA con coordenadas reales)
//   (5) Dirección (calle y altura en el barrio)
//   (6) Contraseña (>= 6 caracteres con ojito mostrar/ocultar)
// - Bloque de aceptación de 'Términos de Convivencia Vecinal y Política de Privacidad'
//   con diálogo de lectura accesible
// - Sin GoogleAccountInputDialog falso
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAccountScreen(
    initialRole: String = "client",
    initialLoginMode: Boolean = false,
    isLoading: Boolean = false,
    authError: String? = null,
    onClearAuthError: () -> Unit = {},
    onContinueGoogle: (email: String, fullName: String, role: String, neighborhood: String, address: String) -> Unit,
    onSubmitManual: (RegisterUserInputDto) -> Unit,
    onSubmitLogin: (emailOrPhone: String, password: String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(initialLoginMode) }
    var role by remember { mutableStateOf(initialRole) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedNeighborhood by remember { mutableStateOf<NeighborhoodOption?>(CABA_GBA_NEIGHBORHOODS.first()) }
    var neighborhoodExpanded by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(true) }

    // Errores individuales por campo (AND-2.3b)
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var neighborhoodError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }
    var googleNotice by remember { mutableStateOf<String?>(null) }

    // Diálogo accesible de lectura de Términos de Convivencia Vecinal y Política de Privacidad (AND-2.4)
    var legalDialogTopic by remember { mutableStateOf<String?>(null) }

    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) {
                googleNotice = null
                val derivedName = accountName.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                val nbLabel = selectedNeighborhood?.label ?: "Caballito, CABA"
                onContinueGoogle(
                    accountName,
                    derivedName.ifBlank { "Usuario Google" },
                    role,
                    nbLabel,
                    address.trim()
                )
            } else {
                googleNotice = "No se pudo leer la cuenta de Google del dispositivo. Completá tus datos manualmente abajo."
            }
        } else {
            googleNotice = "Cancelaste la selección de cuenta de Google. Podés registrarte completando tus datos abajo."
        }
    }

    val launchGoogleFlow: () -> Unit = {
        googleNotice = null
        onClearAuthError()
        runCatching {
            val intent = AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null
            )
            googleAccountPickerLauncher.launch(intent)
        }.onFailure {
            googleNotice = "Tu teléfono no tiene cuentas de Google configuradas. Completá el formulario manual abajo."
        }
    }

    if (legalDialogTopic != null) {
        LegalTermsAccessibleDialog(
            topic = legalDialogTopic!!,
            onAcceptAndClose = {
                acceptedTerms = true
                termsError = null
                legalDialogTopic = null
            },
            onDismiss = { legalDialogTopic = null }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = SurfaceContainerLowest,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, SurfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver atrás",
                                tint = OnSurface
                            )
                        }
                    }

                    GdoBrandLogo(badgeSize = 36.dp)

                    Spacer(modifier = Modifier.width(44.dp))
                }
            }
        },
        containerColor = Surface
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Encabezado de Bienvenida
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = SurfaceContainer,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = PrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Red Vecinal Segura",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = if (isLoginMode) "Ingresá a tu cuenta de Guía de Oficios" else "Creá tu cuenta en Guía de Oficios",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )
                    Text(
                        text = if (isLoginMode)
                            "Ingresá con tu cuenta de Google o con tu correo/teléfono y contraseña."
                        else
                            "Conectate con vecinos y profesionales de confianza de tu barrio en pocos minutos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            // 2. Selector de Rol con borde azul #1E40AF, fondo #EFF6FF y tilde verde #15803D (AND-2.3a)
            if (!isLoginMode) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Me registro como:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StitchRegisterRoleCard(
                                title = "Soy Vecino / Cliente",
                                subtitle = "Busco oficios",
                                icon = Icons.Default.PersonSearch,
                                selected = role == "client",
                                onClick = { role = "client" },
                                modifier = Modifier.weight(1f)
                            )
                            StitchRegisterRoleCard(
                                title = "Soy Profesional",
                                subtitle = "Plomero, Gasista, etc.",
                                icon = Icons.Default.Handyman,
                                selected = role == "professional",
                                onClick = { role = "professional" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Botón rápido: Continuar con Google (sin popup falso)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = launchGoogleFlow,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLowest,
                        border = BorderStroke(2.dp, OutlineVariant),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleColorIcon(modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continuar con Google",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        }
                    }
                    if (!googleNotice.isNullOrBlank()) {
                        Surface(
                            color = SurfaceContainerLow,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PrimaryFixedDim),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = googleNotice!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurface,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // 4. Divisor
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                    Text(
                        text = if (isLoginMode) "O ingresá con tus datos" else "O completá tus datos a mano",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                }
            }

            if (!isLoginMode) {
                // 5. Nombre y Apellido Completo (>= 3 letras)
                item {
                    StitchValidatedInputField(
                        icon = Icons.Default.Person,
                        label = "Nombre y apellido completo",
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            fullNameError = null
                            onClearAuthError()
                        },
                        placeholder = "Ej. Carlos Alberto Gómez",
                        isError = fullNameError != null,
                        supportingText = fullNameError,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                // 6. Teléfono Celular Argentino con normalización +54 9 11
                item {
                    val digitsCount = phone.count { it.isDigit() }
                    val normalizedPreview = if (digitsCount >= 8) normalizeArgentineMobilePhone(phone) else null
                    StitchValidatedInputField(
                        icon = Icons.Default.Call,
                        label = "Teléfono celular con prefijo",
                        badgeText = "Para coordinar por WhatsApp",
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = null
                            onClearAuthError()
                        },
                        placeholder = "Ej. 11 5544-3322 o +54 9 11 5544-3322",
                        isError = phoneError != null,
                        supportingText = phoneError ?: normalizedPreview?.let { "Se guardará normalizado como: $it" },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                // 7. Selector Desplegable de Barrio de CABA / GBA (AND-2.3b)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Tu barrio en CABA o GBA",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            }
                            selectedNeighborhood?.let { nb ->
                                Text(
                                    text = nb.zone,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryContainer
                                )
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = neighborhoodExpanded,
                            onExpandedChange = { neighborhoodExpanded = !neighborhoodExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedNeighborhood?.label ?: "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Seleccioná tu barrio") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = neighborhoodExpanded)
                                },
                                isError = neighborhoodError != null,
                                supportingText = {
                                    if (neighborhoodError != null) {
                                        Text(
                                            text = neighborhoodError!!,
                                            color = ErrorColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Text(
                                            text = "Usamos tu barrio para mostrarte oficios verificados a pocas cuadras.",
                                            color = OnSurfaceVariant
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceContainerLowest,
                                    unfocusedContainerColor = SurfaceContainerLowest,
                                    focusedBorderColor = PrimaryContainer,
                                    unfocusedBorderColor = OutlineVariant,
                                    errorBorderColor = ErrorColor
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = neighborhoodExpanded,
                                onDismissRequest = { neighborhoodExpanded = false },
                                modifier = Modifier.background(SurfaceContainerLowest)
                            ) {
                                CABA_GBA_NEIGHBORHOODS.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = option.label,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = OnSurface
                                                )
                                                Text(
                                                    text = option.zone,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = OnSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedNeighborhood = option
                                            neighborhoodError = null
                                            neighborhoodExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. Dirección (Calle y altura en el barrio)
                item {
                    StitchValidatedInputField(
                        icon = Icons.Default.Home,
                        label = "Calle y altura aproximada",
                        badgeText = "Solo para calcular distancia",
                        value = address,
                        onValueChange = {
                            address = it
                            addressError = null
                            onClearAuthError()
                        },
                        placeholder = "Ej. Av. Pedro Goyena 1420",
                        isError = addressError != null,
                        supportingText = addressError,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }

            // 9. Correo Electrónico (Patterns.EMAIL_ADDRESS)
            item {
                StitchValidatedInputField(
                    icon = Icons.Default.Mail,
                    label = if (isLoginMode) "Correo electrónico o teléfono" else "Correo electrónico",
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                        onClearAuthError()
                    },
                    placeholder = if (isLoginMode) "Ej. carlos@correo.com o 11 5544-3322" else "Ej. carlos@correo.com",
                    isError = emailError != null,
                    supportingText = emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
            }

            // 10. Contraseña (>= 6 caracteres con ojito mostrar/ocultar)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Contraseña",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            onClearAuthError()
                        },
                        placeholder = { Text("Al menos 6 números o letras") },
                        singleLine = true,
                        isError = passwordError != null,
                        supportingText = if (passwordError != null) {
                            {
                                Text(
                                    text = passwordError!!,
                                    color = ErrorColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else null,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = OnSurfaceVariant
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLowest,
                            unfocusedContainerColor = SurfaceContainerLowest,
                            focusedBorderColor = PrimaryContainer,
                            unfocusedBorderColor = OutlineVariant,
                            errorBorderColor = ErrorColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    )
                }
            }

            // 11. Sello de Privacidad y Protección + Aceptación de Términos de Convivencia Vecinal (AND-2.4)
            if (!isLoginMode) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            color = SecondaryFixed.copy(alpha = 0.30f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SecondaryFixedDim.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Secondary,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .padding(top = 2.dp)
                                )
                                Text(
                                    text = "Tus datos están protegidos. Nunca te enviaremos publicidad molesta ni compartiremos tu teléfono sin tu permiso.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurface
                                )
                            }
                        }

                        // Bloque interactivo de Términos de Convivencia Vecinal y Política de Privacidad
                        Surface(
                            color = if (termsError != null) ErrorContainer.copy(alpha = 0.45f) else SurfaceContainerLowest,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = if (termsError != null) ErrorColor else OutlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = acceptedTerms,
                                        onCheckedChange = {
                                            acceptedTerms = it
                                            termsError = null
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = PrimaryContainer,
                                            uncheckedColor = OnSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = "Al registrarme, acepto las normas de respeto y seguridad de la comunidad:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    TextButton(
                                        onClick = { legalDialogTopic = "terms" },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Términos de Convivencia Vecinal",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryContainer,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    }
                                    TextButton(
                                        onClick = { legalDialogTopic = "privacy" },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Política de Privacidad",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryContainer,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    }
                                }

                                if (termsError != null) {
                                    Text(
                                        text = termsError!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorColor,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 12. Error de API inline (si el correo ya existe o falló la red)
            if (!authError.isNullOrBlank()) {
                item {
                    Surface(
                        color = ErrorContainer,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = OnErrorContainer,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = authError,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnErrorContainer
                            )
                        }
                    }
                }
            }

            // 13. Botón Primario CTA de 56dp con estado de carga
            item {
                Button(
                    onClick = {
                        onClearAuthError()
                        if (isLoginMode) {
                            var hasErr = false
                            if (email.trim().isBlank()) {
                                emailError = "Ingresá tu correo electrónico o teléfono celular."
                                hasErr = true
                            }
                            if (password.isBlank()) {
                                passwordError = "Ingresá tu contraseña."
                                hasErr = true
                            }
                            if (!hasErr) {
                                onSubmitLogin(email.trim(), password)
                            }
                        } else {
                            var hasError = false
                            val cleanName = fullName.trim()
                            val lettersCount = cleanName.count { it.isLetter() }
                            if (lettersCount < 3) {
                                fullNameError = "Ingresá tu nombre y apellido completo (al menos 3 letras)."
                                hasError = true
                            }

                            val digitsOnly = phone.filter { it.isDigit() }
                            val hasInvalidLetters = phone.any { it.isLetter() }
                            if (hasInvalidLetters || digitsOnly.length < 8 || digitsOnly.length > 15) {
                                phoneError = "Ingresá un celular argentino válido con prefijo (ej. 11 5544-3322)."
                                hasError = true
                            }

                            val cleanEmail = email.trim()
                            if (cleanEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                                emailError = "Ingresá un correo electrónico válido (ej. carlos@correo.com)."
                                hasError = true
                            }

                            val chosenNb = selectedNeighborhood
                            if (chosenNb == null) {
                                neighborhoodError = "Seleccioná tu barrio de CABA o GBA."
                                hasError = true
                            }

                            val cleanAddress = address.trim()
                            if (cleanAddress.length < 4) {
                                addressError = "Ingresá tu calle y altura aproximada (ej. Av. Rivadavia 5420)."
                                hasError = true
                            }

                            if (password.trim().length < 6) {
                                passwordError = "La contraseña debe tener al menos 6 números o letras."
                                hasError = true
                            }

                            if (!acceptedTerms) {
                                termsError = "Debés aceptar los Términos de Convivencia Vecinal y la Política de Privacidad."
                                hasError = true
                            }

                            if (!hasError && chosenNb != null) {
                                val normalizedPhone = normalizeArgentineMobilePhone(phone)
                                val combinedAddress = if (cleanAddress.lowercase().contains(chosenNb.label.substringBefore(",").lowercase())) {
                                    cleanAddress
                                } else {
                                    "$cleanAddress, ${chosenNb.label}"
                                }
                                onSubmitManual(
                                    RegisterUserInputDto(
                                        full_name = cleanName,
                                        phone = normalizedPhone,
                                        email = cleanEmail,
                                        password = password,
                                        role = role,
                                        auth_provider = "manual",
                                        neighborhood = chosenNb.label,
                                        address = cleanAddress,
                                        saved_address = combinedAddress,
                                        saved_lat = chosenNb.lat,
                                        saved_lng = chosenNb.lng
                                    )
                                )
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isLoginMode) "Ingresando..." else "Creando tu cuenta...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = if (isLoginMode) "Iniciar sesión" else "Crear mi cuenta gratis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            // 14. Pivot inferior "¿Ya tenés una cuenta? Iniciar sesión"
            item {
                Surface(
                    color = SurfaceContainerLow,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isLoginMode) "¿Todavía no tenés una cuenta?" else "¿Ya tenés una cuenta?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                isLoginMode = !isLoginMode
                                onClearAuthError()
                            }
                        ) {
                            Text(
                                text = if (isLoginMode) "Crear mi cuenta gratis" else "Iniciar sesión",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StitchRegisterRoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // AND-2.3a: Borde azul (#1E40AF), fondo #EFF6FF y tilde verde (#15803D)
    val bgColor = if (selected) Color(0xFFEFF6FF) else SurfaceContainerLowest
    val borderColor = if (selected) PrimaryContainer else OutlineVariant
    val emeraldGreen = Color(0xFF15803D)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(2.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (selected) PrimaryContainer else OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (selected) emeraldGreen else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (selected) emeraldGreen else Outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Rol seleccionado",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) Primary else OnSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun StitchValidatedInputField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    badgeText: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isError) ErrorColor else Primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
            if (!badgeText.isNullOrBlank()) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Secondary
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            isError = isError,
            supportingText = if (!supportingText.isNullOrBlank()) {
                {
                    Text(
                        text = supportingText,
                        color = if (isError) ErrorColor else Secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else null,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerLowest,
                unfocusedContainerColor = SurfaceContainerLowest,
                focusedBorderColor = PrimaryContainer,
                unfocusedBorderColor = OutlineVariant,
                errorBorderColor = ErrorColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
    }
}

@Composable
private fun LegalTermsAccessibleDialog(
    topic: String,
    onAcceptAndClose: () -> Unit,
    onDismiss: () -> Unit
) {
    val isTerms = topic == "terms"
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLowest,
        icon = {
            Icon(
                imageVector = if (isTerms) Icons.Default.Gavel else Icons.Default.Shield,
                contentDescription = null,
                tint = PrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = if (isTerms) "Términos de Convivencia Vecinal" else "Política de Privacidad y Datos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isTerms) {
                    Text(
                        text = "1. Respeto y Puntualidad Barrial: Guía de Oficios conecta a vecinos con trabajadores verificados de su propia zona. Ambas partes se comprometen a un trato cordial, puntual y respetuoso.",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = OnSurface
                    )
                    Text(
                        text = "2. Identidad y Matrículas Verificadas: Los profesionales que ofrecen servicios de gas, electricidad o plomería declaran contar con su DNI verificado y, cuando el rubro lo exige, su matrícula vigente.",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = OnSurface
                    )
                    Text(
                        text = "3. Presupuestos Claros y Sin Sorpresas: El valor de la visita técnica se informa antes de iniciar el viaje. Cualquier arreglo adicional debe ser acordado previamente con el vecino.",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = OnSurface
                    )
                } else {
                    Text(
                        text = "1. Protección de tu Teléfono y Dirección: Tu número de WhatsApp y tu calle solo se comparten con el profesional cuando vos decidís iniciar una solicitud de contacto.",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = OnSurface
                    )
                    Text(
                        text = "2. Cero Publicidad Molesta: Guía de Oficios nunca vende ni cede tus datos personales a terceros ni envía spam publicitario.",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = OnSurface
                    )
                    Text(
                        text = "3. Seguridad de Contraseñas: Todas las claves se almacenan cifradas con estándar criptográfico bcrypt y sesiones protegidas.",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = OnSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAcceptAndClose,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Aceptar y continuar",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }
    )
}
