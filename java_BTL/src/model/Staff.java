package model;

import java.time.LocalDate;
import java.util.Date;

import enums.GenderEnum;

public class Staff extends Person {
	private Date dob;
	private double salary;
	private Date hire_date;
	private Account account;
    private Role role;
    private LocalDate startDate;
    private LocalDate endDate;
    private String workShift;
    private String position;
    

    public Staff() {
        super();
    }
    
    // Constructor matching the one used in StaffController
    public Staff(int id, String fullName, GenderEnum gender, String phone, String address, String email, 
                Account account, Role role, LocalDate startDate, LocalDate endDate, double salary, 
                String workShift, String position) {
        super(id, fullName, gender, phone, address, email);
        this.account = account;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
        this.salary = salary;
        this.workShift = workShift;
        this.position = position;
        
        // Convert LocalDate to Date for hire_date if startDate is provided
        if (startDate != null) {
            this.hire_date = java.sql.Date.valueOf(startDate);
        }
    }
    
    // Original constructor (keeping for compatibility)
    public Staff(int id, String fullName, GenderEnum gender, String phone, String address, String email, Date dob, 
                double salary, Date hire_date, Account account, Role role) {
        super(id, fullName, gender, phone, address, email);
        this.dob = dob;
        this.salary = salary;
        this.hire_date = hire_date;
        this.account = account;
        this.role = role;
    }

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public Date getHire_date() {
		return hire_date;
	}

	public void setHire_date(Date hire_date) {
		this.hire_date = hire_date;
	}
	
	public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        if (startDate != null) {
            this.hire_date = java.sql.Date.valueOf(startDate);
        }
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getWorkShift() {
        return workShift;
    }

    public void setWorkShift(String workShift) {
        this.workShift = workShift;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

	@Override
	public String toString() {
		return "Staff [dob=" + dob + ", salary=" + salary + ", hire_date=" + hire_date 
				+ ", account=" + account + ", role=" + role + ", startDate=" + startDate 
				+ ", endDate=" + endDate + ", workShift=" + workShift + ", position=" + position + "]";
	}
}