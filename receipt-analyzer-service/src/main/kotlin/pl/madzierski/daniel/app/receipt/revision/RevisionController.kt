package pl.madzierski.daniel.app.receipt.revision

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.madzierski.daniel.app.receipt.revision.model.AddRevisionRequest
import pl.madzierski.daniel.app.receipt.revision.model.AddRevisionResponse

@RestController
@RequestMapping(
    "/revisions",
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.APPLICATION_JSON_VALUE]
)
@Validated
class RevisionController(val receiptRevisionService: ReceiptRevisionService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addRevision(@Validated @RequestBody revisionRequest: AddRevisionRequest): ResponseEntity<AddRevisionResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptRevisionService.addRevision(revisionRequest))
    }
}