CREATE TABLE roles (
    id CHAR(36) NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    permissions JSON,
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    id CHAR(36) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role_id CHAR(36) NOT NULL,
    last_login DATETIME(3),
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3),
    PRIMARY KEY (id),
    KEY idx_users_role_id (role_id),
    KEY idx_users_email (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO roles (id, name) VALUES
    (UUID(), 'ADMIN'),
    (UUID(), 'TEACHER'),
    (UUID(), 'STUDENT'),
    (UUID(), 'PARENT');

INSERT INTO users (id, email, password_hash, first_name, last_name, role_id)
SELECT UUID(), 'admin@school.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCQqb9QxhFJpUuWq5KiZnGS',
    'System', 'Admin', id
FROM roles WHERE name = 'ADMIN' LIMIT 1;
