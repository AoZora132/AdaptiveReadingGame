-- This SQL is for the player's account information.
 
-- Public account info. No password data here.
CREATE TABLE IF NOT EXISTS accounts (
    id            CHAR(36)     PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,   -- store the NORMALIZED username
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
CREATE TABLE IF NOT EXISTS credentials (
    account_id        CHAR(36)     PRIMARY KEY,
    encoded_verifier  VARCHAR(255) NOT NULL,       -- encoded password hash, NEVER plaintext
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);
 
CREATE TABLE IF NOT EXISTS students (
    id            CHAR(36)     PRIMARY KEY,
    display_name  VARCHAR(100) NOT NULL,
    FOREIGN KEY (id) REFERENCES accounts(id) ON DELETE CASCADE
);
