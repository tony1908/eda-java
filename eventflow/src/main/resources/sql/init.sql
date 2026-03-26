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

-- Event Store table (Module 9: Event Sourcing — fintech-style con concurrencia optimista)
CREATE TABLE event_store (
    global_position BIGINT IDENTITY(1,1) PRIMARY KEY,
    stream_id       VARCHAR(50)   NOT NULL,
    stream_position INT           NOT NULL,
    aggregate_type  VARCHAR(50)   NOT NULL,
    event_type      VARCHAR(100)  NOT NULL,
    payload         NVARCHAR(MAX) NOT NULL,
    metadata        NVARCHAR(MAX) NOT NULL DEFAULT '{}',
    occurred_at     DATETIME2     NOT NULL DEFAULT GETDATE(),

    CONSTRAINT uq_stream_position UNIQUE (stream_id, stream_position)
);
GO

CREATE INDEX idx_event_store_stream ON event_store (stream_id, stream_position);
GO

-- Order Snapshots table (optimization for Event Sourcing)
CREATE TABLE order_snapshots (
    order_id VARCHAR(50) PRIMARY KEY,
    status VARCHAR(30) NOT NULL,
    customer_id VARCHAR(50),
    total_amount FLOAT,
    tracking_number VARCHAR(100),
    version INT NOT NULL DEFAULT 0,
    snapshot_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- CQRS Read Model table (Module 9: CQRS)
CREATE TABLE order_read_model (
    order_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    status_label NVARCHAR(50) NOT NULL DEFAULT 'Pedido Creado',
    total_amount FLOAT NOT NULL DEFAULT 0,
    tracking_number VARCHAR(100),
    item_count INT NOT NULL DEFAULT 0,
    items_summary NVARCHAR(500),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    last_updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    event_count INT NOT NULL DEFAULT 1
);
GO

CREATE INDEX idx_order_read_customer ON order_read_model (customer_id);
GO

CREATE INDEX idx_order_read_status ON order_read_model (status);
GO

-- Saga State table (Module 9: Saga Pattern)
CREATE TABLE saga_state (
    saga_id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'STARTED',
    current_step VARCHAR(50) NOT NULL,
    completed_steps VARCHAR(500),
    failure_reason NVARCHAR(500),
    started_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    completed_at DATETIME2
);
GO

CREATE INDEX idx_saga_order ON saga_state (order_id);
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
