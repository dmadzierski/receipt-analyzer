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

    private TransactionTemplate newTransaction() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }
}
