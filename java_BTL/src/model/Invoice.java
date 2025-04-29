package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import enums.PaymentMethodEnum;
import enums.StatusEnum;

public class Invoice {
	private int invoiceId;
    private Order order;
    private Timestamp paymentDate;
    private BigDecimal total;
    private BigDecimal subtotal; // Added
    private BigDecimal discountAmount; // Added
    private BigDecimal discountPercent; // Added
    private Integer pointsUsed; // Added
    private String promotionCode; // Added
    private BigDecimal amountPaid; // Added
    private String note; // Added
    private PaymentMethodEnum paymentMethod;
    private StatusEnum status;
    private Staff staff;

    // Constructor không tham số
    public Invoice() {}

    // Existing constructor
    public Invoice(int invoiceId, Order order, Timestamp paymentDate, BigDecimal total, 
                   PaymentMethodEnum paymentMethod, StatusEnum status, Staff staff) {
        this.invoiceId = invoiceId;
        this.order = order;
        this.paymentDate = paymentDate;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.staff = staff;
    }

    // Getter and setter for subtotal
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    // Getter and setter for discountAmount
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    // Getter and setter for discountPercent
    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    // Getter and setter for pointsUsed
    public Integer getPointsUsed() {
        return pointsUsed;
    }

    public void setPointsUsed(Integer pointsUsed) {
        this.pointsUsed = pointsUsed;
    }

    // Getter and setter for promotionCode
    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    // Getter and setter for amountPaid
    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    // Getter and setter for note
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // Existing getters and setters remain the same
    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public PaymentMethodEnum getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethodEnum paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    @Override
    public String toString() {
        return "Invoice [invoiceId=" + invoiceId + ", order=" + order + ", paymentDate=" + paymentDate + ", total="
                + total + ", subtotal=" + subtotal + ", discountAmount=" + discountAmount + 
                ", discountPercent=" + discountPercent + ", pointsUsed=" + pointsUsed + 
                ", promotionCode=" + promotionCode + ", amountPaid=" + amountPaid + 
                ", note=" + note + ", paymentMethod=" + paymentMethod + ", status=" + status + ", staff=" + staff + "]";
    }
}