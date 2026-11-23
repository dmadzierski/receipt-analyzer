package pl.madzierski.daniel.file_group;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import pl.madzierski.daniel.IntegrationTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileStorageIT extends IntegrationTest {

    private static final byte[] CONTENT = "content".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private FileStorage fileStorage;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void fileIsDeletedWhenTransactionRollsBack() {
        Path path = uploadDir().resolve("storage-test").resolve(UUID.randomUUID() + ".txt");

        newTransaction().executeWithoutResult(status -> {
            fileStorage.save(path.toString(), CONTENT);
            assertThat(path).exists();
            status.setRollbackOnly();
        });

        assertThat(path).doesNotExist();
    }

    @Test
    void fileIsKeptWhenTransactionCommits() throws IOException {
        Path path = uploadDir().resolve("storage-test").resolve(UUID.randomUUID() + ".txt");

        newTransaction().executeWithoutResult(status -> fileStorage.save(path.toString(), CONTENT));

        assertThat(path).exists();
        assertThat(Files.readAllBytes(path)).isEqualTo(CONTENT);
        Files.delete(path);
    }

    @Test
    void relativePathIsStoredUnderUploadDirectoryAndAbsolutePathIsUsedAsItIs() throws IOException {
        String relative = "storage-test/" + UUID.randomUUID() + ".txt";
        Path absolute = uploadDir().resolve("storage-test").resolve(UUID.randomUUID() + ".txt");

        newTransaction().executeWithoutResult(status -> {
            fileStorage.save(relative, CONTENT);
            fileStorage.save(absolute.toString(), CONTENT);
        });

        assertThat(uploadDir().resolve(relative)).exists();
        assertThat(fileStorage.read(relative)).hasValue(CONTENT);
        assertThat(fileStorage.read(absolute.toString())).hasValue(CONTENT);
        Files.delete(uploadDir().resolve(relative));
        Files.delete(absolute);
    }

    @Test
    void deletedFileIsRemovedOnlyAfterCommit() throws IOException {
        Path path = storedFile();

        newTransaction().executeWithoutResult(status -> {
            fileStorage.delete(path.toString());
            assertThat(path).exists();
        });

        assertThat(path).doesNotExist();
    }

    @Test
    void deletedFileIsKeptWhenTransactionRollsBack() throws IOException {
        Path path = storedFile();

        newTransaction().executeWithoutResult(status -> {
            fileStorage.delete(path.toString());
            status.setRollbackOnly();
        });

        assertThat(path).exists();
        Files.delete(path);
    }

    @Test
    void readReturnsContentOrEmptyForMissingFile() throws IOException {
        Path path = storedFile();

        assertThat(fileStorage.read(path.toString())).contains(CONTENT);
        Files.delete(path);
        assertThat(fileStorage.read(path.toString())).isEmpty();
    }

    private Path storedFile() throws IOException {
        Path path = uploadDir().resolve("storage-test").resolve(UUID.randomUUID() + ".txt");
        Files.createDirectories(path.getParent());
        Files.write(path, CONTENT);
        return path;
    }

    private TransactionTemplate newTransaction() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }
}
