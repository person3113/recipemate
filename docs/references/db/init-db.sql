-- RecipeMate Database Initialization Script
-- This script sets up the initial database configuration for PostgreSQL

-- Set encoding and locale
SET client_encoding = 'UTF8';

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant necessary privileges to the database user
GRANT ALL PRIVILEGES ON DATABASE recipemate TO recipemate;

-- Note: Table schemas will be created automatically by JPA/Hibernate
-- when the Spring Boot application starts with ddl-auto: validate
-- Ensure your schema is already defined via migrations or manual SQL scripts
