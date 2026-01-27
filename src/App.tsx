import React, { useState, useEffect } from 'react';
import './App.css';
import { dashboardService } from './services/dashboardService';
import { clientService } from './services/clientService';
import { authService } from './services/authService';
import { DashboardStats, Client } from './types/api';

function App() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [clients, setClients] = useState<Client[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Check if user is authenticated
      if (!authService.isAuthenticated()) {
        setError('Not authenticated. Please log in.');
        return;
      }

      // Load dashboard stats and clients
      const [statsData, clientsData] = await Promise.all([
        dashboardService.getStats(),
        clientService.getClients()
      ]);

      setStats(statsData);
      setClients(clientsData);
    } catch (err) {
      console.error('Error loading dashboard data:', err);
      setError('Failed to load dashboard data. Please check if the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async () => {
    try {
      // Demo login with test credentials
      const loginData = {
        identifier: 'admin@unza.zm',
        password: 'Admin@123'
      };
      
      const response = await authService.login(loginData);
      authService.setToken(response.token);
      
      // Reload dashboard data after login
      loadDashboardData();
    } catch (err) {
      console.error('Login failed:', err);
      setError('Login failed. Please check your credentials.');
    }
  };

  if (loading) {
    return (
      <div className="App">
        <header className="App-header">
          <h1>UNZA Counseling Management System</h1>
          <p>Loading dashboard data...</p>
        </header>
      </div>
    );
  }

  return (
    <div className="App">
      <header className="App-header">
        <h1>UNZA Counseling Management System</h1>
        
        {error && (
          <div style={{ 
            backgroundColor: '#f8d7da', 
            color: '#721c24', 
            padding: '10px', 
            borderRadius: '5px', 
            margin: '10px 0',
            maxWidth: '600px'
          }}>
            <strong>Error:</strong> {error}
            {!authService.isAuthenticated() && (
              <button 
                onClick={handleLogin}
                style={{
                  marginLeft: '10px',
                  padding: '5px 10px',
                  backgroundColor: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '3px',
                  cursor: 'pointer'
                }}
              >
                Try Demo Login
              </button>
            )}
          </div>
        )}

        {stats && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '20px', marginTop: '20px' }}>
            <div style={statCardStyle}>
              <h3>Total Clients</h3>
              <p style={statNumberStyle}>{stats.totalClients}</p>
            </div>
            <div style={statCardStyle}>
              <h3>Total Appointments</h3>
              <p style={statNumberStyle}>{stats.totalAppointments}</p>
            </div>
            <div style={statCardStyle}>
              <h3>Upcoming Appointments</h3>
              <p style={statNumberStyle}>{stats.upcomingAppointments}</p>
            </div>
            <div style={statCardStyle}>
              <h3>At-Risk Students</h3>
              <p style={statNumberStyle}>{stats.atRiskStudents}</p>
            </div>
          </div>
        )}

        <div style={{ marginTop: '40px', maxWidth: '800px' }}>
          <h2>Recent Clients</h2>
          {clients.length > 0 ? (
            <div style={clientsListStyle}>
              {clients.slice(0, 5).map((client) => (
                <div key={client.id} style={clientCardStyle}>
                  <h4>{client.user.firstName} {client.user.lastName}</h4>
                  <p>Email: {client.user.email}</p>
                  <p>Student ID: {client.studentId || 'N/A'}</p>
                  <p>Status: {client.user.enabled ? 'Active' : 'Inactive'}</p>
                </div>
              ))}
            </div>
          ) : (
            <p>No clients found.</p>
          )}
        </div>

        <div style={{ marginTop: '30px' }}>
          <p>Backend API: <code>http://localhost:8080/api</code></p>
          <p>Frontend URL: <code>http://localhost:3000</code></p>
        </div>
      </header>
    </div>
  );
}

// Styles
const statCardStyle: React.CSSProperties = {
  backgroundColor: '#f8f9fa',
  padding: '20px',
  borderRadius: '8px',
  textAlign: 'center',
  minWidth: '150px',
  boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
};

const statNumberStyle: React.CSSProperties = {
  fontSize: '2rem',
  fontWeight: 'bold',
  color: '#007bff',
  margin: '10px 0 0 0'
};

const clientsListStyle: React.CSSProperties = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
  gap: '15px',
  marginTop: '15px'
};

const clientCardStyle: React.CSSProperties = {
  backgroundColor: '#ffffff',
  padding: '15px',
  borderRadius: '8px',
  boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
  border: '1px solid #e9ecef'
};

export default App;
