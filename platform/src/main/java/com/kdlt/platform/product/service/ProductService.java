package com.kdlt.platform.product.service;

import com.kdlt.platform.category.entity.Category;
import com.kdlt.platform.category.entity.repository.CategoryRepository;
import com.kdlt.platform.exceptions.BadRequestException;
import com.kdlt.platform.exceptions.ResourceNotFoundException;
import com.kdlt.platform.product.dto.CreateProductDto;
import com.kdlt.platform.product.dto.ProductDto;
import com.kdlt.platform.product.dto.UpdateProductDto;
import com.kdlt.platform.product.entity.Product;
import com.kdlt.platform.product.entity.ProductType;
import com.kdlt.platform.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductDto createProduct(CreateProductDto dto) {
        if (productRepository.existBySlug(dto.getSlug())) {
            throw new BadRequestException("Ce slug de produit existe déjà.");
        }
        if (dto.getType() == ProductType.STANDARD && dto.getBasePrice() == null) {
            throw new BadRequestException("Un produit standard doit avoir un prix.");
        }
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable."));

        Product product = new Product();
        product.setCategory(category);
        product.setName(dto.getName());
        product.setSlug(dto.getSlug());
        product.setDescription(dto.getDescription());
        product.setType(dto.getType());
        product.setBasePrice(dto.getType() == ProductType.CUSTOM ? null : dto.getBasePrice());

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public List<ProductDto> getAllActiveProducts(){
        return productRepository.findByActiveTrue().stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<ProductDto> getProductsByCategory(Long categoryId){
        return productRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(this::mapToDto)
                .toList();
    }

    public ProductDto getProductBySlug(String slug){
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable."));
        return mapToDto(product);
    }

    public ProductDto updateProduct(Long id, UpdateProductDto dto){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable."));

        if(dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getType() != null) product.setType(dto.getType());
        if (dto.getBasePrice() != null) product.setBasePrice(dto.getBasePrice());
        if (dto.getActive() != null) product.setActive(dto.getActive());

        if (product.getType() == ProductType.STANDARD && product.getBasePrice() == null) {
            throw new BadRequestException("Un produit standard doit avoir un prix.");
        }

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produit introuvable.");
        }
        productRepository.deleteById(id);
    }

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setDescription(product.getDescription());
        dto.setType(product.getType());
        dto.setBasePrice(product.getBasePrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        return dto;
    }

}
