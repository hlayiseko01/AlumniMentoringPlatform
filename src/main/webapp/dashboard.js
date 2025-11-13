document.addEventListener('DOMContentLoaded', async () => {
  // Check if user is authenticated by calling current-user endpoint
  let user;
  try {
    user = await API.getCurrentUser();
  } catch (error) {
    window.location.href = 'login.html';
    return;
  }

  // Update welcome section
  updateWelcomeSection(user);
  
  // Load dashboard content
  await loadDashboardContent(user);
});

function updateWelcomeSection(user) {
  const welcomeTitle = document.querySelector('.welcome-title');
  const welcomeSubtitle = document.querySelector('.welcome-subtitle');
  const userName = document.querySelector('.user-details h3');
  const userRole = document.querySelector('.user-role');
  
  if (welcomeTitle) {
    welcomeTitle.textContent = `Welcome back, ${user.fullName || user.email}!`;
  }
  
  if (welcomeSubtitle) {
    const roleText = user.role === 'STUDENT' ? 'Student' : 
                    user.role === 'ALUMNI' ? 'Alumni Mentor' : 'Administrator';
    welcomeSubtitle.textContent = `Here's your ${roleText} dashboard overview`;
  }
  
  if (userName) {
    userName.textContent = user.fullName || user.email;
  }
  
  if (userRole) {
    const roleText = user.role === 'STUDENT' ? 'Student' : 
                    user.role === 'ALUMNI' ? 'Alumni Mentor' : 'Administrator';
    userRole.textContent = roleText;
  }
}

async function loadDashboardContent(user) {
  const stats = document.getElementById('stats');
  const quickActions = document.querySelector('.quick-actions');
  const recentActivity = document.querySelector('.recent-activity');
  
  try {
    const userRole = user.role;
    const requests = await API.getRequests();
    const accepted = requests.filter(r => r.status === 'ACCEPTED').length;
    const pending = requests.filter(r => r.status === 'PENDING').length;
    
    // Render stats based on user role
    if (userRole === 'STUDENT') {
      const alumni = await API.getAlumni();
      stats.innerHTML = `
        <div class="stat-card mentors">
          <div class="stat-header">
            <div class="stat-title">Available Mentors</div>
            <div class="stat-icon"><i class="fas fa-users"></i></div>
          </div>
          <div class="stat-value">${alumni.length}</div>
          <div class="stat-description">Connect with experienced alumni</div>
          <a href="alumni.html" class="stat-action">
            Browse Mentors <i class="fas fa-arrow-right"></i>
          </a>
        </div>
        <div class="stat-card requests">
          <div class="stat-header">
            <div class="stat-title">My Pending Requests</div>
            <div class="stat-icon"><i class="fas fa-clock"></i></div>
          </div>
          <div class="stat-value">${pending}</div>
          <div class="stat-description">Awaiting mentor response</div>
          <a href="requests.html" class="stat-action">
            View Requests <i class="fas fa-arrow-right"></i>
          </a>
        </div>
        <div class="stat-card chat">
          <div class="stat-header">
            <div class="stat-title">Active Mentorships</div>
            <div class="stat-icon"><i class="fas fa-handshake"></i></div>
          </div>
          <div class="stat-value">${accepted}</div>
          <div class="stat-description">Ongoing mentoring relationships</div>
          <a href="chat.html" class="stat-action">
            Open Chat <i class="fas fa-arrow-right"></i>
          </a>
        </div>`;
    } else if (userRole === 'ALUMNI' || userRole === 'ADMIN') {
      stats.innerHTML = `
        <div class="stat-card requests">
          <div class="stat-header">
            <div class="stat-title">Pending Requests</div>
            <div class="stat-icon"><i class="fas fa-clock"></i></div>
          </div>
          <div class="stat-value">${pending}</div>
          <div class="stat-description">Awaiting your response</div>
          <a href="requests.html" class="stat-action">
            Review Requests <i class="fas fa-arrow-right"></i>
          </a>
        </div>
        <div class="stat-card chat">
          <div class="stat-header">
            <div class="stat-title">Active Mentorships</div>
            <div class="stat-icon"><i class="fas fa-handshake"></i></div>
          </div>
          <div class="stat-value">${accepted}</div>
          <div class="stat-description">Ongoing mentoring relationships</div>
          <a href="chat.html" class="stat-action">
            Open Chat <i class="fas fa-arrow-right"></i>
          </a>
        </div>
        <div class="stat-card default">
          <div class="stat-header">
            <div class="stat-title">Total Requests</div>
            <div class="stat-icon"><i class="fas fa-chart-bar"></i></div>
          </div>
          <div class="stat-value">${requests.length}</div>
          <div class="stat-description">All mentorship requests</div>
          <a href="requests.html" class="stat-action">
            View All <i class="fas fa-arrow-right"></i>
          </a>
        </div>`;
    }
    
    // Add fade-in animation
    stats.classList.add('fade-in');
    
  } catch (error) {
    console.error('Error loading dashboard data:', error);
    stats.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-exclamation-triangle"></i>
        <h3>Error Loading Dashboard</h3>
        <p>Unable to load dashboard data. Please try refreshing the page.</p>
      </div>`;
  }
}