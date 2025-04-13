package model;

import enums.GenderEnum;

public class Person {
    protected int id;
    protected String fullName;
    protected GenderEnum gender;
    protected String phone;
    protected String address;
    protected String email; 

	public Person() {}

    
	public Person(int id, String fullName, GenderEnum gender, String phone, String address, String email) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.gender = gender;
		this.phone = phone;
		this.address = address;
		this.email = email;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public GenderEnum getGender() {
		return gender;
	}

	public void setGender(GenderEnum gender) {
		this.gender = gender;
	}


	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}



	public String getFullName() {
		return fullName;
	}



	public void setFullName(String fullName) {
		this.fullName = fullName;
	}



	public String getPhone() {
		return phone;
	}



	public void setPhone(String phone) {
		this.phone = phone;
	}



	@Override
	public String toString() {
		return "Person [id=" + id + ", fullName=" + fullName + ", gender=" + gender + ", phone=" + phone + ", address="
				+ address + ", email=" + email + "]";
	}


    
}