const API_BASE = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
    ? 'http://localhost:8080/api' 
    : 'https://jules-trading-2-0-1.onrender.com/api';

document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const clientId = document.getElementById('clientId').value;
    const pin = document.getElementById('pin').value;
    const apiKey = document.getElementById('apiKey').value;
    const totpSecret = document.getElementById('totpSecret').value;
    
    const btn = document.getElementById('submit-btn');
    const errBox = document.getElementById('error-box');
    
    btn.textContent = 'Connecting...';
    btn.style.opacity = '0.7';
    errBox.style.display = 'none';

    try {
        const response = await fetch(`${API_BASE}/connect-broker`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ clientId, pin, apiKey, totpSecret })
        });

        if (response.ok) {
            localStorage.setItem('auth', 'true');
            window.location.href = 'index.html';
        } else {
            const data = await response.json();
            errBox.textContent = data.error || 'Authencation Failed.';
            errBox.style.display = 'block';
        }
    } catch (err) {
        errBox.textContent = 'Network error. Make sure Backend is running.';
        errBox.style.display = 'block';
    } finally {
        btn.textContent = 'Connect to Broker';
        btn.style.opacity = '1';
    }
});
