#!/bin/bash
# Wait for SQL Server to be ready, then run init.sql

echo "Waiting for SQL Server to start..."
for i in {1..30}; do
    /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "EventFlow123!" -Q "SELECT 1" -b -C > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "SQL Server is ready. Running init script..."
        /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "EventFlow123!" -i /docker-entrypoint-initdb.d/init.sql -C
        echo "Init script completed."
        exit 0
    fi
    echo "Waiting... ($i/30)"
    sleep 2
done

echo "ERROR: SQL Server did not become ready in time."
exit 1
