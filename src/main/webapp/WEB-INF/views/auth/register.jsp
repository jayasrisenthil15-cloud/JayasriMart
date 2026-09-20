<c:set var="pageTitle" value="Create Account - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card">
        <div class="auth-header">
            <h1 class="auth-title">Get Started</h1>
            <p class="auth-subtitle">Join JayasriMart as a Customer or Merchant</p>
        </div>

        <form action="${pageContext.request.contextPath}/register" method="POST" novalidate>
            <!-- CSRF Token Protection -->
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">

            <div class="form-group">
                <label class="form-label" for="name">Full Name <span class="required">*</span></label>
                <input type="text" id="name" name="name" class="form-control"
                       placeholder="e.g. Aarav Sharma"
                       value="<c:out value='${name}' />" required autofocus>
            </div>

            <div class="form-group">
                <label class="form-label" for="email">Email Address <span class="required">*</span></label>
                <input type="email" id="email" name="email" class="form-control"
                       placeholder="e.g. aarav@example.com"
                       value="<c:out value='${email}' />" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="password">Password <span class="required">*</span></label>
                <input type="password" id="password" name="password" class="form-control"
                       placeholder="At least 6 characters" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="confirmPassword">Confirm Password <span class="required">*</span></label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-control"
                       placeholder="Re-type password" required>
            </div>

            <div class="form-group">
                <label class="form-label">I want to join as: <span class="required">*</span></label>
                <div class="radio-group">
                    <label class="radio-label">
                        <input type="radio" name="role" value="BUYER"
                               <c:if test="${empty selectedRole || selectedRole == 'BUYER'}">checked</c:if>>
                        <span>Customer (Buyer)</span>
                    </label>
                    <label class="radio-label">
                        <input type="radio" name="role" value="SELLER"
                               <c:if test="${selectedRole == 'SELLER' || param.role == 'SELLER'}">checked</c:if>>
                        <span>Merchant (Seller)</span>
                    </label>
                </div>
                <small class="form-text">Admin accounts cannot be registered publicly.</small>
            </div>

            <button type="submit" class="btn btn-primary btn-block" style="margin-top: 1.25rem;">
                Create Account
            </button>
        </form>

        <div class="auth-footer">
            Already have an account? <a href="${pageContext.request.contextPath}/login">Sign In</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
