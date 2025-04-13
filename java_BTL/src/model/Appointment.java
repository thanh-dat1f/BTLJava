package model;


public class Appointment {
    private String appointmentId;  // ID của lịch hẹn/đơn hàng
    private String customerName;   // Tên khách hàng
    private String petName;        // Thông tin thú cưng
    private String service;        // Dịch vụ đã đăng ký
    private String time;           // Thời gian hẹn (đã được định dạng)
    private String status;         // Trạng thái lịch hẹn

 
    public Appointment() {
    }

  
    public Appointment(String appointmentId, String customerName, String petName, String service, String time, String status) {
        this.appointmentId = appointmentId;
        this.customerName = customerName;
        this.petName = petName;
        this.service = service;
        this.time = time;
        this.status = status;
    }

    // Getters và Setters
    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId='" + appointmentId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", petName='" + petName + '\'' +
                ", service='" + service + '\'' +
                ", time='" + time + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}