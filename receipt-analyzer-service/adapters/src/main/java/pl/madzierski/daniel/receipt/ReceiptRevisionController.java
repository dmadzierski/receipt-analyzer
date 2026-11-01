package pl.madzierski.daniel.receipt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.receipt.model.*;
import pl.madzierski.daniel.user.model.AuthenticatedUser;

import java.util.UUID;

@RestController
@RequestMapping("/revisions")
@Validated
@RequiredArgsConstructor
class ReceiptRevisionController {

    private final ReceiptFacade receiptFacade;

    @PostMapping(path = "/{revisionId}/copy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RevisionCopyResponse> createCopy(AuthenticatedUser user, @PathVariable UUID revisionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptFacade.createRevisionCopy(user, revisionId));
    }

    @GetMapping(path = "/{revisionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetRevisionResponse> getRevision(AuthenticatedUser user, @PathVariable UUID revisionId) {
        return ResponseEntity.ok(receiptFacade.getRevision(user, revisionId));
    }

    @PutMapping(path = "/{revisionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UpdateRevisionResponse> updateRevision(AuthenticatedUser user, @PathVariable UUID revisionId, @RequestBody UpdateRevisionRequest revision) {
        return ResponseEntity.ok(receiptFacade.updateRevision(user, revisionId, revision));
    }

    @PostMapping(path = "/{revisionId}/aliases")
    void updateAliasesByUserRevision(AuthenticatedUser user, @PathVariable UUID revisionId) {
        receiptFacade.updateAliasesByUserRevision(user, revisionId);
    }

    @PostMapping(path = "/{revisionId}/update-product-dict")
    void updateProductDictInRevisionItems(AuthenticatedUser user, @PathVariable UUID revisionId) {
        receiptFacade.updateProductDictInRevisionItems(user, revisionId);
    }

    @DeleteMapping(path = "/{revisionId}")
    ResponseEntity<Void> deleteRevision(AuthenticatedUser user, @PathVariable UUID revisionId) {
        receiptFacade.deleteRevision(user, revisionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{revisionId}/rewrite-product-names")
    void rewriteProductNames(AuthenticatedUser user, @PathVariable UUID revisionId) {
        receiptFacade.rewriteProductNameToItems(user, revisionId);
    }
}
