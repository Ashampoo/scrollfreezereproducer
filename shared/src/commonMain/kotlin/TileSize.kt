@Suppress("MagicNumber")
enum class TileSize(

    /**
     * Database ID for saving in settings
     */
    val id: Int,

    /**
     * On desktop systems the app is presented in a
     * window which size can change. This gives a very
     * broad range of possible sizes. So we need this to be flexible.
     */
    val desktopSizePt: Int,

    /**
     * Number of photos in one row in portrait mode.
     */
    val itemsPerRowPortrait: Int,

    /**
     * Number of photos on one row in landscape mode.
     */
    val itemsPerRowLandscape: Int
) {

    BIG(
        id = 0,
        desktopSizePt = 180,
        itemsPerRowPortrait = 3,
        itemsPerRowLandscape = 5
    ),

    MEDIUM(
        id = 1,
        desktopSizePt = 150,
        itemsPerRowPortrait = 4,
        itemsPerRowLandscape = 6
    ),

    SMALL(
        id = 2,
        desktopSizePt = 120,
        itemsPerRowPortrait = 5,
        itemsPerRowLandscape = 7
    );
}
