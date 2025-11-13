/**
 * Alumni Mentoring Platform - Real-time Chat Application
 * 
 * Handles WebSocket-based real-time messaging between students and alumni.
 * Provides chat room management, message sending/receiving, and UI updates.
 * 
 * @author Alumni Mentoring Platform
 * @version 1.0
 */
class ChatApp {
    constructor() {
        this.websocket = null;
        this.currentChatRoom = null;
        this.userId = null;
        this.chatRooms = [];
        this.init();
    }

    async init() {
        this.setupEventListeners();
        await this.loadUserInfo();
        await this.loadChatRooms();
        this.renderChatRooms();
    }

    setupEventListeners() {
        // Message input
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');

        if (messageInput) {
            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
            });
        }

        if (sendBtn) {
            sendBtn.addEventListener('click', () => this.sendMessage());
        }

        // Chat room selection
        document.addEventListener('click', (e) => {
            if (e.target.closest('.chat-room-item')) {
                const roomId = e.target.closest('.chat-room-item').dataset.roomId;
                this.selectChatRoom(parseInt(roomId));
            }
        });
    }

    async loadUserInfo() {
        try {
            // Get current user info from API
            const user = await API.getCurrentUser();
            this.userId = user.id;
            this.currentUser = user;
            console.log('User loaded for chat:', user);
            console.log('User role:', user.role);
            console.log('User email:', user.email);
            
            // Update user info in sidebar
            const userNameElement = document.getElementById('currentUserName');
            if (userNameElement) {
                userNameElement.textContent = user.fullName || user.email;
            }
        } catch (error) {
            console.error('Error loading user info:', error);
            alert('Failed to load user information');
        }
    }

    async loadChatRooms() {
        try {
            const rooms = await API.getChatRooms();
            console.log('Loaded chat rooms:', rooms);
            console.log('Number of chat rooms:', rooms.length);
            // Chat rooms loaded successfully
            this.chatRooms = rooms;
            if (rooms.length === 0) {
                console.warn('No chat rooms found for this user. Chat rooms are created when mentor requests are accepted.');
            }
            
            // Update unread count in navigation
            this.updateUnreadCount();
        } catch (error) {
            console.error('Error loading chat rooms:', error);
            console.error('Error details:', error.message);
            this.chatRooms = [];
        }
    }

    renderChatRooms() {
        const roomsList = document.getElementById('chatRoomsList');
        if (!roomsList) {
            console.error('Chat rooms list element not found!');
            return;
        }

        if (this.chatRooms.length === 0) {
            roomsList.innerHTML = `
                <div class="loading-state">
                    <i class="fas fa-comments"></i>
                    <span>No chat rooms available. Accept a mentorship request to start chatting!</span>
                </div>
            `;
            return;
        }

        const html = this.chatRooms.map(room => {
            // Determine who you're chatting with based on your role
            let chatPartnerName = '';
            let chatPartnerRole = '';
            
            if (this.currentUser && this.currentUser.role === 'STUDENT') {
                // If you're a student, show the alumni name
                chatPartnerName = room.alumniName || 'Alumni';
                chatPartnerRole = 'Alumni';
            } else if (this.currentUser && (this.currentUser.role === 'ALUMNI' || this.currentUser.role === 'ADMIN')) {
                // If you're an alumni/admin, show the student name
                chatPartnerName = room.studentName || 'Student';
                chatPartnerRole = 'Student';
            } else {
                // Fallback
                chatPartnerName = room.roomName || 'Chat';
                chatPartnerRole = '';
            }
            
            return `
                <div class="chat-room-item ${room.id === this.currentChatRoom?.id ? 'active' : ''}" 
                     data-room-id="${room.id}">
                    <div class="chat-room-avatar">
                        <i class="fas fa-user"></i>
                    </div>
                    <div class="chat-room-content">
                        <div class="chat-room-name">${chatPartnerName}</div>
                        <div class="chat-room-preview">${chatPartnerRole} • ${room.studentName} & ${room.alumniName}</div>
                    </div>
                    <div class="chat-room-meta">
                        <div class="chat-room-time">${room.lastMessageAt ? new Date(room.lastMessageAt).toLocaleDateString() : 'No messages'}</div>
                        ${room.unreadCount > 0 ? `<span class="unread-badge">${room.unreadCount}</span>` : ''}
                    </div>
                </div>
            `;
        }).join('');
        
        roomsList.innerHTML = html;
    }

    selectChatRoom(roomId) {
        this.currentChatRoom = this.chatRooms.find(room => room.id === roomId);
        if (!this.currentChatRoom) {
            console.error('Chat room not found:', roomId);
            return;
        }

        // Update UI
        document.querySelectorAll('.chat-room-item').forEach(item => {
            item.classList.remove('active');
        });
        document.querySelector(`[data-room-id="${roomId}"]`).classList.add('active');

        // Show chat area
        const noChatSelected = document.getElementById('noChatSelected');
        const chatArea = document.getElementById('chatArea');
        const currentChatTitle = document.getElementById('currentChatTitle');
        
        console.log('Showing chat area for room:', roomId);
        console.log('noChatSelected element:', noChatSelected);
        console.log('chatArea element:', chatArea);
        
        noChatSelected.style.display = 'none';
        chatArea.style.display = 'flex';
        
        // Show the person's name instead of room name
        let chatPartnerName = '';
        if (this.currentUser && this.currentUser.role === 'STUDENT') {
            // If you're a student, show the alumni name
            chatPartnerName = this.currentChatRoom.alumniName || 'Alumni';
        } else if (this.currentUser && (this.currentUser.role === 'ALUMNI' || this.currentUser.role === 'ADMIN')) {
            // If you're an alumni/admin, show the student name
            chatPartnerName = this.currentChatRoom.studentName || 'Student';
        } else {
            // Fallback
            chatPartnerName = this.currentChatRoom.roomName || 'Chat';
        }
        
        currentChatTitle.textContent = chatPartnerName;
        
        // Ensure chat area is visible
        chatArea.style.opacity = '1';
        chatArea.style.visibility = 'visible';
        chatArea.style.display = 'flex';
        chatArea.style.flexDirection = 'column';
        chatArea.style.background = '#ffffff';
        chatArea.style.minHeight = '400px';
        chatArea.style.position = 'relative';
        chatArea.style.zIndex = '1';
        

        // Clear any existing messages before loading new ones
        const messagesContainer = document.getElementById('messagesContainer');
        if (messagesContainer) {
            messagesContainer.innerHTML = '';
        } else {
            console.error('Messages container not found!');
        }

        // Mark messages as read when room is opened
        this.markMessagesAsRead(roomId);
        
        // Connect to WebSocket
        this.connectToChat(roomId);

        // Load messages
        this.loadMessages(roomId);
        
        // Enable message input
        this.enableMessageInput();
    }

    connectToChat(roomId) {
        if (this.websocket) {
            this.websocket.close();
        }

        if (!this.userId) {
            console.error('User ID not available for WebSocket connection');
            this.updateConnectionStatus('disconnected', 'User not authenticated');
            return;
        }

        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/AlumniMentoring/chat/${roomId}/${this.userId}`;
        
        console.log('Connecting to WebSocket:', wsUrl);
        console.log('Room ID:', roomId);
        console.log('User ID:', this.userId);
        
        this.updateConnectionStatus('connecting', 'Connecting...');
        
        this.websocket = new WebSocket(wsUrl);
        
        this.websocket.onopen = () => {
            console.log('WebSocket connected successfully');
            this.updateConnectionStatus('connected', 'Connected');
            this.enableMessageInput();
        };
        
        this.websocket.onmessage = (event) => {
            const data = JSON.parse(event.data);
            this.handleMessage(data);
        };
        
        this.websocket.onclose = (event) => {
            console.log('WebSocket closed:', event);
            this.updateConnectionStatus('disconnected', 'Disconnected');
            // Don't disable input on close - keep it enabled for manual typing
            // this.disableMessageInput();
        };
        
        this.websocket.onerror = (error) => {
            console.error('WebSocket error:', error);
            this.updateConnectionStatus('disconnected', 'Connection Error');
        };
    }

    handleMessage(data) {
        console.log('Handling message:', data);
        const messagesContainer = document.getElementById('messagesContainer');
        console.log('Messages container found:', messagesContainer);
        
        if (!messagesContainer) {
            console.error('Messages container not found!');
            return;
        }
        
        if (data.type === 'system') {
            const systemMessage = document.createElement('div');
            systemMessage.className = 'system-message';
            systemMessage.textContent = data.content;
            messagesContainer.appendChild(systemMessage);
        } else if (data.type === 'message') {
            
            // Check if this is a duplicate of an optimistic message
            if (data.senderId == this.userId && data.id && !data.id.toString().startsWith('temp_')) {
                // This is the real message from server, remove any temporary message with same content
                this.removeTemporaryMessage(data.content);
            }
            
            const messageElement = document.createElement('div');
            messageElement.className = data.senderId == this.userId ? 'message sent' : 'message received';
            messageElement.setAttribute('data-message-id', data.id || 'temp_' + Date.now());
            
            messageElement.innerHTML = `
                <div class="message-content">${this.escapeHtml(data.content)}</div>
                <div class="message-info">
                    ${data.senderName} • ${new Date(data.sentAt).toLocaleTimeString()}
                </div>
            `;
            
            messagesContainer.appendChild(messageElement);
        } else {
            console.warn('Unknown message type:', data.type);
        }
        
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    async loadMessages(roomId) {
        try {
            const messages = await API.getChatMessages(roomId);
            this.renderMessages(messages);
        } catch (error) {
            console.error('Error loading messages:', error);
            // Show error message in chat
            const messagesContainer = document.getElementById('messagesContainer');
            if (messagesContainer) {
                const errorMessage = document.createElement('div');
                errorMessage.className = 'system-message';
                errorMessage.textContent = 'Failed to load messages. Please try again.';
                messagesContainer.appendChild(errorMessage);
            }
        }
    }

    renderMessages(messages) {
        const container = document.getElementById('messagesContainer');
        container.innerHTML = '';
        
        messages.forEach(message => {
            const messageElement = document.createElement('div');
            messageElement.className = `message ${message.senderId == this.userId ? 'sent' : 'received'} fade-in`;
            
            const time = new Date(message.sentAt).toLocaleTimeString();
            
            messageElement.innerHTML = `
                <div class="message-content">
                    <p>${this.escapeHtml(message.content)}</p>
                    <div class="message-time">${time}</div>
                </div>
            `;
            
            container.appendChild(messageElement);
        });
        
        container.scrollTop = container.scrollHeight;
    }

    sendMessage() {
        const input = document.getElementById('messageInput');
        const content = input.value.trim();
        
        if (!content || !this.websocket || this.websocket.readyState !== WebSocket.OPEN) {
            return;
        }
        
        // Clear input immediately for better UX
        input.value = '';
        
        // Create optimistic message for immediate display
        const optimisticMessage = {
            id: 'temp_' + Date.now(), // Temporary ID
            content: content,
            senderId: this.userId,
            senderName: 'You',
            sentAt: new Date().toISOString(),
            type: 'message'
        };
        
        // Display message immediately
        this.handleMessage(optimisticMessage);
        
        // Send via WebSocket
        const message = {
            content: content,
            type: 'TEXT'
        };
        
        this.websocket.send(JSON.stringify(message));
    }

    enableMessageInput() {
        const input = document.getElementById('messageInput');
        const button = document.getElementById('sendBtn');
        
        if (input) {
            input.disabled = false;
            
            // Force visible styling
            input.style.opacity = '1';
            input.style.visibility = 'visible';
            input.style.display = 'block';
            input.style.background = '#ffffff';
            input.style.color = '#1f2937';
            input.style.border = '1px solid #e5e7eb';
            input.style.padding = '12px 16px';
            input.style.width = '100%';
            input.style.height = '48px';
            input.style.fontSize = '16px';
            input.style.borderRadius = '8px';
            input.style.boxSizing = 'border-box';
            input.style.outline = 'none';
            input.style.zIndex = '10';
            input.style.position = 'relative';
            
            // Ensure the container is visible too
            const container = input.closest('.message-input-container');
            if (container) {
                container.style.opacity = '1';
                container.style.visibility = 'visible';
                container.style.display = 'block';
                container.style.background = '#ffffff';
                container.style.padding = '16px 20px';
                container.style.borderTop = '1px solid #e5e7eb';
                container.style.zIndex = '10';
                container.style.position = 'relative';
            }
        }
        if (button) {
            button.disabled = false;
            
            // Style the button too
            button.style.opacity = '1';
            button.style.visibility = 'visible';
            button.style.display = 'flex';
            button.style.background = '#6366f1';
            button.style.color = '#ffffff';
            button.style.border = 'none';
            button.style.borderRadius = '50%';
            button.style.width = '40px';
            button.style.height = '40px';
            button.style.alignItems = 'center';
            button.style.justifyContent = 'center';
            button.style.cursor = 'pointer';
            button.style.zIndex = '10';
            button.style.position = 'relative';
        }
    }

    disableMessageInput() {
        const input = document.getElementById('messageInput');
        const button = document.getElementById('sendBtn');
        
        if (input) {
            input.disabled = true;
        }
        if (button) {
            button.disabled = true;
        }
    }

    updateConnectionStatus(status, text) {
        const statusElement = document.getElementById('connectionStatus');
        const textElement = document.getElementById('statusText');
        
        if (statusElement && textElement) {
            statusElement.className = `connection-status ${status}`;
            textElement.textContent = text;
        }
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    removeTemporaryMessage(content) {
        const messagesContainer = document.getElementById('messagesContainer');
        if (!messagesContainer) return;
        
        // Find and remove temporary messages with the same content
        const messages = messagesContainer.querySelectorAll('[data-message-id^="temp_"]');
        messages.forEach(message => {
            const messageContent = message.querySelector('.message-content');
            if (messageContent && messageContent.textContent === content) {
                message.remove();
            }
        });
    }

    async updateUnreadCount() {
        try {
            const response = await API.getUnreadCount();
            const totalUnread = response.unreadCount || 0;
            
            // Update chat link in navigation
            const chatLink = document.querySelector('a[href="chat.html"]');
            if (chatLink) {
                if (totalUnread > 0) {
                    chatLink.innerHTML = `Chat <span class="unread-badge">${totalUnread}</span>`;
                } else {
                    chatLink.innerHTML = 'Chat';
                }
            }
        } catch (error) {
            console.error('Error updating unread count:', error);
        }
    }

    async markMessagesAsRead(roomId) {
        try {
            // Call API to mark messages as read
            await API.markMessagesAsRead(roomId);
            
            // Update local unread count for this room
            const room = this.chatRooms.find(r => r.id === roomId);
            if (room) {
                room.unreadCount = 0;
                this.renderChatRooms(); // Re-render to update UI
            }
            
            // Update total unread count
            this.updateUnreadCount();
        } catch (error) {
            console.error('Error marking messages as read:', error);
        }
    }
}

// Initialize chat app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.chatApp = new ChatApp();
});
