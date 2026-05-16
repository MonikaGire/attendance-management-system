INSERT INTO users (id, email, password_hash, first_name, last_name, role_id)
SELECT UUID(), 'teacher1@school.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCQqb9QxhFJpUuWq5KiZnGS',
    'Priya', 'Sharma', id FROM roles WHERE name='TEACHER' LIMIT 1;

INSERT INTO users (id, email, password_hash, first_name, last_name, role_id)
SELECT UUID(), 'teacher2@school.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCQqb9QxhFJpUuWq5KiZnGS',
    'Rahul', 'Verma', id FROM roles WHERE name='TEACHER' LIMIT 1;

INSERT INTO school_classes (id, name, teacher_id, academic_year, room)
SELECT UUID(), 'Class 10-A',
    (SELECT id FROM users WHERE email='teacher1@school.com'), '2024-25', 'Room 101';

INSERT INTO school_classes (id, name, teacher_id, academic_year, room)
SELECT UUID(), 'Class 10-B',
    (SELECT id FROM users WHERE email='teacher2@school.com'), '2024-25', 'Room 102';

INSERT INTO school_classes (id, name, teacher_id, academic_year, room)
SELECT UUID(), 'Class 11-A',
    (SELECT id FROM users WHERE email='teacher1@school.com'), '2024-25', 'Room 201';

INSERT INTO devices (id, name, location, api_key_hash, status)
VALUES (UUID(), 'Main Gate Device', 'School Main Entrance',
    '$2a$12$deviceapikeyhashplaceholder000000000000000000000', 'ACTIVE');

INSERT INTO students (id, first_name, last_name, phone, parent_phone,
    grade, biometric_id, enrollment_date, whatsapp_consent, status) VALUES
(UUID(),'Aarav','Patel','9900001111','9900002222','10','B-001',CURDATE(),1,'ACTIVE'),
(UUID(),'Diya','Shah','9900003333','9900004444','10','B-002',CURDATE(),1,'ACTIVE'),
(UUID(),'Rohan','Mehta','9900005555','9900006666','10','B-003',CURDATE(),1,'ACTIVE'),
(UUID(),'Ananya','Singh','9900007777','9900008888','10','B-004',CURDATE(),1,'ACTIVE'),
(UUID(),'Kiran','Joshi','9900009999','9900001010','10','B-005',CURDATE(),1,'ACTIVE'),
(UUID(),'Meera','Nair','9900011111','9900012222','11','B-006',CURDATE(),1,'ACTIVE'),
(UUID(),'Arjun','Reddy','9900013333','9900014444','11','B-007',CURDATE(),1,'ACTIVE'),
(UUID(),'Pooja','Kumar','9900015555','9900016666','11','B-008',CURDATE(),1,'ACTIVE'),
(UUID(),'Siddharth','Gupta','9900017777','9900018888','10','B-009',CURDATE(),1,'ACTIVE'),
(UUID(),'Tanvi','Iyer','9900019999','9900020000','11','B-010',CURDATE(),1,'ACTIVE');

INSERT INTO enrollments (id, student_id, class_id)
SELECT UUID(), s.id,
    (SELECT id FROM school_classes WHERE name='Class 10-A' LIMIT 1)
FROM students s WHERE s.biometric_id IN ('B-001','B-002','B-003','B-004','B-005');

INSERT INTO enrollments (id, student_id, class_id)
SELECT UUID(), s.id,
    (SELECT id FROM school_classes WHERE name='Class 10-B' LIMIT 1)
FROM students s WHERE s.biometric_id IN ('B-006','B-007','B-008');

INSERT INTO enrollments (id, student_id, class_id)
SELECT UUID(), s.id,
    (SELECT id FROM school_classes WHERE name='Class 11-A' LIMIT 1)
FROM students s WHERE s.biometric_id IN ('B-009','B-010');

INSERT INTO sessions (id, class_id, session_date, start_time, end_time,
    type, grace_period_minutes, status)
SELECT UUID(), id, CURDATE(), '08:00:00', '09:00:00', 'REGULAR', 15, 'SCHEDULED'
FROM school_classes;

INSERT INTO sessions (id, class_id, session_date, start_time, end_time,
    type, grace_period_minutes, status)
SELECT UUID(), id, DATE_SUB(CURDATE(), INTERVAL 1 DAY),
    '08:00:00', '09:00:00', 'REGULAR', 15, 'COMPLETED'
FROM school_classes;
