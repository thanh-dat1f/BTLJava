package tests;

import java.util.List;

import model.PetType;
import repository.PetTypeRepository;

public class testTypePetRepository {
	public static void main(String[] args) {
		PetTypeRepository petTypeRepository = PetTypeRepository.getInstance();

		// --- Test Insert ---
		System.out.println("--- Test Insert ---");
		PetType newType = new PetType(0, "Thú cưng mới");
		int insertResult = petTypeRepository.insert(newType);
		System.out.println("Thêm thành công? " + (insertResult > 0) + ", ID: " + newType.getTypePetID());

		// --- Test Select All ---
		System.out.println("\n--- Test Select All ---");
		List<PetType> typePetList = petTypeRepository.selectAll();
		for (PetType tp : typePetList) {
			System.out.println(tp);
		}

		// --- Test Select By ID ---
		System.out.println("\n--- Test Select By ID ---");
		PetType foundType = petTypeRepository.selectById(newType.getTypePetID());
		System.out.println(foundType != null ? foundType : "Không tìm thấy loại thú cưng");

		// --- Test Update ---
		System.out.println("\n--- Test Update ---");
		if (foundType != null) {
			foundType.setTypeName("Tên thú cưng mới cập nhật");
			int updateResult = petTypeRepository.update(foundType);
			System.out.println("Cập nhật thành công? " + (updateResult > 0));

			// Kiểm tra lại thông tin
			PetType updatedType = petTypeRepository.selectById(foundType.getTypePetID());
			System.out.println(updatedType);
		}

		// --- Test Delete ---
		System.out.println("\n--- Test Delete ---");
		int deleteResult = petTypeRepository.delete(newType);
		System.out.println("Xóa thành công? " + (deleteResult > 0));

		// Kiểm tra sau khi xóa
		PetType checkDeleted = petTypeRepository.selectById(newType.getTypePetID());
		System.out.println(checkDeleted == null ? "Đã xóa thành công" : "Xóa thất bại");
	}
}
