package com.jayasrimart.service;

import com.jayasrimart.dao.CartDAO;
import com.jayasrimart.dao.OrderDAO;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dto.CheckoutRequestDTO;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.PaymentMethod;
import com.jayasrimart.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderDAO, cartDAO, productDAO);
    }

    @Test
    @DisplayName("placeOrder rejects empty cart")
    void testPlaceOrderEmptyCart() {
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .name("Aarav Sharma")
                .phone("9876543210")
                .address("Anna Nagar")
                .city("Chennai")
                .pincode("600040")
                .paymentMethod(PaymentMethod.UPI)
                .upiId("buyer@upi")
                .build();

        when(cartDAO.findByUserId(4L)).thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> orderService.placeOrder(4L, request));
        verify(orderDAO, never()).create(any(), any());
    }

    @Test
    @DisplayName("cancelOrder rejects cancelling DELIVERED or CANCELLED orders")
    void testCancelOrderIllegalStates() {
        Order deliveredOrder = new Order();
        deliveredOrder.setId(10L);
        deliveredOrder.setBuyerId(4L);
        deliveredOrder.setStatus(OrderStatus.DELIVERED);

        when(orderDAO.findById(10L)).thenReturn(Optional.of(deliveredOrder));

        assertThrows(ValidationException.class, () -> orderService.cancelOrder(4L, 10L));
    }

    @Test
    @DisplayName("updateOrderStatus enforces status workflow transitions")
    void testUpdateOrderStatusWorkflow() {
        Order shippedOrder = new Order();
        shippedOrder.setId(15L);
        shippedOrder.setStatus(OrderStatus.SHIPPED);

        when(orderDAO.findById(15L)).thenReturn(Optional.of(shippedOrder));

        // Illegal: SHIPPED cannot jump back to CONFIRMED or CANCELLED
        assertThrows(ValidationException.class, () -> orderService.updateOrderStatus(15L, OrderStatus.CONFIRMED));
        assertThrows(ValidationException.class, () -> orderService.updateOrderStatus(15L, OrderStatus.CANCELLED));

        // Valid: SHIPPED -> DELIVERED
        orderService.updateOrderStatus(15L, OrderStatus.DELIVERED);
        verify(orderDAO).updateStatus(15L, OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("updateOrderStatus throws ResourceNotFoundException on invalid order")
    void testUpdateOrderStatusNotFound() {
        when(orderDAO.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED));
    }
}
