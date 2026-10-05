# AgroLink Backend

A RESTful backend API for **AgroLink**, built with **Spring Boot, MySQL, Spring Security, JPA, and Firebase**. The backend provides authentication, user management, product-related APIs, order/payment support, and integration services for the AgroLink Android application.

## Overview

AgroLink is an agriculture-focused application that connects the Android mobile application with a Spring Boot backend.

The backend is responsible for:

* User registration and authentication
* User profile management
* Product management and product APIs
* Order-related operations
* Payment integration
* Firebase-based services and notifications
* Secure communication between the mobile application and backend
* Persistent data storage using MySQL

## Technologies

* **Java**
* **Spring Boot**
* **Spring Security**
* **Spring Data JPA**
* **MySQL**
* **Maven**
* **Firebase**
* **REST API**
* **PayHere** for payment-related functionality

## Project Structure

```text
agrolink-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── .gitignore
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Android App Integration

The backend is designed to work with the AgroLink Android application.

When testing the application on a physical Android device, the device and the computer running the backend should normally be connected to the same Wi-Fi network.

Do not use `localhost` as the backend address from a physical Android device. Use the local IP address of the computer running the backend instead.

Example:

```text
http://<YOUR_COMPUTER_IP>:8080/api
```

Replace `<YOUR_COMPUTER_IP>` with the local IP address of the computer running the backend.

For development environments using plain HTTP, Android cleartext traffic may need to be enabled in the mobile application's configuration.

## Database Configuration

The backend uses **MySQL** for persistent data storage.

Database credentials should be supplied through environment variables rather than committed to source control.

Example:

```text
DB_PASSWORD=your_database_password
```

Never commit real database passwords, API keys, tokens, or other credentials to the repository.

## PayHere Configuration

Payment-related configuration is provided through environment variables.

Example:

```text
PAYHERE_SANDBOX=true
PAYHERE_MERCHANT_ID=your_sandbox_merchant_id
PAYHERE_MERCHANT_SECRET=your_merchant_secret
```

The Merchant Secret must not be stored directly in source code or committed to GitHub.

## Firebase

Firebase is used for backend-integrated services such as application messaging and notification-related functionality.

Firebase credentials and service-account files containing private keys should not be committed to the repository.

## Running the Backend

### Prerequisites

Make sure the following are installed:

* Java JDK
* MySQL
* Maven (or use the included Maven Wrapper)

### 1. Clone the repository

```bash
git clone https://github.com/Gayani-Thasmila/agrolink-backend.git
cd agrolink-backend
```

### 2. Configure the database

Create the required MySQL database and configure the required environment variables.

Example:

```text
DB_PASSWORD=your_database_password
```

### 3. Configure required environment variables

Set the required application configuration values before starting the backend.

Do not place real credentials directly in `application.properties`.

### 4. Start the application

Using Maven Wrapper:

**Windows**

```powershell
.\mvnw.cmd spring-boot:run
```

or using Maven:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

when running locally on the development computer.

## API

The backend exposes REST endpoints for the AgroLink Android application.

Example development endpoint:

```text
GET /api/products
```

The complete API implementation can be found under:

```text
src/main/java
```

## Security

The project uses Spring Security and environment-based configuration for sensitive values.

The repository intentionally excludes:

* Database passwords
* Payment merchant secrets
* Private keys
* Runtime logs
* Process ID files
* IDE-specific files
* Build output

## Development Notes

This project was developed as part of a Software Engineering project and demonstrates backend development using the Spring ecosystem, database integration, REST APIs, authentication/security, Firebase services, and mobile application integration.

## Related Project

**AgroLink Mobile**
Android mobile application built with Java and Firebase.

The mobile application communicates with this backend through REST APIs.

## Author

**Gayani Thasmila**

Software Engineering Undergraduate
