package pl.madzierski.daniel.app.file_group

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.file_group.file.FileEntity
import pl.madzierski.daniel.app.file_group.file.FileProvider
import pl.madzierski.daniel.app.file_group.file.FileRepository
import pl.madzierski.daniel.app.receipt.ReceiptEntity
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages
import pl.madzierski.daniel.security.SecurityUtils

@Service
class FileGroupService(
    private val fileGroupRepository: FileGroupRepository,
    private val fileRepository: FileRepository,
    private val fileProvider: FileProvider
) {


}