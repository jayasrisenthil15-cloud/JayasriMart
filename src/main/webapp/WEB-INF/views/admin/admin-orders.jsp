<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Platform Orders Oversight - JayasriMart Admin" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="admin-orders-page">
    <div class="section-heading d-flex justify-between align-center mb-4" style="flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 class="section-title">Platform-Wide Orders Oversight</h1>
            <p class="text-muted">Monitor all customer orders, fulfillment pipelines across sellers, and execute administrative interventions.</p>
        </div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline">
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
                                <span class="font-bold" style="font-size: 1.15rem;">#ORD-${ord.id}</span>
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
                            <!-- Column 1: Customer Details -->
                            <div>
                                <div class="font-semibold text-uppercase text-muted font-sm mb-1">Customer &amp; Shipping</div>
                                <div class="font-bold"><c:out value="${ord.name}" /></div>
                                <div class="font-sm"><c:out value="${ord.buyerEmail}" /></div>
                                <div class="font-sm"><c:out value="${ord.phone}" /></div>
                                <div class="font-sm text-muted mt-1">
                                    <c:out value="${ord.address}" /><br/>
                                    <c:out value="${ord.city}" />, <c:out value="${ord.pincode}" />
                                </div>
                            </div>

                            <!-- Column 2: Ordered Items -->
                            <div>
                                <div class="font-semibold text-uppercase text-muted font-sm mb-1">Order Line Items</div>
                                <c:forEach var="item" items="${ord.items}">
                                    <div class="mb-2 font-sm">
                                        <div class="font-semibold text-truncate" style="max-width: 280px;">
                                            <c:out value="${item.quantity}" />x <c:out value="${item.productName}" />
                                        </div>
                                        <div class="text-muted">
                                            Seller: <strong><c:out value="${item.sellerName}" /></strong> &bull;
                                            ₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00" />
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>

                            <!-- Column 3: Payment & Grand Total -->
                            <div>
                                <div class="font-semibold text-uppercase text-muted font-sm mb-1">Financial Overview</div>
                                <div class="font-sm">Method: <strong><c:out value="${ord.paymentMethod}" /></strong></div>
                                <div class="font-sm text-muted">Delivery: ₹<fmt:formatNumber value="${ord.deliveryCharge}" pattern="#,##0.00" /></div>
                                <div class="font-bold mt-1" style="font-size: 1.25rem; color: #10b981;">
                                    Grand Total: ₹<fmt:formatNumber value="${ord.totalAmount}" pattern="#,##0.00" />
                                </div>
                            </div>
                        </div>

                        <!-- Admin Status Intervention Bar -->
                        <div class="order-card-footer d-flex justify-between align-center"
                             style="border-top: 1px solid var(--border-color); padding-top: 0.85rem; flex-wrap: wrap; gap: 0.75rem;">
                            <div class="font-sm text-muted">
                                Status: <strong><c:out value="${ord.status}" /></strong>
                            </div>

                            <c:if test="${ord.status != 'DELIVERED' && ord.status != 'CANCELLED'}">
                                <form action="${pageContext.request.contextPath}/admin/orders/status"
                                      method="POST"
                                      class="d-flex align-center"
                                      style="gap: 0.5rem;"
                                      onsubmit="return confirm('Update status of order #ORD-${ord.id} to ' + this.status.value + '?');">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                    <input type="hidden" name="orderId" value="${ord.id}" />
                                    <select name="status" class="form-control" style="padding: 0.3rem 0.5rem; font-size: 0.85rem; width: auto;">
                                        <option value="CONFIRMED" ${ord.status == 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                                        <option value="SHIPPED" ${ord.status == 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                                        <option value="DELIVERED">DELIVERED</option>
                                        <option value="CANCELLED">CANCELLED (Restock)</option>
                                    </select>
                                    <button type="submit" class="btn btn-outline btn-sm">
                                        Update Status
                                    </button>
                                </form>
                            </c:if>
                            <c:if test="${ord.status == 'DELIVERED'}">
                                <span class="font-sm" style="color: #10b981; font-weight: 600;">✓ Delivered Successfully</span>
                            </c:if>
                            <c:if test="${ord.status == 'CANCELLED'}">
                                <span class="font-sm text-muted">Order Cancelled &amp; Inventory Restocked</span>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state-card card text-center" style="padding: 3rem 1.5rem;">
                <div style="font-size: 3.5rem; margin-bottom: 1rem;">📦</div>
                <h2>No Orders in the System</h2>
                <p class="text-muted">As customer orders are placed across the platform, they will appear here in chronological order.</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
