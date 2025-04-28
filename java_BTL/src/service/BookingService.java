package service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import enums.StatusEnum;
import model.Booking;
import model.BookingDetail;
import model.Customer;
import model.Pet;
import model.Service;
import model.Staff;
import repository.BookingDetailRepository;
import repository.BookingRepository;
import repository.CustomerRepository;
import repository.PetRepository;
import repository.ServiceRepository;
import repository.StaffRepository;

/**
 * Dịch vụ quản lý booking (đặt lịch)
 */
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final CustomerRepository customerRepository;
    private final PetRepository petRepository;
    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    
    /**
     * Khởi tạo BookingService với các repository cần thiết
     */
    public BookingService() {
        this.bookingRepository = BookingRepository.getInstance();
        this.bookingDetailRepository = new BookingDetailRepository();
        this.customerRepository = CustomerRepository.getInstance();
        this.petRepository = PetRepository.getInstance();
        this.staffRepository = StaffRepository.getInstance();
        this.serviceRepository = ServiceRepository.getInstance();
    }
    
    /**
     * Lấy tất cả booking
     * @return Danh sách booking
     */
    public List<Booking> getAllBookings() {
        try {
            return bookingRepository.selectAll();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy booking theo ID
     * @param bookingId ID của booking
     * @return Booking nếu tìm thấy, null nếu không tìm thấy
     */
    public Booking getBookingById(int bookingId) {
        try {
            return bookingRepository.selectById(bookingId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy booking theo ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy danh sách booking theo ID của nhân viên
     * @param staffId ID của nhân viên
     * @return Danh sách booking của nhân viên
     */
    public List<Booking> getBookingsByStaffId(int staffId) {
        try {
            String condition = "staff_id = ?";
            return bookingRepository.selectByCondition(condition, staffId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy booking theo staff ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy danh sách booking theo ID của khách hàng
     * @param customerId ID của khách hàng
     * @return Danh sách booking của khách hàng
     */
    public List<Booking> getBookingsByCustomerId(int customerId) {
        try {
            String condition = "customer_id = ?";
            return bookingRepository.selectByCondition(condition, customerId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy booking theo customer ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy danh sách booking theo ID của thú cưng
     * @param petId ID của thú cưng
     * @return Danh sách booking của thú cưng
     */
    public List<Booking> getBookingsByPetId(int petId) {
        try {
            String condition = "pet_id = ?";
            return bookingRepository.selectByCondition(condition, petId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy booking theo pet ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy danh sách booking theo ngày
     * @param date Ngày cần lấy booking
     * @return Danh sách booking trong ngày
     */
    public List<Booking> getBookingsByDate(LocalDate date) {
        try {
            String condition = "DATE(booking_time) = ?";
            return bookingRepository.selectByCondition(condition, date);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy booking theo ngày: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy danh sách booking trong khoảng thời gian
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách booking trong khoảng thời gian
     */
    public List<Booking> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            String condition = "DATE(booking_time) BETWEEN ? AND ?";
            return bookingRepository.selectByCondition(condition, startDate, endDate);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy booking theo khoảng thời gian: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy danh sách booking theo trạng thái
     * @param status Trạng thái booking
     * @return Danh sách booking có trạng thái tương ứng
     */
    public List<Booking> getBookingsByStatus(StatusEnum status) {
        try {
            String condition = "status = ?";
            return bookingRepository.selectByCondition(condition, status.name());
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy booking theo trạng thái: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Tạo booking mới
     * @param customerId ID của khách hàng
     * @param petId ID của thú cưng
     * @param staffId ID của nhân viên (có thể null)
     * @param bookingTime Thời gian đặt lịch
     * @param serviceIds Danh sách ID của các dịch vụ
     * @param note Ghi chú cho booking
     * @return Booking đã tạo, null nếu tạo không thành công
     */
    public Booking createBooking(int customerId, int petId, Integer staffId, LocalDateTime bookingTime, List<Integer> serviceIds, String note) {
        try {
            // Kiểm tra dữ liệu đầu vào
            if (customerId <= 0 || petId <= 0 || bookingTime == null || serviceIds == null || serviceIds.isEmpty()) {
                throw new IllegalArgumentException("Dữ liệu đầu vào không hợp lệ");
            }
            
            // Lấy thông tin khách hàng
            Customer customer = customerRepository.selectById(customerId);
            if (customer == null) {
                throw new IllegalArgumentException("Không tìm thấy khách hàng");
            }
            
            // Lấy thông tin thú cưng
            Pet pet = petRepository.selectById(petId);
            if (pet == null) {
                throw new IllegalArgumentException("Không tìm thấy thú cưng");
            }
            
            // Lấy thông tin nhân viên (nếu có)
            Staff staff = null;
            if (staffId != null && staffId > 0) {
                staff = staffRepository.selectById(staffId);
                if (staff == null) {
                    throw new IllegalArgumentException("Không tìm thấy nhân viên");
                }
                
                // Kiểm tra xem nhân viên đã có lịch trùng không
                if (isTimeSlotBooked(staffId, bookingTime)) {
                    throw new IllegalArgumentException("Nhân viên đã có lịch vào thời gian này");
                }
            }
            
            // Kiểm tra thời gian
            if (bookingTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Thời gian đặt lịch phải là thời gian trong tương lai");
            }
            
            // Tạo đối tượng Booking
            Booking booking = new Booking();
            booking.setCustomer(customer);
            booking.setPet(pet);
            booking.setStaff(staff);
            booking.setBookingTime(bookingTime);
            booking.setStatus(StatusEnum.PENDING);
            booking.setNote(note);
            
            // Lưu booking vào database
            int result = bookingRepository.insert(booking);
            if (result <= 0) {
                throw new RuntimeException("Không thể lưu booking");
            }
            
            // Thêm các dịch vụ vào booking
            addServicesToBooking(booking, serviceIds);
            
            return booking;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo booking: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Thêm các dịch vụ vào booking
     * @param booking Booking cần thêm dịch vụ
     * @param serviceIds Danh sách ID của các dịch vụ
     */
    private void addServicesToBooking(Booking booking, List<Integer> serviceIds) {
        for (int serviceId : serviceIds) {
            Service service = serviceRepository.selectById(serviceId);
            if (service != null) {
                BookingDetail detail = new BookingDetail();
                detail.setBooking(booking);
                detail.setService(service);
                detail.setQuantity(1);
                detail.setPrice(service.getPrice());
                
                bookingDetailRepository.insert(detail);
            }
        }
    }
    
    /**
     * Lấy danh sách chi tiết của booking
     * @param bookingId ID của booking
     * @return Danh sách BookingDetail
     */
    public List<BookingDetail> getBookingDetails(int bookingId) {
        try {
            String condition = "booking_id = ?";
            return bookingDetailRepository.selectByCondition(condition, bookingId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy chi tiết booking: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Cập nhật booking
     * @param booking Booking cần cập nhật
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateBooking(Booking booking) {
        try {
            // Kiểm tra booking tồn tại không
            Booking existingBooking = bookingRepository.selectById(booking.getBookingId());
            if (existingBooking == null) {
                throw new IllegalArgumentException("Booking không tồn tại");
            }
            
            // Kiểm tra dữ liệu đầu vào
            if (booking.getCustomer() == null || booking.getPet() == null || 
                booking.getBookingTime() == null || booking.getStatus() == null) {
                throw new IllegalArgumentException("Dữ liệu không hợp lệ");
            }
            
            // Kiểm tra xem thời gian đã được đặt chưa (nếu thay đổi thời gian)
            if (booking.getStaff() != null && 
                !booking.getBookingTime().equals(existingBooking.getBookingTime()) &&
                isTimeSlotBooked(booking.getStaff().getId(), booking.getBookingTime())) {
                throw new IllegalArgumentException("Nhân viên đã có lịch vào thời gian này");
            }
            
            // Cập nhật vào cơ sở dữ liệu
            return bookingRepository.update(booking) > 0;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cập nhật trạng thái booking
     * @param bookingId ID của booking
     * @param status Trạng thái mới
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateBookingStatus(int bookingId, String status) {
        try {
            Booking booking = bookingRepository.selectById(bookingId);
            if (booking == null) {
                throw new IllegalArgumentException("Booking không tồn tại");
            }
            
            // Kiểm tra trạng thái hợp lệ
            StatusEnum statusEnum = StatusEnum.valueOf(status);
            
            // Kiểm tra logic chuyển trạng thái
            validateStatusChange(booking.getStatus(), statusEnum);
            
            // Cập nhật trạng thái
            booking.setStatus(statusEnum);
            return bookingRepository.update(booking) > 0;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Trạng thái không hợp lệ: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật trạng thái booking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Xóa booking
     * @param bookingId ID của booking
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteBooking(int bookingId) {
        try {
            Booking booking = bookingRepository.selectById(bookingId);
            if (booking == null) {
                throw new IllegalArgumentException("Booking không tồn tại");
            }
            
            // Không cho phép xóa các booking đã hoàn thành
            if (StatusEnum.COMPLETED.equals(booking.getStatus())) {
                throw new IllegalArgumentException("Không thể xóa booking đã hoàn thành");
            }
            
            // Xóa các chi tiết booking trước
            List<BookingDetail> details = getBookingDetails(bookingId);
            for (BookingDetail detail : details) {
                bookingDetailRepository.delete(detail);
            }
            
            // Xóa booking
            return bookingRepository.delete(booking) > 0;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa booking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kiểm tra sự thay đổi trạng thái có hợp lệ không
     * @param currentStatus Trạng thái hiện tại
     * @param newStatus Trạng thái mới
     * @throws IllegalArgumentException Nếu sự thay đổi trạng thái không hợp lệ
     */
    private void validateStatusChange(StatusEnum currentStatus, StatusEnum newStatus) {
        // Nếu đã hủy thì không thể chuyển sang trạng thái khác
        if (StatusEnum.CANCELLED.equals(currentStatus) && !StatusEnum.CANCELLED.equals(newStatus)) {
            throw new IllegalArgumentException("Không thể thay đổi trạng thái của booking đã hủy");
        }
        
        // Nếu đã hoàn thành thì không thể chuyển sang trạng thái khác
        if (StatusEnum.COMPLETED.equals(currentStatus) && !StatusEnum.COMPLETED.equals(newStatus)) {
            throw new IllegalArgumentException("Không thể thay đổi trạng thái của booking đã hoàn thành");
        }
        
        // Kiểm tra logic chuyển trạng thái
        switch (currentStatus) {
            case PENDING:
                // Từ pending có thể chuyển sang confirmed, cancelled
                if (!StatusEnum.CONFIRMED.equals(newStatus) && 
                    !StatusEnum.CANCELLED.equals(newStatus) && 
                    !StatusEnum.PENDING.equals(newStatus)) {
                    throw new IllegalArgumentException("Booking đang chờ xác nhận chỉ có thể chuyển sang trạng thái đã xác nhận hoặc đã hủy");
                }
                break;
                
            case CONFIRMED:
                // Từ confirmed có thể chuyển sang completed, cancelled
                if (!StatusEnum.COMPLETED.equals(newStatus) && 
                    !StatusEnum.CANCELLED.equals(newStatus) && 
                    !StatusEnum.CONFIRMED.equals(newStatus)) {
                    throw new IllegalArgumentException("Booking đã xác nhận chỉ có thể chuyển sang trạng thái đã hoàn thành hoặc đã hủy");
                }
                break;
                
            case COMPLETED:
                // Từ completed không thể chuyển sang trạng thái khác
                if (!StatusEnum.COMPLETED.equals(newStatus)) {
                    throw new IllegalArgumentException("Booking đã hoàn thành không thể chuyển sang trạng thái khác");
                }
                break;
                
            case CANCELLED:
                // Từ cancelled không thể chuyển sang trạng thái khác
                if (!StatusEnum.CANCELLED.equals(newStatus)) {
                    throw new IllegalArgumentException("Booking đã hủy không thể chuyển sang trạng thái khác");
                }
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Kiểm tra xem thời gian đã được đặt chưa cho một nhân viên
     * @param staffId ID của nhân viên
     * @param bookingTime Thời gian cần kiểm tra
     * @return true nếu đã có booking vào thời điểm đó, false nếu chưa
     */
    private boolean isTimeSlotBooked(int staffId, LocalDateTime bookingTime) {
        try {
            // Lấy tất cả booking của nhân viên trong ngày
            List<Booking> bookings = getBookingsByStaffAndDate(staffId, bookingTime.toLocalDate());
            
            // Kiểm tra xem có booking nào trùng thời gian không
            for (Booking booking : bookings) {
                LocalDateTime startTime = booking.getBookingTime();
                
                // Giả sử mỗi booking kéo dài 1 giờ
                LocalDateTime endTime = startTime.plusHours(1);
                
                // Nếu thời gian đặt nằm trong khoảng thời gian của một booking đã tồn tại
                if ((bookingTime.isEqual(startTime) || bookingTime.isAfter(startTime)) && 
                    bookingTime.isBefore(endTime)) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra thời gian booking: " + e.getMessage());
            return false; // Mặc định là đã đặt (để tránh xung đột)
        }
    }
    
    /**
     * Lấy danh sách booking của một nhân viên trong ngày
     * @param staffId ID của nhân viên
     * @param date Ngày cần kiểm tra
     * @return Danh sách booking
     */
    public List<Booking> getBookingsByStaffAndDate(int staffId, LocalDate date) {
        try {
            String condition = "staff_id = ? AND DATE(booking_time) = ?";
            return bookingRepository.selectByCondition(condition, staffId, date);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy booking theo nhân viên và ngày: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Gán nhân viên cho booking
     * @param bookingId ID của booking
     * @param staffId ID của nhân viên
     * @return true nếu gán thành công, false nếu thất bại
     */
    public boolean assignStaffToBooking(int bookingId, int staffId) {
        try {
            Booking booking = bookingRepository.selectById(bookingId);
            if (booking == null) {
                throw new IllegalArgumentException("Booking không tồn tại");
            }
            
            Staff staff = staffRepository.selectById(staffId);
            if (staff == null) {
                throw new IllegalArgumentException("Nhân viên không tồn tại");
            }
            
            // Kiểm tra xem nhân viên có lịch trùng không
            if (isTimeSlotBooked(staffId, booking.getBookingTime())) {
                throw new IllegalArgumentException("Nhân viên đã có lịch vào thời gian này");
            }
            
            booking.setStaff(staff);
            return bookingRepository.update(booking) > 0;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi gán nhân viên cho booking: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy danh sách booking trong ngày hiện tại
     * @return Danh sách booking
     */
    public List<Booking> getTodayBookings() {
        return getBookingsByDate(LocalDate.now());
    }
    
    /**
     * Lấy danh sách booking trong tuần hiện tại
     * @return Danh sách booking
     */
    public List<Booking> getCurrentWeekBookings() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return getBookingsByDateRange(startOfWeek, endOfWeek);
    }
    
    /**
     * Lấy danh sách booking sắp tới cho khách hàng
     * @param customerId ID của khách hàng
     * @return Danh sách booking
     */
    public List<Booking> getUpcomingBookingsForCustomer(int customerId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String condition = "customer_id = ? AND booking_time > ? AND status != 'CANCELLED' AND status != 'COMPLETED'";
            return bookingRepository.selectByCondition(condition, customerId, now);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy booking sắp tới cho khách hàng: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy danh sách booking lịch sử cho khách hàng
     * @param customerId ID của khách hàng
     * @return Danh sách booking
     */
    public List<Booking> getHistoryBookingsForCustomer(int customerId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String condition = "customer_id = ? AND (booking_time < ? OR status = 'COMPLETED' OR status = 'CANCELLED')";
            return bookingRepository.selectByCondition(condition, customerId, now);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy booking lịch sử cho khách hàng: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy tổng thời gian của booking (dựa vào các dịch vụ đã chọn)
     * @param bookingId ID của booking
     * @return Tổng thời gian (phút)
     */
    public int getTotalDurationForBooking(int bookingId) {
        try {
            List<BookingDetail> details = getBookingDetails(bookingId);
            int totalDuration = 0;
            
            for (BookingDetail detail : details) {
                Service service = detail.getService();
                if (service != null) {
                    totalDuration += service.getDurationMinutes() * detail.getQuantity();
                }
            }
            
            return totalDuration;
        } catch (Exception e) {
            System.err.println("Lỗi khi tính tổng thời gian booking: " + e.getMessage());
            return 60; // Mặc định 1 giờ
        }
    }
    
    /**
     * Lấy tổng giá tiền của booking (dựa vào các dịch vụ đã chọn)
     * @param bookingId ID của booking
     * @return Tổng giá tiền
     */
    public double getTotalPriceForBooking(int bookingId) {
        try {
            List<BookingDetail> details = getBookingDetails(bookingId);
            double totalPrice = 0;
            
            for (BookingDetail detail : details) {
                totalPrice += detail.getPrice() * detail.getQuantity();
            }
            
            return totalPrice;
        } catch (Exception e) {
            System.err.println("Lỗi khi tính tổng giá tiền booking: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Tìm kiếm booking bằng từ khóa (tên khách hàng, tên thú cưng, ...)
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách booking tìm thấy
     */
    public List<Booking> searchBookings(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllBookings();
            }
            
            String searchTerm = "%" + keyword.trim() + "%";
            String condition = "c.full_name LIKE ? OR p.name LIKE ?";
            return bookingRepository.selectByCondition(condition, searchTerm, searchTerm);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm booking: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}