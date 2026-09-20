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

<div class="category-chips">
    <c:forEach var="cat" items="${featuredCategories}">
        <a href="${pageContext.request.contextPath}/products?category=${cat}" class="category-chip">
            <c:out value="${cat}" />
        </a>
    </c:forEach>
</div>

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
