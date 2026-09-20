<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="${isEdit ? 'Edit Product' : 'Add New Product'} - JayasriMart Seller" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="seller-product-form-page" style="max-width: 800px; margin: 0 auto;">
    <div class="mb-4">
        <a href="${pageContext.request.contextPath}/seller/products" class="font-sm" style="color: var(--primary);">
            &larr; Back to Products Catalog
        </a>
        <h1 class="section-title mt-2">
            <c:out value="${isEdit ? 'Edit Product Listing' : 'Add New Product to Catalog'}" />
        </h1>
        <p class="text-muted">
            <c:out value="${isEdit ? 'Update details, pricing, and stock levels for your product.' : 'Fill out product details below to list a new item in JayasriMart marketplace.'}" />
        </p>
    </div>

    <div class="card">
        <form action="${pageContext.request.contextPath}${isEdit ? '/seller/products/edit' : '/seller/products/new'}"
              method="POST"
              class="product-form">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
            <c:if test="${isEdit}">
                <input type="hidden" name="id" value="${product.id}" />
            </c:if>

            <!-- Product Title -->
            <div class="form-group mb-3">
                <label for="name" class="form-label font-semibold">Product Title <span style="color: #ef4444;">*</span></label>
                <input type="text"
                       id="name"
                       name="name"
                       class="form-control"
                       value="<c:out value='${product.name}' />"
                       placeholder="e.g. Pure Linen Slim-Fit Casual Shirt"
                       required
                       minlength="2"
                       maxlength="150" />
                <div class="font-sm text-muted mt-1">Provide a clear, descriptive title between 2 and 150 characters.</div>
            </div>

            <div class="grid grid-2 mb-3" style="gap: 1.25rem;">
                <!-- Category Selection -->
                <div class="form-group">
                    <label for="category" class="form-label font-semibold">Category <span style="color: #ef4444;">*</span></label>
                    <select id="category" name="category" class="form-control" required onchange="toggleCustomCategory(this.value)">
                        <option value="">-- Select Category --</option>
                        <option value="Electronics" ${product.category == 'Electronics' ? 'selected' : ''}>Electronics</option>
                        <option value="Fashion" ${product.category == 'Fashion' ? 'selected' : ''}>Fashion</option>
                        <option value="Home & Living" ${product.category == 'Home & Living' ? 'selected' : ''}>Home & Living</option>
                        <option value="Groceries" ${product.category == 'Groceries' ? 'selected' : ''}>Groceries</option>
                        <option value="Books" ${product.category == 'Books' ? 'selected' : ''}>Books</option>
                        <option value="Beauty" ${product.category == 'Beauty' ? 'selected' : ''}>Beauty</option>
                        <option value="Sports" ${product.category == 'Sports' ? 'selected' : ''}>Sports</option>
                        <option value="Other" ${not empty product.category && product.category != 'Electronics' && product.category != 'Fashion' && product.category != 'Home & Living' && product.category != 'Groceries' && product.category != 'Books' && product.category != 'Beauty' && product.category != 'Sports' ? 'selected' : ''}>Other (Custom)</option>
                    </select>
                </div>

                <!-- Custom Category (if 'Other' is selected) -->
                <div class="form-group" id="customCategoryWrapper" style="display: ${not empty product.category && product.category != 'Electronics' && product.category != 'Fashion' && product.category != 'Home & Living' && product.category != 'Groceries' && product.category != 'Books' && product.category != 'Beauty' && product.category != 'Sports' ? 'block' : 'none'};">
                    <label for="customCategory" class="form-label font-semibold">Specify Custom Category</label>
                    <input type="text"
                           id="customCategory"
                           name="customCategory"
                           class="form-control"
                           value="<c:out value='${product.category}' />"
                           placeholder="e.g. Toys, Auto Accessories" />
                </div>
            </div>

            <div class="grid grid-2 mb-3" style="gap: 1.25rem;">
                <!-- Price -->
                <div class="form-group">
                    <label for="price" class="form-label font-semibold">Unit Price (₹) <span style="color: #ef4444;">*</span></label>
                    <input type="number"
                           id="price"
                           name="price"
                           class="form-control"
                           value="${product.price}"
                           placeholder="999.00"
                           step="0.01"
                           min="1.00"
                           required />
                </div>

                <!-- Stock Quantity -->
                <div class="form-group">
                    <label for="stockQty" class="form-label font-semibold">Stock Quantity <span style="color: #ef4444;">*</span></label>
                    <input type="number"
                           id="stockQty"
                           name="stockQty"
                           class="form-control"
                           value="${product != null ? product.stockQty : '10'}"
                           placeholder="10"
                           min="0"
                           step="1"
                           required />
                </div>
            </div>

            <!-- Image URL -->
            <div class="form-group mb-3">
                <label for="imageUrl" class="form-label font-semibold">Product Image URL</label>
                <input type="url"
                       id="imageUrl"
                       name="imageUrl"
                       class="form-control"
                       value="<c:out value='${product.imageUrl}' />"
                       placeholder="https://images.unsplash.com/photo-..."
                       oninput="updateImagePreview(this.value)" />
                <div class="font-sm text-muted mt-1">Direct link to product image (JPEG, PNG, WebP).</div>

                <!-- Live Preview -->
                <div class="mt-2 d-flex align-center" style="gap: 1rem;">
                    <img id="imgPreview"
                         src="${not empty product.imageUrl ? product.imageUrl : 'https://placehold.co/120x120/e2e8f0/1e293b?text=Preview'}"
                         alt="Image preview"
                         style="width: 80px; height: 80px; object-fit: cover; border-radius: 8px; border: 1px solid var(--border-color);"
                         onerror="this.src='https://placehold.co/120x120/e2e8f0/1e293b?text=Invalid+URL';" />
                    <span class="font-sm text-muted">Live image thumbnail preview</span>
                </div>
            </div>

            <!-- Description -->
            <div class="form-group mb-4">
                <label for="description" class="form-label font-semibold">Detailed Description</label>
                <textarea id="description"
                          name="description"
                          class="form-control"
                          rows="5"
                          placeholder="Highlight the key specifications, fabric material, warranty, features, and benefits of your product..."><c:out value="${product.description}" /></textarea>
            </div>

            <!-- Active / Published Status -->
            <div class="form-group mb-4" style="background: #f8fafc; padding: 1rem; border-radius: 8px; border: 1px solid var(--border-color);">
                <label class="d-flex align-center" style="cursor: pointer; gap: 0.75rem; user-select: none;">
                    <input type="checkbox"
                           name="active"
                           value="true"
                           ${product == null || product.active ? 'checked' : ''}
                           style="width: 1.25rem; height: 1.25rem; accent-color: var(--primary);" />
                    <div>
                        <div class="font-bold">Publish Product Immediately</div>
                        <div class="font-sm text-muted">When checked, product will be active and discoverable in the buyer marketplace search.</div>
                    </div>
                </label>
            </div>

            <div class="form-actions d-flex justify-between align-center" style="border-top: 1px solid var(--border-color); padding-top: 1.25rem;">
                <a href="${pageContext.request.contextPath}/seller/products" class="btn btn-outline">
                    Cancel
                </a>
                <button type="submit" class="btn btn-primary btn-lg">
                    <c:out value="${isEdit ? 'Save Changes' : 'Create & Publish Product'}" />
                </button>
            </div>
        </form>
    </div>
</div>

<script>
function toggleCustomCategory(val) {
    const customDiv = document.getElementById('customCategoryWrapper');
    if (val === 'Other') {
        customDiv.style.display = 'block';
    } else {
        customDiv.style.display = 'none';
    }
}

function updateImagePreview(url) {
    const preview = document.getElementById('imgPreview');
    if (url && url.trim().length > 0) {
        preview.src = url.trim();
    } else {
        preview.src = 'https://placehold.co/120x120/e2e8f0/1e293b?text=Preview';
    }
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
