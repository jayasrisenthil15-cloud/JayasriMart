package com.jayasrimart.dto;

import com.jayasrimart.model.PaymentMethod;

import java.io.Serializable;

/**
 * Data Transfer Object capturing delivery destination and payment configuration during checkout.
 */
public class CheckoutRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String phone;
    private String address;
    private String city;
    private String pincode;
    private PaymentMethod paymentMethod;
    private String upiId;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;

    public CheckoutRequestDTO() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    public String getCardCvv() {
        return cardCvv;
    }

    public void setCardCvv(String cardCvv) {
        this.cardCvv = cardCvv;
    }

    /**
     * Builder for {@link CheckoutRequestDTO}.
     */
    public static class Builder {
        private final CheckoutRequestDTO dto = new CheckoutRequestDTO();

        public Builder name(String name) {
            dto.setName(name);
            return this;
        }

        public Builder phone(String phone) {
            dto.setPhone(phone);
            return this;
        }

        public Builder address(String address) {
            dto.setAddress(address);
            return this;
        }

        public Builder city(String city) {
            dto.setCity(city);
            return this;
        }

        public Builder pincode(String pincode) {
            dto.setPincode(pincode);
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            dto.setPaymentMethod(paymentMethod);
            return this;
        }

        public Builder upiId(String upiId) {
            dto.setUpiId(upiId);
            return this;
        }

        public Builder cardNumber(String cardNumber) {
            dto.setCardNumber(cardNumber);
            return this;
        }

        public Builder cardExpiry(String cardExpiry) {
            dto.setCardExpiry(cardExpiry);
            return this;
        }

        public Builder cardCvv(String cardCvv) {
            dto.setCardCvv(cardCvv);
            return this;
        }

        public CheckoutRequestDTO build() {
            return dto;
        }
    }
}
