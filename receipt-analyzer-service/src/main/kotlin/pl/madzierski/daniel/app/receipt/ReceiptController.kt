package pl.madzierski.daniel.app.receipt

import org.hibernate.validator.constraints.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pl.madzierski.daniel.app.receipt.model.*


@RestController
@RequestMapping("/receipts", produces = [MediaType.APPLICATION_JSON_VALUE])
@Validated
class ReceiptController(val receiptService: ReceiptService) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addReceipt(
        @RequestPart(required = true, value = "file") file: MultipartFile,
        @RequestPart(required = false, value = "body") body: CreateReceiptRequest
    ): ResponseEntity<CreateReceiptResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptService.addReceipt(file, body))
    }

    @GetMapping(path = ["/list"])
    fun getReceiptList(): ResponseEntity<GetReceiptListResponse> {
        return ResponseEntity.ok(receiptService.getReceiptList())
    }

    @GetMapping(path = ["/{receiptId}"])
    fun getReceiptDetails(@PathVariable @UUID receiptId: String): ResponseEntity<GetReceiptDetailsResponse> {
        return ResponseEntity.ok(receiptService.getReceiptDetails(receiptId))
    }

    @GetMapping(path = ["/{receiptId}/revisions"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getReceiptRevisions(@PathVariable receiptId: String): ResponseEntity<List<GetReceiptRevisionsResponse>> {
        return ResponseEntity.ok(receiptService.getReceiptRevisions(receiptId))
    }
}
