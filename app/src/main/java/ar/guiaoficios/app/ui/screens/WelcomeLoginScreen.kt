package ar.guiaoficios.app.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
// VISTA #2 STITCH (`[AND-1.4]`): bienvenida_y_selecci_n_de_rol
// - Botones táctiles de 56dp ('Continuar con Google', 'Ingresar con correo o teléfono')
// - BottomSheet / Vista dedicada de Login (reemplaza al AlertDialog que se cerraba solo):
//   (a) Campo Identificador (Email o Celular) de 56dp con KeyboardOptions
//   (b) Campo Contraseña de 56dp con ojito mostrar/ocultar
//   (c) Mensaje de error inline en rojo dentro del formulario sin borrar lo escrito
//   (d) Botón 'Ingresar' con spinner CircularProgressIndicator mientras isLoading == true
//   (e) Flujo '¿Olvidaste tu contraseña?' integrado
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeRoleScreen(
    initialRole: String = "client",
    isLoading: Boolean = false,
    authError: String? = null,
    onClearAuthError: () -> Unit = {},
    onGoogleSignIn: (email: String, fullName: String, role: String) -> Unit,
    onLoginWithCredentials: (identifier: String, password: String) -> Unit,
    onNavigateToRegister: (role: String) -> Unit,
    onRequestPasswordRecovery: (identifier: String) -> Unit = {},
    onBackToRoleSelection: (() -> Unit)? = null
) {
    var selectedRole by remember { mutableStateOf(initialRole) }
    var showLoginSheet by remember { mutableStateOf(false) }
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
                onGoogleSignIn(accountName, derivedName.ifBlank { "Usuario Google" }, selectedRole)
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
            role = selectedRole,
            onDismiss = { showGoogleDialog = false },
            onConfirmAccount = { email, fullName, role ->
                showGoogleDialog = false
                onGoogleSignIn(email, fullName, role)
            }
        )
    }

    if (showLoginSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                if (!isLoading) {
                    showLoginSheet = false
                    onClearAuthError()
                }
            },
            sheetState = sheetState,
            containerColor = SurfaceContainerLowest,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            DedicatedLoginSheetContent(
                isLoading = isLoading,
                authError = authError,
                onClearAuthError = onClearAuthError,
                onSubmitLogin = { identifier, password ->
                    // NO cerramos la hoja aquí: esperamos la respuesta real de la API.
                    // Si hay error, se muestra inline en rojo conservando lo que el usuario escribió.
                    onLoginWithCredentials(identifier, password)
                },
                onGoToRegister = {
                    showLoginSheet = false
                    onClearAuthError()
                    onNavigateToRegister(selectedRole)
                },
                onRequestPasswordRecovery = onRequestPasswordRecovery,
                onClose = {
                    if (!isLoading) {
                        showLoginSheet = false
                        onClearAuthError()
                    }
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Pill Superior + Botón Volver opcional + Logo Oficial GDO + Título Principal
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onBackToRoleSelection != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(onClick = onBackToRoleSelection) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cambiar rol",
                                tint = PrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Volver a elegir rol",
                                color = PrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

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
                    text = "Ingresá a tu cuenta para coordinar trabajos seguros con vecinos y profesionales de tu barrio.",
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
                // Botón 1: Continuar con Google (56dp, Blanco, borde 2dp, ícono 4 colores)
                Surface(
                    onClick = launchGoogleFlow,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
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
                        GoogleColorIcon(modifier = Modifier.size(24.dp))
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
                        text = "o ingresá con tu cuenta",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = OutlineVariant
                    )
                }

                // Botón 2: Ingresar con correo o teléfono (56dp, #1E40AF primario)
                Button(
                    onClick = {
                        onClearAuthError()
                        showLoginSheet = true
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
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

        // 3. Tarjeta de Conversión para Usuario Nuevo (Exacta a `bienvenida_y_selecci_n_de_rol/code.html`)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant),
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
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
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
                                fontWeight = FontWeight.Bold,
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

                    Text(
                        text = "Registrate en 1 minuto como vecino o como profesional",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 4. Sello de Confianza Barrial (Verde Esmeralda Stitch)
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
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
                        tint = Secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Red vecinal protegida y verificada · Sin costos ocultos",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSecondaryFixedVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// =============================================================================
// HOJA DEDICADA DE LOGIN ACCESIBLE (`[AND-1.4]`):
// - Campos de 56dp con KeyboardOptions (Email/Phone + ImeAction.Next/Done)
// - Visibilidad de contraseña con ojito
// - Error inline en rojo sin cerrar la hoja ni borrar lo escrito
// - Botón Ingresar con spinner CircularProgressIndicator mientras isLoading == true
// - Flujo '¿Olvidaste tu contraseña?' integrado
// =============================================================================
@Composable
private fun DedicatedLoginSheetContent(
    isLoading: Boolean,
    authError: String?,
    onClearAuthError: () -> Unit,
    onSubmitLogin: (identifier: String, password: String) -> Unit,
    onGoToRegister: () -> Unit,
    onRequestPasswordRecovery: (identifier: String) -> Unit,
    onClose: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var identifierError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isRecoveryMode by remember { mutableStateOf(false) }
    var recoverySentMessage by remember { mutableStateOf<String?>(null) }

    val validateAndSubmit: () -> Unit = {
        val cleanId = identifier.trim()
        var valid = true
        if (cleanId.isBlank()) {
            identifierError = "Ingresá tu correo electrónico o tu número de celular"
            valid = false
        }
        if (password.isBlank()) {
            passwordError = "Ingresá tu contraseña para continuar"
            valid = false
        }
        if (valid && !isLoading) {
            onSubmitLogin(cleanId, password)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isRecoveryMode) {
            // Flujo '¿Olvidaste tu contraseña?'
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = PrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Recuperar tu contraseña",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )
                    Text(
                        text = "Ingresá tu correo o teléfono celular registrado y te ayudamos a restablecerla.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = identifier,
                onValueChange = {
                    identifier = it
                    identifierError = null
                    recoverySentMessage = null
                },
                label = { Text("Correo electrónico o celular") },
                placeholder = { Text("Ej. maria.castro@vecinos.ar o 11 5544-3322") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Mail,
                        contentDescription = null,
                        tint = Primary
                    )
                },
                isError = identifierError != null,
                supportingText = identifierError?.let { err ->
                    { Text(err, color = ErrorColor, fontWeight = FontWeight.SemiBold) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            )

            if (recoverySentMessage != null) {
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Secondary
                        )
                        Text(
                            text = recoverySentMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSecondaryFixedVariant
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val clean = identifier.trim()
                    if (clean.length < 5) {
                        identifierError = "Ingresá un correo o teléfono válido para recuperar tu clave"
                    } else {
                        recoverySentMessage = "Enviamos las instrucciones a $clean. También podés pedir asistencia telefónica al 0800-444-OFICIO."
                        onRequestPasswordRecovery(clean)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
            ) {
                Text(
                    text = "Enviar instrucciones de recuperación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            TextButton(
                onClick = {
                    isRecoveryMode = false
                    recoverySentMessage = null
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "← Volver a ingresar con mi contraseña",
                    color = PrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Vista Principal de Login
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Ingresar a tu cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
                )
                Text(
                    text = "Usá tu correo electrónico o tu número de celular registrado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            // (a) Campo Identificador (Email o Celular) de 56dp con KeyboardOptions
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Correo electrónico o teléfono celular",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                OutlinedTextField(
                    value = identifier,
                    onValueChange = {
                        identifier = it
                        identifierError = null
                        if (authError != null) onClearAuthError()
                    },
                    placeholder = { Text("Ej. maria.castro@vecinos.ar o 11 5544-3322") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    isError = identifierError != null || authError != null,
                    supportingText = identifierError?.let { err ->
                        { Text(err, color = ErrorColor, fontWeight = FontWeight.SemiBold) }
                    },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
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

            // (b) Campo Contraseña de 56dp con ojito mostrar/ocultar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    // (e) Botón '¿Olvidaste tu contraseña?'
                    TextButton(
                        onClick = {
                            isRecoveryMode = true
                            identifierError = null
                            passwordError = null
                            onClearAuthError()
                        },
                        enabled = !isLoading,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                        if (authError != null) onClearAuthError()
                    },
                    placeholder = { Text("Escribí tu contraseña") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = OnSurfaceVariant
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = passwordError != null || authError != null,
                    supportingText = passwordError?.let { err ->
                        { Text(err, color = ErrorColor, fontWeight = FontWeight.SemiBold) }
                    },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { validateAndSubmit() }
                    ),
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

            // (c) Mensaje de error inline en rojo dentro del formulario (sin borrar lo que el usuario escribió)
            if (!authError.isNullOrBlank()) {
                Surface(
                    color = ErrorContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, ErrorColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error de inicio de sesión",
                            tint = ErrorColor,
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

            // (d) Botón 'Ingresar' de 56dp con spinner CircularProgressIndicator mientras isLoading == true
            Button(
                onClick = validateAndSubmit,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Verificando credenciales...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Ingresar",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClose,
                    enabled = !isLoading
                ) {
                    Text("Cancelar", color = OnSurfaceVariant, fontWeight = FontWeight.SemiBold)
                }

                TextButton(
                    onClick = onGoToRegister,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "¿No tenés cuenta? Registrate gratis",
                        color = PrimaryContainer,
                        fontWeight = FontWeight.Bold
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
