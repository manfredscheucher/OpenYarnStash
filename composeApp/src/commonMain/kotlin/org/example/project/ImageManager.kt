package org.example.project

class ImageManager(private val fileHandler: FileHandler) {

    suspend fun saveProjectImage(projectId: Int, imageId: Int, image: ByteArray) {
        fileHandler.writeBytes("img/project/${projectId}_$imageId.jpg", image)
    }

    suspend fun getProjectImage(projectId: Int, imageId: Int): ByteArray? {
        return fileHandler.readBytes("img/project/${projectId}_$imageId.jpg")
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

    suspend fun deleteYarnImage(yarnId: Int, imageId: Int) {
        fileHandler.deleteFile("img/yarn/${yarnId}_$imageId.jpg")
    }
}
