const REFRESH_RATE = 5000;

document.addEventListener('DOMContentLoaded', () => {
    initCharts();
    fetchData();
    setInterval(fetchData, REFRESH_RATE);
});

let revenueChart, loadChart;

function initCharts() {
    // Revenue Chart
    const ctx1 = document.getElementById('revenueChart').getContext('2d');
    revenueChart = new Chart(ctx1, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Daily Revenue (NXS)',
                data: [],
                backgroundColor: '#00f2ff',
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { grid: { display: false } }
            }
        }
    });

    // Load Chart (Mockup for now, could be line)
    const ctx2 = document.getElementById('loadChart').getContext('2d');
    loadChart = new Chart(ctx2, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Network Load %',
                data: [],
                borderColor: '#00ff9d',
                tension: 0.4,
                fill: true,
                backgroundColor: 'rgba(0, 255, 157, 0.1)'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { min: 0, max: 100, grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { display: false }
            }
        }
    });
}

async function fetchData() {
    try {
        const [stats, revenue, business, costs, chain] = await Promise.all([
            fetch('/api/stats').then(r => r.json()),
            fetch('/api/revenue').then(r => r.json()),
            fetch('/api/business').then(r => r.json()),
            fetch('/api/costs').then(r => r.json()),
            fetch('/api/chain').then(r => r.json())
        ]);

        updateUI(stats, revenue, business, costs, chain);
    } catch (e) {
        console.error("Connection failed", e);
    }
}

function updateUI(stats, revenue, business, costs, chain) {
    // Top Bar
    document.getElementById('last-updated-time').innerText = new Date().toLocaleTimeString();
    document.getElementById('node-port').innerText = 'Port: ' + stats.port;
    document.getElementById('node-peers').innerText = 'Peers: ' + stats.peerCount;

    // KPI Cards
    const totalRev = (revenue.totalFeesAllTime + revenue.totalRewardsAllTime).toFixed(2);
    document.getElementById('kpi-revenue').innerText = `${totalRev} NXS`;
    document.getElementById('kpi-wallets').innerText = business.activeWallets;
    document.getElementById('kpi-avg-fee').innerText = business.averageFeePerTx.toFixed(4) + ' NXS';

    // Treasury (Simulated calc from total rewards or fetched if we exposed it directly)
    // For now, let's assume we can calculate it from the 10% rule on total rewards roughly
    const treasuryEst = (revenue.totalRewardsAllTime * 0.10).toFixed(2);
    document.getElementById('kpi-treasury').innerText = `${treasuryEst} NXS`;

    // Charts
    updateRevenueChart(revenue.dailyFees, revenue.dailyRewards);
    updateLoadChart(business.networkLoad * 100);

    // Validator Panel
    document.getElementById('val-cost').innerText = '$' + costs.costPerBlock;
    document.getElementById('val-fees').innerText = (business.averageFeePerTx * 10).toFixed(2) + ' NXS'; // Mock avg vol

    // Blocks List
    const list = document.getElementById('blocks-list');
    list.innerHTML = '';
    // Show last 5 blocks
    chain.slice(-5).reverse().forEach(block => {
        const div = document.createElement('div');
        div.className = 'block-item';
        div.innerHTML = `
            <span>Block #${block.index}</span>
            <span class="hash">${block.hash.substring(0, 10)}...</span>
            <span>${block.transactions.length} txs</span>
        `;
        list.appendChild(div);
    });
}

function updateRevenueChart(dailyFees, dailyRewards) {
    // Combine keys and sort
    const labels = Object.keys(dailyFees).sort();
    const data = labels.map(date => dailyFees[date] + (dailyRewards[date] || 0));

    revenueChart.data.labels = labels;
    revenueChart.data.datasets[0].data = data;
    revenueChart.update();
}

function updateLoadChart(loadPercent) {
    // Push new data point, keep last 10
    const now = new Date().toLocaleTimeString();
    loadChart.data.labels.push(now);
    loadChart.data.datasets[0].data.push(loadPercent);

    if (loadChart.data.labels.length > 20) {
        loadChart.data.labels.shift();
        loadChart.data.datasets[0].data.shift();
    }
    loadChart.update();
}
