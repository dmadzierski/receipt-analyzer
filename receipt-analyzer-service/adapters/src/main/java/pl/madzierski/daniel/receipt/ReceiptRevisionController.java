package pl.madzierski.daniel.receipt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.receipt.model.*;

import java.util.UUID;

@RestController
@RequestMapping("/revisions")
@Validated
@RequiredArgsConstructor
class ReceiptRevisionController {

    private final ReceiptFacade receiptFacade;

    @PostMapping(path = "/{revisionId}/copy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RevisionCopyResponse> createCopy(@PathVariable UUID revisionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptFacade.createRevisionCopy(revisionId));
    }

    @GetMapping(path = "/{revisionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetRevisionResponse> getRevision(@PathVariable UUID revisionId) {
        return ResponseEntity.ok(receiptFacade.getRevision(revisionId));
    }

    @PutMapping(path = "/{revisionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UpdateRevisionResponse> updateRevision(@PathVariable UUID revisionId, @RequestBody UpdateRevisionRequest revision) {
        return ResponseEntity.ok(receiptFacade.updateRevision(revisionId, revision));
    }

    @PostMapping(path = "/{revisionId}/aliases")
    void updateAliasesByUserRevision(@PathVariable UUID revisionId) {
        receiptFacade.updateAliasesByUserRevision(revisionId);
    }

    @PostMapping(path = "/{revisionId}/update-product-dict")
    void updateProductDictInRevisionItems(@PathVariable UUID revisionId) {
        receiptFacade.updateProductDictInRevisionItems(revisionId);
    }

    @DeleteMapping(path = "/{revisionId}")
    ResponseEntity<Void> deleteRevision(@PathVariable UUID revisionId) {
        receiptFacade.deleteRevision(revisionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{revisionId}/rewrite-product-names")
    void rewriteProductNames(@PathVariable UUID revisionId) {
        receiptFacade.rewriteProductNameToItems(revisionId);
    }
}