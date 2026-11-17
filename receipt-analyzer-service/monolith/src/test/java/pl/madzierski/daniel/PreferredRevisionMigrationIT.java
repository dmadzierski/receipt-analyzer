package pl.madzierski.daniel;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PreferredRevisionMigrationIT {

    private static final String SCHEMA = "preferred_revision_migration";

    private DriverManagerDataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void migrateSchemaToVersionBeforePreferredRevisionMove() {
        dataSource = new DriverManagerDataSource(IntegrationTest.POSTGRES.getJdbcUrl(),
            IntegrationTest.POSTGRES.getUsername(), IntegrationTest.POSTGRES.getPassword());
        dataSource.setSchema(SCHEMA);
        jdbcTemplate = new JdbcTemplate(dataSource);
        flyway("8").migrate();
    }

    @AfterEach
    void dropSchema() {
        jdbcTemplate.execute("DROP SCHEMA " + SCHEMA + " CASCADE");
    }

    @Test
    void preferredFlagOfRevisionBecomesPreferredRevisionOfReceipt() {
        UUID userId = insert("INSERT INTO app_user (id, created_date, user_sub) VALUES (gen_random_uuid(), now(), 'migration') RETURNING id");
        UUID walletId = insert("INSERT INTO wallet (id, created_date, name, user_id) VALUES (gen_random_uuid(), now(), 'Home', ?) RETURNING id", userId);
        UUID withPreferred = insertReceipt(walletId, userId);
        UUID withoutPreferred = insertReceipt(walletId, userId);
        insertRevision(withPreferred, false);
        UUID preferred = insertRevision(withPreferred, true);
        insertRevision(withoutPreferred, false);

        flyway("9").migrate();

        assertThat(preferredRevisionOf(withPreferred)).isEqualTo(preferred);
        assertThat(preferredRevisionOf(withoutPreferred)).isNull();
        assertThat(jdbcTemplate.queryForObject(
            "SELECT count(*) FROM information_schema.columns WHERE table_schema = ? AND table_name = 'receipt_revision' "
                + "AND column_name = 'is_preferred_revision'", Integer.class, SCHEMA)).isZero();
    }

    private Flyway flyway(String target) {
        return Flyway.configure()
            .dataSource(dataSource)
            .schemas(SCHEMA)
            .locations("classpath:db/migration")
            .target(target)
            .load();
    }

    private UUID insertReceipt(UUID walletId, UUID ownerId) {
        return insert("INSERT INTO receipt (id, created_date, name, wallet_id, owner_id) "
            + "VALUES (gen_random_uuid(), now(), 'Receipt', ?, ?) RETURNING id", walletId, ownerId);
    }

    private UUID insertRevision(UUID receiptId, boolean preferred) {
        return insert("INSERT INTO receipt_revision (id, created_date, receipt_id, resolver, is_preferred_revision) "
            + "VALUES (gen_random_uuid(), now(), ?, 'USER', ?) RETURNING id", receiptId, preferred);
    }

    private UUID preferredRevisionOf(UUID receiptId) {
        return jdbcTemplate.queryForObject("SELECT preferred_revision_id FROM receipt WHERE id = ?", UUID.class, receiptId);
    }

    private UUID insert(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, UUID.class, args);
    }
}
