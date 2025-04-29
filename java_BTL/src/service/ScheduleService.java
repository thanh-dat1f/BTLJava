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


public class ScheduleService {
    private final WorkScheduleRepository workScheduleRepository;
    
    public ScheduleService() {
        this.workScheduleRepository = WorkScheduleRepository.getInstance();
    }


    public List<WorkSchedule> getSchedulesByStaffAndDate(int staffId, LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        String condition = "staff_id = ? AND work_date = ?";
        return workScheduleRepository.selectByCondition(condition, staffId, java.sql.Date.valueOf(date));
    }


    public List<WorkSchedule> getSchedulesByStaffAndDateRange(int staffId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return new ArrayList<>();
        }
        String condition = "staff_id = ? AND work_date BETWEEN ? AND ?";
        return workScheduleRepository.selectByCondition(condition, staffId, 
                java.sql.Date.valueOf(startDate), 
                java.sql.Date.valueOf(endDate));
    }
    

    public List<WorkSchedule> getSchedulesByStaffDateAndShift(int staffId, LocalDate date, Shift shift) {
        if (date == null || shift == null) {
            return new ArrayList<>();
        }
        String condition = "staff_id = ? AND work_date = ? AND shift = ?";
        return workScheduleRepository.selectByCondition(condition, staffId, 
                java.sql.Date.valueOf(date), shift.name());
    }
    

    public List<WorkSchedule> getSchedulesForMonth(int staffId, int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        return getSchedulesByStaffAndDateRange(staffId, startOfMonth, endOfMonth);
    }

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
    
   
    public boolean requestShiftChange(int staffId, LocalDate currentDate, Shift currentShift, 
                                     LocalDate desiredDate, Shift desiredShift, String reason) {
        // Validate inputs
        if (currentDate == null || currentShift == null || desiredDate == null || desiredShift == null) {
            return false;
        }

        // Check if the current shift exists
        List<WorkSchedule> currentSchedules = getSchedulesByStaffDateAndShift(staffId, currentDate, currentShift);
        if (currentSchedules.isEmpty()) {
            return false; // Current shift does not exist
        }

        // Check if the desired shift is already taken by the staff
        List<WorkSchedule> desiredSchedules = getSchedulesByStaffDateAndShift(staffId, desiredDate, desiredShift);
        if (!desiredSchedules.isEmpty()) {
            return false; // Desired shift is already registered
        }

        // Create a new work schedule entry to record the shift change request
        WorkSchedule schedule = new WorkSchedule();
        Staff staff = new Staff();
        staff.setId(staffId);
        schedule.setStaff(staff);
        schedule.setWorkDate(currentDate); // Store the current date for reference
        schedule.setShift(currentShift); // Store the current shift
        schedule.setNote("Shift change request: From " + currentShift.name() + " on " + 
                         currentDate + " to " + desiredShift.name() + " on " + 
                         desiredDate + ". Reason: " + reason);
        
        // Save the shift change request
        return workScheduleRepository.insert(schedule) > 0;
    }

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
                totalHours += 4; 
            } else if (shift == Shift.AFTERNOON) {
                afternoonShifts++;
                totalHours += 4; 
            } else if (shift == Shift.EVENING) {
                eveningShifts++;
                totalHours += 4; 
            }
        }

        if (totalHours > 160) {
            overtimeHours = totalHours - 160;
        }

        stats.put("totalShifts", totalShifts);
        stats.put("morningShifts", morningShifts);
        stats.put("afternoonShifts", afternoonShifts);
        stats.put("eveningShifts", eveningShifts);
        stats.put("totalHours", totalHours);
        stats.put("overtimeHours", overtimeHours);
        stats.put("standardWorkdays", totalShifts / 2);
        stats.put("leaveCount", 0); 
        
        return stats;
    }
    
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