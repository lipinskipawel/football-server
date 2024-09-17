CREATE OR REPLACE VIEW users_view AS
SELECT  id
        username,
        created,
        terminated
FROM users;
