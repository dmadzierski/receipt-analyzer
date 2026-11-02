ALTER TABLE wallet ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE receipt ALTER COLUMN wallet_id SET NOT NULL;
ALTER TABLE receipt_file_group ALTER COLUMN receipt_id SET NOT NULL;

ALTER TABLE receipt ADD COLUMN owner_id UUID;
UPDATE receipt r SET owner_id = w.user_id FROM wallet w WHERE w.id = r.wallet_id;
ALTER TABLE receipt ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE receipt ADD CONSTRAINT fk_receipt_owner FOREIGN KEY (owner_id) REFERENCES app_user (id);
CREATE INDEX ix_receipt_owner_id ON receipt (owner_id);

ALTER TABLE receipt_file_group ADD COLUMN owner_id UUID;
UPDATE receipt_file_group g SET owner_id = r.owner_id FROM receipt r WHERE r.id = g.receipt_id;
ALTER TABLE receipt_file_group ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE receipt_file_group ADD CONSTRAINT fk_receipt_file_group_owner FOREIGN KEY (owner_id) REFERENCES app_user (id);
CREATE INDEX ix_receipt_file_group_owner_id ON receipt_file_group (owner_id);

ALTER TABLE receipt_file ALTER COLUMN path TYPE VARCHAR(1024);
