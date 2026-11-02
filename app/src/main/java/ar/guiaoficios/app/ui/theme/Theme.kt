package ar.guiaoficios.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// =============================================================================
// Tokens Oficiales Exactos de Stitch (DESIGN.md — "Guía de Oficios")
// NO MODIFICAR: Respeta estrictamente la interfaz generada en Stitch
// =============================================================================

val Surface = Color(0xFFFAF8FF)
val SurfaceDim = Color(0xFFD2D9F4)
val SurfaceBright = Color(0xFFFAF8FF)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF2F3FF)
val SurfaceContainer = Color(0xFFEAEDFF)
val SurfaceContainerHigh = Color(0xFFE2E7FF)
val SurfaceContainerHighest = Color(0xFFDAE2FD)

val OnSurface = Color(0xFF131B2E)
val OnSurfaceVariant = Color(0xFF444653)
val Outline = Color(0xFF757684)
val OutlineVariant = Color(0xFFC4C5D5)

val Primary = Color(0xFF00288E)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF1E40AF)
val OnPrimaryContainer = Color(0xFFA8B8FF)
val PrimaryFixed = Color(0xFFDDE1FF)
val PrimaryFixedDim = Color(0xFFB8C4FF)

val Secondary = Color(0xFF006D30)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFF92F5A4)
val OnSecondaryContainer = Color(0xFF007233)
val SecondaryFixed = Color(0xFF95F8A7)
val SecondaryFixedDim = Color(0xFF79DB8D)
val OnSecondaryFixedVariant = Color(0xFF005323)
val WhatsAppActionGreen = Color(0xFF15803D)

val Tertiary = Color(0xFF532A00)
val TertiaryContainer = Color(0xFF743D00)
val TertiaryFixed = Color(0xFFFFDCC3)
val TertiaryFixedDim = Color(0xFFFFB77D)
val OnTertiaryFixed = Color(0xFF2F1500)

val ErrorColor = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

private val StitchColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    tertiaryContainer = TertiaryContainer,
    error = ErrorColor,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant
)

// Escala tipográfica exacta de DESIGN.md (Inter)
val StitchTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = OnSurface
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = OnSurface
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        color = OnSurface
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = OnSurface
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = OnSurface
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        color = OnSurface
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = OnSurface
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
)

@Composable
fun GuiaDeOficiosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StitchColorScheme,
        typography = StitchTypography,
        content = content
    )
}
