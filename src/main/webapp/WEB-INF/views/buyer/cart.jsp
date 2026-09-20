<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Shopping Cart - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="cart-page">
    <h1 class="section-title mb-4">Shopping Cart (${cart.totalItems} items)</h1>

    <c:choose>
        <c:when test="${not cart.empty}">
            <div class="cart-layout">
                <%-- Items Table / List --%>
                <div class="cart-items-container">
                    <c:forEach var="item" items="${cart.items}">
                        <div class="cart-item-card card mb-3">
                            <div class="cart-item-img-wrap">
                                <c:choose>
                                    <c:when test="${not empty item.product.imageUrl}">
                                        <img src="<c:out value='${item.product.imageUrl}' />" alt="<c:out value='${item.product.name}' />"
                                             class="cart-item-img"
                                             onerror="this.src='https://placehold.co/120x120?text=JayasriMart';">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="cart-item-placeholder">📦</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="cart-item-details">
                                <span class="badge badge-buyer" style="margin-bottom: 0.25rem;"><c:out value="${item.product.category}" /></span>
                                <h3 class="cart-item-title">
                                    <a href="${pageContext.request.contextPath}/product?id=${item.productId}">
                                        <c:out value="${item.product.name}" />
                                    </a>
                                </h3>
                                <div class="text-muted" style="font-size: 0.85rem; margin-bottom: 0.5rem;">
                                    Seller: <c:out value="${not empty item.product.sellerName ? item.product.sellerName : 'Merchant'}" />
                                </div>
                                <div class="cart-item-price">
                                    ₹<fmt:formatNumber value="${item.product.price}" pattern="#,##0.00" /> each
                                </div>
                            </div>

                            <div class="cart-item-actions">
                                <%-- Quantity Modifier Form --%>
                                <div class="d-flex align-center gap-2">
                                    <form action="${pageContext.request.contextPath}/cart/update" method="POST" style="margin: 0;">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                                        <input type="hidden" name="productId" value="${item.productId}">
                                        <input type="hidden" name="quantity" value="${item.quantity - 1}">
                                        <button type="submit" class="btn btn-outline btn-sm qty-btn" title="Decrease Quantity">-</button>
                                    </form>

                                    <span class="font-bold" style="min-width: 24px; text-align: center;">${item.quantity}</span>

                                    <form action="${pageContext.request.contextPath}/cart/update" method="POST" style="margin: 0;">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                                        <input type="hidden" name="productId" value="${item.productId}">
                                        <input type="hidden" name="quantity" value="${item.quantity + 1}">
                                        <button type="submit" class="btn btn-outline btn-sm qty-btn"
                                                <c:if test="${item.quantity >= item.product.stockQty}">disabled</c:if>
                                                title="Increase Quantity">+</button>
                                    </form>
                                </div>

                                <div class="cart-item-subtotal">
                                    ₹<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00" />
                                </div>

                                <form action="${pageContext.request.contextPath}/cart/remove" method="POST" style="margin: 0;">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                                    <input type="hidden" name="productId" value="${item.productId}">
                                    <button type="submit" class="btn btn-outline btn-sm text-muted" title="Remove Item" style="color: var(--danger); border-color: var(--danger-border);">
                                        Remove
                                    </button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <%-- Order Summary Sidebar --%>
                <div class="cart-summary-sidebar">
                    <div class="card">
                        <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1.25rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--border-color);">
                            Order Summary
                        </h2>

                        <div class="summary-line d-flex justify-between mb-2">
                            <span class="text-muted">Subtotal (${cart.totalItems} items):</span>
                            <span class="font-bold">₹<fmt:formatNumber value="${cart.subtotal}" pattern="#,##0.00" /></span>
                        </div>

                        <div class="summary-line d-flex justify-between mb-2">
                            <span class="text-muted">Delivery Fee:</span>
                            <span>
                                <c:choose>
                                    <c:when test="${cart.freeDelivery}">
                                        <strong style="color: var(--success);">FREE</strong>
                                    </c:when>
                                    <c:otherwise>
                                        ₹<fmt:formatNumber value="${cart.deliveryCharge}" pattern="#,##0.00" />
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>

                        <c:if test="${not cart.freeDelivery && cart.amountNeededForFreeDelivery > 0}">
                            <div class="alert alert-info" style="font-size: 0.8rem; padding: 0.5rem 0.75rem; margin-top: 0.5rem;">
                                💡 Add <strong>₹<fmt:formatNumber value="${cart.amountNeededForFreeDelivery}" pattern="#,##0.00" /></strong> more for <strong>FREE Delivery</strong>!
                            </div>
                        </c:if>

                        <div class="summary-total d-flex justify-between mt-3 pt-3" style="border-top: 2px dashed var(--border-color); font-size: 1.25rem;">
                            <span class="font-bold">Total Amount:</span>
                            <span class="font-bold" style="color: var(--secondary);">
                                ₹<fmt:formatNumber value="${cart.grandTotal}" pattern="#,##0.00" />
                            </span>
                        </div>

                        <a href="${pageContext.request.contextPath}/checkout" class="btn btn-primary btn-block btn-lg mt-4">
                            Proceed to Checkout &rarr;
                        </a>

                        <div class="text-center mt-3">
                            <a href="${pageContext.request.contextPath}/products" class="text-muted" style="font-size: 0.875rem;">
                                &larr; Continue Shopping
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </c:when>

        <%-- Empty Cart State --%>
        <c:otherwise>
            <div class="empty-state-card">
                <div style="font-size: 3.5rem; margin-bottom: 1rem;">🛒</div>
                <h2>Your Shopping Cart is Empty</h2>
                <p class="text-muted" style="max-width: 480px; margin: 0.5rem auto 1.5rem;">
                    Looks like you haven't added anything to your cart yet. Explore our curated catalog of fashion, electronics, books, and more!
                </p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg">
                    Start Shopping Now
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
