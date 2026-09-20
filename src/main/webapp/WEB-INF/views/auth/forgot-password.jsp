<c:set var="pageTitle" value="Reset Password - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card">
        <div class="auth-header">
            <h1 class="auth-title">Password Recovery</h1>
            <p class="auth-subtitle">Enter your registered email address to receive password reset instructions.</p>
        </div>

        <form action="${pageContext.request.contextPath}/forgot-password" method="POST">
            <div class="form-group">
                <label class="form-label" for="email">Email Address <span class="required">*</span></label>
                <input type="email" id="email" name="email" class="form-control"
                       placeholder="e.g. name@example.com"
                       value="<c:out value='${submittedEmail}' />" required autofocus>
            </div>

            <button type="submit" class="btn btn-primary btn-block" style="margin-top: 1rem;">
                Send Recovery Link
            </button>
        </form>

        <div class="auth-footer">
            Remembered your password? <a href="${pageContext.request.contextPath}/login">Back to Sign In</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
