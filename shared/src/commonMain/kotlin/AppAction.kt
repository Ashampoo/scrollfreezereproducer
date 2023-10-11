sealed class AppAction : Action {

    data class GalleryViewScrolled(
        val scrollPosition: Double
    ) : AppAction()

    data class SelectPhoto(
        val photoId: Long
    ) : AppAction()

    data class OpenPhoto(
        val photo: Photo
    ) : AppAction()

}