ALTER TABLE reviews
    ADD COLUMN variant_id BIGINT REFERENCES product_variants(id) ON DELETE RESTRICT,
    ADD COLUMN height_cm SMALLINT CHECK (height_cm IS NULL OR height_cm BETWEEN 100 AND 250),
    ADD COLUMN weight_kg NUMERIC(5, 2) CHECK (weight_kg IS NULL OR weight_kg BETWEEN 20 AND 300);

CREATE INDEX ix_reviews_variant_id ON reviews(variant_id);
