package com.mnfarzaneh.taskflow.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary            = Matcha600,
    onPrimary          = Color.White,
    primaryContainer   = Matcha100,
    onPrimaryContainer = Matcha800,
    secondary          = Matcha500,
    onSecondary        = Color.White,
    secondaryContainer = Matcha50,
    surface            = Color(0xFFFBFCF7),
    surfaceVariant     = Color(0xFFEFF3E5),
    onSurface          = Color(0xFF1F231A),
    onSurfaceVariant   = Color(0xFF4A5040),
    outline            = Color(0xFF7A8070),
    background         = BgGradientTop,
    onBackground       = Color(0xFF1F231A),
    error              = DeadlineDanger,
    errorContainer     = Color(0xFFFAE2DF),
    onErrorContainer   = Color(0xFF4A150F)
)

@Composable
fun TaskFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}