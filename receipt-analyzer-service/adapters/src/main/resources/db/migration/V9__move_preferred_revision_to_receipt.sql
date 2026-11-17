ALTER TABLE receipt ADD COLUMN preferred_revision_id UUID;

UPDATE receipt r
SET preferred_revision_id = rr.id
FROM receipt_revision rr
WHERE rr.receipt_id = r.id
  AND rr.is_preferred_revision = true;

ALTER TABLE receipt
    ADD CONSTRAINT fk_receipt_preferred_revision FOREIGN KEY (preferred_revision_id)
        REFERENCES receipt_revision (id) ON DELETE SET NULL;

CREATE INDEX ix_receipt_preferred_revision_id ON receipt (preferred_revision_id);

DROP INDEX ux_receipt_revision_preferred;

ALTER TABLE receipt_revision DROP COLUMN is_preferred_revision;
