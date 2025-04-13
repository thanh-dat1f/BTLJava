package model;

public class BookingDetail {
    private int bookingDetailId;
    private Booking booking;
    private Service service;
    private int quantity;
    private double price;
    
	public BookingDetail() {
		super();
	}
	public BookingDetail(int bookingDetailId, Booking booking, Service service, int quantity, double price) {
		super();
		this.bookingDetailId = bookingDetailId;
		this.booking = booking;
		this.service = service;
		this.quantity = quantity;
		this.price = price;
	}
	public int getBookingDetailId() {
		return bookingDetailId;
	}
	public void setBookingDetailId(int bookingDetailId) {
		this.bookingDetailId = bookingDetailId;
	}
	public Booking getBooking() {
		return booking;
	}
	public void setBooking(Booking booking) {
		this.booking = booking;
	}
	public Service getService() {
		return service;
	}
	public void setService(Service service) {
		this.service = service;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	@Override
	public String toString() {
		return "BookingDetail [bookingDetailId=" + bookingDetailId + ", booking=" + booking + ", service=" + service
				+ ", quantity=" + quantity + ", price=" + price + "]";
	}

    
}

