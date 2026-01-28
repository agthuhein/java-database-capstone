# Smart Clinic Management System - Database Design

## MySQL Database Design

The MySQL database stores structured, validated, and interrelated data. Core entities include patients, doctors, appointments, and admin. Optional tables like clinic_locations and payments can also be added to handle real-world operations. Foreign keys maintain relationships and data integrity.

---

### Table: patients
- **id:** INT, Primary Key, Auto Increment  
- **first_name:** VARCHAR(50), Not Null  
- **last_name:** VARCHAR(50), Not Null  
- **email:** VARCHAR(100), Not Null, Unique  
- **phone:** VARCHAR(20), Not Null  
- **date_of_birth:** DATE, Not Null  
- **created_at:** TIMESTAMP, Default CURRENT_TIMESTAMP  

*Comments:* Patient’s past appointments should be retained even if the patient is inactive. Email and phone can be validated via backend code.

---

### Table: doctors
- **id:** INT, Primary Key, Auto Increment  
- **first_name:** VARCHAR(50), Not Null  
- **last_name:** VARCHAR(50), Not Null  
- **email:** VARCHAR(100), Not Null, Unique  
- **phone:** VARCHAR(20), Not Null  
- **specialization:** VARCHAR(100), Not Null  
- **available_start_time:** TIME, Not Null  
- **available_end_time:** TIME, Not Null  
- **created_at:** TIMESTAMP, Default CURRENT_TIMESTAMP  

*Comments:* Each doctor has defined working hours to prevent overlapping appointments. Additional availability slots could be handled in a separate table if needed.

---

### Table: appointments
- **id:** INT, Primary Key, Auto Increment  
- **doctor_id:** INT, Foreign Key → doctors(id), Not Null  
- **patient_id:** INT, Foreign Key → patients(id), Not Null  
- **appointment_time:** DATETIME, Not Null  
- **duration_minutes:** INT, Not Null, Default 60  
- **status:** ENUM('Scheduled','Completed','Cancelled'), Default 'Scheduled'  
- **created_at:** TIMESTAMP, Default CURRENT_TIMESTAMP  

*Comments:* Deleting a patient or doctor could optionally cascade to cancel their appointments, or mark them as inactive for historical records.

---

### Table: admin
- **id:** INT, Primary Key, Auto Increment  
- **username:** VARCHAR(50), Not Null, Unique  
- **email:** VARCHAR(100), Not Null, Unique  
- **password_hash:** VARCHAR(255), Not Null  
- **created_at:** TIMESTAMP, Default CURRENT_TIMESTAMP  

*Comments:* Admins manage the portal and can add or remove doctors and monitor statistics. Passwords should be hashed and salted.

---

### Table: payments
- **id:** INT, Primary Key, Auto Increment  
- **appointment_id:** INT, Foreign Key → appointments(id), Not Null  
- **amount:** DECIMAL(10,2), Not Null  
- **payment_method:** ENUM('Cash','Card','Insurance'), Not Null  
- **status:** ENUM('Pending','Completed','Failed'), Default 'Pending'  
- **payment_date:** TIMESTAMP, Default CURRENT_TIMESTAMP  

*Comments:* Keeping payments separate allows tracking billing and insurance processes efficiently.

---

### Table: clinic_locations
- **id:** INT, Primary Key, Auto Increment  
- **name:** VARCHAR(100), Not Null  
- **address:** VARCHAR(255), Not Null  
- **city:** VARCHAR(50), Not Null  
- **state:** VARCHAR(50), Not Null  
- **postal_code:** VARCHAR(10), Not Null  

*Comments:* Optional table for clinics with multiple locations. Appointments can reference location if needed.

---

## MongoDB Collection Design

MongoDB stores flexible, schema-less data that doesn’t fit well into relational tables. For example, prescriptions, doctor notes, logs, and feedback.

### Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc123456')",
  "patientId": 101,
  "doctorId": 12,
  "appointmentId": 55,
  "medications": [
    {
      "name": "Paracetamol",
      "dosage": "500mg",
      "frequency": "Every 6 hours",
      "duration_days": 5
    },
    {
      "name": "Amoxicillin",
      "dosage": "250mg",
      "frequency": "Twice daily",
      "duration_days": 7
    }
  ],
  "notes": "Patient has mild allergy to penicillin. Monitor reaction.",
  "tags": ["fever","infection"],
  "refill_count": 1,
  "created_at": "2026-01-28T10:00:00Z",
  "pharmacy": {
    "name": "Walgreens SF",
    "address": "123 Market Street, San Francisco, CA"
  }
}
