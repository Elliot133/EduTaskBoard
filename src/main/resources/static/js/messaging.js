/* EduTaskBoard – Messaging Center with WhatsApp-style contact list + Mobile toggle */
(function () {
  const meId = parseInt(document.querySelector('meta[name="_me_id"]').content, 10);
  let stompClient = null;
  let contactsCache = [];
  let currentConversationId = null;
  let currentType = null;
  let currentPeerId = null;

  // Store last message + unread count per conversation
  const convState = {}; // { "u-5": {lastMsg: "hi", unread: 2, lastMsgTime: 123}, "general": {...} }

  function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, () => {
      stompClient.subscribe('/topic/general', frame => onIncoming(JSON.parse(frame.body)));
      stompClient.subscribe('/user/queue/private', frame => onIncoming(JSON.parse(frame.body)));
      loadSidebar();
    });
  }

  function loadSidebar() {
    fetch('/messages/contacts').then(r => r.json()).then(contacts => {
      contactsCache = contacts;
      // Init state
      contacts.forEach(c => {
        if (!convState[c.conversationId]) {
          convState[c.conversationId] = {
            lastMsg: c.type === 'GENERAL'? 'Everyone' : (c.role || 'Chat'),
            unread: 0,
            lastMsgTime: 0
          };
        }
      });
      renderSidebar(contactsCache);
      maybeAutoOpenFromUrl();
    });
  }

  function renderSidebar(list) {
    const el = document.getElementById('contactsContainer');
    if (!list.length) { el.innerHTML = '<div class="notif-empty">No contacts available yet.</div>'; return; }

    // Sort by last activity: most recent on top
    list.sort((a, b) => {
      const ta = convState[a.conversationId]?.lastMsgTime || 0;
      const tb = convState[b.conversationId]?.lastMsgTime || 0;
      return tb - ta;
    });

    el.innerHTML = list.map(c => {
      const state = convState[c.conversationId] || { lastMsg: '', unread: 0 };
      const unreadBadge = state.unread > 0? `<div class="mc-badge">${state.unread}</div>` : '';
      const unreadClass = state.unread > 0? 'unread' : '';
      const preview = escapeHtml(state.lastMsg).slice(0, 35);

      return `
      <div class="mc-contact ${unreadClass}" data-cid="${c.conversationId}"
        onclick="ETBChat.open('${c.conversationId}','${c.type}',${c.userId?? 'null'},'${escapeAttr(c.name)}','${c.role?? ''}')">
        <div class="mc-avatar">${initials(c.name)}</div>
        <div class="mc-info">
          <div class="mc-name">${escapeHtml(c.name)}</div>
          <div class="mc-sub">${preview}</div>
        </div>
        ${unreadBadge}
      </div>`;
    }).join('');
  }

  function moveToTop(conversationId) {
    const el = document.querySelector(`.mc-contact[data-cid="${conversationId}"]`);
    const container = document.getElementById('contactsContainer');
    if (el && container.firstChild!== el) {
      container.prepend(el);
    }
  }

  function filterContacts(term) {
    const q = (term || '').trim().toLowerCase();
    if (!q) { renderSidebar(contactsCache); return; }
    renderSidebar(contactsCache.filter(c => c.name.toLowerCase().includes(q)));
  }

  function initials(name) {
    return (name || '?').split(' ').filter(Boolean).map(p => p[0]).slice(0, 2).join('').toUpperCase();
  }
  function escapeHtml(str) { return (str || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }
  function escapeAttr(str) { return escapeHtml(str).replace(/'/g, '&#39;'); }

  // MOBILE TOGGLE: show contacts or chat
  function toggleMobileList(showContacts) {
    if (window.innerWidth > 860) return; // desktop ignores this
    const center = document.querySelector('.msg-center');
    if (!center) return;

    if (showContacts) {
      center.classList.remove('chat-open');
    } else {
      center.classList.add('chat-open');
    }
  }

  function open(conversationId, type, peerId, name, role) {
    currentConversationId = conversationId;
    currentType = type;
    currentPeerId = peerId;

    document.getElementById('activeName').textContent = name;
    document.getElementById('activeAvatar').textContent = initials(name);
    document.getElementById('activeStatus').textContent = type === 'GENERAL'? 'Everyone in the school' : role;
    document.getElementById('msgInput').disabled = false;
    document.getElementById('sendBtn').disabled = false;
    document.querySelectorAll('.mc-contact').forEach(elm => elm.classList.toggle('active', elm.dataset.cid === conversationId));

    // MOBILE ONLY: hide contacts, show chat when opening
    if (window.innerWidth <= 860) {
      document.querySelector('.msg-center').classList.add('chat-open');
    }

    // Clear unread when opening
    if (convState[conversationId]) {
      convState[conversationId].unread = 0;
      renderSidebar(contactsCache);
    }

    loadHistory();
  }

  function loadHistory() {
    const body = document.getElementById('mcBody');
    body.innerHTML = '<div class="empty-st">Loading…</div>';
    const url = currentType === 'GENERAL'? '/messages/history/general' : '/messages/history/private/' + currentPeerId;
    fetch(url).then(r => r.ok? r.json() : []).then(list => {
      body.innerHTML = '';
      if (!list.length) { body.innerHTML = '<div class="empty-st">No messages yet — say hello!</div>'; return; }
      list.forEach(renderBubble);
      body.scrollTop = body.scrollHeight;
    });
  }

  function renderBubble(m) {
    const body = document.getElementById('mcBody');
    const mine = m.senderId === meId;
    const div = document.createElement('div');
    div.className = 'mc-bubble' + (mine? ' mine' : '');
    const time = new Date(m.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const senderLabel = (!mine && currentType === 'GENERAL')? `<div class="bubble-sender">${escapeHtml(m.senderName)}</div>` : '';
    div.innerHTML = `
      ${senderLabel}
      <div class="bubble-text">${escapeHtml(m.content)}</div>
      <div class="bubble-meta"><span>${time}</span></div>`;
    body.appendChild(div);
  }

  function belongsToOpenConversation(m) {
    if (currentType === null) return false;
    if (m.type === 'GENERAL') return currentType === 'GENERAL';
    if (m.type!== 'PRIVATE') return false;
    const otherParty = m.senderId === meId? m.receiverId : m.senderId;
    return currentType === 'PRIVATE' && otherParty === currentPeerId;
  }

  function onIncoming(m) {
    const convId = m.type === 'GENERAL'? 'general' : (m.senderId === meId? `u-${m.receiverId}` : `u-${m.senderId}`);

    // Update last message + time
    convState[convId] = convState[convId] || { lastMsg: '', unread: 0, lastMsgTime: 0 };
    convState[convId].lastMsg = m.content;
    convState[convId].lastMsgTime = Date.now();

    // Add unread if not current chat
    if (!belongsToOpenConversation(m)) {
      convState[convId].unread += 1;
    }

    // Re-render sidebar to show preview + badge + move to top
    renderSidebar(contactsCache);
    moveToTop(convId);

    // Render in chat if open
    if (belongsToOpenConversation(m)) {
      const body = document.getElementById('mcBody');
      const empty = body.querySelector('.empty-st');
      if (empty) body.innerHTML = '';
      renderBubble(m);
      body.scrollTop = body.scrollHeight;
    }
  }

  function sendMessage() {
    const input = document.getElementById('msgInput');
    const content = input.value.trim();
    if (!content ||!currentConversationId ||!stompClient) return;
    const payload = currentType === 'GENERAL'
   ? { type: 'GENERAL', content: content }
      : { type: 'PRIVATE', receiverId: currentPeerId, content: content };

    stompClient.send('/app/chat.send', {}, JSON.stringify(payload));

    // Update own last message instantly
    convState[currentConversationId].lastMsg = content;
    convState[currentConversationId].lastMsgTime = Date.now();
    renderSidebar(contactsCache);
    moveToTop(currentConversationId);

    input.value = '';
  }

  function handleTyping(e) {
    if (e.key === 'Enter') sendMessage();
  }

  function maybeAutoOpenFromUrl() {
    const params = new URLSearchParams(window.location.search);
    const peer = params.get('peer');
    if (!peer) return;
    const contact = contactsCache.find(c => c.type === 'PRIVATE' && String(c.userId) === peer);
    if (contact) open(contact.conversationId, contact.type, contact.userId, contact.name, contact.role);
  }

  // Expose to global
  window.toggleMobileList = toggleMobileList;
  window.ETBChat = { open, filterContacts };
  window.sendMessage = sendMessage;
  window.handleTyping = handleTyping;

  // On resize, reset mobile view
  window.addEventListener('resize', () => {
    if (window.innerWidth > 860) {
      document.querySelector('.msg-center')?.classList.remove('chat-open');
    }
  });

  connect();
})();