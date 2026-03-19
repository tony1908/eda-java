-- Wait for SQL Server to be ready, then:

-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'eventflow')
    CREATE DATABASE eventflow;
GO

USE eventflow;
GO

-- Products table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'products')
CREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(1000),
    price DECIMAL(10,2) NOT NULL,
    category NVARCHAR(100),
    stock INT NOT NULL DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);
GO

-- Enable CDC at database level
IF NOT EXISTS (SELECT 1 FROM sys.databases WHERE name = 'eventflow' AND is_cdc_enabled = 1)
    EXEC sys.sp_cdc_enable_db;
GO

-- Enable CDC on products table
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE is_tracked_by_cdc = 1 AND name = 'products')
    EXEC sys.sp_cdc_enable_table
        @source_schema = N'dbo',
        @source_name = N'products',
        @role_name = NULL,
        @supports_net_changes = 1;
GO

-- Outbox table for trigger-based CDC strategy
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'product_outbox')
CREATE TABLE product_outbox (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    operation VARCHAR(10) NOT NULL, -- INSERT, UPDATE
    payload NVARCHAR(MAX) NOT NULL,
    processed BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE()
);
GO

-- Trigger for outbox strategy
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_products_outbox')
    DROP TRIGGER trg_products_outbox;
GO

CREATE TRIGGER trg_products_outbox
ON products
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @operation VARCHAR(10);
    IF EXISTS (SELECT 1 FROM deleted)
        SET @operation = 'UPDATE';
    ELSE
        SET @operation = 'INSERT';

    INSERT INTO product_outbox (product_id, operation, payload)
    SELECT
        i.id,
        @operation,
        (SELECT i.id AS productId, i.name, i.description, i.price, i.category, i.stock, i.updated_at AS updatedAt FOR JSON PATH, WITHOUT_ARRAY_WRAPPER)
    FROM inserted i;
END;
GO

-- Seed data
IF NOT EXISTS (SELECT 1 FROM products)
BEGIN
    INSERT INTO products (id, name, description, price, category, stock)
    VALUES
        ('PROD-001', 'Laptop Gamer Pro', 'Laptop de alto rendimiento para gaming y desarrollo', 1299.99, 'Computadoras', 50),
        ('PROD-002', 'Teclado Mecanico RGB', 'Teclado mecanico con switches Cherry MX y retroiluminacion RGB', 149.99, 'Perifericos', 200),
        ('PROD-003', 'Monitor 4K UltraWide', 'Monitor curvo 34 pulgadas con resolucion 4K', 599.99, 'Monitores', 30);
END;
GO
