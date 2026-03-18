CREATE DATABASE eventflow;
GO

USE eventflow;
GO

CREATE TABLE products (
    id VARCHAR(8) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    price FLOAT NOT NULL,
    category NVARCHAR(100),
    stock INT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- Outbox table for trigger-outbox CDC strategy
CREATE TABLE product_outbox (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id VARCHAR(8) NOT NULL,
    operation VARCHAR(10) NOT NULL,
    processed BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- Trigger: capture INSERTs on products
CREATE TRIGGER trg_products_insert ON products
AFTER INSERT AS
BEGIN
    INSERT INTO product_outbox (product_id, operation)
    SELECT id, 'INSERT' FROM inserted;
END;
GO

-- Trigger: capture UPDATEs on products
CREATE TRIGGER trg_products_update ON products
AFTER UPDATE AS
BEGIN
    INSERT INTO product_outbox (product_id, operation)
    SELECT id, 'UPDATE' FROM inserted;
END;
GO

-- Enable CDC on the database
EXEC sys.sp_cdc_enable_db;
GO

-- Enable CDC on the products table
EXEC sys.sp_cdc_enable_table
    @source_schema = N'dbo',
    @source_name = N'products',
    @role_name = NULL;
GO
