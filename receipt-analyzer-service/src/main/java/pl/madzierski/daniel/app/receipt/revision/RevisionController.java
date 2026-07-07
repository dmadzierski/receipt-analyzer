package pl.madzierski.daniel.app.receipt.revision;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.app.receipt.revision.model.*;

@RestController
@RequestMapping("/revisions")
@Validated
class RevisionController {

    private final ReceiptRevisionService receiptRevisionService;

    public RevisionController(ReceiptRevisionService receiptRevisionService) {
        this.receiptRevisionService = receiptRevisionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AddRevisionResponse> addRevision(@Validated @RequestBody AddRevisionRequest revisionRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptRevisionService.addRevision(revisionRequest));
    }

    @PostMapping(path = "/{revisionId}/copy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RevisionCopyResponse> createCopy(@PathVariable String revisionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptRevisionService.createRevisionCopy(revisionId));
    }

    @GetMapping(path = "/{revisionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRevisionResponse> getRevision(@PathVariable String revisionId) {
        return ResponseEntity.ok(receiptRevisionService.getRevision(revisionId));
    }

    @PutMapping(path = "/{revisionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdateRevisionResponse> updateRevision(@PathVariable String revisionId, @RequestBody UpdateRevisionRequest revision) {
        return ResponseEntity.ok(receiptRevisionService.updateRevision(revisionId, revision));
    }

    @PostMapping("/{revisionId}/aliases")
    void updateAliasesByUserRevision(@RequestParam String revisionId) {
        receiptRevisionService.updateDictByUserRevision(revisionId);
    }
}