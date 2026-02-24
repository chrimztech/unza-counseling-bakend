
import requests
import json

# Test endpoints for Unza Counseling System

BASE_URL = "http://localhost:8080/api"

# Test login and send message
def test_send_message():
    print("Testing login and sending message...")
    
    # Test login
    login_url = f"{BASE_URL}/auth/login"
    login_data = {
        "identifier": "counselor1@unza.zm",
        "password": "11111111"
    }
    
    try:
        login_response = requests.post(login_url, json=login_data)
        print(f"Login status: {login_response.status_code}")
        
        if login_response.status_code == 200:
            login_result = login_response.json()
            token = login_result.get("token")
            
            print(f"Login successful, token received: {token[:50]}...")
            
            # Test sending message
            message_url = f"{BASE_URL}/messages"
            message_headers = {
                "Authorization": f"Bearer {token}"
            }
            message_data = {
                "recipientId": 2,  # Assuming client has ID 2
                "subject": "Test Message",
                "content": "This is a test message from counselor to client"
            }
            
            message_response = requests.post(message_url, headers=message_headers, json=message_data)
            print(f"Send message status: {message_response.status_code}")
            
            if message_response.status_code == 200:
                print("Message sent successfully!")
                print(f"Response: {message_response.json()}")
            else:
                print(f"Failed to send message. Response: {message_response.text}")
        else:
            print(f"Login failed. Response: {login_response.text}")
            
    except Exception as e:
        print(f"Error: {str(e)}")

# Test unread count
def test_unread_count():
    print("\nTesting unread count...")
    
    login_url = f"{BASE_URL}/auth/login"
    login_data = {
        "identifier": "counselor1@unza.zm",
        "password": "11111111"
    }
    
    try:
        login_response = requests.post(login_url, json=login_data)
        
        if login_response.status_code == 200:
            login_result = login_response.json()
            token = login_result.get("token")
            
            unread_url = f"{BASE_URL}/messages/unread-count"
            unread_headers = {
                "Authorization": f"Bearer {token}"
            }
            
            unread_response = requests.get(unread_url, headers=unread_headers)
            print(f"Unread count status: {unread_response.status_code}")
            
            if unread_response.status_code == 200:
                print(f"Unread count: {unread_response.json()}")
            else:
                print(f"Failed to get unread count. Response: {unread_response.text}")
        else:
            print(f"Login failed. Response: {login_response.text}")
            
    except Exception as e:
        print(f"Error: {str(e)}")

# Run tests
if __name__ == "__main__":
    test_send_message()
    test_unread_count()
