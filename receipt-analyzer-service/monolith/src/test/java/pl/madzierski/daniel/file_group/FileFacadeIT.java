package pl.madzierski.daniel.file_group;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.model.FileGroupDto;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileFacadeIT extends IntegrationTest {

    private static final String USER_SUB = "test-user";
    private static final byte[] JSON = "{\"body\": []}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PNG = png();

    @Autowired
    private FileFacade fileFacade;

    private String receiptId;

    @BeforeEach
    void createReceipt() {
        receiptId = insertReceipt(createWallet(createUser(USER_SUB), "Home"), null, "Receipt");
    }

    @Test
    void savesFilesAsOneGroupAndReadsThemBack() {
        FileGroupDto group = fileFacade.save(receiptId, List.of(PNG, PNG), USER_SUB);
        flush();

        assertThat(group.fileType()).isEqualTo(FileType.PNG);
        assertThat(group.isOriginal()).isTrue();
        assertThat(fileFacade.getFiles(group.id())).hasSize(2).allSatisfy(content -> assertThat(content).isEqualTo(PNG));
    }

    @Test
    void recognisesJsonFile() {
        assertThat(fileFacade.save(receiptId, List.of(JSON), USER_SUB).fileType()).isEqualTo(FileType.JSON);
    }

    @Test
    void rejectsGroupWithDifferentFileTypes() {
        assertFileError(List.of(PNG, JSON), AppRuntimeExceptionMessages.FILE_TYPE_MISMATCH);
    }

    @Test
    void rejectsUnsupportedFileType() {
        assertFileError(List.of("just text".getBytes(StandardCharsets.UTF_8)), AppRuntimeExceptionMessages.FILE_TYPE_NOT_DETECTED);
    }

    @Test
    void rejectsEmptyFileAndMissingFiles() {
        assertFileError(List.of(new byte[0]), AppRuntimeExceptionMessages.FILE_EMPTY);
        assertFileError(List.of(), AppRuntimeExceptionMessages.FILE_NOT_FOUND);
    }

    @Test
    void deletesFileGroupWithItsFiles() {
        String groupId = fileFacade.save(receiptId, List.of(PNG), USER_SUB).id();
        flush();

        fileFacade.deleteFileGroupId(groupId, USER_SUB);
        flush();

        assertThat(count("SELECT count(*) FROM receipt_file_group WHERE id = ?", groupId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_file WHERE file_group_id = ?", groupId)).isZero();
    }

    @Test
    void fileGroupUsedAsRevisionSourceCannotBeDeleted() {
        String groupId = fileFacade.save(receiptId, List.of(JSON), USER_SUB).id();
        flush();
        String revisionId = insertRevision(receiptId, "BIEDRONKA_JSON", "0", false, null);
        jdbcTemplate.update("UPDATE receipt_revision SET source_file_group_id = ? WHERE id = ?", groupId, revisionId);

        assertError(() -> fileFacade.deleteFileGroupId(groupId, USER_SUB), AppRuntimeExceptionMessages.FILE_GROUP_USED_BY_REVISION);
    }

    @Test
    void readsSingleFileFromDisk() throws IOException {
        String fileId = fileIdOf(fileFacade.save(receiptId, List.of(PNG), USER_SUB).id());

        assertThat(fileFacade.getFile(fileId).getContentAsByteArray()).isEqualTo(PNG);
    }

    @Test
    void unknownFileIsReported() {
        assertError(() -> fileFacade.getFile(UNKNOWN_ID), AppRuntimeExceptionMessages.FILE_RECEIPT_NOT_FOUND);
        assertError(() -> fileFacade.getFiles(UNKNOWN_ID), AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND);
    }

    @Test
    void fileWithoutPathIsReported() {
        String fileId = fileIdOf(fileFacade.save(receiptId, List.of(PNG), USER_SUB).id());
        jdbcTemplate.update("UPDATE receipt_file SET path = NULL WHERE id = ?", fileId);

        assertError(() -> fileFacade.getFile(fileId), AppRuntimeExceptionMessages.FILE_RECEIPT_PATH_NOT_FOUND);
    }

    @Test
    void fileMissingOnDiskIsReported() throws IOException {
        String groupId = fileFacade.save(receiptId, List.of(PNG), USER_SUB).id();
        String fileId = fileIdOf(groupId);
        Files.delete(Path.of(jdbcTemplate.queryForObject("SELECT path FROM receipt_file WHERE id = ?", String.class, fileId)));

        assertError(() -> fileFacade.getFile(fileId), AppRuntimeExceptionMessages.FILE_RECEIPT_FILE_NOT_FOUND);
        assertError(() -> fileFacade.getFiles(groupId), AppRuntimeExceptionMessages.FILE_RECEIPT_FILE_NOT_FOUND);
    }

    private String fileIdOf(String groupId) {
        flush();
        return jdbcTemplate.queryForObject("SELECT id FROM receipt_file WHERE file_group_id = ?", String.class, groupId);
    }

    private void assertFileError(List<byte[]> files, AppRuntimeExceptionMessages expected) {
        assertError(() -> fileFacade.save(receiptId, files, USER_SUB), expected);
    }

    private static byte[] png() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
