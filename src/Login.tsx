import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from './services/authService';

const Login: React.FC = () => {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!identifier || !password) {
      setErrorMessage('Please enter your ID or email and password.');
      return;
    }

    try {
      setIsLoading(true);
      const response = await authService.login({
        identifier: identifier.trim(),
        password,
      });

      const userData = response?.user;

      if (!userData) {
        throw new Error('Authentication failed');
      }

      redirectByRole(userData);
    } catch (err: any) {
      setErrorMessage(
        err?.response?.data?.error ||
        err?.response?.data?.message ||
        err.message ||
        'Login failed. Please check your credentials.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const redirectByRole = (userData: any) => {
    const roles = userData.roles?.map((r: any) => r.name) || [];

    if (roles.includes('ROLE_ADMIN')) {
      navigate('/admin/dashboard', { replace: true });
      return;
    }

    if (roles.includes('ROLE_COUNSELOR')) {
      navigate('/counselor/dashboard', { replace: true });
      return;
    }

    if (roles.includes('ROLE_STUDENT')) {
      navigate('/client/dashboard', { replace: true });
      return;
    }

    if (roles.includes('ROLE_STAFF')) {
      navigate('/client/dashboard', { replace: true });
      return;
    }

    if (roles.includes('ROLE_CLIENT')) {
      navigate('/client/dashboard', { replace: true });
      return;
    }

    setErrorMessage('Unable to determine user role. Please contact support.');
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      fontFamily: 'Arial, sans-serif'
    }}>
      <div style={{
        background: 'white',
        padding: '2rem',
        borderRadius: '10px',
        boxShadow: '0 15px 35px rgba(0, 0, 0, 0.1)',
        width: '100%',
        maxWidth: '400px',
        animation: 'fadeIn 0.5s ease-in-out'
      }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h1 style={{
            color: '#333',
            marginBottom: '0.5rem',
            fontSize: '2rem',
            fontWeight: 'bold'
          }}>
            UNZA Counseling
          </h1>
          <p style={{ color: '#666', fontSize: '0.9rem' }}>
            University of Zambia Counseling Management System
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '1.5rem' }}>
            <label
              htmlFor="identifier"
              style={{
                display: 'block',
                marginBottom: '0.5rem',
                color: '#333',
                fontWeight: '500'
              }}
            >
              Student ID or University Email
            </label>
            <input
              id="identifier"
              type="text"
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              placeholder="e.g. 2023001234 or name@unza.zm"
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '1rem',
                transition: 'border-color 0.3s ease',
                boxSizing: 'border-box'
              }}
              onFocus={(e) => e.target.style.borderColor = '#667eea'}
              onBlur={(e) => e.target.style.borderColor = '#ddd'}
              disabled={isLoading}
            />
            <p style={{
              marginTop: '0.25rem',
              fontSize: '0.8rem',
              color: '#666'
            }}>
              Students use Student ID • Staff use University Email
            </p>
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <label
              htmlFor="password"
              style={{
                display: 'block',
                marginBottom: '0.5rem',
                color: '#333',
                fontWeight: '500'
              }}
            >
              Password
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '1rem',
                transition: 'border-color 0.3s ease',
                boxSizing: 'border-box'
              }}
              onFocus={(e) => e.target.style.borderColor = '#667eea'}
              onBlur={(e) => e.target.style.borderColor = '#ddd'}
              disabled={isLoading}
            />
          </div>

          {errorMessage && (
            <div style={{
              backgroundColor: '#f8d7da',
              color: '#721c24',
              padding: '0.75rem',
              borderRadius: '5px',
              marginBottom: '1rem',
              fontSize: '0.9rem',
              border: '1px solid #f5c6cb'
            }}>
              {errorMessage}
            </div>
          )}

          <button
            type="submit"
            disabled={isLoading}
            style={{
              width: '100%',
              padding: '0.75rem',
              background: isLoading ? '#ccc' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              fontSize: '1rem',
              fontWeight: '500',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              transition: 'transform 0.2s ease, box-shadow 0.2s ease',
              boxShadow: '0 4px 15px rgba(102, 126, 234, 0.4)'
            }}
            onMouseEnter={(e) => {
              if (!isLoading) {
                e.target.style.transform = 'translateY(-2px)';
                e.target.style.boxShadow = '0 6px 20px rgba(102, 126, 234, 0.6)';
              }
            }}
            onMouseLeave={(e) => {
              if (!isLoading) {
                e.target.style.transform = 'translateY(0)';
                e.target.style.boxShadow = '0 4px 15px rgba(102, 126, 234, 0.4)';
              }
            }}
          >
            {isLoading ? 'Signing In...' : 'Sign In'}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
          <p style={{ color: '#666', fontSize: '0.8rem' }}>
            Need help? Contact IT Support
          </p>
        </div>
      </div>

      <style>
        {`
          @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-20px); }
            to { opacity: 1; transform: translateY(0); }
          }
        `}
      </style>
    </div>
  );
};

export default Login;