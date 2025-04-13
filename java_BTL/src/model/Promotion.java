package model;

import java.time.LocalDate;

public class Promotion {
	private int promotionId;
    private String code;
    private String description;
    private int discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    
    
	public Promotion() {
		super();
	}
	public Promotion(int promotionId, String code, String description, int discountPercent, LocalDate startDate,
			LocalDate endDate, boolean active) {
		super();
		this.promotionId = promotionId;
		this.code = code;
		this.description = description;
		this.discountPercent = discountPercent;
		this.startDate = startDate;
		this.endDate = endDate;
		this.active = active;
	}
	public int getPromotionId() {
		return promotionId;
	}
	public void setPromotionId(int promotionId) {
		this.promotionId = promotionId;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getDiscountPercent() {
		return discountPercent;
	}
	public void setDiscountPercent(int discountPercent) {
		this.discountPercent = discountPercent;
	}
	public LocalDate getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
	public LocalDate getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	@Override
	public String toString() {
		return "Promotion [promotionId=" + promotionId + ", code=" + code + ", description=" + description
				+ ", discountPercent=" + discountPercent + ", startDate=" + startDate + ", endDate=" + endDate
				+ ", active=" + active + "]";
	}
    
    
    
}
