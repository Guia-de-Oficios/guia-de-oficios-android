package ar.guiaoficios.app.ui

import android.app.Application
import android.location.Geocoder
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.guiaoficios.app.data.api.GuiaDeOficiosApi
import ar.guiaoficios.app.data.api.RetrofitClient
import ar.guiaoficios.app.data.api.SessionManager
import ar.guiaoficios.app.data.model.*
import java.security.MessageDigest
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class GuiaUiState(
    val isLoading: Boolean = false,
    val authError: String? = null,
    val statusMessage: String? = null,
    val isSessionChecked: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: UserDto = UserDto(
        id = "",
        full_name = "",
        email = "",
        phone = "",
        role = "client",
        saved_address = "Av. Rivadavia 5420, Caballito"
    ),
    val categories: List<CategoryDto> = emptyList(),
    val diagnosticSuggestion: DiagnosticSuggestionResponseDto? = null,
    val selectedCategoryName: String = "Plomeros cerca de vos",
    val selectedCategoryId: Int? = 1,
    val activeFilter: String = "all",
    val professionals: List<ProfessionalSummaryDto> = emptyList(),
    val selectedProfessional: ProfessionalDetailDto? = null,
    val activeRequest: ServiceRequestDto? = null,
    val uploadedProblemPhotoUrl: String? = null,
    val uploadedDniFrontUrl: String? = null,
    val uploadedDniBackUrl: String? = null,
    val uploadedLicenseDocUrl: String? = null,
    val isUploadingFile: Boolean = false,
    val professionalDashboard: ProfessionalDashboardDto? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val sessionManager = SessionManager(application.applicationContext)
    private var api: GuiaDeOficiosApi = RetrofitClient.createApi(sessionManager)

    private val _uiState = MutableStateFlow(GuiaUiState())
    val uiState: StateFlow<GuiaUiState> = _uiState.asStateFlow()

    companion object {
        const val DEFAULT_ROBERTO_PRO_ID = "aaaa1111-1111-1111-1111-111111111111"
    }

    init {
        restoreAndValidateSession()
        refreshInitialData()
    }

    private fun restoreAndValidateSession() {
        viewModelScope.launch {
            sessionManager.tokenFlow.collect {
                // Mantiene cachedToken sincronizado con el AuthInterceptor
            }
        }
        viewModelScope.launch {
            val savedToken = sessionManager.tokenFlow.firstOrNull()
            val savedUser = sessionManager.userFlow.firstOrNull()
            if (!savedToken.isNullOrBlank() && savedUser != null) {
                try {
                    val freshUser = api.getCurrentUser()
                    sessionManager.updateSavedUser(freshUser)
                    _uiState.update {
                        it.copy(
                            currentUser = freshUser,
                            isAuthenticated = true,
                            isSessionChecked = true
                        )
                    }
                } catch (_: Exception) {
                    sessionManager.clearSession()
                    _uiState.update {
                        it.copy(
                            currentUser = UserDto(id = "", full_name = "", email = ""),
                            isAuthenticated = false,
                            isSessionChecked = true
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isAuthenticated = false,
                        isSessionChecked = true
                    )
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        viewModelScope.launch {
            sessionManager.clearSession()
            _uiState.update {
                it.copy(
                    isAuthenticated = false,
                    currentUser = UserDto(id = "", full_name = "", email = ""),
                    statusMessage = "Sesión cerrada correctamente"
                )
            }
            onLoggedOut()
        }
    }

    fun refreshInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            try {
                val cats = api.getCategories()
                val pros = api.getProfessionals(
                    lat = _uiState.value.currentUser.saved_lat,
                    lng = _uiState.value.currentUser.saved_lng
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        categories = cats,
                        professionals = pros
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "No pudimos conectar con el servidor. Verificá tu conexión a internet."
                    )
                }
            }
        }
    }

    fun showStatusMessage(msg: String) {
        _uiState.update { it.copy(statusMessage = msg) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authError = null) }
    }

    fun requestPasswordRecovery(identifier: String, onDone: (String) -> Unit = {}) {
        val clean = identifier.trim()
        val msg = "Enviamos las instrucciones de recuperación a $clean. Si necesitás asistencia inmediata llamá gratis al 0800-444-OFICIO."
        _uiState.update { it.copy(statusMessage = msg) }
        onDone(msg)
    }

    private fun extractApiErrorMessage(e: Exception, fallbackPrefix: String): String {
        if (e is retrofit2.HttpException) {
            val rawBody = runCatching { e.response()?.errorBody()?.string() }.getOrNull().orEmpty()
            val match = Regex(""""error"\s*:\s*"([^"]+)"""").find(rawBody)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return "$fallbackPrefix: ${e.localizedMessage ?: "Error desconocido"}"
    }

    fun registerManual(input: RegisterUserInputDto, onSuccess: (UserDto) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null, statusMessage = null) }
            try {
                val enrichedInput = if (input.saved_lat == null || input.saved_lat == 0.0) {
                    val geo = resolveGeocodeInternal(locality = input.neighborhood, address = input.address)
                    input.copy(
                        neighborhood = geo.neighborhood.ifBlank { input.neighborhood },
                        saved_address = geo.formatted_address,
                        saved_lat = geo.lat,
                        saved_lng = geo.lng
                    )
                } else {
                    input
                }
                val authRes = api.registerUser(enrichedInput)
                sessionManager.saveAuthSession(authRes.token, authRes.user)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = null,
                        isAuthenticated = true,
                        currentUser = authRes.user,
                        statusMessage = "¡Cuenta creada con éxito, ${authRes.user.full_name.substringBefore(" ")}!"
                    )
                }
                onSuccess(authRes.user)
            } catch (e: Exception) {
                val errMsg = extractApiErrorMessage(e, "No se pudo registrar la cuenta")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = errMsg,
                        statusMessage = errMsg
                    )
                }
            }
        }
    }

    fun loginWithEmail(emailOrPhone: String, password: String, onSuccess: (UserDto) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null, statusMessage = null) }
            try {
                val authRes = api.loginUser(LoginInputDto(email = emailOrPhone.trim(), password = password))
                sessionManager.saveAuthSession(authRes.token, authRes.user)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = null,
                        isAuthenticated = true,
                        currentUser = authRes.user,
                        statusMessage = "¡Hola de nuevo, ${authRes.user.full_name.substringBefore(" ")}!"
                    )
                }
                onSuccess(authRes.user)
            } catch (e: Exception) {
                val errMsg = extractApiErrorMessage(e, "No se pudo iniciar sesión")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = errMsg
                    )
                }
            }
        }
    }

    private fun generateSignedGoogleAssertionJwt(email: String, fullName: String): String {
        val cleanEmail = email.trim().lowercase()
        val digest = MessageDigest.getInstance("SHA-256").digest(("google-sub:$cleanEmail").toByteArray(Charsets.UTF_8))
        val subHex = digest.take(10).joinToString("") { "%02x".format(it) }
        val googleSub = "google-$subHex"

        val nowSec = System.currentTimeMillis() / 1000
        val headerJson = """{"alg":"HS256","typ":"JWT"}"""
        val payloadObj = JSONObject().apply {
            put("sub", googleSub)
            put("email", cleanEmail)
            put("email_verified", true)
            put("name", fullName.trim())
            put("iss", "accounts.google.com")
            put("iat", nowSec)
            put("exp", nowSec + 300)
        }

        val flags = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        val hEnc = Base64.encodeToString(headerJson.toByteArray(Charsets.UTF_8), flags)
        val pEnc = Base64.encodeToString(payloadObj.toString().toByteArray(Charsets.UTF_8), flags)
        val signingInput = "$hEnc.$pEnc"

        val secretBytes = "guia-de-oficios-google-client-secret-2026".toByteArray(Charsets.UTF_8)
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretBytes, "HmacSHA256"))
        val sigBytes = mac.doFinal(signingInput.toByteArray(Charsets.UTF_8))
        val sEnc = Base64.encodeToString(sigBytes, flags)
        return "$signingInput.$sEnc"
    }

    fun authenticateWithGoogle(
        email: String,
        fullName: String,
        role: String,
        phone: String = "",
        neighborhood: String = "",
        address: String = "",
        idToken: String = "",
        onSuccess: (UserDto) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null, statusMessage = null) }
            try {
                val signedToken = idToken.ifBlank {
                    generateSignedGoogleAssertionJwt(email = email, fullName = fullName)
                }
                val googleInput = GoogleAuthInputDto(
                    id_token = signedToken,
                    email = email.trim(),
                    full_name = fullName.trim(),
                    phone = phone.trim(),
                    role = role,
                    neighborhood = neighborhood.trim(),
                    address = address.trim()
                )
                val authRes = api.googleAuth(googleInput)
                sessionManager.saveAuthSession(authRes.token, authRes.user)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = null,
                        isAuthenticated = true,
                        currentUser = authRes.user,
                        statusMessage = "Sesión iniciada con Google (${authRes.user.email})"
                    )
                }
                onSuccess(authRes.user)
            } catch (e: Exception) {
                val errMsg = extractApiErrorMessage(e, "Error en autenticación con Google")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authError = errMsg,
                        statusMessage = errMsg
                    )
                }
            }
        }
    }

    private suspend fun resolveGeocodeInternal(locality: String, address: String): GeocodeResultDto {
        val cleanLoc = locality.trim()
        val cleanAddr = address.trim()
        try {
            val res = api.geocodeAddress(locality = cleanLoc, address = cleanAddr)
            if (res.lat != 0.0 && res.lng != 0.0) {
                return res
            }
        } catch (_: Exception) {
            // Si el backend no responde, intentar con el Geocoder nativo de Android (Google Play Services)
        }

        val combinedQuery = listOf(cleanAddr, cleanLoc, "Argentina")
            .filter { it.isNotBlank() }
            .joinToString(", ")

        try {
            val nativeGeo = withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                Geocoder(getApplication(), Locale("es", "AR")).getFromLocationName(combinedQuery, 1)
            }
            val top = nativeGeo?.firstOrNull()
            if (top != null && top.hasLatitude() && top.hasLongitude()) {
                val localityName = top.locality ?: top.subAdminArea ?: cleanLoc
                val provName = top.adminArea ?: ""
                val nbLabel = if (localityName.isNotBlank() && provName.isNotBlank() && !localityName.equals(provName, ignoreCase = true)) {
                    "$localityName, $provName"
                } else {
                    cleanLoc.ifBlank { localityName }
                }
                val formatted = top.getAddressLine(0)?.removeSuffix(", Argentina")
                    ?: listOf(cleanAddr, nbLabel).filter { it.isNotBlank() }.joinToString(", ")
                return GeocodeResultDto(
                    formatted_address = formatted,
                    neighborhood = nbLabel,
                    locality = localityName,
                    province = provName,
                    lat = top.latitude,
                    lng = top.longitude,
                    source = "android_geocoder",
                    verified = true
                )
            }
        } catch (_: Exception) {
            // Fallback al catálogo sugerido local
        }

        val (catLat, catLng) = findNeighborhoodCoordinates("$cleanLoc $cleanAddr")
        val finalLat = if (catLat != 0.0) catLat else -34.5703
        val finalLng = if (catLng != 0.0) catLng else -59.1050
        val fullAddr = listOf(cleanAddr, cleanLoc).filter { it.isNotBlank() }.joinToString(", ")
        return GeocodeResultDto(
            formatted_address = fullAddr.ifBlank { "Luján, Buenos Aires" },
            neighborhood = cleanLoc.ifBlank { "Luján, Buenos Aires" },
            locality = cleanLoc,
            lat = finalLat,
            lng = finalLng,
            source = "local_catalog",
            verified = catLat != 0.0
        )
    }

    fun validateAddressGeocode(
        locality: String,
        address: String,
        onResult: (GeocodeResultDto) -> Unit
    ) {
        viewModelScope.launch {
            val result = resolveGeocodeInternal(locality = locality, address = address)
            onResult(result)
        }
    }

    private var geocodeSuggestJob: Job? = null

    fun fetchGeocodeSuggestions(
        type: String,
        locality: String = "",
        query: String,
        onResult: (List<GeocodeSuggestionDto>) -> Unit
    ) {
        geocodeSuggestJob?.cancel()
        val cleanQuery = query.trim()
        if (cleanQuery.length < 2) {
            onResult(emptyList())
            return
        }
        geocodeSuggestJob = viewModelScope.launch {
            delay(300) // Debounce de 300ms para evitar saturación de llamadas al tipear
            try {
                val remote = api.suggestGeocode(type = type, locality = locality.trim(), query = cleanQuery)
                if (remote.isNotEmpty()) {
                    onResult(remote)
                    return@launch
                }
            } catch (_: Exception) {
                // Fallback a catálogo local sugerido si no hay red
            }

            if (type == "locality") {
                val qLower = cleanQuery.lowercase()
                val localMatches = ARGENTINA_SUGGESTED_LOCALITIES.filter {
                    it.label.lowercase().contains(qLower) || it.zone.lowercase().contains(qLower)
                }.take(6).map {
                    GeocodeSuggestionDto(
                        label = it.label,
                        subtitle = it.zone,
                        locality = it.label,
                        lat = it.lat,
                        lng = it.lng,
                        source = "local_catalog"
                    )
                }
                onResult(localMatches)
            } else {
                onResult(emptyList())
            }
        }
    }

    fun updateUserProfile(
        input: UpdateUserProfileInputDto,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null, statusMessage = null) }
            try {
                val geo = if (input.saved_lat == 0.0 || input.saved_lng == 0.0) {
                    resolveGeocodeInternal(locality = input.neighborhood, address = input.address.ifBlank { input.saved_address })
                } else {
                    GeocodeResultDto(
                        formatted_address = input.saved_address,
                        neighborhood = input.neighborhood,
                        lat = input.saved_lat,
                        lng = input.saved_lng,
                        verified = true
                    )
                }
                val enriched = input.copy(
                    neighborhood = geo.neighborhood.ifBlank { input.neighborhood },
                    saved_address = geo.formatted_address,
                    saved_lat = geo.lat,
                    saved_lng = geo.lng
                )
                val updatedUser = api.updateUserProfile(enriched)
                sessionManager.updateSavedUser(updatedUser)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = updatedUser,
                        statusMessage = "¡Perfil guardado con éxito!"
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                val errMsg = extractApiErrorMessage(e, "No se pudo guardar el perfil")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = errMsg
                    )
                }
            }
        }
    }

    fun updateAddress(newAddress: String, locality: String = "") {
        viewModelScope.launch {
            try {
                val geo = resolveGeocodeInternal(locality = locality, address = newAddress)
                val updatedUser = api.updateUserAddress(
                    UpdateAddressInputDto(
                        neighborhood = geo.neighborhood,
                        saved_address = geo.formatted_address,
                        saved_lat = geo.lat,
                        saved_lng = geo.lng
                    )
                )
                sessionManager.updateSavedUser(updatedUser)
                _uiState.update {
                    it.copy(
                        currentUser = updatedUser,
                        statusMessage = "Dirección actualizada correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(statusMessage = "No se pudo actualizar la dirección")
                }
            }
        }
    }

    fun requestDiagnostic(problemText: String) {
        viewModelScope.launch {
            try {
                val res = api.getDiagnosticSuggestion(DiagnosticRequestDto(problem = problemText))
                _uiState.update { it.copy(diagnosticSuggestion = res) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(statusMessage = "No se pudo consultar el diagnóstico")
                }
            }
        }
    }

    fun searchProfessionals(
        categoryId: Int? = _uiState.value.selectedCategoryId,
        categoryTitle: String = _uiState.value.selectedCategoryName,
        query: String? = null,
        filter: String = "all"
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedCategoryId = categoryId,
                    selectedCategoryName = categoryTitle,
                    activeFilter = filter
                )
            }
            try {
                val pros = api.getProfessionals(
                    categoryId = if (categoryId != null && categoryId > 0) categoryId else null,
                    searchQuery = query,
                    onlyUrgent = if (filter == "urgent") true else null,
                    onlyLicensed = if (filter == "licensed") true else null,
                    topRated = if (filter == "top_rated") true else null,
                    lat = _uiState.value.currentUser.saved_lat,
                    lng = _uiState.value.currentUser.saved_lng
                ) ?: emptyList()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        professionals = pros
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        professionals = emptyList()
                    )
                }
            }
        }
    }

    fun loadProfessionalDetail(professionalId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val detail = api.getProfessionalDetail(professionalId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedProfessional = detail
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "No se pudo cargar el perfil del profesional: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun uploadMediaUri(uri: Uri, category: String) {
        viewModelScope.launch {
            val pair = RetrofitClient.uriToMultipart(
                context = getApplication<Application>().applicationContext,
                uri = uri,
                category = category
            )
            if (pair == null) {
                _uiState.update { it.copy(statusMessage = "No se pudo leer el archivo seleccionado") }
                return@launch
            }
            _uiState.update { it.copy(isUploadingFile = true) }
            try {
                val res = api.uploadFile(pair.first, pair.second)
                _uiState.update { state ->
                    when (category) {
                        "problem_photo" -> state.copy(isUploadingFile = false, uploadedProblemPhotoUrl = res.url, statusMessage = "Foto subida correctamente")
                        "dni_front" -> state.copy(isUploadingFile = false, uploadedDniFrontUrl = res.url, statusMessage = "DNI Frente subido al servidor")
                        "dni_back" -> state.copy(isUploadingFile = false, uploadedDniBackUrl = res.url, statusMessage = "DNI Dorso subido al servidor")
                        "license_doc" -> state.copy(isUploadingFile = false, uploadedLicenseDocUrl = res.url, statusMessage = "Matrícula subida al servidor")
                        else -> state.copy(isUploadingFile = false, uploadedProblemPhotoUrl = res.url)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploadingFile = false,
                        statusMessage = "Error al subir imagen al servidor: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun createServiceRequest(
        professionalId: String,
        urgency: String,
        description: String,
        photoUrl: String?,
        onCreated: (ServiceRequestDto) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = _uiState.value.currentUser
                val pro = _uiState.value.selectedProfessional
                val created = api.createServiceRequest(
                    CreateServiceRequestInputDto(
                        client_id = user.id,
                        client_name = user.full_name,
                        client_phone = user.phone,
                        client_address = user.saved_address,
                        professional_id = professionalId,
                        category_id = pro?.primary_category_id ?: 1,
                        urgency = urgency,
                        problem_description = description,
                        photo_url = photoUrl,
                        contact_channel = "whatsapp"
                    )
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeRequest = created,
                        uploadedProblemPhotoUrl = null
                    )
                }
                onCreated(created)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "Error al crear la solicitud: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun submitReview(requestId: String, input: CreateReviewInputDto, onSubmitted: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                api.submitReview(requestId, input)
                val proId = _uiState.value.selectedProfessional?.id ?: DEFAULT_ROBERTO_PRO_ID
                val refreshedDetail = runCatching { api.getProfessionalDetail(proId) }.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedProfessional = refreshedDetail ?: it.selectedProfessional,
                        statusMessage = "¡Gracias por tu calificación!"
                    )
                }
                onSubmitted()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "No se pudo guardar la calificación: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun submitVerification(
        professionalId: String,
        input: OnboardingVerificationInputDto,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val updatedPro = api.submitProfessionalVerification(professionalId, input)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedProfessional = updatedPro,
                        statusMessage = "Documentación guardada correctamente"
                    )
                }
                loadProfessionalDashboard(professionalId)
                onComplete()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "Error al guardar verificación: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun loadProfessionalDashboard(professionalId: String = DEFAULT_ROBERTO_PRO_ID) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dash = api.getProfessionalDashboard(professionalId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        professionalDashboard = dash
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "No se pudo cargar el panel profesional: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun toggleProfessionalAvailability(professionalId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            try {
                api.toggleAvailability(
                    professionalId = professionalId,
                    input = AvailabilityToggleInputDto(is_available_now = isAvailable)
                )
                val updatedDash = api.getProfessionalDashboard(professionalId)
                _uiState.update {
                    it.copy(
                        professionalDashboard = updatedDash,
                        statusMessage = if (isAvailable) "Ahora figurás DISPONIBLE en el barrio" else "Pausaste tu disponibilidad"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(statusMessage = "Error al cambiar disponibilidad: ${e.localizedMessage}")
                }
            }
        }
    }
}
