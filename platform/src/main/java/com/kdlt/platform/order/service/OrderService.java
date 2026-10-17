package com.kdlt.platform.order.service;

import com.kdlt.platform.cart.entity.Cart;
import com.kdlt.platform.cart.repository.CartRepository;
import com.kdlt.platform.exceptions.BadRequestException;
import com.kdlt.platform.exceptions.ResourceNotFoundException;
import com.kdlt.platform.order.dto.OrderDto;
import com.kdlt.platform.order.dto.OrderItemDto;
import com.kdlt.platform.order.entity.Order;
import com.kdlt.platform.order.entity.OrderItem;
import com.kdlt.platform.order.entity.OrderStatus;
import com.kdlt.platform.order.repository.OrderRepository;
import com.kdlt.platform.user.entity.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kdlt.platform.order.event.OrderStatusChangedEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ApplicationEventPublisher events;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ApplicationEventPublisher events) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.events = events;
    }

    @Transactional
    public OrderDto placeOrder(User user) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Votre panier est vide."));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Votre panier est vide.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (var cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProduct().getId());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setUnitPrice(cartItem.getProduct().getBasePrice());
            orderItem.setQuantity(cartItem.getQuantity());

            total = total.add(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            order.getItems().add(orderItem);
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);
        publishStatusChange(saved);

        return mapToDto(saved);
    }

    public List<OrderDto> getMyOrders(User user) {
        return orderRepository.findByUserIdOrderByDateCreationDesc(user.getId()).stream()
                .map(this::mapToDto)
                .toList();
    }

    public OrderDto getMyOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable."));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Commande introuvable.");
        }

        return mapToDto(order);
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('STAFF')")
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('STAFF')")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('STAFF')")
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable."));

        OrderStatus current = order.getStatus();
        if (!current.canTransitionTo(newStatus)) {
            throw new BadRequestException(
                    "Impossible de passer cette commande du statut " + current + " au statut " + newStatus + ".");
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        publishStatusChange(saved);
        return mapToDto(saved);
    }

    private void publishStatusChange(Order order) {
        User customer = order.getUser();

        List<OrderStatusChangedEvent.Line> lines = order.getItems().stream()
                .map(item -> new OrderStatusChangedEvent.Line(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        events.publishEvent(new OrderStatusChangedEvent(
                order.getId(),
                customer.getEmail(),
                customer.getFirstName(),
                order.getStatus(),
                lines,
                order.getTotalAmount()));
    }

    private void publishStatusChange(Order order) {
        User customer = order.getUser();

        List<OrderStatusChangedEvent.Line> lines = order.getItems().stream()
                .map(item -> new OrderStatusChangedEvent.Line(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        events.publishEvent(new OrderStatusChangedEvent(
                order.getId(),
                customer.getEmail(),
                customer.getFirstName(),
                order.getStatus(),
                lines,
                order.getTotalAmount()));
    }

    private OrderDto mapToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setDateCreation(order.getDateCreation());

        dto.setItems(order.getItems().stream().map(item -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setProductId(item.getProductId());
            itemDto.setProductName(item.getProductName());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            return itemDto;
        }).toList());

        return dto;
    }
}