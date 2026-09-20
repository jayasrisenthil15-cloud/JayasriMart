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

    // 4. AJAX Wishlist Toggle
    document.querySelectorAll('.wishlist-toggle-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const productId = btn.dataset.productId;
            const csrfToken = btn.dataset.csrf || document.querySelector('input[name="csrfToken"]')?.value || '';
            const contextPath = btn.dataset.contextPath || '';

            if (!productId) return;

            try {
                const formData = new URLSearchParams();
                formData.append('productId', productId);
                formData.append('csrfToken', csrfToken);

                const response = await fetch(`${contextPath}/wishlist/toggle`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest',
                        'Accept': 'application/json'
                    },
                    body: formData.toString()
                });

                if (response.ok) {
                    const result = await response.json();
                    if (result.success) {
                        const inWishlist = result.data.inWishlist;
                        btn.classList.toggle('active', inWishlist);
                        btn.setAttribute('aria-pressed', inWishlist ? 'true' : 'false');
                        btn.innerHTML = inWishlist ? '❤️' : '🤍';
                        showToast(result.message || (inWishlist ? 'Saved to wishlist!' : 'Removed from wishlist!'), 'success');

                        document.querySelectorAll('.badge-buyer').forEach(b => {
                            if (b.closest('a[href$="/wishlist"]')) {
                                b.textContent = result.data.wishlistCount;
                            }
                        });
                    }
                } else if (response.status === 401 || response.status === 403) {
                    window.location.href = `${contextPath}/login?info=Please+sign+in+to+save+items+to+your+wishlist.`;
                }
            } catch (err) {
                console.error('Error toggling wishlist item:', err);
            }
        });
    });
});

function showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `alert alert-${type === 'success' ? 'success' : 'info'}`;
    toast.style.position = 'fixed';
    toast.style.bottom = '24px';
    toast.style.right = '24px';
    toast.style.zIndex = '9999';
    toast.style.boxShadow = '0 10px 15px -3px rgba(0,0,0,0.15)';
    toast.style.padding = '0.75rem 1.25rem';
    toast.style.borderRadius = '8px';
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 2400);
}
