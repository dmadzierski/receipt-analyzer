package pl.madzierski.daniel.file_group;

import org.junit.jupiter.api.Test;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileGroupUT {

    @Test
    void createdGroupHasOneFileWithoutPathPerPart() {
        UUID receiptId = UUID.randomUUID();
        UserQuery owner = new UserQuery(UUID.randomUUID());

        FileGroup group = FileGroup.create(List.of(FileType.PNG, FileType.PNG, FileType.PNG), receiptId, owner);

        assertThat(group.getId()).isNull();
        assertThat(group.getFileType()).isEqualTo(FileType.PNG);
        assertThat(group.getReceiptId()).isEqualTo(receiptId);
        assertThat(group.getOwner()).isEqualTo(owner);
        assertThat(group.getFiles()).extracting(File::getPartNumber).containsExactlyInAnyOrder(0, 1, 2);
        assertThat(group.getFiles()).extracting(File::getPath).containsOnlyNulls();
    }

    @Test
    void groupOfPartsWithDifferentTypesCannotBeCreated() {
        List<FileType> partTypes = List.of(FileType.PNG, FileType.JPEG);

        assertThat(FileGroup.haveSameType(partTypes)).isFalse();
        assertThatThrownBy(() -> FileGroup.create(partTypes, null, null)).isInstanceOf(AppIllegalStateException.class);
    }

    @Test
    void groupNeedsAtLeastOnePart() {
        assertThat(FileGroup.haveSameType(List.of())).isFalse();
    }

    @Test
    void assignPathsUsesPolicyForEveryFile() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        FileGroup group = FileGroup.restore(UUID.randomUUID(), null, null, null, FileType.PDF, null, null, List.of(
            File.restore(firstId, null, null, null, 0),
            File.restore(secondId, null, null, null, 1)));

        group.assignPaths((fileGroup, file) -> fileGroup.getId() + "/" + file.getId() + "." + fileGroup.getFileType().getExtension());

        assertThat(group.getFiles()).extracting(File::getPath).containsExactlyInAnyOrder(
            group.getId() + "/" + firstId + ".pdf",
            group.getId() + "/" + secondId + ".pdf");
    }

    @Test
    void filesCannotBeModifiedFromOutside() {
        FileGroup group = FileGroup.create(List.of(FileType.PNG), null, null);

        assertThatThrownBy(() -> group.getFiles().add(File.create(1))).isInstanceOf(UnsupportedOperationException.class);
    }
}
