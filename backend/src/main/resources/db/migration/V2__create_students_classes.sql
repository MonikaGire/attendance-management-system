CREATE TABLE school_classes (
    id CHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    teacher_id CHAR(36),
    academic_year VARCHAR(20) NOT NULL,
    schedule_json TEXT,
    room VARCHAR(50),
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3),
    PRIMARY KEY (id),
    KEY idx_classes_teacher (teacher_id),
    CONSTRAINT fk_classes_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE students (
    id CHAR(36) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    parent_phone VARCHAR(20),
    grade VARCHAR(20),
    biometric_id VARCHAR(50) NOT NULL UNIQUE,
    enrollment_date DATE NOT NULL,
    whatsapp_consent TINYINT(1) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_biometric_id (biometric_id),
    KEY idx_students_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE enrollments (
    id CHAR(36) NOT NULL,
    student_id CHAR(36) NOT NULL,
    class_id CHAR(36) NOT NULL,
    enrolled_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_enrollment (student_id, class_id),
    KEY idx_enrollment_student (student_id),
    KEY idx_enrollment_class (class_id),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE RESTRICT,
    CONSTRAINT fk_enrollment_class FOREIGN KEY (class_id) REFERENCES school_classes(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
