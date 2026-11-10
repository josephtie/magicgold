package com.nectux.magicgold.catalog.repository;

import com.nectux.magicgold.catalog.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    List<ProductCategory> findByMerchantId(UUID merchantId);
}
