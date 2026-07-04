/* EduTaskBoard – Notification bell + dark mode (Phase 3) */

(function () {
  function refreshUnreadCount() {
    fetch('/notifications/unread-count').then(r => r.ok ? r.json() : null).then(data => {
      if (!data) return;
      const badge = document.getElementById('notifBadge');
      if (!badge) return;
      if (data.count > 0) {
        badge.style.display = 'inline-block';
        badge.textContent = data.count > 99 ? '99+' : data.count;
      } else {
        badge.style.display = 'none';
      }
    }).catch(() => {});
  }

  window.toggleNotifPanel = function () {
    const panel = document.getElementById('notifPanel');
    if (!panel) return;
    const opening = !panel.classList.contains('open');
    panel.classList.toggle('open');
    if (opening) loadNotifications();
  };

  function timeAgo(iso) {
    if (!iso) return '';
    const diffMs = Date.now() - new Date(iso).getTime();
    const mins = Math.floor(diffMs / 60000);
    if (mins < 1) return 'just now';
    if (mins < 60) return mins + 'm ago';
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return hrs + 'h ago';
    return Math.floor(hrs / 24) + 'd ago';
  }

  function loadNotifications() {
    const panel = document.getElementById('notifPanel');
    if (!panel) return;
    fetch('/notifications').then(r => r.json()).then(list => {
      if (!list.length) {
        panel.innerHTML = '<div class="notif-empty">No notifications</div>';
        return;
      }
      panel.innerHTML = '<div class="notif-head"><span>Notifications</span><button onclick="markAllNotifRead()">Mark all read</button></div>' +
        list.slice(0, 20).map(n => `
          <div class="notif-item ${n.read ? '' : 'unread'}" onclick="openNotif(${n.id}, ${n.link ? "'" + n.link + "'" : 'null'})">
            <div class="notif-title">${escapeHtml(n.title)}</div>
            <div class="notif-msg">${escapeHtml(n.message || '')}</div>
            <div class="notif-time">${timeAgo(n.createdAt)}</div>
          </div>`).join('');
    }).catch(() => {});
  }

  window.openNotif = function (id, link) {
    fetch('/notifications/' + id + '/read', { method: 'POST' }).finally(() => {
      refreshUnreadCount();
      if (link) window.location.href = link;
    });
  };

  window.markAllNotifRead = function (e) {
    if (e) e.stopPropagation();
    fetch('/notifications/read-all', { method: 'POST' }).then(() => { refreshUnreadCount(); loadNotifications(); });
  };

  function escapeHtml(str) {
    return (str || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  document.addEventListener('click', function (e) {
    const panel = document.getElementById('notifPanel');
    const btn = document.getElementById('notifBellBtn');
    if (!panel || !panel.classList.contains('open')) return;
    if (panel.contains(e.target) || (btn && btn.contains(e.target))) return;
    panel.classList.remove('open');
  });

  refreshUnreadCount();
  setInterval(refreshUnreadCount, 20000);

  /* ── Dark mode ── */
  window.toggleDarkMode = function () {
    document.documentElement.classList.toggle('dark');
    localStorage.setItem('etb-dark', document.documentElement.classList.contains('dark') ? '1' : '0');
  };
  if (localStorage.getItem('etb-dark') === '1') {
    document.documentElement.classList.add('dark');
  }
})();
