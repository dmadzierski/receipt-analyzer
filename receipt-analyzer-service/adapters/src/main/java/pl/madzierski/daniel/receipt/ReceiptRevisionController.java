package pl.madzierski.daniel.receipt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.receipt.model.GetRevisionResponse;
import pl.madzierski.daniel.receipt.model.RevisionCopyResponse;
import pl.madzierski.daniel.receipt.model.UpdateRevisionRequest;
import pl.madzierski.daniel.receipt.model.UpdateRevisionResponse;

@RestController
@RequestMapping("/revisions")
@Validated
@RequiredArgsConstructor
class ReceiptRevisionController {

    private final ReceiptFacade receiptFacade;

    @PostMapping(path = "/{revisionId}/copy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RevisionCopyResponse> createCopy(@PathVariable String revisionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptFacade.createRevisionCopy(revisionId));
    }

    @GetMapping(path = "/{revisionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetRevisionResponse> getRevision(@PathVariable String revisionId) {
        return ResponseEntity.ok(receiptFacade.getRevision(revisionId));
    }

    @PutMapping(path = "/{revisionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UpdateRevisionResponse> updateRevision(@PathVariable String revisionId, @RequestBody UpdateRevisionRequest revision) {
        return ResponseEntity.ok(receiptFacade.updateRevision(revisionId, revision));
    }

    @PostMapping(path = "/{revisionId}/aliases")
    void updateAliasesByUserRevision(@PathVariable String revisionId) {
        receiptFacade.updateDictByUserRevision(revisionId);
    }
}