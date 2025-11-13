// Session-based auth guard and helpers
(function(global){
  function requireAuth(redirectTo){
    // With API-based authentication, the server handles session management
    // If we can access protected pages, we're authenticated
    // If not, the server will redirect to login.html
    return true;
  }
  
  function logout(){
    // Call logout API endpoint to invalidate the session
    fetch('/AlumniMentoring/resources/auth/logout', {
      method: 'POST',
      credentials: 'same-origin'
    }).then(() => {
      // Redirect to login page after logout
      window.location.href = 'login.html';
    }).catch(() => {
      // Even if logout fails, redirect to login page
      window.location.href = 'login.html';
    });
  }
  
  function isAuthenticated(){
    // Check if we're on a protected page - if so, we're authenticated
    const protectedPages = ['dashboard.html', 'alumni.html', 'requests.html', 'chat.html', 'profile.html'];
    const currentPage = window.location.pathname.split('/').pop();
    return protectedPages.includes(currentPage);
  }
  
  global.Auth = { requireAuth, logout, isAuthenticated };
})(window);


