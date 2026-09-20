<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="JayasriMart - Multi-Seller E-Commerce Marketplace" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="hero-section">
    <h1 class="hero-title">Your One-Stop Multi-Seller Marketplace</h1>
    <p class="hero-tagline">
        Discover quality products across fashion, electronics, home essentials, books, and more from verified merchants.
    </p>
    <div class="hero-actions">
        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg">Browse Catalog</a>
        <a href="${pageContext.request.contextPath}/register?role=SELLER" class="btn btn-secondary btn-lg">Become a Merchant</a>
    </div>
</div>

<div class="section-heading">
    <h2 class="section-title">Explore Categories</h2>
    <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-sm">View All &rarr;</a>
</div>

<div class="category-chips mb-4">
    <c:forEach var="cat" items="${featuredCategories}">
        <a href="${pageContext.request.contextPath}/products?category=${cat}" class="category-chip">
            <c:out value="${cat}" />
        </a>
    </c:forEach>
</div>

<%-- Featured Products Showcase --%>
<c:if test="${not empty featuredProducts}">
    <div class="section-heading mt-4">
        <h2 class="section-title">Featured Top-Rated Products</h2>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-sm">View Full Catalog &rarr;</a>
    </div>

    <div class="product-grid mb-5">
        <c:forEach var="p" items="${featuredProducts}">
            <div class="product-card">
                <div class="product-card-img-wrap">
                    <c:choose>
                        <c:when test="${not empty p.imageUrl}">
                            <img src="<c:out value='${p.imageUrl}' />" alt="<c:out value='${p.name}' />"
                                 class="product-card-img" loading="lazy"
                                 onerror="this.src='https://placehold.co/600x400?text=JayasriMart';">
                        </c:when>
                        <c:otherwise>
                            <div class="product-card-placeholder">📦 No Image Available</div>
                        </c:otherwise>
                    </c:choose>
                    <span class="product-card-category"><c:out value="${p.category}" /></span>
                </div>

                <div class="product-card-body">
                    <h3 class="product-card-title">
                        <a href="${pageContext.request.contextPath}/product?id=${p.id}">
                            <c:out value="${p.name}" />
                        </a>
                    </h3>

                    <div class="product-card-meta">
                        <span class="seller-name">by <c:out value="${not empty p.sellerName ? p.sellerName : 'Merchant'}" /></span>
                        <span class="product-rating">
                            ★ <fmt:formatNumber value="${p.avgRating}" pattern="0.0" />
                        </span>
                    </div>

                    <div class="product-card-footer">
                        <div>
                            <div class="product-price">
                                ₹<fmt:formatNumber value="${p.price}" pattern="#,##0.00" />
                            </div>
                            <span class="stock-tag in-stock">${p.stockQty} in stock</span>
                        </div>
                        <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="btn btn-primary btn-sm">
                            View
                        </a>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</c:if>

<div class="section-heading mt-4">
    <h2 class="section-title">Why Choose JayasriMart</h2>
</div>

<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem; margin-bottom: 3rem;">
    <div class="card">
        <h3 style="margin-bottom: 0.5rem; color: var(--primary);">Verified Sellers</h3>
        <p class="text-muted">Direct marketplace connecting trusted local merchants and artisans with buyers nationwide.</p>
    </div>
    <div class="card">
        <h3 style="margin-bottom: 0.5rem; color: var(--primary);">Fast Mock Checkout</h3>
        <p class="text-muted">Frictionless simulated payment checkout supporting UPI, Credit/Debit Card, and Cash on Delivery.</p>
    </div>
    <div class="card">
        <h3 style="margin-bottom: 0.5rem; color: var(--primary);">Authentic Customer Reviews</h3>
        <p class="text-muted">Strict review policies allow only confirmed buyers with delivered orders to leave ratings and feedback.</p>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
