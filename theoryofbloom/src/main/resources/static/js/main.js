// ==================== THEORY OF BLOOM - MAIN JS ====================

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {

    // 1. STICKY NAVBAR ON SCROLL
    const navbar = document.querySelector('.navbar');
    if (navbar) {
        window.addEventListener('scroll', function() {
            if (window.scrollY > 50) {
                navbar.classList.add('navbar-scrolled');
            } else {
                navbar.classList.remove('navbar-scrolled');
            }
        });
    }

    // 2. SCROLL REVEAL ANIMATION (fade-up)
    const fadeElements = document.querySelectorAll('.fade-up');
    if (fadeElements.length) {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('revealed');
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' });

        fadeElements.forEach(el => observer.observe(el));
    }

    // Optionally, add .fade-up class to sections you want to animate.
    // For simplicity, we'll add fade-up to all sections except navbar/hero
    document.querySelectorAll('section:not(.hero-section) .container, .product-card').forEach(el => {
        if (!el.classList.contains('fade-up')) el.classList.add('fade-up');
    });
    // Re-run observer after adding classes
    const newFadeElements = document.querySelectorAll('.fade-up');
    if (newFadeElements.length) {
        const newObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('revealed');
                    newObserver.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1 });
        newFadeElements.forEach(el => newObserver.observe(el));
    }

    // 3. MOBILE NAVBAR AUTO-COLLAPSE AFTER CLICK
    const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
    const navbarCollapse = document.querySelector('.navbar-collapse');
    if (navLinks.length && navbarCollapse) {
        navLinks.forEach(link => {
            link.addEventListener('click', () => {
                if (window.innerWidth < 992) {
                    const bsCollapse = new bootstrap.Collapse(navbarCollapse, { toggle: false });
                    bsCollapse.hide();
                }
            });
        });
    }

    // 4. NEWSLETTER FORM SUBMISSION (AJAX) - prevents page reload
    const newsletterForm = document.querySelector('.newsletter form');
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const emailInput = this.querySelector('input[type="email"]');
            const email = emailInput.value.trim();
            if (!email) return;

            const submitBtn = this.querySelector('button[type="submit"]');
            const originalBtnText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = 'Subscribing...';

            try {
                const response = await fetch('/subscribe', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ email: email })
                });
                const result = await response.text();
                if (response.ok) {
                    alert('Thank you! Check your inbox for 10% off.');
                    emailInput.value = '';
                } else {
                    alert('Something went wrong. Please try again.');
                }
            } catch (err) {
                alert('Network error. Please try again.');
            } finally {
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
            }
        });
    }

    // 5. PRODUCT QUANTITY INPUT (for product detail page)
    const qtyInput = document.querySelector('input[name="quantity"]');
    if (qtyInput) {
        qtyInput.addEventListener('change', function() {
            let val = parseInt(this.value);
            if (isNaN(val) || val < 1) this.value = 1;
            if (val > 99) this.value = 99;
        });
    }

    // 6. ADD TO CART BUTTON (with loading feedback)
    const addToCartForms = document.querySelectorAll('form[action*="/cart/add"]');
    addToCartForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = 'Adding...';
                setTimeout(() => {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = 'Add to Cart →';
                }, 800);
            }
        });
    });

    // 7. IMAGE FALLBACK (if product image fails to load)
    const productImages = document.querySelectorAll('.product-image');
    productImages.forEach(img => {
        img.addEventListener('error', function() {
            this.src = 'https://placehold.co/400x400/F5F0E8/4A6B5D?text=Bloom';
        });
    });

    // 8. SMOOTH SCROLL FOR ANCHOR LINKS (if any)
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                e.preventDefault();
                targetElement.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });

    // 9. ACTIVE NAVIGATION LINK HIGHLIGHT
    const currentPath = window.location.pathname;
    const navItems = document.querySelectorAll('.navbar-nav .nav-link');
    navItems.forEach(link => {
        const linkPath = link.getAttribute('href');
        if (linkPath && (currentPath === linkPath || (linkPath !== '/' && currentPath.startsWith(linkPath)))) {
            link.classList.add('active');
        }
    });

    // 10. BLOOMING FLOWERS SCROLL ANIMATION (5% Viewport rule)
    let scrollTimeout;
    window.addEventListener('scroll', function() {
        if (!scrollTimeout) {
            scrollTimeout = setTimeout(function() {
                createScrollPetal();
                // Create a couple more for a "water spray" effect
                setTimeout(createScrollPetal, 15);
                setTimeout(createScrollPetal, 30);
                scrollTimeout = null;
            }, 60); // throttle
        }
    });

    function createScrollPetal() {
        if (window.innerWidth < 768 && Math.random() > 0.5) return; // Lessen on mobile

        const petal = document.createElement('div');
        petal.className = 'scroll-petal';
        
        // Start randomly across the width
        petal.style.left = (Math.random() * 100) + 'vw';
        
        // Size around 5% viewport width as requested
        const sizeBase = Math.min(window.innerWidth, window.innerHeight) * 0.05;
        const size = (sizeBase * (Math.random() * 0.5 + 0.8)) + 'px';
        petal.style.width = size;
        petal.style.height = size;
        
        // Randomize physics
        petal.style.setProperty('--drift', (Math.random() * 200 - 100) + 'px');
        petal.style.setProperty('--spin', (Math.random() * 360) + 'deg');
        
        // Sometimes use a leaf instead of a flower for variety
        const isLeaf = Math.random() > 0.7;
        if (isLeaf) {
            petal.style.backgroundImage = "url('data:image/svg+xml;utf8,<svg viewBox=\"0 0 24 24\" fill=\"%234A6B5D\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M12 22C12 22 3 16 3 9C3 5 6 2 10 2C11.5 2 12 3 12 3C12 3 12.5 2 14 2C18 2 21 5 21 9C21 16 12 22 12 22Z\"/></svg>')";
        }

        document.body.appendChild(petal);

        // Cleanup after animation
        setTimeout(() => {
            if (petal.parentNode) petal.parentNode.removeChild(petal);
        }, 2000);
    }

});