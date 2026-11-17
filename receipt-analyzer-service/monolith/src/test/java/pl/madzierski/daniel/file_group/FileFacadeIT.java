package pl.madzierski.daniel.file_group;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.model.FileContentDto;
import pl.madzierski.daniel.user.model.AuthenticatedUser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class FileFacadeIT extends IntegrationTest {

    private static final String USER_SUB = "test-user";
    private static final byte[] JSON = "{\"body\": []}".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PNG = png(1);

    @Autowired
    private FileFacade fileFacade;

    private AuthenticatedUser user;
    private UUID receiptId;

    @BeforeEach
    void createReceipt() {
        UUID userId = createUser(USER_SUB);
        user = new AuthenticatedUser(userId);
        receiptId = insertReceipt(createWallet(userId, "Home"), null, "Receipt");
    }

    @Test
    void savesFilesAsOneGroupAndReadsThemBack() {
        UUID groupId = fileFacade.save(user, receiptId, List.of(PNG, PNG));
        flush();

        assertThat(fileTypeOf(groupId)).isEqualTo(FileType.PNG.name());
        assertThat(fileFacade.getFiles(receiptId, groupId)).hasSize(2).allSatisfy(content -> assertThat(content).isEqualTo(PNG));
    }

    @Test
    void filesOfGroupAreReadInTheOrderTheyWereUploaded() {
        List<byte[]> pages = IntStream.rangeClosed(1, 8).mapToObj(FileFacadeIT::png).toList();
        UUID groupId = fileFacade.save(user, receiptId, pages);
        flush();

        assertThat(fileFacade.getFiles(receiptId, groupId)).containsExactlyElementsOf(pages);
    }

    @Test
    void savedFilesGetPathsBuiltFromOwnerReceiptGroupAndFileIds() {
        UUID groupId = fileFacade.save(user, receiptId, List.of(PNG));
        UUID fileId = fileIdOf(groupId);

        assertThat(jdbcTemplate.queryForObject("SELECT path FROM receipt_file WHERE id = ?", String.class, fileId))
            .endsWith(String.join(java.io.File.separator, "user", user.id().toString(), "receipts", receiptId.toString(),
                "file_group", groupId.toString(), "file", fileId + ".png"));
    }

    @Test
    void recognisesJsonFile() {
        UUID groupId = fileFacade.save(user, receiptId, List.of(JSON));
        flush();

        assertThat(fileTypeOf(groupId)).isEqualTo(FileType.JSON.name());
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
        UUID groupId = fileFacade.save(user, receiptId, List.of(PNG));
        flush();

        fileFacade.deleteFileGroupId(user, groupId);
        flush();

        assertThat(count("SELECT count(*) FROM receipt_file_group WHERE id = ?", groupId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_file WHERE file_group_id = ?", groupId)).isZero();
    }

    @Test
    void fileGroupUsedAsRevisionSourceCannotBeDeleted() {
        UUID groupId = fileFacade.save(user, receiptId, List.of(JSON));
        flush();
        UUID revisionId = insertRevision(receiptId, "BIEDRONKA_JSON", "0", false, null);
        jdbcTemplate.update("UPDATE receipt_revision SET source_file_group_id = ? WHERE id = ?", groupId, revisionId);

        assertError(() -> fileFacade.deleteFileGroupId(user, groupId), AppRuntimeExceptionMessages.FILE_GROUP_USED_BY_REVISION);
    }

    @Test
    void readsSingleFileFromDisk() {
        UUID fileId = fileIdOf(fileFacade.save(user, receiptId, List.of(PNG)));

        FileContentDto file = fileFacade.getFile(user, fileId);

        assertThat(file.name()).isEqualTo(fileId + ".png");
        assertThat(file.content()).isEqualTo(PNG);
    }

    @Test
    void unknownFileIsReported() {
        assertError(() -> fileFacade.getFile(user, UNKNOWN_ID), AppRuntimeExceptionMessages.FILE_RECEIPT_NOT_FOUND);
        assertError(() -> fileFacade.getFiles(receiptId, UNKNOWN_ID), AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND);
    }

    @Test
    void fileWithoutPathIsReported() {
        UUID fileId = fileIdOf(fileFacade.save(user, receiptId, List.of(PNG)));
        jdbcTemplate.update("UPDATE receipt_file SET path = NULL WHERE id = ?", fileId);

        assertError(() -> fileFacade.getFile(user, fileId), AppRuntimeExceptionMessages.FILE_RECEIPT_PATH_NOT_FOUND);
    }

    @Test
    void fileMissingOnDiskIsReported() throws IOException {
        UUID groupId = fileFacade.save(user, receiptId, List.of(PNG));
        UUID fileId = fileIdOf(groupId);
        Files.delete(Path.of(jdbcTemplate.queryForObject("SELECT path FROM receipt_file WHERE id = ?", String.class, fileId)));

        assertError(() -> fileFacade.getFile(user, fileId), AppRuntimeExceptionMessages.FILE_RECEIPT_FILE_NOT_FOUND);
        assertError(() -> fileFacade.getFiles(receiptId, groupId), AppRuntimeExceptionMessages.FILE_RECEIPT_FILE_NOT_FOUND);
    }

    @Test
    void otherUserCannotReadFile() {
        UUID fileId = fileIdOf(fileFacade.save(user, receiptId, List.of(PNG)));

        assertError(() -> fileFacade.getFile(stranger(), fileId), AppRuntimeExceptionMessages.FILE_RECEIPT_NOT_FOUND);
    }

    @Test
    void otherUserCannotUploadFilesToReceipt() {
        assertError(() -> fileFacade.save(stranger(), receiptId, List.of(PNG)), AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND);
        flush();

        assertThat(count("SELECT count(*) FROM receipt_file_group WHERE receipt_id = ?", receiptId)).isZero();
    }

    @Test
    void otherUserCannotDeleteFileGroup() {
        UUID groupId = fileFacade.save(user, receiptId, List.of(PNG));
        flush();

        assertError(() -> fileFacade.deleteFileGroupId(stranger(), groupId), AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND);
        flush();

        assertThat(count("SELECT count(*) FROM receipt_file_group WHERE id = ?", groupId)).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM receipt_file WHERE file_group_id = ?", groupId)).isEqualTo(1);
    }

    @Test
    void fileGroupIsReadOnlyThroughItsOwnReceipt() {
        UUID groupId = fileFacade.save(user, receiptId, List.of(PNG));
        UUID otherReceiptId = insertReceipt(createWallet(createUser("stranger-2"), "Other"), null, "Other receipt");
        flush();

        assertError(() -> fileFacade.getFiles(otherReceiptId, groupId), AppRuntimeExceptionMessages.FILE_GROUP_NOT_FOUND);
    }

    private AuthenticatedUser stranger() {
        return new AuthenticatedUser(createUser("stranger"));
    }

    private UUID fileIdOf(UUID groupId) {
        flush();
        return jdbcTemplate.queryForObject("SELECT id FROM receipt_file WHERE file_group_id = ?", UUID.class, groupId);
    }

    private String fileTypeOf(UUID groupId) {
        return jdbcTemplate.queryForObject("SELECT file_type FROM receipt_file_group WHERE id = ?", String.class, groupId);
    }

    private void assertFileError(List<byte[]> files, AppRuntimeExceptionMessages expected) {
        assertError(() -> fileFacade.save(user, receiptId, files), expected);
    }

    private static byte[] png(int size) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB), "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
