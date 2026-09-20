package com.jayasrimart.service.ai;

import com.jayasrimart.dto.ChatMessageDTO;
import com.jayasrimart.dto.ChatResponseDTO;
import com.jayasrimart.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Robust mock/rule-based AI provider that answers FAQs, explains store policies,
 * and matches catalog items when Gemini API key is absent or offline.
 */
public class MockAiServiceProvider implements AiServiceProvider {

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ChatResponseDTO generateResponse(String userMessage, List<ChatMessageDTO> history, List<Product> catalogContext) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return new ChatResponseDTO("Hello! I am JayaBot, your AI shopping assistant. How can I help you today?");
        }

        String lower = userMessage.toLowerCase(Locale.ROOT).trim();
        List<Product> suggestions = new ArrayList<>();

        if (catalogContext != null && !catalogContext.isEmpty()) {
            for (Product p : catalogContext) {
                if (suggestions.size() < 3) {
                    suggestions.add(p);
                }
            }
        }

        // 1. Delivery & Shipping Policies
        if (lower.contains("delivery") || lower.contains("shipping") || lower.contains("charge") || lower.contains("fee")) {
            return new ChatResponseDTO(
                    "🚚 **Shipping Policy at JayasriMart:**\n"
                    + "- **Free Delivery** on orders of ₹999.00 and above!\n"
                    + "- Standard flat delivery fee of **₹50.00** on orders below ₹999.00.\n"
                    + "- Fast simulated express dispatch within 2-4 business days across India.",
                    suggestions
            );
        }

        // 2. Payment Methods
        if (lower.contains("payment") || lower.contains("pay") || lower.contains("upi") || lower.contains("card") || lower.contains("cod")) {
            return new ChatResponseDTO(
                    "💳 **Accepted Payment Methods:**\n"
                    + "1. **UPI** (Google Pay, PhonePe, Paytm with instant mock verification)\n"
                    + "2. **Credit / Debit Cards** (Visa, MasterCard, RuPay)\n"
                    + "3. **Cash on Delivery (COD)** with zero upfront payment.\n\n"
                    + "All simulated transactions are 100% secure and encrypted!",
                    suggestions
            );
        }

        // 3. Return & Refund Policy
        if (lower.contains("return") || lower.contains("refund") || lower.contains("exchange") || lower.contains("cancel")) {
            return new ChatResponseDTO(
                    "🔄 **Returns & Cancellation Policy:**\n"
                    + "- You can cancel any order directly before it reaches the **SHIPPED** status.\n"
                    + "- Upon cancellation, reserved inventory is automatically restocked.\n"
                    + "- 7-day hassle-free replacement on delivered orders if damaged or defective.",
                    suggestions
            );
        }

        // 4. Seller & Multi-Vendor Inquiries
        if (lower.contains("seller") || lower.contains("sell") || lower.contains("vendor") || lower.contains("register store")) {
            return new ChatResponseDTO(
                    "🏪 **Want to sell on JayasriMart?**\n"
                    + "- Register as a **SELLER** during sign up to unlock the Seller Hub.\n"
                    + "- List catalog products with real-time stock management, custom pricing, and dispatch tracking.\n"
                    + "- Receive real-time orders with zero marketplace listing commission!",
                    suggestions
            );
        }

        // 5. Greeting & Help
        if (lower.equals("hi") || lower.equals("hello") || lower.equals("hey") || lower.contains("help") || lower.contains("who are you")) {
            String greeting = "👋 Hello! I am **JayaBot**, your JayasriMart shopping assistant. "
                    + "I can help you search products, check delivery charges, track order policies, and recommend trending picks. "
                    + "What are you looking for today?";
            return new ChatResponseDTO(greeting, suggestions);
        }

        // 6. Product Search / Recommendations
        if (!suggestions.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Here are some great products matching your interest in **JayasriMart**:\n");
            for (Product p : suggestions) {
                sb.append("• **").append(p.getName()).append("** — ₹").append(p.getPrice())
                  .append(" (★ ").append(p.getAvgRating()).append(")\n");
            }
            sb.append("\nYou can click any card below to view details or add them directly to your cart!");
            return new ChatResponseDTO(sb.toString(), suggestions);
        }

        // 7. General Fallback
        return new ChatResponseDTO(
                "I found several exciting items in our catalog! Feel free to browse our categories like Electronics, Fashion, Groceries, and Home & Living, or ask me about delivery fees and payment methods.",
                suggestions
        );
    }
}
