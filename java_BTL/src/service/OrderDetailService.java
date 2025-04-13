package service;

import java.util.List;

import model.OrderDetail;
import repository.OrderDetailRepository;

//định tạo thêm nhưng mà hồi sau khoong biết làm gì với nó

public class OrderDetailService {

	private final OrderDetailRepository orderDetailRepository;
	private final OrderService orderService;

	public OrderDetailService() {
		this.orderDetailRepository = OrderDetailRepository.getInstance();
		this.orderService = new OrderService(); // Đảm bảo gọi được update tổng
	}

	public List<OrderDetail> getAllOrderDetails() {
		return orderDetailRepository.selectAll();
	}

	public List<OrderDetail> getDetailsByOrderId(int orderId) {
		return orderDetailRepository.selectById(orderId);
	}

	public void addOrderDetail(OrderDetail detail) {
		int rowsAffected = orderDetailRepository.insert(detail);
		if (rowsAffected > 0) {
			System.out.println("Thêm dịch vụ vào lịch hẹn thành công.");
			orderService.updateOrderTotal(detail.getOrder().getOrderId()); // ✅ Cập nhật tổng tiền
		}
	}

	public void updateOrderDetail(OrderDetail detail) {
		int rowsAffected = orderDetailRepository.update(detail);
		if (rowsAffected > 0) {
			System.out.println("Cập nhật dịch vụ trong lịch hẹn thành công.");
			orderService.updateOrderTotal(detail.getOrder().getOrderId()); // ✅
		}
	}

	public void deleteOrderDetail(OrderDetail detail) {
		int rowsAffected = orderDetailRepository.delete(detail);
		if (rowsAffected > 0) {
			System.out.println("Xóa dịch vụ khỏi lịch hẹn thành công.");
			orderService.updateOrderTotal(detail.getOrder().getOrderId()); // ✅
		}
	}
}
