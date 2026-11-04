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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
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
// PANTALLA 2 STITCH: bienvenida_y_selecci_n_de_rol
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

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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
