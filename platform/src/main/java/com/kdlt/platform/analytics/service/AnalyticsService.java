package com.kdlt.platform.analytics.service;

import com.kdlt.platform.analytics.dto.DashboardSummaryDto;
import com.kdlt.platform.analytics.dto.RevenueByMonthDto;
import com.kdlt.platform.analytics.dto.TopProductDto;
import com.kdlt.platform.order.entity.OrderStatus;
import com.kdlt.platform.order.repository.OrderItemRepository;
import com.kdlt.platform.order.repository.OrderRepository;
import com.kdlt.platform.product.repository.ProductRepository;
import com.kdlt.platform.quote.entity.QuoteStatus;
import com.kdlt.platform.quote.repository.QuoteRepository;
import com.kdlt.platform.user.entity.Role;
import com.kdlt.platform.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final QuoteRepository quoteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public AnalyticsService(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            QuoteRepository quoteRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.quoteRepository = quoteRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public DashboardSummaryDto getDashboardSummary() {
        DashboardSummaryDto dto = new DashboardSummaryDto();

        dto.setTotalRevenue(orderRepository.getTotalRevenue());
        dto.setTotalOrders(orderRepository.count());
        dto.setPendingOrders(orderRepository.countByStatus(OrderStatus.PENDING));
        dto.setConfirmedOrders(orderRepository.countByStatus(OrderStatus.CONFIRMED));
        dto.setInProductionOrders(orderRepository.countByStatus(OrderStatus.IN_PRODUCTION));
        dto.setShippedOrders(orderRepository.countByStatus(OrderStatus.SHIPPED));
        dto.setDeliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED));
        dto.setCancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED));

        dto.setPendingQuotes(quoteRepository.countByStatus(QuoteStatus.PENDING));
        dto.setTotalQuotes(quoteRepository.count());

        dto.setTotalCustomers(userRepository.countByRole(Role.CUSTOMER));
        dto.setTotalActiveProducts(productRepository.countByActiveTrue());

        return dto;
    }

    public List<RevenueByMonthDto> getRevenueByMonth() {
        return orderRepository.getMonthlyRevenueRaw().stream()
                .map(row -> new RevenueByMonthDto((String) row[0], (BigDecimal) row[1]))
                .toList();
    }

    public List<TopProductDto> getTopProducts(int limit) {
        return orderItemRepository.findTopProducts(PageRequest.of(0, limit)).stream()
                .map(p -> new TopProductDto(p.getProductId(), p.getProductName(), p.getTotalQuantity()))
                .toList();
    }
}