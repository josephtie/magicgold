package com.nectux.magicgold.catalog.repository;

import com.nectux.magicgold.catalog.domain.Product;
import com.nectux.magicgold.shared.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByMerchantId(UUID merchantId);
    List<Product> findByMerchantIdAndStatus(UUID merchantId, ProductStatus status);
    List<Product> findByMerchantIdAndCategoryId(UUID merchantId, UUID categoryId);
}
