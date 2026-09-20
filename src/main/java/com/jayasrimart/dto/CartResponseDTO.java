package com.jayasrimart.dto;

import com.jayasrimart.model.CartItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing the calculated shopping cart view for a buyer.
 */
public class CartResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("999.00");
    public static final BigDecimal STANDARD_DELIVERY_FEE = new BigDecimal("50.00");

    private final List<CartItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal deliveryCharge;
    private final BigDecimal grandTotal;
    private final int totalItems;
    private final boolean freeDelivery;
    private final BigDecimal amountNeededForFreeDelivery;

    public CartResponseDTO(List<CartItem> items, BigDecimal subtotal, BigDecimal deliveryCharge,
                           BigDecimal grandTotal, int totalItems) {
        this.items = items != null ? items : new ArrayList<>();
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.deliveryCharge = deliveryCharge != null ? deliveryCharge : BigDecimal.ZERO;
        this.grandTotal = grandTotal != null ? grandTotal : BigDecimal.ZERO;
        this.totalItems = totalItems;
        this.freeDelivery = this.subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0;

        if (this.subtotal.compareTo(FREE_DELIVERY_THRESHOLD) < 0 && this.subtotal.compareTo(BigDecimal.ZERO) > 0) {
            this.amountNeededForFreeDelivery = FREE_DELIVERY_THRESHOLD.subtract(this.subtotal);
        } else {
            this.amountNeededForFreeDelivery = BigDecimal.ZERO;
        }
    }

    public static CartResponseDTO empty() {
        return new CartResponseDTO(new ArrayList<>(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
    }

    public List<CartItem> getItems() {
        return items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public boolean isFreeDelivery() {
        return freeDelivery;
    }

    public BigDecimal getAmountNeededForFreeDelivery() {
        return amountNeededForFreeDelivery;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
