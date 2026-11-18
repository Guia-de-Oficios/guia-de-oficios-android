package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.ServiceRequestDto
import ar.guiaoficios.app.ui.theme.*

/**
 * Pestaña 'Mis Contactos' del BottomNavBar (`[AND-4.2]`).
 * Conectada a GET /api/v1/requests/my para que el vecino vea su historial real
 * de solicitudes pendientes, en camino y finalizadas (corrige BUG-02).
 */
@Composable
fun MyRequestsHistoryScreen(
    requests: List<ServiceRequestDto> = emptyList(),
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    onContactWhatsApp: (ServiceRequestDto) -> Unit = {},
    onCallProfessional: (ServiceRequestDto) -> Unit = {},
    onOpenTrackingMap: (ServiceRequestDto) -> Unit = {},
    onOpenReview: (ServiceRequestDto) -> Unit = {},
    onBackHome: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("all") }

    val filteredRequests = remember(requests, selectedFilter) {
        when (selectedFilter) {
            "active" -> requests.filter {
                it.status.equals("pending", ignoreCase = true) ||
                    it.status.equals("contacted", ignoreCase = true) ||
                    it.status.equals("en_route", ignoreCase = true) ||
                    it.status.equals("in_progress", ignoreCase = true)
            }
            "completed" -> requests.filter { it.status.equals("completed", ignoreCase = true) }
            else -> requests
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mis Contactos y Solicitudes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Historial real de profesionales que contactaste (${requests.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar historial",
                        tint = Primary
                    )
                }
            }
        }

        item {
            val activeCount = requests.count { !it.status.equals("completed", ignoreCase = true) }
            val completedCount = requests.count { it.status.equals("completed", ignoreCase = true) }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = {
                            Text("Todos (${requests.size})", fontWeight = FontWeight.Bold)
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "active",
                        onClick = { selectedFilter = "active" },
                        label = {
                            Text("En curso ($activeCount)", fontWeight = FontWeight.Bold)
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "completed",
                        onClick = { selectedFilter = "completed" },
                        label = {
                            Text("Finalizados ($completedCount)", fontWeight = FontWeight.Bold)
                        }
                    )
                }
            }
        }

        if (filteredRequests.isEmpty() && !isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    border = BorderStroke(1.dp, OutlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = PrimaryContainer,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Text(
                            text = if (requests.isEmpty()) {
                                "Todavía no realizaste solicitudes"
                            } else {
                                "No hay solicitudes en esta categoría"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "Cuando contactes a un profesional verificado de tu barrio, vas a poder hacer seguimiento y calificar el trabajo desde acá.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                        Button(
                            onClick = {
                                if (requests.isEmpty()) {
                                    onBackHome()
                                } else {
                                    selectedFilter = "all"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (requests.isEmpty()) {
                                    "Buscar un profesional en mi barrio"
                                } else {
                                    "Ver todas mis solicitudes"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredRequests, key = { it.id }) { req ->
                ServiceRequestHistoryCard(
                    req = req,
                    onContactWhatsApp = { onContactWhatsApp(req) },
                    onCallProfessional = { onCallProfessional(req) },
                    onOpenTrackingMap = { onOpenTrackingMap(req) },
                    onOpenReview = { onOpenReview(req) }
                )
            }
        }
    }
}

@Composable
private fun ServiceRequestHistoryCard(
    req: ServiceRequestDto,
    onContactWhatsApp: () -> Unit,
    onCallProfessional: () -> Unit,
    onOpenTrackingMap: () -> Unit,
    onOpenReview: () -> Unit
) {
    val isUrgent = req.urgency.equals("urgente", ignoreCase = true)
    val isCompleted = req.status.equals("completed", ignoreCase = true)
    val isEnRoute = req.status.equals("en_route", ignoreCase = true)

    val (statusLabel, statusBg, statusFg) = when {
        isCompleted -> Triple("Finalizado", Color(0xFFDCFCE7), Color(0xFF166534))
        isEnRoute -> Triple("En camino", Color(0xFFDBEAFE), Color(0xFF1E40AF))
        else -> Triple("Pendiente", Color(0xFFFEF3C7), Color(0xFF92400E))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(
            width = if (isEnRoute) 2.dp else 1.dp,
            color = if (isEnRoute) PrimaryContainer else OutlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = req.professional_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )
                    if (req.professional_license.isNotBlank()) {
                        Text(
                            text = req.professional_license,
                            style = MaterialTheme.typography.labelMedium,
                            color = Secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusFg
                        )
                    }
                    if (req.time_ago_label.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = req.time_ago_label,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            Surface(
                color = if (isUrgent) Color(0xFFFEF2F2) else SurfaceContainerLow,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isUrgent) ErrorColor.copy(alpha = 0.4f) else OutlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isUrgent) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isUrgent) ErrorColor else PrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isUrgent) "Urgente hoy" else "Solicitud normal",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUrgent) ErrorColor else PrimaryContainer
                    )
                }
            }

            Text(
                text = req.problem_description,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface
            )

            if (req.client_address.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = req.client_address,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onContactWhatsApp,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppActionGreen)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onCallProfessional,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, PrimaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = PrimaryContainer, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Llamar", fontWeight = FontWeight.Bold, color = PrimaryContainer)
                }
            }

            if (!isCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenTrackingMap,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, Primary)
                    ) {
                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Seguir en vivo", fontWeight = FontWeight.Bold, color = Primary)
                    }
                    Button(
                        onClick = onOpenReview,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calificar trabajo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
