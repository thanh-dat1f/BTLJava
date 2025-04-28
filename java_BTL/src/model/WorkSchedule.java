package model;

import java.time.LocalDate;
import java.time.LocalTime;

import enums.Shift;

public class WorkSchedule {
    private int scheduleID;
    private Staff staff;
    private LocalDate workDate;
    private Shift shift;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String task;
    private String note;

    public WorkSchedule() {
    }

    public WorkSchedule(int scheduleID, Staff staff, LocalDate workDate, Shift shift, String note) {
        this.scheduleID = scheduleID;
        this.staff = staff;
        this.workDate = workDate;
        this.shift = shift;
        this.note = note;
    }

    // Getters
    public int getScheduleID() { return scheduleID; }
    public Staff getStaff() { return staff; }
    public int getStaffId() { return staff != null ? staff.getId() : 0; }
    public LocalDate getWorkDate() { return workDate; }
    public Shift getShift() { return shift; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getTask() { return task; }
    public String getNote() { return note; }

    // Setters
    public void setScheduleID(int scheduleID) { this.scheduleID = scheduleID; }
    public void setStaff(Staff staff) { this.staff = staff; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public void setShift(Shift shift) { this.shift = shift; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setLocation(String location) { this.location = location; }
    public void setTask(String task) { this.task = task; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return "WorkSchedule [scheduleID=" + scheduleID + ", staff=" + staff + ", workDate=" + workDate + ", shift="
                + shift + ", startTime=" + startTime + ", endTime=" + endTime + ", location=" + location + ", task=" + task
                + ", note=" + note + "]";
    }
}