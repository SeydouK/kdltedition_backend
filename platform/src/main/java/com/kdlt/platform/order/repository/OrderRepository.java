package com.kdlt.platform.order.repository;

import com.kdlt.platform.order.entity.Order;
import com.kdlt.platform.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByDateCreationDesc(Long userId);

    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.status <> com.kdlt.platform.order.entity.OrderStatus.CANCELLED")
    BigDecimal getTotalRevenue();

    @Query(value = "SELECT to_char(date_trunc('month', date_creation), 'YYYY-MM') as month, " +
            "COALESCE(SUM(total_amount), 0) as revenue " +
            "FROM orders WHERE status <> 'CANCELLED' " +
            "AND date_creation >= (CURRENT_DATE - INTERVAL '6 months') " +
            "GROUP BY month ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyRevenueRaw();

}