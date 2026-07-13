package pl.madzierski.daniel.file_group;

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

    public static final String ATTACHMENT_FILENAME_TEMPLATE = "attachment; filename=\"%s\"";
    private final FileFacade fileFacade;

    @GetMapping(path = "/{receiptFileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Resource> getFileReceipt(@PathVariable String receiptFileId) {
        Resource fileReceipt = fileFacade.getFile(receiptFileId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_FILENAME_TEMPLATE.formatted(fileReceipt.getFilename()))
            .body(fileReceipt);
    }
}