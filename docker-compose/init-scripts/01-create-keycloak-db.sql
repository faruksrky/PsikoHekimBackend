-- Create databases
CREATE DATABASE keycloak;
CREATE DATABASE camunda;
CREATE DATABASE psikohekim;

-- Create single user for all databases
CREATE USER UserPsiko WITH ENCRYPTED PASSWORD 'password';

-- Grant privileges to all databases
GRANT ALL PRIVILEGES ON DATABASE keycloak TO UserPsiko;
GRANT ALL PRIVILEGES ON DATABASE camunda TO UserPsiko;
GRANT ALL PRIVILEGES ON DATABASE psikohekim TO UserPsiko;
