package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.file_group.model.CreateFileGroupRequest;

import java.util.List;

@RestController
@RequestMapping("/receipt-group-files")
@AllArgsConstructor
public class FileGroupController {

    private final FileFacade fileFacade;

    @PostMapping(path = "/{receiptId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> uploadFileReceipt(@PathVariable String receiptId,
                                           @RequestPart("file") List<MultipartFile> files,
                                           @RequestPart("body") CreateFileGroupRequest createFileGroupRequest,
                                           @AuthenticationPrincipal Jwt jwt) {
        fileFacade.uploadFile(jwt.getSubject(), receiptId, files, createFileGroupRequest);
        return ResponseEntity.ok().build();
    }
}
