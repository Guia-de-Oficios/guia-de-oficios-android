package ar.guiaoficios.app.ui.navigation

/**
 * Rutas oficiales de las 15 vistas generadas con Stitch para "Guía de Oficios".
 */
sealed class StitchScreen(val route: String, val title: String) {
    // Vista #8: gu_a_de_oficios_servicios_y_oficios_vecinales (Selector Inicial de Rol)
    object RoleSelection : StitchScreen("role_selection", "Selección Inicial de Rol")

    // Vista #2: bienvenida_y_selecci_n_de_rol (Bienvenida e Inicio de Sesión)
    object WelcomeRoleSelection : StitchScreen("welcome_role", "Bienvenida e Inicio de Sesión")

    // Vista #3: registro_google_o_manual
    object RegisterAccount : StitchScreen("register_account/{role}", "Crear Cuenta (Google o Manual)") {
        fun createRoute(role: String) = "register_account/$role"
    }

    // Vista #4: cliente_inicio_y_categor_as
    object ClientHomeCategories : StitchScreen("client_home", "Inicio del Cliente y Categorías")

    // Vista #5: cliente_resultados_de_profesionales
    object ClientSearchResults : StitchScreen("client_results/{categoryId}", "Resultados de Profesionales") {
        fun createRoute(categoryId: Int) = "client_results/$categoryId"
    }

    // Vista #6: cliente_perfil_del_profesional_y_contacto
    object ClientProfessionalProfile : StitchScreen("professional_profile/{proId}", "Perfil del Profesional") {
        fun createRoute(proId: String) = "professional_profile/$proId"
    }

    // Vista #7: comunidad_top_profesionales_del_mes
    object CommunityTopProfessionals : StitchScreen("community_top_month", "Top Profesionales del Mes")

    // Vista #9: cliente_solicitud_guiada_a_roberto
    object ClientGuidedRequest : StitchScreen("guided_request/{proId}", "Solicitud Guiada Paso 1 de 2") {
        fun createRoute(proId: String) = "guided_request/$proId"
    }

    // Vista #10: cliente_elegir_contacto_y_confirmaci_n
    object ClientRequestConfirmation : StitchScreen("request_confirmation/{requestId}", "Confirmación de Solicitud") {
        fun createRoute(requestId: String) = "request_confirmation/$requestId"
    }

    // Vista #11: cliente_profesional_en_camino_y_mapa_en_vivo
    object ClientProfessionalEnRouteMap : StitchScreen("en_route_map/{requestId}", "Profesional en Camino") {
        fun createRoute(requestId: String) = "en_route_map/$requestId"
    }

    // Vista #12: cliente_trabajo_terminado_y_calificaci_n
    object ClientJobCompletedReview : StitchScreen("job_review/{requestId}", "Trabajo Terminado y Calificación") {
        fun createRoute(requestId: String) = "job_review/$requestId"
    }

    // Vista #13: profesional_registro_y_verificaci_n_dni
    object ProfessionalVerificationOnboarding : StitchScreen("pro_verification/{proId}", "Registro y Verificación DNI") {
        fun createRoute(proId: String) = "pro_verification/$proId"
    }

    // Vista #14: profesional_panel_y_solicitudes_recibidas
    object ProfessionalDashboard : StitchScreen("pro_dashboard/{proId}", "Panel del Profesional y Solicitudes") {
        fun createRoute(proId: String) = "pro_dashboard/$proId"
    }

    // Vista #15: profesional_aceptar_trabajo_e_iniciar_viaje
    object ProfessionalAcceptJob : StitchScreen("pro_accept_job/{requestId}", "Aceptar Trabajo e Iniciar Viaje") {
        fun createRoute(requestId: String) = "pro_accept_job/$requestId"
    }

    // Pestaña Mis Contactos (Historial del Vecino)
    object ClientMyRequestsHistory : StitchScreen("my_requests_history", "Mis Contactos y Solicitudes")

    // Mi Cuenta (Perfil real y Cerrar Sesión)
    object UserAccount : StitchScreen("my_account", "Mi Cuenta")
}
