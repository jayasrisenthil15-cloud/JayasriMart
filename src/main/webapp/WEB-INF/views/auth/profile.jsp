<c:set var="pageTitle" value="My Profile - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container" style="max-width: 720px; margin-top: 1rem;">
    <div class="section-heading">
        <h1 class="section-title">Account Profile</h1>
    </div>

    <div class="card">
        <div style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border-color); padding-bottom: 1rem; margin-bottom: 1.5rem;">
            <div>
                <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.25rem;"><c:out value="${user.name}" /></h2>
                <p class="text-muted" style="margin: 0;"><c:out value="${user.email}" /></p>
            </div>
            <div>
                <c:choose>
                    <c:when test="${user.admin}"><span class="badge badge-admin">Administrator</span></c:when>
                    <c:when test="${user.seller}"><span class="badge badge-seller">Merchant Seller</span></c:when>
                    <c:otherwise><span class="badge badge-buyer">Customer</span></c:otherwise>
                </c:choose>
            </div>
        </div>

        <form action="${pageContext.request.contextPath}/profile" method="POST">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">

            <div class="form-group">
                <label class="form-label" for="name">Display Name <span class="required">*</span></label>
                <input type="text" id="name" name="name" class="form-control"
                       value="<c:out value='${user.name}' />" required>
            </div>

            <div class="form-group">
                <label class="form-label">Email Address</label>
                <input type="email" class="form-control" value="<c:out value='${user.email}' />" disabled
                       style="background: var(--bg-muted); cursor: not-allowed;">
                <small class="form-text">Email address cannot be changed once registered.</small>
            </div>

            <div class="form-group">
                <label class="form-label">Member Since</label>
                <input type="text" class="form-control" value="<c:out value='${user.createdAt}' />" disabled
                       style="background: var(--bg-muted); cursor: not-allowed;">
            </div>

            <div class="d-flex justify-between align-center" style="margin-top: 1.5rem;">
                <button type="submit" class="btn btn-primary">Save Changes</button>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-danger">Log Out</a>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
