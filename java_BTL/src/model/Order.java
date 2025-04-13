package model;

import java.sql.Timestamp;

import enums.StatusEnum;

public class Order {
	private int orderId;
    private Customer customer;
    private Staff staff;
    private Timestamp orderDate;
    private Promotion voucher;
    private double totalAmount;
    private StatusEnum status;
    
    public Order() {}

	public Order(int orderId, Customer customer, Staff staff, Timestamp orderDate, Promotion voucher,
			double totalAmount, StatusEnum status) {
		super();
		this.orderId = orderId;
		this.customer = customer;
		this.staff = staff;
		this.orderDate = orderDate;
		this.voucher = voucher;
		this.totalAmount = totalAmount;
		this.status = status;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Staff getStaff() {
		return staff;
	}

	public void setStaff(Staff staff) {
		this.staff = staff;
	}

	public Timestamp getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Timestamp orderDate) {
		this.orderDate = orderDate;
	}

	public Promotion getVoucher() {
		return voucher;
	}

	public void setVoucher(Promotion voucher) {
		this.voucher = voucher;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", customer=" + customer + ", staff=" + staff + ", orderDate=" + orderDate
				+ ", voucher=" + voucher + ", totalAmount=" + totalAmount + ", status=" + status + "]";
	}
    
    
}
