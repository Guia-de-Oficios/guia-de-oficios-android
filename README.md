# Guía de Oficios — Aplicación Mobile Android (Kotlin + Jetpack Compose) 📱

Repositorio oficial de la aplicación móvil Android para **Guía de Oficios**, desarrollada en **Kotlin + Jetpack Compose (Material 3)** respetando estrictamente el sistema de diseño y las **15 vistas generadas con Stitch**, conectada al backend real en Go + PostgreSQL/PostGIS (cero mocks).

## 🔗 Repositorios Hermanos de la Organización
* ⚙️ **Backend API (Golang + PostgreSQL/PostGIS):** [`Guia-de-Oficios/guia-de-oficios-backend`](https://github.com/Guia-de-Oficios/guia-de-oficios-backend)
* 🎨 **Diseño Stitch (15 Vistas Oficiales) y Documentación General:** [`Guia-de-Oficios/guia-de-oficios-docs-ui`](https://github.com/Guia-de-Oficios/guia-de-oficios-docs-ui)

---

## 📚 Plan de Etapas Funcionales y Documentación en `/docs`
1. 🚀 **[`docs/06_plan_etapas_funcionales_y_tareas.md`](docs/06_plan_etapas_funcionales_y_tareas.md)**: **[HOJA DE RUTA DEL EQUIPO]** Detalle de las **4 Etapas Funcionales**, lo que ya está terminado en la **Etapa 1** y las tareas paso a paso para continuar con las **Etapas 2, 3 y 4**.
2. [`docs/01_especificacion_mvp_y_evolutivos.md`](docs/01_especificacion_mvp_y_evolutivos.md): Especificación funcional del MVP y endpoints REST.
3. [`docs/02_guia_ux_ui_accesible.md`](docs/02_guia_ux_ui_accesible.md): Paleta de colores, tipografía y reglas de accesibilidad (`WCAG AAA`, botones de `56.dp`).
4. [`docs/03_trabajo_colaborativo_y_onboarding.md`](docs/03_trabajo_colaborativo_y_onboarding.md): Organización del trabajo en equipo entre Android Studio y Docker.
5. [`docs/04_vision_negocio_y_estrategia.md`](docs/04_vision_negocio_y_estrategia.md): Visión de producto y evolutivos de monetización.

---

## 🗺️ Estado de las 4 Etapas Funcionales en Android

| Etapa | Pantallas / Componentes | Estado |
| :--- | :--- | :---: |
| **Etapa 1** | • `StitchBrandComponents.kt` (`GdoBrandLogo`, `GoogleColorIcon`, `StitchTopAppBar`, `StitchBottomNavBar`)<br>• `WelcomeRoleScreen` (`bienvenida_y_selecci_n_de_rol`)<br>• `RegisterAccountScreen` (`registro_google_o_manual`)<br>• `HomeScreen` (`cliente_inicio_y_categor_as`) + `UserAccountScreen` (`Mi cuenta` y Cerrar sesión)<br>• Sesión JWT real validada contra `GET /api/v1/auth/me` (sin usuarios hardcodeados) | ✅ **Completada** |
| **Etapa 2** | • `SearchResultsScreen` (`cliente_resultados_de_profesionales`)<br>• `ProfessionalProfileScreen` (`cliente_perfil_del_profesional_y_contacto`)<br>• `TopProfessionalsMonthScreen` (`comunidad_top_profesionales_del_mes`) | 🔜 **Siguiente Etapa** |
| **Etapa 3** | • `GuidedRequestScreen` (`cliente_solicitud_guiada_a_roberto`)<br>• `RequestConfirmationScreen` (`cliente_elegir_contacto_y_confirmaci_n`)<br>• `ProfessionalEnRouteMapScreen` (`cliente_profesional_en_camino_y_mapa_en_vivo`)<br>• `JobCompletedReviewScreen` (`cliente_trabajo_terminado_y_calificaci_n`) | ⏳ **Etapa 3** |
| **Etapa 4** | • `ProfessionalVerificationScreen` (`profesional_registro_y_verificaci_n_dni`)<br>• `ProfessionalDashboardScreen` (`profesional_panel_y_solicitudes_recibidas`)<br>• `ProfessionalAcceptJobScreen` (`profesional_aceptar_trabajo_e_iniciar_viaje`) | ⏳ **Etapa 4** |

---

## 🔌 Conexión Automática con el Servidor Backend (Docker)

El cliente HTTP en [`RetrofitClient.kt`](app/src/main/java/ar/guiaoficios/app/data/api/RetrofitClient.kt) incluye un interceptor de **failover automático** que prueba en orden:
1. `http://127.0.0.1:8080/` (para dispositivos físicos conectados por USB o Wi-Fi ADB con `adb reverse tcp:8080 tcp:8080`).
2. La IP LAN local de la PC (para dispositivos físicos en la misma red Wi-Fi).
3. `http://10.0.2.2:8080/` (para el Emulador de Android Studio).
