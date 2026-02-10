/**
 * ConsentGuard - React Component to guard routes requiring consent
 * 
 * Usage: Wrap protected routes with this component
 * Example:
 *   <Route path="/dashboard" element={
 *     <ConsentGuard>
 *       <Dashboard />
 *     </ConsentGuard>
 *   } />
 */

import React, { useState, useEffect, useCallback } from 'react';
import consentService, { ConsentFormResponse } from '../services/consentService';

interface ConsentGuardProps {
  children: React.ReactNode;
  onConsentRequired?: () => void;
  onConsentComplete?: () => void;
}

interface ConsentGuardState {
  isLoading: boolean;
  hasSigned: boolean;
  consentForm: ConsentFormResponse | null;
  error: string | null;
}

const ConsentGuard: React.FC<ConsentGuardProps> = ({
  children,
  onConsentRequired,
  onConsentComplete,
}) => {
  const [state, setState] = useState<ConsentGuardState>({
    isLoading: true,
    hasSigned: true,
    consentForm: null,
    error: null,
  });

  const checkConsentStatus = useCallback(async () => {
    try {
      console.log('ConsentGuard - Checking consent status...');
      
      // Check if user has signed
      const hasSigned = await consentService.checkSignedConsent();
      console.log('ConsentGuard - Has signed:', hasSigned);
      
      if (hasSigned) {
        setState({
          isLoading: false,
          hasSigned: true,
          consentForm: null,
          error: null,
        });
        return;
      }

      // User hasn't signed, get the consent form
      const consentForm = await consentService.getLatestActiveConsentForm();
      console.log('ConsentGuard - Consent form:', consentForm);
      
      setState({
        isLoading: false,
        hasSigned: false,
        consentForm: consentForm,
        error: null,
      });

      // Notify parent if consent is required
      if (onConsentRequired && !consentForm) {
        onConsentRequired();
      }
    } catch (error: any) {
      console.error('ConsentGuard - Failed to check consent status:', error);
      
      // On error, assume consent is not required (graceful degradation)
      setState({
        isLoading: false,
        hasSigned: true,
        consentForm: null,
        error: null,
      });
    }
  }, [onConsentRequired]);

  useEffect(() => {
    checkConsentStatus();
  }, [checkConsentStatus]);

  if (state.isLoading) {
    return (
      <div style={styles.loadingContainer}>
        <div style={styles.loadingSpinner}></div>
        <p>Checking consent status...</p>
      </div>
    );
  }

  // If consent form is displayed, render consent form instead of children
  if (!state.hasSigned && state.consentForm) {
    return (
      <ConsentFormHandler
        consentForm={state.consentForm}
        onComplete={() => {
          setState({
            isLoading: false,
            hasSigned: true,
            consentForm: null,
            error: null,
          });
          if (onConsentComplete) {
            onConsentComplete();
          }
        }}
      />
    );
  }

  // Consent is signed or not required, render children
  return <>{children}</>;
};

// Consent Form Handler Component
interface ConsentFormHandlerProps {
  consentForm: ConsentFormResponse;
  onComplete: () => void;
}

const ConsentFormHandler: React.FC<ConsentFormHandlerProps> = ({
  consentForm,
  onComplete,
}) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [agreed, setAgreed] = useState(false);

  const handleSubmit = async () => {
    if (!agreed) {
      setError('You must agree to the consent terms to continue.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      console.log('ConsentFormHandler - Signing consent form...');
      
      await consentService.signConsent({
        consentFormId: consentForm.id,
      });
      
      console.log('ConsentFormHandler - Consent signed successfully!');
      onComplete();
    } catch (err: any) {
      console.error('ConsentFormHandler - Failed to sign consent:', err);
      setError(err.response?.data?.message || 'Failed to sign consent. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={styles.formContainer}>
      <div style={styles.formCard}>
        <h2 style={styles.title}>{consentForm.title}</h2>
        
        <div style={styles.version}>
          Version: {consentForm.version} | Effective Date: {new Date(consentForm.effectiveDate).toLocaleDateString()}
        </div>

        <div style={styles.content}>
          <div 
            dangerouslySetInnerHTML={{ __html: consentForm.content }}
            style={styles.htmlContent}
          />
        </div>

        <div style={styles.agreement}>
          <label style={styles.checkboxLabel}>
            <input
              type="checkbox"
              checked={agreed}
              onChange={(e) => setAgreed(e.target.checked)}
              style={styles.checkbox}
            />
            I have read, understood, and agree to the terms outlined above.
          </label>
        </div>

        {error && (
          <div style={styles.error}>
            {error}
          </div>
        )}

        <div style={styles.buttonGroup}>
          <button
            onClick={handleSubmit}
            disabled={!agreed || isSubmitting}
            style={{
              ...styles.button,
              ...styles.primaryButton,
              ...((!agreed || isSubmitting) ? styles.disabledButton : {}),
            }}
          >
            {isSubmitting ? 'Signing...' : 'I Agree & Continue'}
          </button>
        </div>
      </div>
    </div>
  );
};

// Styles
const styles: Record<string, React.CSSProperties> = {
  loadingContainer: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    backgroundColor: '#f5f5f5',
  },
  loadingSpinner: {
    width: '40px',
    height: '40px',
    border: '4px solid #f3f3f3',
    borderTop: '4px solid #3498db',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite',
    marginBottom: '16px',
  },
  formContainer: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    backgroundColor: '#f5f5f5',
    padding: '20px',
  },
  formCard: {
    backgroundColor: 'white',
    borderRadius: '12px',
    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.1)',
    padding: '32px',
    maxWidth: '700px',
    width: '100%',
    maxHeight: '90vh',
    overflowY: 'auto',
  },
  title: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#333',
    marginBottom: '8px',
    textAlign: 'center',
  },
  version: {
    fontSize: '14px',
    color: '#666',
    textAlign: 'center',
    marginBottom: '24px',
    paddingBottom: '16px',
    borderBottom: '1px solid #eee',
  },
  content: {
    marginBottom: '24px',
  },
  htmlContent: {
    fontSize: '15px',
    lineHeight: '1.6',
    color: '#444',
  },
  agreement: {
    marginBottom: '24px',
  },
  checkboxLabel: {
    display: 'flex',
    alignItems: 'flex-start',
    cursor: 'pointer',
    fontSize: '15px',
    color: '#333',
    lineHeight: '1.5',
  },
  checkbox: {
    marginRight: '12px',
    marginTop: '3px',
    width: '18px',
    height: '18px',
    cursor: 'pointer',
  },
  error: {
    backgroundColor: '#fee2e2',
    color: '#dc2626',
    padding: '12px 16px',
    borderRadius: '8px',
    marginBottom: '16px',
    fontSize: '14px',
  },
  buttonGroup: {
    display: 'flex',
    justifyContent: 'center',
  },
  button: {
    padding: '14px 32px',
    fontSize: '16px',
    fontWeight: '600',
    borderRadius: '8px',
    cursor: 'pointer',
    border: 'none',
    transition: 'all 0.2s ease',
  },
  primaryButton: {
    backgroundColor: '#2563eb',
    color: 'white',
  },
  disabledButton: {
    backgroundColor: '#9ca3af',
    cursor: 'not-allowed',
  },
};

// Add global styles for spin animation
const styleSheet = document.createElement('style');
styleSheet.textContent = `
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
`;
document.head.appendChild(styleSheet);

export default ConsentGuard;
