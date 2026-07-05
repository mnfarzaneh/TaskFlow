package com.mnfarzaneh.taskflow.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

// ── پس‌زمینه اصلی اپ — گرادیان سبز ماچا ──────────────────
@Composable
fun GlassBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BgGradientTop,
                        BgGradientMid,
                        BgGradientBottom
                    )
                )
            )
    ) {
        content()
    }
}
// ── Modifier برای افکت شیشه‌ای روی هر Card/Box ──────────
fun Modifier.glassEffect(
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp,
    borderColor: androidx.compose.ui.graphics.Color = GlassBorder,
    backgroundColor: androidx.compose.ui.graphics.Color = GlassWhite
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(
        width = 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(cornerRadius)
    )

// ── نسخه matcha tint برای کارت‌های accent ───────────────
fun Modifier.glassMatchaEffect(
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.linearGradient(
            colors = listOf(GlassMatcha, GlassWhite)
        )
    )
    .border(
        width = 1.dp,
        color = Matcha200.copy(alpha = 0.5f),
        shape = RoundedCornerShape(cornerRadius)
    )