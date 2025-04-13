package service;

import model.Service;
import repository.ServiceRepository;

import java.util.List;

public class ServiceService {

    private ServiceRepository serviceRepository;

    public ServiceService() {
        this.serviceRepository = ServiceRepository.getInstance();
    }

    // Thêm dịch vụ mới (kiểm tra trùng lặp và tính hợp lệ)
    public boolean addService(Service service) {
        if (isServiceValid(service) && !isServiceExists(service.getName())) {
            return serviceRepository.insert(service) > 0;
        }
        return false;
    }

    // Cập nhật thông tin dịch vụ (kiểm tra dịch vụ có tồn tại không)
    public boolean updateService(Service service) {
        if (isServiceExistsById(service.getServiceId())) {
            if (isServiceValid(service)) {
                return serviceRepository.update(service) > 0;
            }
        }
        return false;
    }

    // Xóa dịch vụ (kiểm tra dịch vụ có tồn tại không)
    public boolean deleteService(Service service) {
        if (isServiceExistsById(service.getServiceId())) {
            return serviceRepository.delete(service) > 0;
        }
        return false;
    }

    // Lấy tất cả dịch vụ
    public List<Service> getAllServices() {
        return serviceRepository.selectAll();
    }

    // Lấy dịch vụ theo ID
    public Service getServiceById(int serviceID) {
        return serviceRepository.selectById(serviceID);
    }

    // Lấy dịch vụ theo điều kiện (ví dụ: lọc theo tên dịch vụ, loại dịch vụ...)
    public List<Service> getServicesByCondition(String condition, Object... params) {
        return serviceRepository.selectByCondition(condition, params);
    }

    // Tìm kiếm dịch vụ theo tên
    public List<Service> searchServicesByName(String serviceName) {
        String condition = "serviceName LIKE ?";
        String searchPattern = "%" + serviceName + "%"; // Tìm kiếm theo mẫu tên
        return serviceRepository.selectByCondition(condition, searchPattern);
    }

    // Kiểm tra tính hợp lệ của dịch vụ (tên dịch vụ không được rỗng, giá trị hợp lệ)
    private boolean isServiceValid(Service service) {
        if (service.getName() == null || service.getName().trim().isEmpty()) {
            System.out.println("Tên dịch vụ không được rỗng.");
            return false;
        }
        if (service.getPrice() <= 0) {
            System.out.println("Giá dịch vụ phải lớn hơn 0.");
            return false;
        }
        return true;
    }

    // Kiểm tra xem dịch vụ đã tồn tại hay chưa (theo tên)
    private boolean isServiceExists(String serviceName) {
        List<Service> services = serviceRepository.selectByCondition("serviceName = ?", serviceName);
        return !services.isEmpty();
    }

    // Kiểm tra xem dịch vụ đã tồn tại theo ID hay chưa
    private boolean isServiceExistsById(int serviceID) {
        Service service = serviceRepository.selectById(serviceID);
        return service != null;
    }
    /**
     * Lấy tất cả dịch vụ đang còn hoạt động
     * @return Danh sách các dịch vụ đang hoạt động
     */
    public List<Service> getAllActiveServices() {
        String condition = "active = ?";
        return serviceRepository.selectByCondition(condition, true);
    }
}

