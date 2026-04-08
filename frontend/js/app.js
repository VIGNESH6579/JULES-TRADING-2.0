// Automatically switch between Localhost and Render production backend
const API_BASE = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
    ? 'http://localhost:8080/api' 
    : 'https://jules-trading-2-0-1.onrender.com/api';

const elements = {
    chainBody: document.getElementById('chain-body'),
    indexSelect: document.getElementById('index-select'),
    signalCard: document.getElementById('signal-card'),
    targetVal: document.getElementById('target-val'),
    slVal: document.getElementById('sl-val'),
    pcrVal: document.getElementById('pcr-val'),
    pcrGauge: document.getElementById('pcr-gauge'),
    maxPainVal: document.getElementById('max-pain-val'),
    ivRankVal: document.getElementById('iv-rank-val'),
    alertList: document.getElementById('alert-list')
};

async function fetchDashboardData(symbol) {
    try {
        const response = await fetch(`${API_BASE}/dashboard?symbol=${symbol}`);
        if (!response.ok) throw new Error('Network response was not ok');
        const data = await response.json();
        updateUI(data);
    } catch (error) {
        console.error("Error fetching data:", error);
    }
}

function updateUI(data) {
    if(!data) return;

    // 1. Update Signal
    const { signal } = data;
    elements.signalCard.className = `signal-card ${signal.action.toLowerCase()}`;
    elements.signalCard.querySelector('.signal-direction').textContent = signal.action;
    elements.targetVal.textContent = signal.target || '--';
    elements.slVal.textContent = signal.stopLoss || '--';
    elements.signalCard.querySelector('.signal-rationale').textContent = signal.rationale;

    // 2. Update Metrics
    elements.pcrVal.textContent = data.analytics.pcr.toFixed(2);
    // Rough gauge calc (0.5 to 1.5 range mapped to 0-100%)
    let pct = ((data.analytics.pcr - 0.5) / 1.0) * 100;
    pct = Math.max(0, Math.min(100, pct));
    elements.pcrGauge.style.width = `${pct}%`;
    
    elements.maxPainVal.textContent = data.analytics.maxPain;
    elements.ivRankVal.textContent = `${data.analytics.ivRank}%`;

    // 3. Update Option Chain Table
    renderChainTable(data.chain, data.spotPrice);
}

function renderChainTable(chainRows, spotPrice) {
    elements.chainBody.innerHTML = '';
    
    chainRows.forEach(row => {
        const tr = document.createElement('tr');
        
        // Highlight ITM
        const isCallItm = row.strikePrice < spotPrice;
        const isPutItm = row.strikePrice > spotPrice;

        const callClass = isCallItm ? 'itm-call' : '';
        const putClass = isPutItm ? 'itm-put' : '';

        tr.innerHTML = `
            <td class="${callClass} ${getColorClassForBuildup(row.ce.buildup)}">${row.ce.buildup}</td>
            <td class="${callClass}">${row.ce.greeks?.gamma?.toFixed(4) || '--'}</td>
            <td class="${callClass}">${row.ce.greeks?.vega?.toFixed(2) || '--'}</td>
            <td class="${callClass}">${row.ce.greeks?.theta?.toFixed(2) || '--'}</td>
            <td class="${callClass}">${row.ce.greeks?.delta?.toFixed(2) || '--'}</td>
            <td class="${callClass}"><strong>${row.ce.ltp}</strong></td>
            
            <td class="strike-col">${row.strikePrice}</td>
            
            <td class="${putClass}"><strong>${row.pe.ltp}</strong></td>
            <td class="${putClass}">${row.pe.greeks?.delta?.toFixed(2) || '--'}</td>
            <td class="${putClass}">${row.pe.greeks?.theta?.toFixed(2) || '--'}</td>
            <td class="${putClass}">${row.pe.greeks?.vega?.toFixed(2) || '--'}</td>
            <td class="${putClass}">${row.pe.greeks?.gamma?.toFixed(4) || '--'}</td>
            <td class="${putClass} ${getColorClassForBuildup(row.pe.buildup)}">${row.pe.buildup}</td>
        `;
        
        elements.chainBody.appendChild(tr);
    });
}

function getColorClassForBuildup(buildup) {
    if (!buildup) return '';
    if (buildup.includes('Long Buildup') || buildup.includes('Short Covering')) return 'text-bullish';
    if (buildup.includes('Short Buildup') || buildup.includes('Long Unwinding')) return 'text-bearish';
    return '';
}

function init() {
    const symbol = elements.indexSelect.value;
    fetchDashboardData(symbol);

    elements.indexSelect.addEventListener('change', (e) => {
        fetchDashboardData(e.target.value);
    });

    // Refresh every 10 seconds
    setInterval(() => {
        fetchDashboardData(elements.indexSelect.value);
    }, 10000);
}

document.addEventListener('DOMContentLoaded', init);
