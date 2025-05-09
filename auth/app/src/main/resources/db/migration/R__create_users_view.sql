CREATE OR REPLACE VIEW users_view AS
SELECT  id
        username,
        state,
        created_date,
        updated_date
FROM users;
