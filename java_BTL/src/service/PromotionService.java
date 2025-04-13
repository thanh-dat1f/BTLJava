package service;

import model.Promotion;
import repository.PromotionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PromotionService {
    private final PromotionRepository promotionRepository;
    
    public PromotionService() {
        this.promotionRepository = PromotionRepository.getInstance();
    }
    
    public List<Promotion> getAllPromotions() {
        return promotionRepository.selectAll();
    }
    
    public List<Promotion> getActivePromotions() {
        LocalDate today = LocalDate.now();
        return promotionRepository.selectAll().stream()
                .filter(p -> p.isActive() && 
                       (p.getEndDate() == null || !p.getEndDate().isBefore(today)) &&
                       (p.getStartDate() == null || !p.getStartDate().isAfter(today)))
                .collect(Collectors.toList());
    }
    
    public Promotion getPromotionById(int promotionId) {
        return promotionRepository.selectById(promotionId);
    }
    
    public Promotion getPromotionByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        String condition = "code = ?";
        List<Promotion> promotions = promotionRepository.selectByCondition(condition, code.trim());
        
        if (promotions.isEmpty()) {
            return null;
        }
        
        return promotions.get(0);
    }
    
    public boolean createPromotion(Promotion promotion) {
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion cannot be null");
        }
        
        validatePromotion(promotion);
        
        return promotionRepository.insert(promotion) > 0;
    }
    
    public boolean updatePromotion(Promotion promotion) {
        if (promotion == null || promotion.getPromotionId() <= 0) {
            throw new IllegalArgumentException("Invalid promotion");
        }
        
        validatePromotion(promotion);
        
        return promotionRepository.update(promotion) > 0;
    }
    
    public boolean deletePromotion(int promotionId) {
        Promotion promotion = new Promotion();
        promotion.setPromotionId(promotionId);
        return promotionRepository.delete(promotion) > 0;
    }
    
    private void validatePromotion(Promotion promotion) {
        if (promotion.getCode() == null || promotion.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Promotion code cannot be empty");
        }
        
        if (promotion.getDiscountPercent() <= 0 || promotion.getDiscountPercent() > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 1 and 100");
        }
        
        if (promotion.getStartDate() != null && promotion.getEndDate() != null) {
            if (promotion.getStartDate().isAfter(promotion.getEndDate())) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
        }
    }
}