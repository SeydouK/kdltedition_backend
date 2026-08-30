CREATE TABLE users (
     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     photo_url VARCHAR(500),
     email VARCHAR(255) NOT NULL UNIQUE,
     mot_de_passe_hash VARCHAR(255) NOT NULL,
     first_name VARCHAR(100) NOT NULL,
     last_name VARCHAR(100) NOT NULL,
     phone_number VARCHAR(30),
     role VARCHAR(20) NOT NULL,
     active BOOLEAN NOT NULL DEFAULT true,
     date_creation TIMESTAMP NOT NULL,
     updated_at TIMESTAMP NOT NULL
);