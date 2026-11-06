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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkOutline
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
import ar.guiaoficios.app.data.model.ARGENTINA_SUGGESTED_LOCALITIES
import ar.guiaoficios.app.data.model.GeocodeResultDto
import ar.guiaoficios.app.data.model.GeocodeSuggestionDto
import ar.guiaoficios.app.data.model.NeighborhoodOption
import ar.guiaoficios.app.data.model.RegisterUserInputDto
import ar.guiaoficios.app.data.model.normalizeArgentineMobilePhone
import ar.guiaoficios.app.ui.components.GdoBrandLogo
import ar.guiaoficios.app.ui.components.GoogleColorIcon
import ar.guiaoficios.app.ui.theme.*
import java.util.Locale
import kotlinx.coroutines.delay

val PROFESSIONAL_TRADE_CATEGORIES = listOf(
    1 to "Plomería",
    2 to "Gasista Matriculado",
    3 to "Electricidad",
    4 to "Aire Acondicionado",
    5 to "Albañilería y Construcción",
    6 to "Pintura e Impermeabilización",
    7 to "Cerrajería",
    8 to "Jardinería y Poda"
)

// =============================================================================
// VISTA #3 STITCH (`[AND-2.3]` & `[AND-2.4]`): registro_google_o_manual
// - Muestra un formulario diferenciado según el rol elegido obligatoriamente en
//   la pantalla de Bienvenida ('client' vs 'professional'), sin repetir el selector.
// - Sugerencias de autocompletado en vivo (sin robo de foco del teclado) tanto en
//   Localidad/Ciudad como en Dirección normalizada (Calle y altura).
// - Continuar con Google captura solo Nombre, Apellido y Correo para luego pedir
//   completar los datos faltantes desde Inicio / Mi Cuenta.
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAccountScreen(
    initialRole: String = "client",
    initialLoginMode: Boolean = false,
    isLoading: Boolean = false,
    authError: String? = null,
    onClearAuthError: () -> Unit = {},
    onValidateAddressGeocode: (locality: String, address: String, onResult: (GeocodeResultDto) -> Unit) -> Unit = { _, _, _ -> },
    onSuggestGeocode: (type: String, locality: String, query: String, onResult: (List<GeocodeSuggestionDto>) -> Unit) -> Unit = { _, _, _, _ -> },
    onContinueGoogle: (email: String, fullName: String, role: String, neighborhood: String, address: String) -> Unit,
    onSubmitManual: (RegisterUserInputDto) -> Unit,
    onSubmitLogin: (emailOrPhone: String, password: String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(initialLoginMode) }
    val role = initialRole
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Campos de Localidad y Dirección con sugerencias normalizadas en vivo (sin robo de foco)
    var localityText by remember { mutableStateOf("") }
    var localitySuggestions by remember { mutableStateOf<List<GeocodeSuggestionDto>>(emptyList()) }
    var suppressLocalitySuggest by remember { mutableStateOf(false) }

    var address by remember { mutableStateOf("") }
    var addressSuggestions by remember { mutableStateOf<List<GeocodeSuggestionDto>>(emptyList()) }
    var suppressAddressSuggest by remember { mutableStateOf(false) }

    var verifiedGeocode by remember { mutableStateOf<GeocodeResultDto?>(null) }
    var isGeocoding by remember { mutableStateOf(false) }

    // Campos exclusivos para el formulario de Profesional
    var selectedCategoryId by remember { mutableStateOf(1) }
    var tradeTitle by remember { mutableStateOf("") }
    var coverageRadiusKm by remember { mutableStateOf(10) }
    var tradeTitleError by remember { mutableStateOf<String?>(null) }

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

    // Autocompletado debounced de Localidades (300ms)
    LaunchedEffect(localityText) {
        if (suppressLocalitySuggest) {
            suppressLocalitySuggest = false
            return@LaunchedEffect
        }
        val clean = localityText.trim()
        if (clean.length < 2) {
            localitySuggestions = emptyList()
            return@LaunchedEffect
        }
        delay(280)
        onSuggestGeocode("locality", "", clean) { results ->
            localitySuggestions = results
        }
    }

    // Autocompletado debounced de Direcciones normalizadas (320ms)
    LaunchedEffect(address, localityText) {
        if (suppressAddressSuggest) {
            suppressAddressSuggest = false
            return@LaunchedEffect
        }
        val cleanAddr = address.trim()
        if (cleanAddr.length < 3) {
            addressSuggestions = emptyList()
            return@LaunchedEffect
        }
        delay(320)
        onSuggestGeocode("address", localityText.trim(), cleanAddr) { results ->
            addressSuggestions = results
        }
    }

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
                // Bug 4: Con Google solo tomamos Nombre y Correo; luego el usuario completa su dirección/teléfono en su perfil
                onContinueGoogle(
                    accountName,
                    derivedName.ifBlank { "Usuario Google" },
                    role,
                    "",
                    ""
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
            // 1. Encabezado según el rol seleccionado en la pantalla de Bienvenida (sin repetir selector de rol)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (role == "professional") SecondaryFixed.copy(alpha = 0.45f) else Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(
                                1.dp,
                                if (role == "professional") Secondary else PrimaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (role == "professional") Icons.Default.Handyman else Icons.Default.PersonSearch,
                                    contentDescription = null,
                                    tint = if (role == "professional") Secondary else PrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (role == "professional") "Registro de Profesional de Oficio" else "Registro de Vecino / Cliente",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (role == "professional") Secondary else PrimaryContainer
                                )
                            }
                        }

                        TextButton(
                            onClick = onBack,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Cambiar rol",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryContainer,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }

                    Text(
                        text = when {
                            isLoginMode -> "Ingresá a tu cuenta de Guía de Oficios"
                            role == "professional" -> "Creá tu perfil de Profesional"
                            else -> "Creá tu cuenta de Vecino"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )
                    Text(
                        text = when {
                            isLoginMode -> "Ingresá con tu cuenta de Google o con tu correo/teléfono y contraseña."
                            role == "professional" -> "Registrá tu oficio, zona de cobertura y teléfono comercial para recibir solicitudes directas por WhatsApp."
                            else -> "Conectate con profesionales verificados cerca de tu domicilio en pocos minutos."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            // 2. Botón rápido: Continuar con Google (Obtiene Nombre, Apellido y Correo; el resto se completa luego en el perfil)
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
                    Text(
                        text = "Con Google tomamos tu nombre y correo rápido; luego podrás completar tu dirección y teléfono desde tu perfil.",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
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

            // 3. Divisor
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                    Text(
                        text = if (isLoginMode) "O ingresá con tus datos" else "O completá el formulario manual",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                }
            }

            if (!isLoginMode) {
                // 4. Nombre y Apellido Completo (>= 3 letras)
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

                // 5. Teléfono Celular Argentino con normalización +54 9 ...
                item {
                    val digitsCount = phone.count { it.isDigit() }
                    val normalizedPreview = if (digitsCount >= 8) normalizeArgentineMobilePhone(phone) else null
                    StitchValidatedInputField(
                        icon = Icons.Default.Call,
                        label = if (role == "professional") "Teléfono celular comercial (WhatsApp)" else "Teléfono celular con prefijo",
                        badgeText = if (role == "professional") "Recibirás pedidos aquí" else "Para coordinar por WhatsApp",
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = null
                            onClearAuthError()
                        },
                        placeholder = "Ej. 261 554-3322 o 11 5544-3322",
                        isError = phoneError != null,
                        supportingText = phoneError ?: normalizedPreview?.let { "Se guardará normalizado como: $it" },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                // 6. Campos Exclusivos para Profesionales (Rubro principal, Especialidad y Radio de cobertura)
                if (role == "professional") {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handyman,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Tu rubro u oficio principal",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            }

                            // Grilla de chips de los 8 rubros
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PROFESSIONAL_TRADE_CATEGORIES.chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { (catId, catLabel) ->
                                            val isSelected = selectedCategoryId == catId
                                            Surface(
                                                onClick = {
                                                    selectedCategoryId = catId
                                                    if (tradeTitle.isBlank()) {
                                                        tradeTitle = catLabel
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) Color(0xFFEFF6FF) else SurfaceContainerLowest,
                                                border = BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) PrimaryContainer else OutlineVariant
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = catLabel,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                        color = if (isSelected) PrimaryContainer else OnSurface,
                                                        maxLines = 1
                                                    )
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Color(0xFF15803D),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            StitchValidatedInputField(
                                icon = Icons.Default.WorkOutline,
                                label = "Título o especialidad de tu servicio",
                                badgeText = "Visible en tu perfil",
                                value = tradeTitle,
                                onValueChange = {
                                    tradeTitle = it
                                    tradeTitleError = null
                                },
                                placeholder = "Ej. Gasista Matriculado y Plomería en General",
                                isError = tradeTitleError != null,
                                supportingText = tradeTitleError ?: "Describí brevemente tu especialidad principal.",
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Radio máximo de cobertura desde tu base:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(5, 10, 15, 25).forEach { km ->
                                        val selectedKm = coverageRadiusKm == km
                                        Surface(
                                            onClick = { coverageRadiusKm = km },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selectedKm) Color(0xFFEFF6FF) else SurfaceContainerLowest,
                                            border = BorderStroke(
                                                if (selectedKm) 2.dp else 1.dp,
                                                if (selectedKm) PrimaryContainer else OutlineVariant
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "$km km",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selectedKm) PrimaryContainer else OnSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. Localidad, Ciudad o Barrio (Sin robo de foco al borrar + Sugerencias en vivo de toda Argentina)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    text = "Localidad / Ciudad y Provincia",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            }
                            Text(
                                text = "Autocompletado",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryContainer
                            )
                        }

                        // Atajos rápidos para las zonas prioritarias de lanzamiento
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Gran Mendoza" to "Ciudad de Mendoza, Mendoza",
                                "Palmira (Mza)" to "Palmira, San Martín, Mendoza",
                                "Luján (Bs. As.)" to "Luján, Buenos Aires"
                            ).forEach { (chipLabel, chipValue) ->
                                val isSelected = localityText.equals(chipValue, ignoreCase = true)
                                Surface(
                                    onClick = {
                                        suppressLocalitySuggest = true
                                        localityText = chipValue
                                        localitySuggestions = emptyList()
                                        neighborhoodError = null
                                        verifiedGeocode = null
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSelected) Color(0xFFEFF6FF) else SurfaceContainer,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) PrimaryContainer else OutlineVariant
                                    )
                                ) {
                                    Text(
                                        text = chipLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PrimaryContainer else OnSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = localityText,
                            onValueChange = { newValue ->
                                localityText = newValue
                                neighborhoodError = null
                                verifiedGeocode = null
                                onClearAuthError()
                            },
                            singleLine = true,
                            placeholder = { Text("Escribí tu ciudad (ej. Palmira, Godoy Cruz, Luján...)") },
                            trailingIcon = {
                                if (localityText.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            localityText = ""
                                            localitySuggestions = emptyList()
                                            verifiedGeocode = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Borrar localidad",
                                            tint = OnSurfaceVariant
                                        )
                                    }
                                }
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
                                        text = "Escribí al menos 2 letras para ver sugerencias oficiales de localidades.",
                                        color = OnSurfaceVariant
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
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

                        // Lista inline de sugerencias de Localidades (NO roba foco del teclado)
                        if (localitySuggestions.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                                border = BorderStroke(1.5.dp, PrimaryContainer.copy(alpha = 0.4f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    localitySuggestions.forEachIndexed { index, suggestion ->
                                        Surface(
                                            onClick = {
                                                suppressLocalitySuggest = true
                                                localityText = suggestion.label
                                                localitySuggestions = emptyList()
                                                neighborhoodError = null
                                                verifiedGeocode = null
                                            },
                                            color = Color.Transparent,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationCity,
                                                    contentDescription = null,
                                                    tint = PrimaryContainer,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = suggestion.label,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = OnSurface
                                                    )
                                                    if (suggestion.subtitle.isNotBlank()) {
                                                        Text(
                                                            text = suggestion.subtitle,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = OnSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (index < localitySuggestions.lastIndex) {
                                            HorizontalDivider(color = SurfaceContainer)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 8. Dirección (Calle y altura) con Sugerencias Normalizadas en Vivo + Validación en el Mapa
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Calle y altura aproximada",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            }
                            Text(
                                text = "Sugerencias normalizadas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryContainer
                            )
                        }

                        OutlinedTextField(
                            value = address,
                            onValueChange = { newAddr ->
                                address = newAddr
                                addressError = null
                                verifiedGeocode = null
                                onClearAuthError()
                            },
                            singleLine = true,
                            placeholder = { Text("Ej. Rivadavia 221 o San Martín 1250") },
                            trailingIcon = {
                                if (address.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            address = ""
                                            addressSuggestions = emptyList()
                                            verifiedGeocode = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Borrar dirección",
                                            tint = OnSurfaceVariant
                                        )
                                    }
                                }
                            },
                            isError = addressError != null,
                            supportingText = {
                                if (addressError != null) {
                                    Text(
                                        text = addressError!!,
                                        color = ErrorColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Text(
                                        text = "A medida que escribís tu calle y número te sugerimos la dirección normalizada.",
                                        color = OnSurfaceVariant
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
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

                        // Lista inline de sugerencias de Direcciones Normalizadas (Georef AR + Nominatim OSM)
                        if (addressSuggestions.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                                border = BorderStroke(1.5.dp, PrimaryContainer.copy(alpha = 0.4f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    addressSuggestions.forEachIndexed { index, suggestion ->
                                        Surface(
                                            onClick = {
                                                suppressAddressSuggest = true
                                                address = suggestion.street_address.ifBlank {
                                                    suggestion.label.substringBefore(",").trim()
                                                }
                                                if (suggestion.locality.isNotBlank()) {
                                                    suppressLocalitySuggest = true
                                                    localityText = suggestion.locality
                                                    localitySuggestions = emptyList()
                                                }
                                                addressSuggestions = emptyList()
                                                addressError = null
                                                neighborhoodError = null
                                                verifiedGeocode = GeocodeResultDto(
                                                    formatted_address = suggestion.label,
                                                    neighborhood = suggestion.locality.ifBlank { localityText },
                                                    locality = suggestion.locality,
                                                    province = suggestion.province,
                                                    lat = suggestion.lat,
                                                    lng = suggestion.lng,
                                                    source = suggestion.source,
                                                    verified = true
                                                )
                                            },
                                            color = Color.Transparent,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = Color(0xFF15803D),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = suggestion.label,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = OnSurface
                                                    )
                                                    if (suggestion.subtitle.isNotBlank()) {
                                                        Text(
                                                            text = "Dirección normalizada · ${suggestion.subtitle}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = OnSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (index < addressSuggestions.lastIndex) {
                                            HorizontalDivider(color = SurfaceContainer)
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val cleanLoc = localityText.trim()
                                val cleanAddr = address.trim()
                                if (cleanLoc.length < 3) {
                                    neighborhoodError = "Ingresá tu localidad y provincia (ej. Palmira, Mendoza o Luján, Buenos Aires)."
                                } else if (cleanAddr.length < 3) {
                                    addressError = "Ingresá tu calle y altura para validar en el mapa."
                                } else {
                                    isGeocoding = true
                                    addressSuggestions = emptyList()
                                    localitySuggestions = emptyList()
                                    onValidateAddressGeocode(cleanLoc, cleanAddr) { result ->
                                        isGeocoding = false
                                        verifiedGeocode = result
                                        if (result.neighborhood.isNotBlank()) {
                                            suppressLocalitySuggest = true
                                            localityText = result.neighborhood
                                        }
                                    }
                                }
                            },
                            enabled = !isGeocoding && !isLoading,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, PrimaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            if (isGeocoding) {
                                CircularProgressIndicator(
                                    color = PrimaryContainer,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Validando ubicación en el mapa...",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryContainer
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = PrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Validar dirección en el mapa",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryContainer
                                )
                            }
                        }

                        verifiedGeocode?.let { geo ->
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF15803D).copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Ubicación verificada",
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Ubicación validada: ${geo.formatted_address}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF14532D)
                                        )
                                        Text(
                                            text = String.format(
                                                Locale.US,
                                                "Zona: %s · Coord: %.4f, %.4f",
                                                geo.neighborhood.ifBlank { localityText },
                                                geo.lat,
                                                geo.lng
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }
                            }
                        }
                    }
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

                            val cleanLocality = localityText.trim()
                            if (cleanLocality.length < 3) {
                                neighborhoodError = "Ingresá tu localidad y provincia (ej. Luján, Buenos Aires o Godoy Cruz, Mendoza)."
                                hasError = true
                            }

                            val cleanAddress = address.trim()
                            if (cleanAddress.length < 4) {
                                addressError = "Ingresá tu calle y altura aproximada (ej. San Martín 1250)."
                                hasError = true
                            }

                            if (role == "professional" && tradeTitle.trim().length < 3) {
                                tradeTitleError = "Ingresá el título o especialidad de tu oficio (ej. Gasista Matriculado)."
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

                            if (!hasError) {
                                val normalizedPhone = normalizeArgentineMobilePhone(phone)
                                val geo = verifiedGeocode
                                val finalNeighborhood = geo?.neighborhood?.ifBlank { cleanLocality } ?: cleanLocality
                                val combinedAddress = geo?.formatted_address?.ifBlank {
                                    "$cleanAddress, $finalNeighborhood"
                                } ?: if (cleanAddress.lowercase().contains(finalNeighborhood.substringBefore(",").lowercase())) {
                                    cleanAddress
                                } else {
                                    "$cleanAddress, $finalNeighborhood"
                                }
                                onSubmitManual(
                                    RegisterUserInputDto(
                                        full_name = cleanName,
                                        phone = normalizedPhone,
                                        email = cleanEmail,
                                        password = password,
                                        role = role,
                                        auth_provider = "manual",
                                        neighborhood = finalNeighborhood,
                                        address = cleanAddress,
                                        saved_address = combinedAddress,
                                        saved_lat = geo?.lat ?: 0.0,
                                        saved_lng = geo?.lng ?: 0.0,
                                        primary_category_id = selectedCategoryId,
                                        trade_title = tradeTitle.trim(),
                                        coverage_radius_km = coverageRadiusKm
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
