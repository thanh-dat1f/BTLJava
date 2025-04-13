package model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a work schedule for staff members based on the work_schedule table.
 * This class contains information about staff work schedules including date and shift.
 */
public class Schedule {
    private int scheduleId;
    private int staffId;
    private LocalDate workDate;
    private String shift; // MORNING, AFTERNOON, EVENING - from ENUM in database
    private String note;

    /**
     * Default constructor
     */
    public Schedule() {
    }

    /**
     * Parameterized constructor
     */
    public Schedule(int scheduleId, int staffId, LocalDate workDate, String shift, String note) {
        this.scheduleId = scheduleId;
        this.staffId = staffId;
        this.workDate = workDate;
        this.shift = shift;
        this.note = note;
    }

    // Getters and Setters
    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Checks if this schedule is for morning shift
     * @return true if it's a morning shift
     */
    public boolean isMorningShift() {
        return "MORNING".equals(this.shift);
    }

    /**
     * Checks if this schedule is for afternoon shift
     * @return true if it's an afternoon shift
     */
    public boolean isAfternoonShift() {
        return "AFTERNOON".equals(this.shift);
    }

    /**
     * Checks if this schedule is for evening shift
     * @return true if it's an evening shift
     */
    public boolean isEveningShift() {
        return "EVENING".equals(this.shift);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return scheduleId == schedule.scheduleId &&
                staffId == schedule.staffId &&
                Objects.equals(workDate, schedule.workDate) &&
                Objects.equals(shift, schedule.shift);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId, staffId, workDate, shift);
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "scheduleId=" + scheduleId +
                ", staffId=" + staffId +
                ", workDate=" + workDate +
                ", shift='" + shift + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}