package service;

import enums.Shift;
import model.Staff;
import model.WorkSchedule;
import repository.WorkScheduleRepository;
import utils.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing staff work schedules
 */
public class ScheduleService {
    private final WorkScheduleRepository workScheduleRepository;
    
    /**
     * Constructor - initializes repository
     */
    public ScheduleService() {
        this.workScheduleRepository = WorkScheduleRepository.getInstance();
    }

    /**
     * Get schedules for a specific staff member on a specific date
     * @param staffId Staff ID
     * @param date Date to get schedules for
     * @return List of work schedules
     */
    public List<WorkSchedule> getSchedulesByStaffAndDate(int staffId, LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        String condition = "staff_id = ? AND work_date = ?";
        return workScheduleRepository.selectByCondition(condition, staffId, java.sql.Date.valueOf(date));
    }

    /**
     * Get schedules for a specific staff member within a date range
     * @param staffId Staff ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of work schedules
     */
    public List<WorkSchedule> getSchedulesByStaffAndDateRange(int staffId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return new ArrayList<>();
        }
        String condition = "staff_id = ? AND work_date BETWEEN ? AND ?";
        return workScheduleRepository.selectByCondition(condition, staffId, 
                java.sql.Date.valueOf(startDate), 
                java.sql.Date.valueOf(endDate));
    }
    
    /**
     * Get schedules by staff ID, date and shift
     * @param staffId Staff ID
     * @param date Date to get schedules for
     * @param shift Shift to filter by
     * @return List of work schedules
     */
    public List<WorkSchedule> getSchedulesByStaffDateAndShift(int staffId, LocalDate date, Shift shift) {
        if (date == null || shift == null) {
            return new ArrayList<>();
        }
        String condition = "staff_id = ? AND work_date = ? AND shift = ?";
        return workScheduleRepository.selectByCondition(condition, staffId, 
                java.sql.Date.valueOf(date), shift.name());
    }
    
    /**
     * Get schedules for a month
     * @param staffId Staff ID
     * @param month Month (1-12)
     * @param year Year
     * @return List of work schedules
     */
    public List<WorkSchedule> getSchedulesForMonth(int staffId, int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        return getSchedulesByStaffAndDateRange(staffId, startOfMonth, endOfMonth);
    }

    /**
     * Register a new shift for a staff member
     * @param staffId Staff ID
     * @param date Work date
     * @param shift Shift type
     * @param location Work location
     * @param note Optional note
     * @return true if successfully registered, false otherwise
     */
    public boolean registerShift(int staffId, LocalDate date, Shift shift, String location, String note) {
        // Check if the staff already has a shift for this date and time
        List<WorkSchedule> existingSchedules = getSchedulesByStaffDateAndShift(staffId, date, shift);
        if (!existingSchedules.isEmpty()) {
            return false; // Already has a shift at this time
        }
        
        // Create a new work schedule
        WorkSchedule schedule = new WorkSchedule();
        Staff staff = new Staff();
        staff.setId(staffId);
        schedule.setStaff(staff);
        schedule.setWorkDate(date);
        schedule.setShift(shift);
        schedule.setLocation(location);
        schedule.setNote(note);
        
        // Set default start and end times based on shift
        setDefaultTimes(schedule);
        
        // Save to database
        return workScheduleRepository.insert(schedule) > 0;
    }
    
    /**
     * Request leave for a specific date
     * @param staffId Staff ID
     * @param date Leave date
     * @param reason Reason for leave
     * @return true if request was successful, false otherwise
     */
    public boolean requestLeave(int staffId, LocalDate date, String reason) {
        // Implementation would depend on how leaves are handled in your system
        // This is a placeholder implementation
        WorkSchedule schedule = new WorkSchedule();
        Staff staff = new Staff();
        staff.setId(staffId);
        schedule.setStaff(staff);
        schedule.setWorkDate(date);
        schedule.setNote("Leave request: " + reason);
        
        // You might want to add a status field to WorkSchedule to track leave requests
        // For now, we'll just save it as a special note
        return workScheduleRepository.insert(schedule) > 0;
    }
    
    /**
     * Get monthly statistics for a staff member
     * @param staffId Staff ID
     * @param month Month (1-12)
     * @param year Year
     * @return Map containing statistics
     */
    public Map<String, Object> getMonthlyStatistics(int staffId, int month, int year) {
        List<WorkSchedule> schedules = getSchedulesForMonth(staffId, month, year);
        Map<String, Object> stats = new HashMap<>();
        
        // Calculate statistics
        int totalShifts = schedules.size();
        int morningShifts = 0, afternoonShifts = 0, eveningShifts = 0;
        int totalHours = 0;
        int overtimeHours = 0;
        
        for (WorkSchedule schedule : schedules) {
            Shift shift = schedule.getShift();
            if (shift == Shift.MORNING) {
                morningShifts++;
                totalHours += 4; // Assuming morning shift is 4 hours
            } else if (shift == Shift.AFTERNOON) {
                afternoonShifts++;
                totalHours += 4; // Assuming afternoon shift is 4 hours
            } else if (shift == Shift.EVENING) {
                eveningShifts++;
                totalHours += 4; // Assuming evening shift is 4 hours
            }
        }
        
        // Assuming standard working hours per month is 160
        if (totalHours > 160) {
            overtimeHours = totalHours - 160;
        }
        
        // Populate statistics map
        stats.put("totalShifts", totalShifts);
        stats.put("morningShifts", morningShifts);
        stats.put("afternoonShifts", afternoonShifts);
        stats.put("eveningShifts", eveningShifts);
        stats.put("totalHours", totalHours);
        stats.put("overtimeHours", overtimeHours);
        stats.put("standardWorkdays", totalShifts / 2); // Assuming 2 shifts per day is standard
        stats.put("leaveCount", 0); // Would need leave tracking to calculate this
        
        return stats;
    }
    
    /**
     * Set default start and end times for a schedule based on the shift
     * @param schedule The schedule to set times for
     */
    private void setDefaultTimes(WorkSchedule schedule) {
        switch (schedule.getShift()) {
            case MORNING:
                schedule.setStartTime(LocalTime.of(8, 0));
                schedule.setEndTime(LocalTime.of(12, 0));
                break;
            case AFTERNOON:
                schedule.setStartTime(LocalTime.of(13, 0));
                schedule.setEndTime(LocalTime.of(17, 0));
                break;
            case EVENING:
                schedule.setStartTime(LocalTime.of(18, 0));
                schedule.setEndTime(LocalTime.of(22, 0));
                break;
        }
    }
}