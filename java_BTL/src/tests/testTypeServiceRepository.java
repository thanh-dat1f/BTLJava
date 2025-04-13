package tests;

import java.util.List;

import enums.TypeServiceEnum;
import model.TypeService;
import repository.TypeServiceRepository;

public class testTypeServiceRepository {
	public static void main(String[] args) {
		TypeServiceRepository repository = TypeServiceRepository.getInstance();

		System.out.println("\n========== TEST TYPE SERVICE REPOSITORY ==========\n");

		// 1️⃣ Thêm mới loại dịch vụ
		TypeService newTypeService = new TypeService(0, TypeServiceEnum.VIP); // ID sẽ được tự động gán sau khi insert
		int inserted = repository.insert(newTypeService);
		System.out.println("🟢 [INSERT] Thêm loại dịch vụ: " + (inserted > 0 ? "THÀNH CÔNG" : "THẤT BẠI"));

		// 2️⃣ Lấy danh sách tất cả loại dịch vụ
		List<TypeService> services = repository.selectAll();
		System.out.println("\n📌 [SELECT ALL] Danh sách loại dịch vụ:");
		if (services.isEmpty()) {
			System.out.println("⚠️ Không có dữ liệu.");
		} else {
			for (TypeService service : services) {
				System.out.println(" - ID: " + service.getTypeServiceID() + " | Name: "
						+ service.getTypeServiceName().getDescription());
			}
		}

		// 3️⃣ Cập nhật loại dịch vụ
		if (!services.isEmpty()) {
			TypeService firstService = services.get(0);
			firstService.setTypeServiceName(TypeServiceEnum.VIP); // Cập nhật sang "Bathing"
			int updated = repository.update(firstService);
			System.out.println("\n🟡 [UPDATE] Cập nhật loại dịch vụ ID " + firstService.getTypeServiceID() + ": "
					+ (updated > 0 ? "THÀNH CÔNG" : "THẤT BẠI"));
		} else {
			System.out.println("\n⚠️ Không có dịch vụ nào để cập nhật.");
		}

		// 4️⃣ Tìm kiếm loại dịch vụ theo ID
		if (!services.isEmpty()) {
			int idToFind = services.get(0).getTypeServiceID();
			TypeService foundService = repository.selectById(idToFind);
			System.out.println("\n🔍 [SELECT BY ID] Tìm loại dịch vụ ID " + idToFind + ": "
					+ (foundService != null ? foundService.getTypeServiceName().getDescription() : "KHÔNG TÌM THẤY"));
		} else {
			System.out.println("\n⚠️ Không có dịch vụ nào để tìm kiếm.");
		}

		// 5️⃣ Xóa loại dịch vụ
		if (!services.isEmpty()) {
			TypeService lastService = services.get(services.size() - 1);
			int deleted = repository.delete(lastService);
			System.out.println("\n🔴 [DELETE] Xóa loại dịch vụ ID " + lastService.getTypeServiceID() + ": "
					+ (deleted > 0 ? "THÀNH CÔNG" : "THẤT BẠI"));
		} else {
			System.out.println("\n⚠️ Không có dịch vụ nào để xóa.");
		}

		System.out.println("\n========== TEST KẾT THÚC ==========");
	}
}
