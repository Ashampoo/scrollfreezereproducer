import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

object AppleImageLoader : ImageLoader {

    override suspend fun loadThumbnailImage(photo: Photo): ImageBitmap? {

        val bytes = ApplePhotosThumbnailer.createThumbnailBytes(photo.uri)

        val image = Image.makeFromEncoded(bytes).toComposeImageBitmap()

        /* If the image is too large SKIKO will load invalid images. */
        if (image.width == 0 || image.height == 0)
            return null

        return image
    }
}