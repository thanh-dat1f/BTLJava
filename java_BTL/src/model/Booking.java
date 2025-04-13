package model;

import java.time.LocalDateTime;

import enums.StatusEnum;

public class Booking {
    private int bookingId;
    private Customer customer;
    private Pet pet;
    private Staff staff;
    private LocalDateTime bookingTime;
    private StatusEnum status;
    private String note;
    
	public Booking() {
		super();
	}
	public Booking(int bookingId, Customer customer, Pet pet, Staff staff, LocalDateTime bookingTime, StatusEnum status,
			String note) {
		super();
		this.bookingId = bookingId;
		this.customer = customer;
		this.pet = pet;
		this.staff = staff;
		this.bookingTime = bookingTime;
		this.status = status;
		this.note = note;
	}
	public int getBookingId() {
		return bookingId;
	}
	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public Pet getPet() {
		return pet;
	}
	public void setPet(Pet pet) {
		this.pet = pet;
	}
	public Staff getStaff() {
		return staff;
	}
	public void setStaff(Staff staff) {
		this.staff = staff;
	}
	public LocalDateTime getBookingTime() {
		return bookingTime;
	}
	public void setBookingTime(LocalDateTime bookingTime) {
		this.bookingTime = bookingTime;
	}
	public StatusEnum getStatus() {
		return status;
	}
	public void setStatus(StatusEnum status) {
		this.status = status;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	@Override
	public String toString() {
		return "Booking [bookingId=" + bookingId + ", customer=" + customer + ", pet=" + pet + ", staff=" + staff
				+ ", bookingTime=" + bookingTime + ", status=" + status + ", note=" + note + "]";
	}
    
}
