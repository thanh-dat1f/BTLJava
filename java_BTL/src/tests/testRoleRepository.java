package tests;

import java.util.List;

import model.Role;
import repository.RoleRepository;

public class testRoleRepository {
    public static void main(String[] args) {
        RoleRepository roleRepository = RoleRepository.getInstance();

        // 1. Thêm mới Role
        Role newRole = new Role(0, "Manager");
        int insertResult = roleRepository.insert(newRole);
        System.out.println("Insert Result: " + insertResult);
        System.out.println("Inserted Role ID: " + newRole.getRoleID());

        // 2. Lấy danh sách tất cả Role
        System.out.println("\nDanh sách tất cả Role:");
        List<Role> roleList = roleRepository.selectAll();
        for (Role role : roleList) {
            System.out.println(role);
        }

        // 3. Cập nhật Role
        if (!roleList.isEmpty()) {
            Role updateRole = roleList.get(0);
            updateRole.setRoleName("Updated Manager");
            int updateResult = roleRepository.update(updateRole);
            System.out.println("\nUpdate Result: " + updateResult);
        }

        // 4. Tìm Role theo ID
        if (!roleList.isEmpty()) {
            int roleId = roleList.get(0).getRoleID();
            Role foundRole = roleRepository.selectById(roleId);
            System.out.println("\nRole found by ID: " + foundRole);
        }

        // 5. Tìm Role theo điều kiện
        System.out.println("\nDanh sách Role có tên chứa 'Manager':");
        List<Role> filteredRoles = roleRepository.selectByCondition("roleName LIKE ?", "%Manager%");
        for (Role role : filteredRoles) {
            System.out.println(role);
        }

        // 6. Xóa Role
        if (!roleList.isEmpty()) {
            Role deleteRole = roleList.get(roleList.size() - 1);
            int deleteResult = roleRepository.delete(deleteRole);
            System.out.println("\nDelete Result: " + deleteResult);
        }
    }
}
