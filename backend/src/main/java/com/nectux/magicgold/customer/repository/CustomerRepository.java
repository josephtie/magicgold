package com.nectux.magicgold.customer.repository;

import com.nectux.magicgold.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findByMerchantId(UUID merchantId);
    Optional<Customer> findByMerchantIdAndPhoneNumber(UUID merchantId, String phoneNumber);
}
