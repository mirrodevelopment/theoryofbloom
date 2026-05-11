/* ====================================================
   THEORY OF BLOOM — Universal Header JS v4.0
   ==================================================== */
(function() {

  /* ── Auto-hide header on scroll ── */
  const desktopHdr = document.getElementById('tobHeader');
  const mobileHdr  = document.getElementById('tobMobileHeader');
  let lastY    = 0;
  let ticking  = false;

  window.addEventListener('scroll', function () {
    if (!ticking) {
      window.requestAnimationFrame(function () {
        const currentY     = window.scrollY;
        const scrollingDown = currentY > lastY;
        if (currentY > 80) {
          if (scrollingDown) {
            if (desktopHdr) desktopHdr.classList.add('tob-header-hidden');
            if (mobileHdr)  mobileHdr.classList.add('tob-header-hidden');
          } else {
            if (desktopHdr) desktopHdr.classList.remove('tob-header-hidden');
            if (mobileHdr)  mobileHdr.classList.remove('tob-header-hidden');
          }
        } else {
          if (desktopHdr) desktopHdr.classList.remove('tob-header-hidden');
          if (mobileHdr)  mobileHdr.classList.remove('tob-header-hidden');
        }
        if (desktopHdr) desktopHdr.classList.toggle('scrolled', currentY > 40);
        lastY   = currentY;
        ticking = false;
      });
      ticking = true;
    }
  }, { passive: true });

  /* ── Empty search guard — desktop ── */
  document.querySelectorAll('.tob-search-form').forEach(function(form) {
    form.addEventListener('submit', function(e) {
      const input = form.querySelector('input[type="text"]');
      if (input && input.value.trim() === '') {
        e.preventDefault();
        input.focus();
        input.classList.add('tob-search-shake');
        setTimeout(() => input.classList.remove('tob-search-shake'), 500);
      }
    });
  });

  /* ── Empty search guard — mobile ── */
  document.querySelectorAll('.tob-mob-search-bar form').forEach(function(form) {
    form.addEventListener('submit', function(e) {
      const input = form.querySelector('input[type="text"]');
      if (input && input.value.trim() === '') {
        e.preventDefault();
        input.focus();
        input.classList.add('tob-search-shake');
        setTimeout(() => input.classList.remove('tob-search-shake'), 500);
      }
    });
  });

  /* ═══════════════════════════════════════════════════════════
     SIDE DRAWER — inject backdrop + drawer header, then open/close
  ═══════════════════════════════════════════════════════════ */

  /* ── 1. Create & append global backdrop (once) ── */
  let backdrop = document.getElementById('tobOverlayBackdrop');
  if (!backdrop) {
    backdrop = document.createElement('div');
    backdrop.id        = 'tobOverlayBackdrop';
    backdrop.className = 'tob-overlay-backdrop';
    document.body.appendChild(backdrop);
  }

  /* ── 2. Enhance each tobOverlay with the new drawer header + icon nav ── */
  document.querySelectorAll('#tobOverlay').forEach(function(overlay) {

    /* Skip if already enhanced */
    if (overlay.dataset.drawerReady === 'true') return;
    overlay.dataset.drawerReady = 'true';

    /* ── Resolve user name from existing Thymeleaf-rendered acc element ── */
    let userName = 'Guest';
    let userSub  = 'Browse our collection';
    let userInitial = '✦';

    /* Look for an already-rendered acc-name in the overlay (Thymeleaf output) */
    const accNameEl = overlay.querySelector('.tob-overlay-acc-name');
    if (accNameEl && accNameEl.textContent.trim() && accNameEl.textContent.trim() !== 'User Name') {
      userName    = accNameEl.textContent.trim();
      userInitial = userName.charAt(0).toUpperCase();
      userSub     = 'Botanical Ritual Member';
    }

    /* ── Build drawer header HTML ── */
    const drawerHeader = document.createElement('div');
    drawerHeader.className = 'tob-drawer-header';
    drawerHeader.innerHTML = `
      <div class="tob-drawer-user">
        <div class="tob-drawer-avatar" aria-hidden="true">${userInitial}</div>
        <div class="tob-drawer-identity">
          <div class="tob-drawer-name">${userName}</div>
          <div class="tob-drawer-sub">${userSub}</div>
        </div>
      </div>
      <button class="tob-overlay-close" onclick="tobCloseMenu()" aria-label="Close menu">&#x2715;</button>
    `;

    /* ── Build brand strip ── */
    const brandStrip = document.createElement('div');
    brandStrip.className = 'tob-drawer-brand-strip';
    /* Try to clone the logo img from the mobile header */
    const logoSrc = (document.querySelector('.tob-mob-logo-img') || {}).src || '';
    brandStrip.innerHTML = `
      ${logoSrc ? `<img src="${logoSrc}" alt="Theory of Bloom">` : ''}
      <span class="tob-drawer-brand-label">Theory <em>of</em> Bloom</span>
    `;

    /* ── Prepend header + brand strip before the nav list ── */
    overlay.prepend(brandStrip);
    overlay.prepend(drawerHeader);

    /* Remove any old absolute close button that was already in the HTML
       (the new one is inside drawerHeader) */
    overlay.querySelectorAll(':scope > .tob-overlay-close').forEach(b => b.remove());

    /* ── Enhance nav links with icon + label wrapper ── */
    const NAV_ICONS = {
      '/':              'bi bi-house',
      '/shop':          'bi bi-bag',
      '/saved-blends':  'bi bi-heart',
      '/profile':       'bi bi-person-circle',
      '/contact':       'bi bi-envelope',
      '/order-history': 'bi bi-box-seam',
      '/membership':    'bi bi-stars',
      '/cart':          'bi bi-cart3',
      '/blog':          'bi bi-journal-text',
    };

    overlay.querySelectorAll('.tob-overlay-links a').forEach(function(a) {
      /* Already enhanced */
      if (a.querySelector('.nav-icon')) return;

      const href = (a.getAttribute('href') || '').replace(/\?.*$/, '').toLowerCase();

      /* Find the best matching icon */
      let iconClass = 'bi bi-arrow-right-short';
      for (const [path, cls] of Object.entries(NAV_ICONS)) {
        if (href === path || href.startsWith(path + '?') || href.startsWith(path + '#')) {
          iconClass = cls;
          break;
        }
      }

      /* Wrap existing text */
      const text = a.textContent.trim();
      a.innerHTML = `
        <span class="nav-icon" aria-hidden="true"><i class="${iconClass}"></i></span>
        <span class="nav-label"><span>${text}</span></span>
      `;

      if (href === '/shop') {
        a.classList.add('shop-blends-toggle');
        a.href = '#';
        a.addEventListener('click', function(e) {
          e.preventDefault();
          const sub = a.nextElementSibling;
          if (sub && sub.classList.contains('shop-sub-menu')) {
            const isOpen = sub.style.maxHeight && sub.style.maxHeight !== '0px';
            if (isOpen) {
              sub.style.maxHeight = null;
              sub.classList.remove('open');
              a.classList.remove('expanded');
            } else {
              sub.classList.add('open');
              a.classList.add('expanded');
              sub.style.maxHeight = sub.scrollHeight + 'px';
            }
          }
        });
        
        // Build sub-menu dynamically
        const subMenu = document.createElement('div');
        subMenu.className = 'shop-sub-menu';
        
        let subMenuHtml = '<a href="/shop" class="shop-sub-link">All Blends</a>';
        
        // Extract from footer first (sync)
        const footerLinks = document.querySelectorAll('.footer-links a');
        let foundLinks = false;
        footerLinks.forEach(fl => {
            if (fl.href && fl.href.includes('/shop?category=')) {
                if (fl.textContent.trim().toLowerCase() !== 'all') {
                    subMenuHtml += `<a href="${fl.href}" class="shop-sub-link">${fl.textContent}</a>`;
                    foundLinks = true;
                }
            }
        });
        
        if (foundLinks) {
            subMenu.innerHTML = subMenuHtml;
            a.parentNode.insertBefore(subMenu, a.nextSibling);
        } else {
            // Fallback to fetch if no footer
            fetch('/api/filters')
                .then(res => res.json())
                .then(data => {
                    if (Array.isArray(data)) {
                        data.forEach(cat => {
                            if (cat.trim().toLowerCase() !== 'all') {
                                subMenuHtml += `<a href="/shop?category=${encodeURIComponent(cat)}" class="shop-sub-link">${cat}</a>`;
                            }
                        });
                    }
                    subMenu.innerHTML = subMenuHtml;
                    a.parentNode.insertBefore(subMenu, a.nextSibling);
                })
                .catch(() => {
                    subMenu.innerHTML = subMenuHtml;
                    a.parentNode.insertBefore(subMenu, a.nextSibling);
                });
        }
      }
    });
  });

  /* ── 3. Open / close drawer ── */
  window.tobOpenMenu = function() {
    const overlay  = document.getElementById('tobOverlay');
    const bd       = document.getElementById('tobOverlayBackdrop');
    const ham      = document.querySelector('.tob-mob-ham-btn');
    if (overlay) overlay.classList.add('active');
    if (bd)      bd.classList.add('active');
    if (ham)     ham.classList.add('open');
    document.body.style.overflow = 'hidden';
  };

  window.tobCloseMenu = function() {
    const overlay  = document.getElementById('tobOverlay');
    const bd       = document.getElementById('tobOverlayBackdrop');
    const ham      = document.querySelector('.tob-mob-ham-btn');
    if (overlay) overlay.classList.remove('active');
    if (bd)      bd.classList.remove('active');
    if (ham)     ham.classList.remove('open');
    document.body.style.overflow = '';
  };

  /* ── Ripple effect on overlay menu links ── */
  document.addEventListener('click', function(e) {
    const link = e.target.closest('.tob-overlay-links a');
    if (link) {
      const ripple  = document.createElement('span');
      ripple.className = 'tob-link-ripple';
      const rect    = link.getBoundingClientRect();
      ripple.style.cssText = `left:${e.clientX - rect.left}px;top:${e.clientY - rect.top}px`;
      link.appendChild(ripple);
      setTimeout(() => ripple.remove(), 700);
    }
  });

  /* ── Mobile search slide-down ── */
  window.tobToggleSearch = function() {
    const bar = document.getElementById('tobMobSearch');
    if (!bar) return;
    bar.classList.toggle('open');
    if (bar.classList.contains('open')) {
      const inp = bar.querySelector('input');
      if (inp) setTimeout(() => inp.focus(), 100);
    }
  };

  /* ── Mobile account side-drawer ── */
  window.tobToggleAccDrop = function(e) {
    if (e) e.stopPropagation();
    const drawer   = document.getElementById('tobMobDrawer');
    const bdEl     = document.getElementById('tobMobBackdrop');
    if (!drawer || !bdEl) return;
    drawer.classList.toggle('active');
    bdEl.classList.toggle('active');
    document.body.style.overflow = drawer.classList.contains('active') ? 'hidden' : '';
  };

  window.tobCloseDrawer = function() {
    const drawer = document.getElementById('tobMobDrawer');
    const bdEl   = document.getElementById('tobMobBackdrop');
    if (drawer) drawer.classList.remove('active');
    if (bdEl)   bdEl.classList.remove('active');
    document.body.style.overflow = '';
  };

  /* ── General close handlers ── */
  document.addEventListener('click', function(e) {
    if (e.target.id === 'tobMobBackdrop')    window.tobCloseDrawer();
    if (e.target.id === 'tobOverlayBackdrop') window.tobCloseMenu();
  });

  /* ── Escape key closes overlay ── */
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
      window.tobCloseMenu();
      window.tobCloseDrawer();
    }
  });

})();








// 3D Spinner Category Filter — Merged with VisionProWheel logic
document.addEventListener('DOMContentLoaded', () => {
    const spinnerWraps = document.querySelectorAll('.tob-spinner-wrap');

    spinnerWraps.forEach(wrap => {
        const ring = wrap.querySelector('.tob-spinner-ring');
        if (!ring) return;

        let currentAngle = 0;
        let isDragging = false;
        let previousX = 0;
        let totalDeltaX = 0;
        let autoSpinInterval;

        /* ── Collect static items already in the DOM ── */
        let staticItems = Array.from(ring.querySelectorAll('.tob-spinner-item'));

        /* ── Prevent drag on links ── */
        ring.querySelectorAll('a').forEach(link => {
            link.addEventListener('dragstart', e => e.preventDefault());
        });

        /* ── Dynamically rebuild ring from /api/filters (optional) ── */
        function buildRingFromData(categories) {
            ring.innerHTML = '';
            const angleStep = 360 / categories.length;
            ring.style.setProperty('--angle-step', angleStep + 'deg');
            categories.forEach((cat, i) => {
                const a = document.createElement('a');
                a.href = `/shop?category=${encodeURIComponent(cat)}`;
                a.className = 'tob-spinner-item';
                a.style.cssText = `--i:${i}`;
                a.textContent = cat;
                a.addEventListener('dragstart', e => e.preventDefault());
                ring.appendChild(a);
            });
            /* re-collect after rebuild */
            staticItems = Array.from(ring.querySelectorAll('.tob-spinner-item'));
            updateItems(currentAngle);
        }

        /* ── Fetch categories from Spring Boot; fall back to DOM items ── */
        fetch('/api/filters')
            .then(res => {
                if (!res.ok) throw new Error('no api');
                return res.json();
            })
            .then(data => {
                if (Array.isArray(data) && data.length) buildRingFromData(data);
            })
            .catch(() => {
                /* Keep the static HTML items — no rebuild needed */
            });

        /* ── Helper: get active item label at current snap angle ── */
        function getActiveLabel() {
            const items = ring.querySelectorAll('.tob-spinner-item');
            const count = items.length;
            if (!count) return null;
            const angleStep = 360 / count;
            const index = (((Math.round(-currentAngle / angleStep) % count) + count) % count);
            return items[index] ? (items[index].dataset.cat || items[index].textContent.trim()) : null;
        }

        /* ── Call product filter API after snap ── */
        function notifyFilter(label) {
            if (!label) return;
            fetch(`/api/products?filter=${encodeURIComponent(label)}`)
                .then(res => res.json())
                .then(data => {
                    /* Dispatch a custom event so page-level code can react */
                    document.dispatchEvent(new CustomEvent('tob:filter', { detail: { filter: label, products: data } }));
                })
                .catch(() => {/* silent – page continues working without filter data */});
        }

        /* ── Update item visual state ── */
        function updateItems(angle) {
            const items = ring.querySelectorAll('.tob-spinner-item');
            const angleStep = items.length ? 360 / items.length : 60;

            items.forEach((item, index) => {
                let itemAngle = index * angleStep;
                let relative = (itemAngle + angle) % 360;
                if (relative < 0) relative += 360;

                const center = relative < 20 || relative > 340;
                const depth  = Math.cos((relative * Math.PI) / 180);

                let opacity    = center ? 1 : 0.5;
                let filter     = center ? 'blur(0px)' : 'blur(3px)';
                let scale      = center ? 1.15 : 0.9;
                let visibility = 'visible';

                if (relative > 110 && relative < 250) {
                    opacity    = 0;
                    visibility = 'hidden';
                }

                item.style.opacity      = opacity;
                item.style.visibility   = visibility;
                item.style.pointerEvents = center ? 'auto' : 'none';
                item.style.filter       = filter;
                item.style.boxShadow    = `inset 0 2px 6px rgba(255,255,255,0.7), 0 ${15 * depth}px ${30 * depth}px rgba(0,0,0,0.15)`;
                item.style.setProperty('--scale', scale);

                if (center) {
                    item.style.color           = '#3b2f24';
                    item.style.backgroundColor = 'rgba(255,255,255,0.6)';
                } else {
                    item.style.color           = '#8a8175';
                    item.style.backgroundColor = 'rgba(255,255,255,0.3)';
                }
            });

            ring.querySelectorAll('.tob-spinner-bullet').forEach(b => b.style.display = 'none');
        }

        /* ── Snap to nearest step ── */
        function getAngleStep() {
            const count = ring.querySelectorAll('.tob-spinner-item').length;
            return count ? 360 / count : 60;
        }

        function snapToNearest(duration) {
            duration = duration || 400;
            const step      = getAngleStep();
            const snapAngle = Math.round(currentAngle / step) * step;
            ring.style.transition = `transform ${duration}ms cubic-bezier(0.25,1,0.5,1)`;
            currentAngle = snapAngle;
            ring.style.transform  = `rotateY(${currentAngle}deg)`;

            let startTime = null;
            function animateFade(ts) {
                if (!startTime) startTime = ts;
                updateItems(currentAngle);
                if (ts - startTime < duration) requestAnimationFrame(animateFade);
            }
            requestAnimationFrame(animateFade);

            setTimeout(() => {
                ring.style.transition = 'none';
                updateItems(currentAngle);

                /* ── Vibration (VisionPro behaviour) ── */
                if (navigator.vibrate) navigator.vibrate(8);

                /* ── Notify product filter ── */
                notifyFilter(getActiveLabel());
            }, duration);
        }

        /* ── Auto-spin ── */
        function autoAdvance() {
            if (!isDragging) {
                currentAngle -= getAngleStep();
                snapToNearest(800);
            }
        }

        function startAutoSpin() {
            clearInterval(autoSpinInterval);
            autoSpinInterval = setInterval(autoAdvance, 3000);
        }

        /* ================================================================
           POINTER EVENTS (mouse + stylus + most touch)
        ================================================================ */
        let pointerStartX = 0;

        wrap.addEventListener('pointerdown', e => {
            isDragging    = true;
            pointerStartX = e.clientX;
            previousX     = e.clientX;
            totalDeltaX   = 0;
            ring.style.transition = 'none';
            clearInterval(autoSpinInterval);
        });

        window.addEventListener('pointermove', e => {
            if (!isDragging) return;
            const deltaX = e.clientX - previousX;
            previousX    = e.clientX;
            totalDeltaX += Math.abs(deltaX);

            const drag = e.clientX - pointerStartX;
            ring.style.transform = `rotateY(${currentAngle + drag * 0.3}deg)`;
            updateItems(currentAngle + drag * 0.3);
        });

        const handlePointerUp = e => {
            if (!isDragging) return;
            isDragging = false;

            const diff = e.clientX - pointerStartX;
            if (Math.abs(diff) > 50) {
                currentAngle += (diff > 0 ? 1 : -1) * getAngleStep();
                try {
                    new Audio('https://www.soundjay.com/buttons/sounds/button-16.mp3').play();
                } catch(_) {}
            }

            snapToNearest(400);
            startAutoSpin();
        };

        window.addEventListener('pointerup', handlePointerUp);
        window.addEventListener('pointercancel', handlePointerUp);

        /* ================================================================
           TOUCH EVENTS (explicit — mirrors VisionProWheel React component)
           Needed on iOS Safari where pointer events can be unreliable
        ================================================================ */
        let touchStartX  = 0;
        let touchVelocity = 0;
        let touchPreviousX = 0;

        wrap.addEventListener('touchstart', e => {
            touchStartX  = e.touches[0].clientX;
            touchPreviousX = e.touches[0].clientX;
            touchVelocity = 0;
            totalDeltaX = 0;
            ring.style.transition = 'none';
            clearInterval(autoSpinInterval);
        }, { passive: true });

        wrap.addEventListener('touchmove', e => {
            const currentX = e.touches[0].clientX;
            const stepDelta = currentX - touchPreviousX;
            touchPreviousX = currentX;
            totalDeltaX += Math.abs(stepDelta);

            const delta = currentX - touchStartX;
            touchVelocity = delta;
            ring.style.transform = `rotateY(${currentAngle + delta * 0.35}deg)`;
            updateItems(currentAngle + delta * 0.35);
        }, { passive: true });

        wrap.addEventListener('touchend', () => {
            const step  = getAngleStep();
            let snap    = currentAngle + touchVelocity * 0.6;
            snap        = Math.round(snap / step) * step;
            currentAngle = snap;
            ring.style.transition = `transform 400ms cubic-bezier(.25,.8,.25,1)`;
            ring.style.transform  = `rotateY(${snap}deg)`;

            setTimeout(() => {
                ring.style.transition = 'none';
                updateItems(currentAngle);
                if (navigator.vibrate) navigator.vibrate(8);
                notifyFilter(getActiveLabel());
            }, 400);

            startAutoSpin();
        }, { passive: true });

        /* ── Suppress click during drag ── */
        wrap.addEventListener('click', e => {
            if (totalDeltaX > 5) {
                e.preventDefault();
                e.stopPropagation();
            }
        }, true);

        /* ── Initialise ── */
        updateItems(0);
        startAutoSpin();
    });
});
