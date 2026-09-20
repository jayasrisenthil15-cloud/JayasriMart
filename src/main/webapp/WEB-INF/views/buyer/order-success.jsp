<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Order Confirmation - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="success-page-wrapper">
    <div class="card text-center" style="max-width: 680px; margin: 0 auto; padding: 3rem 2rem;">
        <div class="success-icon-circle">
            ✓
        </div>

        <h1 style="font-size: 1.85rem; font-weight: 800; color: var(--secondary); margin-bottom: 0.5rem;">
            Payment Successful!
        </h1>
        <p class="text-muted" style="font-size: 1.05rem; margin-bottom: 2rem;">
            Thank you for shopping with JayasriMart. Your order has been placed and confirmed with the merchants.
        </p>

        <%-- Order Key Metadata Summary --%>
        <div class="order-summary-box mb-4 text-left" style="background: var(--bg-muted); border-radius: var(--radius-md); padding: 1.5rem; text-align: left;">
            <div class="grid-2-col mb-2">
                <div>
                    <span class="text-muted" style="font-size: 0.85rem;">Order Reference:</span>
                    <div class="font-bold">#ORD-${order.id}</div>
                </div>
                <div>
                    <span class="text-muted" style="font-size: 0.85rem;">Order Date:</span>
                    <div class="font-bold"><fmt:formatDate value="${order.createdAt}" pattern="dd MMM yyyy, hh:mm a" /></div>
                </div>
            </div>

            <div class="grid-2-col mb-3">
                <div>
                    <span class="text-muted" style="font-size: 0.85rem;">Payment Method:</span>
                    <div class="font-bold"><c:out value="${order.paymentMethod}" /></div>
                </div>
                <div>
                    <span class="text-muted" style="font-size: 0.85rem;">Total Paid:</span>
                    <div class="font-bold" style="color: var(--secondary); font-size: 1.1rem;">
                        ₹<fmt:formatNumber value="${order.totalAmount}" pattern="#,##0.00" />
                    </div>
                </div>
            </div>

            <div style="border-top: 1px solid var(--border-color); padding-top: 0.75rem;">
                <span class="text-muted" style="font-size: 0.85rem;">Delivery Destination:</span>
                <div style="font-size: 0.95rem;">
                    <strong><c:out value="${order.name}" /></strong> (${order.phone})<br>
                    <c:out value="${order.address}" />, <c:out value="${order.city}" /> - <c:out value="${order.pincode}" />
                </div>
            </div>
        </div>

        <%-- Purchased Items List --%>
        <div class="text-left mb-4" style="text-align: left;">
            <h3 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 0.75rem;">Ordered Items (${fn:length(order.items)})</h3>
            <div class="checkout-items-list">
                <c:forEach var="item" items="${order.items}">
                    <div class="d-flex justify-between align-center mb-2" style="font-size: 0.9rem; padding: 0.5rem 0; border-bottom: 1px solid var(--border-color);">
                        <div>
                            <strong><c:out value="${item.productName}" /></strong>
                            <div class="text-muted" style="font-size: 0.8rem;">Qty: ${item.quantity} &times; ₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00" /></div>
                        </div>
                        <div class="font-bold">
                            ₹<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00" />
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>

        <div class="d-flex justify-center gap-3" style="justify-content: center; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}" class="btn btn-outline btn-lg">
                View Order Details
            </a>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg">
                Continue Shopping &rarr;
            </a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
