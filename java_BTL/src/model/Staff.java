package model;

import java.util.Date;

import enums.GenderEnum;

public class Staff extends Person {
	private Date dob;
	private double salary;
	private Date hire_date;
	private Account account;
    private Role role;
    

    public Staff() {
        super();
    }
    
    


	public Staff(int id, String fullName, GenderEnum gender, String phone, String address, String email, Date dob, double salary, Date hire_date, Account account, Role role) {
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

	@Override
	public String toString() {
		return "Staff [dob=" + dob + ", salary=" + salary + ", hire_date=" + hire_date + ", account=" + account
				+ ", role=" + role + "]";
	}

	


}