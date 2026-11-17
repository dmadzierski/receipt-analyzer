package pl.madzierski.daniel.file_group;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;
import pl.madzierski.daniel.user.model.UserQuery;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(exclude = {"receipt", "files"})
class FileGroup {

    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private FileType fileType;
    private ReceiptQuery receipt;
    private UserQuery owner;
    @Builder.Default
    private Set<File> files = new HashSet<>();

    static FileGroup create(FileType fileType, ReceiptQuery receipt, UserQuery owner, int partCount) {
        return builder()
            .fileType(fileType)
            .receipt(receipt)
            .owner(owner)
            .files(IntStream.range(0, partCount)
                .mapToObj(File::create)
                .collect(Collectors.toCollection(HashSet::new)))
            .build();
    }

    static FileGroup restore(UUID id, LocalDateTime createdDate, LocalDateTime modifiedDate, FileType fileType,
                             ReceiptQuery receipt, UserQuery owner, Collection<File> files) {
        return builder()
            .id(id)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .fileType(fileType)
            .receipt(receipt)
            .owner(owner)
            .files(new HashSet<>(files))
            .build();
    }

    void assignPaths(FilePathPolicy pathPolicy) {
        files.forEach(file -> file.assignPath(pathPolicy.pathOf(this, file)));
    }

    Set<File> getFiles() {
        return Collections.unmodifiableSet(files);
    }
}
