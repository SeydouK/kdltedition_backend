package com.kdlt.platform.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateCategoryDto {

    @NotBlank(message = "Le nom de la catégorie est obligatoire.")
    @Size(max = 100, message = "Le nom est trop long (100 caractères maximum).")
    private String name;

    // Facultatif : s'il est absent, il est déduit du nom.
    @Size(max = 255, message = "Le slug est trop long (255 caractères maximum).")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Le slug ne peut contenir que des minuscules, des chiffres et des tirets.")
    private String slug;

    @Size(max = 1000, message = "La description est trop longue (1000 caractères maximum).")
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}