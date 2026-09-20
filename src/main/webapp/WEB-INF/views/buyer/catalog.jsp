<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Product Catalog - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="catalog-page">
    <%-- 1. Top Category Chips Navigation --%>
    <div class="category-chips mb-4">
        <a href="${pageContext.request.contextPath}/products"
           class="category-chip <c:if test='${empty selectedCategory}'>active</c:if>">
            All Products
        </a>
        <c:forEach var="cat" items="${categories}">
            <a href="${pageContext.request.contextPath}/products?category=${cat}"
               class="category-chip <c:if test='${selectedCategory == cat}'>active</c:if>">
                <c:out value="${cat}" />
            </a>
        </c:forEach>
    </div>

    <%-- 2. Main Search & Filter Bar --%>
    <div class="card mb-4" style="padding: 1.25rem;">
        <form action="${pageContext.request.contextPath}/products" method="GET" class="filter-form">
            <div class="filter-grid">
                <%-- Search Input --%>
                <div class="form-group mb-0">
                    <label class="form-label" for="keyword">Search Products</label>
                    <input type="text" id="keyword" name="q" class="form-control"
                           placeholder="Search by title, keywords..."
                           value="<c:out value='${keyword}' />">
                </div>

                <%-- Category Select --%>
                <div class="form-group mb-0">
                    <label class="form-label" for="category">Category</label>
                    <select id="category" name="category" class="form-control">
                        <option value="">All Categories</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat}" <c:if test="${selectedCategory == cat}">selected</c:if>>
                                <c:out value="${cat}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <%-- Min Price --%>
                <div class="form-group mb-0">
                    <label class="form-label" for="minPrice">Min Price (₹)</label>
                    <input type="number" id="minPrice" name="minPrice" class="form-control"
                           placeholder="0" min="0" step="1"
                           value="<c:out value='${minPrice}' />">
                </div>

                <%-- Max Price --%>
                <div class="form-group mb-0">
                    <label class="form-label" for="maxPrice">Max Price (₹)</label>
                    <input type="number" id="maxPrice" name="maxPrice" class="form-control"
                           placeholder="10000" min="0" step="1"
                           value="<c:out value='${maxPrice}' />">
                </div>

                <%-- Min Rating --%>
                <div class="form-group mb-0">
                    <label class="form-label" for="minRating">Minimum Rating</label>
                    <select id="minRating" name="minRating" class="form-control">
                        <option value="">All Ratings</option>
                        <option value="4" <c:if test="${minRating == 4}">selected</c:if>>★ 4.0 & Above</option>
                        <option value="3" <c:if test="${minRating == 3}">selected</c:if>>★ 3.0 & Above</option>
                        <option value="2" <c:if test="${minRating == 2}">selected</c:if>>★ 2.0 & Above</option>
                    </select>
                </div>

                <%-- Sort By --%>
                <div class="form-group mb-0">
                    <label class="form-label" for="sort">Sort By</label>
                    <select id="sort" name="sort" class="form-control">
                        <option value="newest" <c:if test="${sortBy == 'newest'}">selected</c:if>>Newest Arrivals</option>
                        <option value="price_asc" <c:if test="${sortBy == 'price_asc'}">selected</c:if>>Price: Low to High</option>
                        <option value="price_desc" <c:if test="${sortBy == 'price_desc'}">selected</c:if>>Price: High to Low</option>
                        <option value="rating_desc" <c:if test="${sortBy == 'rating_desc'}">selected</c:if>>Highest Customer Rating</option>
                    </select>
                </div>
            </div>

            <div class="d-flex justify-between align-center mt-3" style="flex-wrap: wrap; gap: 0.75rem;">
                <span class="text-muted" style="font-size: 0.9rem;">
                    Showing <strong>${fn:length(products)}</strong> of <strong>${totalCount}</strong> products
                </span>
                <div class="d-flex gap-2">
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-sm">Reset Filters</a>
                    <button type="submit" class="btn btn-primary btn-sm">Apply Filters</button>
                </div>
            </div>
        </form>
    </div>

    <%-- 3. Product Grid --%>
    <c:choose>
        <c:when test="${not empty products}">
            <div class="product-grid">
                <c:forEach var="p" items="${products}">
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
                                <a href="${pageContext.request.contextPath}/product?id=${p.id}" title="<c:out value='${p.name}' />">
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
                                    <c:choose>
                                        <c:when test="${p.stockQty > 0}">
                                            <span class="stock-tag in-stock">${p.stockQty} in stock</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="stock-tag out-of-stock">Out of stock</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="d-flex gap-2">
                                    <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="btn btn-outline btn-sm">
                                        Details
                                    </a>
                                    <c:choose>
                                        <c:when test="${p.stockQty > 0}">
                                            <form action="${pageContext.request.contextPath}/cart/add" method="POST" style="margin: 0;">
                                                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                                                <input type="hidden" name="productId" value="${p.id}">
                                                <input type="hidden" name="quantity" value="1">
                                                <button type="submit" class="btn btn-primary btn-sm" title="Add to Cart">
                                                    + Cart
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <button class="btn btn-secondary btn-sm" disabled style="opacity: 0.5;">Sold</button>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <%-- 4. Pagination Controls --%>
            <c:if test="${totalPages > 1}">
                <div class="pagination">
                    <c:if test="${currentPage > 1}">
                        <a href="${pageContext.request.contextPath}/products?q=${fn:escapeXml(keyword)}&category=${fn:escapeXml(selectedCategory)}&minPrice=${minPrice}&maxPrice=${maxPrice}&minRating=${minRating}&sort=${sortBy}&page=${currentPage - 1}"
                           class="page-link">&laquo; Previous</a>
                    </c:if>

                    <c:forEach var="pageNo" begin="1" end="${totalPages}">
                        <a href="${pageContext.request.contextPath}/products?q=${fn:escapeXml(keyword)}&category=${fn:escapeXml(selectedCategory)}&minPrice=${minPrice}&maxPrice=${maxPrice}&minRating=${minRating}&sort=${sortBy}&page=${pageNo}"
                           class="page-link <c:if test='${pageNo == currentPage}'>active</c:if>">
                            ${pageNo}
                        </a>
                    </c:forEach>

                    <c:if test="${currentPage < totalPages}">
                        <a href="${pageContext.request.contextPath}/products?q=${fn:escapeXml(keyword)}&category=${fn:escapeXml(selectedCategory)}&minPrice=${minPrice}&maxPrice=${maxPrice}&minRating=${minRating}&sort=${sortBy}&page=${currentPage + 1}"
                           class="page-link">Next &raquo;</a>
                    </c:if>
                </div>
            </c:if>
        </c:when>

        <%-- Empty State --%>
        <c:otherwise>
            <div class="empty-state-card">
                <div style="font-size: 3rem; margin-bottom: 1rem;">🔍</div>
                <h2>No Products Found</h2>
                <p class="text-muted" style="max-width: 480px; margin: 0.5rem auto 1.5rem;">
                    We couldn't find any products matching your current search or filter criteria. Try adjusting your price range or clearing filters.
                </p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">
                    Clear All Filters
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
