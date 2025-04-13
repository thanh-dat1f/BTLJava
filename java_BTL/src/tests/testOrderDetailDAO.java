package tests;

import model.Order;
import model.OrderDetail;
import model.Service;
import repository.OrderDetailRepository;

import java.math.BigDecimal;
import java.util.List;

public class testOrderDetailDAO {
    public static void main(String[] args) {
        OrderDetailRepository orderDetailRepository = OrderDetailRepository.getInstance();

        // Tạo dữ liệu giả lập
        Order testOrder = new Order();
        testOrder.setOrderId(1); // Đảm bảo có OrderID = 1 trong DB

        Service testService = new Service();
        testService.setServiceID(1); // Đảm bảo có ServiceID = 1 trong DB

        // 1️⃣ Thêm order_detail

        OrderDetail newOrderDetail = new OrderDetail(0, testOrder, testService, 2, BigDecimal.valueOf(testService.getCostPrice()));
        int insertResult = orderDetailRepository.insert(newOrderDetail);
        if (insertResult > 0) {
            System.out.println("Thêm order_detail thành công! ID: " + newOrderDetail.getOrderDetailId());
        } else {
            System.out.println("Thêm order_detail thất bại!");
        }

        // 2️⃣ Lấy danh sách order_detail
        List<OrderDetail> orderDetails = orderDetailRepository.selectAll();
        System.out.println("📌 Danh sách order_detail:");
        for (OrderDetail od : orderDetails) {
            System.out.println("🔹 ID: " + od.getOrderDetailId() + ", OrderID: " + od.getOrder().getOrderId() +
                    ", ServiceID: " + od.getService().getServiceID() + ", Quantity: " + od.getQuantity() +
                    ", Price: " + od.getUnitPrice());
        }

        // 3️⃣ Lấy order_detail theo ID
        OrderDetail fetchedDetail = orderDetailRepository.selectById(newOrderDetail.getOrderDetailId());
        if (fetchedDetail != null) {
            System.out.println("🔍 Tìm thấy order_detail với ID " + fetchedDetail.getOrderDetailId() + ": " +
                    "Số lượng = " + fetchedDetail.getQuantity() + ", Giá = " + fetchedDetail.getUnitPrice());
        } else {
            System.out.println("❌ Không tìm thấy order_detail!");
        }

        // 4️⃣ Cập nhật order_detail
        newOrderDetail.setQuantity(3);
        Service testService2 = new Service();
        testService.setServiceID(2); 
        newOrderDetail.setUnitPrice(BigDecimal.valueOf(testService2.getCostPrice()));
        int updateResult = orderDetailRepository.update(newOrderDetail);
        if (updateResult > 0) {
            System.out.println("Cập nhật order_detail thành công!");
        } else {
            System.out.println("Cập nhật thất bại!");
        }

        // 5️⃣ Xóa order_detail
        int deleteResult = orderDetailRepository.delete(newOrderDetail);
        if (deleteResult > 0) {
            System.out.println("✅ Xóa order_detail thành công!");
        } else {
            System.out.println("❌ Xóa thất bại!");
        }
    }
}
