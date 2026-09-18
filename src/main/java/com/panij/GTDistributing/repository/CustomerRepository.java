package com.panij.GTDistributing.repository;

import com.panij.GTDistributing.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // Add this line to fix the compilation error:
    Optional<Customer> findByCustNo(Integer custNo);

    boolean existsByCustNo(Integer custNo);
    Optional<Customer> findByCustNoAndPassword(Integer custNo, String password);
    List<Customer> findByPassword(String password);
}