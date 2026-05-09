(function () {
    const form = document.getElementById('assistantForm');
    const input = document.getElementById('question');
    const chat = document.getElementById('chat');
    const usage = document.getElementById('usage');

    const token = localStorage.getItem('iamToken') || '';

    function bubble(text, role, sources) {
        const div = document.createElement('div');
        div.className = `bubble ${role}`;
        div.textContent = text;
        if (sources && sources.length) {
            const ul = document.createElement('ul');
            ul.className = 'sources';
            sources.forEach(s => {
                const li = document.createElement('li');
                li.textContent = `${s.sourcePath} #${s.chunkIndex} (rank ${s.rank.toFixed(3)})`;
                ul.appendChild(li);
            });
            div.appendChild(ul);
        }
        chat.appendChild(div);
        chat.scrollTop = chat.scrollHeight;
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const q = input.value.trim();
        if (!q) return;
        bubble(q, 'user');
        input.value = '';
        usage.textContent = 'Thinking…';

        try {
            const res = await fetch('/api/ai/assistant', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(token ? { Authorization: `Bearer ${token}` } : {}),
                },
                body: JSON.stringify({ question: q }),
            });
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                bubble(`Error: ${err.detail || res.statusText}`, 'error');
                usage.textContent = '';
                return;
            }
            const payload = await res.json();
            bubble(payload.answer, 'assistant', payload.sources);
            if (payload.usage) {
                usage.textContent =
                    `model=${payload.model} | in=${payload.usage.inputTokens} out=${payload.usage.outputTokens}` +
                    (payload.usage.cacheReadInputTokens ? ` (cache hit ${payload.usage.cacheReadInputTokens})` : '');
            }
        } catch (e) {
            bubble(`Network error: ${e.message}`, 'error');
            usage.textContent = '';
        }
    });
})();
