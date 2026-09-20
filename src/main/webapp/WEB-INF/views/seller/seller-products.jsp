<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="My Products - JayasriMart Seller" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="seller-products-page">
    <div class="section-heading d-flex justify-between align-center mb-4" style="flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 class="section-title">Store Products Catalog</h1>
            <p class="text-muted">Manage your inventory, prices, product descriptions, and catalog availability.</p>
        </div>
        <a href="${pageContext.request.contextPath}/seller/products/new" class="btn btn-primary">
            + Add New Product
        </a>
    </div>

    <c:choose>
        <c:when test="${not empty products}">
            <div class="card">
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Product Details</th>
                                <th>Category</th>
                                <th>Price</th>
                                <th>Stock</th>
                                <th>Rating</th>
                                <th>Status</th>
                                <th class="text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${products}">
                                <tr>
                                    <td>
                                        <div class="d-flex align-center" style="gap: 0.85rem;">
                                            <img src="${not empty p.imageUrl ? p.imageUrl : 'https://placehold.co/60x60/e2e8f0/1e293b?text=Item'}"
                                                 alt="<c:out value='${p.name}' />"
                                                 style="width: 48px; height: 48px; object-fit: cover; border-radius: 8px; border: 1px solid var(--border-color);" />
                                            <div>
                                                <div class="font-bold"><c:out value="${p.name}" /></div>
                                                <div class="font-sm text-muted">SKU: #PRD-${p.id}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge" style="background: #f1f5f9; color: var(--text-color);">
                                            <c:out value="${p.category}" />
                                        </span>
                                    </td>
                                    <td>
                                        <span class="font-bold">₹<fmt:formatNumber value="${p.price}" pattern="#,##0.00" /></span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.stockQty <= 0}">
                                                <span class="badge" style="background: #fee2e2; color: #dc2626; font-weight: 700;">Out of Stock</span>
                                            </c:when>
                                            <c:when test="${p.stockQty <= 5}">
                                                <span class="badge" style="background: #fef3c7; color: #d97706; font-weight: 700;">Low Stock (${p.stockQty})</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background: #dcfce7; color: #15803d; font-weight: 600;">${p.stockQty} in stock</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div style="color: #f59e0b; font-weight: 600; font-size: 0.9rem;">
                                            ★ <fmt:formatNumber value="${p.avgRating}" pattern="0.0" />
                                        </div>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.active}">
                                                <span class="badge" style="background: #e0f2fe; color: #0369a1; font-weight: 600;">Published</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background: #f1f5f9; color: #64748b; font-weight: 600;">Draft / Hidden</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-right">
                                        <div class="d-flex justify-end align-center" style="gap: 0.5rem;">
                                            <a href="${pageContext.request.contextPath}/product?id=${p.id}"
                                               target="_blank"
                                               class="btn btn-outline btn-sm"
                                               title="View in Buyer Store">
                                                👁️
                                            </a>
                                            <a href="${pageContext.request.contextPath}/seller/products/edit?id=${p.id}"
                                               class="btn btn-outline btn-sm">
                                                Edit
                                            </a>
                                            <form action="${pageContext.request.contextPath}/seller/products/delete"
                                                  method="POST"
                                                  style="display: inline;"
                                                  onsubmit="return confirm('Are you sure you want to delete this product? This action cannot be undone.');">
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                                <input type="hidden" name="id" value="${p.id}" />
                                                <button type="submit" class="btn btn-outline btn-sm" style="color: #ef4444; border-color: #fca5a5;">
                                                    Delete
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state-card card text-center" style="padding: 3rem 1.5rem;">
                <div style="font-size: 3.5rem; margin-bottom: 1rem;">📦</div>
                <h2>No Products in Your Catalog</h2>
                <p class="text-muted" style="max-width: 480px; margin: 0.5rem auto 1.5rem;">
                    You haven't added any products to your seller catalog yet. Start listing products to reach thousands of buyers!
                </p>
                <a href="${pageContext.request.contextPath}/seller/products/new" class="btn btn-primary btn-lg">
                    + Add Your First Product
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
