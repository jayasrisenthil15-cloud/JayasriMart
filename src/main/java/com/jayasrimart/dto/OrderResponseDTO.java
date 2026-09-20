package com.jayasrimart.dto;

import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for order representations.
 * Employs the Builder design pattern.
 */
public class OrderResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final Long buyerId;
    private final BigDecimal totalAmount;
    private final BigDecimal deliveryCharge;
    private final OrderStatus status;
    private final String paymentMethod;
    private final String name;
    private final String phone;
    private final String address;
    private final String city;
    private final String pincode;
    private final Timestamp createdAt;
    private final List<OrderItem> items;

    private OrderResponseDTO(Builder builder) {
        this.id = builder.id;
        this.buyerId = builder.buyerId;
        this.totalAmount = builder.totalAmount != null ? builder.totalAmount : BigDecimal.ZERO;
        this.deliveryCharge = builder.deliveryCharge != null ? builder.deliveryCharge : BigDecimal.ZERO;
        this.status = builder.status != null ? builder.status : OrderStatus.PENDING;
        this.paymentMethod = builder.paymentMethod;
        this.name = builder.name;
        this.phone = builder.phone;
        this.address = builder.address;
        this.city = builder.city;
        this.pincode = builder.pincode;
        this.createdAt = builder.createdAt != null ? new Timestamp(builder.createdAt.getTime()) : null;
        this.items = builder.items != null ? builder.items : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPincode() {
        return pincode;
    }

    public Timestamp getCreatedAt() {
        return createdAt != null ? new Timestamp(createdAt.getTime()) : null;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Builder class for {@link OrderResponseDTO}.
     */
    public static class Builder {
        private Long id;
        private Long buyerId;
        private BigDecimal totalAmount;
        private BigDecimal deliveryCharge;
        private OrderStatus status;
        private String paymentMethod;
        private String name;
        private String phone;
        private String address;
        private String city;
        private String pincode;
        private Timestamp createdAt;
        private List<OrderItem> items = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder buyerId(Long buyerId) {
            this.buyerId = buyerId;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder deliveryCharge(BigDecimal deliveryCharge) {
            this.deliveryCharge = deliveryCharge;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder pincode(String pincode) {
            this.pincode = pincode;
            return this;
        }

        public Builder createdAt(Timestamp createdAt) {
            this.createdAt = createdAt != null ? new Timestamp(createdAt.getTime()) : null;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items != null ? items : new ArrayList<>();
            return this;
        }

        public OrderResponseDTO build() {
            return new OrderResponseDTO(this);
        }
    }
}
