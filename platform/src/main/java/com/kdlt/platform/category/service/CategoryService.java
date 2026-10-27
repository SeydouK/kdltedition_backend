package com.kdlt.platform.category.service;

import com.kdlt.platform.category.dto.CategoryDto;
import com.kdlt.platform.category.dto.CreateCategoryDto;
import com.kdlt.platform.category.entity.Category;
import com.kdlt.platform.category.repository.CategoryRepository;
import com.kdlt.platform.common.SlugUtils;
import com.kdlt.platform.exceptions.BadRequestException;
import com.kdlt.platform.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class CategoryService {

    // Tri alphabétique à la française : « Éducation » se classe parmi les E, pas après les Z.
    private static final Collator COLLATOR = Collator.getInstance(Locale.FRENCH);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDto createCategory(CreateCategoryDto dto) {
        String name = dto.getName().trim();

        if (categoryRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new BadRequestException("Une catégorie portant ce nom existe déjà.");
        }

        Category category = new Category();
        category.setName(name);
        category.setSlug(resolveSlug(dto.getSlug(), name));
        category.setDescription(cleanDescription(dto.getDescription()));

        return mapToDto(categoryRepository.save(category));
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getName, COLLATOR))
                .map(this::mapToDto)
                .toList();
    }

    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable."));
        return mapToDto(category);
    }

    public CategoryDto updateCategory(Long id, CreateCategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable."));

        String name = dto.getName().trim();

        categoryRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Une catégorie portant ce nom existe déjà.");
                });

        category.setName(name);
        category.setDescription(cleanDescription(dto.getDescription()));
        // Le slug ne change jamais après la création : il fait partie des adresses publiques.

        return mapToDto(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Catégorie introuvable.");
        }
        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "Cette catégorie contient des produits : déplacez-les ou supprimez-les d'abord.");
        }
    }

    // Slug fourni : on le vérifie. Sinon on le déduit du nom, avec un suffixe si besoin (sante, sante-2...).
    private String resolveSlug(String requested, String name) {
        if (requested != null && !requested.isBlank()) {
            String slug = requested.trim();
            if (categoryRepository.existsBySlug(slug)) {
                throw new BadRequestException("Ce slug de catégorie existe déjà.");
            }
            return slug;
        }

        String base = SlugUtils.slugify(name);
        if (base.isEmpty()) base = "categorie";

        String slug = base;
        int suffix = 2;
        while (categoryRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private String cleanDescription(String description) {
        if (description == null) return null;
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CategoryDto mapToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        return dto;
    }
}