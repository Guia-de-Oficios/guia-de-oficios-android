package ar.guiaoficios.app.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Handyman
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.RegisterUserInputDto
import ar.guiaoficios.app.ui.components.GdoBrandLogo
import ar.guiaoficios.app.ui.components.GoogleColorIcon
import ar.guiaoficios.app.ui.theme.*

// =============================================================================
// PANTALLA 3 STITCH: registro_google_o_manual
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
