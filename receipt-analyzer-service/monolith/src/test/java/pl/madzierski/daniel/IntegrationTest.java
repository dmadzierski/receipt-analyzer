package pl.madzierski.daniel;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.ThrowableAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTest {

    protected static final UUID UNKNOWN_ID = new UUID(0, 0);

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    static {
        POSTGRES.start();
    }

    private static final Path UPLOAD_DIR = Path.of("build", "test-uploads").toAbsolutePath();

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected EntityManager entityManager;

    @DynamicPropertySource
    static void uploadDirProperty(DynamicPropertyRegistry registry) {
        registry.add("file.upload-dir", UPLOAD_DIR::toString);
    }

    protected static Path uploadDir() {
        return UPLOAD_DIR;
    }

    protected void flush() {
        entityManager.flush();
        entityManager.clear();
    }

    protected byte[] readResource(String path) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new IllegalArgumentException("Test resource not found: " + path);
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected UUID createUser(String userSub) {
        return jdbcTemplate.queryForObject(
            "INSERT INTO app_user (id, created_date, user_sub) VALUES (gen_random_uuid(), now(), ?) RETURNING id",
            UUID.class, userSub);
    }

    protected UUID createWallet(UUID userId, String name) {
        return jdbcTemplate.queryForObject(
            "INSERT INTO wallet (id, created_date, name, user_id) VALUES (gen_random_uuid(), now(), ?, ?) RETURNING id",
            UUID.class, name, userId);
    }

    protected UUID insertReceipt(UUID walletId, UUID storeId, String name) {
        return jdbcTemplate.queryForObject(
            "INSERT INTO receipt (id, created_date, name, wallet_id, store_id) VALUES (gen_random_uuid(), now(), ?, ?, ?) RETURNING id",
            UUID.class, name, walletId, storeId);
    }

    protected UUID insertRevision(UUID receiptId, String resolver, String totalPrice, boolean isCorrect, LocalDateTime paymentDate) {
        return jdbcTemplate.queryForObject(
            "INSERT INTO receipt_revision (id, created_date, receipt_id, resolver, total_price, is_correct, is_preferred_revision, payment_date) "
                + "VALUES (gen_random_uuid(), now(), ?, ?, CAST(? AS numeric), ?, false, ?) RETURNING id",
            UUID.class, receiptId, resolver, totalPrice, isCorrect, paymentDate);
    }

    protected UUID insertItem(UUID revisionId, String name, UUID productId) {
        return jdbcTemplate.queryForObject(
            "INSERT INTO receipt_item (id, created_date, name, receipt_revision_id, product_id, position) "
                + "VALUES (gen_random_uuid(), now(), ?, ?, ?, 1) RETURNING id",
            UUID.class, name, revisionId, productId);
    }

    protected UUID createStore(String brandName, String address, String city) {
        UUID brandId = jdbcTemplate.queryForObject(
            "INSERT INTO store_brand (id, created_date, name) VALUES (gen_random_uuid(), now(), ?) RETURNING id",
            UUID.class, brandName);
        return jdbcTemplate.queryForObject(
            "INSERT INTO store (id, created_date, address, city, postal_code, country, brand_id) "
                + "VALUES (gen_random_uuid(), now(), ?, ?, '60-001', 'Polska', ?) RETURNING id",
            UUID.class, address, city, brandId);
    }

    protected UUID insertProduct(String name) {
        return jdbcTemplate.queryForObject(
            "INSERT INTO product (id, created_date, name) VALUES (gen_random_uuid(), now(), ?) RETURNING id",
            UUID.class, name);
    }

    protected UUID insertAlias(UUID productId, String name) {
        return jdbcTemplate.queryForObject(
            "INSERT INTO product_alias (id, created_date, name, product_id) VALUES (gen_random_uuid(), now(), ?, ?) RETURNING id",
            UUID.class, name, productId);
    }

    protected int count(String sql, Object... args) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return count != null ? count : 0;
    }

    protected static void assertError(ThrowableAssert.ThrowingCallable call, AppRuntimeExceptionMessages expected) {
        assertThatThrownBy(call)
            .isInstanceOf(AppRuntimeException.class)
            .extracting(e -> ((AppRuntimeException) e).getType())
            .isEqualTo(expected);
    }
}
