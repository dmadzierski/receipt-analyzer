package pl.madzierski.daniel.file_group;

import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Optional;
import java.util.UUID;

interface FileRepository {
    Optional<File> findAccessible(UUID fileId, UserQuery user);

    File save(File fileEntity);
}
