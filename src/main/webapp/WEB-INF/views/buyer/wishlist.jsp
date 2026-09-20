<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="My Wishlist - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="wishlist-page">
    <div class="section-heading d-flex justify-between align-center mb-4" style="flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 class="section-title">My Saved Wishlist</h1>
            <p class="text-muted">Keep track of products you love and quickly move them to your cart when ready to purchase.</p>
        </div>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-outline">
            Browse Catalog
        </a>
    </div>

    <c:choose>
        <c:when test="${not empty wishlist}">
            <div class="card mb-5">
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Saved Item</th>
                                <th>Category</th>
                                <th>Unit Price</th>
                                <th>Availability</th>
                                <th class="text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${wishlist}">
                                <tr id="wishlist-row-${p.id}">
                                    <td>
                                        <div class="d-flex align-center" style="gap: 1rem;">
                                            <a href="${pageContext.request.contextPath}/product?id=${p.id}">
                                                <img src="${not empty p.imageUrl ? p.imageUrl : 'https://placehold.co/70x70/e2e8f0/1e293b?text=Item'}"
                                                     alt="<c:out value='${p.name}' />"
                                                     style="width: 56px; height: 56px; object-fit: cover; border-radius: 8px; border: 1px solid var(--border-color);" />
                                            </a>
                                            <div>
                                                <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="font-bold" style="color: var(--text-color);">
                                                    <c:out value="${p.name}" />
                                                </a>
                                                <div class="font-sm text-muted">
                                                    by <c:out value="${p.sellerName}" /> &bull;
                                                    <span style="color: #f59e0b; font-weight: 600;">★ <fmt:formatNumber value="${p.avgRating}" pattern="0.0" /></span>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge" style="background: #f1f5f9;"><c:out value="${p.category}" /></span>
                                    </td>
                                    <td>
                                        <span class="font-bold" style="font-size: 1.1rem; color: var(--text-color);">
                                            ₹<fmt:formatNumber value="${p.price}" pattern="#,##0.00" />
                                        </span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.stockQty <= 0}">
                                                <span class="badge" style="background: #fee2e2; color: #dc2626; font-weight: 700;">Out of Stock</span>
                                            </c:when>
                                            <c:when test="${p.stockQty <= 5}">
                                                <span class="badge" style="background: #fef3c7; color: #d97706; font-weight: 700;">Only ${p.stockQty} left</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background: #dcfce7; color: #15803d; font-weight: 600;">In Stock</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-right">
                                        <div class="d-flex justify-end align-center" style="gap: 0.5rem;">
                                            <c:if test="${p.stockQty > 0}">
                                                <form action="${pageContext.request.contextPath}/cart/add" method="POST" style="display: inline;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                                    <input type="hidden" name="productId" value="${p.id}" />
                                                    <input type="hidden" name="quantity" value="1" />
                                                    <button type="submit" class="btn btn-primary btn-sm">
                                                        Add to Cart
                                                    </button>
                                                </form>
                                            </c:if>
                                            <form action="${pageContext.request.contextPath}/wishlist/remove" method="POST" style="display: inline;">
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                                <input type="hidden" name="productId" value="${p.id}" />
                                                <button type="submit" class="btn btn-outline btn-sm" style="color: #ef4444; border-color: #fca5a5;">
                                                    Remove
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
            <div class="empty-state-card card text-center mb-5" style="padding: 3rem 1.5rem;">
                <div style="font-size: 3.5rem; margin-bottom: 1rem;">❤️</div>
                <h2>Your Wishlist is Empty</h2>
                <p class="text-muted" style="max-width: 480px; margin: 0.5rem auto 1.5rem;">
                    You haven't saved any items to your wishlist yet. Tap the heart icon on any product to save it here for later!
                </p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg">
                    Discover Trending Products
                </a>
            </div>
        </c:otherwise>
    </c:choose>

    <!-- Personalized Product Recommendations Section -->
    <c:if test="${not empty recommendations}">
        <div class="recommendations-section mt-5">
            <div class="section-heading mb-3">
                <h2 class="section-title" style="font-size: 1.35rem;">✨ Recommended For You</h2>
                <p class="text-muted font-sm">Personalized picks based on your shopping interests, ratings, and popular selections.</p>
            </div>

            <div class="grid grid-4" style="gap: 1.25rem;">
                <c:forEach var="rec" items="${recommendations}">
                    <div class="product-card card" style="display: flex; flex-direction: column; justify-content: space-between;">
                        <a href="${pageContext.request.contextPath}/product?id=${rec.id}" style="text-decoration: none; color: inherit;">
                            <div style="position: relative; overflow: hidden; border-radius: 8px; margin-bottom: 0.75rem;">
                                <img src="${not empty rec.imageUrl ? rec.imageUrl : 'https://placehold.co/400x300/e2e8f0/1e293b?text=Item'}"
                                     alt="<c:out value='${rec.name}' />"
                                     style="width: 100%; height: 180px; object-fit: cover;" />
                                <span class="badge" style="position: absolute; top: 8px; left: 8px; background: rgba(15, 23, 42, 0.75); color: #fff; font-size: 0.75rem;">
                                    <c:out value="${rec.category}" />
                                </span>
                            </div>

                            <div class="font-bold text-truncate mb-1" style="font-size: 1rem;">
                                <c:out value="${rec.name}" />
                            </div>

                            <div class="d-flex justify-between align-center mb-2">
                                <div class="font-bold" style="font-size: 1.15rem; color: var(--primary);">
                                    ₹<fmt:formatNumber value="${rec.price}" pattern="#,##0.00" />
                                </div>
                                <div style="color: #f59e0b; font-weight: 600; font-size: 0.85rem;">
                                    ★ <fmt:formatNumber value="${rec.avgRating}" pattern="0.0" />
                                </div>
                            </div>
                        </a>

                        <div class="mt-2">
                            <form action="${pageContext.request.contextPath}/cart/add" method="POST">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                <input type="hidden" name="productId" value="${rec.id}" />
                                <input type="hidden" name="quantity" value="1" />
                                <button type="submit" class="btn btn-outline btn-sm w-100" style="width: 100%;">
                                    + Add to Cart
                                </button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
