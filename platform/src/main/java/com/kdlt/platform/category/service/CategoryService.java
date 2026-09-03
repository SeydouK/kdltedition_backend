package com.kdlt.platform.category.service;

import com.kdlt.platform.category.dto.CategoryDto;
import com.kdlt.platform.category.dto.CreateCategoryDto;
import com.kdlt.platform.category.entity.Category;
import com.kdlt.platform.category.entity.repository.CategoryRepository;
import com.kdlt.platform.exceptions.BadRequestException;
import com.kdlt.platform.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public CategoryDto createCategory(CreateCategoryDto dto) {
        if (categoryRepository.existsBySlug(dto.getSlug())){
            throw new BadRequestException("Ce slug de catégorie existe déjà.");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());

        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    public List<CategoryDto> getAllCategories(){
        return categoryRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();

    }

    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable."));
        return mapToDto(category);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Catégorie introuvable.");
        }
        categoryRepository.deleteById(id);
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
