-- seed roles
INSERT INTO role (id, name)
SELECT uuid_generate_v4(), 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='ROLE_ADMIN');

INSERT INTO role (id, name)
SELECT uuid_generate_v4(), 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='ROLE_USER');

-- get role ids
-- Replace the following queries with actual UUIDs if you prefer fixed IDs.

-- seed users (passwords use bcrypt)
-- Passwords:
-- admin: adminpass
-- user: userpass
-- Replace the hashed_passwords below after generating bcrypt hashes.
INSERT INTO app_user (username, password_hash, role_id)
SELECT 'admin', '$2a$10$u1VbWzK1f3aYpW4Zqz8XEOg0b0cw7oH6h0i0nZQ6Yk9QpJ5ZxX1bK',
       (SELECT id FROM role WHERE name='ROLE_ADMIN' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE username='admin');

INSERT INTO app_user (username, password_hash, role_id)
SELECT 'user', '$2a$10$wzG9qFh6a1zPq3cJ7X8YgO4m9b2n5vEo4tYcR1s2kL6pZ3cVb7Nq',
       (SELECT id FROM role WHERE name='ROLE_USER' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE username='user');

-- sample members
INSERT INTO member (first_name, last_name, date_of_birth, email)
SELECT 'Alice', 'Smith', '1990-01-15', 'alice@example.com'
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email='alice@example.com');

INSERT INTO member (first_name, last_name, date_of_birth, email)
SELECT 'Bob', 'Jones', '1985-07-22', 'bob@example.com'
WHERE NOT EXISTS (SELECT 1 FROM member WHERE email='bob@example.com');
