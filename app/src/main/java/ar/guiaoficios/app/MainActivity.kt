package ar.guiaoficios.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ar.guiaoficios.app.data.model.ProfessionalDetailDto
import ar.guiaoficios.app.data.model.ProfessionalDashboardDto
import ar.guiaoficios.app.data.model.ServiceRequestDto
import ar.guiaoficios.app.ui.MainViewModel
import ar.guiaoficios.app.ui.navigation.StitchScreen
import ar.guiaoficios.app.ui.screens.*
import ar.guiaoficios.app.ui.theme.*
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuiaDeOficiosTheme {
                GuiaDeOficiosApp()
            }
        }
    }
}

@Composable
fun GuiaDeOficiosApp(
    mainViewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: StitchScreen.WelcomeRoleSelection.route

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            mainViewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceContainerLowest,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute.startsWith("welcome") || currentRoute.startsWith("register"),
                    onClick = {
                        navController.navigate(StitchScreen.WelcomeRoleSelection.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Text("👋") },
                    label = { Text("Acceso") }
                )
                NavigationBarItem(
                    selected = currentRoute.startsWith("client_home") || currentRoute.startsWith("client_results"),
                    onClick = {
                        mainViewModel.refreshInitialData()
                        navController.navigate(StitchScreen.ClientHomeCategories.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Text("🏠") },
                    label = { Text("Vecino") }
                )
                NavigationBarItem(
                    selected = currentRoute.startsWith("professional_profile") || currentRoute.startsWith("guided_request"),
                    onClick = {
                        val proId = MainViewModel.DEFAULT_ROBERTO_PRO_ID
                        mainViewModel.loadProfessionalDetail(proId)
                        navController.navigate(StitchScreen.ClientProfessionalProfile.createRoute(proId)) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Text("🔧") },
                    label = { Text("Roberto") }
                )
                NavigationBarItem(
                    selected = currentRoute.startsWith("pro_dashboard") || currentRoute.startsWith("pro_verification"),
                    onClick = {
                        val proId = uiState.currentUser.professional_id ?: MainViewModel.DEFAULT_ROBERTO_PRO_ID
                        mainViewModel.loadProfessionalDashboard(proId)
                        navController.navigate(StitchScreen.ProfessionalDashboard.createRoute(proId)) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Text("🛠️") },
                    label = { Text("Panel Oficio") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = StitchScreen.WelcomeRoleSelection.route
            ) {
                // 1. Pantalla 1: Bienvenida y Selección de Rol
                composable(StitchScreen.WelcomeRoleSelection.route) {
                    WelcomeRoleScreen(
                        onContinueWithRole = { role ->
                            navController.navigate(StitchScreen.RegisterAccount.createRoute(role))
                        },
                        onLoginClick = {
                            navController.navigate(StitchScreen.RegisterAccount.createRoute("client"))
                        }
                    )
                }

                // 2. Pantalla 2: Registro (Google o Manual) e Inicio de Sesión
                composable(
                    route = StitchScreen.RegisterAccount.route,
                    arguments = listOf(navArgument("role") { type = NavType.StringType })
                ) { backStackEntry ->
                    val role = backStackEntry.arguments?.getString("role") ?: "client"
                    RegisterAccountScreen(
                        initialRole = role,
                        onContinueGoogle = { selectedRole ->
                            mainViewModel.authenticateWithGoogle(selectedRole) { user ->
                                if (user.role == "professional") {
                                    val proId = user.professional_id ?: MainViewModel.DEFAULT_ROBERTO_PRO_ID
                                    mainViewModel.loadProfessionalDashboard(proId)
                                    navController.navigate(StitchScreen.ProfessionalVerificationOnboarding.createRoute(proId))
                                } else {
                                    mainViewModel.refreshInitialData()
                                    navController.navigate(StitchScreen.ClientHomeCategories.route)
                                }
                            }
                        },
                        onSubmitManual = { input ->
                            mainViewModel.registerManual(input) { user ->
                                if (user.role == "professional") {
                                    val proId = user.professional_id ?: MainViewModel.DEFAULT_ROBERTO_PRO_ID
                                    navController.navigate(StitchScreen.ProfessionalVerificationOnboarding.createRoute(proId))
                                } else {
                                    mainViewModel.refreshInitialData()
                                    navController.navigate(StitchScreen.ClientHomeCategories.route)
                                }
                            }
                        },
                        onSubmitLogin = { email, password ->
                            mainViewModel.loginWithEmail(email, password) { user ->
                                if (user.role == "professional") {
                                    val proId = user.professional_id ?: MainViewModel.DEFAULT_ROBERTO_PRO_ID
                                    mainViewModel.loadProfessionalDashboard(proId)
                                    navController.navigate(StitchScreen.ProfessionalDashboard.createRoute(proId))
                                } else {
                                    mainViewModel.refreshInitialData()
                                    navController.navigate(StitchScreen.ClientHomeCategories.route)
                                }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // 3. Pantalla 3: Inicio del Cliente y Categorías
                composable(StitchScreen.ClientHomeCategories.route) {
                    HomeScreen(
                        userName = uiState.currentUser.full_name,
                        savedAddress = uiState.currentUser.saved_address,
                        categories = uiState.categories,
                        diagnosticResult = uiState.diagnosticSuggestion,
                        onSelectCategory = { cat ->
                            mainViewModel.searchProfessionals(
                                categoryId = cat.id,
                                categoryTitle = "${cat.name}s cerca de vos",
                                filter = "all"
                            )
                            navController.navigate(StitchScreen.ClientSearchResults.createRoute(cat.id))
                        },
                        onSearchByQuery = { q ->
                            mainViewModel.searchProfessionals(
                                categoryId = 0,
                                categoryTitle = "Resultados para “$q”",
                                query = q,
                                filter = "all"
                            )
                            navController.navigate(StitchScreen.ClientSearchResults.createRoute(0))
                        },
                        onRequestDiagnostic = { problem ->
                            mainViewModel.requestDiagnostic(problem)
                        },
                        onUpdateAddress = { addr ->
                            mainViewModel.updateAddress(addr)
                        }
                    )
                }

                // 4. Pantalla 4: Resultados de Profesionales con Filtros PostGIS
                composable(
                    route = StitchScreen.ClientSearchResults.route,
                    arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val catId = backStackEntry.arguments?.getInt("categoryId") ?: 1
                    SearchResultsScreen(
                        categoryName = uiState.selectedCategoryName,
                        neighborhood = uiState.currentUser.saved_address.substringAfterLast(",").trim().ifBlank { "Caballito, CABA" },
                        professionals = uiState.professionals,
                        activeFilter = uiState.activeFilter,
                        onFilterChange = { newFilter ->
                            mainViewModel.searchProfessionals(
                                categoryId = catId,
                                categoryTitle = uiState.selectedCategoryName,
                                filter = newFilter
                            )
                        },
                        onSelectProfessional = { proId ->
                            mainViewModel.loadProfessionalDetail(proId)
                            navController.navigate(StitchScreen.ClientProfessionalProfile.createRoute(proId))
                        },
                        onCallNeighborhoodSupport = {
                            launchPhoneDialer(context, "08005556342")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // 5. Pantalla 5: Perfil del Profesional y Contacto
                composable(
                    route = StitchScreen.ClientProfessionalProfile.route,
                    arguments = listOf(navArgument("proId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val proId = backStackEntry.arguments?.getString("proId") ?: MainViewModel.DEFAULT_ROBERTO_PRO_ID
                    val proDetail = uiState.selectedProfessional ?: ProfessionalDetailDto(
                        id = proId,
                        full_name = "Roberto Carlos Gómez",
                        trade_title = "Plomero y Gasista Matriculado",
                        phone = "5491145678901",
                        bio = "Vecino de Caballito con más de 22 años solucionando pérdidas de agua, gas, calefones y destapaciones en el día."
                    )

                    ProfessionalProfileScreen(
                        professional = proDetail,
                        onContactWhatsApp = {
                            navController.navigate(StitchScreen.ClientGuidedRequest.createRoute(proId))
                        },
                        onCallPhone = {
                            navController.navigate(StitchScreen.ClientGuidedRequest.createRoute(proId))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // 6. Pantalla 6: Solicitud Guiada Paso 1 de 2 (con Photo Picker y Upload Real)
                composable(
                    route = StitchScreen.ClientGuidedRequest.route,
                    arguments = listOf(navArgument("proId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val proId = backStackEntry.arguments?.getString("proId") ?: MainViewModel.DEFAULT_ROBERTO_PRO_ID
                    val proName = uiState.selectedProfessional?.full_name?.substringBefore(" ") ?: "Roberto"

                    GuidedRequestScreen(
                        professionalName = proName,
                        uploadedPhotoUrl = uiState.uploadedProblemPhotoUrl,
                        isUploadingPhoto = uiState.isUploadingFile,
                        onPickPhotoUri = { uri ->
                            mainViewModel.uploadMediaUri(uri, "problem_photo")
                        },
                        onSubmitRequest = { urgency, description, photoUrl ->
                            mainViewModel.createServiceRequest(
                                professionalId = proId,
                                urgency = urgency,
                                description = description,
                                photoUrl = photoUrl
                            ) { createdReq ->
                                navController.navigate(StitchScreen.ClientRequestConfirmation.createRoute(createdReq.id))
                            }
                        }
                    )
                }

                // 7. Pantalla 7: Confirmación de Solicitud y Contacto Directo (WhatsApp / Llamada)
                composable(
                    route = StitchScreen.ClientRequestConfirmation.route,
                    arguments = listOf(navArgument("requestId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val reqId = backStackEntry.arguments?.getString("requestId") ?: "req-001"
                    val request = uiState.activeRequest ?: ServiceRequestDto(
                        id = reqId,
                        client_name = uiState.currentUser.full_name,
                        client_phone = uiState.currentUser.phone,
                        client_address = uiState.currentUser.saved_address,
                        professional_id = MainViewModel.DEFAULT_ROBERTO_PRO_ID,
                        professional_name = uiState.selectedProfessional?.full_name ?: "Roberto Carlos Gómez",
                        professional_phone = uiState.selectedProfessional?.phone ?: "5491145678901",
                        problem_description = "Pérdida de agua en la cocina"
                    )

                    RequestConfirmationScreen(
                        request = request,
                        onOpenWhatsApp = { req ->
                            val msg = "Hola ${req.professional_name.substringBefore(" ")}, te escribo desde Guía de Oficios. Soy ${req.client_name} (${req.client_address}). Necesito ayuda con: ${req.problem_description}"
                            launchWhatsApp(context, req.professional_phone, msg)
                        },
                        onCallProfessional = { req ->
                            launchPhoneDialer(context, req.professional_phone)
                        },
                        onGoToJobCompletedReview = { id ->
                            navController.navigate(StitchScreen.ClientJobCompletedReview.createRoute(id))
                        },
                        onBackHome = {
                            navController.navigate(StitchScreen.ClientHomeCategories.route)
                        }
                    )
                }

                // 8. Pantalla 8: Trabajo Terminado y Calificación Transaccional
                composable(
                    route = StitchScreen.ClientJobCompletedReview.route,
                    arguments = listOf(navArgument("requestId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val reqId = backStackEntry.arguments?.getString("requestId") ?: "req-001"
                    val proName = uiState.selectedProfessional?.full_name ?: "Roberto Carlos Gómez"

                    JobCompletedReviewScreen(
                        professionalName = proName,
                        authorName = uiState.currentUser.full_name,
                        onSubmitReview = { reviewInput ->
                            mainViewModel.submitReview(reqId, reviewInput) {
                                val proId = uiState.selectedProfessional?.id ?: MainViewModel.DEFAULT_ROBERTO_PRO_ID
                                navController.navigate(StitchScreen.ClientProfessionalProfile.createRoute(proId))
                            }
                        },
                        onLater = {
                            navController.navigate(StitchScreen.ClientHomeCategories.route)
                        }
                    )
                }

                // 9. Pantalla 9: Registro y Verificación DNI + Matrícula del Profesional
                composable(
                    route = StitchScreen.ProfessionalVerificationOnboarding.route,
                    arguments = listOf(navArgument("proId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val proId = backStackEntry.arguments?.getString("proId") ?: MainViewModel.DEFAULT_ROBERTO_PRO_ID
                    ProfessionalVerificationScreen(
                        professionalId = proId,
                        dniFrontUrl = uiState.uploadedDniFrontUrl,
                        dniBackUrl = uiState.uploadedDniBackUrl,
                        licenseDocUrl = uiState.uploadedLicenseDocUrl,
                        onUploadDocumentUri = { uri, category ->
                            mainViewModel.uploadMediaUri(uri, category)
                        },
                        onSubmitVerification = { verificationInput ->
                            mainViewModel.submitVerification(proId, verificationInput) {
                                navController.navigate(StitchScreen.ProfessionalDashboard.createRoute(proId))
                            }
                        },
                        onSkipToDashboard = {
                            mainViewModel.loadProfessionalDashboard(proId)
                            navController.navigate(StitchScreen.ProfessionalDashboard.createRoute(proId))
                        }
                    )
                }

                // 10. Pantalla 10: Panel del Profesional y Solicitudes Recibidas
                composable(
                    route = StitchScreen.ProfessionalDashboard.route,
                    arguments = listOf(navArgument("proId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val proId = backStackEntry.arguments?.getString("proId") ?: MainViewModel.DEFAULT_ROBERTO_PRO_ID
                    val dashboard = uiState.professionalDashboard ?: ProfessionalDashboardDto(
                        professional = ProfessionalDetailDto(
                            id = proId,
                            full_name = "Roberto Carlos Gómez",
                            trade_title = "Plomero y Gasista Matriculado",
                            neighborhood = "Caballito, Flores y Almagro",
                            is_available_now = true,
                            rating_avg = 4.9,
                            reviews_count = 128
                        ),
                        new_today_count = 3,
                        urgent_count = 1,
                        month_contacts = 18,
                        active_requests = emptyList()
                    )

                    ProfessionalDashboardScreen(
                        dashboard = dashboard,
                        onToggleAvailability = { available ->
                            mainViewModel.toggleProfessionalAvailability(proId, available)
                        },
                        onOpenVerificationScreen = {
                            navController.navigate(StitchScreen.ProfessionalVerificationOnboarding.createRoute(proId))
                        },
                        onReplyWhatsApp = { req ->
                            val msg = "Hola ${req.client_name.substringBefore(" ")}, soy ${dashboard.professional.full_name} de Guía de Oficios. Recibí tu consulta por: “${req.problem_description}”."
                            launchWhatsApp(context, req.client_phone, msg)
                        },
                        onCallNeighbor = { req ->
                            launchPhoneDialer(context, req.client_phone)
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary
                )
            }
        }
    }
}

private fun launchWhatsApp(context: Context, rawPhone: String, message: String) {
    val cleanPhone = rawPhone.filter { it.isDigit() }.let {
        if (it.startsWith("54")) it else "549$it"
    }
    val encodedMsg = URLEncoder.encode(message, "UTF-8")
    val uri = Uri.parse("https://wa.me/$cleanPhone?text=$encodedMsg")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun launchPhoneDialer(context: Context, rawPhone: String) {
    val cleanPhone = rawPhone.filter { it.isDigit() || it == '+' }
    val uri = Uri.parse("tel:$cleanPhone")
    val intent = Intent(Intent.ACTION_DIAL, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}
