
# 🏥📱 Medical Clinic App

![Gears turning](https://gifdb.com/images/thumbnail/medica-puneakb9gv2y4s21.gif)

This repository contains an **Android application** and a **backend server** for managing a medical clinic.  
The goal is to integrate **frontend** and **backend** into a complete project. 🚀

---

## 📂 Repository Structure

```

/
├── android/     # Android App (frontend)
├── backend/     # Spring Boot Server (backend)

```

- **android/** → Android application developed in **Java/Kotlin**, responsible for the user interface.  
- **backend/** → REST API built with **Spring Boot**, responsible for business logic and data persistence.  

---

## 📚 Features

- 👨‍⚕️ Doctor and patient management  
- 📅 Appointment scheduling and control  
- 📋 Patient history  
- 📋 Appointment history  
- 📋 Medical records  
- 🔐 Authentication via **Basic Auth** with different access roles  

---

## 🔐 Authentication

This project uses **Basic Auth**.  
Each request to the API must include the following header:

```

Authorization: Basic \<base64(username\:password)>

````

📌 Example using `curl`:

```bash
curl -u admin:123456 http://localhost:8080/medicos
````

### Roles and Permissions

* **Administrador** → Full CRUD; can cancel appointments.
* **Operador** → Create and list doctors, patients and appointments.
* **Médico** → View their own upcoming appointments and patient history.

---

## ⚙️ Technologies Used

### 🔹 Android (Frontend)

* ☕ Java / Kotlin
* 🎨 XML layouts
* 🔗 REST API consumption

### 🔹 Backend (Server)

* ☕ **Java 21**
* 🌱 **Spring Boot 3**
* 🛢️ **PostgreSQL / MySQL / H2**
* 🔐 **Spring Security (Basic Auth)**
* 📖 **Swagger/OpenAPI**
* 📦 **Docker**

---

## 🚀 Goal

Provide an integrated solution for medical clinics, allowing doctors and patients to use the app in a practical way, with centralized management through the backend.

---

![Healthcare gif](https://i.gifer.com/PwIy.gif)

**Frontend and Backend together in one project for digital healthcare!** ✨

```
