package repository;

import java.io.File;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

import enums.PaymentMethodEnum;
import enums.StatusEnum;
import model.Booking;
import model.Invoice;
import model.Order;
import model.OrderDetail;
import model.Staff;
import repository.BookingRepository;
import repository.InvoiceRepository;
import repository.OrderDetailRepository;

import utils.Session;

public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BookingRepository bookingRepository;

    public InvoiceService() {
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.orderRepository = OrderRepository.getInstance();
        this.orderDetailRepository = OrderDetailRepository.getInstance();
        this.bookingRepository = BookingRepository.getInstance();
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.selectAll();
    }

    public Invoice getInvoiceById(int invoiceId) {
        return invoiceRepository.selectById(invoiceId);
    }

    public List<Invoice> getInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String condition = "payment_date BETWEEN ? AND ?";
        return invoiceRepository.selectByCondition(condition, java.sql.Timestamp.valueOf(startDate), 
                                                   java.sql.Timestamp.valueOf(endDate));
    }

    public List<Invoice> getRecentInvoices(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        String condition = "payment_date >= ?";
        return invoiceRepository.selectByCondition(condition, java.sql.Timestamp.valueOf(startDate));
    }

    public boolean createInvoice(int orderId, PaymentMethodEnum paymentMethod) {
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không tồn tại");
        }

        Staff staff = Session.getCurrentStaff();
        if (staff == null) {
            throw new IllegalArgumentException("Không có thông tin nhân viên");
        }

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setPaymentDate(new java.sql.Timestamp(System.currentTimeMillis()));
        invoice.setTotal(BigDecimal.valueOf(order.getTotalAmount()));
        invoice.setPaymentMethod(paymentMethod);
        invoice.setStatus(StatusEnum.COMPLETED);
        invoice.setStaff(staff);

        return invoiceRepository.insert(invoice) > 0;
    }

    public void printInvoice(int invoiceId) {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        // Implementation to print the invoice
        String filePath = "invoice_" + invoiceId + ".pdf";
        generateInvoicePDF(invoice.getOrder().getOrderId(), filePath);
        
        // Here you would add code to send to printer
        System.out.println("Đã gửi hóa đơn " + invoiceId + " đến máy in");
    }

    public void sendInvoiceByEmail(int invoiceId) throws Exception {
        Invoice invoice = invoiceRepository.selectById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Hóa đơn không tồn tại");
        }

        // Generate PDF
        String filePath = "invoice_" + invoiceId + ".pdf";
        generateInvoicePDF(invoice.getOrder().getOrderId(), filePath);

        // Get customer email
        String customerEmail = invoice.getOrder().getCustomer().getEmail();
        if (customerEmail == null || customerEmail.isEmpty()) {
            throw new IllegalArgumentException("Email khách hàng không có");
        }

        // Send email
        sendEmail(customerEmail, "Hóa đơn #" + invoiceId, 
                 "Xin chào,\n\nVui lòng xem hóa đơn đính kèm.\n\nTrân trọng,\nPet Service", 
                 filePath);
    }

    private void sendEmail(String to, String subject, String text, String attachmentPath) throws Exception {
        // Email sending implementation
        // This is just a placeholder - you would implement actual email sending
        System.out.println("Email sent to: " + to + " with attachment: " + attachmentPath);
    }

    // Implement the method to generate PDF invoice
    private void generateInvoicePDF(int orderId, String filePath) {
        // Implementation for PDF generation
        // This is just a placeholder
        System.out.println("Generated PDF invoice at: " + filePath);
    }
}