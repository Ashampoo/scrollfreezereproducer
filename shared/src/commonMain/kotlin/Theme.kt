import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val defaultSpacing: Dp = 8.dp
val minimalSpacing: Dp = 1.dp
val fillWholeMaxSizeModifier: Modifier = Modifier.fillMaxSize()
val fillWholeMaxHeightModifier: Modifier = Modifier.fillMaxHeight()
val minimalPaddingModifier: Modifier = Modifier.padding(minimalSpacing)
val defaultPaddingModifier: Modifier = Modifier.padding(defaultSpacing)

fun Modifier.minimalPadding(): Modifier = composed { minimalPaddingModifier }

fun Modifier.hoverListener(hovered: MutableState<Boolean>): Modifier =
    this.pointerEnterExitFilter(
        onEnter = {
            hovered.value = true
            return@pointerEnterExitFilter true
        },
        onExit = {
            hovered.value = false
            return@pointerEnterExitFilter true
        }
    )