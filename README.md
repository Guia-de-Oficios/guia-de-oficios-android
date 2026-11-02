# Guía de Oficios — Aplicación Mobile Android (Kotlin + Jetpack Compose) 📱

Repositorio oficial de la aplicación móvil Android para **Guía de Oficios**, estructurada en **Kotlin + Jetpack Compose (Material 3)** respetando estrictamente el sistema de diseño y las **10 pantallas generadas con Stitch**.

## 🔗 Repositorios Hermanos de la Organización
* ⚙️ **Backend API (Golang + PostgreSQL/PostGIS):** [`Guia-de-Oficios/guia-de-oficios-backend`](https://github.com/Guia-de-Oficios/guia-de-oficios-backend)
* 🎨 **Diseño Stitch (10 Pantallas Intactas) y Documentación General:** [`Guia-de-Oficios/guia-de-oficios-docs-ui`](https://github.com/Guia-de-Oficios/guia-de-oficios-docs-ui)

---

## 📚 Documentación Incluida en `/docs`
En la carpeta [`docs/`](docs/) encontrarás toda la documentación funcional, de diseño y de negocio para estar al tanto de cada detalle del proyecto:
1. [`docs/01_especificacion_mvp_y_evolutivos.md`](docs/01_especificacion_mvp_y_evolutivos.md): Especificación funcional de las 10 pantallas de Stitch y sus endpoints en el backend Go.
2. [`docs/02_guia_ux_ui_accesible.md`](docs/02_guia_ux_ui_accesible.md): Paleta de colores, tipografía `Inter` y reglas de accesibilidad (`WCAG AAA`, botones de `56dp`).
3. [`docs/03_trabajo_colaborativo_y_onboarding.md`](docs/03_trabajo_colaborativo_y_onboarding.md): Organización del trabajo en equipo entre Android Studio y GoLand.
4. [`docs/04_vision_negocio_y_estrategia.md`](docs/04_vision_negocio_y_estrategia.md): Visión de producto, MVP Alfa/Beta y evolutivos de monetización.

---

## 🎨 Sistema de Diseño Oficial (`Theme.kt`)
Los colores y estilos en [`Theme.kt`](app/src/main/java/ar/guiaoficios/app/ui/theme/Theme.kt) provienen directamente del archivo oficial `DESIGN.md` generado por Stitch (no modificar):
* **Primary (`#00288E`) & PrimaryContainer (`#1E40AF`)**: Azul cívico sereno para encabezados, navegación y acciones de identidad.
* **Secondary (`#006D30`) & WhatsAppActionGreen (`#15803D`)**: Verde de alta visibilidad para acciones de confirmación y contacto directo por WhatsApp.
* **Surface (`#FAF8FF`) & SurfaceContainerLowest (`#FFFFFF`)**: Fondos claros que minimizan el encandilamiento y aseguran contraste máximo con textos en `#131B2E`.
* **Botones Táctiles Accesibles**: Altura mínima estricta de `56.dp` para facilitar el uso a personas mayores.

---

## 📱 Mapa de las 10 Pantallas Implementadas

| # |Ruta en `StitchNavigation.kt` | Composable | Pantalla Original en Stitch |
| :--- | :--- | :--- | :--- |
| **1** | `welcome_role` | `WelcomeRoleScreen` | `bienvenida_y_selecci_n_de_rol` |
| **2** | `register_account/{role}` | `RegisterAccountScreen` | `registro_google_o_manual` |
| **3** | `client_home` | `HomeScreen` | `cliente_inicio_y_categor_as` |
| **4** | `client_results/{categoryId}` | `SearchResultsScreen` | `cliente_resultados_de_profesionales` |
| **5** | `professional_profile/{proId}` | `ProfessionalProfileScreen` | `cliente_perfil_del_profesional_y_contacto` |
| **6** | `guided_request/{proId}` | `GuidedRequestScreen` | `cliente_solicitud_guiada_a_roberto` |
| **7** | `request_confirmation/{requestId}` | `GuidedRequestScreen` (Confirmación) | `cliente_elegir_contacto_y_confirmaci_n` |
| **8** | `job_review/{requestId}` | `JobCompletedReviewScreen` | `cliente_trabajo_terminado_y_calificaci_n` |
| **9** | `pro_verification/{proId}` | Onboarding DNI / Matrícula | `profesional_registro_y_verificaci_n_dni` |
| **10** | `pro_dashboard/{proId}` | `ProfessionalDashboardScreen` | `profesional_panel_y_solicitudes_recibidas` |

---

## 🔌 Conexión con el Servidor Backend (Golang)

La interfaz Retrofit [`GuiaDeOficiosApi.kt`](app/src/main/java/ar/guiaoficios/app/data/api/GuiaDeOficiosApi.kt) consume todos los endpoints del servidor Go:
* Si ejecutás la app desde el **Emulador de Android Studio** mientras el backend Go corre en tu PC en el puerto `8080`, configurá el `BASE_URL` de Retrofit apuntando a:
  ```kotlin
  const val BASE_URL = "http://10.0.2.2:8080/"
  ```
* Si probás desde un **dispositivo Android físico** conectado a la misma red Wi-Fi, usá la IP local de tu máquina (ej. `http://192.168.1.XX:8080/`).
