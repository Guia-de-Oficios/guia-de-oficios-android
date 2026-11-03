package ar.guiaoficios.app.ui.navigation

/**
 * Rutas oficiales de las 10 pantallas generadas con Stitch para "Guía de Oficios".
 */
sealed class StitchScreen(val route: String, val title: String) {
    // 1. bienvenida_y_selecci_n_de_rol
    object WelcomeRoleSelection : StitchScreen("welcome_role", "Bienvenida y Selección de Rol")

    // 2. registro_google_o_manual
    object RegisterAccount : StitchScreen("register_account/{role}", "Crear Cuenta (Google o Manual)") {
        fun createRoute(role: String) = "register_account/$role"
    }

    // 3. cliente_inicio_y_categor_as
    object ClientHomeCategories : StitchScreen("client_home", "Inicio del Cliente y Categorías")

    // 4. cliente_resultados_de_profesionales
    object ClientSearchResults : StitchScreen("client_results/{categoryId}", "Resultados de Profesionales") {
        fun createRoute(categoryId: Int) = "client_results/$categoryId"
    }

    // 5. cliente_perfil_del_profesional_y_contacto
    object ClientProfessionalProfile : StitchScreen("professional_profile/{proId}", "Perfil de Roberto Gómez") {
        fun createRoute(proId: String) = "professional_profile/$proId"
    }

    // 6. cliente_solicitud_guiada_a_roberto
    object ClientGuidedRequest : StitchScreen("guided_request/{proId}", "Solicitud Guiada Paso 1 de 2") {
        fun createRoute(proId: String) = "guided_request/$proId"
    }

    // 7. cliente_elegir_contacto_y_confirmaci_n
    object ClientRequestConfirmation : StitchScreen("request_confirmation/{requestId}", "Confirmación de Solicitud") {
        fun createRoute(requestId: String) = "request_confirmation/$requestId"
    }

    // 8. cliente_trabajo_terminado_y_calificaci_n
    object ClientJobCompletedReview : StitchScreen("job_review/{requestId}", "Trabajo Terminado y Calificación") {
        fun createRoute(requestId: String) = "job_review/$requestId"
    }

    // 9. profesional_registro_y_verificaci_n_dni
    object ProfessionalVerificationOnboarding : StitchScreen("pro_verification/{proId}", "Registro y Verificación DNI") {
        fun createRoute(proId: String) = "pro_verification/$proId"
    }

    // 10. profesional_panel_y_solicitudes_recibidas
    object ProfessionalDashboard : StitchScreen("pro_dashboard/{proId}", "Panel del Profesional y Solicitudes") {
        fun createRoute(proId: String) = "pro_dashboard/$proId"
    }

    // 11. Mi Cuenta (Perfil real y Cerrar Sesión)
    object UserAccount : StitchScreen("my_account", "Mi Cuenta")
}
