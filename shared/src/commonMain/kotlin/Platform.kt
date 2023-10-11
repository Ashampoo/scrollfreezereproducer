import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect val PLATFORM: Platform

enum class Platform(
    val displayName: String
) {

    WINDOWS("Windows"),
    MACOS("macOS"),
    ANDROID("Android"),
    IOS("iOS");

    fun isDesktop(): Boolean =
        this == WINDOWS || this == MACOS

}

@Composable
expect fun VerticalScrollbar(modifier: Modifier, scrollState: LazyGridState)

expect fun Modifier.pointerEnterExitFilter(
    onEnter: () -> Boolean,
    onExit: () -> Boolean
): Modifier