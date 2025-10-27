package org.example.project

class ImageManager(private val fileHandler: FileHandler) {

    suspend fun saveProjectImage(projectId: Int, image: ByteArray) {
        fileHandler.writeBytes("img/project/$projectId.jpg", image)
    }

    suspend fun getProjectImage(projectId: Int): ByteArray? {
        return fileHandler.readBytes("img/project/$projectId.jpg")
    }

    suspend fun deleteProjectImage(projectId: Int) {
        fileHandler.deleteFile("img/project/$projectId.jpg")
    }

    suspend fun saveYarnImage(yarnId: Int, image: ByteArray) {
        fileHandler.writeBytes("img/yarn/$yarnId.jpg", image)
    }

    suspend fun getYarnImage(yarnId: Int): ByteArray? {
        return fileHandler.readBytes("img/yarn/$yarnId.jpg")
    }

    suspend fun deleteYarnImage(yarnId: Int) {
        fileHandler.deleteFile("img/yarn/$yarnId.jpg")
    }
}
