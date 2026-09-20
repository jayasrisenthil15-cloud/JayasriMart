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
    // 5. JayaBot AI Shopping Assistant Widget
    const triggerBtn = document.getElementById('jayabot-trigger');
    const modal = document.getElementById('jayabot-modal');
    const closeBtn = document.getElementById('jayabot-close');
    const chatForm = document.getElementById('jayabot-form');
    const chatInput = document.getElementById('jayabot-input');
    const messagesContainer = document.getElementById('jayabot-messages');

    if (triggerBtn && modal) {
        triggerBtn.addEventListener('click', () => {
            const isVisible = modal.style.display === 'flex';
            modal.style.display = isVisible ? 'none' : 'flex';
            if (!isVisible && chatInput) {
                chatInput.focus();
            }
        });

        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                modal.style.display = 'none';
            });
        }

        // Quick prompt buttons
        document.querySelectorAll('.quick-prompt-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const prompt = btn.dataset.prompt;
                if (prompt && chatInput) {
                    chatInput.value = prompt;
                    chatForm.dispatchEvent(new Event('submit'));
                }
            });
        });

        if (chatForm) {
            chatForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const text = chatInput.value.trim();
                if (!text) return;

                const contextPath = chatForm.dataset.contextPath || '';

                // Append user message
                appendMessage(text, 'user-bubble');
                chatInput.value = '';
                scrollToBottom();

                // Show typing dots
                const typing = showTypingIndicator();
                scrollToBottom();

                try {
                    const response = await fetch(`${contextPath}/api/v1/chat`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Accept': 'application/json'
                        },
                        body: JSON.stringify({ message: text })
                    });

                    typing.remove();

                    if (response.ok) {
                        const resData = await response.json();
                        if (resData.success && resData.data) {
                            appendBotResponse(resData.data, contextPath);
                        } else {
                            appendMessage("I'm sorry, I couldn't understand that. Please try asking again!", 'bot-bubble');
                        }
                    } else {
                        appendMessage("Sorry, I am having trouble connecting right now. Please try again later.", 'bot-bubble');
                    }
                } catch (err) {
                    console.error('AI chat error:', err);
                    typing.remove();
                    appendMessage("Network error. Please check your connection and try again.", 'bot-bubble');
                }
                scrollToBottom();
            });
        }
    }

    function appendMessage(text, className) {
        const bubble = document.createElement('div');
        bubble.className = `chat-bubble ${className}`;
        bubble.innerHTML = formatMarkdown(text);
        messagesContainer.appendChild(bubble);
    }

    function appendBotResponse(data, contextPath) {
        const bubble = document.createElement('div');
        bubble.className = 'chat-bubble bot-bubble';
        bubble.innerHTML = formatMarkdown(data.reply);

        if (data.suggestedProducts && data.suggestedProducts.length > 0) {
            const productList = document.createElement('div');
            productList.className = 'chat-products-list';
            data.suggestedProducts.forEach(p => {
                const card = document.createElement('a');
                card.className = 'chat-product-card';
                card.href = `${contextPath}/product?id=${p.id}`;
                card.innerHTML = `
                    <img src="${p.imageUrl || 'https://placehold.co/44x44/e2e8f0/1e293b?text=Item'}" alt="${escapeHtml(p.name)}" />
                    <div style="flex: 1; min-width: 0;">
                        <div style="font-weight: 600; font-size: 0.85rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${escapeHtml(p.name)}</div>
                        <div style="font-size: 0.75rem; color: #10b981; font-weight: 700;">₹${parseFloat(p.price).toFixed(2)} &bull; ★ ${p.avgRating || '0.0'}</div>
                    </div>
                `;
                productList.appendChild(card);
            });
            bubble.appendChild(productList);
        }

        messagesContainer.appendChild(bubble);
    }

    function showTypingIndicator() {
        const div = document.createElement('div');
        div.className = 'typing-indicator';
        div.innerHTML = '<span class="typing-dot"></span><span class="typing-dot"></span><span class="typing-dot"></span>';
        messagesContainer.appendChild(div);
        return div;
    }

    function scrollToBottom() {
        if (messagesContainer) {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }

    function formatMarkdown(text) {
        if (!text) return '';
        let formatted = escapeHtml(text);
        formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        formatted = formatted.replace(/\*(.*?)\*/g, '<em>$1</em>');
        formatted = formatted.replace(/\n/g, '<br/>');
        return formatted;
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
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
