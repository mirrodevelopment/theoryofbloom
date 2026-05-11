
        function switchTab(tabId, el) {
            document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
            document.querySelectorAll('.sidebar .nav-link').forEach(l => l.classList.remove('active'));

            document.getElementById(tabId).classList.add('active');
            if (el) el.classList.add('active');
            else document.querySelector(`.sidebar .nav-link[onclick*="${tabId}"]`).classList.add('active');

            const titles = {
                'dashboard': 'Dashboard Overview',
                'orders': 'Manage Orders',
                'products': 'Product Catalog',
                'users': 'User Management',
                'reviews': 'Review Approval',
                'returns': 'Return Requests',
                'activity': 'Recent Activity Log',
                'add-product': 'Add New Product',
                'edit-product': 'Update Product',
                'filter-manager': 'Filter Manager',
                'hero-book-editor': 'Hero Book Editor',
                'content-manager': 'Content Manager'
            };
            document.getElementById('pageTitle').innerText = titles[tabId] || tabId;
            window.location.hash = tabId;
        }

        function editProduct(btn) {
            document.getElementById('updateForm').action = '/admin/products/' + btn.getAttribute('data-id') + '/update';
            document.getElementById('updName').value = btn.getAttribute('data-name');
            document.getElementById('updPrice').value = btn.getAttribute('data-price');
            document.getElementById('updCategory').value = btn.getAttribute('data-category');
            document.getElementById('updStock').value = btn.getAttribute('data-stock');

            document.getElementById('updShort').value = btn.getAttribute('data-short') || '';
            document.getElementById('updLong').value = btn.getAttribute('data-long') || '';
            document.getElementById('updIngredients').value = btn.getAttribute('data-ingredients') || '';
            document.getElementById('updBenefits').value = btn.getAttribute('data-benefits') || '';
            document.getElementById('updUsage').value = btn.getAttribute('data-usage') || '';
            document.getElementById('updFaq').value = btn.getAttribute('data-faq') || '';
            document.getElementById('updDirections').value = btn.getAttribute('data-directions') || '';
            document.getElementById('updWeight').value = btn.getAttribute('data-weight') || '';
            document.getElementById('updFeatured').checked = btn.getAttribute('data-featured') === 'true';
            document.getElementById('updBestseller').checked = btn.getAttribute('data-bestseller') === 'true';
            document.getElementById('updNewArrival').checked = btn.getAttribute('data-new-arrival') === 'true';
            document.getElementById('updTopRated').checked = btn.getAttribute('data-top-rated') === 'true';

            // Show current product image preview
            const imgUrl = btn.getAttribute('data-imageurl');
            const imgWrap = document.getElementById('updCurrentImgWrap');
            const imgEl = document.getElementById('updCurrentImg');
            if (imgUrl && imgUrl !== 'null') {
                imgEl.src = imgUrl;
                imgWrap.style.display = 'block';
            } else {
                imgWrap.style.display = 'none';
            }
            // Reset file input labels
            document.getElementById('upd-file-name').innerText = 'No file chosen';
            document.getElementById('upd-sub-file-name').innerText = '0 files chosen';

            switchTab('edit-product', null);
        }

        /* ── Generic Product Tag Toggle (Bestseller / New Arrival / Top Rated) ── */
        async function toggleTag(cb, tagType) {
            const id = cb.dataset.id;
            const name = cb.dataset.name;
            cb.disabled = true;
            const labelMap = { 'bestseller': 'bs-label-', 'new-arrival': 'na-label-', 'top-rated': 'tr-label-' };
            const colorMap = { 'bestseller': '#C9954A', 'new-arrival': '#198754', 'top-rated': '#B03030' };
            const label = document.getElementById((labelMap[tagType] || '') + id);
            try {
                const resp = await fetch('/api/admin/products/' + id + '/toggle-' + tagType, { method: 'POST', body: new FormData() });
                const data = await resp.json();
                if (data.success) {
                    const val = data.bestseller ?? data.newArrival ?? data.topRated;
                    if (label) {
                        label.textContent = val ? 'On' : 'Off';
                        label.style.color = val ? (colorMap[tagType] || '#333') : '#aaa';
                        label.style.fontWeight = val ? '700' : '400';
                    }
                    showAdminToast('success', data.message);
                } else {
                    cb.checked = !cb.checked;
                    showAdminToast('danger', data.message || 'Toggle failed');
                }
            } catch(e) {
                cb.checked = !cb.checked;
                showAdminToast('danger', 'Network error');
            } finally {
                cb.disabled = false;
            }
        }

        /* ── Product Delete via AJAX ── */
        async function deleteProductAjax(productId) {
            if (!confirm('Are you sure you want to delete this product? This cannot be undone.')) return;
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
            try {
                const fd = new FormData();
                const resp = await fetch('/admin/products/' + productId + '/delete', {
                    method: 'POST',
                    body: fd,
                    headers: csrfToken ? { [csrfHeader]: csrfToken } : {}
                });
                if (resp.ok || resp.redirected) {
                    const row = document.getElementById('product-row-' + productId);
                    if (row) row.remove();
                    showAdminToast('success', 'Product deleted successfully.');
                } else {
                    showAdminToast('danger', 'Failed to delete product. Check server logs.');
                }
            } catch(e) {
                showAdminToast('danger', 'Network error during deletion.');
            }
        }


        function openReturnAdminModal(id, decision) {
            document.getElementById('returnAdminForm').action = '/admin/returns/' + id + '/process';
            document.getElementById('returnDecision').value = decision;
            document.getElementById('returnAdminModalTitle').innerText = decision === 'APPROVE' ? 'Approve Return Request' : 'Deny Return Request';
            document.getElementById('returnAdminSubmitBtn').innerText = decision === 'APPROVE' ? 'Approve' : 'Deny';
            document.getElementById('returnAdminSubmitBtn').style.background = decision === 'APPROVE' ? '#28a745' : '#dc3545';
            new bootstrap.Modal(document.getElementById('returnAdminModal')).show();
        }

        function handleOrderFilterTypeChange() {
            const filterType = document.getElementById("orderFilterType").value;
            const container = document.getElementById("orderSearchContainer");

            if (filterType === '3') { // Date
                container.innerHTML = '<input type="date" id="orderSearch" class="form-control" style="max-width: 300px;" onchange="filterTable(\'orderSearch\', \'orderTable\', \'orderFilterType\', true)">';
            } else if (filterType === '6') { // Status
                container.innerHTML = '<select id="orderSearch" class="form-select" style="max-width: 300px;" onchange="filterTable(\'orderSearch\', \'orderTable\', \'orderFilterType\')">' +
                    '<option value="">All Statuses</option>' +
                    '<option value="PENDING">Pending</option>' +
                    '<option value="PAID">Paid</option>' +
                    '<option value="SHIPPED">Shipped</option>' +
                    '<option value="DELIVERED">Delivered</option>' +
                    '<option value="RETURN">Return Requested/Returned</option>' +
                    '<option value="CANCELLED">Cancelled</option>' +
                    '</select>';
            } else {
                container.innerHTML = '<input type="text" id="orderSearch" class="form-control" style="max-width: 300px;" placeholder="Search..." onkeyup="filterTable(\'orderSearch\', \'orderTable\', \'orderFilterType\')">';
            }
            filterTable('orderSearch', 'orderTable', 'orderFilterType');
        }

        function sortOrderTable() {
            const table = document.getElementById("orderTable");
            const tbody = table.querySelector("tbody");
            const rows = Array.from(tbody.querySelectorAll("tr"));
            const sortType = document.getElementById("orderSortType").value;

            rows.sort((a, b) => {
                const idA = parseInt(a.getAttribute("data-id"));
                const idB = parseInt(b.getAttribute("data-id"));
                if (isNaN(idA)) return 1;
                if (isNaN(idB)) return -1;
                if (sortType === "newest") {
                    return idB - idA;
                } else {
                    return idA - idB;
                }
            });

            rows.forEach(row => tbody.appendChild(row));
        }

        function sortActivityTable() {
            const table = document.getElementById("activityTable");
            const tbody = table.querySelector("tbody");
            const rows = Array.from(tbody.querySelectorAll("tr"));
            const sortType = document.getElementById("activitySortType").value;

            rows.sort((a, b) => {
                const idA = parseInt(a.getAttribute("data-id"));
                const idB = parseInt(b.getAttribute("data-id"));
                if (isNaN(idA)) return 1;
                if (isNaN(idB)) return -1;
                if (sortType === "newest") {
                    return idB - idA;
                } else {
                    return idA - idB;
                }
            });

            rows.forEach(row => tbody.appendChild(row));
        }

        function filterTable(inputId, tableId, selectId, isDate = false) {
            const input = document.getElementById(inputId);
            if (!input) return;
            let filter = input.value.toLowerCase();

            if (isDate && input.value) {
                const dateObj = new Date(input.value);
                const months = ["jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"];
                filter = dateObj.getDate().toString().padStart(2, '0') + ' ' + months[dateObj.getMonth()] + ' ' + dateObj.getFullYear();
            }
            const table = document.getElementById(tableId);
            const trs = table.getElementsByTagName("tr");

            const select = selectId ? document.getElementById(selectId) : null;
            const colIndex = select ? parseInt(select.value) : -1;

            for (let i = 1; i < trs.length; i++) { // Skip header row
                let td = trs[i].getElementsByTagName("td");
                let match = false;

                if (colIndex === -1) {
                    for (let j = 0; j < td.length; j++) {
                        if (td[j]) {
                            if (td[j].innerText.toLowerCase().indexOf(filter) > -1) {
                                match = true;
                                break;
                            }
                        }
                    }
                } else {
                    if (td[colIndex]) {
                        if (td[colIndex].innerText.toLowerCase().indexOf(filter) > -1) {
                            match = true;
                        }
                    }
                }

                if (match) {
                    trs[i].style.display = "";
                } else {
                    trs[i].style.display = "none";
                }
            }
        }

        function toggleCustomDates(val) {
            const wrap = document.getElementById('customDateWrap');
            const endSpan = document.getElementById('endDateSpan');
            const endInput = document.getElementById('endDate');

            if (val === 'custom') {
                wrap.style.setProperty('display', 'flex', 'important');
                endSpan.style.display = 'inline';
                endInput.style.display = 'inline-block';
            } else if (val === 'selective') {
                wrap.style.setProperty('display', 'flex', 'important');
                endSpan.style.display = 'none';
                endInput.style.display = 'none';
            } else {
                wrap.style.setProperty('display', 'none', 'important');
                document.getElementById('revenueFilterForm').submit();
            }
        }

        window.onload = function () {
            if (window.location.hash) {
                const tab = window.location.hash.substring(1);
                if (document.getElementById(tab)) {
                    switchTab(tab, null);
                }
            }
            sortOrderTable();
            sortActivityTable();
        };

        // Auto-refresh the dashboard every 30 seconds to fetch new changes from users/admin,
        // but only if there are no modals currently open so we don't interrupt editing.
        setInterval(function () {
            if (document.querySelectorAll('.modal.show').length === 0) {
                window.location.reload();
            }
        }, 30000);
        // Mock Review Functions
        function approveReview(id) {
            document.getElementById('reviewStatus' + id).innerHTML = '<span class="badge-status badge-paid">Approved</span>';
            document.getElementById('reviewActions' + id).innerHTML = `
                <button class="btn-action btn-secondary" style="background:#6c757d;color:#fff;border:none;" onclick="undoReview(${id})"><i class="bi bi-arrow-counterclockwise"></i> Undo</button>
                <button class="btn-action btn-danger ms-1" onclick="deleteReview(${id})"><i class="bi bi-trash"></i> Delete</button>
            `;
        }

        function undoReview(id) {
            document.getElementById('reviewStatus' + id).innerHTML = '<span class="badge-status badge-pending">Pending</span>';
            document.getElementById('reviewActions' + id).innerHTML = `
                <button class="btn-action btn-primary-custom" onclick="approveReview(${id})"><i class="bi bi-check-lg"></i> Approve</button>
                <button class="btn-action btn-danger ms-1" onclick="deleteReview(${id})"><i class="bi bi-trash"></i> Delete</button>
            `;
        }

        function deleteReview(id) {
            if (confirm('Are you sure you want to delete this review?')) {
                document.getElementById('reviewRow' + id).remove();
            }
        }

        function toggleAdminPasswordReq() {
            var roleSelect = document.getElementById('addUserRole');
            var adminPwdDiv = document.getElementById('adminPwdDiv');
            var adminPwdInput = document.getElementById('adminPwdInput');
            if (roleSelect.value === 'ROLE_ADMIN') {
                adminPwdDiv.style.display = 'block';
                adminPwdInput.required = true;
            } else {
                adminPwdDiv.style.display = 'none';
                adminPwdInput.required = false;
            }
        }

        function openDeleteAdminModal(userId) {
            document.getElementById('deleteAdminId').value = userId;
            var modal = new bootstrap.Modal(document.getElementById('deleteAdminModal'));
            modal.show();
        }

        function generateBill(orderId) {
            const tr = document.querySelector(`tr[data-id="${orderId}"]`);
            if (!tr) return;

            const tds = tr.querySelectorAll('td');
            const contactName = tds[1].querySelector('div') ? tds[1].querySelector('div').innerText : '';
            const contactEmail = tds[1].querySelector('small') ? tds[1].querySelector('small').innerText : '';
            const dateStr = tds[3].innerText;
            const amountStr = tds[4].innerText;
            const amountNum = parseFloat(amountStr.replace(/[^0-9.-]+/g, "")) || 0;
            const paymentHtml = tds[5].innerText.replace(/\n/g, ' - ');
            const status = tds[6].querySelector('span') ? tds[6].querySelector('span').innerText : '';

            const productsDiv = tds[2];
            const productItems = Array.from(productsDiv.querySelectorAll('div')).map(div => {
                const text = div.innerText.trim();
                const lastX = text.lastIndexOf('x');
                if (lastX !== -1) {
                    return {
                        name: text.substring(0, lastX).trim(),
                        qty: parseInt(text.substring(lastX + 1).trim()) || 1
                    };
                }
                return { name: text, qty: 1 };
            });

            const gst = parseFloat((amountNum * 0.18).toFixed(2));
            const taxable = parseFloat((amountNum - gst).toFixed(2));
            const grand = parseFloat((amountNum + 49).toFixed(2));
            const invNum = 'TOB-INV-' + String(orderId).padStart(6, '0');

            let itemRows = '';
            let totalQty = 0;
            productItems.forEach((it, index) => {
                const g = index === 0 ? amountNum : 0;
                const t = parseFloat((g / 1.18).toFixed(2));
                const igst = parseFloat((g - t).toFixed(2));
                totalQty += it.qty;
                itemRows += `<tr>
                        <td style="padding:8px 6px;border:1px solid #ddd;font-size:12px;">
                            <strong>${it.name}</strong><br>
                            <span style="color:#666;font-size:11px;">HSN/SAC: 2106 &nbsp;|&nbsp; IGST: 18%</span>
                        </td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:center;font-size:12px;">${it.qty}</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;">₹${g.toFixed(2)}</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;">₹0.00</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;">₹${t.toFixed(2)}</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;">₹${igst.toFixed(2)}</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;font-weight:600;">₹${g.toFixed(2)}</td>
                    </tr>`;
            });

            const html = `
                <div style="font-family:'Times New Roman',serif;color:#111;max-width:780px;margin:0 auto;padding:24px;border:1px solid #999;text-align:left;">
                <style>
                    .inv-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-bottom:14px; }
                    .inv-table-wrap { overflow-x:auto; margin-bottom:12px; }
                    .inv-table { width:100%; min-width:500px; border-collapse:collapse; }
                    @media (max-width: 600px) { .inv-grid { grid-template-columns:1fr; gap:12px; } }
                </style>

                <!-- Header -->
                <div style="display:flex;flex-wrap:wrap;justify-content:space-between;align-items:flex-start;border-bottom:2px solid #222;padding-bottom:12px;margin-bottom:12px;gap:12px;">
                    <div>
                    <div style="font-size:22px;font-weight:700;letter-spacing:1px;">Theory <em>of</em> Bloom</div>
                    <div style="font-size:11px;color:#444;margin-top:4px;">Sold By: Theory of Bloom</div>
                    <div style="font-size:11px;color:#444;">12/87 Sathy Cross No:1, Gandhipuram,</div>
                    <div style="font-size:11px;color:#444;">Coimbatore, Tamil Nadu — 641012</div>
                    <div style="font-size:11px;color:#444;">GSTIN: 29AABCT1234K1ZX</div>
                    </div>
                    <div style="text-align:right;">
                    <div style="font-size:18px;font-weight:700;text-transform:uppercase;letter-spacing:2px;">Tax Invoice</div>
                    <div style="font-size:11px;margin-top:6px;"><strong>Invoice No:</strong> ${invNum}</div>
                    <div style="font-size:11px;"><strong>Order ID:</strong> TOB-ORD-${orderId}</div>
                    <div style="font-size:11px;"><strong>Invoice Date:</strong> ${dateStr}</div>
                    <div style="font-size:11px;margin-top:4px;"><strong>Status:</strong> ${status}</div>
                    </div>
                </div>

                <!-- Bill To / Ship To -->
                <div class="inv-grid">
                    <div style="border:1px solid #ccc;padding:10px;font-size:12px;">
                    <div style="font-size:11px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;color:#555;margin-bottom:6px;border-bottom:1px solid #eee;padding-bottom:4px;">Bill To / Ship To</div>
                    <div style="font-weight:700;">${contactName}</div>
                    <div>Email: ${contactEmail}</div>
                    <div style="margin-top:8px;"><strong>Payment:</strong> ${paymentHtml}</div>
                    </div>
                    <div style="border:1px solid #ccc;padding:10px;font-size:11px;color:#555;">
                        <div style="font-size:10px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;margin-bottom:6px;border-bottom:1px solid #eee;padding-bottom:4px;">Seller Details</div>
                        Theory of Bloom<br>
                        12/87 Sathy Cross No:1, Gandhipuram,<br>
                        Coimbatore, Tamil Nadu — 641012<br>
                        GSTIN: 29AABCT1234K1ZX<br>Nature of Transaction: INTRA &nbsp;|&nbsp; Nature of Supply: Goods
                    </div>
                </div>

                <!-- Product Table -->
                <div class="inv-table-wrap">
                <table class="inv-table">
                    <thead>
                    <tr style="background:#f0f0f0;">
                        <th style="padding:8px 6px;border:1px solid #ddd;text-align:left;font-size:12px;">Product</th>
                        <th style="padding:8px 6px;border:1px solid #ddd;text-align:center;font-size:12px;white-space:nowrap;">Qty</th>
                        <th style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;white-space:nowrap;">Gross Amt ₹</th>
                        <th style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;white-space:nowrap;">Discount ₹</th>
                        <th style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;white-space:nowrap;">Taxable ₹</th>
                        <th style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;white-space:nowrap;">IGST 18% ₹</th>
                        <th style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;white-space:nowrap;">Total ₹</th>
                    </tr>
                    </thead>
                    <tbody>${itemRows}</tbody>
                    <tfoot>
                    <tr style="background:#f9f9f9;font-weight:700;">
                        <td style="padding:8px 6px;border:1px solid #ddd;font-size:12px;">Total</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:center;font-size:12px;">${totalQty}</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;">₹${amountNum.toFixed(2)}</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;">₹0.00</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;">₹${taxable.toFixed(2)}</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;">₹${gst.toFixed(2)}</td>
                        <td style="padding:8px 6px;border:1px solid #ddd;text-align:right;font-size:12px;">₹${amountNum.toFixed(2)}</td>
                    </tr>
                    </tfoot>
                </table>
                </div>

                <!-- Delivery + Grand Total -->
                <div style="display:flex;flex-wrap:wrap;justify-content:space-between;align-items:flex-end;margin-bottom:20px;gap:12px;">
                    <div style="font-size:11px;color:#555;max-width:320px;">
                    <em>* Keep this invoice for warranty and return purposes.</em><br><br>
                    <strong>Note:</strong> Delivery charges of ₹49 are included in the Grand Total.
                    </div>
                    <div style="text-align:right;">
                    <table style="font-size:12px;border-collapse:collapse;margin-left:auto;">
                        <tr><td style="padding:4px 16px 4px 0;color:#555;">Subtotal</td><td style="padding:4px;font-weight:600;">₹${amountNum.toFixed(2)}</td></tr>
                        <tr><td style="padding:4px 16px 4px 0;color:#555;">Delivery</td><td style="padding:4px;">₹49.00</td></tr>
                        <tr><td style="padding:4px 16px 4px 0;color:#555;">IGST (18%)</td><td style="padding:4px;">₹${gst.toFixed(2)}</td></tr>
                        <tr style="border-top:2px solid #222;">
                        <td style="padding:8px 16px 4px 0;font-weight:700;font-size:14px;">Grand Total</td>
                        <td style="padding:8px 4px;font-weight:700;font-size:14px;">₹${grand.toFixed(2)}</td>
                        </tr>
                    </table>
                    </div>
                </div>

                <!-- Signature -->
                <div style="display:flex;justify-content:space-between;border-top:1px solid #ccc;padding-top:14px;">
                    <div style="font-size:11px;color:#555;">
                    <div><strong>Consignor:</strong> Theory of Bloom</div>
                    <div><strong>Consignee:</strong> ${contactName}</div>
                    </div>
                    <div style="text-align:right;">
                    <img src="/images/signature.svg" onerror="this.style.display='none'" alt="Signature" style="height:50px;margin-bottom:4px;opacity:0.9;">
                    <div style="font-size:11px;color:#555;margin-bottom:8px;">Authorised Signatory</div>
                    <div style="border-top:1px solid #333;width:160px;padding-top:6px;font-size:12px;font-weight:700;font-family:'Times New Roman',serif;font-style:italic;">Theory of Bloom</div>
                    </div>
                </div>

                </div>
            `;

            document.getElementById('billPrintArea').innerHTML = html;
            new bootstrap.Modal(document.getElementById('billModal')).show();
        }

        function printBill() {
            const printContent = document.getElementById('billPrintArea').innerHTML;
            const originalContent = document.body.innerHTML;

            document.body.innerHTML = printContent;
            window.print();

            document.body.innerHTML = originalContent;
            window.location.reload();
        }
    
