<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Write a Review - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="review-form-page">
    <div class="auth-card" style="max-width: 600px; margin: 0 auto;">
        <div class="auth-header">
            <h1 class="auth-title">Verified Customer Review</h1>
            <p class="auth-subtitle">Share your authentic experience with other shoppers</p>
        </div>

        <%-- Product Context Card --%>
        <div class="d-flex align-center gap-3 mb-4" style="background: var(--bg-muted); padding: 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-color);">
            <c:choose>
                <c:when test="${not empty product.imageUrl}">
                    <img src="<c:out value='${product.imageUrl}' />" alt="<c:out value='${product.name}' />"
                         style="width: 70px; height: 70px; object-fit: cover; border-radius: var(--radius-sm);"
                         onerror="this.src='https://placehold.co/70x70?text=Product';">
                </c:when>
                <c:otherwise>
                    <div style="width: 70px; height: 70px; display: flex; align-items: center; justify-content: center; background: var(--bg-surface); border-radius: var(--radius-sm);">📦</div>
                </c:otherwise>
            </c:choose>

            <div>
                <h3 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 0.25rem;">
                    <c:out value="${product.name}" />
                </h3>
                <div class="text-muted" style="font-size: 0.85rem;">
                    From Order <strong>#ORD-${order.id}</strong> (Delivered)
                </div>
            </div>
        </div>

        <form action="${pageContext.request.contextPath}/reviews" method="POST" novalidate>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
            <input type="hidden" name="productId" value="${product.id}">
            <input type="hidden" name="orderId" value="${order.id}">

            <%-- Star Rating Selector --%>
            <div class="form-group mb-4">
                <label class="form-label">Your Overall Rating <span class="required">*</span></label>
                <div class="star-rating-selector">
                    <c:forEach begin="1" end="5" var="val">
                        <label class="star-label">
                            <input type="radio" name="rating" value="${val}"
                                   <c:if test="${(empty selectedRating && val == 5) || selectedRating == val}">checked</c:if>>
                            <span class="star-text">${val} ★</span>
                        </label>
                    </c:forEach>
                </div>
                <small class="form-text">Choose from 1 star (Poor) to 5 stars (Excellent).</small>
            </div>

            <%-- Review Comment --%>
            <div class="form-group mb-4">
                <label class="form-label" for="comment">Your Written Feedback <span class="required">*</span></label>
                <textarea id="comment" name="comment" class="form-control" rows="5"
                          placeholder="What did you like or dislike about this product? How is the quality, packaging, and fit?"
                          minlength="5" maxlength="1000" required><c:out value="${submittedComment}" /></textarea>
                <small class="form-text">Minimum 5 characters, maximum 1000 characters.</small>
            </div>

            <button type="submit" class="btn btn-primary btn-block btn-lg">
                Submit Verified Review
            </button>

            <div class="text-center mt-3">
                <a href="${pageContext.request.contextPath}/orders/detail?id=${order.id}" class="text-muted" style="font-size: 0.875rem;">
                    &larr; Cancel and return to order
                </a>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
