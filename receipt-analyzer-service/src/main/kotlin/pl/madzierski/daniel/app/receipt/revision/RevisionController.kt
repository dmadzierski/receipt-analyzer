package pl.madzierski.daniel.app.receipt.revision

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import pl.madzierski.daniel.app.receipt.revision.model.AddRevisionRequest
import pl.madzierski.daniel.app.receipt.revision.model.AddRevisionResponse
import pl.madzierski.daniel.app.receipt.revision.model.RevisionCopyResponse
import pl.madzierski.daniel.app.receipt.revision.model.GetRevisionResponse

@RestController
@RequestMapping(
    "/revisions",
)
@Validated
class RevisionController(val receiptRevisionService: ReceiptRevisionService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addRevision(@Validated @RequestBody revisionRequest: AddRevisionRequest): ResponseEntity<AddRevisionResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptRevisionService.addRevision(revisionRequest))
    }

    @PostMapping(path = ["/{revisionId}/copy"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createCopy(@PathVariable revisionId: String): ResponseEntity<RevisionCopyResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptRevisionService.createRevisionCopy(revisionId))
    }

    @GetMapping(path = ["/{revisionId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getRevision(@PathVariable revisionId: String): ResponseEntity<GetRevisionResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptRevisionService.getRevision(revisionId))
    }

}