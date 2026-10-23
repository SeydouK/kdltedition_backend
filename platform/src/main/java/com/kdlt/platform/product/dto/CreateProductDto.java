package com.kdlt.platform.product.dto;

import com.kdlt.platform.product.entity.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateProductDto {

    @NotNull(message = "La catégorie est obligatoire.")
    private Long categoryId;

    @NotBlank(message = "Le nom du produit est obligatoire.")
    @Size(max = 255, message = "Le nom est trop long (255 caractères maximum).")
    private String name;

    @NotBlank(message = "Le slug est obligatoire.")
    @Size(max = 255, message = "Le slug est trop long (255 caractères maximum).")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Le slug ne peut contenir que des minuscules, des chiffres et des tirets.")
    private String slug;

    @Size(max = 2000, message = "La description est trop longue (2000 caractères maximum).")
    private String description;

    @NotNull(message = "Le type de produit est obligatoire.")
    private ProductType type;

    @PositiveOrZero(message = "Le prix ne peut pas être négatif.")
    private BigDecimal basePrice;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProductType getType() { return type; }
    public void setType(ProductType type) { this.type = type; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
}