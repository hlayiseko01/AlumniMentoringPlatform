document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('registerForm');
  const notice = document.getElementById('notice');
  const role = document.getElementById('role');
  const studentFields = document.getElementById('studentFields');
  const alumniFields = document.getElementById('alumniFields');

  role.addEventListener('change', () => {
    const v = role.value;
    studentFields.style.display = v === 'STUDENT' ? 'grid' : 'none';
    alumniFields.style.display = v === 'ALUMNI' ? 'grid' : 'none';
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    notice.innerHTML = '';

    const payload = {
      fullName: document.getElementById('fullName').value.trim(),
      email: document.getElementById('email').value.trim(),
      password: document.getElementById('password').value,
      role: role.value
    };

    if (payload.role === 'STUDENT'){
      payload.enrollmentYear = parseInt(document.getElementById('enrollmentYear').value||'0')||null;
      payload.major = document.getElementById('major').value||null;
    } else if (payload.role === 'ALUMNI'){
      payload.graduationYear = parseInt(document.getElementById('graduationYear').value||'0')||null;
      payload.company = document.getElementById('company').value||null;
      payload.position = document.getElementById('position').value||null;
      payload.bio = document.getElementById('bio').value||null;
      const skillsStr = document.getElementById('skills').value||'';
      payload.skills = skillsStr ? skillsStr.split(',').map(s=>s.trim()).filter(Boolean) : [];
      payload.linkedin = document.getElementById('linkedin').value||null;
    }

    try {
      const resp = await API.register(payload);
      if (resp.ok){
        notice.innerHTML = '<div class="notice success">Registered successfully. Please login.</div>';
        setTimeout(()=> window.location.href='login.html', 600);
      } else {
        const txt = await resp.text();
        notice.innerHTML = '<div class="notice error">Failed: '+(txt||'Invalid data')+'</div>';
      }
    } catch(err){
      notice.innerHTML = '<div class="notice error">Network error</div>';
    }
  });
});


