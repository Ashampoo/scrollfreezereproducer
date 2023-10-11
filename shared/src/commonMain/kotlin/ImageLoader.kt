import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap

/*
 * ImageLoader, marked as stable for Compose, because it does not
 * have any state and will never change during runtime.
 */
@Stable
interface ImageLoader {

    /*
     * Declare Throws to prevent crashes on Apple
     */
    @Throws(Throwable::class)
    suspend fun loadThumbnailImage(photo: Photo): ImageBitmap?

}