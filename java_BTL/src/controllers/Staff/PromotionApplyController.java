package controllers.Staff;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Promotion;
import model.Service;
import service.PromotionService;
import service.ServiceService;

import utils.RoleChecker; 
public class PromotionApplyController implements Initializable {

    @FXML
    private TableView<Service> serviceTable;
    
    @FXML
    private TableColumn<Service, Integer> idColumn;
    
    @FXML
    private TableColumn<Service, String> nameColumn;
    
    @FXML
    private TableColumn<Service, Double> priceColumn;
    
    @FXML
    private TableColumn<Service, Integer> durationColumn;
    
    @FXML
    private ComboBox<Promotion> promotionComboBox;
    
    @FXML
    private TextField promoCodeField;
    
    @FXML
    private Label discountLabel;
    
    @FXML
    private Label totalPriceLabel;
    
    @FXML
    private Label discountedPriceLabel;
    
    @FXML
    private Button applyButton;
    
    @FXML
    private Button removePromoButton;
    
    private ServiceService serviceService;
    private PromotionService promotionService;
    private ObservableList<Service> serviceList;
    private ObservableList<Service> selectedServices = FXCollections.observableArrayList();
    private Promotion selectedPromotion;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo service
        serviceService = new ServiceService();
        promotionService = new PromotionService();
        
        // Khởi tạo các cột cho bảng
        idColumn.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        
        // Format số tiền
        priceColumn.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<Service, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f VND", item));
                    }
                }
            };
        });
        
        // Tải dữ liệu dịch vụ
        loadServices();
        
        // Tải danh sách khuyến mãi
        loadPromotions();
        
        // Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
        setupButtonVisibility();
        
        // Xử lý sự kiện khi chọn dịch vụ
        serviceTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null && !selectedServices.contains(newValue)) {
                    selectedServices.add(newValue);
                    updateTotalPrice();
                }
            });
        
        // Xử lý sự kiện khi chọn khuyến mãi
        promotionComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedPromotion = newValue;
                if (newValue != null) {
                    promoCodeField.setText(newValue.getCode());
                    discountLabel.setText(newValue.getDiscountPercent() + "%");
                    updateTotalPrice();
                }
            });
    }
    
    /**
     * Thiết lập hiển thị/ẩn các nút dựa trên quyền của người dùng
     */
    private void setupButtonVisibility() {
        boolean canApplyPromotion = RoleChecker.hasPermission("APPLY_PROMOTION");
        
        promotionComboBox.setDisable(!canApplyPromotion);
        promoCodeField.setDisable(!canApplyPromotion);
        applyButton.setDisable(!canApplyPromotion);
        removePromoButton.setDisable(!canApplyPromotion);
    }
    
    /**
     * Tải danh sách dịch vụ
     */
 // Modify loadServices() method to use the correct method from ServiceService
    private void loadServices() {
        try {
            // Use the available method from ServiceService
            List<Service> services = serviceService.getAllServices();
            // Filter active services if needed
            services = services.stream()
                          .filter(Service::isActive)
                          .collect(java.util.stream.Collectors.toList());
                          
            serviceList = FXCollections.observableArrayList(services);
            serviceTable.setItems(serviceList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách dịch vụ", e.getMessage());
        }
    }
    
    /**
     * Tải danh sách khuyến mãi còn hiệu lực
     */
    private void loadPromotions() {
        try {
            List<Promotion> promotions = promotionService.getActivePromotions();
            promotionComboBox.setItems(FXCollections.observableArrayList(promotions));
            
            // Tùy chỉnh hiển thị của ComboBox
            promotionComboBox.setCellFactory(p -> new javafx.scene.control.ListCell<Promotion>() {
                @Override
                protected void updateItem(Promotion item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getCode() + " (" + item.getDiscountPercent() + "%)");
                    }
                }
            });
            
            // Tùy chỉnh hiển thị của button ComboBox
            promotionComboBox.setButtonCell(new javafx.scene.control.ListCell<Promotion>() {
                @Override
                protected void updateItem(Promotion item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Chọn khuyến mãi");
                    } else {
                        setText(item.getCode() + " (" + item.getDiscountPercent() + "%)");
                    }
                }
            });
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách khuyến mãi", e.getMessage());
        }
    }
    
    /**
     * Cập nhật tính toán giá và giảm giá
     */
    private void updateTotalPrice() {
        double totalPrice = 0;
        
        // Tính tổng giá
        for (Service service : selectedServices) {
            totalPrice += service.getPrice();
        }
        
        // Hiển thị tổng giá
        totalPriceLabel.setText(String.format("%,.0f VND", totalPrice));
        
        // Tính và hiển thị giá sau khi giảm
        if (selectedPromotion != null) {
            double discountPercent = selectedPromotion.getDiscountPercent();
            double discountedPrice = totalPrice * (1 - discountPercent / 100);
            discountedPriceLabel.setText(String.format("%,.0f VND", discountedPrice));
        } else {
            discountedPriceLabel.setText(String.format("%,.0f VND", totalPrice));
        }
    }
    
    /**
     * Xác nhận và áp dụng mã khuyến mãi
     */
    @FXML
    private void applyPromotion(ActionEvent event) {
        if (selectedServices.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn dịch vụ", 
                    "Vui lòng chọn ít nhất một dịch vụ để áp dụng khuyến mãi.");
            return;
        }
        
        if (selectedPromotion == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn khuyến mãi", 
                    "Vui lòng chọn một mã khuyến mãi để áp dụng.");
            return;
        }
        
        try {
            // Kiểm tra xem mã khuyến mãi có hợp lệ hay không
            String promoCode = promoCodeField.getText();
            Promotion promotion = promotionService.getPromotionByCode(promoCode);
            
            if (promotion == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã khuyến mãi không hợp lệ", 
                        "Mã khuyến mãi không tồn tại hoặc đã hết hạn.");
                return;
            }
            
            // Kiểm tra ngày hết hạn
            if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mã khuyến mãi đã hết hạn", 
                        "Mã khuyến mãi này đã hết hạn vào ngày " + promotion.getEndDate());
                return;
            }
            
            // Áp dụng khuyến mãi
            selectedPromotion = promotion;
            discountLabel.setText(promotion.getDiscountPercent() + "%");
            updateTotalPrice();
            
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã áp dụng khuyến mãi", 
                    "Mã khuyến mãi " + promotion.getCode() + " đã được áp dụng thành công.");
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể áp dụng khuyến mãi", e.getMessage());
        }
    }
    
    /**
     * Xóa mã khuyến mãi đã chọn
     */
    @FXML
    private void removePromotion(ActionEvent event) {
        selectedPromotion = null;
        promoCodeField.clear();
        discountLabel.setText("0%");
        updateTotalPrice();
        
        // Xóa lựa chọn trong ComboBox
        promotionComboBox.getSelectionModel().clearSelection();
        
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã xóa khuyến mãi", 
                "Đã xóa mã khuyến mãi đang áp dụng.");
    }
    
    /**
     * Xóa dịch vụ đã chọn
     */
    @FXML
    private void removeSelectedService() {
        Service selectedService = serviceTable.getSelectionModel().getSelectedItem();
        if (selectedService != null) {
            selectedServices.remove(selectedService);
            updateTotalPrice();
        }
    }
    
    /**
     * Xóa tất cả dịch vụ đã chọn
     */
    @FXML
    private void clearAllServices() {
        selectedServices.clear();
        updateTotalPrice();
    }
    
    /**
     * Hiển thị thông báo
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}