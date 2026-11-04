# Guía de Oficios — Aplicación Mobile Android (Kotlin + Jetpack Compose) 📱

Repositorio oficial de la aplicación móvil Android para **Guía de Oficios**, desarrollada en **Kotlin + Jetpack Compose (Material 3)** respetando estrictamente el sistema de diseño y las **15 vistas generadas con Stitch**, conectada al backend real en Go + PostgreSQL/PostGIS (cero mocks).

## 🔗 Repositorios Hermanos de la Organización
* ⚙️ **Backend API (Golang + PostgreSQL/PostGIS):** [`Guia-de-Oficios/guia-de-oficios-backend`](https://github.com/Guia-de-Oficios/guia-de-oficios-backend)
* 🎨 **Diseño Stitch (15 Vistas Oficiales) y Documentación General:** [`Guia-de-Oficios/guia-de-oficios-docs-ui`](https://github.com/Guia-de-Oficios/guia-de-oficios-docs-ui)

---

## 📚 Plan de Ejecución y Documentación en `/docs`
1. 🚀 **[`docs/06_radiografia_vistas_y_plan_paso_a_paso.md`](docs/06_radiografia_vistas_y_plan_paso_a_paso.md)**: **[HOJA DE RUTA DEL EQUIPO]** Radiografía completa de las 15 vistas, auditoría del esqueleto base, guía de ejecución local y checklist de los **14 Pasos Secuenciales** (ramas `feat/paso-XX-...`).
2. [`docs/01_especificacion_mvp_y_evolutivos.md`](docs/01_especificacion_mvp_y_evolutivos.md): Especificación funcional del MVP y endpoints REST.
3. [`docs/02_guia_ux_ui_accesible.md`](docs/02_guia_ux_ui_accesible.md): Paleta de colores, tipografía y reglas de accesibilidad (`WCAG AAA`, botones de `56.dp`).
4. [`docs/03_trabajo_colaborativo_y_onboarding.md`](docs/03_trabajo_colaborativo_y_onboarding.md): Organización del trabajo en equipo entre Android Studio y Docker, modularización y ramas.
5. [`docs/04_vision_negocio_y_estrategia.md`](docs/04_vision_negocio_y_estrategia.md): Visión de producto y evolutivos de monetización.

---

## 🗺️ Mapa de los 14 Pasos de Trabajo en Android

| Paso | Pantallas / Componentes | Estado |
| :---: | :--- | :---: |
| **Paso 1** | • Modularización de `StitchWorkflowScreens.kt` en archivos individuales por vista<br>• `GdoLogoBadge` oficial (escudo #1E40AF + tilde verde #15803D)<br>• `RoleSelectionScreen.kt` (Vista #8 con selector de rol inicial 96dp)<br>• Rediseño de Login: eliminar AlertDialog precario, formulario dedicado con estados de carga y manejo inline de errores | 🔜 **En curso (Punto de partida)** |
| **Paso 2** | • `RegisterScreen.kt`: selector visual de rol, normalización telefónica, selector de barrio real y términos de convivencia legal | ⏳ Pendiente |
| **Paso 3** | • Redirección inteligente post-login según rol (`client` vs `professional` verificado vs onboarding) | ⏳ Pendiente |
| **Paso 4** | • Separación de botones TopAppBar, pestaña 'Mis contactos' a historial real y pantalla 'Mi Cuenta' por rol | ⏳ Pendiente |
| **Paso 5** | • Home del vecino (Vista #4): chips de diagnóstico sin textos pre-escritos y navegación a resultados | ⏳ Pendiente |
| **Paso 6** | • `SearchResultsScreen` (Vista #5): fotos de perfil 64dp, badges combinados (urgencia + matrícula) y tira para mayores | ⏳ Pendiente |
| **Paso 7** | • `ProfessionalProfileScreen` (Vista #6): sin fallbacks falsos, 3 tarjetas de garantía, portfolio Coil y opiniones | ⏳ Pendiente |
| **Paso 8** | • `TopProfessionalsMonthScreen` (Vista #7): podio barrial (#1 Oro, #2 Plata, #3 Bronce) y filtros | ⏳ Pendiente |
| **Paso 9** | • `GuidedRequestScreen` (Vista #9): tarjetas de urgencia 72dp, preview y borrado de foto | ⏳ Pendiente |
| **Paso 10** | • `RequestConfirmationScreen` (Vista #10): confirmación con tilde 64dp, botones gigantes WhatsApp/Llamada | ⏳ Pendiente |
| **Paso 11** | • `ProfessionalEnRouteMapScreen` (Vista #11) + pestaña 'Mis contactos' con mapa y ETA | ⏳ Pendiente |
| **Paso 12** | • `JobCompletedReviewScreen` (Vista #12): 5 estrellas doradas 48dp, 5 aspectos destacables | ⏳ Pendiente |
| **Paso 13** | • `ProfessionalVerificationScreen` (Vista #13): selector de 8 rubros, fotos reales DNI Frente/Dorso | ⏳ Pendiente |
| **Paso 14** | • `ProfessionalDashboardScreen` (Vista #14) + `ProfessionalAcceptJobScreen` (Vista #15) + APK Final | ⏳ Pendiente |

---

## 🔌 Conexión Automática con el Servidor Backend (Docker)

El cliente HTTP en [`RetrofitClient.kt`](app/src/main/java/ar/guiaoficios/app/data/api/RetrofitClient.kt) incluye un interceptor de **failover automático** que prueba en orden:
1. `http://127.0.0.1:8080/` (para dispositivos físicos conectados por USB o Wi-Fi ADB con `adb reverse tcp:8080 tcp:8080`).
2. La IP LAN local de la PC (para dispositivos físicos en la misma red Wi-Fi).
3. `http://10.0.2.2:8080/` (para el Emulador de Android Studio).
