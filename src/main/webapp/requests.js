document.addEventListener('DOMContentLoaded', async () => {
  // Check if user is authenticated by calling current-user endpoint
  let user;
  try {
    user = await API.getCurrentUser();
  } catch (error) {
    window.location.href = 'login.html';
    return;
  }

  const list = document.getElementById('list');
  const status = document.getElementById('status');
  const userRole = user.role;
  
  console.log('User role:', userRole);
  console.log('User email:', user.email);

  let data = [];
  async function load(){
    try {
      data = await API.getRequests(status.value||undefined);
      console.log('Loaded requests:', data);
      console.log('Number of requests:', data.length);
      render(data);
    } catch(e){
      console.error('Error loading requests:', e);
      list.innerHTML = '<div class="notice error">Failed to load requests</div>';
    }
  }

  function render(items){
    console.log('Rendering requests with data:', items);
    list.innerHTML = items.map(r => {
      console.log('Processing request:', r);
      console.log('Student full name:', r.studentFullName);
      console.log('Alumni full name:', r.alumniFullName);
      console.log('All request fields:', Object.keys(r));
      console.log('All request values:', r);
      
      // Only show action buttons based on user role
      let actionButtons = '';
      
      if (userRole === 'ALUMNI' || userRole === 'ADMIN') {
        // Alumni and admins can approve/reject pending requests
        if (r.status === 'PENDING') {
          actionButtons = `<button class="btn btn-primary" data-acc="${r.id}">Accept</button><button class="btn btn-danger" data-rej="${r.id}">Reject</button>`;
        } else if (r.status === 'ACCEPTED') {
          actionButtons = `<a class="btn btn-secondary" href="chat.html">Open Chat</a>`;
        }
      } else if (userRole === 'STUDENT') {
        // Students can only view their requests and open chat if accepted
        if (r.status === 'ACCEPTED') {
          actionButtons = `<a class="btn btn-secondary" href="chat.html">Open Chat</a>`;
        } else if (r.status === 'PENDING') {
          actionButtons = `<span class="text-muted">Waiting for response</span>`;
        }
      }

      // Determine what to display based on user role
      let requestTitle = '';
      let requestSubtitle = '';
      
      if (userRole === 'STUDENT') {
        // Students see the alumni they're requesting from
        const alumniName = r.alumniFullName || r.alumniName || 'Unknown Alumni';
        const alumniCompany = r.alumniCompany || 'alumni';
        const alumniPosition = r.alumniPosition || '';
        requestTitle = `Request to ${alumniName}`;
        requestSubtitle = `Mentorship request to ${alumniCompany}${alumniPosition ? ' • ' + alumniPosition : ''} alumni`;
      } else if (userRole === 'ALUMNI' || userRole === 'ADMIN') {
        // Alumni see the student who is requesting
        let studentName = r.studentFullName || r.studentName;
        console.log('Initial studentName:', studentName);
        console.log('studentName type:', typeof studentName);
        console.log('studentName length:', studentName ? studentName.length : 'null/undefined');
        
        // If no name is available, create a descriptive name using available info
        if (!studentName || studentName.trim() === '') {
          console.log('Using fallback name logic');
          // Try to extract name from email (before @)
          const email = r.studentEmail || '';
          if (email) {
            const emailPrefix = email.split('@')[0];
            studentName = emailPrefix.charAt(0).toUpperCase() + emailPrefix.slice(1);
          } else {
            // Create a descriptive name using available info
            const major = r.studentMajor || '';
            const year = r.studentEnrollmentYear || '';
            const id = r.studentId || '';
            
            if (major && year) {
              studentName = `${major} Student (${year})`;
            } else if (major) {
              studentName = `${major} Student`;
            } else if (year) {
              studentName = `Student (${year})`;
            } else if (id) {
              studentName = `Student #${id}`;
            } else {
              studentName = 'Student';
            }
          }
        }
        
        const studentMajor = r.studentMajor || '';
        const studentYear = r.studentEnrollmentYear || '';
        requestTitle = `Request from ${studentName}`;
        
        // Build subtitle without repeating the name
        let subtitleParts = [];
        if (studentMajor) subtitleParts.push(studentMajor);
        if (studentYear) subtitleParts.push(`Year ${studentYear}`);
        
        requestSubtitle = subtitleParts.length > 0 
          ? `Student: ${subtitleParts.join(' • ')}`
          : 'Wants mentorship';
      } else {
        // Fallback
        requestTitle = 'Mentorship Request';
        requestSubtitle = r.message || '';
      }

      return `
        <div class="req">
          <div class="head">
            <div>
              <div class="h2">${requestTitle}</div>
              <div class="p">${requestSubtitle}</div>
              ${r.message ? `<div class="p" style="margin-top: 0.5rem; font-style: italic;">"${r.message}"</div>` : ''}
            </div>
            <span class="badge ${r.status}">${r.status}</span>
          </div>
          <div class="actions">
            ${actionButtons}
          </div>
        </div>`;
    }).join('');

    // Only add event listeners for buttons that exist
    list.querySelectorAll('[data-acc]').forEach(b=> b.addEventListener('click', ()=>update(b.getAttribute('data-acc'),'ACCEPTED')));
    list.querySelectorAll('[data-rej]').forEach(b=> b.addEventListener('click', ()=>update(b.getAttribute('data-rej'),'REJECTED')));
  }

  async function update(id, newStatus){
    try {
      const resp = await API.updateRequest(id, newStatus);
      if (resp.ok){ await load(); } else { alert('Failed to update'); }
    } catch(e){ alert('Network error'); }
  }

  status.addEventListener('change', load);
  load();
});


