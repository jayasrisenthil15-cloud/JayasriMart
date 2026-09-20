<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="${product.name} - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="product-detail-page">
    <%-- Breadcrumbs --%>
    <nav class="breadcrumb mb-4">
        <a href="${pageContext.request.contextPath}/">Home</a> &gt;
        <a href="${pageContext.request.contextPath}/products?category=${fn:escapeXml(product.category)}"><c:out value="${product.category}" /></a> &gt;
        <span><c:out value="${product.name}" /></span>
    </nav>

    <div class="product-detail-layout card mb-4">
        <%-- Product Image Gallery --%>
        <div class="product-detail-gallery">
            <c:choose>
                <c:when test="${not empty product.imageUrl}">
                    <img src="<c:out value='${product.imageUrl}' />" alt="<c:out value='${product.name}' />"
                         class="product-detail-img"
                         onerror="this.src='https://placehold.co/800x600?text=JayasriMart';">
                </c:when>
                <c:otherwise>
                    <div class="product-detail-placeholder">📦 No Image Available</div>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- Product Information & Purchase Panel --%>
        <div class="product-detail-info">
            <span class="product-detail-category"><c:out value="${product.category}" /></span>
            <h1 class="product-detail-title"><c:out value="${product.name}" /></h1>

            <div class="product-detail-meta">
                <span class="seller-badge">
                    Sold by: <strong><c:out value="${not empty product.sellerName ? product.sellerName : 'Verified Merchant'}" /></strong>
                </span>
                <span class="rating-badge">
                    ★ <fmt:formatNumber value="${product.avgRating}" pattern="0.0" /> / 5.0
                    <span class="text-muted">(${fn:length(reviews)} reviews)</span>
                </span>
            </div>

            <div class="product-detail-price-box">
                <div class="product-detail-price">
                    ₹<fmt:formatNumber value="${product.price}" pattern="#,##0.00" />
                </div>
                <small class="text-muted">Inclusive of all local taxes</small>
            </div>

            <div class="product-detail-stock mb-3">
                <c:choose>
                    <c:when test="${product.stockQty > 5}">
                        <span class="stock-tag in-stock">✓ In Stock (${product.stockQty} units available)</span>
                    </c:when>
                    <c:when test="${product.stockQty > 0}">
                        <span class="stock-tag" style="background: #fffbeb; color: #b45309; border: 1px solid #fde68a;">
                            ⚡ Only ${product.stockQty} left in stock - order soon
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span class="stock-tag out-of-stock">✗ Currently Out of Stock</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="product-detail-description mb-4">
                <h3 style="font-size: 1.05rem; margin-bottom: 0.5rem; color: var(--secondary);">Product Overview</h3>
                <p class="text-muted" style="line-height: 1.6;">
                    <c:out value="${product.description}" />
                </p>
            </div>

            <%-- Action Form: Add to Cart / Buy Now --%>
            <c:if test="${product.stockQty > 0}">
                <form action="${pageContext.request.contextPath}/cart/add" method="POST" class="purchase-form">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                    <input type="hidden" name="productId" value="${product.id}">

                    <div class="form-group mb-3">
                        <label class="form-label" for="qty-input">Select Quantity</label>
                        <div class="quantity-control">
                            <button type="button" class="btn btn-outline btn-sm qty-btn" onclick="adjustQty(-1)">-</button>
                            <input type="number" id="qty-input" name="quantity" value="1" min="1" max="${product.stockQty}" class="qty-field" readonly>
                            <button type="button" class="btn btn-outline btn-sm qty-btn" onclick="adjustQty(1)">+</button>
                        </div>
                    </div>

                    <div class="purchase-actions">
                        <button type="submit" name="action" value="add" class="btn btn-primary btn-lg flex-1">
                            🛒 Add to Cart
                        </button>
                        <button type="submit" name="action" value="buyNow" class="btn btn-secondary btn-lg flex-1">
                            ⚡ Buy Now
                        </button>
                    </div>
                </form>
            </c:if>

            <c:if test="${product.stockQty <= 0}">
                <div class="alert alert-warning">
                    This item is currently out of stock. Please check back later.
                </div>
            </c:if>
        </div>
    </div>

    <%-- Verified Customer Reviews Section --%>
    <div class="reviews-section card">
        <div class="section-heading mb-3">
            <h2 class="section-title">Verified Customer Reviews</h2>
            <span class="text-muted" style="font-size: 0.95rem;">
                ★ <fmt:formatNumber value="${product.avgRating}" pattern="0.0" /> average across ${fn:length(reviews)} rating(s)
            </span>
        </div>

        <c:choose>
            <c:when test="${not empty reviews}">
                <div class="reviews-list">
                    <c:forEach var="rev" items="${reviews}">
                        <div class="review-item">
                            <div class="review-header">
                                <div class="review-author">
                                    <strong><c:out value="${not empty rev.userName ? rev.userName : 'Verified Customer'}" /></strong>
                                    <span class="verified-tag">✓ Verified Purchase</span>
                                </div>
                                <div class="review-date text-muted">
                                    <fmt:formatDate value="${rev.createdAt}" pattern="dd MMM yyyy" />
                                </div>
                            </div>
                            <div class="review-rating mb-2">
                                <c:forEach begin="1" end="5" var="star">
                                    <c:choose>
                                        <c:when test="${star <= rev.rating}"><span style="color: #f59e0b;">★</span></c:when>
                                        <c:otherwise><span style="color: #cbd5e1;">★</span></c:otherwise>
                                    </c:choose>
                                </c:forEach>
                                <span class="rating-text">(${rev.rating} / 5)</span>
                            </div>
                            <p class="review-comment">
                                <c:out value="${rev.comment}" />
                            </p>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-reviews text-center py-4">
                    <p class="text-muted">No reviews yet for this product. Verified buyers can submit feedback after receiving their order.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script>
function adjustQty(delta) {
    const input = document.getElementById('qty-input');
    if (!input) return;
    const current = parseInt(input.value) || 1;
    const max = parseInt(input.getAttribute('max')) || 999;
    const min = parseInt(input.getAttribute('min')) || 1;
    let nextVal = current + delta;
    if (nextVal < min) nextVal = min;
    if (nextVal > max) nextVal = max;
    input.value = nextVal;
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
