package model;


public class Service {
	private int serviceId;
    private String name;
    private String description;
    private double price;
    private int durationMinutes;
    private boolean active;

    public Service() {
        super();
    }

	public Service(int serviceId, String name, String description, double price, int durationMinutes, boolean active) {
		super();
		this.serviceId = serviceId;
		this.name = name;
		this.description = description;
		this.price = price;
		this.durationMinutes = durationMinutes;
		this.active = active;
	}

	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "Service [serviceId=" + serviceId + ", name=" + name + ", description=" + description + ", price="
				+ price + ", durationMinutes=" + durationMinutes + ", active=" + active + "]";
	}

}
