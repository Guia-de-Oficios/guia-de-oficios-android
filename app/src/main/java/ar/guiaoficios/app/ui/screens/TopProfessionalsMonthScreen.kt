package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.ProfessionalSummaryDto
import ar.guiaoficios.app.ui.theme.*

/**
 * VISTA #7 STITCH: `comunidad_top_profesionales_del_mes/code.html`
 * Archivo modular reservado para el Paso 8 (Podio Barrial #1 Oro, #2 Plata y #3 Bronce).
 */
@Composable
fun TopProfessionalsMonthScreen(
    neighborhood: String = "Caballito, CABA",
    professionals: List<ProfessionalSummaryDto> = emptyList(),
    onSelectProfessional: (String) -> Unit = {},
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
            text = "Top Profesionales del Mes · $neighborhood",
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
