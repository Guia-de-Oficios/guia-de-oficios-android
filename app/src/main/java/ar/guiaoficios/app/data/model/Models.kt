package ar.guiaoficios.app.data.model

data class CategoryDto(
    val id: Int,
    val slug: String,
    val name: String,
    val helper_examples: String,
    val icon_name: String,
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
    val saved_address: String? = null
)

data class UserDto(
    val id: String,
    val full_name: String,
    val email: String,
    val phone: String,
    val role: String,
    val auth_provider: String,
    val saved_address: String
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
    val author_zone: String,
    val rating: Int,
    val highlights: List<String>,
    val comment: String
)

data class ProfessionalSummaryDto(
    val id: String,
    val user_id: String,
    val full_name: String,
    val phone: String,
    val avatar_url: String,
    val primary_category_id: Int,
    val category_name: String,
    val trade_title: String,
    val specialties: List<String>,
    val neighborhood: String,
    val distance_label: String,
    val distance_km: Double,
    val is_available_now: Boolean,
    val attends_emergencies: Boolean,
    val is_dni_verified: Boolean,
    val is_licensed: Boolean,
    val license_number: String?,
    val license_issuer: String?,
    val visit_price_amount: Int,
    val visit_price_label: String,
    val rating_avg: Double,
    val rating_label: String,
    val reviews_count: Int,
    val jobs_completed_count: Int,
    val monthly_contacts_count: Int
)

data class ProfessionalDetailDto(
    val id: String,
    val user_id: String,
    val full_name: String,
    val phone: String,
    val avatar_url: String,
    val primary_category_id: Int,
    val category_name: String,
    val trade_title: String,
    val specialties: List<String>,
    val neighborhood: String,
    val distance_label: String,
    val is_available_now: Boolean,
    val attends_emergencies: Boolean,
    val is_dni_verified: Boolean,
    val is_licensed: Boolean,
    val license_number: String?,
    val license_issuer: String?,
    val visit_price_amount: Int,
    val visit_price_label: String,
    val rating_avg: Double,
    val rating_label: String,
    val reviews_count: Int,
    val jobs_completed_count: Int,
    val monthly_contacts_count: Int,
    val bio: String,
    val years_experience: Int,
    val guarantee_percentage: Int,
    val portfolio: List<PortfolioItemDto>,
    val reviews: List<ReviewDto>
)

data class OnboardingVerificationInputDto(
    val primary_category_id: Int,
    val trade_title: String,
    val specialties: List<String>,
    val neighborhood: String,
    val coverage_radius_km: Int = 5,
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
    val client_name: String,
    val client_initials: String,
    val client_phone: String,
    val client_address: String,
    val client_distance_label: String,
    val professional_id: String,
    val professional_name: String,
    val professional_phone: String,
    val professional_license: String,
    val category_id: Int,
    val urgency: String,
    val problem_description: String,
    val photo_url: String?,
    val contact_channel: String,
    val status: String,             // "pending" | "scheduled" | "completed"
    val scheduled_note: String?,
    val time_ago_label: String
)

data class ProfessionalDashboardDto(
    val professional: ProfessionalDetailDto,
    val new_today_count: Int,
    val urgent_count: Int,
    val month_contacts: Int,
    val active_requests: List<ServiceRequestDto>
)

data class CreateReviewInputDto(
    val author_name: String,
    val author_zone: String = "Vecina Verificada de Caballito",
    val rating: Int,
    val highlights: List<String>,
    val comment: String
)
