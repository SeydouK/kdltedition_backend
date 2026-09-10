CREATE TABLE quotes (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        user_id BIGINT NOT NULL REFERENCES users(id),
                        product_id BIGINT NOT NULL REFERENCES products(id),
                        quantity INT NOT NULL,
                        specifications VARCHAR(2000),
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        proposed_price NUMERIC(10,2),
                        staff_response VARCHAR(2000),
                        date_creation TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_quotes_user ON quotes(user_id);
CREATE INDEX idx_quotes_status ON quotes(status);