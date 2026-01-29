# 🐔 Djaj Bladi API Documentation

> **Base URL:** `http://localhost:8081/api`  
> **Version:** 1.0.0  
> **Content-Type:** `application/json`

---

## 📋 Table of Contents

- [Authentication](#-authentication)
  - [Register](#1-register)
  - [Login](#2-login)
- [Admin API](#-admin-api) (requires `Authorization: Bearer <JWT>` and Admin role)
  - [Create User (Ouvrier / Vétérinaire)](#1-create-user-ouvrier--vétérinaire)
  - [Buildings](#2-buildings)
  - [Batches](#3-batches)
  - [Stock](#4-stock)
- [Error Handling](#-error-handling)
- [Models](#-models)
- [TypeScript Types](#-typescript-types)
- [Code Examples](#-code-examples)

---

## 🔐 Authentication

### 1. Register

Creates a new user account.

**Endpoint:** `POST /api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "Mohamed",
  "lastName": "Hmidouch",
  "email": "mohamed@example.com",
  "password": "SecurePass@123",
  "phoneNumber": "+212600000001",
  "role": "Client"
}
```

**Request Fields:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `firstName` | string | ✅ Yes | max 100 chars | User's first name |
| `lastName` | string | ✅ Yes | max 100 chars | User's last name |
| `email` | string | ✅ Yes | valid email, max 255 chars | Unique email address |
| `password` | string | ✅ Yes | min 8 chars | User password |
| `phoneNumber` | string | ❌ No | - | Phone number (E.164 format recommended) |
| `role` | string | ❌ No | enum | User role (defaults to `Client`) |

**Only `Client` can self-register** (default when `role` is omitted). `Admin`, `Ouvrier` and `Veterinaire` cannot register via this endpoint; they are created via database seed/migration or by an admin (see Admin API).

**Success Response:** `201 Created`
```json
{
  "id": 1,
  "fullName": "Mohamed Hmidouch",
  "email": "mohamed@example.com",
  "phoneNumber": "+212600000001",
  "role": "Client",
  "isActive": true,
  "address": null,
  "city": null,
  "postalCode": null,
  "createdAt": "2026-01-28T21:30:00Z",
  "updatedAt": "2026-01-28T21:30:00Z",
  "lastLoginAt": null
}
```

**Error Responses:**

| Status | Description | Response |
|--------|-------------|----------|
| `400 Bad Request` | Email already exists | Empty body |
| `400 Bad Request` | Registration with role `Admin`, `Ouvrier` or `Veterinaire` not allowed (only `Client` can self-register) | ProblemDetail |
| `400 Bad Request` | Validation error | `{ "errors": { "field": "message" } }` |

**Validation Error Example:**
```json
{
  "errors": {
    "email": "Email must be valid",
    "password": "Password must be at least 8 characters",
    "firstName": "First name is required"
  }
}
```

---

### 2. Login

Authenticates a user and returns JWT tokens.

**Endpoint:** `POST /api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "mohamed@example.com",
  "password": "SecurePass@123"
}
```

**Request Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `email` | string | ✅ Yes | Registered email address |
| `password` | string | ✅ Yes | User password |

**Success Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "email": "mohamed@example.com",
  "role": "Client"
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `token` | string | JWT access token (expires in 24h) |
| `refreshToken` | string | JWT refresh token (expires in 7 days) |
| `type` | string | Token type (always "Bearer") |
| `email` | string | User's email |
| `role` | string | User's role |

**Error Responses:**

| Status | Description | Response |
|--------|-------------|----------|
| `401 Unauthorized` | Invalid credentials | `{ "error": "Bad credentials" }` |
| `400 Bad Request` | Validation error | `{ "errors": { "field": "message" } }` |

---

## 🔑 Using JWT Tokens

After login, include the JWT token in the `Authorization` header for protected endpoints:

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Token Expiration:**
- Access Token: 24 hours (86400000 ms)
- Refresh Token: 7 days (604800000 ms)

---

## 🛠 Admin API

All admin endpoints require `Authorization: Bearer <JWT>` and the authenticated user must have role **Admin**.  
Base path: `/api/admin`.

### 1. Create User (Ouvrier / Vétérinaire)

Creates an Ouvrier or Veterinaire account. Only these two roles are allowed.

**Endpoint:** `POST /api/admin/users`

**Request Body:**
```json
{
  "firstName": "Jean",
  "lastName": "Ouvrier",
  "email": "jean.ouvrier@example.com",
  "password": "SecurePass@123",
  "phoneNumber": "+212600000002",
  "role": "Ouvrier"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `firstName` | string | ✅ | max 100 chars |
| `lastName` | string | ✅ | max 100 chars |
| `email` | string | ✅ | valid email, unique |
| `password` | string | ✅ | min 8 chars |
| `phoneNumber` | string | ❌ | optional |
| `role` | string | ✅ | `Ouvrier` or `Veterinaire` only |

**Success:** `201 Created` — returns `UserResponse` (same shape as register).

**Errors:** `400` if email already exists or role is not Ouvrier/Veterinaire; `404` if admin user not found.

---

### 2. Buildings

Create and list buildings (name, max capacity, optional image).

**Create:** `POST /api/admin/buildings`

```json
{
  "name": "Bâtiment A",
  "maxCapacity": 5000,
  "imageUrl": "https://example.com/images/batiment-a.jpg"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | ✅ | max 100 chars |
| `maxCapacity` | number | ✅ | positive integer (poussins) |
| `imageUrl` | string | ❌ | max 512 chars, URL or path to image |

**Success:** `201 Created` — returns `BuildingResponse`: `id`, `name`, `maxCapacity`, `imageUrl`, `createdAt`, `updatedAt`.

**List:** `GET /api/admin/buildings` → `200 OK`, array of `BuildingResponse`.

**Get by ID:** `GET /api/admin/buildings/{id}` → `200 OK` or `404`.

---

### 3. Batches

Create a new batch (lot) with strain, quantity, purchase price, arrival date, optional building.

**Endpoint:** `POST /api/admin/batches`

```json
{
  "batchNumber": "BL-2026-001",
  "strain": "Cobb 500",
  "chickenCount": 2000,
  "arrivalDate": "2026-02-15",
  "purchasePrice": 15000.00,
  "buildingId": 1,
  "notes": "Premier lot de l'année"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `batchNumber` | string | ✅ | unique, max 50 chars |
| `strain` | string | ✅ | souche, max 100 chars |
| `chickenCount` | number | ✅ | positive |
| `arrivalDate` | string | ✅ | ISO date (YYYY-MM-DD) |
| `purchasePrice` | number | ✅ | prix d'achat |
| `buildingId` | number | ❌ | optional, must exist |
| `notes` | string | ❌ | max 2000 chars |

**Success:** `201 Created` — returns `BatchResponse`: `id`, `batchNumber`, `strain`, `chickenCount`, `arrivalDate`, `purchasePrice`, `buildingId`, `buildingName`, `status`, `notes`, `createdById`, `createdAt`, `updatedAt`.

**Errors:** `409 Conflict` if batch number already exists; `404` if building or user not found.

---

### 4. Stock

Add and list stock items (aliments, vaccins, vitamines).

**Add:** `POST /api/admin/stock`

```json
{
  "type": "Feed",
  "name": "Aliment démarrage",
  "quantity": 100.5,
  "unit": "sac"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | string | ✅ | `Feed` \| `Vaccine` \| `Vitamin` |
| `name` | string | ❌ | max 200 chars |
| `quantity` | number | ✅ | positive |
| `unit` | string | ✅ | e.g. `sac`, `dose`, `flacon`, max 50 chars |

**Success:** `201 Created` — returns `StockItemResponse`: `id`, `type`, `name`, `quantity`, `unit`, `createdAt`, `updatedAt`.

**List:** `GET /api/admin/stock` → `200 OK`, array of `StockItemResponse` (ordered by type, then name).

**Get by ID:** `GET /api/admin/stock/{id}` → `200 OK` or `404`.

---

## ❌ Error Handling

All errors follow a consistent format:

**Validation Errors (400):**
```json
{
  "errors": {
    "fieldName": "Error message"
  }
}
```

**Authentication Errors (401):**
```json
{
  "error": "Bad credentials",
  "status": 401
}
```

**General Errors:**
```json
{
  "message": "Error description",
  "status": 500
}
```

---

## 📦 Models

### User Response
```json
{
  "id": 1,
  "fullName": "string",
  "email": "string",
  "phoneNumber": "string | null",
  "role": "Admin | Ouvrier | Veterinaire | Client",
  "isActive": true,
  "address": "string | null",
  "city": "string | null",
  "postalCode": "string | null",
  "createdAt": "ISO 8601 datetime",
  "updatedAt": "ISO 8601 datetime",
  "lastLoginAt": "ISO 8601 datetime | null"
}
```

### JWT Response
```json
{
  "token": "string",
  "refreshToken": "string",
  "type": "Bearer",
  "email": "string",
  "role": "string"
}
```

---

## 📝 TypeScript Types

```typescript
// ============ Enums ============
export enum Role {
  Admin = 'Admin',
  Ouvrier = 'Ouvrier',
  Veterinaire = 'Veterinaire',
  Client = 'Client',
}

// ============ Request DTOs ============
export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber?: string;
  role?: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

// ============ Response DTOs ============
export interface UserResponse {
  id: number;
  fullName: string;
  email: string;
  phoneNumber: string | null;
  role: Role;
  isActive: boolean;
  address: string | null;
  city: string | null;
  postalCode: string | null;
  createdAt: string;
  updatedAt: string;
  lastLoginAt: string | null;
}

export interface JwtResponse {
  token: string;
  refreshToken: string;
  type: 'Bearer';
  email: string;
  role: string;
}

export interface ValidationError {
  errors: Record<string, string>;
}

export interface ApiError {
  error?: string;
  message?: string;
  status: number;
}
```

---

## 💻 Code Examples

### JavaScript/TypeScript (Fetch)

```typescript
const API_BASE = 'http://localhost:8081/api';

// Register
async function register(data: RegisterRequest): Promise<UserResponse> {
  const response = await fetch(`${API_BASE}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.errors ? Object.values(error.errors).join(', ') : 'Registration failed');
  }
  
  return response.json();
}

// Login
async function login(email: string, password: string): Promise<JwtResponse> {
  const response = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  
  if (!response.ok) {
    throw new Error('Invalid credentials');
  }
  
  return response.json();
}

// Authenticated Request
async function fetchWithAuth<T>(url: string, token: string): Promise<T> {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  });
  
  if (!response.ok) {
    throw new Error('Request failed');
  }
  
  return response.json();
}
```

### React Hook Example

```typescript
import { useState } from 'react';

interface AuthState {
  user: UserResponse | null;
  token: string | null;
  isLoading: boolean;
  error: string | null;
}

export function useAuth() {
  const [state, setState] = useState<AuthState>({
    user: null,
    token: localStorage.getItem('token'),
    isLoading: false,
    error: null,
  });

  const login = async (email: string, password: string) => {
    setState(s => ({ ...s, isLoading: true, error: null }));
    try {
      const response = await fetch('http://localhost:8081/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        throw new Error('Invalid credentials');
      }

      const data: JwtResponse = await response.json();
      localStorage.setItem('token', data.token);
      localStorage.setItem('refreshToken', data.refreshToken);
      
      setState({
        user: null, // fetch user profile separately if needed
        token: data.token,
        isLoading: false,
        error: null,
      });
      
      return data;
    } catch (err) {
      setState(s => ({
        ...s,
        isLoading: false,
        error: err instanceof Error ? err.message : 'Login failed',
      }));
      throw err;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    setState({ user: null, token: null, isLoading: false, error: null });
  };

  return { ...state, login, logout };
}
```

### Axios Instance

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  headers: { 'Content-Type': 'application/json' },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  register: (data: RegisterRequest) => api.post<UserResponse>('/auth/register', data),
  login: (data: LoginRequest) => api.post<JwtResponse>('/auth/login', data),
};
```

---

## 🧪 Testing with cURL

```bash
# Register (Client only; Admin/Ouvrier/Veterinaire cannot self-register)
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Mohamed",
    "lastName": "Client",
    "email": "client@example.com",
    "password": "Client@123"
  }'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@example.com",
    "password": "Client@123"
  }'

# Admin: create Ouvrier (use JWT from admin login)
curl -X POST http://localhost:8081/api/admin/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
    "firstName": "Jean",
    "lastName": "Ouvrier",
    "email": "jean@example.com",
    "password": "Ouvrier@123",
    "role": "Ouvrier"
  }'

# Admin: create building
curl -X POST http://localhost:8081/api/admin/buildings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{"name": "Bâtiment A", "maxCapacity": 5000}'

# Admin: create batch
curl -X POST http://localhost:8081/api/admin/batches \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
    "batchNumber": "BL-2026-001",
    "strain": "Cobb 500",
    "chickenCount": 2000,
    "arrivalDate": "2026-02-15",
    "purchasePrice": 15000
  }'

# Admin: add stock
curl -X POST http://localhost:8081/api/admin/stock \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{"type": "Feed", "name": "Aliment démarrage", "quantity": 100, "unit": "sac"}'
```

---

## 📌 Notes

- All timestamps are in **ISO 8601** format (UTC)
- Phone numbers should follow **E.164** format (e.g., `+212600000001`)
- Passwords must be **at least 8 characters**
- Email must be **unique** across all users
- Default role is `Client` if not specified

---

**Last Updated:** January 29, 2026  
**API Version:** 1.0.0
