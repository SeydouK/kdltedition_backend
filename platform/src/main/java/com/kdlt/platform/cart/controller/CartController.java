package com.kdlt.platform.cart.controller;

import com.kdlt.platform.cart.dto.AddCartItemDto;
import com.kdlt.platform.cart.dto.CartDto;
import com.kdlt.platform.cart.service.CartService;
import com.kdlt.platform.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getMyCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getMyCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(@AuthenticationPrincipal User user,
                                           @Valid @RequestBody AddCartItemDto dto) {
        return ResponseEntity.ok(cartService.addItem(user, dto));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateItemQuantity(@AuthenticationPrincipal User user,
                                                      @PathVariable Long itemId,
                                                      @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(user, itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal User user,
                                           @PathVariable Long itemId) {
        cartService.removeItem(user, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}