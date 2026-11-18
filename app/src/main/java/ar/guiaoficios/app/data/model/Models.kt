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

data class NeighborhoodOption(
    val label: String,
    val zone: String,
    val lat: Double,
    val lng: Double
)

val ARGENTINA_SUGGESTED_LOCALITIES: List<NeighborhoodOption> = listOf(
    // Gran Mendoza y Zona Este (Zonas de lanzamiento prioritario)
    NeighborhoodOption("Ciudad de Mendoza, Mendoza", "Gran Mendoza", -32.8895, -68.8458),
    NeighborhoodOption("Godoy Cruz, Mendoza", "Gran Mendoza", -32.9253, -68.8444),
    NeighborhoodOption("Guaymallén, Mendoza", "Gran Mendoza", -32.8997, -68.7975),
    NeighborhoodOption("Las Heras, Mendoza", "Gran Mendoza", -32.8527, -68.8284),
    NeighborhoodOption("Luján de Cuyo, Mendoza", "Gran Mendoza", -33.0381, -68.8794),
    NeighborhoodOption("Maipú, Mendoza", "Gran Mendoza", -32.9774, -68.7867),
    NeighborhoodOption("Palmira, San Martín, Mendoza", "Mendoza Este", -33.0546, -68.5613),
    NeighborhoodOption("San Martín, Mendoza", "Mendoza Este", -33.0810, -68.4681),
    NeighborhoodOption("Rivadavia, Mendoza", "Mendoza Este", -33.1905, -68.4608),
    NeighborhoodOption("San Rafael, Mendoza", "Mendoza Sur", -34.6177, -68.3301),
    // Luján y Zona Oeste PBA (Zonas de lanzamiento prioritario)
    NeighborhoodOption("Luján, Buenos Aires", "PBA Oeste", -34.5703, -59.1050),
    NeighborhoodOption("Open Door, Luján, Buenos Aires", "PBA Oeste", -34.4961, -59.0692),
    NeighborhoodOption("Jáuregui, Luján, Buenos Aires", "PBA Oeste", -34.5969, -59.1756),
    NeighborhoodOption("General Rodríguez, Buenos Aires", "PBA Oeste", -34.6083, -58.9489),
    NeighborhoodOption("Mercedes, Buenos Aires", "PBA Oeste", -34.6515, -59.4307),
    NeighborhoodOption("Pilar, Buenos Aires", "GBA Norte/Oeste", -34.4587, -58.9142),
    // CABA y GBA
    NeighborhoodOption("Caballito, CABA", "CABA Centro", -34.6186, -58.4368),
    NeighborhoodOption("Flores, CABA", "CABA Oeste", -34.6283, -58.4632),
    NeighborhoodOption("Almagro, CABA", "CABA Centro", -34.6095, -58.4206),
    NeighborhoodOption("Palermo, CABA", "CABA Norte", -34.5889, -58.4305),
    NeighborhoodOption("Belgrano, CABA", "CABA Norte", -34.5627, -58.4583),
    NeighborhoodOption("Recoleta, CABA", "CABA Norte", -34.5895, -58.3974),
    NeighborhoodOption("Villa Urquiza, CABA", "CABA Noroeste", -34.5736, -58.4864),
    NeighborhoodOption("Villa Crespo, CABA", "CABA Centro", -34.5986, -58.4426),
    NeighborhoodOption("Villa Devoto, CABA", "CABA Oeste", -34.6019, -58.5122),
    NeighborhoodOption("Parque Chacabuco, CABA", "CABA Sur", -34.6347, -58.4428),
    NeighborhoodOption("Boedo, CABA", "CABA Sur", -34.6258, -58.4168),
    NeighborhoodOption("San Telmo, CABA", "CABA Centro/Sur", -34.6212, -58.3731),
    NeighborhoodOption("Barracas, CABA", "CABA Sur", -34.6416, -58.3768),
    NeighborhoodOption("Núñez, CABA", "CABA Norte", -34.5453, -58.4619),
    NeighborhoodOption("Colegiales, CABA", "CABA Norte", -34.5750, -58.4485),
    NeighborhoodOption("Chacarita, CABA", "CABA Centro/Norte", -34.5881, -58.4542),
    NeighborhoodOption("Liniers, CABA", "CABA Oeste", -34.6428, -58.5215),
    NeighborhoodOption("Mataderos, CABA", "CABA Suroeste", -34.6578, -58.5032),
    NeighborhoodOption("Vicente López, GBA Norte", "GBA Norte", -34.5265, -58.4795),
    NeighborhoodOption("San Isidro, GBA Norte", "GBA Norte", -34.4721, -58.5272),
    NeighborhoodOption("Avellaneda, GBA Sur", "GBA Sur", -34.6625, -58.3653),
    NeighborhoodOption("Lanús, GBA Sur", "GBA Sur", -34.7069, -58.3908),
    NeighborhoodOption("Lomas de Zamora, GBA Sur", "GBA Sur", -34.7609, -58.4063),
    NeighborhoodOption("Morón, GBA Oeste", "GBA Oeste", -34.6534, -58.6198),
    NeighborhoodOption("Ramos Mejía, GBA Oeste", "GBA Oeste", -34.6453, -58.5647),
    NeighborhoodOption("Caseros, GBA Oeste", "GBA Oeste", -34.6094, -58.5633),
    // Otras ciudades de Argentina
    NeighborhoodOption("La Plata, Buenos Aires", "PBA Capital", -34.9205, -57.9536),
    NeighborhoodOption("Córdoba Capital, Córdoba", "Córdoba", -31.4201, -64.1888),
    NeighborhoodOption("Rosario, Santa Fe", "Santa Fe", -32.9442, -60.6505)
)

val CABA_GBA_NEIGHBORHOODS: List<NeighborhoodOption> = ARGENTINA_SUGGESTED_LOCALITIES

fun findNeighborhoodCoordinates(text: String): Pair<Double, Double> {
    val lower = text.lowercase()
    val match = ARGENTINA_SUGGESTED_LOCALITIES.firstOrNull { option ->
        val shortName = option.label.substringBefore(",").trim().lowercase()
        lower.contains(shortName)
    }
    return if (match != null) {
        match.lat to match.lng
    } else {
        0.0 to 0.0
    }
}

fun normalizeArgentineMobilePhone(rawPhone: String): String {
    val digits = rawPhone.filter { it.isDigit() }
    val localDigits = when {
        digits.startsWith("549") && digits.length >= 11 -> digits.removePrefix("549")
        digits.startsWith("54") && digits.length >= 10 -> digits.removePrefix("54")
        digits.startsWith("0") && digits.length >= 9 -> digits.removePrefix("0")
        else -> digits
    }.let { cleaned ->
        if (cleaned.startsWith("15") && cleaned.length == 10) {
            "11" + cleaned.removePrefix("15")
        } else if (cleaned.length == 8) {
            "11$cleaned"
        } else {
            cleaned
        }
    }
    return if (localDigits.startsWith("11") && localDigits.length == 10) {
        val part1 = localDigits.substring(2, 6)
        val part2 = localDigits.substring(6, 10)
        "+54 9 11 $part1-$part2"
    } else if (localDigits.length >= 8) {
        "+54 9 $localDigits"
    } else {
        rawPhone.trim()
    }
}

data class RegisterUserInputDto(
    val full_name: String,
    val phone: String,
    val email: String,
    val password: String? = null,
    val role: String,           // "client" | "professional"
    val auth_provider: String = "manual",
    val neighborhood: String = "",
    val address: String = "",
    val saved_address: String? = "",
    val saved_lat: Double? = 0.0,
    val saved_lng: Double? = 0.0,
    val primary_category_id: Int = 1,
    val trade_title: String = "",
    val coverage_radius_km: Int = 10
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
    val role: String = "client",
    val neighborhood: String = "",
    val address: String = ""
)

data class UpdateAddressInputDto(
    val neighborhood: String = "",
    val saved_address: String,
    val saved_lat: Double = 0.0,
    val saved_lng: Double = 0.0
)

data class UpdateUserProfileInputDto(
    val full_name: String,
    val phone: String,
    val neighborhood: String = "",
    val address: String = "",
    val saved_address: String = "",
    val saved_lat: Double = 0.0,
    val saved_lng: Double = 0.0,
    val primary_category_id: Int = 0,
    val trade_title: String = "",
    val coverage_radius_km: Int = 0
)

data class GeocodeResultDto(
    val formatted_address: String = "",
    val neighborhood: String = "",
    val locality: String = "",
    val province: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val source: String = "",
    val verified: Boolean = false
)

data class GeocodeSuggestionDto(
    val label: String = "",
    val subtitle: String = "",
    val locality: String = "",
    val province: String = "",
    val street_address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val source: String = ""
)

data class UserDto(
    val id: String,
    val full_name: String,
    val email: String,
    val phone: String = "",
    val role: String = "client",
    val auth_provider: String = "manual",
    val saved_address: String = "",
    val saved_lat: Double = -32.8895,
    val saved_lng: Double = -68.8458,
    val professional_id: String? = null
) {
    val isProfileIncomplete: Boolean
        get() = phone.isBlank() ||
            saved_address.isBlank() ||
            saved_address.equals("Sin especificar", ignoreCase = true)
}

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
    val portfolio: List<PortfolioItemDto>? = emptyList(),
    val reviews: List<ReviewDto>? = emptyList()
) {
    val safeSpecialties: List<String> get() = specialties ?: emptyList()
    val safePortfolio: List<PortfolioItemDto> get() = portfolio ?: emptyList()
    val safeReviews: List<ReviewDto> get() = reviews ?: emptyList()
}

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
