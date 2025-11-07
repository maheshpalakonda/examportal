



CREATE DATABASE IF NOT EXISTS newexam;
USE newexam;


CREATE TABLE admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    hall_ticket_number VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    mobile_number VARCHAR(20),
    college_name VARCHAR(255),
    branch VARCHAR(100),
    skills TEXT,
    cgpa DECIMAL(4, 2),
    date_of_birth DATE,
    gender VARCHAR(20),
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    exam_link_sent TIMESTAMP NULL
) ENGINE=InnoDB;




CREATE TABLE exams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exam_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE sections (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    duration_in_minutes INT NOT NULL,
    order_index INT NOT NULL,
    marks INT NOT NULL DEFAULT 0,
    has_min_pass_marks BOOLEAN NOT NULL DEFAULT FALSE,
    min_pass_marks INT,
    num_questions_to_select INT,
    exam_id INT,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
) ENGINE=InnoDB;




CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text TEXT NOT NULL,
    section_id INT,
    
    option1 VARCHAR(255), 
    option2 VARCHAR(255),
    option3 VARCHAR(255), 
    option4 VARCHAR(255),
    correct_answer VARCHAR(255),

    boilerplate_java TEXT,
    boilerplate_python TEXT,
    boilerplate_c TEXT,
    boilerplate_sql TEXT,

    test_cases TEXT,
    setup_sql TEXT,

    is_coding_question BIT(1) NOT NULL DEFAULT 0,

    FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE
);


CREATE TABLE results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_email VARCHAR(255) NOT NULL,
    exam_id INT,
    score INT NOT NULL DEFAULT 0,
    total_questions INT NOT NULL DEFAULT 0,
    exam_date DATETIME(6) NOT NULL,
    mcq_answers TEXT,
    coding_answers TEXT,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
);

















ALTER TABLE results MODIFY total_questions INT NOT NULL DEFAULT 0;
ALTER TABLE results MODIFY score INT NOT NULL DEFAULT 0;

ALTER TABLE questions MODIFY question_text TEXT NOT NULL DEFAULT '';
ALTER TABLE questions MODIFY correct_answer VARCHAR(255) DEFAULT '';


INSERT INTO admins (username, password) VALUES ('admin', '{noop}admin');
select * from questions;
select * from students;





-- Add a unique constraint to prevent duplicate section names within the same exam
ALTER TABLE sections ADD CONSTRAINT uk_exam_section_name UNIQUE(exam_id, name);


select *from questions;
desc students;



-- CREATE DATABASE campusdb;

-- USE campusdb;

-- CREATE TABLE students (
--   id INT AUTO_INCREMENT PRIMARY KEY,
--   name VARCHAR(100),
--   hall_ticket_number VARCHAR(50),
--   email VARCHAR(100),
--   mobile_number VARCHAR(15),
--     college_name VARCHAR(255),
--   branch VARCHAR(50),
--     skills TEXT,

--   cgpa DECIMAL(4,2),
--   date_of_birth DATE,
--   gender VARCHAR(10),
--     registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     exam_link_sent TIMESTAMP NULL
-- );



-- ALTER TABLE students 
-- ADD COLUMN hall_ticket VARCHAR(20) NOT NULL;




-- ALTER TABLE students ADD COLUMN exam_link_sent TIMESTAMP NULL;


select *from exams;
