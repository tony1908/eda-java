IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'pubsub')
    CREATE DATABASE pubsub;
GO

USE pubsub;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'channels')
CREATE TABLE channels (
    id VARCHAR(36) PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE()
);
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'subscriptions')
CREATE TABLE subscriptions (
    id VARCHAR(36) PRIMARY KEY DEFAULT NEWID(),
    channel_id VARCHAR(36) NOT NULL,
    webhook_url NVARCHAR(500) NOT NULL,
    secret VARCHAR(64) NOT NULL,
    description NVARCHAR(200),
    active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (channel_id) REFERENCES channels(id)
);
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'messages')
CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY DEFAULT NEWID(),
    channel_id VARCHAR(36) NOT NULL,
    payload NVARCHAR(MAX) NOT NULL,
    publisher_id VARCHAR(100),
    published_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (channel_id) REFERENCES channels(id)
);
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'delivery_log')
CREATE TABLE delivery_log (
    id VARCHAR(36) PRIMARY KEY DEFAULT NEWID(),
    message_id VARCHAR(36) NOT NULL,
    subscription_id VARCHAR(36) NOT NULL,
    channel_name NVARCHAR(100) NOT NULL,
    webhook_url NVARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,        -- DELIVERED, FAILED, RETRYING
    http_status INT,
    attempt INT DEFAULT 1,
    error_message NVARCHAR(1000),
    delivered_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id)
);
GO