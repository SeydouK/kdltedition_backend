package com.kdlt.platform.cart.service;

import com.kdlt.platform.cart.dto.AddCartItemDto;
import com.kdlt.platform.cart.dto.CartDto;
import com.kdlt.platform.cart.dto.CartItemDto;
import com.kdlt.platform.cart.entity.Cart;
import com.kdlt.platform.cart.entity.CartItem;
import com.kdlt.platform.cart.repository.CartItemRepository;
import com.kdlt.platform.cart.repository.CartRepository;
import com.kdlt.platform.exceptions.BadRequestException;
import com.kdlt.platform.exceptions.ResourceNotFoundException;
import com.kdlt.platform.product.entity.Product;
import com.kdlt.platform.product.entity.ProductType;
import com.kdlt.platform.product.repository.ProductRepository;
import com.kdlt.platform.user.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartDto getMyCart(User user) {
        Cart cart = getOrCreateCart(user);
        return mapToDto(cart);
    }

    public CartDto addItem(User user, AddCartItemDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable."));

        if (product.getType() != ProductType.STANDARD) {
            throw new BadRequestException("Ce produit nécessite un devis et ne peut pas être ajouté au panier.");
        }

        if (!product.isActive()) {
            throw new BadRequestException("Ce produit n'est plus disponible.");
        }

        Cart cart = getOrCreateCart(user);

        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + dto.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            cartItemRepository.save(item);
        }

        return mapToDto(cartRepository.findById(cart.getId()).orElseThrow());
    }

    public CartDto updateItemQuantity(User user, Long itemId, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("La quantité doit être au moins 1.");
        }

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Article introuvable."));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Cet article n'appartient pas à votre panier.");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return mapToDto(item.getCart());
    }

    public void removeItem(User user, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Article introuvable."));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Cet article n'appartient pas à votre panier.");
        }

        cartItemRepository.delete(item);
    }

    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    private CartDto mapToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());

        List<CartItemDto> itemDtos = cart.getItems().stream().map(item -> {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setProductSlug(item.getProduct().getSlug());
            itemDto.setUnitPrice(item.getProduct().getBasePrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setLineTotal(item.getProduct().getBasePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            return itemDto;
        }).toList();

        dto.setItems(itemDtos);
        dto.setTotal(itemDtos.stream()
                .map(CartItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return dto;
    }
}