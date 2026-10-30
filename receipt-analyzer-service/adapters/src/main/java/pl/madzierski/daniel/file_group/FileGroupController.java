package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/receipt-group-files")
@AllArgsConstructor
public class FileGroupController {

    private final FileFacade fileFacade;

    @PostMapping(path = "/{receiptId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> uploadFileReceipt(@PathVariable UUID receiptId,
                                           @RequestPart("file") List<MultipartFile> files,
                                           @AuthenticationPrincipal Jwt jwt) {
        fileFacade.save(receiptId, MultipartFileMapper.toBytes(files), jwt.getSubject());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{fileGroupId}")
    ResponseEntity<Void> deleteFileGroup(@PathVariable UUID fileGroupId, @AuthenticationPrincipal Jwt jwt) {
        fileFacade.deleteFileGroupId(fileGroupId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
