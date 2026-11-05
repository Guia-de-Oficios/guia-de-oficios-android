package ar.guiaoficios.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.guiaoficios.app.ui.theme.*

/**
 * VISTA #1 STITCH (`[AND-1.2]`): Escudo e insignia oficial `GdoLogoBadge`
 * Respeta `logo_provisional_gdo/code.html`:
 * - Escudo circular azul cobalto (`#1E40AF`) con borde blanco sutil
 * - Ícono de llave inglesa blanca o siglas "GDO" en el centro
 * - Círculo esmeralda (`#15803D`) con tilde blanco en la esquina inferior derecha
 */
@Composable
fun GdoLogoBadge(
    modifier: Modifier = Modifier,
    badgeSize: Dp = 48.dp,
    showWrenchIcon: Boolean = false
) {
    Box(
        modifier = modifier.size(badgeSize),
        contentAlignment = Alignment.Center
    ) {
        // Círculo contenedor principal #1E40AF
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(PrimaryContainer)
                .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (showWrenchIcon) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Guía de Oficios",
                    tint = Color.White,
                    modifier = Modifier.size(badgeSize * 0.48f)
                )
            } else {
                Text(
                    text = "GDO",
                    color = Color.White,
                    fontSize = (badgeSize.value * 0.34f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }
        }
        // Insignia verde esmeralda #15803D de verificación en esquina inferior derecha
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(badgeSize * 0.36f)
                .clip(CircleShape)
                .background(WhatsAppActionGreen)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Verificado",
                tint = Color.White,
                modifier = Modifier.size(badgeSize * 0.22f)
            )
        }
    }
}

/**
 * Logo oficial completo de Guía de Oficios extraído de `logo_provisional_gdo/code.html`:
 * - `GdoLogoBadge` (#1E40AF + tilde #15803D)
 * - Texto de marca "Guía de Oficios" + subtítulo "RED VECINAL DE CONFIANZA"
 */
@Composable
fun GdoBrandLogo(
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    badgeSize: Dp = 44.dp,
    showWrenchIcon: Boolean = false
) {
    val content: @Composable () -> Unit = {
        GdoLogoBadge(
            badgeSize = badgeSize,
            showWrenchIcon = showWrenchIcon
        )

        if (centered) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Guía de Oficios",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary
                )
                Text(
                    text = "RED VECINAL DE CONFIANZA",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Guía de Oficios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary,
                    lineHeight = 20.sp
                )
                Text(
                    text = "RED VECINAL DE CONFIANZA",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    letterSpacing = 0.6.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }

    if (centered) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Ícono oficial de Google de 4 colores (#4285F4, #34A853, #FBBC05, #EA4335)
 * tal como aparece en el botón "Continuar con Google" de Stitch.
 */
@Composable
fun GoogleColorIcon(
    modifier: Modifier = Modifier.size(22.dp)
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.20f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(diameter, diameter)

        // Rojo superior (#EA4335)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = -145f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Amarillo izquierdo (#FBBC05)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 140f,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Verde inferior (#34A853)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 95f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Azul derecho (#4285F4)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -12f,
            sweepAngle = 58f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Barra horizontal de la G (#4285F4)
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.50f, size.height * 0.50f),
            end = Offset(size.width * 0.92f, size.height * 0.50f),
            strokeWidth = strokeWidth * 0.92f,
            cap = StrokeCap.Butt
        )
    }
}

/**
 * TopAppBar oficial de Stitch (`cliente_inicio_y_categor_as/code.html`):
 * - Botón Menú a la izquierda
 * - Logo GDO + "Guía de Oficios / RED VECINAL DE CONFIANZA"
 * - Botón "Mi cuenta / Cambiar" a la derecha
 */
@Composable
fun StitchTopAppBar(
    onMenuClick: () -> Unit,
    onActionClick: () -> Unit,
    actionLabel: String = "Cuenta"
) {
    Surface(
        color = SurfaceContainerLowest,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, SurfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Abrir menú",
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                GdoBrandLogo(badgeSize = 38.dp)
            }

            Surface(
                color = SurfaceContainerLow,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, OutlineVariant),
                modifier = Modifier.clickable { onActionClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = actionLabel,
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
        }
    }
}

/**
 * BottomNavigationBar oficial de Stitch (`cliente_inicio_y_categor_as/code.html`):
 * - Inicio (Home)
 * - Mis contactos (Handshake)
 * - Ayuda (HelpOutline)
 * - Mi cuenta (AccountCircle)
 */
@Composable
fun StitchBottomNavBar(
    selectedTab: String,
    onSelectTab: (String) -> Unit
) {
    Surface(
        color = SurfaceContainerLowest,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, SurfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StitchBottomNavItem(
                tabKey = "home",
                label = "Inicio",
                icon = Icons.Default.Home,
                selected = selectedTab == "home",
                onClick = { onSelectTab("home") },
                modifier = Modifier.weight(1f)
            )
            StitchBottomNavItem(
                tabKey = "contacts",
                label = "Mis contactos",
                icon = Icons.Default.Handshake,
                selected = selectedTab == "contacts",
                onClick = { onSelectTab("contacts") },
                modifier = Modifier.weight(1f)
            )
            StitchBottomNavItem(
                tabKey = "help",
                label = "Ayuda",
                icon = Icons.Default.HelpOutline,
                selected = selectedTab == "help",
                onClick = { onSelectTab("help") },
                modifier = Modifier.weight(1f)
            )
            StitchBottomNavItem(
                tabKey = "account",
                label = "Mi cuenta",
                icon = Icons.Default.AccountCircle,
                selected = selectedTab == "account",
                onClick = { onSelectTab("account") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StitchBottomNavItem(
    tabKey: String,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) SurfaceContainerLow else Color.Transparent
    val contentColor = if (selected) PrimaryContainer else OnSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1
        )
    }
}
