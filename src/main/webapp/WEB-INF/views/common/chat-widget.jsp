<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- JayasriMart AI Shopping Assistant Floating Widget -->
<div id="jayabot-widget" class="jayabot-container">
    <!-- Floating Trigger Button -->
    <button id="jayabot-trigger" class="jayabot-trigger-btn" aria-label="Open AI Shopping Assistant">
        <span class="jayabot-icon">🤖</span>
        <span class="jayabot-label">AI Assistant</span>
    </button>

    <!-- Chat Modal Window -->
    <div id="jayabot-modal" class="jayabot-modal" style="display: none;">
        <!-- Header -->
        <div class="jayabot-header">
            <div class="d-flex align-center" style="gap: 0.6rem;">
                <div class="jayabot-avatar">🤖</div>
                <div>
                    <div class="font-bold" style="font-size: 0.95rem; color: #fff;">JayaBot</div>
                    <div class="font-sm" style="color: #a7f3d0; font-size: 0.75rem; display: flex; align-items: center; gap: 0.25rem;">
                        <span style="width: 7px; height: 7px; background: #10b981; border-radius: 50%; display: inline-block;"></span>
                        AI Shopping Assistant (Active)
                    </div>
                </div>
            </div>
            <button id="jayabot-close" class="jayabot-close-btn" aria-label="Close Chat">&times;</button>
        </div>

        <!-- Messages Area -->
        <div id="jayabot-messages" class="jayabot-messages">
            <div class="chat-bubble bot-bubble">
                👋 Hi there! I'm <strong>JayaBot</strong>, your AI shopping assistant. Ask me about products, category recommendations, delivery charges, or store policies!
            </div>
        </div>

        <!-- Quick Prompt Suggestions -->
        <div class="jayabot-quick-prompts">
            <button type="button" class="quick-prompt-btn" data-prompt="What are the delivery charges?">🚚 Delivery Info</button>
            <button type="button" class="quick-prompt-btn" data-prompt="What payment methods do you accept?">💳 Payments</button>
            <button type="button" class="quick-prompt-btn" data-prompt="Show me trending fashion shirts">👕 Fashion</button>
            <button type="button" class="quick-prompt-btn" data-prompt="Show top electronics">📱 Electronics</button>
        </div>

        <!-- Input Box -->
        <form id="jayabot-form" class="jayabot-input-area" data-context-path="${pageContext.request.contextPath}">
            <input type="text"
                   id="jayabot-input"
                   placeholder="Ask JayaBot anything..."
                   autocomplete="off"
                   required />
            <button type="submit" id="jayabot-send-btn" aria-label="Send Message">
                ➤
            </button>
        </form>
    </div>
</div>
