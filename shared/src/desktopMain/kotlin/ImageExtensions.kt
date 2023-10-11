import org.jetbrains.skia.*

private val highQualityPaint = Paint().apply {
    isAntiAlias = true
}

fun Image.encodeToJpg(quality: Int): ByteArray {

    val jpgData = encodeToData(EncodedImageFormat.JPEG, quality)

    requireNotNull(jpgData) { "Encoding skia.Image to JPG failed." }

    return jpgData.bytes
}

fun Image.scale(longSidePx: Int): Image {

    require(longSidePx > 0) { "Parameter 'longSidePx' must be positive, but was $longSidePx." }
    require(width > 0 && height > 0) { "Image has illegal dimensions: $width x $height" }

    val originalSize = Size(width, height)
    val thumbnailSize = originalSize.toScaledSize(longSidePx)

    check(thumbnailSize.width > 0 && thumbnailSize.height > 0) {
        "Illegal dimensions $thumbnailSize after scaling $width x $height to $longSidePx."
    }

    val surface = Surface.makeRasterN32Premul(
        thumbnailSize.width,
        thumbnailSize.height
    )

    surface.canvas.drawImageRect(
        this,
        Rect(0f, 0f, thumbnailSize.width.toFloat(), thumbnailSize.height.toFloat()),
        highQualityPaint
    )

    return surface.makeImageSnapshot()
}