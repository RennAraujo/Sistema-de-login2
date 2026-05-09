(function () {
    const grid = document.getElementById('grid');
    const actorInput = document.getElementById('actorFilter');
    const actionInput = document.getElementById('actionFilter');
    const reload = document.getElementById('reload');
    const token = localStorage.getItem('iamToken') || '';

    async function load() {
        const params = new URLSearchParams({ size: '50' });
        if (actorInput.value)  params.append('actor',  actorInput.value);
        if (actionInput.value) params.append('action', actionInput.value);
        try {
            const res = await fetch(`/api/audit/events?${params}`, {
                headers: token ? { Authorization: `Bearer ${token}` } : {},
            });
            if (!res.ok) {
                grid.innerHTML = `<div class="empty-state">Error ${res.status} — login as ROLE_AUDITOR or ROLE_ADMIN.</div>`;
                return;
            }
            const page = await res.json();
            if (!page.content || !page.content.length) {
                grid.innerHTML = '<div class="empty-state">No matching audit events.</div>';
                return;
            }
            grid.innerHTML = `
              <table class="data-table">
                <thead>
                  <tr><th>Timestamp</th><th>Actor</th><th>Action</th><th>Outcome</th><th>Resource</th><th>Correlation</th></tr>
                </thead>
                <tbody>
                  ${page.content.map(e => `
                    <tr>
                      <td>${new Date(e.timestamp).toLocaleString()}</td>
                      <td>${e.actor || ''}</td>
                      <td><code>${e.action}</code></td>
                      <td><span class="badge badge-${(e.outcome||'').toLowerCase()}">${e.outcome}</span></td>
                      <td>${e.resourceType || ''} ${e.resourceId ? '#' + e.resourceId : ''}</td>
                      <td><small>${e.correlationId ? e.correlationId.substring(0, 8) : ''}</small></td>
                    </tr>`).join('')}
                </tbody>
              </table>`;
        } catch (e) {
            grid.innerHTML = `<div class="empty-state">Network error: ${e.message}</div>`;
        }
    }

    reload.addEventListener('click', load);
    load();
})();
