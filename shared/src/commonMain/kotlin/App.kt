import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import com.ashampoo.photos.shared.model.GridStyle
import kotlinx.collections.immutable.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val THUMBNAIL_LONG_SITE_PX: Int = 180 * 2

val settings = Settings(
    tileSize = TileSize.SMALL,
    gridStyle = GridStyle.SQUARE,
    sortOrder = SortOrder.ASCENDING
)

@Composable
fun App(
    photoLoader: PhotoLoader,
    imageLoader: ImageLoader
) {

    MaterialTheme {

        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = fillWholeMaxSizeModifier
        ) {

            val screenSize = Size(maxWidth.value.toInt(), maxHeight.value.toInt())

            val photosToDisplay = produceState<ImmutableList<Photo>?>(null) {

                withContext(Dispatchers.Default) {
                    value = photoLoader.findAllPhotos().toPersistentList()
                }
            }

            val photosToDisplayValue = photosToDisplay.value

            if (photosToDisplayValue == null) {

                Text("Loading photos...")

            } else if (photosToDisplayValue.isEmpty()) {

                Text("No photos found!")

            } else {

                GalleryViewScrollView(
                    photosToDisplay = photosToDisplayValue,
                    settings = settings,
                    screenSize = screenSize,
                    photoSelectionMode = false,
                    selectedPhotoIds = persistentSetOf(),
                    galleryViewScrollPosition = 0.0,
                    imageLoader = imageLoader,
                    dispatch = {
                        println("Received action: $it")
                    }
                )
            }
        }
    }
}
