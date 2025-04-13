package controllers.admin;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Service;
import service.ServiceService;

public class ServiceController {

	@FXML
	private TableView<Service> serviceTable;
	@FXML
	private TableColumn<Service, Integer> serviceIdColumn;
	@FXML
	private TableColumn<Service, String> serviceNameColumn;
	@FXML
	private TableColumn<Service, Double> costPriceColumn;
	@FXML
	private TableColumn<Service, String> typeServiceColumn;
	@FXML
	private TableColumn<Service, String> descriptionColumn;

	@FXML
	private TextField searchTextField;
	@FXML
	private VBox formBox;
	@FXML
	private TextField serviceNameField;
	@FXML
	private TextField costPriceField;
	@FXML
	private ComboBox<String> typeServiceComboBox;
	@FXML
	private TextField descriptionField;
	@FXML
	private Button saveButton;

	private ServiceService serviceService;
	private ObservableList<Service> serviceList;
	private boolean isEditMode = false;
	private Service serviceBeingEdited = null;

	public ServiceController() {
		serviceService = new ServiceService();
	}

	@FXML
	public void initialize() {
		
		serviceIdColumn.setCellValueFactory(
				cellData -> new SimpleIntegerProperty(cellData.getValue().getServiceId()).asObject());

		serviceNameColumn
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

		costPriceColumn.setCellValueFactory(
				cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());

		typeServiceColumn.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));

		descriptionColumn
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		
		loadServiceData();

	}

	private void loadServiceData() {
		serviceList = FXCollections.observableArrayList(serviceService.getAllServices());
		serviceTable.getItems().setAll(serviceList);
	}

	@FXML
	private void handleSearch() {
		String searchTerm = searchTextField.getText().trim();
		if (searchTerm.isEmpty()) {
			loadServiceData();
			showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập tên dịch vụ để tìm kiếm.");
			return;
		}

		ObservableList<Service> searchResults = FXCollections
				.observableArrayList(serviceService.searchServicesByName(searchTerm));
		if (searchResults.isEmpty()) {
			showAlert(Alert.AlertType.INFORMATION, "Không có kết quả", "Không tìm thấy dịch vụ nào với tên đã nhập.");
		} else {
			serviceTable.setItems(searchResults);
		}
	}

	@FXML
	private void handleAddService() {
		clearFormFields();
		isEditMode = false;
		serviceBeingEdited = null;
		formBox.setVisible(true);
		formBox.setManaged(true);
	}

	@FXML
	private void handleEditService() {
		Service selectedService = serviceTable.getSelectionModel().getSelectedItem();
		if (selectedService == null) {
			showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một dịch vụ để sửa.");
			return;
		}

		serviceNameField.setText(selectedService.getName());
		costPriceField.setText(String.valueOf(selectedService.getPrice()));
		descriptionField.setText(selectedService.getDescription());

		isEditMode = true;
		serviceBeingEdited = selectedService;
		formBox.setVisible(true);
		formBox.setManaged(true);
	}

	@FXML
	private void handleDeleteService() {
	    Service selectedService = serviceTable.getSelectionModel().getSelectedItem();
	    if (selectedService == null) {
	        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một dịch vụ để xóa.");
	        return;
	    }

	    // Hiển thị hộp thoại xác nhận
	    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
	    confirmationAlert.setTitle("Xác nhận xóa");
	    confirmationAlert.setHeaderText("Bạn có chắc chắn muốn xóa dịch vụ này?");
	    confirmationAlert.setContentText("Tên dịch vụ: " + selectedService.getName());

	    ButtonType buttonYes = new ButtonType("OK", ButtonBar.ButtonData.YES);
	    ButtonType buttonNo = new ButtonType("Hủy", ButtonBar.ButtonData.NO);
	    confirmationAlert.getButtonTypes().setAll(buttonYes, buttonNo);

	    // Hiển thị và chờ người dùng phản hồi
	    confirmationAlert.showAndWait().ifPresent(response -> {
	        if (response == buttonYes) {
	            boolean success = serviceService.deleteService(selectedService);
	            if (success) {
	                serviceList.remove(selectedService);
	                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Dịch vụ đã được xóa.");
	                serviceTable.getItems().setAll(serviceList);
	            } else {
	                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xóa dịch vụ không thành công.");
	            }
	        }
	        
	    });
	}

	@FXML
	private void handleSaveService() {
		String name = serviceNameField.getText().trim();
		String costStr = costPriceField.getText().trim();
		String selectedTypeDesc = typeServiceComboBox.getValue();
		String description = descriptionField.getText().trim();

		if (name.isEmpty() || costStr.isEmpty() || selectedTypeDesc == null || description.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ thông tin dịch vụ.");
			return;
		}

		double cost;
		try {
			cost = Double.parseDouble(costStr);
		} catch (NumberFormatException e) {
			showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giá dịch vụ phải là số.");
			return;
		}

		if (isEditMode && serviceBeingEdited != null) {
			serviceBeingEdited.setName(name);
			serviceBeingEdited.setPrice(cost);
			serviceBeingEdited.setDescription(description);

			if (serviceService.updateService(serviceBeingEdited)) {
				serviceTable.refresh();
				showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật dịch vụ thành công.");
			} else {
				showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể cập nhật dịch vụ.");
			}
		} else {
			Service newService = new Service(0, name, cost description);
			if (serviceService.addService(newService)) {
				serviceList.add(newService);
				serviceTable.getItems().setAll(serviceList);
				showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm dịch vụ mới.");
			} else {
				showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể thêm dịch vụ.");
			}
		}

		clearFormFields();
		formBox.setVisible(false);
		formBox.setManaged(false);
	}

	private void clearFormFields() {
		serviceNameField.clear();
		costPriceField.clear();
		typeServiceComboBox.setValue(null);
		descriptionField.clear();
	}

	private void showAlert(Alert.AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
