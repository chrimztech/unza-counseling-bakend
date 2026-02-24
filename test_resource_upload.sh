#!/bin/bash

# Test script for resource management endpoints

BASE_URL="http://localhost:8080/api"
ADMIN_EMAIL="superadmin@unza.zm"
ADMIN_PASSWORD="password"

# Function to get JWT token
get_token() {
    echo "Getting JWT token..."
    RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"identifier\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}")
    
    TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    
    if [ -z "$TOKEN" ]; then
        echo "Error: Failed to get token from response: $RESPONSE"
        return 1
    fi
    
    echo "Token received: $TOKEN"
    return 0
}

# Function to test creating a resource without file
test_create_resource() {
    echo "Testing create resource endpoint..."
    
    DATA=$(cat <<EOF
{
    "title": "Test Resource",
    "description": "This is a test resource",
    "type": "ARTICLE",
    "category": "Anxiety Management",
    "tags": ["anxiety", "stress", "test"],
    "featured": true,
    "url": "https://example.com/test-resource"
}
EOF
)
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/resources" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "$DATA")
    
    echo "Response: $RESPONSE"
    
    if [ $(echo "$RESPONSE" | grep -c '"success":true') -gt 0 ] || [ $(echo "$RESPONSE" | grep -c '"id":') -gt 0 ]; then
        echo "✅ Create resource test passed"
    else
        echo "❌ Create resource test failed"
        return 1
    fi
}

# Function to test uploading a resource with file
test_upload_resource() {
    echo "Testing upload resource endpoint..."
    
    # Create a dummy file
    cat > test_file.txt <<EOF
This is a test file for resource upload
EOF
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/resources/upload" \
        -H "Authorization: Bearer $TOKEN" \
        -F "file=@test_file.txt" \
        -F "title=Test Uploaded Resource" \
        -F "description=This is a test uploaded resource" \
        -F "type=DOCUMENT" \
        -F "category=Depression Support" \
        -F "tags=depression, test, upload" \
        -F "featured=false")
    
    echo "Response: $RESPONSE"
    
    if [ $(echo "$RESPONSE" | grep -c '"success":true') -gt 0 ] || [ $(echo "$RESPONSE" | grep -c '"id":') -gt 0 ]; then
        echo "✅ Upload resource test passed"
    else
        echo "❌ Upload resource test failed"
        return 1
    fi
    
    rm test_file.txt
}

# Function to test getting all resources
test_get_resources() {
    echo "Testing get resources endpoint..."
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/resources" \
        -H "Authorization: Bearer $TOKEN")
    
    echo "Response: $RESPONSE"
    
    if [ $(echo "$RESPONSE" | grep -c '"title":') -gt 0 ]; then
        echo "✅ Get resources test passed"
    else
        echo "❌ Get resources test failed"
        return 1
    fi
}

# Main execution
echo "Starting resource management endpoint tests..."
echo "=============================================="

if ! get_token; then
    exit 1
fi

echo
test_create_resource

echo
test_upload_resource

echo
test_get_resources

echo
echo "=============================================="
echo "All tests completed"
