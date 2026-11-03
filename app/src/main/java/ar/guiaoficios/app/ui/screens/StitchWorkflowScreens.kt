package ar.guiaoficios.app.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.guiaoficios.app.data.model.*
import ar.guiaoficios.app.ui.components.GdoBrandLogo
import ar.guiaoficios.app.ui.components.GoogleColorIcon
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// COMPONENTE COMPARTIDO: Diálogo de Cuenta Google (cuando el dispositivo no devuelve cuenta nativa)
// =============================================================================
@Composable
fun GoogleAccountInputDialog(
    role: String,
    onDismiss: () -> Unit,
    onConfirmAccount: (email: String, fullName: String, selectedRole: String) -> Unit
) {
    var googleEmail by remember { mutableStateOf("") }
    var googleName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(role) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLowest,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GoogleColorIcon(modifier = Modifier.size(24.dp))
                Text(
                    text = "Continuar con Google",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Ingresá tu cuenta de Google para acceder o crear tu perfil en Guía de Oficios:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                OutlinedTextField(
                    value = googleEmail,
                    onValueChange = {
                        googleEmail = it
                        errorMsg = null
                    },
                    label = { Text("Correo de Google (Gmail)") },
                    placeholder = { Text("tu.nombre@gmail.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = googleName,
                    onValueChange = {
                        googleName = it
                        errorMsg = null
                    },
                    label = { Text("Tu nombre y apellido") },
                    placeholder = { Text("Ej. Nicolás Gómez") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Tipo de perfil:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRole == "client",
                        onClick = { selectedRole = "client" },
                        label = { Text("Vecino / Cliente") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedRole == "professional",
                        onClick = { selectedRole = "professional" },
                        label = { Text("Profesional") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanEmail = googleEmail.trim()
                    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                        errorMsg = "Ingresá un correo electrónico válido (ej. usuario@gmail.com)"
                        return@Button
                    }
                    val cleanName = googleName.trim().ifBlank {
                        cleanEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                    }
                    onConfirmAccount(cleanEmail, cleanName, selectedRole)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ingresar con Google", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = OnSurfaceVariant)
            }
        }
    )
}

// =============================================================================
// PANTALLA 1 STITCH: bienvenida_y_selecci_n_de_rol
// Respeta 1:1 `bienvenida_y_selecci_n_de_rol/code.html` + `screen.png`
// =============================================================================
@Composable
fun WelcomeRoleScreen(
    onGoogleSignIn: (email: String, fullName: String, role: String) -> Unit,
    onLoginWithCredentials: (identifier: String, password: String) -> Unit,
    onNavigateToRegister: (role: String) -> Unit
) {
    var selectedRole by remember { mutableStateOf("client") }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    // Selector nativo de cuenta Google de Android (AccountManager)
    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) {
                val derivedName = accountName.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                onGoogleSignIn(accountName, derivedName.ifBlank { "Usuario Google" }, selectedRole)
            } else {
                showGoogleDialog = true
            }
        } else {
            // Si el dispositivo no tiene cuentas de Google sincronizadas o el usuario prefiere escribirla
            showGoogleDialog = true
        }
    }

    val launchGoogleFlow: () -> Unit = {
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
            showGoogleDialog = true
        }
    }

    if (showGoogleDialog) {
        GoogleAccountInputDialog(
            role = selectedRole,
            onDismiss = { showGoogleDialog = false },
            onConfirmAccount = { email, fullName, role ->
                showGoogleDialog = false
                onGoogleSignIn(email, fullName, role)
            }
        )
    }

    if (showLoginDialog) {
        LoginCredentialsDialog(
            onDismiss = { showLoginDialog = false },
            onSubmitLogin = { identifier, password ->
                showLoginDialog = false
                onLoginWithCredentials(identifier, password)
            },
            onGoToRegister = {
                showLoginDialog = false
                onNavigateToRegister(selectedRole)
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Pill Superior + Logo Oficial GDO + Título Principal
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = Color(0xFFE4E7F9),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, PrimaryFixedDim)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Buenos Aires y Gran Buenos Aires",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                GdoBrandLogo(centered = true, badgeSize = 56.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Encontrá y ofrecé oficios de confianza cerca de tu casa",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Conectate con vecinos y profesionales verificados de tu barrio. Rápido, directo y sin vueltas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 2. Bloque de Ingreso Principal (Exacto a `bienvenida_y_selecci_n_de_rol/code.html`)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Botón 1: Continuar con Google (Blanco, borde 2dp #C4C5D9, ícono 4 colores)
                Surface(
                    onClick = launchGoogleFlow,
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

                // Divisor "o ingresá con tu cuenta"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = OutlineVariant
                    )
                    Text(
                        text = "O INGRESÁ CON TU CUENTA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = OutlineVariant
                    )
                }

                // Botón 2: Ingresar con correo o teléfono (#1E40AF primario)
                Button(
                    onClick = { showLoginDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mail,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Ingresar con correo o teléfono",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 3. Tarjeta de Registro Nueva Cuenta (con preselección de rol Vecino / Profesional)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(2.dp, SurfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "¿No tenés una cuenta todavía?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Creá tu perfil en 1 minuto como vecino o profesional.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // Selector rápido de rol antes de ir al registro
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StitchRoleChoicePill(
                            title = "Vecino",
                            subtitle = "Busco oficios",
                            icon = Icons.Default.PersonSearch,
                            selected = selectedRole == "client",
                            onClick = { selectedRole = "client" },
                            modifier = Modifier.weight(1f)
                        )
                        StitchRoleChoicePill(
                            title = "Profesional",
                            subtitle = "Ofrezco oficio",
                            icon = Icons.Default.Handyman,
                            selected = selectedRole == "professional",
                            onClick = { selectedRole = "professional" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(
                        onClick = { onNavigateToRegister(selectedRole) },
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(2.dp, PrimaryFixedDim),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Registrarme gratis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = PrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Sello de Confianza Barrial (Verde Esmeralda Stitch)
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F8ED),
                border = BorderStroke(1.dp, SecondaryFixed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = SecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Red vecinal protegida y verificada · Sin costos ocultos",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSecondaryFixedVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StitchRoleChoicePill(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) SurfaceContainerLow else Surface
    val borderColor = if (selected) PrimaryContainer else OutlineVariant
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) PrimaryContainer else OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Primary else OnSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

// =============================================================================
// DIÁLOGO DE INICIO DE SESIÓN CON CORREO O TELÉFONO
// =============================================================================
@Composable
fun LoginCredentialsDialog(
    onDismiss: () -> Unit,
    onSubmitLogin: (identifier: String, password: String) -> Unit,
    onGoToRegister: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLowest,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Ingresar a tu cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
                )
                Text(
                    text = "Usá tu correo electrónico o teléfono celular registrado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Correo electrónico o teléfono",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            localError = null
                        },
                        placeholder = { Text("Ej. carlos@correo.com o 11 5544-3322") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Contraseña",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                        placeholder = { Text("Tu contraseña") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Ver contraseña",
                                    tint = OnSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (localError != null) {
                    Text(
                        text = localError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onGoToRegister,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "¿Todavía no tenés cuenta? Registrate gratis",
                        color = PrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (identifier.trim().isBlank()) {
                        localError = "Ingresá tu correo electrónico o número de teléfono"
                        return@Button
                    }
                    if (password.isBlank()) {
                        localError = "Ingresá tu contraseña"
                        return@Button
                    }
                    onSubmitLogin(identifier.trim(), password)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Ingresar →", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = OnSurfaceVariant)
            }
        }
    )
}

// =============================================================================
// PANTALLA 2 STITCH: registro_google_o_manual
// Respeta 1:1 `registro_google_o_manual/code.html` y `screen.png`
// =============================================================================
@Composable
fun RegisterAccountScreen(
    initialRole: String = "client",
    initialLoginMode: Boolean = false,
    onContinueGoogle: (email: String, fullName: String, role: String) -> Unit,
    onSubmitManual: (RegisterUserInputDto) -> Unit,
    onSubmitLogin: (emailOrPhone: String, password: String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(initialLoginMode) }
    var role by remember { mutableStateOf(initialRole) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) {
                val derivedName = accountName.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                onContinueGoogle(accountName, derivedName.ifBlank { "Usuario Google" }, role)
            } else {
                showGoogleDialog = true
            }
        } else {
            showGoogleDialog = true
        }
    }

    val launchGoogleFlow: () -> Unit = {
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
            showGoogleDialog = true
        }
    }

    if (showGoogleDialog) {
        GoogleAccountInputDialog(
            role = role,
            onDismiss = { showGoogleDialog = false },
            onConfirmAccount = { gEmail, gName, gRole ->
                showGoogleDialog = false
                onContinueGoogle(gEmail, gName, gRole)
            }
        )
    }

    Scaffold(
        topBar = {
            // Sticky Header exacto a `registro_google_o_manual/code.html`
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
                                contentDescription = "Volver",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Pill "Red Vecinal Segura" + Título y Subtítulo
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = SurfaceContainerLow,
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
                                text = "RED VECINAL SEGURA",
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

            // 2. Botón Continuar con Google (56dp, ícono 4 colores)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = launchGoogleFlow,
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
                    Text(
                        text = "Acceso rápido con un solo toque usando tu cuenta de Gmail",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3. Divisor "O completá tus datos a mano"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                    Text(
                        text = if (isLoginMode) "O INGRESÁ CON TUS DATOS" else "O COMPLETÁ TUS DATOS A MANO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                }
            }

            // 4. Selector de Rol de 2 Columnas exacto a `registro_google_o_manual/code.html`
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
                                title = "Busco oficios",
                                subtitle = "Cliente / Vecino",
                                icon = Icons.Default.PersonSearch,
                                selected = role == "client",
                                onClick = { role = "client" },
                                modifier = Modifier.weight(1f)
                            )
                            StitchRegisterRoleCard(
                                title = "Soy profesional",
                                subtitle = "Electricista, etc.",
                                icon = Icons.Default.Handyman,
                                selected = role == "professional",
                                onClick = { role = "professional" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Campo 1: Nombre y apellido completo
                item {
                    StitchLabeledInputField(
                        icon = Icons.Default.Person,
                        label = "Nombre y apellido completo",
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            validationError = null
                        },
                        placeholder = "Ej. Carlos Alberto Gómez"
                    )
                }

                // Campo 2: Teléfono celular con prefijo + badge verde "Para coordinar trabajos"
                item {
                    StitchLabeledInputField(
                        icon = Icons.Default.Call,
                        label = "Teléfono celular con prefijo",
                        badgeText = "Para coordinar trabajos",
                        value = phone,
                        onValueChange = {
                            phone = it
                            validationError = null
                        },
                        placeholder = "Ej. 11 5544-3322 (para WhatsApp)",
                        keyboardType = KeyboardType.Phone
                    )
                }
            }

            // Campo 3: Correo electrónico (o teléfono si está en modo login)
            item {
                StitchLabeledInputField(
                    icon = Icons.Default.Mail,
                    label = if (isLoginMode) "Correo electrónico o teléfono" else "Correo electrónico",
                    value = email,
                    onValueChange = {
                        email = it
                        validationError = null
                    },
                    placeholder = if (isLoginMode) "Ej. carlos@correo.com o 11 5544-3322" else "Ej. carlos@correo.com",
                    keyboardType = KeyboardType.Email
                )
            }

            // Campo 4: Contraseña con botón de ojo funcional
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
                            validationError = null
                        },
                        placeholder = { Text("Al menos 6 números o letras") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                            unfocusedBorderColor = OutlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    )
                }
            }

            // Mensaje de validación local si falta algún dato
            if (validationError != null) {
                item {
                    Surface(
                        color = ErrorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = validationError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnErrorContainer,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            // Caja verde de privacidad (Exacta a `registro_google_o_manual/code.html`)
            if (!isLoginMode) {
                item {
                    Surface(
                        color = SecondaryFixed.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SecondaryContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Secondary,
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(top = 2.dp)
                            )
                            Text(
                                text = "Tus datos están protegidos. Solo compartimos tu teléfono cuando vos decidís contactar a un profesional o vecino.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurface
                            )
                        }
                    }
                }
            }

            // Botón Principal 56dp: "Crear mi cuenta →" / "Iniciar sesión →"
            item {
                Button(
                    onClick = {
                        if (isLoginMode) {
                            if (email.trim().isBlank() || password.isBlank()) {
                                validationError = "Completá tu correo/teléfono y tu contraseña para ingresar."
                                return@Button
                            }
                            onSubmitLogin(email.trim(), password)
                        } else {
                            if (fullName.trim().length < 3) {
                                validationError = "Ingresá tu nombre y apellido completo."
                                return@Button
                            }
                            if (phone.trim().length < 6) {
                                validationError = "Ingresá tu teléfono celular con prefijo para coordinar trabajos."
                                return@Button
                            }
                            if (!email.trim().contains("@")) {
                                validationError = "Ingresá un correo electrónico válido (ej. nombre@correo.com)."
                                return@Button
                            }
                            if (password.trim().length < 6) {
                                validationError = "La contraseña debe tener al menos 6 números o letras."
                                return@Button
                            }
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
                        text = if (isLoginMode) "Iniciar sesión" else "Crear mi cuenta",
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

            // Footer: "¿Ya tenés una cuenta? Iniciar sesión"
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
                                validationError = null
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
    val bgColor = if (selected) SurfaceContainerLow else SurfaceContainerLowest
    val borderColor = if (selected) PrimaryContainer else OutlineVariant

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Radio circle indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (selected) PrimaryContainer else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (selected) PrimaryContainer else Outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (selected) Primary else OnSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
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
private fun StitchLabeledInputField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    badgeText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
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
                    tint = Primary,
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerLowest,
                unfocusedContainerColor = SurfaceContainerLowest,
                focusedBorderColor = PrimaryContainer,
                unfocusedBorderColor = OutlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
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
