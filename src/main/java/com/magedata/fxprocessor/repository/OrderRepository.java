package com.magedata.fxprocessor.repository;

import com.magedata.fxprocessor.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") UUID id);

    @Query("SELECT COUNT(o) AS totalOrders, " +
           "COALESCE(SUM(o.netTotal), 0) AS totalSpend, " +
           "COALESCE(SUM(o.feeAmount), 0) AS cumulativeFeesPaid " +
           "FROM OrderEntity o WHERE o.customerId = :customerId")
    CustomerSummaryProjection findSummaryByCustomerId(@Param("customerId") String customerId);
}
