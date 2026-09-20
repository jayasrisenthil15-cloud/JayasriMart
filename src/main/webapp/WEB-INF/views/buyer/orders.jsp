<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="My Orders - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="orders-page">
    <div class="section-heading mb-4">
        <h1 class="section-title">My Orders</h1>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-sm">Explore Catalog</a>
    </div>

    <c:choose>
        <c:when test="${not empty orders}">
            <div class="orders-list">
                <c:forEach var="ord" items="${orders}">
                    <div class="order-card card mb-3">
                        <div class="order-card-header d-flex justify-between align-center" style="border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem; margin-bottom: 1rem; flex-wrap: wrap; gap: 0.5rem;">
                            <div>
                                <span class="font-bold" style="font-size: 1.05rem;">#ORD-${ord.id}</span>
                                <span class="text-muted" style="font-size: 0.85rem; margin-left: 0.5rem;">
                                    Placed on <fmt:formatDate value="${ord.createdAt}" pattern="dd MMM yyyy, hh:mm a" />
                                </span>
                            </div>

                            <div>
                                <span class="order-status-badge status-${fn:toLowerCase(ord.status)}">
                                    <c:out value="${ord.status}" />
                                </span>
                            </div>
                        </div>

                        <div class="order-card-body d-flex justify-between align-center" style="flex-wrap: wrap; gap: 1rem;">
                            <div class="order-items-preview" style="max-width: 60%;">
                                <c:forEach var="item" items="${ord.items}" varStatus="loop">
                                    <div class="text-truncate mb-1" style="font-size: 0.9rem;">
                                        <strong><c:out value="${item.quantity}" />&times;</strong> <c:out value="${item.productName}" />
                                        <span class="text-muted">(by <c:out value="${item.sellerName}" />)</span>
                                    </div>
                                </c:forEach>
                            </div>

                            <div class="text-right">
                                <div class="text-muted" style="font-size: 0.85rem;">Total Amount</div>
                                <div class="font-bold" style="font-size: 1.25rem; color: var(--secondary);">
                                    ₹<fmt:formatNumber value="${ord.totalAmount}" pattern="#,##0.00" />
                                </div>
                                <div class="text-muted" style="font-size: 0.8rem; margin-bottom: 0.75rem;">
                                    via <c:out value="${ord.paymentMethod}" />
                                </div>

                                <a href="${pageContext.request.contextPath}/orders/detail?id=${ord.id}" class="btn btn-primary btn-sm">
                                    View Details &rarr;
                                </a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>

        <%-- Empty State --%>
        <c:otherwise>
            <div class="empty-state-card">
                <div style="font-size: 3.5rem; margin-bottom: 1rem;">📦</div>
                <h2>No Orders Yet</h2>
                <p class="text-muted" style="max-width: 480px; margin: 0.5rem auto 1.5rem;">
                    You haven't placed any orders yet. Discover trending products and enjoy smooth simulated checkout!
                </p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg">
                    Start Shopping
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
