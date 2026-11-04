package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.data.model.UserDto
import ar.guiaoficios.app.ui.theme.*

/**
 * Vista de "Mi cuenta" accesible desde el BottomNavBar oficial de Stitch:
 * Muestra los datos reales del usuario autenticado en PostgreSQL, permite cambiar dirección,
 * ir al panel profesional (si corresponde) y cerrar sesión de forma limpia.
 */
@Composable
fun UserAccountScreen(
    user: UserDto,
    onUpdateAddress: (String) -> Unit,
    onOpenProfessionalPanel: () -> Unit,
    onLogout: () -> Unit
) {
    var showAddressDialog by remember { mutableStateOf(false) }
    var addressDraft by remember(user.saved_address) { mutableStateOf(user.saved_address) }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            containerColor = SurfaceContainerLowest,
            title = { Text("Actualizar tu dirección", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = addressDraft,
                    onValueChange = { addressDraft = it },
                    label = { Text("Calle, altura y barrio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addressDraft.isNotBlank()) {
                            onUpdateAddress(addressDraft.trim())
                        }
                        showAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.full_name.trim().take(1).uppercase().ifBlank { "U" },
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.full_name.ifBlank { "Usuario Registrado" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSurface
                            )
                            Surface(
                                color = if (user.role == "professional") SecondaryFixed else SurfaceContainerLow,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = if (user.role == "professional") "Perfil Profesional de Oficio" else "Vecino / Cliente Verificado",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (user.role == "professional") OnSecondaryFixedVariant else PrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = SurfaceContainer)

                    AccountDetailRow(
                        icon = Icons.Default.Mail,
                        label = "Correo electrónico",
                        value = user.email.ifBlank { "Sin especificar" }
                    )
                    AccountDetailRow(
                        icon = Icons.Default.Call,
                        label = "Teléfono celular / WhatsApp",
                        value = user.phone.ifBlank { "Sin especificar" }
                    )
                    AccountDetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Dirección guardada",
                        value = user.saved_address.ifBlank { "Av. Rivadavia 5420, Caballito" }
                    )

                    OutlinedButton(
                        onClick = { showAddressDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, OutlineVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditLocationAlt,
                            contentDescription = null,
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cambiar mi dirección",
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                }
            }
        }

        if (user.role == "professional") {
            item {
                Button(
                    onClick = onOpenProfessionalPanel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.Handyman, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Abrir mi Panel de Profesional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, ErrorColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ErrorContainer.copy(alpha = 0.25f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = ErrorColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Cerrar sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ErrorColor
                )
            }
        }
    }
}

@Composable
private fun AccountDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface
            )
        }
    }
}
