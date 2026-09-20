<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Incoming Store Orders - JayasriMart Seller" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="seller-orders-page">
    <div class="section-heading d-flex justify-between align-center mb-4" style="flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 class="section-title">Incoming Customer Orders</h1>
            <p class="text-muted">Process customer orders, verify dispatch addresses, and update fulfillment progress.</p>
        </div>
        <a href="${pageContext.request.contextPath}/seller/dashboard" class="btn btn-outline">
            &larr; Back to Dashboard
        </a>
    </div>

    <c:choose>
        <c:when test="${not empty orders}">
            <div class="orders-list">
                <c:forEach var="ord" items="${orders}">
                    <div class="order-card card mb-4">
                        <div class="order-card-header d-flex justify-between align-center"
                             style="border-bottom: 1px solid var(--border-color); padding-bottom: 0.85rem; margin-bottom: 1rem; flex-wrap: wrap; gap: 0.75rem;">
                            <div>
                                <span class="font-bold" style="font-size: 1.15rem; color: var(--text-color);">
                                    #ORD-${ord.id}
                                </span>
                                <span class="text-muted font-sm" style="margin-left: 0.5rem;">
                                    Placed on <fmt:formatDate value="${ord.createdAt}" pattern="dd MMM yyyy, hh:mm a" />
                                </span>
                            </div>

                            <div class="d-flex align-center" style="gap: 0.75rem;">
                                <span class="order-status-badge status-${fn:toLowerCase(ord.status)}">
                                    <c:out value="${ord.status}" />
                                </span>
                            </div>
                        </div>

                        <div class="grid grid-3 mb-3" style="gap: 1.25rem;">
                            <!-- Column 1: Buyer & Shipping Details -->
                            <div>
                                <div class="font-semibold text-uppercase text-muted font-sm mb-1">Buyer &amp; Shipping Details</div>
                                <div class="font-bold"><c:out value="${ord.name}" /></div>
                                <div class="font-sm"><c:out value="${ord.phone}" /></div>
                                <div class="font-sm text-muted mt-1">
                                    <c:out value="${ord.address}" /><br/>
                                    <c:out value="${ord.city}" />, <c:out value="${ord.pincode}" />
                                </div>
                            </div>

                            <!-- Column 2: Items Sold by Seller in this Order -->
                            <div>
                                <div class="font-semibold text-uppercase text-muted font-sm mb-1">Your Store Items</div>
                                <c:set var="sellerSubtotal" value="0" />
                                <c:forEach var="item" items="${ord.items}">
                                    <c:if test="${item.sellerId == sessionScope.currentUser.id}">
                                        <div class="mb-2 font-sm">
                                            <div class="font-semibold text-truncate" style="max-width: 260px;">
                                                <c:out value="${item.quantity}" />x <c:out value="${item.productName}" />
                                            </div>
                                            <div class="text-muted">
                                                ₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00" /> each =
                                                <span class="font-bold" style="color: var(--text-color);">
                                                    ₹<fmt:formatNumber value="${item.unitPrice * item.quantity}" pattern="#,##0.00" />
                                                </span>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </div>

                            <!-- Column 3: Payment & Overall Total -->
                            <div>
                                <div class="font-semibold text-uppercase text-muted font-sm mb-1">Order Payment</div>
                                <div class="font-sm">Method: <strong><c:out value="${ord.paymentMethod}" /></strong></div>
                                <div class="font-sm text-muted">Order Grand Total:</div>
                                <div class="font-bold" style="font-size: 1.2rem; color: #10b981;">
                                    ₹<fmt:formatNumber value="${ord.totalAmount}" pattern="#,##0.00" />
                                </div>
                            </div>
                        </div>

                        <!-- Status Transition Action Bar -->
                        <div class="order-card-footer d-flex justify-between align-center"
                             style="border-top: 1px solid var(--border-color); padding-top: 0.85rem; flex-wrap: wrap; gap: 0.75rem;">
                            <div class="font-sm text-muted">
                                Current Status: <strong><c:out value="${ord.status}" /></strong>
                            </div>

                            <div class="order-actions d-flex align-center" style="gap: 0.5rem;">
                                <%-- 1. PENDING -> CONFIRM or CANCEL --%>
                                <c:if test="${ord.status == 'PENDING'}">
                                    <form action="${pageContext.request.contextPath}/seller/orders" method="POST" style="display:inline;">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                        <input type="hidden" name="orderId" value="${ord.id}" />
                                        <input type="hidden" name="status" value="CONFIRMED" />
                                        <button type="submit" class="btn btn-primary btn-sm">
                                            Confirm Order &rarr;
                                        </button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/seller/orders" method="POST" style="display:inline;"
                                          onsubmit="return confirm('Cancel this order? Customer stock will be restored.');">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                        <input type="hidden" name="orderId" value="${ord.id}" />
                                        <input type="hidden" name="status" value="CANCELLED" />
                                        <button type="submit" class="btn btn-outline btn-sm" style="color: #ef4444; border-color: #fca5a5;">
                                            Cancel Order
                                        </button>
                                    </form>
                                </c:if>

                                <%-- 2. CONFIRMED -> SHIP or CANCEL --%>
                                <c:if test="${ord.status == 'CONFIRMED'}">
                                    <form action="${pageContext.request.contextPath}/seller/orders" method="POST" style="display:inline;">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                        <input type="hidden" name="orderId" value="${ord.id}" />
                                        <input type="hidden" name="status" value="SHIPPED" />
                                        <button type="submit" class="btn btn-primary btn-sm" style="background: #0284c7; border-color: #0284c7;">
                                            🚚 Mark as Dispatched / Shipped
                                        </button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/seller/orders" method="POST" style="display:inline;"
                                          onsubmit="return confirm('Cancel this order? Restocks inventory.');">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                        <input type="hidden" name="orderId" value="${ord.id}" />
                                        <input type="hidden" name="status" value="CANCELLED" />
                                        <button type="submit" class="btn btn-outline btn-sm" style="color: #ef4444; border-color: #fca5a5;">
                                            Cancel
                                        </button>
                                    </form>
                                </c:if>

                                <%-- 3. SHIPPED -> DELIVER --%>
                                <c:if test="${ord.status == 'SHIPPED'}">
                                    <form action="${pageContext.request.contextPath}/seller/orders" method="POST" style="display:inline;">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                        <input type="hidden" name="orderId" value="${ord.id}" />
                                        <input type="hidden" name="status" value="DELIVERED" />
                                        <button type="submit" class="btn btn-primary btn-sm" style="background: #10b981; border-color: #10b981;">
                                            ✓ Mark as Delivered
                                        </button>
                                    </form>
                                </c:if>

                                <%-- 4. DELIVERED / CANCELLED -> Done --%>
                                <c:if test="${ord.status == 'DELIVERED'}">
                                    <span class="font-sm" style="color: #10b981; font-weight: 600;">
                                        ✓ Fulfillment Complete
                                    </span>
                                </c:if>
                                <c:if test="${ord.status == 'CANCELLED'}">
                                    <span class="font-sm text-muted">
                                        Order Cancelled
                                    </span>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state-card card text-center" style="padding: 3rem 1.5rem;">
                <div style="font-size: 3.5rem; margin-bottom: 1rem;">📦</div>
                <h2>No Orders Received Yet</h2>
                <p class="text-muted" style="max-width: 480px; margin: 0.5rem auto 1.5rem;">
                    When buyers purchase your products, the orders and fulfillment controls will appear here in real-time.
                </p>
                <a href="${pageContext.request.contextPath}/seller/products/new" class="btn btn-primary">
                    + Add More Products
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
