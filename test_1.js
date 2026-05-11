
                function cmsSwitchTab(name, el) {
                    document.querySelectorAll('.cms-page-tab').forEach(t => t.classList.remove('active'));
                    document.querySelectorAll('.cms-panel').forEach(p => p.classList.remove('active'));
                    el.classList.add('active');
                    document.getElementById('cmsPanel-' + name).classList.add('active');
                }
                function cmsToggle(head) {
                    const body = head.nextElementSibling;
                    const open = body.classList.contains('open');
                    document.querySelectorAll('.cms-section-body').forEach(b => b.classList.remove('open'));
                    if (!open) body.classList.add('open');
                }
                function addTestimonial() {
                    const r = document.getElementById('testiRepeater');
                    const item = document.createElement('div');
                    item.className = 'testi-item';
                    item.innerHTML = `
                    <button type="button" class="remove-testi" onclick="this.closest('.testi-item').remove()"><i class="bi bi-x"></i></button>
                    <div class="cms-row">
                        <div class="cms-field"><label>Name</label><input type="text" name="testiName" placeholder="Customer Name"></div>
                        <div class="cms-field"><label>Location</label><input type="text" name="testiLocation" placeholder="City"></div>
                    </div>
                    <div class="cms-row">
                        <div class="cms-field"><label>Stars (★)</label><input type="text" name="testiStars" value="★★★★★"></div>
                        <div class="cms-field"><label>Quote</label><input type="text" name="testiQuote" placeholder="Customer review..."></div>
                    </div>`;
                    r.appendChild(item);
                }
                function previewImg(inputId, previewId) {
                    const src = document.getElementById(inputId).value;
                    const img = document.getElementById(previewId);
                    if (src) { img.src = src; img.style.display = 'block'; }
                    else img.style.display = 'none';
                }
                function previewFile(input, previewId) {
                    if (input.files && input.files[0]) {
                        const reader = new FileReader();
                        reader.onload = function (e) {
                            const img = document.getElementById(previewId);
                            img.src = e.target.result;
                            img.style.display = 'block';
                        };
                        reader.readAsDataURL(input.files[0]);
                    }
                }
                // Auto-open first section in active panel
                document.addEventListener('DOMContentLoaded', () => {
                    document.querySelectorAll('.cms-panel.active .cms-section-body').forEach((b, i) => { if (i === 0) b.classList.add('open'); });
                });

                // Handle AJAX submissions for CMS forms
                document.addEventListener('DOMContentLoaded', () => {
                    document.querySelectorAll('.cms-panel form').forEach(form => {
                        form.addEventListener('submit', async (e) => {
                            e.preventDefault();

                            const submitBtn = form.querySelector('.cms-save-btn');
                            const originalText = submitBtn.innerHTML;
                            submitBtn.innerHTML = '<i class="spinner-border spinner-border-sm me-2"></i> Saving...';
                            submitBtn.disabled = true;

                            try {
                                const formData = new FormData(form);
                                // Convert action URL from /admin/content/... to /api/admin/content/...
                                const url = form.getAttribute('action').replace('/admin/content/', '/api/admin/content/');

                                const response = await fetch(url, {
                                    method: 'POST',
                                    body: formData
                                });

                                const data = await response.json();

                                if (response.ok && data.success) {
                                    showToast('Success', data.message || 'Content saved successfully.', 'success');
                                } else {
                                    showToast('Error', data.message || 'Failed to save content.', 'danger');
                                }
                            } catch (err) {
                                showToast('Error', 'Network error while saving content.', 'danger');
                                console.error(err);
                            } finally {
                                submitBtn.innerHTML = originalText;
                                submitBtn.disabled = false;
                            }
                        });
                    });
                });

                function showToast(title, message, type = 'success') {
                    let container = document.getElementById('toast-container');
                    if (!container) {
                        container = document.createElement('div');
                        container.id = 'toast-container';
                        container.style.position = 'fixed';
                        container.style.bottom = '20px';
                        container.style.right = '20px';
                        container.style.zIndex = '9999';
                        document.body.appendChild(container);
                    }

                    const toast = document.createElement('div');
                    toast.className = `toast align-items-center text-white bg-${type} border-0 show`;
                    toast.setAttribute('role', 'alert');
                    toast.setAttribute('aria-live', 'assertive');
                    toast.setAttribute('aria-atomic', 'true');
                    toast.style.marginBottom = '10px';

                    toast.innerHTML = `
                  <div class="d-flex">
                    <div class="toast-body">
                      <strong>${title}</strong>: ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close" onclick="this.closest('.toast').remove()"></button>
                  </div>
                `;

                    container.appendChild(toast);

                    setTimeout(() => {
                        if (toast.parentNode) {
                            toast.classList.remove('show');
                            setTimeout(() => toast.remove(), 300);
                        }
                    }, 4000);
                }
            
