package pl.madzierski.daniel.app.file_group.file;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/receipt-files")
@AllArgsConstructor
class FileController {

    private final FileService fileService;

    @GetMapping(path = "/{receiptFileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Resource> getFileReceipt(@PathVariable String receiptFileId) {
        Resource fileReceipt = fileService.getFileReceipt(receiptFileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileReceipt.getFilename() + "\"")
                .body(fileReceipt);
    }
}