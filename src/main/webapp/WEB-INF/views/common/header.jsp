<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle != null ? pageTitle : 'JayasriMart - Multi-Seller E-Commerce'}" /></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
    <header class="navbar">
        <div class="container nav-container">
            <a href="${pageContext.request.contextPath}/" class="brand">
                <span>JayasriMart</span>
                <c:if test="${not empty sessionScope.currentUser}">
                    <span class="brand-badge"><c:out value="${sessionScope.currentUser.role}" /></span>
                </c:if>
            </a>

            <!-- Desktop Navigation -->
            <nav class="nav-links">
                <c:choose>
                    <%-- 1. Guest Visitor Links --%>
                    <c:when test="${empty sessionScope.currentUser}">
                        <a href="${pageContext.request.contextPath}/" class="nav-link">Home</a>
                        <a href="${pageContext.request.contextPath}/products" class="nav-link">Browse Products</a>
                        <a href="${pageContext.request.contextPath}/login" class="nav-link">Sign In</a>
                        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-sm">Register</a>
                    </c:when>

                    <%-- 2. Buyer Navigation --%>
                    <c:when test="${sessionScope.currentUser.buyer}">
                        <a href="${pageContext.request.contextPath}/products" class="nav-link">Catalog</a>
                        <a href="${pageContext.request.contextPath}/wishlist" class="nav-link">
                            Wishlist
                            <c:if test="${not empty sessionScope.wishlistCount && sessionScope.wishlistCount > 0}">
                                <span class="badge badge-buyer"><c:out value="${sessionScope.wishlistCount}" /></span>
                            </c:if>
                        </a>
                        <a href="${pageContext.request.contextPath}/cart" class="nav-link">
                            Cart
                            <c:if test="${not empty sessionScope.cartCount && sessionScope.cartCount > 0}">
                                <span class="badge badge-buyer"><c:out value="${sessionScope.cartCount}" /></span>
                            </c:if>
                        </a>
                        <a href="${pageContext.request.contextPath}/orders" class="nav-link">My Orders</a>
                        <a href="${pageContext.request.contextPath}/profile" class="nav-link">Profile</a>
                        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Sign Out</a>
                    </c:when>

                    <%-- 3. Seller Navigation --%>
                    <c:when test="${sessionScope.currentUser.seller}">
                        <a href="${pageContext.request.contextPath}/seller/dashboard" class="nav-link">Dashboard</a>
                        <a href="${pageContext.request.contextPath}/seller/products" class="nav-link">My Products</a>
                        <a href="${pageContext.request.contextPath}/seller/products/new" class="nav-link">+ Add Product</a>
                        <a href="${pageContext.request.contextPath}/seller/orders" class="nav-link">Orders</a>
                        <a href="${pageContext.request.contextPath}/profile" class="nav-link">Profile</a>
                        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Sign Out</a>
                    </c:when>

                    <%-- 4. Admin Navigation --%>
                    <c:when test="${sessionScope.currentUser.admin}">
                        <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link">Dashboard</a>
                        <a href="${pageContext.request.contextPath}/admin/users" class="nav-link">Users</a>
                        <a href="${pageContext.request.contextPath}/admin/products" class="nav-link">Products</a>
                        <a href="${pageContext.request.contextPath}/admin/orders" class="nav-link">Orders</a>
                        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Sign Out</a>
                    </c:when>
                </c:choose>
            </nav>

            <!-- Mobile Hamburger Button -->
            <button class="mobile-toggle" aria-label="Toggle navigation menu">
                <svg width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
                </svg>
            </button>
        </div>

        <!-- Mobile Drawer Menu -->
        <div class="mobile-menu">
            <c:choose>
                <c:when test="${empty sessionScope.currentUser}">
                    <a href="${pageContext.request.contextPath}/" class="nav-link">Home</a>
                    <a href="${pageContext.request.contextPath}/products" class="nav-link">Browse Products</a>
                    <a href="${pageContext.request.contextPath}/login" class="nav-link">Sign In</a>
                    <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-sm">Register</a>
                </c:when>
                <c:when test="${sessionScope.currentUser.buyer}">
                    <a href="${pageContext.request.contextPath}/products" class="nav-link">Catalog</a>
                    <a href="${pageContext.request.contextPath}/wishlist" class="nav-link">Wishlist</a>
                    <a href="${pageContext.request.contextPath}/cart" class="nav-link">Cart</a>
                    <a href="${pageContext.request.contextPath}/orders" class="nav-link">My Orders</a>
                    <a href="${pageContext.request.contextPath}/profile" class="nav-link">Profile</a>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Sign Out</a>
                </c:when>
                <c:when test="${sessionScope.currentUser.seller}">
                    <a href="${pageContext.request.contextPath}/seller/dashboard" class="nav-link">Dashboard</a>
                    <a href="${pageContext.request.contextPath}/seller/products" class="nav-link">My Products</a>
                    <a href="${pageContext.request.contextPath}/seller/products/new" class="nav-link">+ Add Product</a>
                    <a href="${pageContext.request.contextPath}/seller/orders" class="nav-link">Orders</a>
                    <a href="${pageContext.request.contextPath}/profile" class="nav-link">Profile</a>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Sign Out</a>
                </c:when>
                <c:when test="${sessionScope.currentUser.admin}">
                    <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link">Dashboard</a>
                    <a href="${pageContext.request.contextPath}/admin/users" class="nav-link">Users</a>
                    <a href="${pageContext.request.contextPath}/admin/products" class="nav-link">Products</a>
                    <a href="${pageContext.request.contextPath}/admin/orders" class="nav-link">Orders</a>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Sign Out</a>
                </c:when>
            </c:choose>
        </div>
    </header>

    <main class="main-content">
        <div class="container">
            <%-- Flash Messages --%>
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error">
                    <c:out value="${errorMessage}" />
                </div>
            </c:if>
            <c:if test="${not empty param.error}">
                <div class="alert alert-error">
                    <c:out value="${param.error}" />
                </div>
            </c:if>
            <c:if test="${not empty successMessage}">
                <div class="alert alert-success">
                    <c:out value="${successMessage}" />
                </div>
            </c:if>
            <c:if test="${not empty param.success}">
                <div class="alert alert-success">
                    <c:out value="${param.success}" />
                </div>
            </c:if>
            <c:if test="${not empty infoMessage}">
                <div class="alert alert-info">
                    <c:out value="${infoMessage}" />
                </div>
            </c:if>
            <c:if test="${not empty param.info}">
                <div class="alert alert-info">
                    <c:out value="${param.info}" />
                </div>
            </c:if>
