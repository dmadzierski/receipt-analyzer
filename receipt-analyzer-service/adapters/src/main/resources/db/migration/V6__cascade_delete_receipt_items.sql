ALTER TABLE receipt_item DROP CONSTRAINT fk_receipt_item_receipt_revision;
ALTER TABLE receipt_item ADD CONSTRAINT fk_receipt_item_receipt_revision
    FOREIGN KEY (receipt_revision_id) REFERENCES receipt_revision (id) ON DELETE CASCADE;
