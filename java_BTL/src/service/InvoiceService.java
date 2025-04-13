package service;

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

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;

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

public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public InvoiceService() {
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.orderRepository = OrderRepository.getInstance();
        this.orderDetailRepository = OrderDetailRepository.getInstance();
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

    // Phương thức xuất hóa đơn ra file PDF
    public void generateInvoicePDF(int orderId, String filePath) {
        try {
            // Tạo PdfFont với phông Arial Unicode MS từ hệ thống (hoặc phông hỗ trợ tiếng Việt)
            PdfFont vietnameseFont = PdfFontFactory.createFont("C:/Windows/Fonts/arial.ttf",
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

            // 1. Lấy thông tin đơn hàng từ OrderRepository
            Order order = orderRepository.selectById(orderId);
            if (order == null) {
                System.out.println("Không tìm thấy đơn hàng.");
                return;
            }

            // 2. Lấy thông tin hóa đơn từ InvoiceRepository
            List<Invoice> invoices = invoiceRepository.selectByCondition("order_id = ?", orderId);
            if (invoices.isEmpty()) {
                System.out.println("Không tìm thấy hóa đơn.");
                return;
            }
            Invoice invoice = invoices.get(0);

            // 3. Lấy thông tin chi tiết đơn hàng từ OrderDetailRepository
            List<OrderDetail> orderDetails = orderDetailRepository.selectByCondition("order_id = ?", orderId);

            // Tạo PdfWriter
            PdfWriter writer = new PdfWriter(filePath);

            // Tạo PdfDocument
            PdfDocument pdfDoc = new PdfDocument(writer);

            // Tạo Document từ PdfDocument
            Document document = new Document(pdfDoc);

            // Tiêu đề (Hóa đơn bán hàng)
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
            Paragraph title = new Paragraph(
                    new Text("HÓA ĐƠN SỐ " + invoice.getInvoiceId()).setFont(boldFont).setFont(vietnameseFont))
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(20);
            document.add(title);

            // Thông tin nhân viên (có thể lấy từ Order hoặc thông tin hệ thống)
            Paragraph staffInfo = new Paragraph("Nhân viên: " + order.getStaff().getFullName()).setFont(vietnameseFont)
                    .setTextAlignment(TextAlignment.LEFT).setFontSize(12);
            document.add(staffInfo);

            // Thông tin người mua
            Paragraph buyerInfo = new Paragraph("KHÁCH HÀNG: " + order.getCustomer().getFullName() + "\n"
                    + "Số điện thoại: " + order.getCustomer().getPhone() + "\n").setFont(vietnameseFont)
                    .setTextAlignment(TextAlignment.LEFT).setFontSize(12);
            document.add(buyerInfo);

            // Thông tin hóa đơn
            document.add(new Paragraph("Mã hóa đơn: " + invoice.getInvoiceId()).setFont(vietnameseFont));

            // Tạo bảng chi tiết đơn hàng
            Table table = new Table(5); // 5 cột: Mã dịch vụ, Tên dịch vụ, Số lượng, Đơn giá, Thành tiền
            table.addCell(
                    new Cell().add(new Paragraph(new Text("Mã dịch vụ").setFont(boldFont).setFont(vietnameseFont)))); // Mã dịch vụ
            table.addCell(
                    new Cell().add(new Paragraph(new Text("Tên dịch vụ").setFont(boldFont).setFont(vietnameseFont)))); // Tên dịch vụ
            table.addCell(
                    new Cell().add(new Paragraph(new Text("Số lượng").setFont(boldFont).setFont(vietnameseFont)))); // Số lượng
            table.addCell(new Cell().add(new Paragraph(new Text("Đơn giá").setFont(boldFont).setFont(vietnameseFont)))); // Đơn giá
            table.addCell(
                    new Cell().add(new Paragraph(new Text("Thành tiền").setFont(boldFont).setFont(vietnameseFont)))); // Thành tiền

            // Thêm chi tiết vào bảng
            for (OrderDetail detail : orderDetails) {
                table.addCell(new Cell().add(
                        new Paragraph(String.valueOf(detail.getService().getServiceId())).setFont(vietnameseFont)));
                table.addCell(new Cell().add(new Paragraph(detail.getService().getName())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.getQuantity()))));
                table.addCell(new Cell().add(new Paragraph(detail.getPrice().toString())));
                table.addCell(new Cell().add(new Paragraph(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())).toString())));
            }

            document.add(table);

            // Thông tin thanh toán
            Paragraph paymentInfo = new Paragraph("Tổng số tiền: " + invoice.getTotal() + " VNĐ\n")
                    .setFont(vietnameseFont).setTextAlignment(TextAlignment.LEFT).setFontSize(12);
            document.add(paymentInfo);

            // Ngày xuất hóa đơn (in bill)
            Paragraph printDate = new Paragraph("Ngày in hóa đơn: " + java.time.LocalDate.now()).setFont(vietnameseFont)
                    .setTextAlignment(TextAlignment.RIGHT).setFontSize(12);
            document.add(printDate);

            document.close();
            System.out.println("Hóa đơn đã được tạo thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}