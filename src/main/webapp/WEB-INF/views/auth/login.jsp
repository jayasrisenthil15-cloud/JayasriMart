<c:set var="pageTitle" value="Sign In - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card">
        <div class="auth-header">
            <h1 class="auth-title">Welcome Back</h1>
            <p class="auth-subtitle">Sign in to your JayasriMart account</p>
        </div>

        <form action="${pageContext.request.contextPath}/login" method="POST" novalidate>
            <!-- CSRF Token Protection -->
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">

            <div class="form-group">
                <label class="form-label" for="email">Email Address <span class="required">*</span></label>
                <input type="email" id="email" name="email" class="form-control"
                       placeholder="e.g. name@example.com"
                       value="<c:out value='${email != null ? email : param.email}' />" required autofocus>
            </div>

            <div class="form-group">
                <div class="d-flex justify-between align-center">
                    <label class="form-label" for="password">Password <span class="required">*</span></label>
                    <a href="${pageContext.request.contextPath}/forgot-password" class="form-text" style="color: var(--primary);">Forgot?</a>
                </div>
                <input type="password" id="password" name="password" class="form-control"
                       placeholder="Enter your account password" required>
            </div>

            <button type="submit" class="btn btn-primary btn-block" style="margin-top: 1rem;">
                Sign In
            </button>
        </form>

        <div style="margin-top: 1.5rem; background: var(--bg-muted); padding: 1rem; border-radius: var(--radius-md); font-size: 0.825rem;">
            <strong>Demo Credentials:</strong>
            <ul style="margin: 0.5rem 0 0 1.25rem; line-height: 1.5;">
                <li><strong>Buyer:</strong> buyer1@jayasrimart.com / <code>Buyer@123</code></li>
                <li><strong>Seller:</strong> seller1@jayasrimart.com / <code>Seller@123</code></li>
                <li><strong>Admin:</strong> admin@jayasrimart.com / <code>Admin@123</code></li>
            </ul>
        </div>

        <div class="auth-footer">
            Don't have an account? <a href="${pageContext.request.contextPath}/register">Create an account</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
