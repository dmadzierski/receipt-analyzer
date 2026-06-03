package pl.madzierski.daniel.app.file_group.file

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException

@Service
class FileProvider(private val fileRepository: FileRepository) {

    fun saveFile(path: String, file: MultipartFile) {
        try {
            val destFile = File(path)
            destFile.parentFile?.mkdirs()
            file.transferTo(destFile)
        } catch (e: IOException) {
            throw e
        }
    }

    fun save(fileEntity: FileEntity): FileEntity {
        return fileRepository.save(fileEntity)
    }

}