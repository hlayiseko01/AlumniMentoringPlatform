document.addEventListener('DOMContentLoaded', async () => {
  if (!Auth.requireAuth('login.html')) return;
  
  let currentUser = null;
  
  try {
    // Load current user data from API
    currentUser = await API.getCurrentUser();
    console.log('Loaded user profile:', currentUser);
    
    // Populate common fields
    document.getElementById('email').value = currentUser.email || '';
    document.getElementById('fullName').value = currentUser.fullName || '';
    
    // Show role-specific fields
    if (currentUser.role === 'STUDENT') {
      document.getElementById('studentFields').style.display = 'block';
      document.getElementById('enrollmentYear').value = currentUser.enrollmentYear || '';
      document.getElementById('major').value = currentUser.major || '';
    } else if (currentUser.role === 'ALUMNI') {
      document.getElementById('alumniFields').style.display = 'block';
      document.getElementById('graduationYear').value = currentUser.graduationYear || '';
      document.getElementById('company').value = currentUser.company || '';
      document.getElementById('position').value = currentUser.position || '';
      document.getElementById('bio').value = currentUser.bio || '';
      document.getElementById('linkedin').value = currentUser.linkedin || '';
      document.getElementById('availableForMentoring').checked = currentUser.availableForMentoring !== false;
      document.getElementById('skills').value = (currentUser.skills || []).join(', ');
    }
    
  } catch (error) {
    console.error('Error loading user profile:', error);
    showError('Failed to load profile data');
  }
  
  // Save button event listener
  document.getElementById('saveBtn').addEventListener('click', async () => {
    await saveProfile();
  });
  
  // Cancel button event listener
  document.getElementById('cancelBtn').addEventListener('click', () => {
    location.reload(); // Reload to reset form
  });
  
  async function saveProfile() {
    try {
      hideMessages();
      
      // Validate required fields
      const fullName = document.getElementById('fullName').value.trim();
      if (!fullName) {
        showError('Full name is required');
        return;
      }
      
      // Prepare user data based on role
      let userData = {
        id: currentUser.id,
        email: currentUser.email,
        fullName: fullName,
        role: currentUser.role,
        password: currentUser.password // Keep existing password
      };
      
      if (currentUser.role === 'STUDENT') {
        userData.enrollmentYear = parseInt(document.getElementById('enrollmentYear').value) || null;
        userData.major = document.getElementById('major').value.trim();
      } else if (currentUser.role === 'ALUMNI') {
        userData.graduationYear = parseInt(document.getElementById('graduationYear').value) || null;
        userData.company = document.getElementById('company').value.trim();
        userData.position = document.getElementById('position').value.trim();
        userData.bio = document.getElementById('bio').value.trim();
        userData.linkedin = document.getElementById('linkedin').value.trim();
        userData.availableForMentoring = document.getElementById('availableForMentoring').checked;
        
        // Parse skills from comma-separated string
        const skillsText = document.getElementById('skills').value.trim();
        userData.skills = skillsText ? skillsText.split(',').map(s => s.trim()).filter(s => s.length > 0) : [];
      }
      
      console.log('Saving profile data:', userData);
      
      // Call API to update profile
      const updatedUser = await API.updateProfile(userData);
      console.log('Profile updated successfully:', updatedUser);
      
      // Update current user data
      currentUser = updatedUser;
      
      showSuccess('Profile updated successfully!');
      
    } catch (error) {
      console.error('Error saving profile:', error);
      showError('Failed to update profile. Please try again.');
    }
  }
  
  function showSuccess(message) {
    document.getElementById('successMessage').textContent = message;
    document.getElementById('successMessage').style.display = 'block';
    document.getElementById('errorMessage').style.display = 'none';
  }
  
  function showError(message) {
    document.getElementById('errorMessage').textContent = message;
    document.getElementById('errorMessage').style.display = 'block';
    document.getElementById('successMessage').style.display = 'none';
  }
  
  function hideMessages() {
    document.getElementById('successMessage').style.display = 'none';
    document.getElementById('errorMessage').style.display = 'none';
  }
});


