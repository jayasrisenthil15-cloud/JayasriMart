<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Seller Dashboard - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="seller-dashboard-page">
    <div class="dashboard-header d-flex justify-between align-center mb-4" style="flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 class="section-title">Seller Hub Dashboard</h1>
            <p class="text-muted">Welcome back, <strong><c:out value="${sessionScope.currentUser.name}" /></strong>! Here's an overview of your store's sales and inventory.</p>
        </div>
        <div class="dashboard-actions d-flex" style="gap: 0.75rem;">
            <a href="${pageContext.request.contextPath}/seller/products/new" class="btn btn-primary">
                + Add New Product
            </a>
            <a href="${pageContext.request.contextPath}/seller/orders" class="btn btn-outline">
                Manage Orders
            </a>
        </div>
    </div>

    <!-- Overview Metric Cards -->
    <div class="grid grid-4 mb-4" style="gap: 1.25rem;">
        <div class="metric-card card">
            <div class="metric-icon" style="font-size: 2rem; margin-bottom: 0.5rem;">📦</div>
            <div class="text-muted font-sm font-semibold text-uppercase">Total Products</div>
            <div class="metric-value font-bold" style="font-size: 1.85rem; color: var(--text-color); margin: 0.25rem 0;">
                <c:out value="${dashboard.totalProducts}" />
            </div>
            <a href="${pageContext.request.contextPath}/seller/products" class="font-sm" style="color: var(--primary);">
                View Catalog &rarr;
            </a>
        </div>

        <div class="metric-card card">
            <div class="metric-icon" style="font-size: 2rem; margin-bottom: 0.5rem;">⚠️</div>
            <div class="text-muted font-sm font-semibold text-uppercase">Low Stock Alerts</div>
            <div class="metric-value font-bold" style="font-size: 1.85rem; color: ${dashboard.lowStockCount > 0 ? '#ef4444' : 'var(--text-color)'}; margin: 0.25rem 0;">
                <c:out value="${dashboard.lowStockCount}" />
            </div>
            <span class="font-sm text-muted">Stock &le; 5 units</span>
        </div>

        <div class="metric-card card">
            <div class="metric-icon" style="font-size: 2rem; margin-bottom: 0.5rem;">🛍️</div>
            <div class="text-muted font-sm font-semibold text-uppercase">Total Orders</div>
            <div class="metric-value font-bold" style="font-size: 1.85rem; color: var(--text-color); margin: 0.25rem 0;">
                <c:out value="${dashboard.totalOrders}" />
            </div>
            <a href="${pageContext.request.contextPath}/seller/orders" class="font-sm" style="color: var(--primary);">
                Track Orders &rarr;
            </a>
        </div>

        <div class="metric-card card">
            <div class="metric-icon" style="font-size: 2rem; margin-bottom: 0.5rem;">💰</div>
            <div class="text-muted font-sm font-semibold text-uppercase">Store Revenue</div>
            <div class="metric-value font-bold" style="font-size: 1.85rem; color: #10b981; margin: 0.25rem 0;">
                ₹<fmt:formatNumber value="${dashboard.totalRevenue}" pattern="#,##0.00" />
            </div>
            <span class="font-sm text-muted">Completed &amp; in-transit</span>
        </div>
    </div>

    <!-- Low Stock Alert Warning Box (Conditional) -->
    <c:if test="${not empty dashboard.lowStockProducts}">
        <div class="card mb-4" style="border-left: 4px solid #ef4444;">
            <div class="d-flex justify-between align-center mb-3">
                <h2 style="font-size: 1.25rem; color: #b91c1c; display: flex; align-items: center; gap: 0.5rem;">
                    <span>⚠️</span> Low Stock Inventory Attention Needed
                </h2>
                <span class="badge" style="background: #fee2e2; color: #b91c1c; font-weight: 600;">
                    <c:out value="${fn:length(dashboard.lowStockProducts)}" /> items low
                </span>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Product</th>
                            <th>Category</th>
                            <th>Price</th>
                            <th>Current Stock</th>
                            <th class="text-right">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${dashboard.lowStockProducts}">
                            <tr>
                                <td>
                                    <div class="d-flex align-center" style="gap: 0.75rem;">
                                        <img src="${not empty item.imageUrl ? item.imageUrl : 'https://placehold.co/50x50/e2e8f0/1e293b?text=Item'}"
                                             alt="<c:out value='${item.name}' />"
                                             style="width: 42px; height: 42px; object-fit: cover; border-radius: 6px;" />
                                        <div>
                                            <strong><c:out value="${item.name}" /></strong>
                                        </div>
                                    </div>
                                </td>
                                <td><span class="badge" style="background: #f1f5f9;"><c:out value="${item.category}" /></span></td>
                                <td>₹<fmt:formatNumber value="${item.price}" pattern="#,##0.00" /></td>
                                <td>
                                    <span class="badge" style="background: #fef2f2; color: #dc2626; font-weight: 700;">
                                        <c:out value="${item.stockQty}" /> left
                                    </span>
                                </td>
                                <td class="text-right">
                                    <a href="${pageContext.request.contextPath}/seller/products/edit?id=${item.id}" class="btn btn-outline btn-sm">
                                        Update Stock
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <!-- Recent Orders Section -->
    <div class="card mb-4">
        <div class="d-flex justify-between align-center mb-3">
            <h2 style="font-size: 1.25rem;">Recent Incoming Orders</h2>
            <a href="${pageContext.request.contextPath}/seller/orders" class="font-sm" style="color: var(--primary);">
                View All Orders &rarr;
            </a>
        </div>

        <c:choose>
            <c:when test="${not empty dashboard.recentOrders}">
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Order ID</th>
                                <th>Buyer Info</th>
                                <th>Date</th>
                                <th>Items</th>
                                <th>Status</th>
                                <th class="text-right">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="ord" items="${dashboard.recentOrders}">
                                <tr>
                                    <td><strong>#ORD-${ord.id}</strong></td>
                                    <td>
                                        <div><strong><c:out value="${ord.name}" /></strong></div>
                                        <div class="text-muted font-sm"><c:out value="${ord.city}" />, <c:out value="${ord.pincode}" /></div>
                                    </td>
                                    <td class="font-sm text-muted">
                                        <fmt:formatDate value="${ord.createdAt}" pattern="dd MMM, hh:mm a" />
                                    </td>
                                    <td>
                                        <c:forEach var="i" items="${ord.items}">
                                            <div class="font-sm text-truncate" style="max-width: 250px;">
                                                &bull; <c:out value="${i.quantity}" />x <c:out value="${i.productName}" />
                                            </div>
                                        </c:forEach>
                                    </td>
                                    <td>
                                        <span class="order-status-badge status-${fn:toLowerCase(ord.status)}">
                                            <c:out value="${ord.status}" />
                                        </span>
                                    </td>
                                    <td class="text-right">
                                        <a href="${pageContext.request.contextPath}/seller/orders" class="btn btn-outline btn-sm">
                                            Process Order
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-center" style="padding: 2.5rem 1rem;">
                    <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🛍️</div>
                    <p class="text-muted">No orders received yet. As soon as buyers purchase your products, they'll appear here.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
