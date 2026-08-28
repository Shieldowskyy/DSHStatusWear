package yt.dsh.statuswear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val DshColors = Colors(
    primary = StatusUp,
    primaryVariant = StatusUp,
    secondary = StatusPending,
    secondaryVariant = StatusPending,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = StatusDown,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = TextPrimary
)

@Composable
fun DshStatusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = DshColors,
        content = content
    )
}
