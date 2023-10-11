import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ashampoo.photos.shared.model.GridStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlin.math.max

/*
 * The first time the app gets launched we want to scroll to the bottom,
 * because the newest photos are located there.
 */
private var performInitialScroll: Boolean = true

@Composable
fun GalleryViewScrollView(
    photosToDisplay: ImmutableList<Photo>,
    settings: Settings,
    screenSize: Size,
    photoSelectionMode: Boolean,
    selectedPhotoIds: ImmutableSet<Long>,
    galleryViewScrollPosition: Double,
    imageLoader: ImageLoader,
    dispatch: (AppAction) -> Unit
) {

    /*
     * We track the visiblePhotos here and not in AppState because
     * firing of AppState events during scrolling seems to be too
     * expensive for a good scrolling performance on low-end Android devices.
     * This handling is similar to iOS where this also turned out to be a problem.
     */
    val visiblePhotosState = remember { mutableStateOf(emptyList<Photo>()) }

    /*
     * Define values that are the same for all generated GalleryPhotoView here,
     * because if the user scrolls fast through the gallery these get called
     * over and over again.
     */
    val tileSize = remember(settings) { settings.tileSize }
    val cropImage = remember(settings) { settings.gridStyle == GridStyle.TILE }
    val backgroundColor = MaterialTheme.colorScheme.surface

    BoxWithConstraints {

        /*
         * We need this state to track and modify scrolling positions.
         *
         * This must be in this Box so that the recomposition scope is
         * as small as possible. Scrolling will recompose here.
         */
        val lazyGridState = rememberLazyGridState()

        /* Information if the LazyList has already been placed (and therefore measured). */
        val lazyGridPlaced = remember { mutableStateOf(false) }

        /*
         * We calculate the correct tileSize here once when the screen size changes
         * to reduce expensive .aspectRatio() calls.
         */
        val tileSizePt: Dp = remember(maxWidth, tileSize, screenSize) {
            calcTileSizePt(maxWidth, tileSize, screenSize)
        }

        val cells: GridCells = remember(tileSize, screenSize) {
            calcCells(tileSize, screenSize)
        }

        LazyVerticalGrid(
            columns = cells,
            state = lazyGridState,
            modifier = minimalPaddingModifier.onPlaced {
                lazyGridPlaced.value = true
            }
        ) {

            items(photosToDisplay, key = { it.getIdNotNull() }) { photo ->

                /*
                 * Use an onAppear()/onDisappear() mechanic similar to
                 * what we do on iOS to track currently visible photos.
                 */
                DisposableEffect(true) {

                    visiblePhotosState.value += photo

                    onDispose {

                        visiblePhotosState.value -= photo
                    }
                }

                /**
                 * As calling contains() in Swift is slow a preceding isNotEmpty()
                 * should help with the most common case that nothing is selected.
                 */
                val selected =
                    selectedPhotoIds.isNotEmpty() && selectedPhotoIds.contains(photo.getIdNotNull())

                GalleryPhotoView(
                    dispatch = dispatch,
                    photo = photo,
                    photoSelectionMode = photoSelectionMode,
                    imageLoader = imageLoader,
                    cropImage = cropImage,
                    selected = selected,
                    size = tileSizePt,
                    backgroundColor = backgroundColor
                )
            }
        }

        VerticalScrollbar(
            modifier = remember { fillWholeMaxHeightModifier.align(Alignment.CenterEnd) },
            scrollState = lazyGridState
        )

        LaunchEffectsForSavingAndRestoringScrollState(
            galleryViewScrollPosition = galleryViewScrollPosition,
            sortOrder = settings.sortOrder,
            lazyGridState = lazyGridState,
            lazyGridPlaced = lazyGridPlaced,
            dispatch = dispatch
        )
    }
}

@Composable
private fun LaunchEffectsForSavingAndRestoringScrollState(
    galleryViewScrollPosition: Double,
    sortOrder: SortOrder,
    lazyGridState: LazyGridState,
    lazyGridPlaced: MutableState<Boolean>,
    dispatch: (AppAction) -> Unit
) {

    /**
     * LaunchedEffect with key = "true" fires this once the Composable
     * is first shown after it was disposed. This is true if we come
     * back from another tab or the hero view. Only then we want to scroll.
     */
    LaunchedEffect(true) {

        /*
         * If the LazyList we want to scroll on is not yet placed and
         * measured a call to scrollToItem() will throw an exception.
         */
        if (!lazyGridPlaced.value)
            return@LaunchedEffect

        /**
         * We saved both integers into one double. See the comment above.
         */
        val positionArray = galleryViewScrollPosition.toString().split(".")

        val firstVisibleItemIndex = positionArray[0].toInt()
        val firstVisibleItemScrollOffset = positionArray[1].toInt()

        if (performInitialScroll) {

            /*
             * New photos are at the bottom.
             * So we want to scroll there if we sort ascending.
             */
            if (sortOrder == SortOrder.ASCENDING)
                lazyGridState.scrollToBottom()

            performInitialScroll = false

        } else {

            /**
             * Scroll silently without animation because the user should never
             * know that we actually reloaded the whole list he came just back to.
             */
            lazyGridState.scrollToItem(
                index = firstVisibleItemIndex,
                scrollOffset = firstVisibleItemScrollOffset
            )
        }
    }

    /**
     * We use this DisposableEffect to send an update for
     * the scroll position once every time the user leaves
     * the GalleryView behind.
     *
     * This is of course more efficient than firing an event
     * everytime the view is scrolled. We don't need that.
     */
    DisposableEffect(true) {

        onDispose {

            /**
             * We want to save the scroll position as a double to be
             * in line with what SwiftUI does. So this magic double
             * needs to be split on the dot to get the two integers back.
             */
            val scrollPosition = (
                    "${lazyGridState.firstVisibleItemIndex}." +
                            "${lazyGridState.firstVisibleItemScrollOffset}"
                    ).toDouble()

            dispatch(AppAction.GalleryViewScrolled(scrollPosition))
        }
    }
}

private fun calcTileSizePt(
    maxWidth: Dp,
    tileSize: TileSize,
    screenSize: Size
): Dp {

    val gridWidth: Dp = maxWidth - minimalSpacing.times(2)

    val itemsPerRow = if (PLATFORM.isDesktop())
        (gridWidth / tileSize.desktopSizePt).value.toInt()
    else if (screenSize.height > screenSize.width)
        tileSize.itemsPerRowPortrait
    else
        tileSize.itemsPerRowLandscape

    return gridWidth / itemsPerRow
}

private fun calcCells(
    tileSize: TileSize,
    screenSize: Size
): GridCells {

    return if (PLATFORM.isDesktop()) {

        GridCells.Adaptive(tileSize.desktopSizePt.dp)

    } else {

        /*
         * Use a fixed grid for smartphones and tablets.
         */

        val columnCount =
            if (screenSize.height > screenSize.width)
                tileSize.itemsPerRowPortrait
            else
                tileSize.itemsPerRowLandscape

        GridCells.Fixed(count = columnCount)
    }
}

private suspend fun LazyGridState.scrollToBottom() = this.scrollToItem(
    index = max(0, this.layoutInfo.totalItemsCount - 1),
    scrollOffset = 0
)
