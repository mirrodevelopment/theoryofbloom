package com.theoryofbloom.repository;

import com.theoryofbloom.model.Order;
import com.theoryofbloom.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.user = :user ORDER BY o.createdAt DESC")
    List<Order> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);

    List<Order> findByStatus(String status);

    List<Order> findByReturnStatus(String returnStatus);
}

