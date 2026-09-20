<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="System Administration - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="admin-dashboard-page">
    <div class="dashboard-header d-flex justify-between align-center mb-4" style="flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 class="section-title">Platform Administration Console</h1>
            <p class="text-muted">Master overview of marketplace health, multi-tenant users, global catalog, and total GMV.</p>
        </div>
        <div class="dashboard-actions d-flex" style="gap: 0.75rem;">
            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-outline">
                👥 Manage Users
            </a>
            <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-outline">
                🛡️ Product Moderation
            </a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-primary">
                📦 All Orders
            </a>
        </div>
    </div>

    <!-- Platform Metric Cards -->
    <div class="grid grid-4 mb-4" style="gap: 1.25rem;">
        <div class="metric-card card">
            <div class="metric-icon" style="font-size: 2rem; margin-bottom: 0.5rem;">👥</div>
            <div class="text-muted font-sm font-semibold text-uppercase">Total Users</div>
            <div class="metric-value font-bold" style="font-size: 1.85rem; color: var(--text-color); margin: 0.25rem 0;">
                <c:out value="${dashboard.totalUsers}" />
            </div>
            <div class="font-sm text-muted">
                <span style="color: #2563eb; font-weight: 600;">${dashboard.totalBuyers} Buyers</span> &bull;
                <span style="color: #7c3aed; font-weight: 600;">${dashboard.totalSellers} Sellers</span> &bull;
                <span style="color: #ea580c; font-weight: 600;">${dashboard.totalAdmins} Admins</span>
            </div>
        </div>

        <div class="metric-card card">
            <div class="metric-icon" style="font-size: 2rem; margin-bottom: 0.5rem;">📦</div>
            <div class="text-muted font-sm font-semibold text-uppercase">Catalog Products</div>
            <div class="metric-value font-bold" style="font-size: 1.85rem; color: var(--text-color); margin: 0.25rem 0;">
                <c:out value="${dashboard.totalProducts}" />
            </div>
            <a href="${pageContext.request.contextPath}/admin/products" class="font-sm" style="color: var(--primary);">
                Moderate Catalog &rarr;
            </a>
        </div>

        <div class="metric-card card">
            <div class="metric-icon" style="font-size: 2rem; margin-bottom: 0.5rem;">🛍️</div>
            <div class="text-muted font-sm font-semibold text-uppercase">Total Orders Placed</div>
            <div class="metric-value font-bold" style="font-size: 1.85rem; color: var(--text-color); margin: 0.25rem 0;">
                <c:out value="${dashboard.totalOrders}" />
            </div>
            <a href="${pageContext.request.contextPath}/admin/orders" class="font-sm" style="color: var(--primary);">
                View All Orders &rarr;
            </a>
        </div>

        <div class="metric-card card">
            <div class="metric-icon" style="font-size: 2rem; margin-bottom: 0.5rem;">💰</div>
            <div class="text-muted font-sm font-semibold text-uppercase">Gross Merchandise Value</div>
            <div class="metric-value font-bold" style="font-size: 1.85rem; color: #10b981; margin: 0.25rem 0;">
                ₹<fmt:formatNumber value="${dashboard.totalRevenue}" pattern="#,##0.00" />
            </div>
            <span class="font-sm text-muted">Platform Total GMV</span>
        </div>
    </div>

    <!-- Recent Users and Recent Orders Grids -->
    <div class="grid grid-2 mb-4" style="gap: 1.5rem;">
        <!-- Recent Users Card -->
        <div class="card">
            <div class="d-flex justify-between align-center mb-3">
                <h2 style="font-size: 1.2rem;">Recently Registered Users</h2>
                <a href="${pageContext.request.contextPath}/admin/users" class="font-sm" style="color: var(--primary);">
                    View All &rarr;
                </a>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>User</th>
                            <th>Role</th>
                            <th class="text-right">Joined</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="u" items="${dashboard.recentUsers}">
                            <tr>
                                <td>
                                    <div class="font-bold"><c:out value="${u.name}" /></div>
                                    <div class="font-sm text-muted"><c:out value="${u.email}" /></div>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${u.role == 'ADMIN'}">
                                            <span class="badge" style="background: #ffedd5; color: #c2410c; font-weight: 700;">ADMIN</span>
                                        </c:when>
                                        <c:when test="${u.role == 'SELLER'}">
                                            <span class="badge" style="background: #f3e8ff; color: #7e22ce; font-weight: 700;">SELLER</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge" style="background: #dbeafe; color: #1d4ed8; font-weight: 600;">BUYER</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-right font-sm text-muted">
                                    <fmt:formatDate value="${u.createdAt}" pattern="dd MMM" />
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Recent Orders Card -->
        <div class="card">
            <div class="d-flex justify-between align-center mb-3">
                <h2 style="font-size: 1.2rem;">Recent Marketplace Orders</h2>
                <a href="${pageContext.request.contextPath}/admin/orders" class="font-sm" style="color: var(--primary);">
                    View All &rarr;
                </a>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Order</th>
                            <th>Amount</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="ord" items="${dashboard.recentOrders}">
                            <tr>
                                <td>
                                    <div><strong>#ORD-${ord.id}</strong></div>
                                    <div class="font-sm text-muted"><c:out value="${ord.name}" /></div>
                                </td>
                                <td>
                                    <span class="font-bold">₹<fmt:formatNumber value="${ord.totalAmount}" pattern="#,##0.00" /></span>
                                </td>
                                <td>
                                    <span class="order-status-badge status-${fn:toLowerCase(ord.status)}">
                                        <c:out value="${ord.status}" />
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
