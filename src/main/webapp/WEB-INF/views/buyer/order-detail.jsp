<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Order #ORD-${order.id} - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="order-detail-page">
    <%-- Breadcrumbs --%>
    <nav class="breadcrumb mb-4">
        <a href="${pageContext.request.contextPath}/">Home</a> &gt;
        <a href="${pageContext.request.contextPath}/orders">My Orders</a> &gt;
        <span>#ORD-${order.id}</span>
    </nav>

    <div class="card mb-4">
        <%-- Header Info --%>
        <div class="d-flex justify-between align-center" style="border-bottom: 1px solid var(--border-color); padding-bottom: 1.25rem; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;">
            <div>
                <h1 style="font-size: 1.5rem; font-weight: 800; color: var(--secondary); margin-bottom: 0.25rem;">
                    Order #ORD-${order.id}
                </h1>
                <div class="text-muted" style="font-size: 0.875rem;">
                    Placed on <fmt:formatDate value="${order.createdAt}" pattern="dd MMMM yyyy, hh:mm a" />
                </div>
            </div>

            <div class="d-flex align-center gap-3">
                <span class="order-status-badge status-${fn:toLowerCase(order.status)}" style="font-size: 0.95rem; padding: 0.4rem 1rem;">
                    <c:out value="${order.status}" />
                </span>

                <%-- Cancellation Button for PENDING or CONFIRMED orders --%>
                <c:if test="${order.status == 'PENDING' || order.status == 'CONFIRMED'}">
                    <form action="${pageContext.request.contextPath}/orders/cancel" method="POST"
                          onsubmit="return confirm('Are you sure you want to cancel this order? Stock will be restored.');" style="margin: 0;">
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                        <input type="hidden" name="orderId" value="${order.id}">
                        <button type="submit" class="btn btn-outline btn-sm" style="color: var(--danger); border-color: var(--danger-border);">
                            Cancel Order
                        </button>
                    </form>
                </c:if>
            </div>
        </div>

        <%-- Order Lifecycle Tracker Stepper --%>
        <c:choose>
            <c:when test="${order.status != 'CANCELLED'}">
                <div class="order-tracker mb-4">
                    <div class="tracker-step <c:if test="${order.status == 'PENDING' || order.status == 'CONFIRMED' || order.status == 'SHIPPED' || order.status == 'DELIVERED'}">completed</c:if>">
                        <div class="step-circle">1</div>
                        <div class="step-label">Placed</div>
                    </div>
                    <div class="tracker-line <c:if test="${order.status == 'CONFIRMED' || order.status == 'SHIPPED' || order.status == 'DELIVERED'}">completed</c:if>"></div>
                    <div class="tracker-step <c:if test="${order.status == 'CONFIRMED' || order.status == 'SHIPPED' || order.status == 'DELIVERED'}">completed</c:if>">
                        <div class="step-circle">2</div>
                        <div class="step-label">Confirmed</div>
                    </div>
                    <div class="tracker-line <c:if test="${order.status == 'SHIPPED' || order.status == 'DELIVERED'}">completed</c:if>"></div>
                    <div class="tracker-step <c:if test="${order.status == 'SHIPPED' || order.status == 'DELIVERED'}">completed</c:if>">
                        <div class="step-circle">3</div>
                        <div class="step-label">Shipped</div>
                    </div>
                    <div class="tracker-line <c:if test="${order.status == 'DELIVERED'}">completed</c:if>"></div>
                    <div class="tracker-step <c:if test="${order.status == 'DELIVERED'}">completed</c:if>">
                        <div class="step-circle">4</div>
                        <div class="step-label">Delivered</div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-error mb-4">
                    ⚠️ This order was cancelled. Restocked into inventory.
                </div>
            </c:otherwise>
        </c:choose>

        <%-- Two Columns: Delivery & Summary --%>
        <div class="grid-2-col mb-4" style="gap: 1.5rem;">
            <div style="background: var(--bg-muted); border-radius: var(--radius-md); padding: 1.25rem;">
                <h3 style="font-size: 1rem; font-weight: 700; margin-bottom: 0.75rem;">Delivery Destination</h3>
                <div style="line-height: 1.5; font-size: 0.9rem;">
                    <strong><c:out value="${order.name}" /></strong><br>
                    📞 <c:out value="${order.phone}" /><br>
                    📍 <c:out value="${order.address}" /><br>
                    <c:out value="${order.city}" /> - <c:out value="${order.pincode}" />
                </div>
            </div>

            <div style="background: var(--bg-muted); border-radius: var(--radius-md); padding: 1.25rem;">
                <h3 style="font-size: 1rem; font-weight: 700; margin-bottom: 0.75rem;">Payment Information</h3>
                <div style="line-height: 1.5; font-size: 0.9rem;">
                    <strong>Method:</strong> <c:out value="${order.paymentMethod}" /><br>
                    <strong>Delivery Fee:</strong>
                    <c:choose>
                        <c:when test="${order.deliveryCharge > 0}">
                            ₹<fmt:formatNumber value="${order.deliveryCharge}" pattern="#,##0.00" />
                        </c:when>
                        <c:otherwise>
                            <span style="color: var(--success); font-weight: 600;">FREE</span>
                        </c:otherwise>
                    </c:choose><br>
                    <strong>Grand Total:</strong> <span class="font-bold" style="color: var(--secondary); font-size: 1.05rem;">₹<fmt:formatNumber value="${order.totalAmount}" pattern="#,##0.00" /></span>
                </div>
            </div>
        </div>

        <%-- Items Table --%>
        <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 1rem;">Items in this Order (${fn:length(order.items)})</h3>
        <div class="order-items-table">
            <c:forEach var="item" items="${order.items}">
                <div class="order-item-row d-flex justify-between align-center" style="padding: 1rem 0; border-bottom: 1px solid var(--border-color); flex-wrap: wrap; gap: 1rem;">
                    <div class="d-flex align-center gap-3" style="max-width: 60%;">
                        <c:choose>
                            <c:when test="${not empty item.productImageUrl}">
                                <img src="<c:out value='${item.productImageUrl}' />" alt="<c:out value='${item.productName}' />"
                                     style="width: 64px; height: 64px; object-fit: cover; border-radius: var(--radius-sm); background: var(--bg-muted);"
                                     onerror="this.src='https://placehold.co/64x64?text=Item';">
                            </c:when>
                            <c:otherwise>
                                <div style="width: 64px; height: 64px; display: flex; align-items: center; justify-content: center; background: var(--bg-muted); border-radius: var(--radius-sm);">📦</div>
                            </c:otherwise>
                        </c:choose>

                        <div>
                            <h4 style="font-size: 1rem; font-weight: 600; margin-bottom: 0.25rem;">
                                <a href="${pageContext.request.contextPath}/product?id=${item.productId}">
                                    <c:out value="${item.productName}" />
                                </a>
                            </h4>
                            <div class="text-muted" style="font-size: 0.85rem;">
                                Seller: <c:out value="${item.sellerName}" />
                            </div>
                            <div style="font-size: 0.85rem; margin-top: 0.25rem;">
                                Qty: ${item.quantity} &times; ₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00" />
                            </div>
                        </div>
                    </div>

                    <div class="text-right">
                        <div class="font-bold" style="font-size: 1.15rem;">
                            ₹<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00" />
                        </div>

                        <%-- Review Button if DELIVERED --%>
                        <c:if test="${order.status == 'DELIVERED'}">
                            <div class="mt-2">
                                <a href="${pageContext.request.contextPath}/reviews/new?orderId=${order.id}&productId=${item.productId}"
                                   class="btn btn-primary btn-sm" style="font-size: 0.8rem;">
                                    ✍️ Rate & Review
                                </a>
                            </div>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div class="d-flex justify-between align-center mt-4">
            <a href="${pageContext.request.contextPath}/orders" class="btn btn-outline">
                &larr; Back to My Orders
            </a>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">
                Browse More Products
            </a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
