/* ═══════════════════════════════════
   CoVoyage Admin Dashboard — App JS
   ═══════════════════════════════════ */

const API_BASE = window.location.origin + '/api/dashboard';
let autoRefreshTimer = null;
let currentSection = 'overview';

// ── Init ──
document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();
    setupControls();
    loadAllData();

    // Auto-refresh every 15s
    if (document.getElementById('autoRefresh').checked) {
        startAutoRefresh();
    }
});

// ═══════════════════════════════════
// NAVIGATION
// ═══════════════════════════════════

function setupNavigation() {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const section = item.dataset.section;
            navigateTo(section);
        });
    });

    // See-all buttons
    document.querySelectorAll('.see-all-btn').forEach(btn => {
        btn.addEventListener('click', () => navigateTo(btn.dataset.go));
    });

    // Mobile menu
    document.getElementById('menuToggle').addEventListener('click', () => {
        document.getElementById('sidebar').classList.toggle('open');
    });

    // Close sidebar on section click (mobile)
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', () => {
            document.getElementById('sidebar').classList.remove('open');
        });
    });
}

function navigateTo(section) {
    currentSection = section;

    // Update nav active state
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.toggle('active', item.dataset.section === section);
    });

    // Show/hide sections
    document.querySelectorAll('.content-section').forEach(sec => {
        sec.classList.toggle('active', sec.id === `section-${section}`);
    });

    // Update page title
    const titles = {
        overview: 'Overview',
        payments: 'Payments',
        journeys: 'Journeys',
        bookings: 'Bookings',
        revenue: 'Revenue Analytics',
    };
    document.getElementById('pageTitle').textContent = titles[section] || 'Dashboard';
}

// ═══════════════════════════════════
// CONTROLS
// ═══════════════════════════════════

function setupControls() {
    document.getElementById('refreshBtn').addEventListener('click', () => {
        const btn = document.getElementById('refreshBtn');
        btn.classList.add('spinning');
        loadAllData().then(() => {
            setTimeout(() => btn.classList.remove('spinning'), 500);
        });
    });

    document.getElementById('autoRefresh').addEventListener('change', (e) => {
        if (e.target.checked) {
            startAutoRefresh();
        } else {
            stopAutoRefresh();
        }
    });
}

function startAutoRefresh() {
    stopAutoRefresh();
    autoRefreshTimer = setInterval(loadAllData, 15000);
}

function stopAutoRefresh() {
    if (autoRefreshTimer) {
        clearInterval(autoRefreshTimer);
        autoRefreshTimer = null;
    }
}

// ═══════════════════════════════════
// DATA LOADING
// ═══════════════════════════════════

async function loadAllData() {
    try {
        const [stats, payments, revenue, journeys, bookings] = await Promise.all([
            fetchJson('/stats'),
            fetchJson('/payments'),
            fetchJson('/revenue'),
            fetchJson('/journeys'),
            fetchJson('/bookings'),
        ]);

        renderStats(stats);
        renderPayments(payments);
        renderRevenue(revenue);
        renderJourneys(journeys);
        renderBookings(bookings);

        updateConnectionStatus(true);
    } catch (err) {
        console.error('Failed to load data:', err);
        updateConnectionStatus(false);
    }
}

async function fetchJson(endpoint) {
    const res = await fetch(API_BASE + endpoint);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
}

function updateConnectionStatus(online) {
    const dot = document.querySelector('#connectionStatus .status-dot');
    const text = document.querySelector('#connectionStatus span:last-child');
    if (online) {
        dot.className = 'status-dot online';
        text.textContent = 'Server connected';
    } else {
        dot.className = 'status-dot offline';
        text.textContent = 'Connection lost';
    }
}

// ═══════════════════════════════════
// RENDER: OVERVIEW STATS
// ═══════════════════════════════════

function renderStats(stats) {
    document.getElementById('totalRevenue').textContent = formatCurrency(stats.totalRevenue);
    document.getElementById('platformFees').textContent = formatCurrency(stats.platformFees);
    document.getElementById('totalPayments').textContent = stats.totalPayments;
    document.getElementById('totalJourneys').textContent = stats.totalJourneys;

    document.getElementById('paymentBreakdown').textContent =
        `${stats.heldPayments} held · ${stats.pendingPayments} pending`;
    document.getElementById('bookingsCount').textContent =
        `${stats.totalBookings} bookings · ${stats.totalUsers} users`;

    // Status bars
    const total = stats.totalPayments || 1;
    const bars = [
        { status: 'Pending', cls: 'pending', count: stats.pendingPayments },
        { status: 'Held (Escrow)', cls: 'held', count: stats.heldPayments },
        { status: 'Released', cls: 'released', count: stats.releasedPayments },
        { status: 'Failed', cls: 'failed', count: stats.failedPayments },
    ];

    document.getElementById('statusBars').innerHTML = bars.map(b => `
        <div class="status-bar-row">
            <span class="status-bar-label">${b.status}</span>
            <div class="status-bar-track">
                <div class="status-bar-fill ${b.cls}" style="width: ${(b.count / total * 100)}%"></div>
            </div>
            <span class="status-bar-count">${b.count}</span>
        </div>
    `).join('');
}

// ═══════════════════════════════════
// RENDER: PAYMENTS
// ═══════════════════════════════════

function renderPayments(payments) {
    document.getElementById('paymentsCount').textContent = payments.length;

    // Recent (overview, max 5)
    document.getElementById('recentPaymentsBody').innerHTML =
        payments.length === 0
            ? '<tr><td colspan="5" class="empty-state">No payments yet</td></tr>'
            : payments.slice(0, 5).map(p => `
                <tr>
                    <td><strong>${esc(p.passengerName)}</strong></td>
                    <td><strong>${formatCurrency(p.amount)}</strong> XAF</td>
                    <td>${methodBadge(p.paymentMethod)}</td>
                    <td>${statusBadge(p.status)}</td>
                    <td>${formatDate(p.createdAt)}</td>
                </tr>
            `).join('');

    // All payments table
    document.getElementById('allPaymentsBody').innerHTML =
        payments.length === 0
            ? '<tr><td colspan="9" class="empty-state">No payments recorded</td></tr>'
            : payments.map(p => `
                <tr>
                    <td><strong>${esc(p.passengerName)}</strong></td>
                    <td>${esc(p.passengerPhone)}</td>
                    <td><strong>${formatCurrency(p.amount)}</strong> XAF</td>
                    <td>${formatCurrency(p.platformFee)} XAF</td>
                    <td>${formatCurrency(p.driverPayout)} XAF</td>
                    <td>${methodBadge(p.paymentMethod)}</td>
                    <td>${statusBadge(p.status)}</td>
                    <td><code style="font-size:11px;color:var(--text-muted)">${esc(p.txRef).slice(0, 20)}…</code></td>
                    <td>${formatDate(p.createdAt)}</td>
                </tr>
            `).join('');
}

// ═══════════════════════════════════
// RENDER: JOURNEYS
// ═══════════════════════════════════

function renderJourneys(journeys) {
    document.getElementById('journeysCount').textContent = journeys.length;

    document.getElementById('journeysBody').innerHTML =
        journeys.length === 0
            ? '<tr><td colspan="6" class="empty-state">No journeys listed</td></tr>'
            : journeys.map(j => `
                <tr>
                    <td><strong>${esc(j.departureCity)} → ${esc(j.arrivalCity)}</strong></td>
                    <td>${esc(j.driverName)}</td>
                    <td>${formatDate(j.departureDate)}</td>
                    <td>${j.availableSeats} / ${j.totalSeats}</td>
                    <td><strong>${formatCurrency(j.pricePerSeat)}</strong> XAF</td>
                    <td>${statusBadge(j.status)}</td>
                </tr>
            `).join('');
}

// ═══════════════════════════════════
// RENDER: BOOKINGS
// ═══════════════════════════════════

function renderBookings(bookings) {
    document.getElementById('bookingsCountBadge').textContent = bookings.length;

    document.getElementById('bookingsBody').innerHTML =
        bookings.length === 0
            ? '<tr><td colspan="6" class="empty-state">No bookings yet</td></tr>'
            : bookings.map(b => `
                <tr>
                    <td><strong>${esc(b.passengerName)}</strong></td>
                    <td><code style="font-size:11px;color:var(--text-muted)">${esc(b.journeyId).slice(0, 16)}…</code></td>
                    <td>${b.seatsBooked}</td>
                    <td><strong>${formatCurrency(b.totalAmount)}</strong> XAF</td>
                    <td>${statusBadge(b.status)}</td>
                    <td>${formatDate(b.createdAt)}</td>
                </tr>
            `).join('');
}

// ═══════════════════════════════════
// RENDER: REVENUE
// ═══════════════════════════════════

function renderRevenue(revenue) {
    const total = revenue.reduce((sum, r) => sum + r.totalAmount, 0) || 1;
    const totalFees = revenue.reduce((sum, r) => sum + r.platformFees, 0);

    // Stats cards
    revenue.forEach(r => {
        if (r.method === 'MTN_MOMO') {
            document.getElementById('revMtn').textContent = formatCurrency(r.totalAmount) + ' XAF';
            document.getElementById('revMtnCount').textContent = `${r.count} transactions`;
        } else if (r.method === 'ORANGE_MONEY') {
            document.getElementById('revOrange').textContent = formatCurrency(r.totalAmount) + ' XAF';
            document.getElementById('revOrangeCount').textContent = `${r.count} transactions`;
        } else if (r.method === 'CARD') {
            document.getElementById('revCard').textContent = formatCurrency(r.totalAmount) + ' XAF';
            document.getElementById('revCardCount').textContent = `${r.count} transactions`;
        }
    });

    // Revenue bars
    const barMap = { 'MTN_MOMO': 'mtn', 'ORANGE_MONEY': 'orange', 'CARD': 'card' };
    const labels = { 'MTN_MOMO': '📱 MTN MoMo', 'ORANGE_MONEY': '🍊 Orange Money', 'CARD': '💳 Card' };

    document.getElementById('revenueBars').innerHTML = revenue.map(r => `
        <div class="revenue-bar-item">
            <span class="revenue-bar-label">${labels[r.method] || r.method}</span>
            <div class="revenue-bar-track">
                <div class="revenue-bar-fill ${barMap[r.method] || ''}" style="width: ${(r.totalAmount / total * 100)}%">
                    ${r.totalAmount > 0 ? Math.round(r.totalAmount / total * 100) + '%' : ''}
                </div>
            </div>
            <span class="revenue-bar-amount">${formatCurrency(r.totalAmount)} XAF</span>
        </div>
    `).join('');

    // Fee summary
    document.getElementById('feeSummary').innerHTML = `
        <div class="fee-card">
            <div class="fee-card-label">Total Collected</div>
            <div class="fee-card-value">${formatCurrency(total === 1 ? 0 : total)} XAF</div>
            <div class="fee-card-sub">From all methods</div>
        </div>
        <div class="fee-card">
            <div class="fee-card-label">Platform Fees (10%)</div>
            <div class="fee-card-value" style="color: var(--gold)">${formatCurrency(totalFees)} XAF</div>
            <div class="fee-card-sub">CoVoyage revenue</div>
        </div>
        <div class="fee-card">
            <div class="fee-card-label">Driver Payouts (90%)</div>
            <div class="fee-card-value" style="color: var(--green-light)">${formatCurrency((total === 1 ? 0 : total) - totalFees)} XAF</div>
            <div class="fee-card-sub">Released to drivers</div>
        </div>
    `;
}

// ═══════════════════════════════════
// HELPERS
// ═══════════════════════════════════

function formatCurrency(amount) {
    return new Intl.NumberFormat('fr-CM').format(amount || 0);
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    try {
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
    } catch {
        return dateStr;
    }
}

function esc(str) {
    const el = document.createElement('span');
    el.textContent = str || '';
    return el.innerHTML;
}

function statusBadge(status) {
    const s = (status || '').toLowerCase();
    return `<span class="status-badge ${s}">${status || '—'}</span>`;
}

function methodBadge(method) {
    const icons = {
        'MTN_MOMO': '📱 MTN',
        'ORANGE_MONEY': '🍊 OM',
        'CARD': '💳 Card',
    };
    return `<span class="method-badge">${icons[method] || method || '—'}</span>`;
}
