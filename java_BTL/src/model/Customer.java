package model;


import java.sql.Timestamp;

import enums.GenderEnum;

public class Customer extends Person {
	private int point;
	private Timestamp created_at;

	public Customer() {
		super();
	}

	public Customer(int id, String fullName, GenderEnum gender, String phone, String address, String email, int point, Timestamp created_at) {
		super(id, fullName, gender, phone, address, email);
		this.point = point;
		this.created_at = created_at;
	}


	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public Timestamp getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Timestamp created_at) {
		this.created_at = created_at;
	}

	@Override
	public String toString() {
		return "Customer [point=" + point + ", created_at=" + created_at + "]";
	}
	

}