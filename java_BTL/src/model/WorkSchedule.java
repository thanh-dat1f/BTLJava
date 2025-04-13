package model;

import java.time.LocalDate;

import enums.Shift;

public class WorkSchedule {
    private int scheduleID;
    private Staff staff;
    private LocalDate workDate;
    private Shift shift;
    private String note;
	public WorkSchedule(int scheduleID, Staff staff, LocalDate workDate, Shift shift, String note) {
		super();
		this.scheduleID = scheduleID;
		this.staff = staff;
		this.workDate = workDate;
		this.shift = shift;
		this.note = note;
	}
	public int getScheduleID() {
		return scheduleID;
	}
	public void setScheduleID(int scheduleID) {
		this.scheduleID = scheduleID;
	}
	public Staff getStaff() {
		return staff;
	}
	public void setStaff(Staff staff) {
		this.staff = staff;
	}
	public LocalDate getWorkDate() {
		return workDate;
	}
	public void setWorkDate(LocalDate workDate) {
		this.workDate = workDate;
	}
	public Shift getShift() {
		return shift;
	}
	public void setShift(Shift shift) {
		this.shift = shift;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	@Override
	public String toString() {
		return "WorkSchedule [scheduleID=" + scheduleID + ", staff=" + staff + ", workDate=" + workDate + ", shift="
				+ shift + ", note=" + note + "]";
	}
	

    
}
