
/* ═══════════════════════════════════════════════════════════════════════
   USER ROLE MANAGEMENT
═══════════════════════════════════════════════════════════════════════ */
let _roleTargetId = null;

function openRoleChangeModal(userId, userName, currentRole) {
    _roleTargetId = userId;
    document.getElementById('roleChangeUserName').textContent = userName;
    document.getElementById('roleChangeAdminPwd').value = '';
    const select = document.getElementById('roleChangeSelect');
    if (select) select.value = currentRole;
    new bootstrap.Modal(document.getElementById('roleChangeModal')).show();
}

async function submitRoleChange() {
    const pwd = document.getElementById('roleChangeAdminPwd').value.trim();
    const newRole = document.getElementById('roleChangeSelect').value;
    if (!pwd) { showAdminToast('danger', 'Please enter your admin password.'); return; }

    const fd = new FormData();
    fd.append('adminPassword', pwd);
    fd.append('newRole', newRole);
    
    // Get CSRF Token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    
    try {
        const resp = await fetch('/api/admin/users/' + _roleTargetId + '/change-role', { 
            method: 'POST', 
            body: fd,
            headers: csrfToken ? { [csrfHeader]: csrfToken } : {}
        });
        const data = await resp.json();
        
        if (resp.ok && data.success) {
            bootstrap.Modal.getInstance(document.getElementById('roleChangeModal')).hide();
            showAdminToast('success', data.message);
            // Update UI dynamically instead of full reload
            const badge = document.getElementById('role-badge-' + _roleTargetId);
            if (badge) {
                if (data.newRole === 'ROLE_ADMIN') {
                    badge.className = 'badge bg-dark';
                    badge.textContent = 'ADMIN';
                } else {
                    badge.className = 'badge';
                    badge.style.background = '#6c757d';
                    badge.textContent = 'USER';
                }
            }
        } else {
            showAdminToast('danger', data.message || 'Role change failed.');
        }
    } catch(e) { 
        showAdminToast('danger', 'Network error.'); 
    }
}

/* ═══════════════════════════════════════════════════════════════════════
   ANALYTICS ENGINE  (Chart.js powered)
═══════════════════════════════════════════════════════════════════════ */
let _trendChart = null;
let _pieChart   = null;
let _analyticsGroupBy = 'day';

const PIE_COLORS = [
    '#1A3A6B','#C9954A','#B03030','#198754','#6f42c1',
    '#0dcaf0','#fd7e14','#20c997','#e83e8c','#6c757d'
];

function setAnalyticsPeriod(btn, days) {
    document.querySelectorAll('.analytics-period-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    const to   = new Date();
    const from = new Date();
    from.setDate(from.getDate() - days + 1);
    document.getElementById('analyticsFrom').value = from.toISOString().split('T')[0];
    document.getElementById('analyticsTo').value   = to.toISOString().split('T')[0];
    loadAnalytics();
}

function setGroupBy(btn, groupBy) {
    document.querySelectorAll('.analytics-group-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    _analyticsGroupBy = groupBy;
    loadSalesTrend();
}

async function loadAnalytics() {
    await Promise.all([loadOverview(), loadSalesTrend(), loadProductsTable()]);
}

async function loadOverview() {
    try {
        const resp = await fetch('/api/admin/analytics/products/overview');
        if (!resp.ok) return;
        const d = await resp.json();

        document.getElementById('stat-totalProducts').textContent = d.totalProducts || 0;
        document.getElementById('stat-totalSold').textContent     = d.totalSoldUnits || 0;
        document.getElementById('stat-totalRevenue').textContent  = '₹' + (Number(d.totalProductRevenue || 0).toFixed(2));
        if (d.topProducts && d.topProducts.length > 0) {
            document.getElementById('stat-topProduct').textContent = d.topProducts[0].name + ' (' + d.topProducts[0].unitsSold + ' sold)';
        }
        
        document.getElementById('stat-totalStock').textContent = d.totalStock || 0;
        document.getElementById('stat-lowStock').textContent = d.lowStockCount || 0;
        document.getElementById('stat-outOfStock').textContent = d.outOfStockCount || 0;

        // Pie chart
        if (d.categorySales && Object.keys(d.categorySales).length > 0) {
            const labels = Object.keys(d.categorySales);
            const values = Object.values(d.categorySales);
            const ctx = document.getElementById('categoryPieChart').getContext('2d');
            if (_pieChart) _pieChart.destroy();
            _pieChart = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels,
                    datasets: [{
                        data: values,
                        backgroundColor: PIE_COLORS.slice(0, labels.length),
                        borderWidth: 2,
                        borderColor: '#fff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { font: { size: 11 }, padding: 12 } }
                    }
                }
            });
        }
    } catch(e) { console.error('Overview error', e); }
}

async function loadSalesTrend() {
    const from = document.getElementById('analyticsFrom').value;
    const to   = document.getElementById('analyticsTo').value;
    if (!from || !to) return;
    try {
        const url = `/api/admin/analytics/sales/by-date?from=${from}&to=${to}&groupBy=${_analyticsGroupBy}`;
        const resp = await fetch(url);
        if (!resp.ok) return;
        const d = await resp.json();
        const labels   = d.data.map(x => x.label);
        const revenues = d.data.map(x => Number(x.revenue || 0));
        const orders   = d.data.map(x => Number(x.orders || 0));

        const ctx = document.getElementById('salesTrendChart').getContext('2d');
        if (_trendChart) _trendChart.destroy();
        _trendChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Revenue (₹)',
                        data: revenues,
                        borderColor: '#1A3A6B',
                        backgroundColor: 'rgba(26,58,107,0.08)',
                        borderWidth: 2,
                        fill: true,
                        tension: 0.4,
                        pointBackgroundColor: '#1A3A6B',
                        pointRadius: 3,
                        yAxisID: 'y'
                    },
                    {
                        label: 'Orders',
                        data: orders,
                        borderColor: '#C9954A',
                        backgroundColor: 'transparent',
                        borderWidth: 2,
                        borderDash: [5,3],
                        tension: 0.4,
                        pointBackgroundColor: '#C9954A',
                        pointRadius: 3,
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: { position: 'top', labels: { font: { size: 11 } } }
                },
                scales: {
                    y:  { type: 'linear', position: 'left',  ticks: { callback: v => '₹'+v } },
                    y1: { type: 'linear', position: 'right', grid: { drawOnChartArea: false }, ticks: { stepSize: 1 } }
                }
            }
        });
    } catch(e) { console.error('Trend error', e); }
}

async function loadProductsTable() {
    const tbody = document.getElementById('analyticsTableBody');
    try {
        const resp = await fetch('/api/admin/analytics/products/all-sales');
        if (!resp.ok) return;
        const products = await resp.json();
        if (!products.length) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">No sales data yet.</td></tr>';
            return;
        }
        const maxSold = Math.max(...products.map(p => Number(p.unitsSold || 0)));
        tbody.innerHTML = products.map((p, i) => {
            const pct = maxSold > 0 ? Math.round((Number(p.unitsSold||0) / maxSold) * 100) : 0;
            const rank = i < 3 ? ['🥇','🥈','🥉'][i] : (i+1);
            return `<tr>
                <td>${rank}</td>
                <td style="font-weight:600; color:#2A2621;">${p.name}</td>
                <td><span class="badge" style="background:rgba(201,149,74,0.15); color:#7a5c2a; font-size:0.7rem;">${p.category||'—'}</span></td>
                <td>₹${Number(p.price||0).toFixed(2)}</td>
                <td>${p.stock ?? '—'}</td>
                <td style="font-weight:700; color:#1A3A6B;">${p.unitsSold||0}</td>
                <td style="font-weight:600; color:#198754;">₹${Number(p.revenue||0).toFixed(2)}</td>
                <td>
                    <div class="perf-bar-wrap"><div class="perf-bar" style="width:${pct}%;"></div></div>
                    <div style="font-size:0.7rem; color:#888; margin-top:2px;">${pct}%</div>
                </td>
            </tr>`;
        }).join('');
    } catch(e) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-danger py-4">Failed to load analytics.</td></tr>';
    }
}

// Auto-init analytics when tab is shown
document.addEventListener('DOMContentLoaded', () => {
    // Set default date range (7 days)
    const to   = new Date();
    const from = new Date();
    from.setDate(from.getDate() - 6);
    document.getElementById('analyticsFrom').value = from.toISOString().split('T')[0];
    document.getElementById('analyticsTo').value   = to.toISOString().split('T')[0];
});

// Lazy-load analytics only when tab is opened
const _origSwitchTab = window.switchTab;
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', () => {
        if (link.textContent.trim() === 'Analytics') {
            setTimeout(loadAnalytics, 100);
        }
    });
});

