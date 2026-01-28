# Smart Clinic Management System - Architecture Summary

## Section 1: Architecture Summary
This Smart Clinic Management System is built using **Spring Boot** and follows a combination of **MVC (Model-View-Controller)** and **RESTful API** architecture. The application uses **Thymeleaf templates** for rendering Admin and Doctor dashboards, while REST APIs handle CRUD operations for Patients, Appointments, and Prescriptions.  

The system interacts with **two databases**: **MySQL** stores structured data such as users (admins, doctors, patients) and appointments, using JPA entities for object-relational mapping, whereas **MongoDB** stores flexible document-based data, including prescriptions.  

All requests first pass through **controllers**, which delegate the business logic to a **service layer**. The service layer then interacts with the appropriate **repository layer** to perform database operations. This architecture ensures a clear separation of concerns, maintainable code, and a scalable structure for future enhancements.

---

## Section 2: Numbered Flow of Data and Control

1. A user (Admin, Doctor, or Patient) accesses the system via a **frontend page** (Thymeleaf dashboard or REST client).  
2. The **controller** receives the HTTP request and identifies the required action (view, create, update, delete).  
3. The controller forwards the request to the **service layer**, which contains the core business logic.  
4. The service layer calls the appropriate **repository** methods to interact with either **MySQL** (structured data) or **MongoDB** (document data).  
5. The database executes the query or operation, returning the requested data or confirmation of changes.  
6. The service layer processes the data and sends it back to the **controller**.  
7. Finally, the controller returns a **response**: either a rendered Thymeleaf page for dashboards or a JSON payload for REST APIs.

