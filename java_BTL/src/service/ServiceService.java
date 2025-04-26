package service;

import model.Service;
import repository.ServiceRepository;

import java.util.ArrayList;
import java.util.List;

public class ServiceService {

    private ServiceRepository serviceRepository;

    public ServiceService() {
        this.serviceRepository = ServiceRepository.getInstance();
    }

    /**
     * Thêm dịch vụ mới
     * @param service Thông tin dịch vụ cần thêm
     * @return true nếu thêm thành công, false nếu không
     */
    public boolean addService(Service service) {
        if (!isServiceValid(service)) {
            return false;
        }
        
        // Kiểm tra dịch vụ trùng tên
        List<Service> existingServices = searchServicesByName(service.getName());
        if (!existingServices.isEmpty()) {
            System.out.println("Dịch vụ với tên này đã tồn tại.");
            return false;
        }
        
        return serviceRepository.insert(service) > 0;
    }

    /**
     * Cập nhật thông tin dịch vụ
     * @param service Thông tin dịch vụ mới
     * @return true nếu cập nhật thành công, false nếu không
     */
    public boolean updateService(Service service) {
        if (!isServiceExistsById(service.getServiceId())) {
            System.out.println("Không tìm thấy dịch vụ với ID: " + service.getServiceId());
            return false;
        }
        
        if (!isServiceValid(service)) {
            return false;
        }
        
        return serviceRepository.update(service) > 0;
    }

    /**
     * Xóa dịch vụ
     * @param service Dịch vụ cần xóa
     * @return true nếu xóa thành công, false nếu không
     */
    public boolean deleteService(Service service) {
        if (!isServiceExistsById(service.getServiceId())) {
            System.out.println("Không tìm thấy dịch vụ với ID: " + service.getServiceId());
            return false;
        }
        
        return serviceRepository.delete(service) > 0;
    }

    /**
     * Lấy tất cả dịch vụ
     * @return Danh sách dịch vụ
     */
    public List<Service> getAllServices() {
        return serviceRepository.selectAll();
    }

    /**
     * Lấy dịch vụ theo ID
     * @param serviceId ID dịch vụ cần lấy
     * @return Dịch vụ nếu tìm thấy, null nếu không tìm thấy
     */
    public Service getServiceById(int serviceId) {
        return serviceRepository.selectById(serviceId);
    }

    /**
     * Tìm kiếm dịch vụ theo tên
     * @param serviceName Tên dịch vụ cần tìm
     * @return Danh sách dịch vụ tìm thấy
     */
    public List<Service> searchServicesByName(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String condition = "name LIKE ?";
        String searchPattern = "%" + serviceName + "%";
        return serviceRepository.selectByCondition(condition, searchPattern);
    }

    /**
     * Lấy tất cả dịch vụ đang còn hoạt động
     * @return Danh sách các dịch vụ đang hoạt động
     */
    public List<Service> getAllActiveServices() {
        String condition = "active = ?";
        return serviceRepository.selectByCondition(condition, true);
    }

    /**
     * Kiểm tra tính hợp lệ của dịch vụ
     * @param service Dịch vụ cần kiểm tra
     * @return true nếu dịch vụ hợp lệ, false nếu không
     */
    private boolean isServiceValid(Service service) {
        if (service == null) {
            System.out.println("Dịch vụ không được phép null");
            return false;
        }
        
        if (service.getName() == null || service.getName().trim().isEmpty()) {
            System.out.println("Tên dịch vụ không được để trống");
            return false;
        }
        
        if (service.getPrice() <= 0) {
            System.out.println("Giá dịch vụ phải lớn hơn 0");
            return false;
        }
        
        return true;
    }

    /**
     * Kiểm tra dịch vụ có tồn tại không (dựa trên ID)
     * @param serviceId ID dịch vụ cần kiểm tra
     * @return true nếu dịch vụ tồn tại, false nếu không
     */
    private boolean isServiceExistsById(int serviceId) {
        return serviceRepository.selectById(serviceId) != null;
    }
}