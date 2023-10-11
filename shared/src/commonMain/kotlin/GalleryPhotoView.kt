import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

@Suppress("LongParameterList", "kotlin:S107")
@Composable
fun GalleryPhotoView(
    dispatch: (AppAction) -> Unit,
    photo: Photo,
    photoSelectionMode: Boolean,
    imageLoader: ImageLoader,
    cropImage: Boolean,
    selected: Boolean,
    size: Dp,
    backgroundColor: Color
) {

    val painterState = produceThumbnailBitmapPainter(photo, imageLoader)

    val painter = painterState.value

    if (painter == null) {

        /*
         * Show a placeholder while loading the image.
         *
         * If the thumbnail is broken and can't be loaded,
         * at least the image should be clickable.
         */
        PlaceholderBox(dispatch, photo, size, backgroundColor, photoSelectionMode)

        return
    }

    GalleryPhotoBox(
        dispatch,
        photo,
        painter,
        size,
        backgroundColor,
        cropImage,
        selected,
        photoSelectionMode
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaceholderBox(
    dispatch: (AppAction) -> Unit,
    photo: Photo,
    size: Dp,
    backgroundColor: Color,
    photoSelectionMode: Boolean
) {

    val modifier = remember(size, backgroundColor) {

        Modifier
            .size(size)
            .minimalPadding()
            .background(backgroundColor)
    }

    Box(
        modifier = modifier
            .combinedClickable(
                onClick = {

                    if (photoSelectionMode)
                        dispatch(AppAction.SelectPhoto(photo.getIdNotNull()))
                    else
                        dispatch(AppAction.OpenPhoto(photo))
                },
                onLongClick = {

                    dispatch(AppAction.SelectPhoto(photo.getIdNotNull()))
                }
            )
    )
}

/* Own Composable to better profile it. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@Suppress("LongParameterList", "kotlin:S107")
private fun GalleryPhotoBox(
    dispatch: (AppAction) -> Unit,
    photo: Photo,
    painter: Painter,
    size: Dp,
    backgroundColor: Color,
    cropImage: Boolean,
    selected: Boolean,
    photoSelectionMode: Boolean
) {

    val modifier = remember(size, backgroundColor) {

        Modifier
            .size(size)
            .minimalPadding()
            .background(backgroundColor)
    }

    val hovered = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .hoverListener(hovered)
            .testTag(photo.id.toString())
            .combinedClickable(
                onClick = {

                    if (photoSelectionMode)
                        dispatch(AppAction.SelectPhoto(photo.getIdNotNull()))
                    else
                        dispatch(AppAction.OpenPhoto(photo))
                },
                onLongClick = {

                    dispatch(AppAction.SelectPhoto(photo.getIdNotNull()))
                }
            )
    ) {

        GalleryPhotoImage(painter, cropImage)

        if (selected)
            SelectioMakerBox(backgroundColor)

//        if (photoSelectionMode)
//            SelectionModeMarkers(selected, hovered)
    }
}

/* Own Composable to better profile it. */
@Composable
private fun GalleryPhotoImage(
    painter: Painter,
    cropImage: Boolean
) {

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = if (cropImage) ContentScale.Crop else ContentScale.Fit,
        modifier = fillWholeMaxSizeModifier // <-- to grow cropped images if too small
    )
}

@Composable
private fun SelectioMakerBox(
    backgroundColor: Color
) {

    Box(
        modifier = fillWholeMaxSizeModifier
            .border(3.dp, MaterialTheme.colorScheme.secondary)
            .border(5.dp, backgroundColor)
    )
}

@Composable
fun produceThumbnailBitmapPainter(
    photo: Photo?,
    imageLoader: ImageLoader
) = produceState<BitmapPainter?>(initialValue = null, photo) {
    withContext(Dispatchers.Default) {
        value = createThumbnailImagePainter(photo, imageLoader)
    }
}

private suspend fun createThumbnailImagePainter(
    photo: Photo?,
    imageLoader: ImageLoader
): BitmapPainter? {

    if (photo == null)
        return null

    try {

        /* Check for fast scrolling through the gallery. */
        if (!coroutineContext.isActive)
            return null

        /* Additional yield() for safety. */
        yield()

        val image = imageLoader.loadThumbnailImage(photo)
            ?: return null

        /* Check again if it's really needed anymore. */
        if (!coroutineContext.isActive)
            return null

        /* Additional yield() for safety. */
        yield()

        return BitmapPainter(
            image = image,
            filterQuality = FilterQuality.Low // <-- to render thumbnails faster
        )

    } catch (ignore: CancellationException) {

        /*
         * In case of a cancellation we don't need to log.
         * The user scrolled just very fast and this image is not needed anymore.
         */
        return null

    } catch (ex: Throwable) {

        /*
         * Catch for Throwable to include OutOfMemoryError.
         * This situation should not crash the render process.
         */

        return null
    }
}
