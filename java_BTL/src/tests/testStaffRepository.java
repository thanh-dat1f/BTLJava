package tests;

import java.time.LocalDate;
import java.util.List;

import enums.GenderEnum;
import model.Account;
import model.Role;
import model.Staff;
import repository.StaffRepository;

public class testStaffRepository {
	public static void main(String[] args) {
		StaffRepository staffRepo = new StaffRepository();

		System.out.println("===== TEST INSERT STAFF =====");
		Staff newStaff = new Staff(0, "Nguyễn", "Vương Khang", GenderEnum.MALE, "0909123456", "123456789", "Long An",
				"khang@example.com", new Account(1, "khangnv", null, "khang@example.com", null),
				new Role(1, "Nhân viên chăm sóc"), LocalDate.now(), null, 8000000, "Sáng", "Nhân viên");

		int insertResult = staffRepo.insert(newStaff);
		if (insertResult > 0) {
			System.out.println("✅ Thêm nhân viên thành công!");
		} else {
			System.out.println("❌ Thêm thất bại!");
		}

		System.out.println("\n===== TEST SELECT BY ID =====");
		Staff foundStaff = staffRepo.selectById(newStaff.getId());
		if (foundStaff != null) {
			System.out.println("✅ Tìm thấy nhân viên: " + foundStaff.getFirstName() + " " + foundStaff.getLastName());
		} else {
			System.out.println("❌ Không tìm thấy nhân viên!");
		}

		System.out.println("\n===== TEST UPDATE STAFF =====");
		newStaff.setFirstName("Khang Nguyễn");
		newStaff.setSalary(9000000);
		int updateResult = staffRepo.update(newStaff);
		if (updateResult > 0) {
			System.out.println("✅ Cập nhật nhân viên thành công!");
		} else {
			System.out.println("❌ Cập nhật thất bại!");
		}

		System.out.println("\n===== TEST SELECT ALL STAFFS =====");
		List<Staff> staffList = staffRepo.selectAll();
		System.out.println("Danh sách nhân viên:");
		for (Staff s : staffList) {
			System.out.println(s.getId() + " - " + s.getFirstName() + " " + s.getLastName() + " - " + s.getSalary());
		}

		System.out.println("\n===== TEST DELETE STAFF =====");
		int deleteResult = staffRepo.delete(newStaff);
		if (deleteResult > 0) {
			System.out.println("✅ Xóa nhân viên thành công!");
		} else {
			System.out.println("❌ Xóa thất bại!");
		}
	}
}
