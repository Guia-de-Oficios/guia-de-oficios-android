package ar.guiaoficios.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ar.guiaoficios.app.ui.components.GdoBrandLogo
import ar.guiaoficios.app.ui.theme.*

/**
 * VISTA #8 STITCH: `gu_a_de_oficios_servicios_y_oficios_vecinales/code.html`
 * Selector inicial de rol con:
 * - Pill superior 'Buenos Aires y Gran Buenos Aires'
 * - Logo oficial GdoBrandLogo / GdoLogoBadge
 * - Título 'Encontrá profesionales de confianza cerca de tu casa'
 * - Pregunta '¿Cómo querés usar Guía de Oficios hoy?'
 * - 2 Tarjetas Radio de 96dp ('Necesito un oficio para mi casa' vs 'Ofrezco mis servicios de oficio')
 * - Botón 'Comenzar ->' de 56dp que pre-selecciona el rol para el registro
 * - Enlace '¿Ya tenés una cuenta? Iniciar sesión'
 * - Sello 'Red vecinal protegida y verificada'
 */
@Composable
fun RoleSelectionScreen(
    initialRole: String = "",
    onStartWithRole: (selectedRole: String) -> Unit,
    onGoToLogin: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(initialRole) }
    var roleError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Pill Ubicación + Logo Oficial GDO
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Gran Mendoza · Luján · AMBA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    GdoBrandLogo(centered = true, badgeSize = 56.dp)
                }
            }

            // 2. Título de Bienvenida y Propuesta de Valor
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Encontrá profesionales de confianza cerca de tu casa",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Sin complicaciones, sin registros difíciles y con personas reales de tu barrio verificadas con DNI y oficio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 3. Módulo Selector de Rol (2 Tarjetas Radio de 96dp)
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿Cómo querés usar Guía de Oficios hoy?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    LargeRoleRadioCard(
                        title = "Necesito un oficio para mi casa",
                        subtitle = "Busco plomeros, electricistas, gasistas y más en mi barrio.",
                        icon = Icons.Default.Home,
                        selected = selectedRole == "client",
                        onClick = {
                            selectedRole = "client"
                            roleError = null
                        }
                    )

                    LargeRoleRadioCard(
                        title = "Ofrezco mis servicios de oficio",
                        subtitle = "Quiero recibir solicitudes de vecinos de mi zona.",
                        icon = Icons.Default.Handyman,
                        selected = selectedRole == "professional",
                        onClick = {
                            selectedRole = "professional"
                            roleError = null
                        }
                    )

                    if (roleError != null) {
                        Text(
                            text = roleError!!,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = ErrorColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 4. Footer con CTA 56dp, enlace a Login y Sello de Confianza
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (selectedRole.isBlank()) {
                        roleError = "Seleccioná una opción arriba (Vecino o Profesional) para comenzar."
                    } else {
                        onStartWithRole(selectedRole)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer)
            ) {
                Text(
                    text = "Comenzar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            TextButton(onClick = onGoToLogin) {
                Text(
                    text = "¿Ya tenés una cuenta? Iniciar sesión",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    textDecoration = TextDecoration.Underline
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Red vecinal protegida y verificada",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSecondaryFixedVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LargeRoleRadioCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) PrimaryContainer else OutlineVariant
    val iconBoxBg = if (selected) Color(0xFFDBEAFE) else SurfaceContainerLow
    val iconBoxBorder = if (selected) Color(0xFFBFDBFE) else OutlineVariant
    val iconTint = if (selected) PrimaryContainer else OnSurfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLowest,
        border = BorderStroke(2.dp, borderColor),
        shadowElevation = if (selected) 3.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBoxBg)
                    .border(1.dp, iconBoxBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) PrimaryContainer else OnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            // Radio button indicator
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = 2.dp,
                        color = if (selected) PrimaryContainer else OutlineVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                    )
                }
            }
        }
    }
}
