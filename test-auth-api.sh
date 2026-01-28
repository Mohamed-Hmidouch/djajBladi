#!/bin/bash
# Tests API REST avec curl pour AuthService
# ✅ Spring Boot Best Practice & Agent-MD: Tests des endpoints

BASE_URL="http://localhost:8081/api/auth"

echo "========================================="
echo "🧪 Tests API REST - AuthService"
echo "========================================="
echo ""

# Test 1: Register - Admin
echo "📝 Test 1: Register Admin"
REGISTER_ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Admin",
    "lastName": "User",
    "email": "admin@djajbladi.com",
    "password": "Admin@123",
    "phoneNumber": "+212600000001",
    "role": "Admin"
  }')

echo "Response: $REGISTER_ADMIN_RESPONSE"
echo ""

# Test 2: Register - Ouvrier
echo "📝 Test 2: Register Ouvrier"
REGISTER_WORKER_RESPONSE=$(curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ouvrier",
    "lastName": "Worker",
    "email": "ouvrier@djajbladi.com",
    "password": "Worker@123",
    "phoneNumber": "+212600000002",
    "role": "Ouvrier"
  }')

echo "Response: $REGISTER_WORKER_RESPONSE"
echo ""

# Test 3: Register - Veterinaire
echo "📝 Test 3: Register Veterinaire"
REGISTER_VET_RESPONSE=$(curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Dr",
    "lastName": "Veterinaire",
    "email": "vet@djajbladi.com",
    "password": "Vet@123",
    "phoneNumber": "+212600000003",
    "role": "Veterinaire"
  }')

echo "Response: $REGISTER_VET_RESPONSE"
echo ""

# Test 4: Register - Client
echo "📝 Test 4: Register Client"
REGISTER_CLIENT_RESPONSE=$(curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Client",
    "lastName": "Test",
    "email": "client@djajbladi.com",
    "password": "Client@123",
    "phoneNumber": "+212600000004",
    "role": "Client"
  }')

echo "Response: $REGISTER_CLIENT_RESPONSE"
echo ""

# Test 5: Register - Email déjà existant (doit échouer)
echo "📝 Test 5: Register avec email existant (doit échouer - 400)"
REGISTER_DUPLICATE_RESPONSE=$(curl -s -w "\nHTTP Status: %{http_code}" -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Duplicate",
    "lastName": "User",
    "email": "admin@djajbladi.com",
    "password": "Test@123",
    "role": "Admin"
  }')

echo "Response: $REGISTER_DUPLICATE_RESPONSE"
echo ""

# Test 6: Login - Admin
echo "📝 Test 6: Login Admin"
LOGIN_ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@djajbladi.com",
    "password": "Admin@123"
  }')

echo "Response: $LOGIN_ADMIN_RESPONSE"

# Extraire le JWT token pour les tests suivants
JWT_TOKEN=$(echo $LOGIN_ADMIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "JWT Token: ${JWT_TOKEN:0:50}..."
echo ""

# Test 7: Login - Credentials incorrects (doit échouer)
echo "📝 Test 7: Login avec mauvais mot de passe (doit échouer - 401)"
LOGIN_FAILED_RESPONSE=$(curl -s -w "\nHTTP Status: %{http_code}" -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@djajbladi.com",
    "password": "WrongPassword"
  }')

echo "Response: $LOGIN_FAILED_RESPONSE"
echo ""

# Test 8: Login - Email inexistant (doit échouer)
echo "📝 Test 8: Login avec email inexistant (doit échouer - 401)"
LOGIN_NOT_FOUND_RESPONSE=$(curl -s -w "\nHTTP Status: %{http_code}" -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@djajbladi.com",
    "password": "Test@123"
  }')

echo "Response: $LOGIN_NOT_FOUND_RESPONSE"
echo ""

# Test 9: Register - Validation (champs manquants)
echo "📝 Test 9: Register sans email (doit échouer - 400)"
REGISTER_NO_EMAIL_RESPONSE=$(curl -s -w "\nHTTP Status: %{http_code}" -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "password": "Test@123",
    "role": "Client"
  }')

echo "Response: $REGISTER_NO_EMAIL_RESPONSE"
echo ""

echo "========================================="
echo "✅ Tests API REST terminés!"
echo "========================================="
echo ""
echo "✅ Conforme aux bonnes pratiques Spring Boot & Agent-MD"
