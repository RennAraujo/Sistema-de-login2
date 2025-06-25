// Configuração global
const CONFIG = {
    API_BASE_URL: '',
    TOKEN_KEY: 'auth_token',
    USER_KEY: 'user_data'
};

// Estado da aplicação
const AppState = {
    isAuthenticated: false,
    user: null,
    token: null
};

// Utilitários localStorage
const Storage = {
    set(key, value) {
        localStorage.setItem(key, JSON.stringify(value));
    },
    get(key) {
        const item = localStorage.getItem(key);
        return item ? JSON.parse(item) : null;
    },
    remove(key) {
        localStorage.removeItem(key);
    }
};

// Sistema de notificações
const Notifications = {
    container: null,
    init() {
        this.container = document.getElementById('notifications');
    },
    show(message, type = 'info') {
        if (!this.container) return;
        
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <div style="flex: 1;">${message}</div>
            <button onclick="this.parentElement.remove()" style="background: none; border: none; cursor: pointer;">×</button>
        `;
        
        this.container.appendChild(notification);
        setTimeout(() => notification.remove(), 5000);
    },
    success(message) { this.show(message, 'success'); },
    error(message) { this.show(message, 'error'); }
};

// API helper
const API = {
    async request(url, options = {}) {
        const defaultOptions = {
            headers: { 'Content-Type': 'application/json' }
        };
        
        if (AppState.token) {
            defaultOptions.headers['Authorization'] = `Bearer ${AppState.token}`;
        }
        
        const response = await fetch(url, { ...defaultOptions, ...options });
        const data = await response.json();
        
        if (!response.ok) throw new Error(data.message || 'Erro na requisição');
        return data;
    },
    get(url) { return this.request(url, { method: 'GET' }); },
    post(url, data) { return this.request(url, { method: 'POST', body: JSON.stringify(data) }); }
};

// Navegação
const Navigation = {
    showSection(name) {
        ['auth', 'dashboard', 'test'].forEach(section => {
            const element = document.getElementById(`${section}Section`);
            if (element) element.style.display = section === name ? 'block' : 'none';
        });
    }
};

// Gerenciador de abas
const TabManager = {
    init() {
        document.querySelectorAll('.tab-btn').forEach(tab => {
            tab.addEventListener('click', () => {
                const targetTab = tab.getAttribute('data-tab');
                this.switchTab(targetTab);
            });
        });
    },
    switchTab(tabName) {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
        
        const activeBtn = document.querySelector(`[data-tab="${tabName}"]`);
        const activeContent = document.getElementById(`${tabName}Tab`);
        
        if (activeBtn) activeBtn.classList.add('active');
        if (activeContent) activeContent.classList.add('active');
    }
};

// Toggle senha
const PasswordToggle = {
    init() {
        document.querySelectorAll('.toggle-password').forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                const targetId = button.getAttribute('data-target');
                const input = document.getElementById(targetId);
                const icon = button.querySelector('i');
                
                if (input.type === 'password') {
                    input.type = 'text';
                    icon.className = 'fas fa-eye-slash';
                } else {
                    input.type = 'password';
                    icon.className = 'fas fa-eye';
                }
            });
        });
    }
};

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    Notifications.init();
    TabManager.init();
    PasswordToggle.init();
    
    const token = Storage.get(CONFIG.TOKEN_KEY);
    if (token) {
        AppState.token = token;
        AppState.isAuthenticated = true;
        Navigation.showSection('dashboard');
    } else {
        Navigation.showSection('auth');
    }
    
    // Event listeners
    document.getElementById('testButton')?.addEventListener('click', () => {
        Navigation.showSection('test');
    });
    
    document.getElementById('closeTestBtn')?.addEventListener('click', () => {
        Navigation.showSection(AppState.isAuthenticated ? 'dashboard' : 'auth');
    });
});

function closeTwoFactorModal() {
    document.getElementById('twoFactorModal').style.display = 'none';
} 