UPDATE users SET password_hash = '$2b$12$c3VhEcfRU3gKmLaMkPu0BeDVg8kySwogTR0y..GjQQd4FtCElXImm'
WHERE email = 'admin@school.com';

UPDATE users SET password_hash = '$2b$12$c3VhEcfRU3gKmLaMkPu0BeDVg8kySwogTR0y..GjQQd4FtCElXImm'
WHERE email IN ('teacher1@school.com', 'teacher2@school.com');
