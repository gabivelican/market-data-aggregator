const API_URL = 'http://localhost:8080/api';
const WS_URL = 'http://localhost:8080/ws';

let stompClient = null;
let currentToken = localStorage.getItem('jwt_token');
let priceChart = null;
let chartData = { labels: [], datasets: [] };

// 1. Initializare
document.addEventListener('DOMContentLoaded', () => {
    if (currentToken) {
        showDashboard();
    } else {
        showLogin();
    }

    // Login Form Handler
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        await login(username, password);
    });
});

// 2. Autentificare
async function login(username, password) {
    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            currentToken = data.token;
            localStorage.setItem('jwt_token', currentToken);
            localStorage.setItem('username', data.username);
            showDashboard();
        } else {
            showError('Invalid credentials');
        }
    } catch (err) {
        showError('Server error: ' + err.message);
    }
}

function logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    if (stompClient) stompClient.disconnect();
    location.reload();
}

function showLogin() {
    document.getElementById('login-screen').classList.remove('d-none');
    document.getElementById('dashboard-screen').classList.add('d-none');
}

function showDashboard() {
    document.getElementById('login-screen').classList.add('d-none');
    document.getElementById('dashboard-screen').classList.remove('d-none');
    document.getElementById('user-display').innerText = `👤 ${localStorage.getItem('username')}`;

    initChart();
    connectWebSocket();
    loadSymbols();
}

function showError(msg) {
    const el = document.getElementById('login-error');
    el.innerText = msg;
    el.classList.remove('d-none');
}

// 3. WebSocket Connection
function connectWebSocket() {
    const socket = new SockJS(WS_URL);
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Disable debug logs

    stompClient.connect({}, (frame) => {
        console.log('Connected: ' + frame);

        // Subscribe to Prices
        stompClient.subscribe('/topic/prices', (message) => {
            const data = JSON.parse(message.body);
            updatePriceUI(data);
            updateChart(data);
        });

        // Subscribe to Alerts
        stompClient.subscribe('/topic/alerts', (message) => {
            const alert = JSON.parse(message.body);
            addAlertUI(alert);
        });
    }, (error) => {
        console.error('WebSocket Error:', error);
        setTimeout(connectWebSocket, 5000); // Retry
    });
}

// 4. Data Loading & UI Updates
async function loadSymbols() {
    // Incarcam lista de simboluri (cu Token)
    const res = await fetch(`${API_URL}/symbols`, {
        headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const symbols = await res.json();

    const list = document.getElementById('symbols-list');
    const cards = document.getElementById('price-cards-container');

    list.innerHTML = '';
    cards.innerHTML = '';

    symbols.forEach(sym => {
        // Sidebar list
        list.innerHTML += `<li class="list-group-item d-flex justify-content-between align-items-center">
            ${sym.symbolCode} <span class="badge bg-primary rounded-pill">${sym.type}</span>
        </li>`;

        // Price Cards
        cards.innerHTML += `
            <div class="col-md-4 mb-3">
                <div class="card shadow-sm text-center">
                    <div class="card-body">
                        <h5 class="card-title text-muted">${sym.name}</h5>
                        <div class="card-price-value" id="price-${sym.symbolCode}">---</div>
                        <small class="text-muted" id="time-${sym.symbolCode}">Waiting for data...</small>
                    </div>
                </div>
            </div>
        `;
    });
}

function updatePriceUI(data) {
    const priceEl = document.getElementById(`price-${data.symbolCode}`);
    const timeEl = document.getElementById(`time-${data.symbolCode}`);

    if (priceEl) {
        const oldPrice = parseFloat(priceEl.innerText.replace('$', '')) || 0;
        const newPrice = data.price;

        priceEl.innerText = `$${newPrice.toFixed(2)}`;
        timeEl.innerText = new Date(data.timestamp).toLocaleTimeString();

        // Color coding
        priceEl.className = 'card-price-value ' + (newPrice > oldPrice ? 'price-up' : 'price-down');

        // Reset animation class
        setTimeout(() => priceEl.className = 'card-price-value', 1000);
    }
}

function addAlertUI(alert) {
    const feed = document.getElementById('alerts-feed');
    // Remove "No alerts" msg if exists
    if (feed.innerText.includes('No alerts')) feed.innerHTML = '';

    const html = `
        <div class="list-group-item alert-item alert-${alert.alertType}">
            <div class="d-flex w-100 justify-content-between">
                <h6 class="mb-1">${alert.symbolCode}: ${alert.alertType}</h6>
                <small>${new Date(alert.triggeredAt).toLocaleTimeString()}</small>
            </div>
            <p class="mb-1 small">${alert.details}</p>
            <button class="btn btn-sm btn-outline-secondary py-0" style="font-size: 0.7rem">Acknowledge</button>
        </div>
    `;
    feed.insertAdjacentHTML('afterbegin', html);

    // Update counter
    const countEl = document.getElementById('stat-alerts');
    countEl.innerText = parseInt(countEl.innerText) + 1;
}

// 5. Chart.js Configuration
function initChart() {
    const ctx = document.getElementById('mainChart').getContext('2d');

    // Configurare simplă pentru grafic
    priceChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'AAPL',
                    data: [],
                    borderColor: '#3498db',
                    tension: 0.4
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                x: { display: false } // Ascundem axa X pentru curatenie
            }
        }
    });
}

function updateChart(data) {
    // Momentan actualizam doar pentru AAPL ca exemplu
    if (data.symbolCode === 'AAPL' && priceChart) {
        const time = new Date(data.timestamp).toLocaleTimeString();

        // Adaugam date noi
        priceChart.data.labels.push(time);
        priceChart.data.datasets[0].data.push(data.price);

        // Pastram doar ultimele 20 puncte
        if (priceChart.data.labels.length > 20) {
            priceChart.data.labels.shift();
            priceChart.data.datasets[0].data.shift();
        }

        priceChart.update();
    }
}