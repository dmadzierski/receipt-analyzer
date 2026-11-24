package pl.madzierski.daniel.receipt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.digitization.ReceiptResolverStrategyType;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.FileFacade;
import pl.madzierski.daniel.receipt.model.CreateReceiptRequest;
import pl.madzierski.daniel.user.model.AuthenticatedUser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ReceiptFilesOnDiskIT extends IntegrationTest {

    private static final String RECEIPT_JSON = "receipts/biedronka/biedronka_e_receipt.json";

    @Autowired
    private ReceiptFacade receiptFacade;

    @Autowired
    private FileFacade fileFacade;

    private UUID userId;
    private UUID walletId;
    private UUID storeId;
    private AuthenticatedUser user;

    @BeforeEach
    void createOwnerWalletAndStore() {
        userId = createUser("files-on-disk-" + UUID.randomUUID());
        user = new AuthenticatedUser(userId);
        walletId = createWallet(userId, "Files wallet");
        storeId = createStore("Biedronka", "ul. Plikowa 1", "Poznań");
    }

    @AfterEach
    void removeCommittedData() throws IOException {
        jdbcTemplate.update("DELETE FROM receipt_revision WHERE receipt_id IN (SELECT id FROM receipt WHERE owner_id = ?)", userId);
        jdbcTemplate.update("DELETE FROM receipt_file_group WHERE owner_id = ?", userId);
        jdbcTemplate.update("DELETE FROM receipt WHERE owner_id = ?", userId);
        jdbcTemplate.update("DELETE FROM wallet WHERE id = ?", walletId);
        UUID brandId = jdbcTemplate.queryForObject("SELECT brand_id FROM store WHERE id = ?", UUID.class, storeId);
        jdbcTemplate.update("DELETE FROM store WHERE id = ?", storeId);
        jdbcTemplate.update("DELETE FROM store_brand WHERE id = ?", brandId);
        jdbcTemplate.update("DELETE FROM app_user WHERE id = ?", userId);
        deleteRecursively(userUploadDir());
    }

    @Test
    void deletingReceiptRemovesFilesOfAllItsGroupsFromDiskAfterCommit() {
        UUID receiptId = addReceipt(storeId);
        fileFacade.save(user, receiptId, List.of(png(1), png(2)));
        List<Path> files = filesOfReceipt(receiptId);
        assertThat(files).hasSize(3).allSatisfy(file -> assertThat(file).exists());

        receiptFacade.deleteReceipt(user, receiptId);

        assertThat(files).allSatisfy(file -> assertThat(file).doesNotExist());
        assertThat(count("SELECT count(*) FROM receipt WHERE id = ?", receiptId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_file f JOIN receipt_file_group g ON g.id = f.file_group_id WHERE g.owner_id = ?",
            userId)).isZero();
    }

    @Test
    void deletingFileGroupRemovesOnlyItsOwnFilesFromDisk() {
        UUID receiptId = addReceipt(storeId);
        List<Path> receiptJson = filesOfReceipt(receiptId);
        UUID imagesGroupId = fileFacade.save(user, receiptId, List.of(png(1), png(2)));
        List<Path> images = filesOfGroup(imagesGroupId);

        fileFacade.deleteFileGroupId(user, imagesGroupId);

        assertThat(images).hasSize(2).allSatisfy(file -> assertThat(file).doesNotExist());
        assertThat(receiptJson).singleElement().satisfies(file -> assertThat(file).exists());
        assertThat(count("SELECT count(*) FROM receipt_file WHERE file_group_id = ?", imagesGroupId)).isZero();
    }

    @Test
    void fileGroupUsedByRevisionIsKeptInDatabaseAndOnDisk() {
        UUID receiptId = addReceipt(storeId);
        UUID groupId = jdbcTemplate.queryForObject("SELECT id FROM receipt_file_group WHERE receipt_id = ?", UUID.class, receiptId);
        List<Path> files = filesOfGroup(groupId);

        assertError(() -> fileFacade.deleteFileGroupId(user, groupId), AppRuntimeExceptionMessages.FILE_GROUP_USED_BY_REVISION);

        assertThat(files).singleElement().satisfies(file -> assertThat(file).exists());
        assertThat(count("SELECT count(*) FROM receipt_file WHERE file_group_id = ?", groupId)).isEqualTo(1);
    }

    @Test
    void failedReceiptCreationLeavesNoRowsAndNoFilesOnDisk() {
        assertThatThrownBy(() -> addReceipt(UNKNOWN_ID)).isInstanceOf(RuntimeException.class);

        assertThat(count("SELECT count(*) FROM receipt WHERE owner_id = ?", userId)).isZero();
        assertThat(count("SELECT count(*) FROM receipt_file_group WHERE owner_id = ?", userId)).isZero();
        assertThat(filesIn(userUploadDir())).isEmpty();
    }

    private UUID addReceipt(UUID storeId) {
        return receiptFacade.addReceipt(user, List.of(readResource(RECEIPT_JSON)),
            new CreateReceiptRequest("Files receipt", null, ReceiptResolverStrategyType.BIEDRONKA_JSON, walletId, storeId)).id();
    }

    private List<Path> filesOfReceipt(UUID receiptId) {
        return jdbcTemplate.queryForList(
                "SELECT f.path FROM receipt_file f JOIN receipt_file_group g ON g.id = f.file_group_id WHERE g.receipt_id = ?",
                String.class, receiptId)
            .stream().map(path -> uploadDir().resolve(path)).toList();
    }

    private List<Path> filesOfGroup(UUID groupId) {
        return jdbcTemplate.queryForList("SELECT path FROM receipt_file WHERE file_group_id = ?", String.class, groupId)
            .stream().map(path -> uploadDir().resolve(path)).toList();
    }

    private Path userUploadDir() {
        return uploadDir().resolve("user").resolve(userId.toString());
    }

    private static List<Path> filesIn(Path dir) {
        if (!Files.exists(dir)) return List.of();
        try (Stream<Path> paths = Files.walk(dir)) {
            return paths.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
        }
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
