ALTER TABLE receipt_file DROP CONSTRAINT fk_receipt_file_file_group;
ALTER TABLE receipt_file ADD CONSTRAINT fk_receipt_file_file_group
    FOREIGN KEY (file_group_id) REFERENCES receipt_file_group (id) ON DELETE CASCADE;
