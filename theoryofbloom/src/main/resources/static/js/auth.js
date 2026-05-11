// ========== AUTH PAGES JS ==========
document.addEventListener('DOMContentLoaded', function() {

    // 1. PASSWORD TOGGLE (show/hide)
    const toggleButtons = document.querySelectorAll('.password-toggle');
    toggleButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            const input = document.getElementById(targetId);
            if (input) {
                const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                input.setAttribute('type', type);
                const icon = this.querySelector('i');
                icon.classList.toggle('fa-eye-slash');
                icon.classList.toggle('fa-eye');
            }
        });
    });

    // 2. FLOWER SPRAY EFFECT (shared function)
    function createSpray(event) {
        const flower = event.currentTarget;
        const rect = flower.getBoundingClientRect();
        const centerX = rect.left + rect.width / 2;
        const centerY = rect.top + rect.height / 2;
        const particleCount = 24;
        for (let i = 0; i < particleCount; i++) {
            const particle = document.createElement('div');
            particle.classList.add('spray-particle');
            const angle = Math.random() * Math.PI * 2;
            const distance = 50 + Math.random() * 70;
            const dx = Math.cos(angle) * distance;
            const dy = Math.sin(angle) * distance;
            particle.style.setProperty('--dx', dx + 'px');
            particle.style.setProperty('--dy', dy + 'px');
            particle.style.left = (centerX - 5) + 'px';
            particle.style.top = (centerY - 5) + 'px';
            document.body.appendChild(particle);
            setTimeout(() => particle.remove(), 600);
        }
    }

    const flowers = document.querySelectorAll('.flower-corner');
    flowers.forEach(flower => {
        flower.addEventListener('click', createSpray);
    });

    // 3. LOGIN FORM: loading state
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function() {
            const btn = document.getElementById('loginBtn');
            if (btn) {
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Signing In...';
            }
        });
    }

    // 4. REGISTER FORM: validation + loading
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        const password = document.getElementById('password');
        const confirm = document.getElementById('confirmPassword');
        const termsCheck = document.getElementById('agreeTerms');

        function validatePasswordMatch() {
            if (password && confirm && password.value !== confirm.value) {
                confirm.setCustomValidity("Passwords do not match");
                return false;
            } else if (confirm) {
                confirm.setCustomValidity("");
                return true;
            }
            return true;
        }
        if (password && confirm) {
            password.addEventListener('change', validatePasswordMatch);
            confirm.addEventListener('keyup', validatePasswordMatch);
        }

        registerForm.addEventListener('submit', function(e) {
            // Email validation
            const email = document.getElementById('email');
            const emailPattern = /^[^\s@]+@([^\s@]+\.)+[^\s@]+$/;
            if (email && !emailPattern.test(email.value)) {
                email.setCustomValidity("Please enter a valid email address");
                e.preventDefault();
                return false;
            } else if (email) {
                email.setCustomValidity("");
            }
            // Password match
            if (!validatePasswordMatch()) {
                e.preventDefault();
                if (confirm) confirm.focus();
                return false;
            }
            // Terms & Conditions
            if (termsCheck && !termsCheck.checked) {
                alert("You must agree to the Terms & Conditions to create an account.");
                e.preventDefault();
                return false;
            }
            // Loading
            const btn = document.getElementById('registerBtn');
            if (btn) {
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating Account...';
            }
        });
    }

    // 5. FORGOT PASSWORD FORM: loading
    const forgotForm = document.getElementById('forgotPasswordForm');
    if (forgotForm) {
        forgotForm.addEventListener('submit', function() {
            const btn = document.getElementById('forgotBtn');
            if (btn) {
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';
            }
        });
    }

    // 6. RESET PASSWORD FORM: validation + loading
    const resetForm = document.getElementById('resetPasswordForm');
    if (resetForm) {
        const newPass = document.getElementById('password');
        const confirmPass = document.getElementById('confirmPassword');
        function validateResetMatch() {
            if (newPass && confirmPass && newPass.value !== confirmPass.value) {
                confirmPass.setCustomValidity("Passwords do not match");
                return false;
            } else if (confirmPass) {
                confirmPass.setCustomValidity("");
                return true;
            }
            return true;
        }
        if (newPass && confirmPass) {
            newPass.addEventListener('change', validateResetMatch);
            confirmPass.addEventListener('keyup', validateResetMatch);
        }
        resetForm.addEventListener('submit', function(e) {
            if (!validateResetMatch()) {
                e.preventDefault();
                if (confirmPass) confirmPass.focus();
                return false;
            }
            const btn = document.getElementById('resetBtn');
            if (btn) {
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Resetting...';
            }
        });
    }
});