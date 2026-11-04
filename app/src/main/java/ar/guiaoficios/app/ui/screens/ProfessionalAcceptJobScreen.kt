package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.ServiceRequestDto
import ar.guiaoficios.app.ui.theme.*

/**
 * VISTA #15 STITCH: `profesional_aceptar_trabajo_e_iniciar_viaje/code.html`
 * Archivo modular reservado para el Paso 14 (Aceptar Trabajo e Iniciar Viaje).
 */
@Composable
fun ProfessionalAcceptJobScreen(
    request: ServiceRequestDto? = null,
    onStartTrip: () -> Unit = {},
    onOpenMaps: () -> Unit = {},
    onWhatsAppClient: () -> Unit = {},
    onRejectRequest: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Aceptar Trabajo e Iniciar Viaje",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBack) {
            Text("Volver al Panel")
        }
    }
}
