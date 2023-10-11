import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSSortDescriptor
import platform.Photos.*

object ApplePhotoLoader : PhotoLoader {

    private var idCounter: Long = 0

    private val fetchOptions = PHFetchOptions().apply {
        sortDescriptors = listOf(NSSortDescriptor("creationDate", true))
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun findAllPhotos(): Set<Photo> {

        val allAssets = mutableSetOf<PHAsset>()

        val assetsInMainLibrary: PHFetchResult = PHAsset.fetchAssetsWithMediaType(
            mediaType = PHAssetMediaTypeImage,
            options = fetchOptions
        )

        assetsInMainLibrary.enumerateObjectsUsingBlock { phAsset, _, _ ->
            allAssets.add(phAsset as PHAsset)
        }

        val albums = PHAssetCollection.fetchAssetCollectionsWithType(
            type = PHAssetCollectionTypeAlbum,
            subtype = PHAssetCollectionSubtypeAny,
            options = null
        )

        albums.enumerateObjectsUsingBlock { collection, _, _ ->

            collection as PHAssetCollection

            val assetsInAlbum = PHAsset.fetchAssetsInAssetCollection(
                assetCollection = collection,
                options = fetchOptions
            )

            assetsInAlbum.enumerateObjectsUsingBlock { phAsset, _, _ ->
                allAssets.add(phAsset as PHAsset)
            }
        }

        val photos = mutableSetOf<Photo>()

        for (phAsset in allAssets) {

            val photo = Photo(
                id = idCounter++,
                uri = phAsset.localIdentifier
            )

            photos.add(photo)
        }

        return photos
    }
}