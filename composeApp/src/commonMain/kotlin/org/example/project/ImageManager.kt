package org.example.project

class ImageManager(private val fileHandler: FileHandler) {

    companion object {
        private const val THUMBNAIL_WIDTH = 256
        private const val THUMBNAIL_HEIGHT = 256
    }

    private fun getProjectImagePath(projectId: Int, imageId: Int) = "img/project/${projectId}_$imageId.jpg"
    private fun getProjectImageThumbnailPath(projectId: Int, imageId: Int, width: Int, height: Int) = "img/project/${projectId}_${imageId}_${width}x${height}.jpg"

    private fun getYarnImagePath(yarnId: Int, imageId: Int) = "img/yarn/${yarnId}_$imageId.jpg"
    private fun getYarnImageThumbnailPath(yarnId: Int, imageId: Int, width: Int, height: Int) = "img/yarn/${yarnId}_${imageId}_${width}x${height}.jpg"


    suspend fun saveProjectImage(projectId: Int, imageId: Int, image: ByteArray) {
        fileHandler.writeBytes(getProjectImagePath(projectId, imageId), image)
    }

    suspend fun getProjectImage(projectId: Int, imageId: Int): ByteArray? {
        return fileHandler.readBytes(getProjectImagePath(projectId, imageId))
    }

    fun getProjectImageInputStream(projectId: Int, imageId: Int): FileInputSource? {
        return fileHandler.openInputStream(getProjectImagePath(projectId, imageId))
    }

    suspend fun getProjectImageThumbnail(projectId: Int, imageId: Int): ByteArray? {
        val thumbnailPath = getProjectImageThumbnailPath(projectId, imageId, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
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

    suspend fun deleteProjectImage(projectId: Int, imageId: Int) {
        fileHandler.deleteFile(getProjectImagePath(projectId, imageId))
        fileHandler.deleteFile(getProjectImageThumbnailPath(projectId, imageId, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT))
    }

    suspend fun saveYarnImage(yarnId: Int, imageId: Int, image: ByteArray) {
        fileHandler.writeBytes(getYarnImagePath(yarnId, imageId), image)
    }

    suspend fun getYarnImage(yarnId: Int, imageId: Int): ByteArray? {
        return fileHandler.readBytes(getYarnImagePath(yarnId, imageId))
    }

    fun getYarnImageInputStream(yarnId: Int, imageId: Int): FileInputSource? {
        return fileHandler.openInputStream(getYarnImagePath(yarnId, imageId))
    }

    suspend fun getYarnImageThumbnail(yarnId: Int, imageId: Int): ByteArray? {
        val thumbnailPath = getYarnImageThumbnailPath(yarnId, imageId, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
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

    suspend fun deleteYarnImage(yarnId: Int, imageId: Int) {
        fileHandler.deleteFile(getYarnImagePath(yarnId, imageId))
        fileHandler.deleteFile(getYarnImageThumbnailPath(yarnId, imageId, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT))
    }
}
