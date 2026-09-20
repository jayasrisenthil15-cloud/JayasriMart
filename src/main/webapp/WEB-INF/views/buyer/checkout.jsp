<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Secure Checkout - JayasriMart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="checkout-page">
    <h1 class="section-title mb-4">Secure Checkout</h1>

    <form action="${pageContext.request.contextPath}/checkout" method="POST" class="checkout-layout" novalidate>
        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">

        <%-- Left Column: Delivery & Payment Details --%>
        <div class="checkout-main">
            <%-- 1. Delivery Address Card --%>
            <div class="card mb-4">
                <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1.25rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--border-color);">
                    1. Shipping & Delivery Address
                </h2>

                <div class="grid-2-col">
                    <div class="form-group">
                        <label class="form-label" for="name">Full Name <span class="required">*</span></label>
                        <input type="text" id="name" name="name" class="form-control"
                               placeholder="e.g. Aarav Sharma"
                               value="<c:out value='${not empty checkoutData.name ? checkoutData.name : defaultName}' />" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="phone">10-Digit Mobile Phone <span class="required">*</span></label>
                        <input type="tel" id="phone" name="phone" class="form-control"
                               placeholder="e.g. 9876543210" pattern="[6-9][0-9]{9}" maxlength="10"
                               value="<c:out value='${checkoutData.phone}' />" required>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="address">Street Address / House No. <span class="required">*</span></label>
                    <input type="text" id="address" name="address" class="form-control"
                           placeholder="e.g. Flat 402, Green Meadows, Anna Nagar"
                           value="<c:out value='${checkoutData.address}' />" required>
                </div>

                <div class="grid-2-col">
                    <div class="form-group">
                        <label class="form-label" for="city">City / Town <span class="required">*</span></label>
                        <input type="text" id="city" name="city" class="form-control"
                               placeholder="e.g. Chennai"
                               value="<c:out value='${checkoutData.city}' />" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="pincode">Postal PIN Code <span class="required">*</span></label>
                        <input type="text" id="pincode" name="pincode" class="form-control"
                               placeholder="e.g. 600040" pattern="[0-9]{6}" maxlength="6"
                               value="<c:out value='${checkoutData.pincode}' />" required>
                    </div>
                </div>
            </div>

            <%-- 2. Mock Payment Method Card (Strategy Pattern) --%>
            <div class="card mb-4">
                <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1.25rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--border-color);">
                    2. Payment Method (Simulated Strategy)
                </h2>

                <div class="payment-method-selector mb-3">
                    <%-- UPI Strategy --%>
                    <label class="payment-option <c:if test="${empty checkoutData.paymentMethod || checkoutData.paymentMethod == 'UPI'}">selected</c:if>"
                           onclick="selectPaymentMethod('UPI')">
                        <input type="radio" name="paymentMethod" value="UPI"
                               <c:if test="${empty checkoutData.paymentMethod || checkoutData.paymentMethod == 'UPI'}">checked</c:if>>
                        <div>
                            <strong>UPI Payment (Google Pay / PhonePe / Paytm)</strong>
                            <div class="text-muted" style="font-size: 0.8rem;">Instant payment using your Virtual Payment Address (VPA)</div>
                        </div>
                    </label>

                    <%-- Card Strategy --%>
                    <label class="payment-option <c:if test="${checkoutData.paymentMethod == 'CARD'}">selected</c:if>"
                           onclick="selectPaymentMethod('CARD')">
                        <input type="radio" name="paymentMethod" value="CARD"
                               <c:if test="${checkoutData.paymentMethod == 'CARD'}">checked</c:if>>
                        <div>
                            <strong>Credit / Debit Card</strong>
                            <div class="text-muted" style="font-size: 0.8rem;">Simulated Visa, MasterCard, RuPay processing</div>
                        </div>
                    </label>

                    <%-- COD Strategy --%>
                    <label class="payment-option <c:if test="${checkoutData.paymentMethod == 'COD'}">selected</c:if>"
                           onclick="selectPaymentMethod('COD')">
                        <input type="radio" name="paymentMethod" value="COD"
                               <c:if test="${checkoutData.paymentMethod == 'COD'}">checked</c:if>>
                        <div>
                            <strong>Cash on Delivery (COD)</strong>
                            <div class="text-muted" style="font-size: 0.8rem;">Pay cash or scan QR upon doorstep package delivery</div>
                        </div>
                    </label>
                </div>

                <%-- UPI Fields --%>
                <div id="upi-details" class="payment-fields" style="display: ${empty checkoutData.paymentMethod || checkoutData.paymentMethod == 'UPI' ? 'block' : 'none'};">
                    <div class="form-group mb-0">
                        <label class="form-label" for="upiId">Virtual Payment Address (UPI ID) <span class="required">*</span></label>
                        <input type="text" id="upiId" name="upiId" class="form-control"
                               placeholder="e.g. yourname@okaxis or buyer@upi"
                               value="<c:out value='${not empty checkoutData.upiId ? checkoutData.upiId : \"buyer@upi\"}' />">
                        <small class="form-text">Test UPI ID pre-filled. Strategy pattern will simulate approval.</small>
                    </div>
                </div>

                <%-- Card Fields --%>
                <div id="card-details" class="payment-fields" style="display: ${checkoutData.paymentMethod == 'CARD' ? 'block' : 'none'};">
                    <div class="form-group">
                        <label class="form-label" for="cardNumber">Card Number (16 Digits) <span class="required">*</span></label>
                        <input type="text" id="cardNumber" name="cardNumber" class="form-control"
                               placeholder="1234567812345678" maxlength="16"
                               value="<c:out value='${not empty checkoutData.cardNumber ? checkoutData.cardNumber : \"4111222233334444\"}' />">
                    </div>
                    <div class="grid-2-col">
                        <div class="form-group mb-0">
                            <label class="form-label" for="cardExpiry">Expiry Date (MM/YY) <span class="required">*</span></label>
                            <input type="text" id="cardExpiry" name="cardExpiry" class="form-control"
                                   placeholder="12/28" maxlength="5"
                                   value="<c:out value='${not empty checkoutData.cardExpiry ? checkoutData.cardExpiry : \"12/28\"}' />">
                        </div>
                        <div class="form-group mb-0">
                            <label class="form-label" for="cardCvv">CVV (3 Digits) <span class="required">*</span></label>
                            <input type="password" id="cardCvv" name="cardCvv" class="form-control"
                                   placeholder="123" maxlength="4"
                                   value="<c:out value='${not empty checkoutData.cardCvv ? checkoutData.cardCvv : \"123\"}' />">
                        </div>
                    </div>
                </div>

                <%-- COD Notice --%>
                <div id="cod-details" class="payment-fields" style="display: ${checkoutData.paymentMethod == 'COD' ? 'block' : 'none'};">
                    <div class="alert alert-info mb-0">
                        📦 No payment required now. Your order will be placed immediately and collected in cash at delivery.
                    </div>
                </div>
            </div>
        </div>

        <%-- Right Column: Order Summary --%>
        <div class="checkout-sidebar">
            <div class="card">
                <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1.25rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--border-color);">
                    Order Summary
                </h2>

                <div class="checkout-items-list mb-3">
                    <c:forEach var="item" items="${cart.items}">
                        <div class="d-flex justify-between align-center mb-2" style="font-size: 0.875rem;">
                            <div style="max-width: 65%;">
                                <div class="font-bold text-truncate"><c:out value="${item.product.name}" /></div>
                                <span class="text-muted">Qty: ${item.quantity} &times; ₹<fmt:formatNumber value="${item.product.price}" pattern="#,##0.00" /></span>
                            </div>
                            <div class="font-bold">
                                ₹<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00" />
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <div class="summary-line d-flex justify-between mb-2 pt-2" style="border-top: 1px solid var(--border-color);">
                    <span class="text-muted">Subtotal:</span>
                    <span class="font-bold">₹<fmt:formatNumber value="${cart.subtotal}" pattern="#,##0.00" /></span>
                </div>

                <div class="summary-line d-flex justify-between mb-2">
                    <span class="text-muted">Delivery Fee:</span>
                    <span>
                        <c:choose>
                            <c:when test="${cart.freeDelivery}">
                                <strong style="color: var(--success);">FREE</strong>
                            </c:when>
                            <c:otherwise>
                                ₹<fmt:formatNumber value="${cart.deliveryCharge}" pattern="#,##0.00" />
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>

                <div class="summary-total d-flex justify-between mt-3 pt-3" style="border-top: 2px dashed var(--border-color); font-size: 1.25rem;">
                    <span class="font-bold">Total Payable:</span>
                    <span class="font-bold" style="color: var(--secondary);">
                        ₹<fmt:formatNumber value="${cart.grandTotal}" pattern="#,##0.00" />
                    </span>
                </div>

                <button type="submit" class="btn btn-primary btn-block btn-lg mt-4">
                    Place Order (Pay ₹<fmt:formatNumber value="${cart.grandTotal}" pattern="#,##0.00" />)
                </button>

                <div class="text-center mt-3">
                    <a href="${pageContext.request.contextPath}/cart" class="text-muted" style="font-size: 0.875rem;">
                        &larr; Back to Cart
                    </a>
                </div>
            </div>
        </div>
    </form>
</div>

<script>
function selectPaymentMethod(method) {
    document.querySelectorAll('.payment-option').forEach(el => el.classList.remove('selected'));
    const radio = document.querySelector('input[name="paymentMethod"][value="' + method + '"]');
    if (radio) {
        radio.checked = true;
        radio.closest('.payment-option').classList.add('selected');
    }

    document.getElementById('upi-details').style.display = (method === 'UPI') ? 'block' : 'none';
    document.getElementById('card-details').style.display = (method === 'CARD') ? 'block' : 'none';
    document.getElementById('cod-details').style.display = (method === 'COD') ? 'block' : 'none';
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
