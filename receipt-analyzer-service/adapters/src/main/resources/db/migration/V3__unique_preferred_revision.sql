CREATE UNIQUE INDEX ux_receipt_revision_preferred
    ON receipt_revision (receipt_id) WHERE is_preferred_revision = true;
