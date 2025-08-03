package pl.madzierski.daniel.app.receipt

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.receipt.model.CreateReceiptRequestBody
import pl.madzierski.daniel.app.receipt.model.CreateReceiptResponseBody


@RestController
@RequestMapping("/receipt", produces = [MediaType.APPLICATION_JSON_VALUE])
class ReceiptController(val receiptService: ReceiptService) {

    @PostMapping(path = ["/create"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addReceipt(
        @RequestPart(required = true, value = "file") file: MultipartFile,
        @RequestPart(required = false, value = "body") body: CreateReceiptRequestBody?
    ): ResponseEntity<CreateReceiptResponseBody> {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptService.addReceipt(file, body))
    }


}
