#!/bin/bash

# Get token
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin@unza.zm","password":"Admin@123"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | sed 's/.*"token":"\([^"]*\)".*/\1/')
echo "Token obtained: ${TOKEN:0:50}..."

echo ""
echo "=============================================="
echo "Testing all endpoints after login"
echo "=============================================="

echo ""
echo "1. Dashboard Stats..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/dashboard/stats" | head -c 500

echo ""
echo ""
echo "2. Risk Assessments..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/risk-assessments?page=0&size=10" | head -c 500

echo ""
echo ""
echo "3. Counselors..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/counselors" | head -c 500

echo ""
echo ""
echo "4. Sessions..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/sessions" | head -c 500

echo ""
echo ""
echo "5. Appointments..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/appointments" | head -c 500

echo ""
echo ""
echo "6. Analytics Overview..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/analytics/overview" | head -c 500

echo ""
echo ""
echo "7. Reports..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/reports" | head -c 500

echo ""
echo ""
echo "8. Users..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/users" | head -c 500

echo ""
echo ""
echo "9. Clients..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/clients" | head -c 500

echo ""
echo ""
echo "10. Notifications..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/notifications" | head -c 500

echo ""
echo ""
echo "11. Settings..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/settings" | head -c 500

echo ""
echo ""
echo "12. Profile..."
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/users/2" | head -c 500

echo ""
echo ""
echo "=============================================="
echo "All tests completed"
echo "=============================================="
