package pl.madzierski.daniel.app.receipt;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.app.receipt.model.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/receipts", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<CreateReceiptResponse> addReceipt(@RequestPart(value = "file") MultipartFile file, @RequestPart(value = "body") @NotNull CreateReceiptRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.receiptService.addReceipt(file, body));
    }

    @GetMapping(path = {"/list"})
    public ResponseEntity<GetReceiptListResponse> getReceiptList() {
        return ResponseEntity.ok(this.receiptService.getReceiptList());
    }

    @GetMapping(path = {"/{receiptId}"})
    public ResponseEntity<GetReceiptDetailsResponse> getReceiptDetails(@PathVariable @UUID String receiptId) {
        return ResponseEntity.ok(this.receiptService.getReceiptDetails(receiptId));
    }

    @GetMapping(path = {"/{receiptId}/revisions"}, produces = {"application/json"})
    public ResponseEntity<List<GetReceiptRevisionsResponse>> getReceiptRevisions(@PathVariable String receiptId) {
        return ResponseEntity.ok(this.receiptService.getReceiptRevisions(receiptId));
    }
}