# Database Migration Guide

## Migration: Add Optimistic Locking Support

### Overview
This migration adds `version` columns to `carts` and `cart_items` tables to support optimistic locking for handling concurrent updates.

### Prerequisites
- PostgreSQL database running
- Docker container `ute-phonehub-db` is up

### Steps to Apply Migration

#### Option 1: Using Docker Exec (Recommended)
```bash
# Navigate to backend directory
cd backend

# Apply migration
docker exec -i ute-phonehub-db psql -U postgres -d utephonehub < migrations/001_add_version_columns.sql
```

#### Option 2: Using psql Command Line
```bash
# Connect to database
docker exec -it ute-phonehub-db psql -U postgres -d utephonehub

# Run migration manually
\i /path/to/migrations/001_add_version_columns.sql

# Or copy-paste the SQL content
```

#### Option 3: If Database is Fresh
If you're starting fresh, just run:
```bash
docker-compose down -v
docker-compose up -d
```
The `init.sql` already includes the `version` columns.

### Verify Migration
```sql
-- Check if version column exists in carts table
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'carts' AND column_name = 'version';

-- Check if version column exists in cart_items table
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'cart_items' AND column_name = 'version';

-- Both should return:
-- column_name | data_type | is_nullable
-- version     | bigint    | NO
```

### Rollback (if needed)
```sql
ALTER TABLE carts DROP COLUMN IF EXISTS version;
ALTER TABLE cart_items DROP COLUMN IF EXISTS version;
```

### What This Migration Enables
- ✅ Concurrent update handling
- ✅ Optimistic locking with automatic retry
- ✅ Prevention of lost updates in multi-device scenarios
- ✅ 409 Conflict responses with detailed error info

### Testing
After applying migration, run integration tests:
```bash
mvn test -Dtest=CartServiceImplIntegrationTest
```

All tests should pass, especially the concurrent update test.
