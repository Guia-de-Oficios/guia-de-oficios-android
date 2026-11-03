package ar.guiaoficios.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.guiaoficios.app.data.api.GuiaDeOficiosApi
import ar.guiaoficios.app.data.api.RetrofitClient
import ar.guiaoficios.app.data.api.SessionManager
import ar.guiaoficios.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GuiaUiState(
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val currentUser: UserDto = UserDto(
        id = "11111111-1111-1111-1111-111111111111",
        full_name = "María Elena Castro",
        email = "maria.elena@correo.com",
        phone = "11 5544-3322",
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
        observeSavedSession()
        refreshInitialData()
    }

    private fun observeSavedSession() {
        viewModelScope.launch {
            sessionManager.tokenFlow.collect {
                // Mantiene cachedToken sincronizado con el AuthInterceptor
            }
        }
        viewModelScope.launch {
            sessionManager.userFlow.collect { savedUser ->
                if (savedUser != null) {
                    _uiState.update { it.copy(currentUser = savedUser) }
                }
            }
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
                        statusMessage = "Error de conexión con la API (${e.localizedMessage}). Verificá que el servidor Go + PostgreSQL esté encendido."
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun registerManual(input: RegisterUserInputDto, onSuccess: (UserDto) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            try {
                val authRes = api.registerUser(input)
                sessionManager.saveAuthSession(authRes.token, authRes.user)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = authRes.user,
                        statusMessage = "¡Cuenta creada con éxito, ${authRes.user.full_name.substringBefore(" ")}!"
                    )
                }
                onSuccess(authRes.user)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "No se pudo registrar la cuenta: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun loginWithEmail(email: String, password: String, onSuccess: (UserDto) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            try {
                val authRes = api.loginUser(LoginInputDto(email = email, password = password))
                sessionManager.saveAuthSession(authRes.token, authRes.user)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = authRes.user,
                        statusMessage = "¡Bienvenido/a nuevamente, ${authRes.user.full_name.substringBefore(" ")}!"
                    )
                }
                onSuccess(authRes.user)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "Credenciales inválidas o error de conexión: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun authenticateWithGoogle(role: String, onSuccess: (UserDto) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            try {
                val isPro = role == "professional"
                val googleInput = GoogleAuthInputDto(
                    id_token = "google-oauth-token-${System.currentTimeMillis()}",
                    email = if (isPro) "roberto.gomez@oficios.com" else "maria.elena@correo.com",
                    full_name = if (isPro) "Roberto Carlos Gómez" else "María Elena Castro",
                    phone = if (isPro) "11 4567-8901" else "11 5544-3322",
                    role = role
                )
                val authRes = api.googleAuth(googleInput)
                sessionManager.saveAuthSession(authRes.token, authRes.user)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = authRes.user,
                        statusMessage = "Sesión iniciada con Google (${authRes.user.email})"
                    )
                }
                onSuccess(authRes.user)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "Error en autenticación con Google: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun updateAddress(newAddress: String) {
        viewModelScope.launch {
            try {
                val updatedUser = api.updateUserAddress(
                    UpdateAddressInputDto(
                        saved_address = newAddress,
                        saved_lat = -34.6186,
                        saved_lng = -58.4368
                    )
                )
                sessionManager.updateSavedUser(updatedUser)
                _uiState.update {
                    it.copy(
                        currentUser = updatedUser,
                        statusMessage = "Dirección actualizada en PostgreSQL: $newAddress"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(statusMessage = "No se pudo actualizar la dirección: ${e.localizedMessage}")
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
                    it.copy(statusMessage = "No se pudo consultar el diagnóstico: ${e.localizedMessage}")
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
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        professionals = pros
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = "Error buscando profesionales: ${e.localizedMessage}"
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
                        statusMessage = "¡Gracias por tu calificación! El puntaje de Roberto se actualizó en PostgreSQL."
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
                        statusMessage = "Documentación DNI y Matrícula guardada en PostgreSQL."
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
