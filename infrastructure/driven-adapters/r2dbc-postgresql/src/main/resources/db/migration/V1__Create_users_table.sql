-- Migración V1: Creación de tabla users

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birthday DATE,
    address TEXT,
    email VARCHAR(255) UNIQUE NOT NULL,
    identification VARCHAR(50) UNIQUE NOT NULL,
    phone VARCHAR(20),
    base_salary DECIMAL(15,2)
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_identification ON users(identification);
