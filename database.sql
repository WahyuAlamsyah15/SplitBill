-- Active: 1763068057657@@127.0.0.1@5432@splitbill
CREATE DATABASE splitBill;

-- 1. Tabel Resto (master resto)
CREATE TABLE restos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    name VARCHAR(150) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabel Bill (satu sesi makan)
CREATE TABLE bills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    resto_id UUID NOT NULL REFERENCES restos(id) ON DELETE RESTRICT,
    note TEXT,
    tax_percent DECIMAL(5,2) DEFAULT 0.00,
    service_percent DECIMAL(5,2) DEFAULT 0.00,
    bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Item yang dibeli
CREATE TABLE bill_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    bill_id UUID NOT NULL REFERENCES bills(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    subtotal DECIMAL(15,2) GENERATED ALWAYS AS (price * quantity) STORED
);

-- 4. Participants (orang yang ikut)
CREATE TABLE bill_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    bill_id UUID NOT NULL REFERENCES bills(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL
);

-- 5. Assignment: Siapa pesan item apa
CREATE TABLE item_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    bill_item_id UUID NOT NULL REFERENCES bill_items(id) ON DELETE CASCADE,
    participant_id UUID NOT NULL REFERENCES bill_participants(id) ON DELETE CASCADE,
    quantity_taken INTEGER NOT NULL DEFAULT 1,
    UNIQUE (bill_item_id, participant_id)
);

-- Index untuk performa
CREATE INDEX idx_bills_resto_id ON bills(resto_id);
CREATE INDEX idx_bill_items_bill_id ON bill_items(bill_id);
CREATE INDEX idx_participants_bill_id ON bill_participants(bill_id);
CREATE INDEX idx_assignments_item_id ON item_assignments(bill_item_id);
CREATE INDEX idx_assignments_participant_id ON item_assignments(participant_id);

-- Contoh data resto (biar langsung bisa test)
INSERT INTO restos (name) VALUES 
('Warung Makan Bahari'),
('Kopi Kenangan'),
('Ayam Geprek Bensu'),
('RamenYA');

-- Hapus generated column yang bikin error
ALTER TABLE bill_items DROP COLUMN IF EXISTS subtotal;
ALTER TABLE bill_items ADD COLUMN subtotal DECIMAL(15,2) DEFAULT 0;

ALTER TABLE restos ADD COLUMN deleted BOOLEAN DEFAULT FALSE;

-- Hapus constraint lama
ALTER TABLE bills DROP CONSTRAINT IF EXISTS fk_resto;

-- Buat constraint baru dengan ON DELETE CASCADE
ALTER TABLE bills 
    ADD CONSTRAINT fk_resto 
    FOREIGN KEY (resto_id) 
    REFERENCES restos(id) 
    ON DELETE CASCADE;

-- Atau kalau mau generated column tetap hidup, pakai BigDecimal juga:
-- ALTER TABLE bill_items ALTER COLUMN price TYPE DECIMAL(15,2);
-- ALTER TABLE bill_items ALTER COLUMN subtotal TYPE DECIMAL(15,2);

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    app_user_role VARCHAR(50) NOT NULL,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_db_name TEXT NOT NULL UNIQUE
);


