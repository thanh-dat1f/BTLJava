package model;

import java.time.LocalDate;

import enums.GenderEnum;

public class Pet {
	private int petId;
    private String name;
    private PetType petType; 
    private GenderEnum gender;
    private LocalDate dob;
    private double weight;
    private String note;
    private Customer owner;

    public Pet() {}

	public Pet(int petId, String name, PetType petType, GenderEnum gender, LocalDate dob, double weight, String note,
			Customer owner) {
		super();
		this.petId = petId;
		this.name = name;
		this.petType = petType;
		this.gender = gender;
		this.dob = dob;
		this.weight = weight;
		this.note = note;
		this.owner = owner;
	}

	public int getPetId() {
		return petId;
	}

	public void setPetId(int petId) {
		this.petId = petId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PetType getTypePet() {
		return petType;
	}

	public void setTypePet(PetType petType) {
		this.petType = petType;
	}

	public GenderEnum getGender() {
		return gender;
	}

	public void setGender(GenderEnum gender) {
		this.gender = gender;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Customer getOwner() {
		return owner;
	}

	public void setOwner(Customer owner) {
		this.owner = owner;
	}

	@Override
	public String toString() {
		return "Pet [petId=" + petId + ", name=" + name + ", typePet=" + petType + ", gender=" + gender + ", dob=" + dob
				+ ", weight=" + weight + ", note=" + note + ", owner=" + owner + "]";
	}

    
}
