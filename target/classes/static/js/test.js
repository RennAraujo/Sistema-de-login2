// Gerenciador da seção de testes
const TestDemo = {
    async loadTestData() {
        await this.loadSystemInfo();
        this.setupTestTabs();
    },

    setupTestTabs() {
        const testTabs = document.querySelectorAll('.test-tab-btn');
        testTabs.forEach(tab => {
            tab.addEventListener('click', async () => {
                const targetTab = tab.getAttribute('data-tab');
                this.switchTestTab(targetTab);
                
                if (targetTab === 'demo') {
                    await this.loadTotpDemo();
                } else if (targetTab === 'stats') {
                    await this.loadSystemStats();
                }
            });
        });
    },

    switchTestTab(tabName) {
        document.querySelectorAll('.test-tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.test-tab-content').forEach(content => content.classList.remove('active'));
        
        const activeBtn = document.querySelector(`[data-tab="${tabName}"].test-tab-btn`);
        const activeContent = document.getElementById(`${tabName}Tab`);
        
        if (activeBtn) activeBtn.classList.add('active');
        if (activeContent) activeContent.classList.add('active');
    },

    async loadSystemInfo() {
        try {
            const response = await fetch('/api/test/demo');
            const data = await response.json();
            
            const infoContainer = document.getElementById('infoTab');
            infoContainer.innerHTML = `
                <div class="system-info">
                    <h3>${data.sistema}</h3>
                    <p>Status: ${data.status}</p>
                    
                    <h4>Tecnologias:</h4>
                    <ul>
                        ${data.tecnologias.map(tech => `<li>${tech}</li>`).join('')}
                    </ul>
                    
                    <h4>Funcionalidades:</h4>
                    <ul>
                        ${data.funcionalidades.map(func => `<li>${func}</li>`).join('')}
                    </ul>
                    
                    <h4>Endpoints:</h4>
                    <ul>
                        ${Object.entries(data.endpoints).map(([name, endpoint]) => 
                            `<li><strong>${name}:</strong> ${endpoint}</li>`
                        ).join('')}
                    </ul>
                </div>
            `;
        } catch (error) {
            document.getElementById('infoTab').innerHTML = '<p>Erro ao carregar informações</p>';
        }
    },

    async loadTotpDemo() {
        try {
            const response = await fetch('/api/test/totp-demo');
            const data = await response.json();
            
            const demoContainer = document.getElementById('demoTab');
            demoContainer.innerHTML = `
                <div class="totp-demo">
                    <h3>Demonstração 2FA</h3>
                    <p>${data.explicacao}</p>
                    
                    <h4>Secret:</h4>
                    <code>${data.secret}</code>
                    
                    <h4>Código Atual:</h4>
                    <div style="font-size: 1.5rem; font-weight: bold; margin: 1rem 0;">
                        ${data.currentCode}
                    </div>
                    
                    <h4>Testar Código:</h4>
                    <input type="text" id="testCode" placeholder="Digite um código">
                    <button onclick="TestDemo.verifyCode('${data.secret}')" class="btn btn-primary">
                        Verificar
                    </button>
                    <div id="verificationResult"></div>
                </div>
            `;
        } catch (error) {
            document.getElementById('demoTab').innerHTML = '<p>Erro ao carregar demo</p>';
        }
    },

    async loadSystemStats() {
        try {
            const response = await fetch('/api/test/stats');
            const data = await response.json();
            
            const statsContainer = document.getElementById('statsTab');
            statsContainer.innerHTML = `
                <div class="system-stats">
                    <h3>Estatísticas do Sistema</h3>
                    <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem; margin: 1rem 0;">
                        <div style="padding: 1rem; background: var(--bg-secondary); border-radius: 8px;">
                            <h4>${data.totalUsuarios || 0}</h4>
                            <p>Total de Usuários</p>
                        </div>
                        <div style="padding: 1rem; background: var(--bg-secondary); border-radius: 8px;">
                            <h4>${data.usuariosAtivos || 0}</h4>
                            <p>Usuários Ativos</p>
                        </div>
                        <div style="padding: 1rem; background: var(--bg-secondary); border-radius: 8px;">
                            <h4>${data.usuariosCom2FA || 0}</h4>
                            <p>Usuários com 2FA</p>
                        </div>
                        <div style="padding: 1rem; background: var(--bg-secondary); border-radius: 8px;">
                            <h4>${Math.round(data.percentual2FA || 0)}%</h4>
                            <p>Taxa de Adoção 2FA</p>
                        </div>
                    </div>
                </div>
            `;
        } catch (error) {
            document.getElementById('statsTab').innerHTML = '<p>Erro ao carregar estatísticas</p>';
        }
    },

    async verifyCode(secret) {
        const code = document.getElementById('testCode').value;
        const resultElement = document.getElementById('verificationResult');
        
        if (!code) {
            resultElement.innerHTML = '<p style="color: red;">Digite um código</p>';
            return;
        }
        
        try {
            const response = await fetch('/api/test/verify-totp', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ secret, code })
            });
            
            const data = await response.json();
            
            resultElement.innerHTML = `
                <p style="color: ${data.valid ? 'green' : 'red'}; margin-top: 1rem;">
                    ${data.message}
                </p>
            `;
            
            document.getElementById('testCode').value = '';
            
        } catch (error) {
            resultElement.innerHTML = '<p style="color: red;">Erro ao verificar código</p>';
        }
    }
};

// Adicionar estilos CSS para a seção de teste
document.addEventListener('DOMContentLoaded', () => {
    const style = document.createElement('style');
    style.textContent = `
        .system-info, .totp-demo, .system-stats {
            max-width: 100%;
        }

        .info-header, .demo-header, .stats-header {
            text-align: center;
            margin-bottom: 2rem;
            padding-bottom: 1rem;
            border-bottom: 2px solid var(--primary-color);
        }

        .status-indicator {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            margin-top: 0.5rem;
        }

        .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: var(--success-color);
        }

        .info-section, .demo-section {
            margin: 2rem 0;
            padding: 1.5rem;
            background: var(--bg-secondary);
            border-radius: 8px;
        }

        .tech-grid, .feature-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1rem;
            margin-top: 1rem;
        }

        .tech-item, .feature-item {
            padding: 0.75rem;
            background: var(--bg-primary);
            border-radius: 6px;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            border: 1px solid var(--border-color);
        }

        .tech-item i { color: var(--success-color); }
        .feature-item i { color: var(--primary-color); }

        .endpoint-list {
            display: grid;
            gap: 0.5rem;
            margin-top: 1rem;
        }

        .endpoint-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.75rem;
            background: var(--bg-primary);
            border-radius: 6px;
            border: 1px solid var(--border-color);
        }

        .endpoint-name {
            font-weight: 500;
            color: var(--text-primary);
        }

        .endpoint-url {
            font-family: 'Courier New', monospace;
            background: var(--bg-secondary);
            padding: 0.25rem 0.5rem;
            border-radius: 4px;
            color: var(--primary-color);
        }

        .code-display {
            display: flex;
            align-items: center;
            gap: 1rem;
            margin-top: 1rem;
        }

        .code-display code {
            flex: 1;
            padding: 1rem;
            background: var(--bg-primary);
            border: 2px solid var(--border-color);
            border-radius: 6px;
            font-family: 'Courier New', monospace;
            font-size: 0.9rem;
            word-break: break-all;
        }

        .btn-copy {
            padding: 0.75rem;
            background: var(--primary-color);
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
        }

        .current-code {
            text-align: center;
            margin: 1rem 0;
        }

        .totp-code {
            font-size: 2rem;
            font-family: 'Courier New', monospace;
            font-weight: bold;
            color: var(--primary-color);
            display: block;
            margin-bottom: 1rem;
        }

        .code-timer {
            display: flex;
            align-items: center;
            gap: 1rem;
            justify-content: center;
        }

        .timer-bar {
            width: 100%;
            height: 4px;
            background: var(--primary-color);
            border-radius: 2px;
            transition: width 1s linear;
            max-width: 200px;
        }

        .verification-test .form-group {
            display: flex;
            gap: 1rem;
            margin-bottom: 1rem;
        }

        .verification-test input {
            flex: 1;
            padding: 0.75rem;
            border: 2px solid var(--border-color);
            border-radius: 6px;
            text-align: center;
            font-size: 1.2rem;
            letter-spacing: 0.2rem;
        }

        .verification-result {
            margin-top: 1rem;
        }

        .result {
            padding: 1rem;
            border-radius: 6px;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .result.success {
            background: rgba(16, 185, 129, 0.1);
            color: var(--success-color);
            border: 1px solid var(--success-color);
        }

        .result.error {
            background: rgba(239, 68, 68, 0.1);
            color: var(--error-color);
            border: 1px solid var(--error-color);
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1.5rem;
            margin: 2rem 0;
        }

        .stat-card {
            background: var(--bg-secondary);
            padding: 1.5rem;
            border-radius: 8px;
            display: flex;
            align-items: center;
            gap: 1rem;
            border: 1px solid var(--border-color);
        }

        .stat-icon {
            padding: 1rem;
            border-radius: 50%;
            background: var(--primary-color);
            color: white;
        }

        .stat-icon.active { background: var(--success-color); }
        .stat-icon.secure { background: var(--warning-color); }
        .stat-icon.percentage { background: var(--secondary-color); }

        .stat-info h4 {
            font-size: 1.5rem;
            margin: 0;
            color: var(--text-primary);
        }

        .stat-info p {
            margin: 0;
            color: var(--text-secondary);
            font-size: 0.9rem;
        }

        .error-message {
            text-align: center;
            padding: 2rem;
            color: var(--error-color);
        }

        @keyframes pulse {
            0% { transform: scale(1); }
            50% { transform: scale(1.05); }
            100% { transform: scale(1); }
        }

        @media (max-width: 768px) {
            .tech-grid, .feature-grid {
                grid-template-columns: 1fr;
            }
            
            .endpoint-item {
                flex-direction: column;
                align-items: flex-start;
                gap: 0.5rem;
            }
            
            .verification-test .form-group {
                flex-direction: column;
            }
            
            .stats-grid {
                grid-template-columns: 1fr;
            }
        }
    `;
    document.head.appendChild(style);
}); 