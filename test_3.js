
/* ═══════════════════════════════════════════════════════════════════════════
   FEATURED BLEND TOGGLE  (real-time, no page reload)
═══════════════════════════════════════════════════════════════════════════ */
async function toggleFeaturedBlend(cb) {
    const id   = cb.dataset.id;
    const name = cb.dataset.name;
    const label = document.getElementById('feat-label-' + id);
    cb.disabled = true;

    try {
        const fd = new FormData();
        const resp = await fetch('/api/admin/products/' + id + '/toggle-featured', {
            method: 'POST',
            body: fd
        });
        const data = await resp.json();
        if (data.success) {
            const isFeatured = data.featured;
            if (label) {
                label.textContent = isFeatured ? 'Featured' : 'Off';
                label.style.color  = isFeatured ? '#1A3A6B' : '#aaa';
                label.style.fontWeight = isFeatured ? '700' : '400';
            }
            showAdminToast(isFeatured ? 'success' : 'secondary', data.message);
        } else {
            cb.checked = !cb.checked;  // revert
            showAdminToast('danger', data.message || 'Toggle failed');
        }
    } catch(e) {
        cb.checked = !cb.checked;
        showAdminToast('danger', 'Network error — toggle failed');
    } finally {
        cb.disabled = false;
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
   REFUND MANAGEMENT  (AJAX approve / reject)
═══════════════════════════════════════════════════════════════════════════ */
async function approveRefundAjax(orderId) {
    if (!confirm('Approve refund for Order #' + orderId + '?\n\nThis will trigger the Razorpay refund (if applicable) and cannot be undone.')) return;

    const btns = document.querySelectorAll(`[data-order-id="${orderId}"]`);
    btns.forEach(b => b.disabled = true);

    try {
        const fd = new FormData();
        const resp = await fetch('/api/admin/orders/' + orderId + '/approve-refund', {
            method: 'POST', body: fd
        });
        const data = await resp.json();
        if (data.success) {
            showAdminToast('success', data.message);
            updateReturnRow(orderId, 'APPROVED', 'REFUNDED');
        } else {
            showAdminToast('danger', data.message || 'Refund approval failed');
            btns.forEach(b => b.disabled = false);
        }
    } catch(e) {
        showAdminToast('danger', 'Network error during refund approval');
        btns.forEach(b => b.disabled = false);
    }
}

async function rejectRefundAjax(orderId) {
    const reason = prompt('Optional: Add a reason for rejection (shown to customer):') || '';

    const btns = document.querySelectorAll(`[data-order-id="${orderId}"]`);
    btns.forEach(b => b.disabled = true);

    try {
        const fd = new FormData();
        fd.append('adminMessage', reason);
        const resp = await fetch('/api/admin/orders/' + orderId + '/reject-refund', {
            method: 'POST', body: fd
        });
        const data = await resp.json();
        if (data.success) {
            showAdminToast('warning', 'Refund request rejected for Order #' + orderId);
            updateReturnRow(orderId, 'REJECTED', 'REJECTED');
        } else {
            showAdminToast('danger', data.message || 'Rejection failed');
            btns.forEach(b => b.disabled = false);
        }
    } catch(e) {
        showAdminToast('danger', 'Network error');
        btns.forEach(b => b.disabled = false);
    }
}

function updateReturnRow(orderId, returnStatus, refundStatus) {
    const row = document.getElementById('return-row-' + orderId);
    if (!row) return;
    const cells = row.querySelectorAll('td');
    // Return status (col 5)
    if (cells[5]) {
        const color = returnStatus === 'APPROVED' ? 'bg-success' : (returnStatus === 'REJECTED' ? 'bg-danger' : 'bg-warning text-dark');
        cells[5].innerHTML = `<span class="badge ${color}">${returnStatus}</span>`;
    }
    // Refund status (col 6)
    if (cells[6]) {
        const color2 = refundStatus === 'REFUNDED' ? 'bg-success' : (refundStatus === 'REJECTED' ? 'bg-danger' : 'bg-info text-dark');
        cells[6].innerHTML = `<span class="badge ${color2}"><i class="bi bi-${refundStatus === 'REFUNDED' ? 'check-circle' : 'x-circle'}"></i> ${refundStatus}</span>`;
    }
    // Actions (col 7) — lock it
    if (cells[7]) {
        cells[7].innerHTML = '<span class="text-muted" style="font-size:0.8rem;"><i class="bi bi-lock"></i> Processed</span>';
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
   FILTER MANAGER — Add / Remove / Save
═══════════════════════════════════════════════════════════════════════════ */
function addFilterTag(inputId, containerId, section) {
    const input = document.getElementById(inputId);
    const val   = (input.value || '').trim();
    if (!val) return;

    const container = document.getElementById(containerId);
    const span = document.createElement('span');
    span.className = 'filter-tag';
    span.textContent = val;
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'remove-filter';
    btn.innerHTML = '<i class="bi bi-x"></i>';
    btn.onclick = function() { removeFilterTag(this, section); };
    span.appendChild(btn);
    container.appendChild(span);
    input.value = '';
}

function removeFilterTag(btn, section) {
    btn.closest('.filter-tag').remove();
}

async function saveFilterSection(section) {
    let containerId, apiKey, apiSection;
    if (section === 'shop') {
        containerId = 'shop-filter-tags';
        apiSection = 'shop';
        apiKey = 'filterCategories';
    } else {
        containerId = 'spinner-filter-tags';
        apiSection = 'home';
        apiKey = 'spinnerCategories';
    }

    const tags = [...document.getElementById(containerId).querySelectorAll('.filter-tag')];
    const value = tags.map(t => t.textContent.trim()).filter(Boolean).join(',');

    const fd = new FormData();
    fd.append(apiKey, value);

    try {
        const resp = await fetch('/api/admin/content/' + apiSection, { method: 'POST', body: fd });
        const data = await resp.json();
        if (data.success) {
            showAdminToast('success', (section === 'shop' ? 'Shop' : 'Spinner') + ' filters saved: ' + value);
        } else {
            showAdminToast('danger', data.message || 'Failed to save filters');
        }
    } catch(e) {
        showAdminToast('danger', 'Network error saving filters');
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
   AUTO-REFRESH  — Every 10 minutes, preserving active tab & scroll position
═══════════════════════════════════════════════════════════════════════════ */
(function initAutoRefresh() {
    const REFRESH_MS = 10 * 60 * 1000; // 10 minutes
    let remaining = REFRESH_MS / 1000;
    const timerEl = document.getElementById('refresh-timer');

    function formatTime(s) {
        const m = Math.floor(s / 60);
        const sec = s % 60;
        return m + ':' + String(sec).padStart(2, '0');
    }

    const tick = setInterval(() => {
        remaining--;
        if (timerEl) timerEl.textContent = formatTime(remaining);
        if (remaining <= 0) {
            clearInterval(tick);
            // Save active tab before reload
            const activeTab = document.querySelector('.nav-link.active');
            if (activeTab) {
                const label = activeTab.textContent.trim();
                sessionStorage.setItem('adminActiveTab', label);
            }
            // Save scroll position
            sessionStorage.setItem('adminScrollY', window.scrollY);
            window.location.reload();
        }
    }, 1000);

    // Restore tab after reload
    window.addEventListener('DOMContentLoaded', () => {
        const savedTab = sessionStorage.getItem('adminActiveTab');
        const savedScroll = sessionStorage.getItem('adminScrollY');
        if (savedTab) {
            const navLinks = document.querySelectorAll('.nav-link');
            navLinks.forEach(link => {
                if (link.textContent.trim() === savedTab) {
                    link.click();
                }
            });
            sessionStorage.removeItem('adminActiveTab');
        }
        if (savedScroll) {
            setTimeout(() => window.scrollTo(0, parseInt(savedScroll)), 100);
            sessionStorage.removeItem('adminScrollY');
        }
    });
})();

/* ═══════════════════════════════════════════════════════════════════════════
   TOAST NOTIFICATION  (shared utility)
═══════════════════════════════════════════════════════════════════════════ */
function showAdminToast(type, message) {
    let container = document.getElementById('admin-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'admin-toast-container';
        Object.assign(container.style, {
            position: 'fixed', bottom: '28px', right: '20px', zIndex: '9999',
            display: 'flex', flexDirection: 'column', gap: '8px'
        });
        document.body.appendChild(container);
    }
    const colorMap = {
        success: '#198754', danger: '#dc3545', warning: '#856404',
        secondary: '#1A3A6B', info: '#0d6efd'
    };
    const bgMap = {
        success: 'rgba(25,135,84,0.08)', danger: 'rgba(220,53,69,0.08)',
        warning: 'rgba(255,193,7,0.15)', secondary: 'rgba(26,58,107,0.08)', info: 'rgba(13,110,253,0.08)'
    };
    const toast = document.createElement('div');
    toast.style.cssText = `
        background: #fff;
        border-left: 4px solid ${colorMap[type] || '#333'};
        border-radius: 6px;
        padding: 12px 18px;
        font-size: 0.82rem;
        font-family: 'Inter', sans-serif;
        color: ${colorMap[type] || '#333'};
        box-shadow: 0 6px 24px rgba(0,0,0,0.12);
        max-width: 340px;
        word-break: break-word;
        animation: fadeIn .3s ease;
        background: ${bgMap[type] || '#f9f9f9'};
    `;
    toast.innerHTML = `<i class="bi bi-${type === 'success' ? 'check-circle' : (type === 'danger' ? 'x-circle' : 'info-circle')}"></i> ${message}`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity .3s';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}

