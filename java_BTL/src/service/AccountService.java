package service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import exception.AccountException;
import exception.BusinessException;
import model.Account;
import model.Role;
import repository.AccountRepository;
import repository.RoleRepository;

public class AccountService {
    
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    // Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ và số

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    public AccountService() {
        this.accountRepository = AccountRepository.getInstance();
        this.roleRepository = RoleRepository.getInstance();
    }

    /**
     * Đăng ký tài khoản mới
     */
    public boolean register(String username, String password, Role role) {
        // Kiểm tra dữ liệu
        validateAccountData(username, password);

        // Kiểm tra username đã tồn tại chưa
        if (accountRepository.isAccountExist(username)) {
            throw new AccountException("Tên đăng nhập đã tồn tại!");
        }

        // Tạo tài khoản mới với mật khẩu đã mã hóa
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        Account newAccount = new Account(0, username, hashedPassword, role);
        
        return accountRepository.insert(newAccount) > 0;
    }

    /**
     * Đăng nhập
     */
    public Optional<Account> login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BusinessException("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
        }

        // Lấy tài khoản theo username
        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return Optional.empty();
        }

        // Kiểm tra mật khẩu
        boolean passwordMatch;
        try {
            passwordMatch = BCrypt.checkpw(password, account.getPassword());
        } catch (Exception e) {
            // Xử lý trường hợp mật khẩu không được mã hóa đúng định dạng
            passwordMatch = password.equals(account.getPassword());
        }

        if (!passwordMatch) {
            return Optional.empty();
        }

        return Optional.of(account);
    }


    /**
     * Cập nhật thông tin tài khoản
     */
    public boolean updateAccount(int accountID, String newUsername, String newPassword, Role role) {
        Account existingAccount = accountRepository.selectById(accountID);
        if (existingAccount == null) {
            throw new AccountException("Tài khoản không tồn tại!");
        }

        // Kiểm tra username mới có trùng với tài khoản khác không
        if (newUsername != null && !newUsername.isEmpty() && 
            !existingAccount.getUserName().equals(newUsername) && 
            accountRepository.isAccountExist(newUsername)) {
            throw new AccountException("Tên đăng nhập mới đã được sử dụng!");
        }

        // Cập nhật thông tin
        if (newUsername != null && !newUsername.isEmpty()) {
            existingAccount.setUserName(newUsername);
        }
        
        if (newPassword != null && !newPassword.isEmpty()) {
            // Mã hóa mật khẩu mới
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            existingAccount.setPassword(hashedPassword);
        }

        if (role != null) {
            existingAccount.setRole(role);
        }

        return accountRepository.update(existingAccount) > 0;
    }

    /**
     * Cập nhật mật khẩu
     */
    public boolean updatePassword(int accountID, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new AccountException("Mật khẩu mới không được để trống");
        }

        Account account = accountRepository.selectById(accountID);
        if (account == null) {
            throw new AccountException("Tài khoản không tồn tại!");
        }

        // Kiểm tra mật khẩu mới có đúng định dạng không
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new AccountException("Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm chữ và số");
        }

        // Mã hóa mật khẩu mới
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        try {
            return accountRepository.updatePassword(accountID, hashedPassword);
        } catch (Exception e) {
            throw new AccountException("Lỗi khi cập nhật mật khẩu: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách tất cả tài khoản
     */
    public List<Account> getAllAccounts() {
        return accountRepository.selectAll();
    }

    /**
     * Kiểm tra dữ liệu tài khoản
     */
    private void validateAccountData(String username, String password) {
    	if (username == null || username.trim().isEmpty()) {
            throw new AccountException("Tên đăng nhập không được để trống!");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new AccountException("Mật khẩu không được để trống!");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new AccountException("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ và số!");
        }
    }
}