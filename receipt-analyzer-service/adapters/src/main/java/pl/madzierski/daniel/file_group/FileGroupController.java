package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.user.model.AuthenticatedUser;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/receipt-group-files")
@AllArgsConstructor
public class FileGroupController {

    private final FileFacade fileFacade;

    @PostMapping(path = "/{receiptId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> uploadFileReceipt(AuthenticatedUser user, @PathVariable UUID receiptId,
                                           @RequestPart("file") List<MultipartFile> files) {
        fileFacade.save(user, receiptId, MultipartFileMapper.toBytes(files));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{fileGroupId}")
    ResponseEntity<Void> deleteFileGroup(AuthenticatedUser user, @PathVariable UUID fileGroupId) {
        fileFacade.deleteFileGroupId(user, fileGroupId);
        return ResponseEntity.noContent().build();
    }
}
