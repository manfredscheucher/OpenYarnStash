package org.example.project

class ImageManager(private val fileHandler: FileHandler) {

    companion object {
        private const val THUMBNAIL_WIDTH = 256
        private const val THUMBNAIL_HEIGHT = 256
    }
    private val projectImagesDir = "img/project"
    private val projectImageThumbnailsDir = "img/project/thumbnails"
    private val yarnImagesDir = "img/yarn"
    private val yarnImageThumbnailsDir = "img/yarn/thumbnails"

    private fun getProjectImagePath(projectId: UInt, imageId: UInt) = "$projectImagesDir/${projectId}_$imageId.jpg"
    private fun getProjectImageThumbnailPath(projectId: UInt, imageId: UInt) = "$projectImageThumbnailsDir/${projectId}_${imageId}_${THUMBNAIL_WIDTH}x${THUMBNAIL_HEIGHT}.jpg"

    private fun getYarnImagePath(yarnId: UInt, imageId: UInt) = "$yarnImagesDir/${yarnId}_$imageId.jpg"
    private fun getYarnImageThumbnailPath(yarnId: UInt, imageId: UInt) = "$yarnImageThumbnailsDir/${yarnId}_${imageId}_${THUMBNAIL_WIDTH}x${THUMBNAIL_HEIGHT}.jpg"


    suspend fun saveProjectImage(projectId: UInt, imageId: UInt, image: ByteArray) {
        fileHandler.writeBytes(getProjectImagePath(projectId, imageId), image)
    }

    suspend fun getProjectImage(projectId: UInt, imageId: UInt): ByteArray? {
        return fileHandler.readBytes(getProjectImagePath(projectId, imageId))
    }

    fun getProjectImageInputStream(projectId: UInt, imageId: UInt): FileInputSource? {
        return fileHandler.openInputStream(getProjectImagePath(projectId, imageId))
    }

    suspend fun getProjectImageThumbnail(projectId: UInt, imageId: UInt): ByteArray? {
        val thumbnailPath = getProjectImageThumbnailPath(projectId, imageId)
        val cachedThumbnail = fileHandler.readBytes(thumbnailPath)
        if (cachedThumbnail != null) {
            return cachedThumbnail
        }

        val imageBytes = getProjectImage(projectId, imageId)
        return imageBytes?.let {
            val resizedImage = resizeImage(it, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
            resizedImage?.let {
                fileHandler.writeBytes(thumbnailPath, it)
            }
            resizedImage
        }
    }

    suspend fun deleteProjectImage(projectId: UInt, imageId: UInt) {
        fileHandler.deleteFile(getProjectImagePath(projectId, imageId))
        fileHandler.deleteFile(getProjectImageThumbnailPath(projectId, imageId))
    }

    suspend fun saveYarnImage(yarnId: UInt, imageId: UInt, image: ByteArray) {
        fileHandler.writeBytes(getYarnImagePath(yarnId, imageId), image)
    }

    suspend fun getYarnImage(yarnId: UInt, imageId: UInt): ByteArray? {
        return fileHandler.readBytes(getYarnImagePath(yarnId, imageId))
    }

    fun getYarnImageInputStream(yarnId: UInt, imageId: UInt): FileInputSource? {
        return fileHandler.openInputStream(getYarnImagePath(yarnId, imageId))
    }

    suspend fun getYarnImageThumbnail(yarnId: UInt, imageId: UInt): ByteArray? {
        val thumbnailPath = getYarnImageThumbnailPath(yarnId, imageId)
        val cachedThumbnail = fileHandler.readBytes(thumbnailPath)
        if (cachedThumbnail != null) {
            return cachedThumbnail
        }

        val imageBytes = getYarnImage(yarnId, imageId)
        return imageBytes?.let {
            val resizedImage = resizeImage(it, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
            resizedImage?.let {
                fileHandler.writeBytes(thumbnailPath, it)
            }
            resizedImage
        }
    }

    suspend fun deleteYarnImage(yarnId: UInt, imageId: UInt) {
        fileHandler.deleteFile(getYarnImagePath(yarnId, imageId))
        fileHandler.deleteFile(getYarnImageThumbnailPath(yarnId, imageId))
    }
}
