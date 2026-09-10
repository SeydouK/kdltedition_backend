CREATE TABLE categories (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            name VARCHAR(255) NOT NULL UNIQUE,
                            slug VARCHAR(255) NOT NULL UNIQUE,
                            description VARCHAR(1000),
                            date_creation TIMESTAMP NOT NULL
);

CREATE TABLE products (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          category_id BIGINT NOT NULL REFERENCES categories(id),
                          name VARCHAR(255) NOT NULL,
                          slug VARCHAR(255) NOT NULL UNIQUE,
                          description VARCHAR(2000),
                          type VARCHAR(20) NOT NULL,
                          base_price NUMERIC(10,2),
                          image_url VARCHAR(500),
                          active BOOLEAN NOT NULL DEFAULT true,
                          date_creation TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_products_category ON products(category_id);