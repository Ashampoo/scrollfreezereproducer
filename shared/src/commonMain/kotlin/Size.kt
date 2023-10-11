import kotlin.math.max

data class Size(
    val width: Int,
    val height: Int
) {

    fun toScaledSize(longSide: Int): Size {

        val isLandscape = width > height

        val resizeFactor =
            if (isLandscape)
                longSide / width.toFloat()
            else
                longSide / height.toFloat()

        /*
         * If an input image is only 1 pixel wide or tall, we cannot round it down to zero
         * because this would be an invalid operation. Therefore, we must ensure that
         * there is always at least one pixel in the resulting image, regardless of the input size.
         */
        return Size(
            width = max(1, (resizeFactor * width).toInt()),
            height = max(1, (resizeFactor * height).toInt())
        )
    }
}