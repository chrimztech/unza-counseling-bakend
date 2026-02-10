# Frontend Consent Implementation

This directory contains the React frontend implementation for the consent system.

## Files

- `src/services/consentService.ts` - API service for consent operations
- `src/components/ConsentGuard.tsx` - React component to guard routes requiring consent

## Installation

1. Copy the files to your React project:
   - Copy `src/services/consentService.ts` to your project's `src/services/` directory
   - Copy `src/components/ConsentGuard.tsx` to your project's `src/components/` directory

2. Install axios:
   ```bash
   npm install axios
   ```

## Usage

### 1. Wrap Protected Routes with ConsentGuard

In your routing configuration (e.g., `App.tsx` or `routes.tsx`):

```tsx
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import ConsentGuard from './components/ConsentGuard';
import Dashboard from './pages/Dashboard';
import Appointments from './pages/Appointments';
import Login from './pages/Login';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        
        <Route path="/dashboard" element={
          <ConsentGuard>
            <Dashboard />
          </ConsentGuard>
        } />
        
        <Route path="/appointments" element={
          <ConsentGuard>
            <Appointments />
          </ConsentGuard>
        } />
        
        {/* Other protected routes... */}
      </Routes>
    </Router>
  );
}

export default App;
```

### 2. Handle Consent Events (Optional)

You can handle consent events with callbacks:

```tsx
<ConsentGuard
  onConsentRequired={() => {
    console.log('User needs to sign consent');
    // Track analytics, show toast, etc.
  }}
  onConsentComplete={() => {
    console.log('Consent signed successfully');
    // Track analytics, show welcome message, etc.
  }}
>
  <Dashboard />
</ConsentGuard>
```

## How It Works

1. **User Login**: User logs in with their credentials
2. **Consent Check**: When accessing a protected route, `ConsentGuard` checks if the user has signed the consent form
3. **Consent Required**: If the user hasn't signed, they see the consent form
4. **Sign & Continue**: User reads and agrees to the terms, then submits
5. **Access Granted**: After signing, the user can access the protected content

## Backend Endpoints

The frontend expects these backend endpoints (already implemented in the backend):

- `GET /api/consent/check-signed` - Check if user has signed
- `GET /api/consent/forms/latest` - Get the latest consent form
- `POST /api/consent/sign` - Sign a consent form
- `GET /api/consent/history` - Get user's consent history

## Customization

### Styling

The components use inline styles for simplicity. You can customize by:

1. Modifying the `styles` object in `ConsentGuard.tsx`
2. Converting to CSS modules or styled-components
3. Using a UI library (MUI, AntD, etc.)

### Consent Form Content

The consent form content is stored in the backend database. Use the admin interface to create/edit consent forms.

## Troubleshooting

### 404 Error on Consent Endpoints

Make sure the backend is running and the API URL is correct:

```typescript
// In consentService.ts
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
```

Set the environment variable in `.env`:
```
REACT_APP_API_URL=http://localhost:8080/api
```

### Token Not Being Sent

Ensure the user is logged in before accessing protected routes. The consent service expects a valid JWT token in localStorage:

```javascript
// After login
localStorage.setItem('token', 'your-jwt-token');
```

### CORS Errors

Make sure your backend allows CORS requests from your frontend origin. In the backend `SecurityConfig.java`:

```java
registry.addMapping("/**")
  .allowedOriginPatterns("http://localhost:*")
  .allowedMethods("GET", "POST", "PUT", "DELETE");
```
