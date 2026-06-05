package pl.madzierski.daniel.app.file_group

import org.springframework.stereotype.Service
import pl.madzierski.daniel.app.file_group.file.FileProvider
import pl.madzierski.daniel.app.file_group.file.FileRepository

@Service
class FileGroupService(
    private val fileGroupRepository: FileGroupRepository,
    private val fileRepository: FileRepository,
    private val fileProvider: FileProvider
) {


}