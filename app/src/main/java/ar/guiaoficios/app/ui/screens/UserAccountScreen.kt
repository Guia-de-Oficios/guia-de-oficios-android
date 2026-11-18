package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.GeocodeResultDto
import ar.guiaoficios.app.data.model.GeocodeSuggestionDto
import ar.guiaoficios.app.data.model.UpdateUserProfileInputDto
import ar.guiaoficios.app.data.model.UserDto
import ar.guiaoficios.app.data.model.normalizeArgentineMobilePhone
import ar.guiaoficios.app.ui.theme.*
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Vista de "Mi cuenta" accesible desde el BottomNavBar oficial de Stitch:
 * - Muestra una alerta destacada si el usuario ingresó con Google y aún no completó teléfono o dirección.
 * - Permite editar TODOS los campos del perfil (Nombre y apellido, Teléfono celular con normalización,
 *   Localidad con autocompletado en vivo, Dirección con sugerencias normalizadas en vivo y validación en el mapa,
 *   más Rubro/Especialidad/Radio si es Profesional) impactando en PostgreSQL + PostGIS via PUT /api/v1/users/me.
 */
@Composable
fun UserAccountScreen(
    user: UserDto,
    isLoading: Boolean = false,
    onUpdateAddress: (String) -> Unit = {},
    onUpdateProfile: (UpdateUserProfileInputDto) -> Unit = {},
    onValidateAddressGeocode: (locality: String, address: String, onResult: (GeocodeResultDto) -> Unit) -> Unit = { _, _, _ -> },
    onSuggestGeocode: (type: String, locality: String, query: String, onResult: (List<GeocodeSuggestionDto>) -> Unit) -> Unit = { _, _, _, _ -> },
    onOpenProfessionalPanel: () -> Unit,
    onLogout: () -> Unit
) {
    val initialStreet = remember(user.saved_address) {
        if (user.saved_address.isBlank() || user.saved_address.equals("Sin especificar", ignoreCase = true)) {
            ""
        } else {
            user.saved_address.substringBefore(",").trim()
        }
    }
    val initialLocality = remember(user.saved_address) {
        if (user.saved_address.contains(",")) {
            user.saved_address.substringAfter(",").trim()
        } else {
            ""
        }
    }

    var isEditing by remember(user.id, user.isProfileIncomplete) {
        mutableStateOf(user.isProfileIncomplete)
    }

    var fullName by remember(user.full_name) { mutableStateOf(user.full_name) }
    var phone by remember(user.phone) { mutableStateOf(user.phone) }
    var localityText by remember(initialLocality) { mutableStateOf(initialLocality) }
    var addressText by remember(initialStreet) { mutableStateOf(initialStreet) }

    var localitySuggestions by remember { mutableStateOf<List<GeocodeSuggestionDto>>(emptyList()) }
    var suppressLocalitySuggest by remember { mutableStateOf(false) }

    var addressSuggestions by remember { mutableStateOf<List<GeocodeSuggestionDto>>(emptyList()) }
    var suppressAddressSuggest by remember { mutableStateOf(false) }

    var verifiedGeocode by remember { mutableStateOf<GeocodeResultDto?>(null) }
    var isGeocoding by remember { mutableStateOf(false) }

    // Campos adicionales si es profesional
    var selectedCategoryId by remember { mutableStateOf(1) }
    var tradeTitle by remember { mutableStateOf("") }
    var coverageRadiusKm by remember { mutableStateOf(10) }

    // Errores de validación
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var localityError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }

    // Autocompletado de Localidad en vivo
    LaunchedEffect(localityText, isEditing) {
        if (!isEditing) return@LaunchedEffect
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

    // Autocompletado de Direcciones normalizadas en vivo
    LaunchedEffect(addressText, localityText, isEditing) {
        if (!isEditing) return@LaunchedEffect
        if (suppressAddressSuggest) {
            suppressAddressSuggest = false
            return@LaunchedEffect
        }
        val cleanAddr = addressText.trim()
        if (cleanAddr.length < 3) {
            addressSuggestions = emptyList()
            return@LaunchedEffect
        }
        delay(320)
        onSuggestGeocode("address", localityText.trim(), cleanAddr) { results ->
            addressSuggestions = results
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 36.dp)
    ) {
        // 0. Alerta destacada si faltan datos del perfil (ej. luego de registrarse con Google)
        if (user.isProfileIncomplete) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.5.dp, Color(0xFFD97706)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Completá los datos faltantes de tu perfil",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Text(
                            text = "Completá tu teléfono celular y tu dirección con autocompletado abajo para que podamos conectarte con profesionales de tu zona.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }
        }

        // 1. Tarjeta Principal de Perfil (Lectura + Modo Edición Completo)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.full_name.trim().take(1).uppercase().ifBlank { "U" },
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.full_name.ifBlank { "Usuario Registrado" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSurface
                            )
                            Surface(
                                color = if (user.role == "professional") SecondaryFixed else SurfaceContainerLow,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = if (user.role == "professional") "Perfil Profesional de Oficio" else "Vecino / Cliente Verificado",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (user.role == "professional") OnSecondaryFixedVariant else PrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = SurfaceContainer)

                    if (!isEditing) {
                        AccountDetailRow(
                            icon = Icons.Default.Person,
                            label = "Nombre y apellido",
                            value = user.full_name.ifBlank { "Sin especificar" }
                        )
                        AccountDetailRow(
                            icon = Icons.Default.Mail,
                            label = "Correo electrónico",
                            value = user.email.ifBlank { "Sin especificar" }
                        )
                        AccountDetailRow(
                            icon = Icons.Default.Call,
                            label = "Teléfono celular / WhatsApp",
                            value = user.phone.ifBlank { "Pendiente de completar" }
                        )
                        AccountDetailRow(
                            icon = Icons.Default.LocationOn,
                            label = "Dirección y localidad guardada",
                            value = user.saved_address.ifBlank { "Pendiente de completar" }
                        )

                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Editar todos los datos de mi perfil",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // MODO EDICIÓN COMPLETO (Nombre, Teléfono, Localidad con sugerencias, Dirección con sugerencias y Rubro si es profesional)
                        Text(
                            text = "Editá los datos de tu perfil",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryContainer
                        )

                        // A. Nombre y apellido
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                fullNameError = null
                            },
                            label = { Text("Nombre y apellido completo") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Primary) },
                            trailingIcon = {
                                if (fullName.isNotEmpty()) {
                                    IconButton(onClick = { fullName = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Borrar nombre")
                                    }
                                }
                            },
                            isError = fullNameError != null,
                            supportingText = fullNameError?.let { err -> { Text(err, color = ErrorColor, fontWeight = FontWeight.Bold) } },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // B. Teléfono celular / WhatsApp
                        val digitsCount = phone.count { it.isDigit() }
                        val normalizedPhonePreview = if (digitsCount >= 8) normalizeArgentineMobilePhone(phone) else null
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneError = null
                            },
                            label = { Text("Teléfono celular (WhatsApp)") },
                            placeholder = { Text("Ej. 261 554-3322 o 11 5544-3322") },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = Primary) },
                            trailingIcon = {
                                if (phone.isNotEmpty()) {
                                    IconButton(onClick = { phone = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Borrar teléfono")
                                    }
                                }
                            },
                            isError = phoneError != null,
                            supportingText = {
                                if (phoneError != null) {
                                    Text(phoneError!!, color = ErrorColor, fontWeight = FontWeight.Bold)
                                } else if (normalizedPhonePreview != null) {
                                    Text("Se guardará como: $normalizedPhonePreview", color = OnSurfaceVariant)
                                } else {
                                    Text("Con código de área para coordinar por WhatsApp.", color = OnSurfaceVariant)
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // C. Localidad / Ciudad y Provincia con sugerencias en vivo
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = localityText,
                                onValueChange = {
                                    localityText = it
                                    localityError = null
                                    verifiedGeocode = null
                                },
                                label = { Text("Localidad / Ciudad y Provincia") },
                                placeholder = { Text("Ej. Palmira, Mendoza o Luján, Buenos Aires") },
                                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = Primary) },
                                trailingIcon = {
                                    if (localityText.isNotEmpty()) {
                                        IconButton(onClick = {
                                            localityText = ""
                                            localitySuggestions = emptyList()
                                            verifiedGeocode = null
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Borrar localidad")
                                        }
                                    }
                                },
                                isError = localityError != null,
                                supportingText = {
                                    if (localityError != null) {
                                        Text(localityError!!, color = ErrorColor, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Escribí al menos 2 letras para ver sugerencias de localidades.", color = OnSurfaceVariant)
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (localitySuggestions.isNotEmpty()) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                                    border = BorderStroke(1.5.dp, PrimaryContainer.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        localitySuggestions.forEachIndexed { idx, sug ->
                                            Surface(
                                                onClick = {
                                                    suppressLocalitySuggest = true
                                                    localityText = sug.label
                                                    localitySuggestions = emptyList()
                                                    localityError = null
                                                    verifiedGeocode = null
                                                },
                                                color = Color.Transparent,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                                                            text = sug.label,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = OnSurface
                                                        )
                                                        if (sug.subtitle.isNotBlank()) {
                                                            Text(
                                                                text = sug.subtitle,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = OnSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            if (idx < localitySuggestions.lastIndex) {
                                                HorizontalDivider(color = SurfaceContainer)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // D. Dirección (Calle y altura) con sugerencias normalizadas en vivo
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = addressText,
                                onValueChange = {
                                    addressText = it
                                    addressError = null
                                    verifiedGeocode = null
                                },
                                label = { Text("Calle y altura") },
                                placeholder = { Text("Ej. Rivadavia 221 o San Martín 1250") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Primary) },
                                trailingIcon = {
                                    if (addressText.isNotEmpty()) {
                                        IconButton(onClick = {
                                            addressText = ""
                                            addressSuggestions = emptyList()
                                            verifiedGeocode = null
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Borrar dirección")
                                        }
                                    }
                                },
                                isError = addressError != null,
                                supportingText = {
                                    if (addressError != null) {
                                        Text(addressError!!, color = ErrorColor, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Te sugerimos la dirección normalizada mientras escribís.", color = OnSurfaceVariant)
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (addressSuggestions.isNotEmpty()) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                                    border = BorderStroke(1.5.dp, PrimaryContainer.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        addressSuggestions.forEachIndexed { idx, sug ->
                                            Surface(
                                                onClick = {
                                                    suppressAddressSuggest = true
                                                    addressText = sug.street_address.ifBlank {
                                                        sug.label.substringBefore(",").trim()
                                                    }
                                                    if (sug.locality.isNotBlank()) {
                                                        suppressLocalitySuggest = true
                                                        localityText = sug.locality
                                                        localitySuggestions = emptyList()
                                                    }
                                                    addressSuggestions = emptyList()
                                                    addressError = null
                                                    localityError = null
                                                    verifiedGeocode = GeocodeResultDto(
                                                        formatted_address = sug.label,
                                                        neighborhood = sug.locality.ifBlank { localityText },
                                                        locality = sug.locality,
                                                        province = sug.province,
                                                        lat = sug.lat,
                                                        lng = sug.lng,
                                                        source = sug.source,
                                                        verified = true
                                                    )
                                                },
                                                color = Color.Transparent,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                                                            text = sug.label,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = OnSurface
                                                        )
                                                        if (sug.subtitle.isNotBlank()) {
                                                            Text(
                                                                text = "Dirección normalizada · ${sug.subtitle}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = OnSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            if (idx < addressSuggestions.lastIndex) {
                                                HorizontalDivider(color = SurfaceContainer)
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val cleanLoc = localityText.trim()
                                    val cleanAddr = addressText.trim()
                                    if (cleanLoc.length < 2) {
                                        localityError = "Ingresá tu localidad y provincia."
                                    } else if (cleanAddr.length < 3) {
                                        addressError = "Ingresá tu calle y altura."
                                    } else {
                                        isGeocoding = true
                                        addressSuggestions = emptyList()
                                        localitySuggestions = emptyList()
                                        onValidateAddressGeocode(cleanLoc, cleanAddr) { res ->
                                            isGeocoding = false
                                            verifiedGeocode = res
                                            if (res.neighborhood.isNotBlank()) {
                                                suppressLocalitySuggest = true
                                                localityText = res.neighborhood
                                            }
                                        }
                                    }
                                },
                                enabled = !isGeocoding && !isLoading,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, PrimaryContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = PrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isGeocoding) "Validando en el mapa..." else "Validar dirección en el mapa",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryContainer
                                )
                            }

                            verifiedGeocode?.let { geo ->
                                Surface(
                                    color = Color(0xFFF0FDF4),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF15803D).copy(alpha = 0.45f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Validada: ${geo.formatted_address}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF14532D)
                                            )
                                            Text(
                                                text = String.format(Locale.US, "Coord: %.4f, %.4f", geo.lat, geo.lng),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF166534)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // E. Campos adicionales de Profesional si user.role == "professional"
                        if (user.role == "professional") {
                            OutlinedTextField(
                                value = tradeTitle,
                                onValueChange = { tradeTitle = it },
                                label = { Text("Título o especialidad profesional (opcional)") },
                                placeholder = { Text("Ej. Gasista Matriculado y Plomería") },
                                leadingIcon = { Icon(Icons.Default.WorkOutline, contentDescription = null, tint = Primary) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!user.isProfileIncomplete) {
                                OutlinedButton(
                                    onClick = { isEditing = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancelar", fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    var hasErr = false
                                    if (fullName.trim().count { it.isLetter() } < 3) {
                                        fullNameError = "Ingresá tu nombre y apellido (mínimo 3 letras)."
                                        hasErr = true
                                    }
                                    val digits = phone.filter { it.isDigit() }
                                    if (digits.length < 8) {
                                        phoneError = "Ingresá un teléfono celular válido con código de área."
                                        hasErr = true
                                    }
                                    if (localityText.trim().length < 2) {
                                        localityError = "Ingresá tu localidad y provincia."
                                        hasErr = true
                                    }
                                    if (addressText.trim().length < 3) {
                                        addressError = "Ingresá tu calle y altura."
                                        hasErr = true
                                    }
                                    if (!hasErr) {
                                        val normPhone = normalizeArgentineMobilePhone(phone)
                                        val geo = verifiedGeocode
                                        val finalLoc = geo?.neighborhood?.ifBlank { localityText.trim() } ?: localityText.trim()
                                        val combinedAddr = geo?.formatted_address?.ifBlank {
                                            "${addressText.trim()}, $finalLoc"
                                        } ?: "${addressText.trim()}, $finalLoc"
                                        onUpdateProfile(
                                            UpdateUserProfileInputDto(
                                                full_name = fullName.trim(),
                                                phone = normPhone,
                                                neighborhood = finalLoc,
                                                address = addressText.trim(),
                                                saved_address = combinedAddr,
                                                saved_lat = geo?.lat ?: 0.0,
                                                saved_lng = geo?.lng ?: 0.0,
                                                primary_category_id = if (user.role == "professional") selectedCategoryId else 0,
                                                trade_title = tradeTitle.trim(),
                                                coverage_radius_km = if (user.role == "professional") coverageRadiusKm else 0
                                            )
                                        )
                                        isEditing = false
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isLoading) "Guardando..." else "Guardar perfil",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        if (user.role == "professional") {
            item {
                Button(
                    onClick = onOpenProfessionalPanel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.Handyman, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Abrir mi Panel de Profesional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, ErrorColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ErrorContainer.copy(alpha = 0.25f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = ErrorColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Cerrar sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ErrorColor
                )
            }
        }
    }
}

@Composable
private fun AccountDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface
            )
        }
    }
}
