package com.kdlt.platform.product.dto;

import com.kdlt.platform.product.entity.ProductType;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateProductDto {

    @Size(max = 255, message = "Le nom est trop long (255 caractères maximum).")
    private String name;

    @Size(max = 2000, message = "La description est trop longue (2000 caractères maximum).")
    private String description;

    private ProductType type;

    @PositiveOrZero(message = "Le prix ne peut pas être négatif.")
    private BigDecimal basePrice;

    private Boolean active;
    private Long categoryId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProductType getType() { return type; }
    public void setType(ProductType type) { this.type = type; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}