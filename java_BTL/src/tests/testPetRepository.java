package tests;

import java.util.List;

import model.Customer;
import model.Pet;
import model.PetType;
import repository.CustomerRepository;
import repository.PetRepository;

public class testPetRepository {
	public static void main(String[] args) {
		PetRepository petRepository = PetRepository.getInstance();
		CustomerRepository customerRepository = CustomerRepository.getInstance();
		
		// Test thêm mới Pet
		System.out.println("--- Test Insert ---");
		Customer customer = customerRepository.selectById(1);
		PetType petType = new PetType(1, "Dog");
		Pet pet = new Pet(101, "Lucky", 3, customer, petType);
		petRepository.insert(pet);

		// Test cập nhật Pet
		System.out.println("--- Test Update ---");
		pet.setPetName("Lucky Updated");
		petRepository.update(pet);

		// Test lấy tất cả Pet
		System.out.println("--- Test Select All ---");
		List<Pet> petList = petRepository.selectAll();
		for (Pet p : petList) {
			System.out.println(p);
		}

		// Test lấy Pet theo ID
		System.out.println("--- Test Select By ID ---");
		Pet foundPet = petRepository.selectById(new Pet(1010, null, 0, null, null));
		System.out.println(foundPet);

		// Test xóa Pet
		System.out.println("--- Test Delete ---");
		petRepository.delete(pet);
	}
}
