package ar.guiaoficios.app.data.api

import ar.guiaoficios.app.data.model.*
import retrofit2.http.*

/**
 * Cliente Retrofit completo para las 10 pantallas de Stitch en "Guía de Oficios".
 */
interface GuiaDeOficiosApi {

    // 1. Bienvenida y Registro (Google o Manual)
    @POST("api/v1/auth/register")
    suspend fun registerUser(
        @Body input: RegisterUserInputDto
    ): UserDto

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(
        @Query("role") role: String? = null
    ): UserDto

    // 2. Inicio del Cliente y Categorías
    @GET("api/v1/categories")
    suspend fun getCategories(): List<CategoryDto>

    // 3. Resultados de Profesionales (Todos, Urgencias hoy, Matriculados, Mejor valorados)
    @GET("api/v1/professionals")
    suspend fun getProfessionals(
        @Query("category_id") categoryId: Int? = null,
        @Query("q") searchQuery: String? = null,
        @Query("only_urgent") onlyUrgent: Boolean? = null,
        @Query("only_licensed") onlyLicensed: Boolean? = null,
        @Query("top_rated") topRated: Boolean? = null
    ): List<ProfessionalSummaryDto>

    // 4. Perfil Detallado del Profesional
    @GET("api/v1/professionals/{id}")
    suspend fun getProfessionalDetail(
        @Path("id") professionalId: String
    ): ProfessionalDetailDto

    // 5. Solicitud Guiada y Confirmación de Contacto
    @POST("api/v1/requests")
    suspend fun createServiceRequest(
        @Body input: CreateServiceRequestInputDto
    ): ServiceRequestDto

    @GET("api/v1/requests/{id}")
    suspend fun getServiceRequest(
        @Path("id") requestId: String
    ): ServiceRequestDto

    // 6. Trabajo Terminado y Calificación
    @POST("api/v1/requests/{id}/reviews")
    suspend fun submitReview(
        @Path("id") requestId: String,
        @Body input: CreateReviewInputDto
    ): ReviewDto

    // 7. Registro de Profesional (DNI y Matrícula) y Panel de Solicitudes Recibidas
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
