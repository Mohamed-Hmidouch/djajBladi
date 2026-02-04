# 🐔 Djaj Bladi - Poultry Farm Management System

> Backend API for managing poultry farms in Morocco

---

## 📋 Project Overview

**Djaj Bladi** is a comprehensive poultry farm management system that helps farm owners track:
- 🏗️ Buildings and infrastructure
- 🐔 Chicken batches (lots)
- 📦 Stock (feed, vaccines, vitamins)
- 🍽️ Daily feeding records
- 💀 Mortality tracking
- 🩺 Veterinary health records
- 👥 Staff management (workers, vets)

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Language |
| Spring Boot | 4.0.1 | Framework |
| PostgreSQL | 16 | Database |
| Redis | 7 | Caching |
| Flyway | - | DB Migrations |
| JWT | jjwt 0.12.5 | Authentication |
| Docker | - | Containerization |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.8+

### 1. Clone the repository
```bash
git clone https://github.com/Mohamed-Hmidouch/djajBladi.git
cd djajBladi
```

### 2. Start Docker services
```bash
docker compose up -d
```

This starts:
- **PostgreSQL** on port `5432`
- **Redis** on port `6379`
- **pgAdmin** on port `5050`

### 3. Run the application
```bash
./mvnw spring-boot:run
```

The API will be available at: `http://localhost:8081`

### 4. Test the API
```bash
# Register a user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","password":"Test@123"}'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@123"}'
```

---

## 👥 User Roles

| Role | Description | Access |
|------|-------------|--------|
| **Admin** | Farm owner/manager | Full access |
| **Ouvrier** | Farm worker | Feeding, mortality records |
| **Veterinaire** | Veterinarian | Health records |
| **Client** | Customer | View profile only |

---

## 📁 Project Structure

```
src/main/java/org/example/djajbladibackend/
├── controller/
│   ├── admin/          # Admin endpoints
│   ├── auth/           # Authentication
│   ├── ouvrier/        # Worker endpoints
│   ├── vet/            # Vet endpoints
│   └── profile/        # User profile
├── services/           # Business logic
├── repository/         # Data access
├── models/             # JPA entities
├── dto/                # Data transfer objects
├── security/           # JWT & Spring Security
└── exception/          # Error handling
```

---

## 🔗 API Documentation

See `docs/API_DOCUMENTATION.md` for detailed endpoint documentation.

---

## 📝 License

This project is private and proprietary.
