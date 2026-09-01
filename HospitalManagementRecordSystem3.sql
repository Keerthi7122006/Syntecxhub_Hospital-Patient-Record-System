-- Hospital Patient Record System
-- MySQL Database

CREATE DATABASE IF NOT EXISTS hospital_patient_db;

USE hospital_patient_db;

-- Patients table
CREATE TABLE IF NOT EXISTS patients (
    patient_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10),
    phone VARCHAR(15),
    address VARCHAR(255),
    blood_group VARCHAR(5),
    admission_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Medical history table
CREATE TABLE IF NOT EXISTS medical_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    diagnosis VARCHAR(255),
    treatment VARCHAR(255),
    doctor_name VARCHAR(100),
    visit_date DATE,
    notes TEXT,
    FOREIGN KEY (patient_id)
        REFERENCES patients(patient_id)
        ON DELETE CASCADE
);

-- Users table for secure access
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Sample patients
INSERT INTO patients
(patient_name, age, gender, phone, address, blood_group, admission_date)
VALUES
('Rahul Kumar', 35, 'Male', '9876543210',
 'Visakhapatnam', 'O+', '2026-09-01'),

('Priya Sharma', 28, 'Female', '9876543211',
 'Hyderabad', 'A+', '2026-09-01');

-- Sample medical history
INSERT INTO medical_history
(patient_id, diagnosis, treatment, doctor_name, visit_date, notes)
VALUES
(1, 'Fever', 'Medication and rest',
 'Dr. Ravi Kumar', '2026-09-01',
 'Patient advised to take medicines regularly'),

(2, 'Migraine', 'Pain management',
 'Dr. Anitha Rao', '2026-09-01',
 'Follow-up required after one week');

-- Sample users
INSERT INTO users (username, password, role)
VALUES
('admin', 'admin123', 'ADMIN'),
('doctor', 'doctor123', 'DOCTOR');

-- View all patients
SELECT * FROM patients;

-- View patient details with medical history
SELECT
    p.patient_id,
    p.patient_name,
    p.age,
    p.gender,
    p.phone,
    p.address,
    p.blood_group,
    m.diagnosis,
    m.treatment,
    m.doctor_name,
    m.visit_date,
    m.notes
FROM patients p
LEFT JOIN medical_history m
ON p.patient_id = m.patient_id;

-- Update patient information
UPDATE patients
SET phone = '9999999999',
    address = 'Visakhapatnam'
WHERE patient_id = 1;

-- Update medical history
UPDATE medical_history
SET treatment = 'Updated medication',
    notes = 'Patient is improving'
WHERE history_id = 1;

-- Search patient by ID
SELECT * FROM patients
WHERE patient_id = 1;

-- Delete a patient
-- DELETE FROM patients WHERE patient_id = 1;
