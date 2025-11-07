package org.example.project

class ImageManager(private val fileHandler: FileHandler) {

    suspend fun saveProjectImage(projectId: Int, image: ByteArray) {
        fileHandler.writeBytes("img/project/${projectId}_1.jpg", image)
    }

    suspend fun getProjectImage(projectId: Int): ByteArray? {
        return fileHandler.readBytes("img/project/${projectId}_1.jpg")
    }

    suspend fun deleteProjectImage(projectId: Int) {
        fileHandler.deleteFile("img/project/${projectId}_1.jpg")
    }

    suspend fun saveYarnImage(yarnId: Int, image: ByteArray) {
        fileHandler.writeBytes("img/yarn/${yarnId}_1.jpg", image)
    }

    suspend fun getYarnImage(yarnId: Int): ByteArray? {
        return fileHandler.readBytes("img/yarn/${yarnId}_1.jpg")
    }

    suspend fun deleteYarnImage(yarnId: Int) {
        fileHandler.deleteFile("img/yarn/${yarnId}_1.jpg")
    }
}
