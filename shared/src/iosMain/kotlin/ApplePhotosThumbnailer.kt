

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Photos.PHAsset
import platform.Photos.PHImageContentModeAspectFit
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeHighQualityFormat
import platform.Photos.PHImageRequestOptionsResizeModeExact
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

object ApplePhotosThumbnailer {

    private val imageManager = PHImageManager.defaultManager()

    private val thumbnailImageRequestOptions = PHImageRequestOptions().apply {

        /*
         * High-quality images
         */
        deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat

        /*
         * Resizes
         */
        resizeMode = PHImageRequestOptionsResizeModeExact

        /*
         * Photos on iCloud are not really on the device and must be
         * downloaded if accessed. This is why we need network access.
         */
        networkAccessAllowed = true

        /*
         * Should wait for the result handler.
         * We do async computation in other ways, so this shouldn't be a problem.
         */
        synchronous = true
    }

    @OptIn(ExperimentalForeignApi::class)
    private val thumbnailSize = CGSizeMake(
        width = THUMBNAIL_LONG_SITE_PX.toDouble(),
        height = THUMBNAIL_LONG_SITE_PX.toDouble()
    )

    @OptIn(ExperimentalForeignApi::class)
    @Throws(Exception::class)
    fun createThumbnailBytes(uri: String): ByteArray {

        val asset: PHAsset = fetchAsset(uri) ?: error("Asset $uri not found.")

        var image: UIImage? = null

        imageManager.requestImageForAsset(
            asset = asset,
            targetSize = thumbnailSize,
            contentMode = PHImageContentModeAspectFit,
            options = thumbnailImageRequestOptions
        ) { result, _ ->

            image = result
        }

        val nonNullImage = checkNotNull(image) { "Failed to load asset $uri" }

        val data = UIImageJPEGRepresentation(
            image = nonNullImage,
            compressionQuality = 0.75
        ) ?: error("Failed to create JPEG for asset $uri")

        return DataByteArrayConverter.convertDataToByteArray(data)
    }

    private fun fetchAsset(uri: String): PHAsset? =
        PHAsset.fetchAssetsWithLocalIdentifiers(
            identifiers = listOf(uri),
            options = null
        ).firstObject as? PHAsset
}

@Suppress("unused")
object DataByteArrayConverter {

    @OptIn(ExperimentalForeignApi::class)
    fun convertDataToByteArray(data: NSData): ByteArray {

        return ByteArray(data.length.toInt()).apply {
            usePinned {
                memcpy(
                    __dst = it.addressOf(0),
                    __src = data.bytes,
                    __n = data.length
                )
            }
        }
    }
}