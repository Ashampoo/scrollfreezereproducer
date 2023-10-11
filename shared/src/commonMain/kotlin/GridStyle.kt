package com.ashampoo.photos.shared.model

enum class GridStyle(
    val id: Int
) {

    SQUARE(0),
    TILE(1),
    FLUID(2);

    companion object {

        /*
         * Note that using an integer ID instead of the name
         * allows to freely rename the enum afterward.
         * In this method future migration might be handled.
         */
        fun valueOf(id: Int): GridStyle {

            return when (id) {
                0 -> SQUARE
                1 -> TILE
                2 -> FLUID
                else -> TILE
            }
        }
    }
}
