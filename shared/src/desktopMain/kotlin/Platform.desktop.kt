import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

actual val PLATFORM = Platform.WINDOWS

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.pointerEnterExitFilter(
    onEnter: () -> Boolean,
    onExit: () -> Boolean
): Modifier = this
    .onPointerEvent(PointerEventType.Enter) {
        onEnter()
    }
    .onPointerEvent(PointerEventType.Exit) {
        onExit()
    }

private const val ALPHA = 0.8F

@Composable
@Suppress("OptionalUnit")
actual fun VerticalScrollbar(
    modifier: Modifier,
    scrollState: LazyGridState,
): Unit = androidx.compose.foundation.VerticalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    style = defaultScrollbarStyle().copy(
        unhoverColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ALPHA),
        hoverColor = MaterialTheme.colorScheme.secondary
    ),
    modifier = modifier
)