package pl.madzierski.daniel.app.file_group.file

import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/receipt-files")
class FileController(
    private val fileService: FileService
) {

    @GetMapping(path = ["/{receiptFileId}"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    private fun getFileReceipt(@PathVariable receiptFileId: String): ResponseEntity<Resource> {
        val fileReceipt = fileService.getFileReceipt(receiptFileId)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${fileReceipt.filename}\"")
            .body(fileReceipt)
    }
}