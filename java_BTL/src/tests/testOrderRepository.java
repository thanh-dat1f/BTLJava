package tests;

import model.Customer;
import model.HappenStatus;
import model.Order;
import model.Staff;
import repository.OrderRepository;

import java.sql.Timestamp;
import java.util.List;

import enums.TypeOrder;

public class testOrderRepository {
    public static void main(String[] args) {
        OrderRepository orderRepository = OrderRepository.getInstance();

        Customer customer = new Customer();
        customer.setId(1);

        Staff staff = new Staff();
        staff.setId(2);

        HappenStatus happenStatus = new HappenStatus();
        happenStatus.setHappenStatusID(1);

        // 1️ Thêm đơn hàng mới
        Order newOrder = new Order(
            0,
            new Timestamp(System.currentTimeMillis()),
            new Timestamp(System.currentTimeMillis() + 86400000),
            TypeOrder.APPOINTMENT,
            0.0,
            customer,
            staff,
            happenStatus
        );

        int result = orderRepository.insert(newOrder);
        if (result > 0) {
            System.out.println(" Thêm đơn hàng thành công! OrderID = " + newOrder.getOrderId());

            // 2️ Kiểm tra cập nhật đơn hàng
            newOrder.setTotal(1000.0);
            int updateResult = orderRepository.update(newOrder);
            System.out.println(updateResult > 0 ? " Cập nhật đơn hàng thành công!" : " Lỗi khi cập nhật đơn hàng.");

            // 3️ Lấy đơn hàng theo ID
            Order foundOrder = orderRepository.selectById(newOrder.getOrderId());
            System.out.println(foundOrder != null ? " Tìm thấy đơn hàng: " + foundOrder : " Không tìm thấy đơn hàng.");

            // 4️ Lấy danh sách đơn hàng theo điều kiện (lọc theo customerID)
            List<Order> filteredOrders = orderRepository.selectByCondition("Customer_ID = ?", newOrder.getCustomer().getId());
            System.out.println(" Danh sách đơn hàng của khách hàng ID=" + newOrder.getCustomer().getId());
            for (Order order : filteredOrders) {
                System.out.println(order);
            }

            // 5️⃣ Xóa đơn hàng
            int deleteResult = orderRepository.delete(newOrder);
            System.out.println(deleteResult > 0 ? " Xóa đơn hàng thành công!" : " Lỗi khi xóa đơn hàng.");
        } else {
            System.out.println("Lỗi khi thêm đơn hàng.");
        }
    }
}
