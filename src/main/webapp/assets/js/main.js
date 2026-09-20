/**
 * JayasriMart Main Client-Side JavaScript
 * Pure Vanilla JavaScript (No External Dependencies)
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Mobile Menu Toggle
    const mobileToggle = document.querySelector('.mobile-toggle');
    const mobileMenu = document.querySelector('.mobile-menu');

    if (mobileToggle && mobileMenu) {
        mobileToggle.addEventListener('click', () => {
            const isOpen = mobileMenu.classList.toggle('open');
            mobileToggle.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
        });
    }

    // 2. Password Confirmation Live Match Checker
    const regForm = document.querySelector('form[action$="/register"]');
    if (regForm) {
        const passwordInput = regForm.querySelector('input[name="password"]');
        const confirmInput = regForm.querySelector('input[name="confirmPassword"]');
        const submitBtn = regForm.querySelector('button[type="submit"]');

        if (passwordInput && confirmInput) {
            const validateMatch = () => {
                if (confirmInput.value && passwordInput.value !== confirmInput.value) {
                    confirmInput.setCustomValidity('Passwords do not match');
                } else {
                    confirmInput.setCustomValidity('');
                }
            };
            passwordInput.addEventListener('input', validateMatch);
            confirmInput.addEventListener('input', validateMatch);
        }
    }

    // 3. Prevent duplicate form submissions with visual loading indicator
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function(e) {
            if (this.checkValidity()) {
                const submitButton = this.querySelector('button[type="submit"]');
                if (submitButton && !submitButton.dataset.noSpinner) {
                    submitButton.disabled = true;
                    submitButton.dataset.originalText = submitButton.innerHTML;
                    submitButton.innerHTML = '<span class="spinner"></span> Processing...';
                }
            }
        });
    });
});
