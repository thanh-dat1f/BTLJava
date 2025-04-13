package model;

public class PetType {
    private int typePetID;
    private String species;
    private String breed;

    public PetType() {}

	public PetType(int typePetID, String species, String breed) {
		super();
		this.typePetID = typePetID;
		this.species = species;
		this.breed = breed;
	}

	public int getTypePetID() {
		return typePetID;
	}

	public void setTypePetID(int typePetID) {
		this.typePetID = typePetID;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getBreed() {
		return breed;
	}

	public void setBreed(String breed) {
		this.breed = breed;
	}

	@Override
	public String toString() {
		return "TypePet [typePetID=" + typePetID + ", species=" + species + ", breed=" + breed + "]";
	}

    
}
