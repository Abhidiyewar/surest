-- seed roles
INSERT INTO role (id, name)
SELECT uuid_generate_v4(), 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='ROLE_ADMIN');

INSERT INTO role (id, name)
SELECT uuid_generate_v4(), 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='ROLE_USER');

