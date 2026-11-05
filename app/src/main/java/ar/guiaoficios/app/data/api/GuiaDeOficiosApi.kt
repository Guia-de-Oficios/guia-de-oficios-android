package ar.guiaoficios.app.data.api

import ar.guiaoficios.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Cliente Retrofit completo para las 10 pantallas de Stitch en "Guía de Oficios" (Sin mocks).
 */
interface GuiaDeOficiosApi {

    // 1. Bienvenida, Registro, Login, Google Auth y Perfil de Sesión (GDO-11, GDO-12, GDO-13)
    @POST("api/v1/auth/register")
    suspend fun registerUser(
        @Body input: RegisterUserInputDto
    ): AuthResponseDto

    @POST("api/v1/auth/login")
    suspend fun loginUser(
        @Body input: LoginInputDto
    ): AuthResponseDto

    @POST("api/v1/auth/google")
    suspend fun googleAuth(
        @Body input: GoogleAuthInputDto
    ): AuthResponseDto

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(
        @Query("role") role: String? = null,
        @Query("user_id") userId: String? = null
    ): UserDto

    @PATCH("api/v1/users/me/address")
    suspend fun updateUserAddress(
        @Body input: UpdateAddressInputDto
    ): UserDto

    @GET("api/v1/geocode")
    suspend fun geocodeAddress(
        @Query("locality") locality: String = "",
        @Query("address") address: String = ""
    ): GeocodeResultDto

    // 2. Subida real Multipart de imágenes (Averías, DNI Frente/Dorso, Matrícula) (GDO-10)
    @Multipart
    @POST("api/v1/uploads")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("category") category: RequestBody
    ): UploadResponseDto

    // 3. Inicio del Cliente, Categorías y Diagnóstico "¿Qué está pasando?" (GDO-14)
    @GET("api/v1/categories")
    suspend fun getCategories(): List<CategoryDto>

    @POST("api/v1/diagnostic")
    suspend fun getDiagnosticSuggestion(
        @Body input: DiagnosticRequestDto
    ): DiagnosticSuggestionResponseDto

    // 4. Resultados de Profesionales con PostGIS (Todos, Urgencias hoy, Matriculados, Mejor valorados) (GDO-14)
    @GET("api/v1/professionals")
    suspend fun getProfessionals(
        @Query("category_id") categoryId: Int? = null,
        @Query("q") searchQuery: String? = null,
        @Query("only_urgent") onlyUrgent: Boolean? = null,
        @Query("only_licensed") onlyLicensed: Boolean? = null,
        @Query("top_rated") topRated: Boolean? = null,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null
    ): List<ProfessionalSummaryDto>

    // 5. Perfil Detallado del Profesional (GDO-15)
    @GET("api/v1/professionals/{id}")
    suspend fun getProfessionalDetail(
        @Path("id") professionalId: String
    ): ProfessionalDetailDto

    // 6. Solicitud Guiada, Historial y Confirmación de Contacto (GDO-16)
    @POST("api/v1/requests")
    suspend fun createServiceRequest(
        @Body input: CreateServiceRequestInputDto
    ): ServiceRequestDto

    @GET("api/v1/requests/my")
    suspend fun getMyClientRequests(
        @Query("client_id") clientId: String? = null
    ): List<ServiceRequestDto>

    @GET("api/v1/requests/{id}")
    suspend fun getServiceRequest(
        @Path("id") requestId: String
    ): ServiceRequestDto

    // 7. Trabajo Terminado y Calificación Transaccional (GDO-17)
    @POST("api/v1/requests/{id}/reviews")
    suspend fun submitReview(
        @Path("id") requestId: String,
        @Body input: CreateReviewInputDto
    ): ReviewDto

    // 8. Registro de Profesional (DNI y Matrícula) y Panel de Solicitudes Recibidas (GDO-18)
    @POST("api/v1/professionals/{id}/verification")
    suspend fun submitProfessionalVerification(
        @Path("id") professionalId: String,
        @Body input: OnboardingVerificationInputDto
    ): ProfessionalDetailDto

    @PATCH("api/v1/professionals/{id}/availability")
    suspend fun toggleAvailability(
        @Path("id") professionalId: String,
        @Body input: AvailabilityToggleInputDto
    ): ProfessionalDetailDto

    @GET("api/v1/professionals/{id}/dashboard")
    suspend fun getProfessionalDashboard(
        @Path("id") professionalId: String
    ): ProfessionalDashboardDto
}
