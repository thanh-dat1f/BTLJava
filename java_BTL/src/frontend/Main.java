package frontend;

import controllers.SceneSwitcher;


import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneSwitcher.setMainStage(primaryStage);
        SceneSwitcher.switchScene("home.fxml"); 
    }

   
    public static void main(String[] args) {
        launch(args);
    }
}

//
//import service.InvoiceService;
//import repository.InvoiceRepository;
//import repository.OrderRepository;
//import repository.OrderDetailRepository;
//import model.Invoice;
//import model.Order;
//import model.OrderDetail;
//
//import java.awt.Desktop;
//import java.io.File;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//public class Main {
//    public static void main(String[] args) {
//        // Tạo các mock repository
//        InvoiceRepository invoiceRepository = new InvoiceRepository();
//        OrderRepository orderRepository = new OrderRepository();
//        OrderDetailRepository orderDetailRepository = new OrderDetailRepository();
//
//        // Tạo đối tượng InvoiceService với các repository mock
//        InvoiceService invoiceService = new InvoiceService(invoiceRepository, orderRepository, orderDetailRepository);
//
//        // Đường dẫn tới file PDF đầu ra
//        String filePath = "resources/xuatHD/Invoice.pdf";
//
//        // Gọi phương thức để tạo hóa đơn PDF
//        invoiceService.generateInvoicePDF(1, filePath);
//        
//     // Mở file PDF sau khi tạo
//        try {
//            File pdfFile = new File(filePath);
//            if (pdfFile.exists()) {
//                Desktop.getDesktop().open(pdfFile); 
//            } else {
//                System.out.println("File không tồn tại!");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
//

