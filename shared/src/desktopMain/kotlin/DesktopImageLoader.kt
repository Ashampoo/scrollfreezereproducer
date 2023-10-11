import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.io.File

object DesktopImageLoader : ImageLoader {

    override suspend fun loadThumbnailImage(photo: Photo): ImageBitmap? {

        val bytes = File(photo.uri).readBytes()

        val image = Image.makeFromEncoded(bytes)

        /* If the image is too large SKIKO will load invalid images. */
        if (image.width == 0 || image.height == 0)
            return null

        val thumbnailImage = image.scale(THUMBNAIL_LONG_SITE_PX)

        return thumbnailImage.toComposeImageBitmap()
    }
}