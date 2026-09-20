package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.file_group.model.CreateFileGroupRequest;

import java.util.List;

@RestController
@RequestMapping("/receipt-files")
@AllArgsConstructor
class FileController {

    private static final String ATTACHMENT_FILENAME_TEMPLATE = "attachment; filename=\"%s\"";
    private final FileFacade fileFacade;

    @GetMapping(path = "/{receiptFileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Resource> getFileReceipt(@PathVariable String receiptFileId) {
        Resource fileReceipt = fileFacade.getFile(receiptFileId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_FILENAME_TEMPLATE.formatted(fileReceipt.getFilename()))
            .body(fileReceipt);
    }

    @PostMapping(path = "/{receiptId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> uploadFileGroup(@PathVariable String receiptId,
                                         @RequestPart("file") List<MultipartFile> files,
                                         @RequestPart("body") CreateFileGroupRequest createFileGroupRequest,
                                         @AuthenticationPrincipal Jwt jwt) {
        fileFacade.uploadFile(jwt.getSubject(), receiptId, files, createFileGroupRequest);
        return ResponseEntity.ok().build();
    }

}