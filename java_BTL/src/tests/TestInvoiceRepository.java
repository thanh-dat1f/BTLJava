package tests;

import model.Invoice;
import model.Order;
import model.PaymentStatus;
import repository.InvoiceRepository;
import repository.OrderRepository;
import repository.PaymentStatusRepository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class TestInvoiceRepository {
    public static void main(String[] args) {
        InvoiceRepository invoiceRepository = new InvoiceRepository();
        OrderRepository orderRepository = new OrderRepository();
        PaymentStatusRepository paymentStatusRepository = new PaymentStatusRepository();
        
        
        Order order = orderRepository.selectById(3);
        if (order == null) {
            System.out.println("Không tìm thấy Order có ID = 3. Vui lòng kiểm tra lại dữ liệu!");
            return;
        }
        
        PaymentStatus payment = paymentStatusRepository.selectById(1);
        
        
        // 1. Test Insert
        
        Invoice newInvoice = new Invoice(0, order, BigDecimal.valueOf(order.getTotal()), new Timestamp(System.currentTimeMillis()), payment);

        int insertResult = invoiceRepository.insert(newInvoice);
        System.out.println("Insert Result: " + insertResult);
        System.out.println("Inserted Invoice ID: " + newInvoice.getInvoiceId());

        // 2. Test Update
        newInvoice.setTotalAmount(BigDecimal.valueOf(order.getTotal()));
        int updateResult = invoiceRepository.update(newInvoice);
        System.out.println("Update Result: " + updateResult);

        // 3. Test Select By ID
        Invoice selectedInvoice = invoiceRepository.selectById(newInvoice.getInvoiceId());
        System.out.println("Selected Invoice: " + selectedInvoice);

        // 4. Test Select All
        List<Invoice> invoices = invoiceRepository.selectAll();
        System.out.println("List of Invoices: ");
        for (Invoice invoice : invoices) {
            System.out.println(invoice);
        }

        // 5. Test Delete
        int deleteResult = invoiceRepository.delete(newInvoice);
        System.out.println("Delete Result: " + deleteResult);
    }
}
