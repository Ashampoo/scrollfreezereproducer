import java.io.File

object DesktopPhotoLoader : PhotoLoader {

    private var idCounter: Long = 0

    override fun findAllPhotos(): Set<Photo> {

        val photoFolder = System.getProperty("user.home") + "/Pictures/Test"

        val files = File(photoFolder).listFiles() ?: return emptySet()

        val photos = mutableSetOf<Photo>()

        for (file in files) {

            if (!file.isFile)
                continue

            if (file.path.endsWith(".jpg")) {

                photos.add(
                    Photo(
                        id = idCounter++,
                        uri = file.absolutePath
                    )
                )
            }
        }

        return photos
    }
}