(function () {
    const grid = document.getElementById('grid');
    const filterInput = document.getElementById('filter');
    const stateFilter = document.getElementById('stateFilter');
    const token = localStorage.getItem('iamToken') || '';
    let users = [];

    async function load() {
        try {
            const res = await fetch('/api/identity/users', {
                headers: token ? { Authorization: `Bearer ${token}` } : {},
            });
            if (res.status === 401 || res.status === 403) {
                grid.innerHTML = '<div class="empty-state">Login as an admin first (token in localStorage.iamToken).</div>';
                return;
            }
            if (!res.ok) {
                grid.innerHTML = `<div class="empty-state">Error: ${res.statusText}</div>`;
                return;
            }
            users = await res.json();
            render();
        } catch (e) {
            grid.innerHTML = `<div class="empty-state">Network error: ${e.message}</div>`;
        }
    }

    function render() {
        const fName = filterInput.value.toLowerCase();
        const fState = stateFilter.value;
        const visible = users.filter(u => {
            if (fName && !u.username.toLowerCase().includes(fName)) return false;
            if (fState && u.lifecycleState !== fState) return false;
            return true;
        });
        if (!visible.length) {
            grid.innerHTML = '<div class="empty-state">No users match the filter.</div>';
            return;
        }
        grid.innerHTML = `
            <table class="data-table">
              <thead>
                <tr><th>ID</th><th>Username</th><th>Email</th><th>State</th><th>2FA</th><th>Actions</th></tr>
              </thead>
              <tbody>
                ${visible.map(u => `
                  <tr>
                    <td>${u.id}</td>
                    <td><strong>${u.username}</strong></td>
                    <td>${u.email}</td>
                    <td><span class="badge badge-${(u.lifecycleState||'').toLowerCase()}">${u.lifecycleState}</span></td>
                    <td>${u.twoFactorEnabled ? 'on' : '—'}</td>
                    <td><div class="row-actions">
                      <button data-id="${u.id}" data-op="approve">Approve</button>
                      <button data-id="${u.id}" data-op="suspend">Suspend</button>
                      <button data-id="${u.id}" data-op="reactivate">Reactivate</button>
                      <button data-id="${u.id}" data-op="offboard">Offboard</button>
                    </div></td>
                  </tr>
                `).join('')}
              </tbody>
            </table>`;
        grid.querySelectorAll('button[data-op]').forEach(btn => {
            btn.addEventListener('click', () => transition(btn.dataset.id, btn.dataset.op));
        });
    }

    async function transition(id, op) {
        const res = await fetch(`/api/identity/users/${id}/${op}`, {
            method: 'POST',
            headers: token ? { Authorization: `Bearer ${token}` } : {},
        });
        if (!res.ok) {
            alert(`Transition failed: ${res.status} ${res.statusText}`);
            return;
        }
        await load();
    }

    filterInput.addEventListener('input', render);
    stateFilter.addEventListener('change', render);
    load();
})();
