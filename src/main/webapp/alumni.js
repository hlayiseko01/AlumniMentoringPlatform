document.addEventListener('DOMContentLoaded', async () => {
  if (!Auth.requireAuth('login.html')) return;
  const list = document.getElementById('list');
  const search = document.getElementById('search');
  const availability = document.getElementById('availability');

  let data = [];
  
  // Show loading state
  list.innerHTML = `
    <div class="loading-state">
      <i class="fas fa-spinner fa-spin"></i>
      <span>Loading mentors...</span>
    </div>
  `;
  
  try {
    data = await API.getAlumni();
    render(data);
  } catch(e){
    console.error('Error loading alumni:', e);
    list.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-exclamation-triangle"></i>
        <h3>Failed to Load Mentors</h3>
        <p>Unable to load mentor information. Please try refreshing the page.</p>
      </div>
    `;
  }

  function render(items){
    const term = search.value.toLowerCase();
    const avail = availability.value;
    const filtered = items.filter(a => {
      const matches = !term || (a.fullName?.toLowerCase().includes(term) || a.company?.toLowerCase().includes(term) || (a.skills||[]).some(s=>s.toLowerCase().includes(term)));
      const availMatch = !avail || (avail==='available'? a.availableForMentoring : !a.availableForMentoring);
      return matches && availMatch;
    });
    
    if (filtered.length === 0) {
      list.innerHTML = `
        <div class="empty-state">
          <i class="fas fa-search"></i>
          <h3>No Mentors Found</h3>
          <p>Try adjusting your search criteria or check back later for new mentors.</p>
        </div>
      `;
      return;
    }
    
    list.innerHTML = filtered.map(a => `
      <div class="alumni-card">
        <div class="alumni-header">
          <div class="alumni-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="alumni-info">
            <h3>${a.fullName}</h3>
            <div class="alumni-company">${a.company||'No company listed'}</div>
            <div class="alumni-position">${a.position||'No position listed'}</div>
          </div>
        </div>
        <div class="alumni-bio">${a.bio||'No bio available'}</div>
        <div class="skills">${(a.skills||[]).map(s=>`<span class="skill">${s}</span>`).join('')}</div>
        <div class="actions">
          <button class="btn-request" data-req="${a.id}">
            <i class="fas fa-handshake"></i>
            Request Mentorship
          </button>
          ${a.linkedin? `<a class="btn-request" target="_blank" href="${a.linkedin}" style="background: #0077b5;">
            <i class="fab fa-linkedin"></i>
            LinkedIn
          </a>`:''}
        </div>
      </div>`).join('');

    list.querySelectorAll('[data-req]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const alumniId = parseInt(btn.getAttribute('data-req'));
        const msg = prompt('Message to mentor:');
        if (!msg) return;
        try {
          const resp = await API.createRequest({ alumni: { id: alumniId }, message: msg });
          if (resp.ok){
            alert('Request sent');
          } else {
            alert('Failed to send');
          }
        } catch(e){ alert('Network error'); }
      });
    });
  }

  search.addEventListener('input', ()=>render(data));
  availability.addEventListener('change', ()=>render(data));
});


