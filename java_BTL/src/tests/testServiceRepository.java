package tests;

import java.util.List;

import enums.TypeServiceEnum;
import model.Service;
import repository.ServiceRepository;

public class testServiceRepository {
	public static void main(String[] args) {
		ServiceRepository serviceRepository = ServiceRepository.getInstance();

		System.out.println("===== TEST INSERT =====");
		Service testService = new Service(0, "Dịch vụ tắm spa", 150000, TypeServiceEnum.BASIC, "Tắm gội, vệ sinh tai");
		int insertResult = serviceRepository.insert(testService);
		if (insertResult > 0) {
			System.out.println("Thêm thành công! ID mới: " + testService.getServiceID());
		} else {
			System.out.println("Thêm thất bại!");
		}

		System.out.println("\n===== TEST SELECT BY ID =====");
		Service foundService = serviceRepository.selectById(testService.getServiceID());
		if (foundService != null) {
			System.out.println("Tìm thấy dịch vụ: " + foundService.getServiceName());
		} else {
			System.out.println("Không tìm thấy dịch vụ!");
		}

		System.out.println("\n===== TEST UPDATE =====");
		testService.setServiceName("Dịch vụ VIP Tắm Spa");
		int updateResult = serviceRepository.update(testService);
		if (updateResult > 0) {
			System.out.println("Cập nhật thành công!");
		} else {
			System.out.println("Cập nhật thất bại!");
		}

		System.out.println("\n===== TEST SELECT ALL =====");
		List<Service> services = serviceRepository.selectAll();
		System.out.println("Danh sách dịch vụ:");
		for (Service s : services) {
			System.out.println(s.getServiceID() + " - " + s.getServiceName() + " - " + s.getCostPrice());
		}

		System.out.println("\n===== TEST DELETE =====");
		int deleteResult = serviceRepository.delete(testService);
		if (deleteResult > 0) {
			System.out.println("Xóa thành công!");
		} else {
			System.out.println("Xóa thất bại!");
		}
	}
}
