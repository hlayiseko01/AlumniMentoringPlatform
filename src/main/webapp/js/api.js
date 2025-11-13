/**
 * Alumni Mentoring Platform - API Helper
 * 
 * Centralized API client for making HTTP requests to the backend.
 * Handles session-based authentication and provides convenient methods
 * for all REST endpoints.
 * 
 * @author Alumni Mentoring Platform
 * @version 1.0
 */
(function(global){
  const API_BASE = '/AlumniMentoring/resources';

  async function apiFetch(path, options={}){
    // With session-based authentication, cookies are automatically sent
    const headers = Object.assign({ 'Content-Type':'application/json' }, options.headers||{});
    const fullUrl = API_BASE + path;
    try {
      const resp = await fetch(fullUrl, Object.assign({}, options, { 
        headers,
        credentials: 'same-origin' // Include cookies in requests
      }));
      return resp;
    } catch (error) {
      console.error('Fetch failed:', error);
      throw error;
    }
  }

  global.API = {
    login: async (email, password) => {
      const body = JSON.stringify({ email, password });
      const url = API_BASE + 'resources/auth/login';
      const resp = await apiFetch('/auth/login', { 
        method:'POST', 
        body,
        headers: {
          'Authorization': 'Basic ' + btoa(email + ':' + password)
        }
      });
      return resp;
    },
    register: (user) => apiFetch('/auth/register', { method:'POST', body: JSON.stringify(user) }),
    getAlumni: () => apiFetch('/alumni').then(r=>r.json()),
    getRequests: (status) => apiFetch('/requests' + (status? ('?status='+encodeURIComponent(status)) : '')).then(r=>r.json()),
    createRequest: (payload) => apiFetch('/requests', { method:'POST', body: JSON.stringify(payload) }),
    updateRequest: (id, status) => apiFetch('/requests/'+id+'?status='+encodeURIComponent(status), { method:'PUT' }),
    getChatRooms: () => apiFetch('/chat/rooms').then(r=>r.json()),
    getChatMessages: (roomId) => apiFetch('/chat/rooms/'+roomId+'/messages').then(r=>r.json()),
    getCurrentUser: () => apiFetch('/auth/current-user').then(r=>r.json()),
    updateProfile: (userData) => apiFetch('/auth/profile', { method:'PUT', body: JSON.stringify(userData) }).then(r=>r.json()),
    getUnreadCount: () => apiFetch('/chat/unread-count').then(r=>r.json()),
    markMessagesAsRead: (roomId) => apiFetch('/chat/rooms/'+roomId+'/mark-read', { method:'POST' }),
  };
})(window);


