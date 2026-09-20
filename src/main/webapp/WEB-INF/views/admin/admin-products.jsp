<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Global Product Moderation - JayasriMart Admin" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="admin-products-page">
    <div class="section-heading d-flex justify-between align-center mb-4" style="flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 class="section-title">Global Product Moderation</h1>
            <p class="text-muted">Oversee all vendor products, moderate catalog listings, and toggle marketplace visibility.</p>
        </div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline">
            &larr; Back to Dashboard
        </a>
    </div>

    <div class="card">
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Product Details</th>
                        <th>Seller Store</th>
                        <th>Category</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Status</th>
                        <th class="text-right">Moderation Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${products}">
                        <tr>
                            <td>
                                <div class="d-flex align-center" style="gap: 0.85rem;">
                                    <img src="${not empty p.imageUrl ? p.imageUrl : 'https://placehold.co/60x60/e2e8f0/1e293b?text=Item'}"
                                         alt="<c:out value='${p.name}' />"
                                         style="width: 44px; height: 44px; object-fit: cover; border-radius: 6px; border: 1px solid var(--border-color);" />
                                    <div>
                                        <div class="font-bold"><c:out value="${p.name}" /></div>
                                        <div class="font-sm text-muted">#PRD-${p.id} &bull; ★ <fmt:formatNumber value="${p.avgRating}" pattern="0.0" /></div>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <div><strong><c:out value="${not empty p.sellerName ? p.sellerName : ('Seller #' + p.sellerId)}" /></strong></div>
                            </td>
                            <td>
                                <span class="badge" style="background: #f1f5f9;"><c:out value="${p.category}" /></span>
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
                                        <span class="badge" style="background: #fef3c7; color: #d97706; font-weight: 700;">${p.stockQty} left</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge" style="background: #dcfce7; color: #15803d;">${p.stockQty} in stock</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.active}">
                                        <span class="badge" style="background: #dbeafe; color: #1d4ed8; font-weight: 600;">Active</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge" style="background: #fee2e2; color: #b91c1c; font-weight: 600;">Unlisted / Hidden</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-right">
                                <div class="d-flex justify-end align-center" style="gap: 0.5rem;">
                                    <a href="${pageContext.request.contextPath}/product?id=${p.id}" target="_blank" class="btn btn-outline btn-sm">
                                        👁️ View
                                    </a>
                                    <form action="${pageContext.request.contextPath}/admin/products/toggle" method="POST" style="display:inline;">
                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                        <input type="hidden" name="id" value="${p.id}" />
                                        <button type="submit" class="btn btn-outline btn-sm" style="${p.active ? 'color: #d97706; border-color: #fde68a;' : 'color: #15803d; border-color: #bbf7d0;'}">
                                            <c:out value="${p.active ? 'Hide / Unlist' : 'Publish / List'}" />
                                        </button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/admin/products/delete"
                                          method="POST"
                                          style="display:inline;"
                                          onsubmit="return confirm('Permanently delete product #${p.id} from marketplace catalog?');">
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
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
