ALTER TABLE orders
    ADD COLUMN delivery_phone     VARCHAR(30),
    ADD COLUMN delivery_address   VARCHAR(300),
    ADD COLUMN delivery_city      VARCHAR(100),
    ADD COLUMN delivery_notes     VARCHAR(500),
    ADD COLUMN delivery_latitude  NUMERIC(9,6),
    ADD COLUMN delivery_longitude NUMERIC(9,6);