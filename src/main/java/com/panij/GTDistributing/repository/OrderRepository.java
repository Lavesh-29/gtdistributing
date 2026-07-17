package com.panij.GTDistributing.repository;

import com.panij.GTDistributing.model.OrderHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository
        extends JpaRepository<OrderHeader, Long> {
    List<OrderHeader> findByStatus(String status);
    List<OrderHeader> findAllByOrderByIdDesc();
}