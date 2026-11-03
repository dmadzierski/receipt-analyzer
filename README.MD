# Receipt Analyzer - Receipt Digitization and Expense Tracking 

A modular web application for receipt digitization and expense tracking with advanced OCR processing. Created to collect training data for an ML model.

---

## Project Description

**Receipt Analyzer** is a comprehensive solution for:
- Receipt and invoice digitization with automatic text recognition (OCR)
- Organizing expenses in wallets
- Product categorization
- Secure access management through Keycloak
- Responsive web interface

**Key Features:**
- Automatic data extraction from PDF and JSON receipts
- Support for multiple receipt formats
- Receipt files kept as file groups (PDF, JPEG/PNG/WEBP images, JSON exports)
- Image editor: rotate and crop receipt photos, the result is saved as a new file group and the original stays untouched
- Versioning system created for training an ML model to read receipt data
- Editing receipt items with product autocomplete on the item name (free text still allowed) and CSV paste from the clipboard
- Product dictionary management, with aliases learned from user corrections
- Per-user data isolation: wallets, receipts and files are visible only to their owner
- Authentication and authorization via Keycloak
- Angular Material interface


---

## Architecture

### Architectural Pattern

The project uses Hexagonal Architecture and CQRS:

```mermaid
---
config:
  layout: dagre
---
flowchart TB
 subgraph Frontend["Frontend"]
        A["Angular"]
  end
 subgraph Gateway["API Gateway"]
        B["Nginx"]
  end
 subgraph Adapters["Adapters Layer"]
        C["REST<br>Controllers"]
  end
 subgraph Application["Application Layer"]
        D["Services &amp;<br>Facades"]
  end
 subgraph Domain["Domain Layer"]
        E["Domain<br>Models"]
  end
 subgraph Service["Receipt Analyzer Service"]
        Adapters
        Application
        Domain
  end
 subgraph OAuth["OAuth"]
        K["Keycloak"]
  end
 subgraph Database["Database"]
        L["PostgreSQL"]
  end
    Gateway --> Service
    Service --> OAuth & Database
    Frontend --> Gateway
    Adapters --> Application
    Application --> Domain
    Adapters --> Domain
```
---

## Tech Stack

### Backend
```
Language:           Java 21
Framework:          Spring Boot 3.5
Build Tool:         Gradle (multi-module: domain, app, adapters, monolith)
Database:           PostgreSQL 17
Migrations:         Flyway
Authentication:     Keycloak 26.3 (OAuth2 resource server, JWT)
OCR:                Tesseract (tess4j)
File type detection: Apache Tika
Persistence:        Hibernate JPA
Tests:              JUnit 5, Testcontainers (PostgreSQL)
```

### Frontend
```
Language:           TypeScript 5.8
Framework:          Angular 20.1
UI Library:         Angular Material 20.2, Bootstrap 5.3
```

### Infrastructure
```
Container:          Docker & Docker Compose
Web Server:         Nginx 1.29.3
Orchestration:      Docker Compose
CI/CD:              Jenkins (integration tests, image build, push to a private registry)
```
---

## What Has Been Done?

### Core Functionality
- [x] Authentication and authorization via Keycloak
- [x] PDF file upload and processing
- [x] OCR receipt processing
- [x] Support for multiple receipt formats
- [x] Versioning system to track data corrections (for ML training)
- [x] Product dictionary with categorization
- [x] Product aliases system
- [x] Image file groups and image editor (rotate, crop, save as new)
- [x] Manual linking of receipt items to products (autocomplete)
- [x] Stores and store brands
- [x] Per-user ownership of wallets, receipts and files (IDOR protection)
- [x] Database schema versioned with Flyway
- [x] Angular interface

### Architecture and Design
- [x] Hexagonal Architecture
- [x] Command Query Responsibility Segregation (CQRS)
- [x] Strategy Pattern for processing different formats

### Infrastructure
- [x] Docker containerization
- [x] Docker Compose setup (prod + local dev)
- [x] Nginx reverse proxy
- [x] Environment configuration
- [x] Jenkins pipeline: integration tests, Docker image build and push

### Tests
- [x] Backend integration tests (Testcontainers + PostgreSQL)

---

## Future Plans

- [ ] **Improved Data Reading**
    - Implementation of ML model for receipt recognition
    - Optimization for different PDF qualities
    - Asynchronous processing (RabbitMQ)

- [ ] **Support for New Store Formats**
    - Expansion to other retail chains
    - Adaptation of ML model to different formats

- [ ] **Advanced Analytics**
    - Dashboard with expense statistics
    - Expense category charts
    - Product trends over time

- [ ] **Automated Tests**
    - Backend unit tests

---

## Contact

**Daniel Madzierski**

- Email: daniel.madzierski.pl@gmail.com
- Location: Poznań, Poland