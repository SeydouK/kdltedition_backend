CREATE TABLE carts (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
                       date_creation TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL
);

CREATE TABLE cart_items (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
                            product_id BIGINT NOT NULL REFERENCES products(id),
                            quantity INT NOT NULL DEFAULT 1
);

CREATE TABLE orders (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        user_id BIGINT NOT NULL REFERENCES users(id),
                        total_amount NUMERIC(10,2) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        date_creation TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP NOT NULL
);

CREATE TABLE order_items (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id BIGINT NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             unit_price NUMERIC(10,2) NOT NULL,
                             quantity INT NOT NULL
);

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);