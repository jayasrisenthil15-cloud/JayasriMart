<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="User Management - JayasriMart Admin" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="admin-users-page">
    <div class="section-heading d-flex justify-between align-center mb-4" style="flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 class="section-title">User Directory &amp; Role Governance</h1>
            <p class="text-muted">Manage registered user accounts, assign seller privileges, and administer system access.</p>
        </div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-outline">
            &larr; Back to Dashboard
        </a>
    </div>

    <!-- Role Filter Pills -->
    <div class="d-flex mb-4" style="gap: 0.5rem; flex-wrap: wrap;">
        <a href="${pageContext.request.contextPath}/admin/users"
           class="btn btn-sm ${empty selectedRole || selectedRole == 'ALL' ? 'btn-primary' : 'btn-outline'}">
            All Users
        </a>
        <a href="${pageContext.request.contextPath}/admin/users?role=BUYER"
           class="btn btn-sm ${selectedRole == 'BUYER' ? 'btn-primary' : 'btn-outline'}">
            Buyers Only
        </a>
        <a href="${pageContext.request.contextPath}/admin/users?role=SELLER"
           class="btn btn-sm ${selectedRole == 'SELLER' ? 'btn-primary' : 'btn-outline'}">
            Sellers Only
        </a>
        <a href="${pageContext.request.contextPath}/admin/users?role=ADMIN"
           class="btn btn-sm ${selectedRole == 'ADMIN' ? 'btn-primary' : 'btn-outline'}">
            Admins Only
        </a>
    </div>

    <div class="card">
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>User ID</th>
                        <th>Name</th>
                        <th>Email Address</th>
                        <th>Current Role</th>
                        <th>Registered Date</th>
                        <th class="text-right">Change Role</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${users}">
                        <tr>
                            <td><strong>#USR-${u.id}</strong></td>
                            <td>
                                <strong><c:out value="${u.name}" /></strong>
                                <c:if test="${u.id == sessionScope.currentUser.id}">
                                    <span class="font-sm text-muted">(You)</span>
                                </c:if>
                            </td>
                            <td><c:out value="${u.email}" /></td>
                            <td>
                                <c:choose>
                                    <c:when test="${u.role == 'ADMIN'}">
                                        <span class="badge" style="background: #ffedd5; color: #c2410c; font-weight: 700;">ADMIN</span>
                                    </c:when>
                                    <c:when test="${u.role == 'SELLER'}">
                                        <span class="badge" style="background: #f3e8ff; color: #7e22ce; font-weight: 700;">SELLER</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge" style="background: #dbeafe; color: #1d4ed8; font-weight: 600;">BUYER</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="font-sm text-muted">
                                <fmt:formatDate value="${u.createdAt}" pattern="dd MMM yyyy, hh:mm a" />
                            </td>
                            <td class="text-right">
                                <c:choose>
                                    <c:when test="${u.id == sessionScope.currentUser.id}">
                                        <span class="font-sm text-muted">Protected (Self)</span>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${pageContext.request.contextPath}/admin/users/role"
                                              method="POST"
                                              class="d-flex justify-end align-center"
                                              style="gap: 0.5rem;"
                                              onsubmit="return confirm('Change role of ${u.email} to ' + this.role.value + '?');">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                            <input type="hidden" name="userId" value="${u.id}" />
                                            <select name="role" class="form-control" style="padding: 0.25rem 0.5rem; font-size: 0.85rem; width: auto;">
                                                <option value="BUYER" ${u.role == 'BUYER' ? 'selected' : ''}>BUYER</option>
                                                <option value="SELLER" ${u.role == 'SELLER' ? 'selected' : ''}>SELLER</option>
                                                <option value="ADMIN" ${u.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                                            </select>
                                            <button type="submit" class="btn btn-outline btn-sm">
                                                Update
                                            </button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
