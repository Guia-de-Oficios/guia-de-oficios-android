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
 * VISTA #11 STITCH: `cliente_profesional_en_camino_y_mapa_en_vivo/code.html`
 * Archivo modular reservado para el Paso 11 (Seguimiento en vivo con Mapa y ETA).
 */
@Composable
fun ProfessionalEnRouteMapScreen(
    request: ServiceRequestDto? = null,
    onCallProfessional: () -> Unit = {},
    onWhatsAppProfessional: () -> Unit = {},
    onCompleteJobAndReview: () -> Unit = {},
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
            text = "Seguimiento en Vivo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBack) {
            Text("Volver")
        }
    }
}
