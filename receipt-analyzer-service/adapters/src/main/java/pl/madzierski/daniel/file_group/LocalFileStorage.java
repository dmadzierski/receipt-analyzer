package pl.madzierski.daniel.file_group;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Service
class LocalFileStorage implements FileStorage {

    @Override
    public void save(String path, byte[] content) {
        Path destination = Path.of(path);
        try {
            if (destination.getParent() != null) Files.createDirectories(destination.getParent());
            Files.write(destination, content);
        } catch (IOException e) {
            throw new AppRuntimeException(AppRuntimeExceptionMessages.ERROR_DURING_SAVING_FILE);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) deleteQuietly(destination);
                }
            });
        }
    }

    @Override
    public Optional<byte[]> read(String path) {
        Path source = Path.of(path);
        if (!Files.isRegularFile(source)) return Optional.empty();
        try {
            return Optional.of(Files.readAllBytes(source));
        } catch (IOException e) {
            log.warn("Could not read file {}", source, e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String path) {
        Path target = Path.of(path);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteQuietly(target);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteQuietly(target);
            }
        });
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete file {}", path, e);
        }
    }
}
