package model;

import java.math.BigDecimal;

public class OrderDetail { 
    private int orderDetailId;
    private Order order;
    private Service service;
    private int quantity;
    private BigDecimal price;


    // Constructor không tham số
    public OrderDetail() {}


	public OrderDetail(int orderDetailId, Order order, Service service, int quantity, BigDecimal price) {
		super();
		this.orderDetailId = orderDetailId;
		this.order = order;
		this.service = service;
		this.quantity = quantity;
		this.price = price;
	}


	public int getOrderDetailId() {
		return orderDetailId;
	}


	public void setOrderDetailId(int orderDetailId) {
		this.orderDetailId = orderDetailId;
	}


	public Order getOrder() {
		return order;
	}


	public void setOrder(Order order) {
		this.order = order;
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


	public BigDecimal getPrice() {
		return price;
	}


	public void setPrice(BigDecimal price) {
		this.price = price;
	}


	@Override
	public String toString() {
		return "OrderDetail [orderDetailId=" + orderDetailId + ", order=" + order + ", service=" + service
				+ ", quantity=" + quantity + ", price=" + price + "]";
	}

    
}
