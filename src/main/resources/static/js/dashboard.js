(function () {
    const metrics = document.getElementById('metrics');
    const lifecycleGrid = document.getElementById('lifecycleGrid');
    const recentAudit = document.getElementById('recentAudit');
    const token = localStorage.getItem('iamToken') || '';
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    function metric(label, value) {
        return `<div class="metric"><div class="label">${label}</div><div class="value">${value}</div></div>`;
    }

    async function loadMetrics() {
        try {
            const [usersRes, healthRes] = await Promise.all([
                fetch('/api/identity/users', { headers }),
                fetch('/actuator/health'),
            ]);
            if (!usersRes.ok) {
                metrics.innerHTML = '<div class="empty-state">Login as ROLE_ADMIN to see metrics.</div>';
                return;
            }
            const users = await usersRes.json();
            const health = await healthRes.json();
            const counts = users.reduce((acc, u) => {
                acc[u.lifecycleState] = (acc[u.lifecycleState] || 0) + 1;
                return acc;
            }, {});
            metrics.innerHTML =
                metric('Total users', users.length) +
                metric('Active', counts.ACTIVE || 0) +
                metric('Pending approval', counts.PENDING_APPROVAL || 0) +
                metric('Suspended', counts.SUSPENDED || 0) +
                metric('App health', health.status || '-');

            lifecycleGrid.innerHTML = `
              <table class="data-table">
                <thead><tr><th>State</th><th>Count</th></tr></thead>
                <tbody>${Object.entries(counts).map(([k, v]) =>
                    `<tr><td><span class="badge badge-${k.toLowerCase()}">${k}</span></td><td>${v}</td></tr>`
                ).join('')}</tbody>
              </table>`;
        } catch (e) {
            metrics.innerHTML = `<div class="empty-state">Network error: ${e.message}</div>`;
        }
    }

    async function loadRecent() {
        try {
            const res = await fetch('/api/audit/events?size=10', { headers });
            if (!res.ok) {
                recentAudit.innerHTML = '<div class="empty-state">Audit log requires ROLE_AUDITOR.</div>';
                return;
            }
            const page = await res.json();
            if (!page.content || !page.content.length) {
                recentAudit.innerHTML = '<div class="empty-state">No audit events yet.</div>';
                return;
            }
            recentAudit.innerHTML = `
              <table class="data-table">
                <thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Outcome</th></tr></thead>
                <tbody>${page.content.map(e =>
                    `<tr>
                       <td>${new Date(e.timestamp).toLocaleTimeString()}</td>
                       <td>${e.actor || ''}</td>
                       <td><code>${e.action}</code></td>
                       <td><span class="badge badge-${(e.outcome||'').toLowerCase()}">${e.outcome}</span></td>
                     </tr>`
                ).join('')}</tbody>
              </table>`;
        } catch (e) {
            recentAudit.innerHTML = `<div class="empty-state">${e.message}</div>`;
        }
    }

    loadMetrics();
    loadRecent();
})();
