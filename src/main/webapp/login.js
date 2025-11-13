/**
 * Alumni Mentoring Platform - Login Page Script
 * 
 * Handles user authentication form submission and API communication.
 * Provides client-side validation and error handling for login process.
 * 
 * @author Alumni Mentoring Platform
 * @version 1.0
 */
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('loginForm');
  const notice = document.getElementById('notice');

  // Check if API is loaded
  if (typeof API === 'undefined') {
    notice.innerHTML = '<div class="notice error">API not loaded. Please refresh the page.</div>';
    return;
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    notice.innerHTML = '';
    
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    if (!email || !password) {
      notice.innerHTML = '<div class="notice error">Please enter both email and password</div>';
      return;
    }

    try {
      const resp = await API.login(email, password);

      if (resp.ok) {
        // Login successful - redirect to dashboard
        window.location.href = 'dashboard.html';
      } else if (resp.status === 401) {
        notice.innerHTML = '<div class="notice error">Invalid email or password</div>';
      } else {
        const errorData = await resp.text();
        console.error('Login failed with status:', resp.status, 'Error:', errorData);
        notice.innerHTML = `<div class="notice error">Login failed: ${errorData || 'Unknown error'}</div>`;
      }
    } catch (err) {
      console.error('Login error details:', err);
      console.error('Error type:', err.name);
      console.error('Error message:', err.message);
      notice.innerHTML = `<div class="notice error">Network error: ${err.message}. Please check if the server is running.</div>`;
    }
  });
});
