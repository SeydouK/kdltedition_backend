package com.kdlt.platform.order.repository;

import com.kdlt.platform.order.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select oi.productId as productId, oi.productName as productName, " +
            "sum(oi.quantity) as totalQuantity " +
            "from OrderItem oi group by oi.productId, oi.productName " +
            "order by sum(oi.quantity) desc")
    List<TopProductProjection> findTopProducts(Pageable pageable);

    interface TopProductProjection {
        Long getProductId();
        String getProductName();
        Long getTotalQuantity();
    }
}