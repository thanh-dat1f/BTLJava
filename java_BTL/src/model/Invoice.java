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
    private PaymentMethodEnum paymentMethod;
    private StatusEnum status;
    private Staff staff;
    // Constructor không tham số
    public Invoice() {}

    
    public Invoice(int invoiceId) {
		super();
		this.invoiceId = invoiceId;
	}


	public Invoice(int invoiceId, Order order, Timestamp paymentDate, BigDecimal total, PaymentMethodEnum paymentMethod,
			StatusEnum status, Staff staff) {
		super();
		this.invoiceId = invoiceId;
		this.order = order;
		this.paymentDate = paymentDate;
		this.total = total;
		this.paymentMethod = paymentMethod;
		this.status = status;
		this.staff = staff;
	}


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
				+ total + ", paymentMethod=" + paymentMethod + ", status=" + status + ", staff=" + staff + "]";
	}


	
}
