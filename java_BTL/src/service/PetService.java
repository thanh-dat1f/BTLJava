package service;

import model.Pet;
import repository.PetRepository;
import java.util.List;

public class PetService {

    private PetRepository petRepository;

    public PetService() {
        petRepository = PetRepository.getInstance();
    }

    // Thêm thú cưng
    public int addPet(Pet pet) {
        // Kiểm tra dữ liệu trước khi thêm
        if (pet == null || pet.getName().isEmpty() || pet.getWeight() <= 0) {
            System.out.println("Dữ liệu không hợp lệ.");
            return 0;
        }
        // Thực hiện thêm thú cưng vào DB thông qua PetRepository
        return petRepository.insert(pet);
    }

    // Cập nhật thông tin thú cưng
    public int updatePet(Pet pet) {
        // Kiểm tra dữ liệu trước khi cập nhật
        if (pet == null || pet.getPetId()<= 0 || pet.getName().isEmpty()) {
            System.out.println("Dữ liệu không hợp lệ.");
            return 0;
        }
        // Thực hiện cập nhật thú cưng vào DB thông qua PetRepository
        return petRepository.update(pet);
    }

    // Xóa thú cưng
    public int deletePet(int petID) {
        if (petID <= 0) {
            System.out.println("Dữ liệu không hợp lệ.");
            return 0;
        }
        // Tạo đối tượng Pet tạm để gọi phương thức delete
        Pet pet = new Pet();
        pet.setPetId(petID);
        return petRepository.delete(pet);
    }

    // Lấy tất cả thú cưng
    public List<Pet> getAllPets() {
        return petRepository.selectAll();
    }

    // Lấy thú cưng theo ID
    public Pet getPetById(int petID) {
        Pet pet = new Pet();
        pet.setPetId(petID);
        return petRepository.selectById(pet);
    }

    // Lấy thú cưng theo điều kiện
    public List<Pet> getPetsByCondition(String condition, Object... params) {
        return petRepository.selectByCondition(condition, params);
    }
}
