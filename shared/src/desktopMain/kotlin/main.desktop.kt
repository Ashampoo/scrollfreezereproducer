import androidx.compose.runtime.Composable

@Composable
fun MainView() = App(
    photoLoader = DesktopPhotoLoader,
    imageLoader = DesktopImageLoader
)
