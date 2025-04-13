package model;

public class DashboardSummary {
    private int totalCustomers;
    private int totalBookings;
    private int totalInvoices;
    private double totalRevenue;


    public DashboardSummary() {
		super();
	}


	public DashboardSummary(int totalCustomers, int totalBookings, int totalInvoices, double totalRevenue) {
        this.totalCustomers = totalCustomers;
        this.totalBookings = totalBookings;
        this.totalInvoices = totalInvoices;
        this.totalRevenue = totalRevenue;
    }


	public int getTotalCustomers() {
		return totalCustomers;
	}


	public void setTotalCustomers(int totalCustomers) {
		this.totalCustomers = totalCustomers;
	}


	public int getTotalBookings() {
		return totalBookings;
	}


	public void setTotalBookings(int totalBookings) {
		this.totalBookings = totalBookings;
	}


	public int getTotalInvoices() {
		return totalInvoices;
	}


	public void setTotalInvoices(int totalInvoices) {
		this.totalInvoices = totalInvoices;
	}


	public double getTotalRevenue() {
		return totalRevenue;
	}


	public void setTotalRevenue(double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}


	@Override
	public String toString() {
		return "DashboardSummary [totalCustomers=" + totalCustomers + ", totalBookings=" + totalBookings
				+ ", totalInvoices=" + totalInvoices + ", totalRevenue=" + totalRevenue + "]";
	}
	
	
	

}
