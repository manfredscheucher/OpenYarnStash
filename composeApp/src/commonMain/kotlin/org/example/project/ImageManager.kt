package org.example.project

class ImageManager(private val fileHandler: FileHandler) {

    suspend fun saveProjectImage(projectId: Int, imageId: Int, image: ByteArray) {
        fileHandler.writeBytes("img/project/${projectId}_$imageId.jpg", image)
    }

    suspend fun getProjectImage(projectId: Int, imageId: Int): ByteArray? {
        return fileHandler.readBytes("img/project/${projectId}_$imageId.jpg")
    }

    fun getProjectImageInputStream(projectId: Int, imageId: Int): FileInputSource? {
        return fileHandler.openInputStream("img/project/${projectId}_$imageId.jpg")
    }

    suspend fun getProjectImageThumbnail(projectId: Int, imageId: Int, maxWidth: Int=256, maxHeight: Int=256): ByteArray? {
        val thumbnailPath = "img/project/${projectId}_${imageId}_${maxWidth}x${maxHeight}.jpg"
        val cachedThumbnail = fileHandler.readBytes(thumbnailPath)
        if (cachedThumbnail != null) {
            return cachedThumbnail
        }

        val imageBytes = getProjectImage(projectId, imageId)
        return imageBytes?.let {
            val resizedImage = resizeImage(it, maxWidth, maxHeight)
            resizedImage?.let {
                fileHandler.writeBytes(thumbnailPath, it)
            }
            resizedImage
        }
    }

    suspend fun deleteProjectImage(projectId: Int, imageId: Int) {
        fileHandler.deleteFile("img/project/${projectId}_$imageId.jpg")
    }

    suspend fun saveYarnImage(yarnId: Int, imageId: Int, image: ByteArray) {
        fileHandler.writeBytes("img/yarn/${yarnId}_$imageId.jpg", image)
    }

    suspend fun getYarnImage(yarnId: Int, imageId: Int): ByteArray? {
        return fileHandler.readBytes("img/yarn/${yarnId}_$imageId.jpg")
    }

    fun getYarnImageInputStream(yarnId: Int, imageId: Int): FileInputSource? {
        return fileHandler.openInputStream("img/yarn/${yarnId}_$imageId.jpg")
    }

    suspend fun getYarnImageThumbnail(yarnId: Int, imageId: Int, maxWidth: Int=256, maxHeight: Int=256): ByteArray? {
        val thumbnailPath = "img/yarn/${yarnId}_${imageId}_${maxWidth}x${maxHeight}.jpg"
        val cachedThumbnail = fileHandler.readBytes(thumbnailPath)
        if (cachedThumbnail != null) {
            return cachedThumbnail
        }

        val imageBytes = getYarnImage(yarnId, imageId)
        return imageBytes?.let {
            val resizedImage = resizeImage(it, maxWidth, maxHeight)
            resizedImage?.let {
                fileHandler.writeBytes(thumbnailPath, it)
            }
            resizedImage
        }
    }

    suspend fun deleteYarnImage(yarnId: Int, imageId: Int) {
        fileHandler.deleteFile("img/yarn/${yarnId}_$imageId.jpg")
    }
}
