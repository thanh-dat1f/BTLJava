package service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import enums.PaymentMethodEnum;
import enums.StatusEnum;
import model.Invoice;
import model.Order;
import model.OrderDetail;
import model.Staff;
import repository.InvoiceRepository;
import repository.OrderDetailRepository;
import repository.OrderRepository;
import utils.Session;

/**
 * Lớp dịch vụ xử lý các chức năng liên quan đến hóa đơn
 */
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    /**
     * Khởi tạo InvoiceService với các repository cần thiết
     */
    public InvoiceService() {
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.orderRepository = OrderRepository.getInstance();
        this.orderDetailRepository = OrderDetailRepository.getInstance();
    }

    /**
     * Lấy tất cả hóa đơn
     * @return Danh sách hóa đơn
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.selectAll();
    }

    /**
     * Lấy hóa đơn theo ID
     * @param invoiceId ID của hóa đơn
     * @return Hóa đơn tìm thấy, null nếu không tìm thấy
     */
    public Invoice getInvoiceById(int invoiceId) {
        return invoiceRepository.selectById(invoiceId);
    }

    /**
     * Lấy hóa đơn theo khoảng thời gian
     * @param startDate Thời gian bắt đầu
     * @param endDate Thời gian kết thúc
     * @return Danh sách hóa đơn trong khoảng thời gian
     */
    public List<Invoice> getInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String condition = "payment_date BETWEEN ? AND ?";
        return invoiceRepository.selectByCondition(condition, 
                java.sql.Timestamp.valueOf(startDate), 
                java.sql.Timestamp.valueOf(endDate));
    }

    /**
     * Lấy danh sách hóa đơn gần đây
     * @param days Số ngày gần đây
     * @return Danh sách hóa đơn
     */
    public List<Invoice> getRecentInvoices(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        String condition = "payment_date >= ?";
        return invoiceRepository.selectByCondition(condition, java.sql.Timestamp.valueOf(startDate));
    }

    /**
     * Tạo hóa đơn mới
     * @param orderId ID của đơn hàng
     * @param paymentMethod Phương thức thanh toán
     * @return true nếu tạo thành công, false nếu thất bại
     */
    public boolean createInvoice(int orderId, PaymentMethodEnum paymentMethod) {
        try {
            // Kiểm tra đơn hàng tồn tại
            Order order = orderRepository.selectById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("Đơn hàng không tồn tại");
            }
    
            // Lấy thông tin nhân viên hiện tại
            Staff staff = Session.getCurrentStaff();
            if (staff == null) {
                throw new IllegalArgumentException("Không có thông tin nhân viên");
            }
    
            // Tạo hóa đơn mới
            Invoice invoice = new Invoice();
            invoice.setOrder(order);
            invoice.setPaymentDate(new java.sql.Timestamp(System.currentTimeMillis()));
            invoice.setTotal(BigDecimal.valueOf(order.getTotalAmount()));
            invoice.setPaymentMethod(paymentMethod);
            invoice.setStatus(StatusEnum.COMPLETED);
            invoice.setStaff(staff);
    
            // Lưu hóa đơn vào database
            return invoiceRepository.insert(invoice) > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo hóa đơn: " + e.getMessage());
            return false;
        }
    }

    /**
     * In hóa đơn
     * @param invoiceId ID của hóa đơn
     */
    public void printInvoice(int invoiceId) {
        try {
            // Kiểm tra hóa đơn tồn tại
            Invoice invoice = invoiceRepository.selectById(invoiceId);
            if (invoice == null) {
                throw new IllegalArgumentException("Hóa đơn không tồn tại");
            }

            // Tạo file PDF hóa đơn
            String filePath = "invoice_" + invoiceId + ".pdf";
            generateInvoicePDF(invoice.getOrder().getOrderId(), filePath);
            
            // Mở file PDF để in
            File pdfFile = new File(filePath);
            if (pdfFile.exists()) {
                java.awt.Desktop.getDesktop().open(pdfFile);
                System.out.println("Đã mở file hóa đơn: " + filePath);
            } else {
                System.err.println("Không tìm thấy file hóa đơn: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi in hóa đơn: " + e.getMessage());
        }
    }

    /**
     * Gửi hóa đơn qua email
     * @param invoiceId ID của hóa đơn
     * @throws Exception Nếu có lỗi xảy ra
     */
    public void sendInvoiceByEmail(int invoiceId) throws Exception {
        try {
            // Kiểm tra hóa đơn tồn tại
            Invoice invoice = invoiceRepository.selectById(invoiceId);
            if (invoice == null) {
                throw new IllegalArgumentException("Hóa đơn không tồn tại");
            }

            // Tạo file PDF hóa đơn
            String filePath = "invoice_" + invoiceId + ".pdf";
            generateInvoicePDF(invoice.getOrder().getOrderId(), filePath);

            // Lấy email của khách hàng
            String customerEmail = invoice.getOrder().getCustomer().getEmail();
            if (customerEmail == null || customerEmail.isEmpty()) {
                throw new IllegalArgumentException("Email khách hàng không có");
            }

            // Gửi email (giả lập)
            System.out.println("Đã gửi hóa đơn qua email đến: " + customerEmail);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi hóa đơn qua email: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Tạo file PDF hóa đơn
     * @param orderId ID của đơn hàng
     * @param filePath Đường dẫn lưu file
     */
    public void generateInvoicePDF(int orderId, String filePath) {
        try {
            // Lấy thông tin đơn hàng
            Order order = orderRepository.selectById(orderId);
            if (order == null) {
                System.out.println("Không tìm thấy đơn hàng.");
                return;
            }

            // Lấy thông tin chi tiết đơn hàng
            String condition = "order_id = ?";
            List<OrderDetail> orderDetails = orderDetailRepository.selectByCondition(condition, orderId);

            // Tạo file PDF
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                // Thay thế code tạo PDF bằng phương pháp khác nếu không dùng thư viện iText
                String content = generateInvoiceContent(order, orderDetails);
                outputStream.write(content.getBytes());
            }

            System.out.println("Hóa đơn đã được tạo thành công: " + filePath);
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo file PDF hóa đơn: " + e.getMessage());
        }
    }

    /**
     * Tạo nội dung hóa đơn
     * @param order Đơn hàng
     * @param orderDetails Chi tiết đơn hàng
     * @return Nội dung hóa đơn
     */
    private String generateInvoiceContent(Order order, List<OrderDetail> orderDetails) {
        StringBuilder content = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Tiêu đề
        content.append("HÓA ĐƠN BÁN HÀNG\n");
        content.append("-----------------------------------\n\n");

        // Thông tin hóa đơn
        content.append("Mã đơn hàng: ").append(order.getOrderId()).append("\n");
        content.append("Ngày: ").append(order.getOrderDate() != null ? 
                order.getOrderDate().toLocalDateTime().format(dateFormatter) : "N/A").append("\n");
        content.append("Khách hàng: ").append(order.getCustomer() != null ? 
                order.getCustomer().getFullName() : "N/A").append("\n");
        content.append("Nhân viên: ").append(order.getStaff() != null ? 
                order.getStaff().getFullName() : "N/A").append("\n\n");

        // Chi tiết đơn hàng
        content.append("CHI TIẾT ĐƠN HÀNG\n");
        content.append("-----------------------------------\n");
        content.append("STT | Tên dịch vụ | Số lượng | Đơn giá | Thành tiền\n");

        double total = 0;
        int index = 1;
        for (OrderDetail detail : orderDetails) {
            String serviceName = detail.getService() != null ? detail.getService().getName() : "N/A";
            int quantity = detail.getQuantity();
            double price = detail.getPrice().doubleValue();
            double amount = quantity * price;
            total += amount;

            content.append(index).append(" | ");
            content.append(serviceName).append(" | ");
            content.append(quantity).append(" | ");
            content.append(String.format("%,.0f", price)).append(" | ");
            content.append(String.format("%,.0f", amount)).append("\n");
            index++;
        }

        content.append("-----------------------------------\n");
        content.append("Tổng cộng: ").append(String.format("%,.0f VNĐ", total)).append("\n");

        // Thông tin khuyến mãi nếu có
        if (order.getVoucher() != null) {
            int discountPercent = order.getVoucher().getDiscountPercent();
            double discountAmount = total * discountPercent / 100;
            double finalTotal = total - discountAmount;
            
            content.append("Giảm giá (").append(discountPercent).append("%): ");
            content.append(String.format("%,.0f VNĐ", discountAmount)).append("\n");
            content.append("Thành tiền: ").append(String.format("%,.0f VNĐ", finalTotal)).append("\n");
        }

        // Chân trang
        content.append("\n-----------------------------------\n");
        content.append("Cảm ơn quý khách đã sử dụng dịch vụ!\n");
        content.append("Thời gian in: ").append(LocalDateTime.now().format(dateFormatter));

        return content.toString();
    }
    public boolean updateInvoice(Invoice invoice) {
        try {
            return invoiceRepository.update(invoice) > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật hóa đơn: " + e.getMessage());
            return false;
        }
    }
}