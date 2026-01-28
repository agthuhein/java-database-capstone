# Smart Clinic Management System - User Stories

## 1. Admin User Stories

### **User Story 1**
**Title:**  
_As an admin, I want to log into the portal with my username and password, so that I can manage the platform securely._

**Acceptance Criteria:**  
1. Admin can access login page.  
2. System validates username and password.  
3. Admin is redirected to the admin dashboard upon successful login.  

**Priority:** High  
**Story Points:** 3  
**Notes:** Use JWT authentication for secure login.

---

### **User Story 2**
**Title:**  
_As an admin, I want to log out of the portal, so that I can protect system access._

**Acceptance Criteria:**  
1. Admin sees a logout button on the dashboard.  
2. Clicking logout ends the session and redirects to login page.  
3. Accessing protected pages after logout is blocked.  

**Priority:** High  
**Story Points:** 2  
**Notes:** Ensure JWT tokens are invalidated after logout.

---

### **User Story 3**
**Title:**  
_As an admin, I want to add doctors to the portal, so that they can manage their appointments._

**Acceptance Criteria:**  
1. Admin can fill out doctor registration form.  
2. System validates required fields (name, specialization, contact).  
3. Doctor profile is saved in MySQL and accessible in the doctor list.  

**Priority:** High  
**Story Points:** 3  
**Notes:** Send email notification to the doctor after adding.

---

### **User Story 4**
**Title:**  
_As an admin, I want to delete a doctor's profile from the portal, so that outdated or inactive accounts can be removed._

**Acceptance Criteria:**  
1. Admin can view a list of all doctors.  
2. Admin can click a delete button next to a doctor.  
3. System confirms deletion before removing the record.  

**Priority:** Medium  
**Story Points:** 2  
**Notes:** Ensure deletion also removes related appointments.

---

### **User Story 5**
**Title:**  
_As an admin, I want to run a stored procedure in MySQL to get the number of appointments per month, so that I can track usage statistics._

**Acceptance Criteria:**  
1. Admin can trigger the stored procedure via the portal or CLI.  
2. The procedure returns the number of appointments grouped by month.  
3. Results are displayed in a readable format or exported as CSV.  

**Priority:** Medium  
**Story Points:** 3  
**Notes:** Useful for reporting and tracking system usage trends.

---

## 2. Patient User Stories

### **User Story 1**
**Title:**  
_As a patient, I want to view a list of doctors without logging in, so that I can explore options before registering._

**Acceptance Criteria:**  
1. Patient can access a public doctor list page.  
2. Doctors are displayed with basic details (name, specialization, rating).  
3. Clicking on a doctor shows a detailed profile.  

**Priority:** Medium  
**Story Points:** 2  
**Notes:** No login required for exploration.

---

### **User Story 2**
**Title:**  
_As a patient, I want to sign up using my email and password, so that I can book appointments._

**Acceptance Criteria:**  
1. Patient can access signup page.  
2. Email and password are validated.  
3. Account is saved in MySQL and patient can log in.  

**Priority:** High  
**Story Points:** 3  
**Notes:** Send confirmation email after signup.

---

### **User Story 3**
**Title:**  
_As a patient, I want to log into the portal, so that I can manage my bookings._

**Acceptance Criteria:**  
1. Patient login page accepts email and password.  
2. Successful login redirects to patient dashboard.  
3. JWT authentication is used to secure the session.  

**Priority:** High  
**Story Points:** 3  
**Notes:** Failed login attempts are limited to prevent brute force attacks.

---

### **User Story 4**
**Title:**  
_As a patient, I want to log out of the portal, so that my account remains secure._

**Acceptance Criteria:**  
1. Logout button is available on the dashboard.  
2. Clicking logout invalidates the session.  
3. Accessing protected pages after logout is blocked.  

**Priority:** High  
**Story Points:** 2  
**Notes:** JWT token should be cleared from browser storage.

---

### **User Story 5**
**Title:**  
_As a patient, I want to book an hour-long appointment with a doctor, so that I can consult about my health._

**Acceptance Criteria:**  
1. Patient selects doctor and available time slot.  
2. System validates slot availability.  
3. Appointment is saved in MySQL and confirmation is sent to patient.  

**Priority:** High  
**Story Points:** 3  
**Notes:** Prevent double-booking of the same slot.

---

### **User Story 6**
**Title:**  
_As a patient, I want to view my upcoming appointments, so that I can prepare accordingly._

**Acceptance Criteria:**  
1. Patient dashboard shows a list of upcoming appointments.  
2. Each appointment shows doctor, time, and location.  
3. Option to cancel or reschedule is provided.  

**Priority:** Medium  
**Story Points:** 2  
**Notes:** Include notifications/reminders for appointments.

---

## 3. Doctor User Stories

### **User Story 1**
**Title:**  
_As a doctor, I want to log into the portal, so that I can manage my appointments._

**Acceptance Criteria:**  
1. Doctor login page accepts email and password.  
2. Successful login redirects to doctor dashboard.  
3. Session is secured using JWT.  

**Priority:** High  
**Story Points:** 3  
**Notes:** Failed logins should be limited to prevent brute force attacks.

---

### **User Story 2**
**Title:**  
_As a doctor, I want to log out of the portal, so that my data remains protected._

**Acceptance Criteria:**  
1. Logout button is accessible from dashboard.  
2. Session is invalidated after logout.  
3. Access to protected pages after logout is blocked.  

**Priority:** High  
**Story Points:** 2  
**Notes:** JWT token cleared from local storage.

---

### **User Story 3**
**Title:**  
_As a doctor, I want to view my appointment calendar, so that I can stay organized._

**Acceptance Criteria:**  
1. Doctor dashboard displays appointments in calendar view.  
2. Appointments include patient name, time, and purpose.  
3. Can filter by day, week, or month.  

**Priority:** High  
**Story Points:** 3  
**Notes:** Highlight upcoming or urgent appointments.

---

### **User Story 4**
**Title:**  
_As a doctor, I want to mark my unavailability, so that patients only book available slots._

**Acceptance Criteria:**  
1. Doctor can select time slots as unavailable.  
2. System prevents patients from booking these slots.  
3. Unavailability is saved in MySQL.  

**Priority:** Medium  
**Story Points:** 2  
**Notes:** Include recurring unavailability (e.g., weekly off).

---

### **User Story 5**
**Title:**  
_As a doctor, I want to update my profile with specialization and contact information, so that patients have accurate information._

**Acceptance Criteria:**  
1. Doctor can edit profile details on dashboard.  
2. Changes are validated and saved in MySQL.  
3. Updated information is visible to patients.  

**Priority:** Medium  
**Story Points:** 2  
**Notes:** Optional profile picture upload.

---

### **User Story 6**
**Title:**  
_As a doctor, I want to view patient details for upcoming appointments, so that I can be prepared for consultations._

**Acceptance Criteria:**  
1. Doctor can click an appointment to see patient history.  
2. Patient details include past appointments, prescriptions, and notes.  
3. Data is read-only for the doctor unless prescribed.  

**Priority:** High  
**Story Points:** 3  
**Notes:** Sensitive data must comply with privacy regulations (HIPAA/GDPR).

