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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.style.TextDirection
import com.mnfarzaneh.taskflow.ui.chain.matchaTextFieldColors

@Composable
fun PersianOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier,
        label         = label,
        placeholder   = placeholder,
        singleLine    = singleLine,
        minLines      = minLines,
        maxLines      = maxLines,
        keyboardOptions = keyboardOptions,
        enabled       = enabled,
        textStyle     = androidx.compose.material3.LocalTextStyle.current.copy(
            textDirection = TextDirection.Rtl
        ),
        colors = matchaTextFieldColors()
    )
}
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