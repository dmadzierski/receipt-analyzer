package pl.madzierski.daniel.receipt;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.receipt.model.CreateReceiptRequest;
import pl.madzierski.daniel.receipt.model.CreateReceiptResponse;
import pl.madzierski.daniel.receipt.model.GetReceiptDetailsResponse;
import pl.madzierski.daniel.receipt.model.GetReceiptRevisionsResponse;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/receipts", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
class ReceiptController {

    private final ReceiptFacade receiptFacade;

    @PostMapping(consumes = {"multipart/form-data"})
    ResponseEntity<CreateReceiptResponse> addReceipt(@AuthenticationPrincipal Jwt jwt, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "body") @NotNull CreateReceiptRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.receiptFacade.addReceipt(jwt.getSubject(), file,
            body));
    }

    @GetMapping(path = {"/{receiptId}"})
    ResponseEntity<GetReceiptDetailsResponse> getReceiptDetails(@PathVariable @UUID String receiptId) {
        return ResponseEntity.ok(this.receiptFacade.getReceiptDetails(receiptId));
    }

    @GetMapping(path = {"/{receiptId}/revisions"}, produces = {"application/json"})
    ResponseEntity<List<GetReceiptRevisionsResponse>> getReceiptRevisions(@PathVariable String receiptId) {
        return ResponseEntity.ok(this.receiptFacade.getReceiptRevisions(receiptId));
    }
}