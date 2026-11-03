package ar.guiaoficios.app.data.model

data class CategoryDto(
    val id: Int,
    val slug: String,
    val name: String,
    val helper_examples: String = "",
    val icon_name: String = "",
    val badge_text: String? = null,
    val badge_type: String? = null,
    val display_order: Int = 0
)

data class RegisterUserInputDto(
    val full_name: String,
    val phone: String,
    val email: String,
    val password: String? = null,
    val role: String,           // "client" | "professional"
    val auth_provider: String = "manual",
    val saved_address: String? = "Av. Rivadavia 5420, Caballito",
    val saved_lat: Double? = -34.6186,
    val saved_lng: Double? = -58.4368
)

data class LoginInputDto(
    val email: String,
    val password: String
)

data class GoogleAuthInputDto(
    val id_token: String,
    val email: String,
    val full_name: String,
    val phone: String = "",
    val role: String = "client"
)

data class UpdateAddressInputDto(
    val saved_address: String,
    val saved_lat: Double = -34.6186,
    val saved_lng: Double = -58.4368
)

data class UserDto(
    val id: String,
    val full_name: String,
    val email: String,
    val phone: String = "",
    val role: String = "client",
    val auth_provider: String = "manual",
    val saved_address: String = "Av. Rivadavia 5420, Caballito",
    val saved_lat: Double = -34.6186,
    val saved_lng: Double = -58.4368,
    val professional_id: String? = null
)

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)

data class UploadResponseDto(
    val url: String,
    val filename: String,
    val category: String,
    val size: Long
)

data class DiagnosticRequestDto(
    val problem: String
)

data class DiagnosticSuggestionResponseDto(
    val problem_text: String,
    val suggested_category: CategoryDto,
    val explanation: String
)

data class PortfolioItemDto(
    val id: String,
    val title: String,
    val subtitle: String,
    val image_url: String,
    val is_verified: Boolean
)

data class ReviewDto(
    val id: String,
    val author_name: String,
    val author_zone: String = "",
    val rating: Int,
    val highlights: List<String> = emptyList(),
    val comment: String
)

data class ProfessionalSummaryDto(
    val id: String,
    val user_id: String = "",
    val full_name: String,
    val phone: String = "",
    val avatar_url: String = "",
    val primary_category_id: Int = 1,
    val category_name: String = "",
    val trade_title: String,
    val specialties: List<String> = emptyList(),
    val neighborhood: String = "Caballito, CABA",
    val distance_label: String = "A 8 cuadras de tu ubicación",
    val distance_km: Double = 0.8,
    val is_available_now: Boolean = true,
    val attends_emergencies: Boolean = false,
    val is_dni_verified: Boolean = true,
    val is_licensed: Boolean = false,
    val license_number: String? = null,
    val license_issuer: String? = null,
    val visit_price_amount: Int = 15000,
    val visit_price_label: String = "$15.000",
    val rating_avg: Double = 4.9,
    val rating_label: String = "Excelente",
    val reviews_count: Int = 0,
    val jobs_completed_count: Int = 0,
    val monthly_contacts_count: Int = 0
)

data class ProfessionalDetailDto(
    val id: String,
    val user_id: String = "",
    val full_name: String,
    val phone: String = "",
    val avatar_url: String = "",
    val primary_category_id: Int = 1,
    val category_name: String = "",
    val trade_title: String,
    val specialties: List<String> = emptyList(),
    val neighborhood: String = "Caballito, CABA",
    val distance_label: String = "A 8 cuadras de tu ubicación",
    val is_available_now: Boolean = true,
    val attends_emergencies: Boolean = false,
    val is_dni_verified: Boolean = true,
    val is_licensed: Boolean = false,
    val license_number: String? = null,
    val license_issuer: String? = null,
    val visit_price_amount: Int = 15000,
    val visit_price_label: String = "$15.000",
    val rating_avg: Double = 4.9,
    val rating_label: String = "Excelente",
    val reviews_count: Int = 0,
    val jobs_completed_count: Int = 0,
    val monthly_contacts_count: Int = 0,
    val bio: String = "",
    val years_experience: Int = 10,
    val guarantee_percentage: Int = 100,
    val dni_front_url: String? = null,
    val dni_back_url: String? = null,
    val license_doc_url: String? = null,
    val portfolio: List<PortfolioItemDto> = emptyList(),
    val reviews: List<ReviewDto> = emptyList()
)

data class OnboardingVerificationInputDto(
    val primary_category_id: Int,
    val trade_title: String,
    val specialties: List<String>,
    val neighborhood: String,
    val coverage_radius_km: Int = 5,
    val base_lat: Double = -34.6186,
    val base_lng: Double = -58.4368,
    val dni_front_url: String,
    val dni_back_url: String,
    val license_number: String? = null,
    val license_issuer: String? = null,
    val license_doc_url: String? = null
)

data class AvailabilityToggleInputDto(
    val is_available_now: Boolean,
    val neighborhood: String? = null
)

data class CreateServiceRequestInputDto(
    val client_id: String? = null,
    val client_name: String,
    val client_phone: String,
    val client_address: String,
    val professional_id: String,
    val category_id: Int,
    val urgency: String,            // "normal" | "urgente"
    val problem_description: String,
    val photo_url: String? = null,
    val contact_channel: String = "whatsapp"
)

data class ServiceRequestDto(
    val id: String,
    val client_id: String? = null,
    val client_name: String,
    val client_initials: String = "ME",
    val client_phone: String,
    val client_address: String,
    val client_distance_label: String = "A 6 cuadras",
    val professional_id: String,
    val professional_name: String = "Roberto Carlos Gómez",
    val professional_phone: String = "5491145678901",
    val professional_license: String = "Matrícula ENARGAS #18.492",
    val category_id: Int = 1,
    val urgency: String = "normal",
    val problem_description: String,
    val photo_url: String? = null,
    val contact_channel: String = "whatsapp",
    val status: String = "pending",
    val scheduled_note: String? = null,
    val time_ago_label: String = "Hace instantes"
)

data class ProfessionalDashboardDto(
    val professional: ProfessionalDetailDto,
    val new_today_count: Int = 0,
    val urgent_count: Int = 0,
    val month_contacts: Int = 0,
    val active_requests: List<ServiceRequestDto> = emptyList()
)

data class CreateReviewInputDto(
    val author_name: String,
    val author_zone: String = "Vecina Verificada de Caballito",
    val rating: Int,
    val highlights: List<String>,
    val comment: String
)
