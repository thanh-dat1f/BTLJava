package tests;

import java.util.List;

import enums.PaymentMethodEnum;
import model.PaymentStatus;
import repository.PaymentStatusRepository;

public class testPaymentStatusRepository {
	public static void main(String[] args) {
		PaymentStatusRepository repository = PaymentStatusRepository.getInstance();

		// **INSERT TEST**
		System.out.println("▶ Thêm PaymentStatus...");
		PaymentStatus newStatus = new PaymentStatus(0, PaymentMethodEnum.PENDING);
		int insertResult = repository.insert(newStatus);
		System.out.println("Insert Result: " + (insertResult > 0 ? "Thành công" : "Thất bại"));

		// **SELECT ALL TEST**
		System.out.println("\n▶ Lấy danh sách PaymentStatus...");
		List<PaymentStatus> statuses = repository.selectAll();
		for (PaymentStatus status : statuses) {
			System.out.println(status);
		}

		// **SELECT BY ID TEST**
		if (!statuses.isEmpty()) {
			int testId = statuses.get(0).getPaymentStatusID();
			System.out.println("\n▶ Tìm PaymentStatus theo ID: " + testId);
			PaymentStatus foundStatus = repository.selectById(testId);
			System.out.println(foundStatus != null ? foundStatus : "Không tìm thấy");
		}

		// **UPDATE TEST**
		if (newStatus.getPaymentStatusID() != 0) {
			System.out.println("\n▶ Cập nhật PaymentStatus...");
			newStatus.setStatus(PaymentMethodEnum.PENDING);
			int updateResult = repository.update(newStatus);
			System.out.println("Update Result: " + (updateResult > 0 ? "Thành công" : "Thất bại"));
		}

		// **DELETE TEST**
		System.out.println("\n▶ Xóa PaymentStatus...");
		int deleteResult = repository.delete(newStatus);
		System.out.println("Delete Result: " + (deleteResult > 0 ? "Thành công" : "Thất bại"));
	}
}
