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
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kdlt.platform.order.event.OrderStatusChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.kdlt.platform.user.service.StaffDirectory;
import java.math.BigDecimal;
import java.util.List;
import com.kdlt.platform.order.dto.DeliveryAddressDto;
import com.kdlt.platform.order.dto.PlaceOrderRequest;
import com.kdlt.platform.order.entity.DeliveryAddress;
import java.math.RoundingMode;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ApplicationEventPublisher events;
    private final StaffDirectory staffDirectory;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ApplicationEventPublisher events,
                        StaffDirectory staffDirectory) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.events = events;
        this.staffDirectory = staffDirectory;
    }

    @Transactional
    public OrderDto placeOrder(User user, PlaceOrderRequest request) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Votre panier est vide."));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Votre panier est vide.");
        }

        DeliveryAddress delivery = buildDeliveryAddress(request);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(delivery);

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

    private DeliveryAddress buildDeliveryAddress(PlaceOrderRequest request) {
        boolean hasLatitude = request.getLatitude() != null;
        boolean hasLongitude = request.getLongitude() != null;
        if (hasLatitude != hasLongitude) {
            throw new BadRequestException("Les coordonnées GPS sont incomplètes.");
        }

        DeliveryAddress delivery = new DeliveryAddress();
        delivery.setPhone(request.getDeliveryPhone().trim());
        delivery.setAddress(request.getDeliveryAddress().trim());
        delivery.setCity(request.getDeliveryCity().trim());

        String notes = request.getDeliveryNotes() == null ? "" : request.getDeliveryNotes().trim();
        delivery.setNotes(notes.isEmpty() ? null : notes);

        if (hasLatitude) {
            delivery.setLatitude(request.getLatitude().setScale(6, RoundingMode.HALF_UP));
            delivery.setLongitude(request.getLongitude().setScale(6, RoundingMode.HALF_UP));
        }

        return delivery;
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
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "dateCreation")).stream()
                .map(this::mapToDto)
                .toList();
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('STAFF')")
    public OrderDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable."));
        return mapToDto(order);
    }


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

        // L'équipe n'est alertée que pour une nouvelle commande ; les autres étapes ne concernent que le client.
        List<String> staffEmails = order.getStatus() == OrderStatus.PENDING
                ? staffDirectory.activeStaffEmails()
                : List.of();

        DeliveryAddress address = order.getDeliveryAddress();
        OrderStatusChangedEvent.Delivery delivery = address == null ? null
                : new OrderStatusChangedEvent.Delivery(
                address.getPhone(),
                address.getAddress(),
                address.getCity(),
                address.getNotes(),
                address.getLatitude(),
                address.getLongitude());

        events.publishEvent(new OrderStatusChangedEvent(
                order.getId(),
                customer.getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                order.getStatus(),
                lines,
                order.getTotalAmount(),
                delivery,
                staffEmails));
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
        User customer = order.getUser();
        dto.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        dto.setCustomerEmail(customer.getEmail());

        DeliveryAddress delivery = order.getDeliveryAddress();
        if (delivery != null) {
            DeliveryAddressDto deliveryDto = new DeliveryAddressDto();
            deliveryDto.setPhone(delivery.getPhone());
            deliveryDto.setAddress(delivery.getAddress());
            deliveryDto.setCity(delivery.getCity());
            deliveryDto.setNotes(delivery.getNotes());
            deliveryDto.setLatitude(delivery.getLatitude());
            deliveryDto.setLongitude(delivery.getLongitude());
            dto.setDeliveryAddress(deliveryDto);
        }

        return dto;
    }
}