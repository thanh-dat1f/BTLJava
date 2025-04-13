DROP DATABASE IF EXISTS bestpets;
CREATE DATABASE IF NOT EXISTS bestpets;
USE bestpets;

-- Vai trò trong hệ thống
CREATE TABLE role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Tên vai trò: MANAGER, STAFF,...'
);

-- Tài khoản đăng nhập
CREATE TABLE `account` (
    account_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    role_id INT,
    `active` BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE SET NULL
);

-- 				Tạo bảng Con người
CREATE TABLE `person` (
	`person_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	full_name VARCHAR(100) NOT NULL,
	gender ENUM('MALE', 'FEMALE', 'OTHER') DEFAULT 'OTHER',
    `phone` VARCHAR(10) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`address` TEXT NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`email` TEXT NOT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`person_id`) USING BTREE,
	UNIQUE INDEX `Person_Phone` (`phone`) USING BTREE,
	CONSTRAINT `CkPerson_phone` CHECK ((length(`phone`) = 10))
);

-- Nhân viên
CREATE TABLE staff (
    staff_id INT UNSIGNED NOT NULL,
    dob DATE,
    salary DECIMAL(12, 2) DEFAULT 0.0,
    hire_date DATE,
    account_id INT UNIQUE,
    role_id INT,
    PRIMARY KEY (staff_id),
    FOREIGN KEY (staff_id) REFERENCES `person`(person_id) ON DELETE CASCADE ON UPDATE CASCADE ,
    FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE SET NULL,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE SET NULL
);

-- Khách hàng
CREATE TABLE customer (
    customer_id INT UNSIGNED NOT NULL,
    `point` INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (customer_id),
    FOREIGN KEY (customer_id) REFERENCES person(person_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- chuẩn hóa dữ liệu giống loài
CREATE TABLE pet_type (
    type_id INT PRIMARY KEY AUTO_INCREMENT,
    species VARCHAR(50) NOT NULL,   -- Ví dụ: "Chó", "Mèo"
    breed VARCHAR(100) NOT NULL     -- Ví dụ: "Poodle", "Alaska"
);

-- Thú cưng
CREATE TABLE pet (
    pet_id INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    type_id INT,
    pet_gender ENUM('MALE', 'FEMALE', 'UNKNOWN') DEFAULT 'UNKNOWN',
    dob DATE,
    weight DECIMAL(5,2),
    note TEXT,
    customer_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (type_id) REFERENCES pet_type(type_id),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);

-- Chương trình khuyến mãi
CREATE TABLE promotion (
    promotion_id INT PRIMARY KEY AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `description` TEXT,
    discount_percent INT CHECK (discount_percent >= 0 AND discount_percent <= 100),
    start_date DATE,
    end_date DATE,
    CHECK (start_date <= end_date),
    active BOOLEAN DEFAULT TRUE
);

-- Đơn hàng
CREATE TABLE `order` (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT UNSIGNED NOT NULL,
    staff_id INT UNSIGNED,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    voucher_code VARCHAR(50),
    total_amount DECIMAL(12,2) DEFAULT 0.0,
    `status` ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id),
    FOREIGN KEY (voucher_code) REFERENCES promotion(code)
);

-- Dịch vụ chăm sóc thú cưng
CREATE TABLE service (
    service_id INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    price DECIMAL(10, 2) NOT NULL,
    duration_minutes INT,
    `active` BOOLEAN DEFAULT TRUE
);

-- Đặt lịch chăm sóc
CREATE TABLE booking (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT UNSIGNED NOT NULL,
    pet_id INT NOT NULL,
    staff_id INT UNSIGNED,
    booking_time DATETIME NOT NULL,
    `status` ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
    note TEXT,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (pet_id) REFERENCES pet(pet_id),
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
);

-- 1 booking có thể chứa nhiều dịch vụ
CREATE TABLE booking_detail (
    booking_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    service_id INT NOT NULL,
    quantity INT DEFAULT 1,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES service(service_id)
);

-- Hóa đơn
CREATE TABLE invoice (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(12,2),
    payment_method ENUM('CASH', 'CARD', 'MOMO', 'BANKING') DEFAULT 'CASH',
    `status` ENUM('COMPLETED', 'PENDING', 'CANCELLED', 'FAILED') DEFAULT 'COMPLETED',
	staff_id INT UNSIGNED,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id),
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
);

-- Chi tiết đơn hàng
CREATE TABLE order_detail (
    order_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    service_id INT NOT NULL,
    quantity INT DEFAULT 1,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES service(service_id)
);

--  Lịch làm việc của nhân viên
CREATE TABLE work_schedule (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    staff_id INT UNSIGNED NOT NULL,
    work_date DATE NOT NULL,
    shift ENUM('MORNING', 'AFTERNOON', 'EVENING'),
    note TEXT,
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
);

-- Danh sách quyền trong hệ thống
CREATE TABLE permission (
    permission_code VARCHAR(100) PRIMARY KEY, -- VD: 'CREATE_BOOKING', 'MANAGE_STAFF'
    `description` TEXT NOT NULL
);

-- Phân quyền chi tiết cho tài khoản
CREATE TABLE account_permission (
    permission_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id INT NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    FOREIGN KEY (permission_code) REFERENCES permission(permission_code) ON DELETE CASCADE
);

CREATE VIEW dashboard_summary AS
SELECT
    (SELECT COUNT(*) FROM customer) AS total_customers,
    (SELECT COUNT(*) FROM booking) AS total_bookings,
    (SELECT COUNT(*) FROM invoice) AS total_invoices,
    (SELECT SUM(total) FROM invoice WHERE status = 'COMPLETED') AS total_revenue;


-- Tự động cập nhật total_amount cho order
DELIMITER //
CREATE TRIGGER trg_update_order_total
AFTER INSERT ON order_detail
FOR EACH ROW
BEGIN
    UPDATE `order`
    SET total_amount = (
        SELECT SUM(price * quantity) FROM order_detail WHERE order_id = NEW.order_id
    )
    WHERE order_id = NEW.order_id;
END;
// 
DELIMITER ;

-- Tạo đơn hàng mới và chi tiết đơn hàng
DELIMITER //
CREATE PROCEDURE create_order_with_details (
    IN p_customer_id INT,
    IN p_staff_id INT,
    IN p_service_id INT,
    IN p_quantity INT
)
BEGIN
    DECLARE new_order_id INT;
    
    -- 1. Tạo đơn hàng mới
    INSERT INTO `order` (customer_id, staff_id, order_date, status)
    VALUES (p_customer_id, p_staff_id, NOW(), 'PENDING');
    
    SET new_order_id = LAST_INSERT_ID();

    -- 2. Thêm chi tiết dịch vụ
    INSERT INTO order_detail (order_id, service_id, quantity, price)
    SELECT new_order_id, service_id, p_quantity, price FROM service WHERE service_id = p_service_id;

    -- 3. Trả về ID đơn hàng (tùy chọn)
    SELECT new_order_id AS created_order_id;
END;
//
DELIMITER ;

-- cập nhật trạng thái đơn hàng khi hóa đơn thanh toán xong
DELIMITER //
CREATE TRIGGER trg_invoice_paid_update_order
AFTER UPDATE ON invoice
FOR EACH ROW
BEGIN
    IF NEW.status = 'COMPLETED' THEN
        UPDATE `order`
        SET status = 'COMPLETED'
        WHERE order_id = NEW.order_id;
    END IF;
END;
//
DELIMITER ;

-- cập nhật total_amount nếu sửa order_detail
DELIMITER //
CREATE TRIGGER trg_update_order_total_after_update
AFTER UPDATE ON order_detail
FOR EACH ROW
BEGIN
    UPDATE `order`
    SET total_amount = (
        SELECT SUM(price * quantity) FROM order_detail WHERE order_id = NEW.order_id
    )
    WHERE order_id = NEW.order_id;
END;
//
DELIMITER ;

-- gán quyền
DROP PROCEDURE IF EXISTS assign_permission_by_role;
DELIMITER $$

CREATE PROCEDURE assign_permission_by_role(IN acc_id INT)
BEGIN
    DECLARE role_name VARCHAR(50);

    -- Lấy role_name từ account → staff → role
    SELECT r.role_name INTO role_name
    FROM staff s
    JOIN role r ON s.role_id = r.role_id
    WHERE s.account_id = acc_id;

    -- Xoá quyền cũ nếu có
    DELETE FROM account_permission WHERE account_id = acc_id;

    -- Gán quyền mới dựa trên role_name
    IF role_name = 'ADMIN' THEN
        INSERT INTO account_permission(account_id, permission_code)
        SELECT acc_id, permission_code FROM permission;

    ELSEIF role_name = 'STAFF_CARE' THEN
        INSERT INTO account_permission(account_id, permission_code)
        SELECT acc_id, permission_code FROM permission
        WHERE permission_code IN ('MANAGE_PET', 'VIEW_CUSTOMER', 'UPDATE_PROFILE');

    ELSEIF role_name = 'STAFF_CASHIER' THEN
        INSERT INTO account_permission(account_id, permission_code)
        SELECT acc_id, permission_code FROM permission
        WHERE permission_code IN ('MANAGE_INVOICE', 'VIEW_CUSTOMER', 'UPDATE_PROFILE');

    ELSEIF role_name = 'STAFF_RECEPTION' THEN
        INSERT INTO account_permission(account_id, permission_code)
        SELECT acc_id, permission_code FROM permission
        WHERE permission_code IN ('BOOK_SERVICE', 'VIEW_CUSTOMER', 'UPDATE_PROFILE');
    END IF;
END$$

DELIMITER ;

-- Trigger AFTER INSERT trên bảng staff để tự động gán quyền
DROP TRIGGER IF EXISTS trg_assign_permission_after_staff_insert;
DELIMITER $$

CREATE TRIGGER trg_assign_permission_after_staff_insert
AFTER INSERT ON staff
FOR EACH ROW
BEGIN
    CALL assign_permission_by_role(NEW.account_id);
END$$

DELIMITER ;

-- Thủ tục GÁN quyền cho tài khoản
DROP PROCEDURE IF EXISTS grant_permission;
DELIMITER $$

CREATE PROCEDURE grant_permission(
    IN p_account_id INT,
    IN p_permission_code VARCHAR(100)
)
BEGIN
    -- Chỉ thêm nếu chưa tồn tại
    IF NOT EXISTS (
        SELECT 1 FROM account_permission
        WHERE account_id = p_account_id AND permission_code = p_permission_code
    ) THEN
        INSERT INTO account_permission(account_id, permission_code)
        VALUES (p_account_id, p_permission_code);
    END IF;
END$$

DELIMITER ;

-- Thủ tục XÓA quyền của tài khoản
DROP PROCEDURE IF EXISTS revoke_permission;
DELIMITER $$

CREATE PROCEDURE revoke_permission(
    IN p_account_id INT,
    IN p_permission_code VARCHAR(100)
)
BEGIN
    DELETE FROM account_permission
    WHERE account_id = p_account_id AND permission_code = p_permission_code;
END$$

DELIMITER ;

-- Trigger cập nhật điểm point của khách hàng khi thanh toán hóa đơn
DROP TRIGGER IF EXISTS trg_update_point_after_invoice;
DELIMITER $$

CREATE TRIGGER trg_update_point_after_invoice
AFTER UPDATE ON invoice
FOR EACH ROW
BEGIN
    DECLARE v_customer_id INT;

    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        -- Lấy customer_id từ bảng order
        SELECT customer_id INTO v_customer_id
        FROM `order`
        WHERE order_id = NEW.order_id;

        -- Cập nhật điểm thưởng
        UPDATE customer
        SET point = point + FLOOR(NEW.total / 1000)
        W HERE customer_id = v_customer_id;
    END IF;
END$$

DELIMITER ;








