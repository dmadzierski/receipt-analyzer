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
import pl.madzierski.daniel.user.model.AuthenticatedUser;

import java.util.UUID;

@RestController
@RequestMapping("/receipt-files")
@AllArgsConstructor
class FileController {

    private static final String ATTACHMENT_FILENAME_TEMPLATE = "attachment; filename=\"%s\"";
    private final FileFacade fileFacade;

    @GetMapping(path = "/{receiptFileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Resource> getFileReceipt(AuthenticatedUser user, @PathVariable UUID receiptFileId) {
        Resource fileReceipt = fileFacade.getFile(user, receiptFileId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_FILENAME_TEMPLATE.formatted(fileReceipt.getFilename()))
            .body(fileReceipt);
    }
}