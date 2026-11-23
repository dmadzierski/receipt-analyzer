package pl.madzierski.daniel.file_group;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.exception.AppIllegalStateExceptionMessages;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;
import pl.madzierski.daniel.user.model.UserQuery;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(exclude = {"receipt", "files"})
class FileGroup {

    private UUID id;
    private Long version;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private FileType fileType;
    private ReceiptQuery receipt;
    private UserQuery owner;
    @Builder.Default
    private Set<File> files = new HashSet<>();

    static boolean haveSameType(List<FileType> partTypes) {
        return !partTypes.isEmpty() && partTypes.stream().allMatch(type -> type != null && type == partTypes.getFirst());
    }

    static FileGroup create(List<FileType> partTypes, ReceiptQuery receipt, UserQuery owner) {
        if (!haveSameType(partTypes)) throw new AppIllegalStateException(AppIllegalStateExceptionMessages.FILE_TYPE_MISMATCH);
        return builder()
            .fileType(partTypes.getFirst())
            .receipt(receipt)
            .owner(owner)
            .files(IntStream.range(0, partTypes.size())
                .mapToObj(File::create)
                .collect(Collectors.toCollection(HashSet::new)))
            .build();
    }

    static FileGroup restore(UUID id, Long version, LocalDateTime createdDate, LocalDateTime modifiedDate, FileType fileType,
                             ReceiptQuery receipt, UserQuery owner, Collection<File> files) {
        return builder()
            .id(id)
            .version(version)
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
